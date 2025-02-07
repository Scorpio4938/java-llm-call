package com.scorpio4938.LLMCall.providers;

import java.util.List;

/**
 * Interface class for llm providers.
 *
 */
public class Provider {
    private String provider;
    private String url;
    private String key;
    private List<String> models;

    public Provider(String provider, String url, String key, List<String> models) {
        this.provider = provider;
        this.url = url;
        this.key = key;
        this.models = models;
    }

    // Getters
    public String getProvider() {
        return provider;
    }

    public String getUrl() {
        return url;
    }

    public String getKey() {
        return key;
    }

    public List<String> getModels() {
        return models;
    }

    public String getModel(String modelName) {
        for (String model : models) {
            if (model.equals(modelName)) {
                return model;
            }
        }
        throw new IllegalArgumentException("Unsupported model: " + modelName);
    }
}
