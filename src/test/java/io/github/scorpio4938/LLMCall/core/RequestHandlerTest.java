package io.github.scorpio4938.LLMCall.core;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.github.scorpio4938.LLMCall.core.builder.LLMRequestBuilder;
import io.github.scorpio4938.LLMCall.core.messages.prompts.BasicPrompt;
import io.github.scorpio4938.LLMCall.core.providers.Provider;
import io.github.scorpio4938.LLMCall.core.retry.DefaultRetry;
import io.github.scorpio4938.LLMCall.service.exceptions.message.LLMResponseException;
import io.github.scorpio4938.LLMCall.service.retry.RetryableErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RequestHandlerTest {
        private Provider testProvider;
        private RequestHandler handler;
        private HttpClient mockClient;

        @BeforeEach
        void setUp() {
                testProvider = mock(Provider.class);
                when(testProvider.getUrl()).thenReturn("http://test-provider");
                when(testProvider.getKey()).thenReturn("test-key");
                when(testProvider.getModel("test-model")).thenReturn("test-model");

                mockClient = mock(HttpClient.class);
                handler = new RequestHandler(testProvider, mockClient);
        }

        @Test
        void shouldBuildValidRequestBody() {
                LLMRequestBuilder builder = new LLMRequestBuilder("test-model")
                                .withData(Map.of("user", "Hello", "assistant", "Hi"))
                                .withPrompt(new BasicPrompt())
                                .withParams(Map.of("temperature", 0.7));

                String json = handler.buildRequest(builder);
                JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

                assertEquals("test-model", jsonObject.get("model").getAsString());
                assertTrue(jsonObject.get("messages").getAsJsonArray().size() > 0);
                assertEquals(0.7, jsonObject.get("temperature").getAsDouble());
        }

        @Test
        void shouldSortMessagesWithPromptFirst() {
                LLMRequestBuilder builder = new LLMRequestBuilder("test-model")
                                .withData(Map.of("user", "Hello", "assistant", "Hi"))
                                .withPrompt(new BasicPrompt("system", "Be helpful"));

                String json = handler.buildRequest(builder);
                JsonObject firstMessage = JsonParser.parseString(json)
                                .getAsJsonObject()
                                .get("messages").getAsJsonArray()
                                .get(0).getAsJsonObject();

                assertEquals("system", firstMessage.get("role").getAsString());
                assertEquals("Be helpful", firstMessage.get("content").getAsString());
        }

        @Test
        void shouldThrowOnInvalidInput() {
                assertThrows(IllegalArgumentException.class, () -> handler.buildRequest(new LLMRequestBuilder("")));

                assertThrows(IllegalArgumentException.class,
                                () -> handler.buildRequest(new LLMRequestBuilder("model").withData(null)));
        }

        @Test
        @SuppressWarnings("unchecked")
        void shouldRetryOnServerError() throws Exception {
                // Mock HTTP responses - first fails with 503
                HttpResponse<String> failedResponse = mock(HttpResponse.class);
                when(failedResponse.statusCode()).thenReturn(503);
                when(failedResponse.body()).thenReturn("Service Unavailable");

                // Second response succeeds with 200
                HttpResponse<String> successResponse = mock(HttpResponse.class);
                when(successResponse.statusCode()).thenReturn(200);
                when(successResponse.body()).thenReturn("{\"choices\":[{\"message\":{\"content\":\"Success\"}}]}");

                // Set up client to return failed response first, then success
                when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                                .thenReturn(failedResponse)
                                .thenReturn(successResponse);

                // Create builder with retry config
                LLMRequestBuilder builder = new LLMRequestBuilder("test-model")
                                .withData(Map.of("user", "Hello"))
                                .withRetryConfig(new DefaultRetry(1, 10)); // 1 retry with 10ms delay

                // Execute request
                String result = handler.sendRequestWithRetry("test-body", builder);

                // Verify client was called twice
                verify(mockClient, times(2)).send(any(), any());
                assertEquals("{\"choices\":[{\"message\":{\"content\":\"Success\"}}]}", result);
        }

        @Test
        @SuppressWarnings("unchecked")
        void shouldUseExponentialBackoff() throws Exception {
                // Mock HTTP responses - all fail with 429 (rate limit)
                HttpResponse<String> rateLimitResponse = mock(HttpResponse.class);
                when(rateLimitResponse.statusCode()).thenReturn(429);
                when(rateLimitResponse.body()).thenReturn("Rate limit exceeded");

                // Set up client to always return rate limit response
                when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                                .thenReturn(rateLimitResponse);

                // Create builder with exponential backoff
                DefaultRetry backoffConfig = DefaultRetry.withExponentialBackoff(2, 100, 2.0);
                LLMRequestBuilder builder = new LLMRequestBuilder("test-model")
                                .withData(Map.of("user", "Hello"))
                                .withRetryConfig(backoffConfig);

                // Execute request (will fail after all retries)
                Exception exception = assertThrows(LLMResponseException.class,
                                () -> handler.sendRequestWithRetry("test-body", builder));

                // Verify client was called 3 times (initial + 2 retries)
                verify(mockClient, times(3)).send(any(), any());
                assertEquals(429, ((LLMResponseException) exception).getStatusCode());

                // Verify the backoff delays were used correctly
                assertEquals(100, backoffConfig.getDelayForAttempt(1));
                assertEquals(200, backoffConfig.getDelayForAttempt(2));
                assertEquals(400, backoffConfig.getDelayForAttempt(3));
        }

        @Test
        void shouldClassifyErrorsCorrectly() throws Exception {
                // Create different exception types
                LLMResponseException rateLimitException = new LLMResponseException("Rate limit", 429);
                LLMResponseException serverErrorException = new LLMResponseException("Server error", 503);
                LLMResponseException clientErrorException = new LLMResponseException("Bad request", 400);
                IOException networkException = new IOException("Network error");

                // Test error classification using reflection to access private method
                java.lang.reflect.Method getErrorTypeMethod = RequestHandler.class.getDeclaredMethod(
                                "getErrorType", Exception.class);
                getErrorTypeMethod.setAccessible(true);

                assertEquals(RetryableErrorType.RATE_LIMIT,
                                getErrorTypeMethod.invoke(handler, rateLimitException));
                assertEquals(RetryableErrorType.SERVER_ERROR,
                                getErrorTypeMethod.invoke(handler, serverErrorException));
                assertEquals(RetryableErrorType.OTHER,
                                getErrorTypeMethod.invoke(handler, clientErrorException));
                assertEquals(RetryableErrorType.NETWORK_ERROR,
                                getErrorTypeMethod.invoke(handler, networkException));
        }

        @Test
        @SuppressWarnings("unchecked")
        void shouldNotRetryClientErrors() throws Exception {
                // Mock HTTP response with 400 error
                HttpResponse<String> clientErrorResponse = mock(HttpResponse.class);
                when(clientErrorResponse.statusCode()).thenReturn(400);
                when(clientErrorResponse.body()).thenReturn("Bad request");

                // Set up client to return client error
                when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                                .thenReturn(clientErrorResponse);

                // Create builder with retry config
                LLMRequestBuilder builder = new LLMRequestBuilder("test-model")
                                .withData(Map.of("user", "Hello"))
                                .withRetryConfig(new DefaultRetry(3, 10)); // 3 retries with 10ms delay

                // Execute request (should fail immediately without retrying)
                Exception exception = assertThrows(LLMResponseException.class,
                                () -> handler.sendRequestWithRetry("test-body", builder));

                // Verify client was called only once (no retries for client errors)
                verify(mockClient, times(1)).send(any(), any());
                assertEquals(400, ((LLMResponseException) exception).getStatusCode());
        }
}