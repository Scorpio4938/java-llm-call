package io.github.scorpio4938.LLMCall;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.github.scorpio4938.LLMCall.config.LLMRequestConfig;
import io.github.scorpio4938.LLMCall.core.RequestHandler;
import io.github.scorpio4938.LLMCall.core.builder.LLMRequestBuilder;
import io.github.scorpio4938.LLMCall.core.messages.LLMRequest;
import io.github.scorpio4938.LLMCall.core.messages.LLMResponse;
import io.github.scorpio4938.LLMCall.core.messages.LLMResponseException;
import io.github.scorpio4938.LLMCall.core.messages.prompts.Prompt;
import io.github.scorpio4938.LLMCall.core.providers.Provider;
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

    private final RequestHandler requestHandler;

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
                .connectTimeout(Duration.ofSeconds(30))
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
        this.requestHandler = new RequestHandler(provider, httpClient);
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
        String requestBody = requestHandler.buildRequestBody(builder);
        String responseBody = requestHandler.sendRequestWithRetry(requestBody, maxRetries, retryDelayMillis);
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
