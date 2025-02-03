package com.scorpio4938.LLMCall.api.llm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
 * llm api client.
 *
 */
public class LLMApiClient {
    private Provider provider;
    private int maxTokens;

    private Gson gson = new GsonBuilder().create();

    // Constructor takes the  provider (OpenAI, Anthropic, etc.) and maxTokens (Default 100)
    public LLMApiClient(Provider provider1, @Nullable Integer maxTokens) {
        this.provider = provider1;
        this.maxTokens = (maxTokens != null) ? maxTokens : 100;
    }

    private String buildMessage(String model, Map<String, String> map) {

        List<LLMRequest.Message> messages = new ArrayList<>();
        Map<String, String> sorted = MapSorter.sortByKeys(map);

        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            messages.add(LLMRequest.createMessage(entry.getKey(), entry.getValue()));
        }

        return this.gson.toJson(new LLMRequest(this.provider.getModel(model), messages, this.maxTokens));
    }

    // Send the request based on provider and model
    private String sendRequest1(String requestBody) throws Exception {
        // Get the appropriate API URL based on the provider
        String apiUrl = this.provider.getUrl();

        // Create HttpClient
        HttpClient client = HttpClient.newHttpClient();

        // Create HttpRequest
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + this.provider.getKey())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        // Send the request and get the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Debug
        Debugger.log("Request Headers: " + request.headers());
        Debugger.log("Response body: " + response.body());

        // Return the response body
        return response.body();
    }

    private String response(String response) {
        LLMResponse llmResponse = this.gson.fromJson(response, LLMResponse.class);
        return llmResponse.getFirstMessageContent();
    }

    /**
     * Call the LLM with the given model and message.
     *
     * @param model   The model to use.
     * @param message The message to send to the LLM.
     * @return The response from the LLM.
     * @throws Exception If there is an error sending the request.
     */
    public String callLLM(String model, Map<String, String> message) throws Exception {
        return this.response(this.sendRequest1(this.buildMessage(model, message)));
    }

    // Getters
    public int getMaxTokens() {
        return this.maxTokens;
    }
}
