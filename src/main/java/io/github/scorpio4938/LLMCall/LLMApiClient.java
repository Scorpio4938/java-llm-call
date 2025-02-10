package io.github.scorpio4938.LLMCall;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.github.scorpio4938.LLMCall.messages.LLMRequest;
import io.github.scorpio4938.LLMCall.messages.LLMResponse;
import io.github.scorpio4938.LLMCall.messages.LLMResponseException;
import io.github.scorpio4938.LLMCall.providers.Provider;
import io.github.scorpio4938.LLMCall.service.debug.Debugger;
import io.github.scorpio4938.LLMCall.service.utils.MapSorter;

// import javax.annotation.Nullable;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Simplified LLM API Client for making requests to language models.
 * 
 * @since 1.0.0
 */
public class LLMApiClient {
    private static final Gson GSON = new GsonBuilder().create();
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
    private static final int DEFAULT_MAX_TOKENS = 100;

    private final Provider provider;
    private final HttpClient httpClient;

    private int maxRetries = 3;
    private long retryDelayMillis = 1000;

    /**
     * Constructs a new LLMApiClient with the specified provider.
     *
     * @param provider The LLM provider to use (must not be null)
     * @throws IllegalArgumentException if provider is null
     * 
     * @since 1.0.0
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
     * 
     * @since 1.0.0
     */
    private String buildRequestBody(String model, Map<String, String> data, Map<String, Object> params) {
        // Objects.requireNonNull(model, "Model must not be null");
        // Objects.requireNonNull(data, "Data must not be null");
        // Objects.requireNonNull(params, "Params must not be null");

        if (model.trim().isEmpty()) {
            throw new IllegalArgumentException("Model must not be empty");
        }

        Map<String, String> sortedData = MapSorter.sortByKeys(data);
        List<LLMRequest.Message> dataList = new ArrayList<>();
        for (Map.Entry<String, String> entry : sortedData.entrySet()) {
            dataList.add(LLMRequest.createMessage(entry.getKey(), entry.getValue()));
        }

        // Create request with dynamic parameters
        LLMRequest request = new LLMRequest(provider.getModel(model), dataList);
        request.addParameters(params);

        return GSON.toJson(request);
    }

    /**
     * Sends HTTP request to the provider's API.
     *
     * @param requestBody The request body to send
     * @return The response body
     * @throws Exception if there is an error while sending the request
     * 
     * @since 1.0.0
     */
    @SuppressWarnings("unused")
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
            throw new LLMResponseException(response);
        }

        Debugger.log("Response received: " + response.body());
        return response.body();
    }

    /**
     * Sends HTTP request to the provider's API with retry logic.
     *
     * @param requestBody The request body to send
     * @return The response body
     * @throws Exception if there is an error while sending the request
     * 
     * @since 1.0.1
     */
    private String sendRequestWithRetry(String requestBody) throws Exception {
        String apiUrl = provider.getUrl();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + provider.getKey())
                .timeout(DEFAULT_TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        int totalAttempts = maxRetries + 1;
        Exception lastError = null;

        for (int attempt = 1; attempt <= totalAttempts; attempt++) {
            try {
                Debugger.log("Attempt %d/%d to: %s".formatted(attempt, totalAttempts, apiUrl));
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() >= 400) {
                    throw new LLMResponseException(response);
                }

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
     * 
     * @since 1.0.0
     */
    public String directCallLLM(String model, Map<String, String> data, Map<String, Object> params) throws Exception {
        String requestBody = buildRequestBody(model, data, params);
        String responseBody = sendRequestWithRetry(requestBody);
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
     * 
     * @since 1.0.0
     */
    public String directCallLLM(String model, Map<String, String> data) throws Exception {
        return directCallLLM(model, data, Map.of("max_tokens", DEFAULT_MAX_TOKENS));
    }

    /**
     * Calls the LLM with the given model and message map using default
     * parameters.
     *
     * @param model The model to use
     * @param data  The message data
     * @return The content of the first message in the response
     * 
     * @since 1.0.0
     */
    public ModelChain callLLM(String model, Map<String, String> data) {
        return new ModelChain(model, data, Map.of("max_tokens", DEFAULT_MAX_TOKENS));
    }

    /**
     * Calls the LLM with the given model and message map using the specified
     * parameters.
     *
     * @param model  The model to use
     * @param data   The message data
     * @param params Additional parameters for the LLM call
     * @return The content of the first message in the response
     * 
     * @since 1.0.0
     */

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

        /**
         * Adds fallback models to the chain.
         *
         * @param models The models to add
         * @return The updated ModelChain
         * 
         * @since 1.0.0
         */
        public ModelChain withFallback(String... models) {
            fallbackModels.addAll(Arrays.asList(models));
            return this;
        }

        /**
         * Executes the model chain.
         *
         * @return The content of the first message in the response
         * @throws Exception if all models fail
         * 
         * @since 1.0.0
         */
        public String execute() throws Exception {
            List<String> allModels = new ArrayList<>();
            allModels.add(primaryModel);
            allModels.addAll(fallbackModels);

            StringBuilder errors = new StringBuilder(); // Track all errors
            Exception lastError = null;
            for (String model : allModels) {
                try {
                    return LLMApiClient.this.directCallLLM(model, data, params);
                } catch (Exception e) {
                    errors.append("Model ").append(model).append(" failed: ").append(e.getMessage()).append("\n");
                    lastError = e;
                    Debugger.log("Model " + model + " failed: " + e.getMessage());
                }
            }
            throw new Exception("All models failed. Errors:\n" + errors, lastError);
        }
    }

    /**
     * Calls the LLM asynchronously.
     *
     * @param model The model to use
     * @param data  The message data
     * @return The content of the first message in the response
     * 
     * @since 1.0.1
     */
    public CompletableFuture<String> asyncCallLLM(String model, Map<String, String> data) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return directCallLLM(model, data);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public void setRetryDelay(long delay, java.util.concurrent.TimeUnit unit) {
        this.retryDelayMillis = unit.toMillis(delay);
    }

    private boolean shouldRetry(Exception e) {
        if (e instanceof LLMResponseException) {
            int statusCode = ((LLMResponseException) e).getStatusCode();
            return statusCode == 429 || (statusCode >= 500 && statusCode < 600);
        }
        return e instanceof java.io.IOException; // Retry on network errors
    }
}
