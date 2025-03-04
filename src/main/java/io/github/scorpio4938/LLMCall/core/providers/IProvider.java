package io.github.scorpio4938.LLMCall.core.providers;

import java.util.List;

import io.github.scorpio4938.LLMCall.service.exceptions.llm.ModelNotSupportedException;

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
     * 
     * @since 1.0.2
     */
    String getProvider();

    /**
     * Gets the provider API URL.
     *
     * @return The API URL
     * 
     * @since 1.0.2
     */
    String getUrl();

    /**
     * Gets the API key for the provider.
     *
     * @return The API key
     * 
     * @since 1.0.2
     */
    String getKey();

    /**
     * Gets the list of supported models.
     *
     * @return List of supported model names
     * 
     * @since 1.0.2
     */
    List<String> getModels();

    /**
     * Gets a specific model by name.
     *
     * @param modelName The name of the model to retrieve
     * @return The model name if supported
     * @throws ModelNotSupportedException if the model is not supported
     * 
     * @since 1.0.2
     */
    String getModel(String modelName);
}