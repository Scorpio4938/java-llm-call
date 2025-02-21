package io.github.scorpio4938.LLMCall.config;

import java.util.Map;
import java.util.Objects;

import io.github.scorpio4938.LLMCall.core.messages.prompts.Prompt;

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

    private LLMRequestConfig(Builder builder) {
        this.model = builder.model;
        this.data = builder.data;
        this.params = builder.params;
        this.prompt = builder.prompt;
    }

    public static Builder newBuilder(String model) {
        return new Builder(model);
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
     * Builder class for constructing LLMRequestConfig instances.
     * 
     * @since 1.0.2
     */
    public static class Builder {
        private final String model;
        private Map<String, String> data = Map.of();
        private Map<String, Object> params = Map.of();
        private Prompt prompt;

        /**
         * @param model The LLM model to use (required)
         */
        public Builder(String model) {
            this.model = Objects.requireNonNull(model, "Model cannot be null");
        }

        /**
         * Sets the message data for the request.
         * 
         * @param data Map containing role/content pairs
         * @return This builder instance
         */
        public Builder withData(Map<String, String> data) {
            this.data = data;
            return this;
        }

        /**
         * Sets additional parameters for the LLM call.
         * 
         * @param params Map of parameters (e.g., temperature, max_tokens)
         * @return This builder instance
         */
        public Builder withParams(Map<String, Object> params) {
            this.params = params;
            return this;
        }

        /**
         * Sets the system prompt/guidelines for the request.
         * 
         * @param prompt Prompt implementation to use
         * @return This builder instance
         */
        public Builder withPrompt(Prompt prompt) {
            this.prompt = prompt;
            return this;
        }

        /**
         * Constructs the final configuration object.
         * 
         * @return Configured LLMRequestConfig instance
         */
        public LLMRequestConfig build() {
            return new LLMRequestConfig(this);
        }
    }
}