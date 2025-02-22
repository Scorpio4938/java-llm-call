package io.github.scorpio4938.LLMCall.config;

import io.github.scorpio4938.LLMCall.core.builder.LLMRequestBuilder;
import io.github.scorpio4938.LLMCall.core.messages.prompts.Prompt;
import java.util.Map;
import java.util.Objects;

/**
 * Configuration object for LLM API requests using builder pattern.
 * Provides a fluent interface for constructing request parameters.
 * 
 * <p>
 * Example usage:
 * 
 * <pre>{@code
 * LLMRequestConfig config = LLMRequestConfig.newBuilder("gpt-4")
 *         .withData(messageMap)
 *         .withParams(params)
 *         .withPrompt(new BasicPrompt())
 *         .build();
 * }</pre>
 * 
 * @since 1.0.2
 */
public class LLMRequestConfig {
    private final String model;
    private final Map<String, String> data;
    private final Map<String, Object> params;
    private final Prompt prompt;

    public LLMRequestConfig(LLMRequestBuilder builder) {
        this.model = builder.getModel();
        this.data = builder.getData();
        this.params = builder.getParams();
        this.prompt = builder.getPrompt();
    }

    // Getters
    public String getModel() {
        return model;
    }

    public Map<String, String> getData() {
        return data;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public Prompt getPrompt() {
        return prompt;
    }

    /**
     * Directly use LLMRequestBuilder for configuration
     */
    public static LLMRequestBuilder newBuilder(String model) {
        return new LLMRequestBuilder(model);
    }
}