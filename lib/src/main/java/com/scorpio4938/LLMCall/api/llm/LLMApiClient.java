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
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Simplified LLM API Client for making requests to language models.
 */
public class LLMApiClient {
    private static final Gson GSON = new GsonBuilder().create();
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
    private static final int DEFAULT_MAX_TOKENS = 100;

    private final Provider provider;
    private final HttpClient httpClient;

    /**
     * Constructs a new LLMApiClient with the specified provider.
     *
     * @param provider The LLM provider to use (must not be null)
     * @throws IllegalArgumentException if provider is null
     */
    public LLMApiClient(Provider provider) {
        this(provider, HttpClient.newBuilder()
                .connectTimeout(DEFAULT_TIMEOUT)
                .build());
    }

    /**
     * Constructs a new LLMApiClient with custom HttpClient configuration.
     *
     * @param provider   The LLM provider to use (must not be null)
     * @param httpClient Custom HttpClient instance (must not be null)
     * @throws IllegalArgumentException if provider or httpClient is null
     */
    public LLMApiClient(Provider provider, HttpClient httpClient) {
        this.provider = Objects.requireNonNull(provider, "Provider must not be null");
        this.httpClient = Objects.requireNonNull(httpClient, "HttpClient must not be null");
    }

    /**
     * Builds the JSON request body from the model, message map, and parameters.
     *
     * @param model  The model to use
     * @param data   The message data
     * @param params Additional parameters for the LLM call (e.g., max_tokens,
     *               temperature)
     * @return JSON string representing the request body
     * @throws IllegalArgumentException if model is null or empty, or data is null
     */
    private String buildRequestBody(String model, Map<String, String> messages, Map<String, Object> params) {
        // Objects.requireNonNull(model, "Model must not be null");
        // Objects.requireNonNull(messages, "Messages must not be null");
        // Objects.requireNonNull(params, "Params must not be null");

        if (model.trim().isEmpty()) {
            throw new IllegalArgumentException("Model must not be empty");
        }

        Map<String, String> sortedMessages = MapSorter.sortByKeys(messages);
        List<LLMRequest.Message> messagesList = new ArrayList<>();
        for (Map.Entry<String, String> entry : sortedMessages.entrySet()) {
            messagesList.add(LLMRequest.createMessage(entry.getKey(), entry.getValue()));
        }

        // Create request with dynamic parameters
        LLMRequest request = new LLMRequest(provider.getModel(model), messagesList);
        request.addParameters(params);

        return GSON.toJson(request);
    }

    /**
     * Sends HTTP request to the provider's API.
     *
     * @param requestBody The request body to send
     * @return The response body
     * @throws Exception if there is an error while sending the request
     */
    private String sendRequest(String requestBody) throws Exception {
        String apiUrl = provider.getUrl();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + provider.getKey())
                .timeout(DEFAULT_TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        Debugger.log("Sending request to: " + apiUrl);
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new Exception("API request failed with status code: " + response.statusCode());
        }

        Debugger.log("Response received: " + response.body());
        return response.body();
    }

    /**
     * Calls the LLM with the given model, message map, and parameters.
     *
     * @param model  The model to use
     * @param data   The message data
     * @param params Additional parameters for the LLM call
     * @return The content of the first message in the response
     * @throws Exception                if there is an error while processing the
     *                                  request
     * @throws IllegalArgumentException if model is null or empty, or data is null
     */
    public String directCallLLM(String model, Map<String, String> data, Map<String, Object> params) throws Exception {
        String requestBody = buildRequestBody(model, data, params);
        String responseBody = sendRequest(requestBody);
        LLMResponse response = GSON.fromJson(responseBody, LLMResponse.class);
        return response.getFirstMessageContent();
    }

    /**
     * Calls the LLM with the given model and message map using default
     * parameters.
     *
     * @param model The model to use
     * @param data  The message data
     * @return The content of the first message in the response
     * @throws Exception                if there is an error while processing the
     *                                  request
     * @throws IllegalArgumentException if model is null or empty, or data is null
     */
    public String directCallLLM(String model, Map<String, String> data) throws Exception {
        return directCallLLM(model, data, Map.of("max_tokens", DEFAULT_MAX_TOKENS));
    }

    public ModelChain callLLM(String model, Map<String, String> data) {
        return new ModelChain(model, data, Map.of("max_tokens", DEFAULT_MAX_TOKENS));
    }

    public ModelChain callLLM(String model, Map<String, String> data, Map<String, Object> params) {
        return new ModelChain(model, data, params);
    }

    public class ModelChain {
        private final String primaryModel;
        private final Map<String, String> data;
        private final Map<String, Object> params;
        private final List<String> fallbackModels = new ArrayList<>();

        public ModelChain(String model, Map<String, String> data, Map<String, Object> params) {
            this.primaryModel = model;
            this.data = data;
            this.params = params;
        }

        public ModelChain withFallback(String... models) {
            fallbackModels.addAll(Arrays.asList(models));
            return this;
        }

        public String execute() throws Exception {
            List<String> allModels = new ArrayList<>();
            allModels.add(primaryModel);
            allModels.addAll(fallbackModels);

            Exception lastError = null;
            for (String model : allModels) {
                try {
                    return LLMApiClient.this.directCallLLM(model, data, params);
                } catch (Exception e) {
                    lastError = e;
                    Debugger.log("Model " + model + " failed: " + e.getMessage());
                }
            }
            throw new Exception("All models failed", lastError);
        }
    }
}
