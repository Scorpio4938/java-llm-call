package io.github.scorpio4938.LLMCall.core.providers;

import java.util.List;

/**
 * Interface class for llm providers.
 *
 */
public class Provider implements IProvider {
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

    @Override
    public String getProvider() {
        return provider;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public List<String> getModels() {
        return models;
    }

    @Override
    public String getModel(String modelName) {
        for (String model : models) {
            if (model.equals(modelName)) {
                return model;
            }
        }
        throw new ModelNotSupportedException(modelName);
    }
}
