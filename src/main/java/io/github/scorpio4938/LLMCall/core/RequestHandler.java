package io.github.scorpio4938.LLMCall.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.github.scorpio4938.LLMCall.config.RetryConfig;
import io.github.scorpio4938.LLMCall.core.builder.LLMRequestBuilder;
import io.github.scorpio4938.LLMCall.core.messages.LLMRequest;
import io.github.scorpio4938.LLMCall.core.messages.LLMResponseException;
import io.github.scorpio4938.LLMCall.core.messages.prompts.Prompt;
import io.github.scorpio4938.LLMCall.core.providers.Provider;
import io.github.scorpio4938.LLMCall.service.debug.Debugger;
import io.github.scorpio4938.LLMCall.service.utils.MapSorter;

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

    private final Provider provider;
    private final HttpClient client;

    public RequestHandler(Provider provider, HttpClient client) {
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
            throw new IllegalArgumentException("Invalid model");
        }
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Message data required");
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
                buildHttpRequest(requestBody),
                HttpResponse.BodyHandlers.ofString());
        validateResponse(response);
        return response.body();
    }

    private HttpRequest buildHttpRequest(String requestBody) {
        return HttpRequest.newBuilder()
                .uri(URI.create(provider.getUrl()))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + provider.getKey())
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
    }

    /**
     * Sends the request to the LLM provider with retry
     * 
     * @param requestBody The request body
     * @param retryConfig The retry configuration
     * @return The response body
     * 
     * @since 1.0.2
     */
    public String sendRequestWithRetry(String requestBody, RetryConfig retryConfig)
            throws Exception {
        HttpRequest request = buildHttpRequest(requestBody);
        Exception lastError = null;

        for (int attempt = 1; attempt <= retryConfig.getMaxRetries() + 1; attempt++) {
            try {
                Debugger.log("Attempt %d/%d to: %s".formatted(
                        attempt, retryConfig.getMaxRetries() + 1, provider.getUrl()));
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                validateResponse(response);
                return response.body();
            } catch (Exception e) {
                lastError = e;
                if (shouldRetryRequest(e, attempt, retryConfig.getMaxRetries())) {
                    Debugger.log("Retrying in %dms...".formatted(retryConfig.getRetryDelayMillis()));
                    Thread.sleep(retryConfig.getRetryDelayMillis());
                }
            }
        }
        throw lastError;
    }

    private boolean shouldRetryRequest(Exception e, int attempt, int maxRetries) {
        return attempt <= maxRetries && shouldRetry(e);
    }

    /**
     * Determines if the request should be retried
     * 
     * @param e The exception
     * @return True if the request should be retried, false otherwise
     * 
     * @since 1.0.2
     */
    private boolean shouldRetry(Exception e) {
        if (e instanceof LLMResponseException) {
            int statusCode = ((LLMResponseException) e).getStatusCode();
            return statusCode == 429 || (statusCode >= 500 && statusCode < 600);
        }
        return e instanceof java.io.IOException;
    }

    private void validateResponse(HttpResponse<String> response) {
        if (response.statusCode() >= 400) {
            throw new LLMResponseException(response);
        }
    }
}