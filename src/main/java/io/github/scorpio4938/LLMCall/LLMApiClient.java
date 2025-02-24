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
import io.github.scorpio4938.LLMCall.config.DefaultRetry;

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
import java.util.concurrent.TimeUnit;

/**
 * Simplified LLM API Client for making requests to language models.
 * 
 * @since 1.0.0
 */
public class LLMApiClient {
    private static final Gson GSON = new GsonBuilder().create();

    private final RequestHandler requestHandler;

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
        String requestBody = requestHandler.buildRequest(builder);
        String responseBody = requestHandler.sendRequestWithRetry(requestBody, builder);
        return parseResponse(responseBody);
    }

    private String parseResponse(String responseBody) {
        LLMResponse response = GSON.fromJson(responseBody, LLMResponse.class);
        return response.getFirstMessageContent();
    }

    /**
     * Calls the LLM with fallback support
     */
    public String callLLM(LLMRequestBuilder builder) throws Exception {
        Exception lastError = null;
        StringBuilder errors = new StringBuilder();

        for (String model : builder.getModels()) {
            try {
                return directCallLLM(builder.cloneWithModel(model));
            } catch (Exception e) {
                String errorMsg = "Model " + model + " failed: " + e.getMessage();
                errors.append(errorMsg).append("\n");
                Debugger.log(errorMsg);
                lastError = e;
            }
        }

        throw new Exception("All models failed. Errors:\n" + errors, lastError);
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
}
