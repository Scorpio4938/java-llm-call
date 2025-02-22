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
    private static final Gson GSON = new GsonBuilder().create();
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    private final Provider provider;
    private final HttpClient httpClient;

    public RequestHandler(Provider provider, HttpClient httpClient) {
        this.provider = Objects.requireNonNull(provider, "Provider must not be null");
        this.httpClient = Objects.requireNonNull(httpClient, "HttpClient must not be null");
    }

    public String buildRequestBody(LLMRequestBuilder builder) {
        String model = builder.getModel();
        Map<String, String> data = builder.getData();
        Map<String, Object> params = builder.getParams();
        Prompt prompt = builder.getPrompt();

        validateInputs(model, data);

        List<LLMRequest.Message> dataList = buildMessageList(prompt, data);

        Debugger.log("Data list: " + dataList.toString());

        LLMRequest request = createLLMRequest(model, dataList, params);

        Debugger.log("Request: " + request);

        // Merge parameters directly into root JSON object
        JsonObject jsonObject = GSON.toJsonTree(request).getAsJsonObject();
        params.forEach((k, v) -> jsonObject.add(k, GSON.toJsonTree(v)));

        Debugger.log("Request body: " + GSON.toJson(jsonObject));

        return GSON.toJson(jsonObject);
        // return GSON.toJson(request);
    }

    private void validateInputs(String model, Map<String, String> data) {
        if (model == null || model.trim().isEmpty()) {
            throw new IllegalArgumentException("Model must not be empty");
        }
        if (data == null) {
            throw new IllegalArgumentException("Message data must not be null");
        }
        if (data.isEmpty()) {
            throw new IllegalArgumentException("Message data must not be empty");
        }
    }

    private List<LLMRequest.Message> buildMessageList(Prompt prompt, Map<String, String> data) {
        Map<String, String> sortedData = MapSorter.sortByKeys(data);
        List<LLMRequest.Message> dataList = new ArrayList<>();

        if (prompt != null) {
            dataList.add(LLMRequest.createMessage(prompt.getRole(), prompt.getContent()));
        }

        sortedData.forEach((role, content) -> {
            if (role.equalsIgnoreCase("content")) {
                dataList.add(LLMRequest.createMessage("user", content));
            }
            // else {
            // dataList.add(LLMRequest.createMessage(role, content));
            // }
        });
        return dataList;
    }

    private LLMRequest createLLMRequest(String model, List<LLMRequest.Message> dataList, Map<String, Object> params) {
        return new LLMRequest(provider.getModel(model), dataList);
    }

    public String sendRequest(String requestBody) throws Exception {
        HttpRequest request = buildBaseRequest(requestBody);
        Debugger.log("Sending request to: " + provider.getUrl());

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validateResponse(response);

        Debugger.log("Response received: " + response.body());
        return response.body();
    }

    public String sendRequestWithRetry(String requestBody, int maxRetries, long retryDelayMillis) throws Exception {
        HttpRequest request = buildBaseRequest(requestBody);
        int totalAttempts = maxRetries + 1;
        Exception lastError = null;

        for (int attempt = 1; attempt <= totalAttempts; attempt++) {
            try {
                Debugger.log("Attempt %d/%d to: %s".formatted(attempt, totalAttempts, provider.getUrl()));
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
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