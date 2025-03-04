package io.github.scorpio4938.LLMCall.core.builder;

import io.github.scorpio4938.LLMCall.core.messages.prompts.Prompt;
import io.github.scorpio4938.LLMCall.core.retry.DefaultRetry;

import java.util.Map;
import java.util.Objects;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import io.github.scorpio4938.LLMCall.service.debug.Debugger;

/**
 * Builder class for constructing LLM request configurations.
 * 
 * <p>
 * Example usage:
 * 
 * <pre>{@code
 * LLMRequest request = new LLMRequestBuilder("gpt-4")
 *         .withData(messageMap)
 *         .withMaxTokens(100)
 *         .withPrompt(prompt)
 *         .withTemperature(0.7)
 *         .build();
 * }</pre>
 * 
 * @since 1.0.2
 */
public class LLMRequestBuilder {
    private final List<String> models;
    private Map<String, String> data;
    private final Map<String, Object> params;
    private Prompt prompt;
    private DefaultRetry retryConfig;
    private boolean isBuilt;

    public LLMRequestBuilder(String model) {
        this.models = new ArrayList<>();
        this.models.add(Objects.requireNonNull(model, "Model cannot be null"));
        this.data = new HashMap<>();
        this.params = new HashMap<>();
        this.retryConfig = new DefaultRetry();
    }

    /**
     * Set the data for the request
     * 
     * @param data The data to set
     * @return The current builder
     * 
     * @since 1.0.2
     */
    public LLMRequestBuilder withData(Map<String, String> data) {
        checkNotBuilt();
        this.data = new HashMap<>(Objects.requireNonNull(data, "Data map cannot be null"));
        return this;
    }

    /**
     * Set the prompt for the request
     * 
     * @param prompt The prompt to set
     * @return The current builder
     * 
     * @since 1.0.2
     */
    public LLMRequestBuilder withPrompt(Prompt prompt) {
        checkNotBuilt();
        this.prompt = Objects.requireNonNull(prompt, "Prompt cannot be null");
        return this;
    }

    /**
     * Set the max tokens for the request
     * 
     * @param maxTokens The max tokens to set
     * @return The current builder
     * 
     * @since 1.0.2
     */
    public LLMRequestBuilder withMaxTokens(int maxTokens) {
        checkNotBuilt();
        params.put("max_tokens", maxTokens);
        return this;
    }

    /**
     * Set the temperature for the request
     * 
     * @param temperature The temperature to set
     * @return The current builder
     * 
     * @since 1.0.2
     */
    public LLMRequestBuilder withTemperature(double temperature) {
        checkNotBuilt();
        params.put("temperature", temperature);
        return this;
    }

    /**
     * Set the top p for the request
     * 
     * @param topP The top p to set
     * @return The current builder
     * 
     * @since 1.0.2
     */
    public LLMRequestBuilder withTopP(double topP) {
        checkNotBuilt();
        params.put("top_p", topP);
        return this;
    }

    /**
     * Set a custom parameter for the request
     * 
     * @param key   The key to set
     * @param value The value to set
     * @return The current builder
     * 
     * @since 1.0.2
     */
    public LLMRequestBuilder withCustomParam(String key, Object value) {
        checkNotBuilt();
        params.put(Objects.requireNonNull(key, "Parameter key cannot be null"),
                Objects.requireNonNull(value, "Parameter value cannot be null"));
        return this;
    }

    /**
     * Set a map of parameters for the request
     * 
     * @param newParams The parameters to set
     * @return The current builder
     * 
     * @since 1.0.2
     */
    public LLMRequestBuilder withParams(Map<String, Object> newParams) {
        checkNotBuilt();
        Objects.requireNonNull(newParams, "Parameters map cannot be null")
                .forEach(this::withCustomParam);
        return this;
    }

    /**
     * Add fallback models to try if the primary model fails
     * 
     * @param fallbackModels The fallback models to try
     * @return The current builder
     * 
     * @since 1.0.2
     */
    public LLMRequestBuilder withFallback(String... fallbackModels) {
        checkNotBuilt();
        Objects.requireNonNull(fallbackModels, "Fallback models cannot be null");
        Arrays.stream(fallbackModels)
                .filter(Objects::nonNull)
                .forEach(models::add);
        return this;
    }

    /**
     * Set custom retry configuration
     * 
     * @param retryConfig The retry configuration to set
     * @return The current builder
     * 
     * @since 1.0.2
     */
    public LLMRequestBuilder withRetryConfig(DefaultRetry retryConfig) {
        checkNotBuilt();
        this.retryConfig = Objects.requireNonNull(retryConfig, "RetryConfig cannot be null");
        return this;
    }

    public LLMRequestBuilder build() {
        validate();
        isBuilt = true;
        return this;
    }

    private void validate() {
        if (models.isEmpty()) {
            throw new IllegalStateException("At least one model must be specified");
        }
        if (params.containsKey("max_tokens") && (int) params.get("max_tokens") <= 0) {
            throw new IllegalArgumentException("maxTokens must be positive");
        }
        if (params.containsKey("temperature")) {
            double temp = (double) params.get("temperature");
            if (temp < 0 || temp > 2) {
                throw new IllegalArgumentException("Temperature must be between 0 and 2");
            }
        }
        if (params.containsKey("top_p")) {
            double topP = (double) params.get("top_p");
            if (topP < 0 || topP > 1) {
                throw new IllegalArgumentException("top_p must be between 0 and 1");
            }
        }
    }

    private void checkNotBuilt() {
        if (isBuilt) {
            throw new IllegalStateException("Builder has already been built");
        }
    }

    // Getters return immutable copies
    public DefaultRetry getRetryConfig() {
        return retryConfig;
    }

    public String getModel() {
        return models.get(0);
    }

    public List<String> getModels() {
        return Collections.unmodifiableList(models);
    }

    public Map<String, String> getData() {
        return Collections.unmodifiableMap(data);
    }

    public Map<String, Object> getParams() {
        return Collections.unmodifiableMap(params);
    }

    public Prompt getPrompt() {
        return prompt;
    }

    /**
     * Clone the current builder with a new model
     * 
     * @param newModel The new model to set
     * @return A new LLMRequestBuilder with the updated model
     * 
     * @since 1.0.2
     */
    public LLMRequestBuilder cloneWithModel(String newModel) {
        LLMRequestBuilder clone = new LLMRequestBuilder(newModel)
                .withData(this.data)
                .withParams(this.params)
                .withPrompt(this.prompt)
                .withRetryConfig(this.retryConfig);
        models.stream().skip(1).forEach(m -> clone.withFallback(m));
        return clone.build();
    }
}