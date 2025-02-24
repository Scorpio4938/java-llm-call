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
import io.github.scorpio4938.LLMCall.config.RetryConfig;

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
    private RetryConfig retryConfig = new RetryConfig(); // Default retry config

    public LLMRequestBuilder(String model) {
        Objects.requireNonNull(model, "Model cannot be null");
        models.add(model);
        this.data = Map.of();
    }

    public LLMRequestBuilder withData(Map<String, String> data) {
        this.data = data;
        return this;
    }

    public LLMRequestBuilder withPrompt(Prompt prompt) {
        this.prompt = prompt;
        return this;
    }

    public LLMRequestBuilder withMaxTokens(int maxTokens) {
        if (maxTokens <= 0) {
            throw new IllegalArgumentException("maxTokens must be positive");
        }
        params.put("max_tokens", maxTokens);
        return this;
    }

    public LLMRequestBuilder withTemperature(double temperature) {
        if (temperature < 0 || temperature > 2) {
            throw new IllegalArgumentException("Temperature must be between 0 and 2");
        }
        params.put("temperature", temperature);
        return this;
    }

    public LLMRequestBuilder withTopP(double topP) {
        if (topP < 0 || topP > 1) {
            throw new IllegalArgumentException("top_p must be between 0 and 1");
        }
        params.put("top_p", topP);
        return this;
    }

    public LLMRequestBuilder withCustomParam(String key, Object value) {
        Objects.requireNonNull(key, "parameter key cannot be null");
        params.put(key, value);
        return this;
    }

    public LLMRequestBuilder withParams(Map<String, Object> params) {
        params.forEach(
                (key, value) -> this.params.put(key, Objects.requireNonNull(value, "Parameter value cannot be null")));
        return this;
    }

    /**
     * Add fallback models to try if the primary model fails
     */
    public LLMRequestBuilder withFallback(String... fallbackModels) {
        models.addAll(Arrays.asList(fallbackModels));
        return this;
    }

    /**
     * Set custom retry configuration
     */
    public LLMRequestBuilder withRetryConfig(RetryConfig retryConfig) {
        this.retryConfig = Objects.requireNonNull(retryConfig, "RetryConfig cannot be null");
        return this;
    }

    /**
     * Get current retry configuration
     */
    public RetryConfig getRetryConfig() {
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