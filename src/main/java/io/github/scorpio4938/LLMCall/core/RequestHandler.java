package io.github.scorpio4938.LLMCall.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
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

public class RequestHandler {
    private static final Gson GSON = new Gson();
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    private final Provider provider;
    private final HttpClient client;

    public RequestHandler(Provider provider, HttpClient client) {
        this.provider = Objects.requireNonNull(provider);
        this.client = Objects.requireNonNull(client);
    }

    public String buildRequest(LLMRequestBuilder builder) {
        String model = builder.getModel();
        Map<String, String> data = builder.getData();

        validate(model, data);

        List<LLMRequest.Message> messages = new ArrayList<>();
        if (builder.getPrompt() != null) {
            messages.add(createMessage(builder.getPrompt()));
        }

        data.forEach((role, content) -> messages.add(new LLMRequest.Message(role, content)));

        JsonObject body = new JsonObject();
        body.addProperty("model", provider.getModel(model));
        body.add("messages", GSON.toJsonTree(messages));
        builder.getParams().forEach((k, v) -> body.add(k, GSON.toJsonTree(v)));

        return GSON.toJson(body);
    }

    public String send(String requestBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(provider.getUrl()))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + provider.getKey())
                .timeout(DEFAULT_TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new LLMResponseException(response);
        }
        return response.body();
    }

    private void validate(String model, Map<String, String> data) {
        if (model == null || model.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid model");
        }
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Message data required");
        }
    }

    private LLMRequest.Message createMessage(Prompt prompt) {
        return new LLMRequest.Message(prompt.getRole(), prompt.getContent());
    }

    public String sendRequestWithRetry(String requestBody, int maxRetries, long retryDelayMillis) throws Exception {
        HttpRequest request = buildBaseRequest(requestBody);
        int totalAttempts = maxRetries + 1;
        Exception lastError = null;

        for (int attempt = 1; attempt <= totalAttempts; attempt++) {
            try {
                Debugger.log("Attempt %d/%d to: %s".formatted(attempt, totalAttempts, provider.getUrl()));
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                validateResponse(response);

                Debugger.log("Response received: " + response.body());
                return response.body();
            } catch (Exception e) {
                lastError = e;
                if (attempt < totalAttempts && shouldRetry(e)) {
                    Debugger.log("Retrying in %dms...".formatted(retryDelayMillis));
                    Thread.sleep(retryDelayMillis);
                }
            }
        }
        throw lastError;
    }

    private HttpRequest buildBaseRequest(String requestBody) {
        return HttpRequest.newBuilder()
                .uri(URI.create(provider.getUrl()))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + provider.getKey())
                .timeout(DEFAULT_TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
    }

    private void validateResponse(HttpResponse<String> response) {
        if (response.statusCode() >= 400) {
            throw new LLMResponseException(response);
        }
    }

    private boolean shouldRetry(Exception e) {
        if (e instanceof LLMResponseException) {
            int statusCode = ((LLMResponseException) e).getStatusCode();
            return statusCode == 429 || (statusCode >= 500 && statusCode < 600);
        }
        return e instanceof java.io.IOException;
    }
}