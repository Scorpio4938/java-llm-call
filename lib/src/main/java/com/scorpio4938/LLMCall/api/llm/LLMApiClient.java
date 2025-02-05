package com.scorpio4938.LLMCall.api.llm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.scorpio4938.LLMCall.service.utils.MapSorter;
import com.scorpio4938.LLMCall.service.utils.debug.Debugger;

import javax.annotation.Nullable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Simplified LLM API Client.
 */
public class LLMApiClient {
    private final Provider provider;
    private final int maxTokens;
    private static final Gson GSON = new GsonBuilder().create();

    // Constructor: accept provider and optional maxTokens (default: 100)
    public LLMApiClient(Provider provider, @Nullable Integer maxTokens) {
        this.provider = provider;
        this.maxTokens = (maxTokens != null) ? maxTokens : 100;
    }

    // Build the JSON request body from the model and message map.
    private String buildRequestBody(String model, Map<String, String> data) {
        Map<String, String> sortedData = MapSorter.sortByKeys(data);
        List<LLMRequest.Message> messages = new ArrayList<>();
        for (Map.Entry<String, String> entry : sortedData.entrySet()) {
            messages.add(LLMRequest.createMessage(entry.getKey(), entry.getValue()));
        }
        LLMRequest request = new LLMRequest(provider.getModel(model), messages, maxTokens);
        return GSON.toJson(request);
    }

    // Send HTTP request to the provider's API.
    private String sendRequest(String requestBody) throws Exception {
        String apiUrl = provider.getUrl();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + provider.getKey())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());
        Debugger.log("Request Headers: " + request.headers());
        Debugger.log("Response body: " + response.body());
        return response.body();
    }

    /**
     * Call the LLM with the given model and message map.
     *
     * @param model   The model to use.
     * @param data    The message data.
     * @return The content of the first message in the response.
     * @throws Exception If there is an error while sending the request.
     */
    public String callLLM(String model, Map<String, String> data) throws Exception {
        String requestBody = buildRequestBody(model, data);
        String responseBody = sendRequest(requestBody);
        LLMResponse response = GSON.fromJson(responseBody, LLMResponse.class);
        return response.getFirstMessageContent();
    }

    public int getMaxTokens() {
        return maxTokens;
    }
}
