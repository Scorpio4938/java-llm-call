package io.github.scorpio4938.LLMCall;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.github.scorpio4938.LLMCall.config.LLMRequestConfig;
import io.github.scorpio4938.LLMCall.core.messages.LLMRequest;
import io.github.scorpio4938.LLMCall.core.messages.LLMResponse;
import io.github.scorpio4938.LLMCall.core.messages.LLMResponseException;
import io.github.scorpio4938.LLMCall.core.messages.prompts.Prompt;
import io.github.scorpio4938.LLMCall.core.providers.Provider;
import io.github.scorpio4938.LLMCall.service.debug.Debugger;
import io.github.scorpio4938.LLMCall.service.utils.MapSorter;
import io.github.scorpio4938.LLMCall.core.LLMRequestBuilder;

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
     * @param builder The LLM request builder
     * @return JSON string representing the request body
     * @throws IllegalArgumentException if model is null or empty, or data is null
     * 
     * @since 1.0.0
     */
    private String buildRequestBody(LLMRequestBuilder builder) {
        String model = builder.getModel();
        Map<String, String> data = builder.getData();
        Map<String, Object> params = builder.getParams();
        Prompt prompt = builder.getPrompt();

        if (model.trim().isEmpty()) {
            throw new IllegalArgumentException("Model must not be empty");
        }
        if (data == null) {
            throw new IllegalArgumentException("Message data must not be null");
        }

        Map<String, String> sortedData = MapSorter.sortByKeys(data);
        List<LLMRequest.Message> dataList = new ArrayList<>();

        // Add prompt if provided
        if (prompt != null) {
            dataList.add(LLMRequest.createMessage(prompt.getRole(), prompt.getContent()));
        }

        for (Map.Entry<String, String> entry : sortedData.entrySet()) {
            dataList.add(LLMRequest.createMessage(entry.getKey(), entry.getValue()));
        }

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
     * Unified LLM call using request builder
     * 
     * @param builder The LLM request builder
     * @return The content of the first message in the response
     * @throws Exception if there's an error processing the request
     * 
     * @since 1.0.0
     */
    public String directCallLLM(LLMRequestBuilder builder) throws Exception {
        String requestBody = buildRequestBody(builder);
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
     * 
     * @since 1.0.0
     */
    public ModelChain callLLM(LLMRequestBuilder builder) {
        return new ModelChain(builder);
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
    public CompletableFuture<String> asyncCallLLM(LLMRequestBuilder builder) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return directCallLLM(builder);
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

    /**
     * Executes an LLM call using a pre-configured request object.
     * 
     * @param config Configured request parameters
     * @return The content of the first message in the response
     * @throws Exception If there's an error processing the request
     * @see LLMRequestConfig
     * @since 1.0.2
     */
    public String callLLM(LLMRequestConfig config) throws Exception {
        LLMRequestBuilder builder = new LLMRequestBuilder(config.getModel())
                .withData(config.getData())
                .withParams(config.getParams())
                .withPrompt(config.getPrompt());
        return directCallLLM(builder);
    }

    /**
     * Alternative signature for directCallLLM using configuration object.
     * 
     * @param config Configured request parameters
     * @return The content of the first message in the response
     * @throws Exception If there's an error processing the request
     * @see #callLLM(LLMRequestConfig)
     * @since 1.0.2
     */
    public String directCallLLM(LLMRequestConfig config) throws Exception {
        return callLLM(config);
    }

    public class ModelChain {
        private final LLMRequestBuilder baseBuilder;
        private final List<String> fallbackModels = new ArrayList<>();

        public ModelChain(LLMRequestBuilder builder) {
            this.baseBuilder = builder;
        }

        public ModelChain withFallback(String... models) {
            fallbackModels.addAll(Arrays.asList(models));
            return this;
        }

        public ModelChain withPrompt(Prompt prompt) {
            baseBuilder.withPrompt(prompt);
            return this;
        }

        public String execute() throws Exception {
            List<String> allModels = new ArrayList<>();
            allModels.add(baseBuilder.getModel());
            allModels.addAll(fallbackModels);

            StringBuilder errors = new StringBuilder();
            Exception lastError = null;
            for (String model : allModels) {
                try {
                    LLMRequestBuilder currentBuilder = new LLMRequestBuilder(model)
                            .withData(baseBuilder.getData())
                            .withParams(baseBuilder.getParams())
                            .withPrompt(baseBuilder.getPrompt());

                    return LLMApiClient.this.directCallLLM(currentBuilder);
                } catch (Exception e) {
                    errors.append("Model ").append(model).append(" failed: ").append(e.getMessage()).append("\n");
                    lastError = e;
                    Debugger.log("Model " + model + " failed: " + e.getMessage());
                }
            }
            throw new Exception("All models failed. Errors:\n" + errors, lastError);
        }
    }
}
