package io.github.scorpio4938.LLMCall.core.builder;

import io.github.scorpio4938.LLMCall.core.messages.prompts.Prompt;
import java.util.Map;
import java.util.Objects;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import io.github.scorpio4938.LLMCall.service.debug.Debugger;
import io.github.scorpio4938.LLMCall.config.DefaultRetry;

/**
 * Builder class for constructing LLM request configurations.
 * 
 * <p>
 * Example usage:
 * 
 * <pre>{@code
 * LLMRequestBuilder builder = new LLMRequestBuilder("gpt-4")
 *         .withData(messageMap)
 *         .withMaxTokens(100)
 *         .withPrompt(new BasicPrompt())
 *         .withTemperature(0.7);
 * }</pre>
 * 
 * @since 1.0.2
 */
public class LLMRequestBuilder {
    private final List<String> models = new ArrayList<>();
    private Map<String, String> data;
    private final Map<String, Object> params = new HashMap<>();
    private Prompt prompt;
    private DefaultRetry retryConfig = new DefaultRetry(); // Default retry config

    public LLMRequestBuilder(String model) {
        Objects.requireNonNull(model, "Model cannot be null");
        models.add(model);
        this.data = Map.of();
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
        this.data = data;
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
        this.prompt = prompt;
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
        if (maxTokens <= 0) {
            throw new IllegalArgumentException("maxTokens must be positive");
        }
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
        if (temperature < 0 || temperature > 2) {
            throw new IllegalArgumentException("Temperature must be between 0 and 2");
        }
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
        if (topP < 0 || topP > 1) {
            throw new IllegalArgumentException("top_p must be between 0 and 1");
        }
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
        Objects.requireNonNull(key, "parameter key cannot be null");
        params.put(key, value);
        return this;
    }

    /**
     * Set a map of parameters for the request
     * 
     * @param params The parameters to set
     * @return The current builder
     * 
     * @since 1.0.2
     */
    public LLMRequestBuilder withParams(Map<String, Object> params) {
        params.forEach(
                (key, value) -> this.params.put(key, Objects.requireNonNull(value, "Parameter value cannot be null")));
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
        models.addAll(Arrays.asList(fallbackModels));
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
        this.retryConfig = Objects.requireNonNull(retryConfig, "RetryConfig cannot be null");
        return this;
    }

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
        return data;
    }

    public Map<String, Object> getParams() {
        return Collections.unmodifiableMap(new HashMap<>(params));
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
                .withPrompt(this.prompt);
        // Skip first model since we already set it in constructor
        models.stream().skip(1).forEach(m -> clone.withFallback(m));
        return clone;
    }
}