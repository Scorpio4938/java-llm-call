package io.github.scorpio4938.LLMCall.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.github.scorpio4938.LLMCall.core.builder.LLMRequestBuilder;
import io.github.scorpio4938.LLMCall.core.messages.LLMRequest;
import io.github.scorpio4938.LLMCall.core.messages.prompts.Prompt;
import io.github.scorpio4938.LLMCall.core.providers.IProvider;
import io.github.scorpio4938.LLMCall.core.retry.DefaultRetry;
import io.github.scorpio4938.LLMCall.service.debug.Debugger;
import io.github.scorpio4938.LLMCall.service.utils.MapSorter;
import io.github.scorpio4938.LLMCall.service.retry.RetryableErrorType;
import io.github.scorpio4938.LLMCall.service.exceptions.llm.LLMErrorCode;
import io.github.scorpio4938.LLMCall.service.exceptions.llm.LLMException;
import io.github.scorpio4938.LLMCall.service.exceptions.llm.LLMValidationException;
import io.github.scorpio4938.LLMCall.service.exceptions.message.LLMResponseException;
import io.github.scorpio4938.LLMCall.service.exceptions.llm.RetryException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Handles the request to the LLM provider
 * 
 * @since 1.0.2
 */
public class RequestHandler {
    private static final Gson GSON = new GsonBuilder().create();

    private final IProvider provider;
    private final HttpClient client;

    public RequestHandler(IProvider provider, HttpClient client) {
        this.provider = Objects.requireNonNull(provider);
        this.client = Objects.requireNonNull(client);
    }

    /**
     * Builds the request to the LLM provider
     * 
     * @param builder The builder to build the request
     * @return The request body
     * 
     * @since 1.0.2
     */
    public String buildRequest(LLMRequestBuilder builder) {
        validateRequest(builder);

        JsonObject body = new JsonObject();
        body.addProperty("model", provider.getModel(builder.getModel()));
        body.add("messages", buildMessages(builder));
        builder.getParams().forEach((k, v) -> body.add(k, GSON.toJsonTree(v)));

        return GSON.toJson(body);
    }

    private JsonElement buildMessages(LLMRequestBuilder builder) {
        List<LLMRequest.Message> messages = new ArrayList<>();
        if (builder.getPrompt() != null) {
            messages.add(new LLMRequest.Message(builder.getPrompt().getRole(),
                    builder.getPrompt().getContent()));
        }
        builder.getData().forEach((role, content) -> messages.add(new LLMRequest.Message(role, content)));
        return GSON.toJsonTree(messages);
    }

    private void validateRequest(LLMRequestBuilder builder) {
        String model = builder.getModel();
        Map<String, String> data = builder.getData();

        if (model == null || model.trim().isEmpty()) {
            throw new LLMValidationException("model", model);
        }
        if (data == null || data.isEmpty()) {
            throw new LLMValidationException("data", data);
        }
    }

    /**
     * Sends the request to the LLM provider
     * 
     * @param requestBody The request body
     * @return The response body
     * 
     * @since 1.0.2
     */
    public String send(String requestBody) throws Exception {
        HttpResponse<String> response = client.send(
                buildHttpRequest(requestBody, Duration.ofSeconds(30)),
                HttpResponse.BodyHandlers.ofString());
        validateResponse(response);
        return response.body();
    }

    /**
     * Sends the request with retry based on builder configuration
     * 
     * @param requestBody The request body
     * @param builder     The request builder containing retry configuration
     * @return The response body as a string
     * @throws Exception if all retry attempts fail
     * 
     * @since 1.0.2
     */
    public String sendRequestWithRetry(String requestBody, LLMRequestBuilder builder)
            throws LLMException {
        DefaultRetry retryConfig = builder.getRetryConfig();
        HttpRequest request = buildHttpRequest(requestBody, retryConfig.getConnectionTimeout());
        Exception lastError = null;
        int attemptsMade = 0;

        for (int attempt = 1; attempt <= retryConfig.getMaxRetries() + 1; attempt++) {
            attemptsMade = attempt;
            boolean isLastAttempt = attempt > retryConfig.getMaxRetries();
            try {
                Debugger.log("Attempt %d/%d to: %s".formatted(
                        attempt, retryConfig.getMaxRetries() + 1, provider.getUrl()));
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                validateResponse(response);
                Debugger.log("Request succeeded on attempt " + attempt);
                return response.body();
            } catch (Exception e) {
                lastError = e;
                if (isLastAttempt) {
                    Debugger.log("Final attempt failed: " + e.getMessage());
                    break;
                }

                if (shouldRetry(e)) {
                    long delayMillis = retryConfig.getDelayForAttempt(attempt);
                    Debugger.log("Attempt %d failed: %s. Retrying in %dms...".formatted(
                            attempt, e.getMessage(), delayMillis));
                    try {
                        Thread.sleep(delayMillis);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new LLMException(LLMErrorCode.GENERAL_ERROR, "Retry interrupted", ie);
                    }
                } else {
                    Debugger.log("Non-retryable error: " + e.getMessage());
                    break;
                }
            }
        }

        boolean retriesExhausted = attemptsMade == (retryConfig.getMaxRetries() + 1);
        if (retriesExhausted) {
            throw new RetryException(
                    retryConfig.getMaxRetries(),
                    retryConfig.getDelayForAttempt(retryConfig.getMaxRetries()),
                    lastError);
        } else {
            if (lastError instanceof LLMException) {
                throw (LLMException) lastError;
            }
            throw new LLMException(LLMErrorCode.GENERAL_ERROR, "Request failed after retries", lastError);
        }
    }

    private HttpRequest buildHttpRequest(String requestBody, Duration timeout) {
        return HttpRequest.newBuilder()
                .uri(URI.create(provider.getUrl()))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + provider.getKey())
                .timeout(timeout)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
    }

    /**
     * Gets the type of error from an exception
     * 
     * @param e The exception
     * @return The error type
     */
    private RetryableErrorType getErrorType(Exception e) {
        if (e instanceof LLMResponseException) {
            int statusCode = ((LLMResponseException) e).getStatusCode();
            if (statusCode == 429) {
                return RetryableErrorType.RATE_LIMIT;
            } else if (statusCode >= 500 && statusCode < 600) {
                return RetryableErrorType.SERVER_ERROR;
            }
        } else if (e instanceof java.io.IOException) {
            return RetryableErrorType.NETWORK_ERROR;
        } else if (e instanceof java.util.concurrent.TimeoutException) {
            return RetryableErrorType.TIMEOUT;
        }
        return RetryableErrorType.OTHER;
    }

    /**
     * Determines if the request should be retried based on the exception type
     * 
     * @param e The exception that occurred
     * @return True if the request should be retried, false otherwise
     * 
     * @since 1.0.2
     */
    private boolean shouldRetry(Exception e) {
        RetryableErrorType errorType = getErrorType(e);
        Debugger.log("Error type: " + errorType);
        return errorType.isRetryable();
    }

    private void validateResponse(HttpResponse<String> response) {
        if (response.statusCode() >= 400) {
            throw new LLMResponseException(
                    "HTTP error: " + response.statusCode(),
                    response.statusCode(),
                    response.body());
        }
    }
}