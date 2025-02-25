package io.github.scorpio4938.LLMCall.core.providers;

import java.util.List;

/**
 * Interface for LLM providers.
 * Defines the contract that all provider implementations must follow.
 *
 * @since 1.0.2
 */
public interface IProvider {
    /**
     * Gets the provider name.
     *
     * @return The provider name
     */
    String getProvider();

    /**
     * Gets the provider API URL.
     *
     * @return The API URL
     */
    String getUrl();

    /**
     * Gets the API key for the provider.
     *
     * @return The API key
     */
    String getKey();

    /**
     * Gets the list of supported models.
     *
     * @return List of supported model names
     */
    List<String> getModels();

    /**
     * Gets a specific model by name.
     *
     * @param modelName The name of the model to retrieve
     * @return The model name if supported
     * @throws ModelNotSupportedException if the model is not supported
     */
    String getModel(String modelName);
} 