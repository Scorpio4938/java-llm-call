package io.github.scorpio4938.LLMCall.core.builder;

import io.github.scorpio4938.LLMCall.core.messages.prompts.Prompt;
import java.util.Map;
import java.util.Objects;
import java.util.HashMap;
import java.util.Collections;

/**
 * Builder class for constructing LLM request configurations.
 * 
 * <p>
 * Example usage:
 * 
 * <pre>{@code
 * LLMRequestConfig config = LLMRequestConfig.newBuilder("gpt-4")
 *         .withData(messageMap)
 *         .withParams(params)
 *         .withPrompt(new BasicPrompt())
 * }</pre>
 * 
 * @since 1.0.2
 */
public class LLMRequestBuilder {
    private final String model;
    private Map<String, String> data;
    private final Map<String, Object> params = new HashMap<>();
    private Prompt prompt;

    public LLMRequestBuilder(String model) {
        this.model = Objects.requireNonNull(model, "Model cannot be null");
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

    // Getters
    public String getModel() {
        return model;
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
        return new LLMRequestBuilder(newModel)
                .withData(this.data)
                .withParams(this.params)
                .withPrompt(this.prompt);
    }
}