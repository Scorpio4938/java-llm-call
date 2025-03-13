package io.github.scorpio4938.LLMCall;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.github.scorpio4938.LLMCall.core.RequestHandler;
import io.github.scorpio4938.LLMCall.core.builder.LLMRequestBuilder;
import io.github.scorpio4938.LLMCall.core.messages.LLMResponse;
import io.github.scorpio4938.LLMCall.core.providers.IProvider;
import io.github.scorpio4938.LLMCall.service.debug.Debugger;
import io.github.scorpio4938.LLMCall.service.exceptions.llm.LLMErrorCode;
import io.github.scorpio4938.LLMCall.service.exceptions.llm.LLMException;
import io.github.scorpio4938.LLMCall.core.retry.DefaultRetry;

// import javax.annotation.Nullable;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Simplified LLM API Client for making requests to language models.
 * 
 * @since 1.0.0
 */
public class LLMApiClient {
    private static final Gson GSON = new GsonBuilder().create();

    private final RequestHandler requestHandler;
    private final ExecutorService asyncExecutor;

    /**
     * Constructs a new LLMApiClient with the specified provider.
     *
     * @param provider The LLM provider to use (must not be null)
     * @throws IllegalArgumentException if provider is null
     * 
     * @since 1.0.0
     */
    public LLMApiClient(IProvider provider) {
        this(provider, Duration.ofSeconds(30), new DefaultRetry());
    }

    /**
     * Constructs a new LLMApiClient with custom HttpClient configuration.
     *
     * @param provider    The LLM provider to use (must not be null)
     * @param timeout     Custom timeout configuration
     * @param retryConfig Custom retry configuration
     * @throws IllegalArgumentException if provider or timeout is null
     */
    public LLMApiClient(IProvider provider, Duration timeout, DefaultRetry retryConfig) {
        this.requestHandler = new RequestHandler(provider, HttpClient.newBuilder()
                .connectTimeout(timeout)
                .build(), retryConfig);
        this.asyncExecutor = Executors.newCachedThreadPool();
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
        String requestBody = requestHandler.buildRequest(builder);
        String responseBody = requestHandler.sendRequestWithRetry(requestBody, builder);
        return parseResponse(responseBody);
    }

    private String parseResponse(String responseBody) {
        LLMResponse response = GSON.fromJson(responseBody, LLMResponse.class);
        return response.getFirstMessageContent();
    }

    /**
     * Calls the LLM with fallback support. Tries each model in sequence until one
     * succeeds.
     * 
     * @param builder The request builder containing models and configuration
     * @return The content of the first message in the response
     * @throws Exception if all models fail
     * 
     * @since 1.0.2
     */
    public String callLLM(LLMRequestBuilder builder) throws LLMException {
        LLMException lastError = null;
        StringBuilder errors = new StringBuilder();
        List<String> models = builder.getModels();

        Debugger.log("Attempting call with " + models.size() + " model(s): " + String.join(", ", models));

        for (int i = 0; i < models.size(); i++) {
            String model = models.get(i);
            try {
                Debugger.log("Trying model " + model + " (" + (i + 1) + "/" + models.size() + ")");
                String result = directCallLLM(builder.cloneWithModel(model));
                Debugger.log("Model " + model + " succeeded");
                return result;
            } catch (Exception e) {
                String errorMsg = "Model " + model + " failed: " + e.getMessage();
                errors.append(errorMsg).append("\n");
                Debugger.log(errorMsg);
                lastError = e instanceof LLMException ? (LLMException) e
                        : new LLMException(LLMErrorCode.GENERAL_ERROR, "Unexpected error", e);
            }
        }

        throw new LLMException(LLMErrorCode.GENERAL_ERROR, "All models failed. Errors:\n" + errors.toString(),
                lastError);
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
                throw e instanceof LLMException ? (LLMException) e
                        : new LLMException(LLMErrorCode.GENERAL_ERROR, "Async call failed", e);
            }
        });
    }

    public LLMApiClient withTimeout(Duration timeout) {
        return new LLMApiClient(
                this.requestHandler.getProvider(),
                timeout,
                this.requestHandler.getRetryConfig());
    }

    public LLMApiClient withRetry(DefaultRetry retryConfig) {
        return new LLMApiClient(
                this.requestHandler.getProvider(),
                this.requestHandler.getTimeout(),
                retryConfig);
    }

    public CompletableFuture<String> asyncCall(LLMRequestBuilder request) {
        return CompletableFuture.supplyAsync(() -> callLLM(request), asyncExecutor);
    }

    public static LLMApiClientBuilder builder() {
        return new LLMApiClientBuilder();
    }
}
