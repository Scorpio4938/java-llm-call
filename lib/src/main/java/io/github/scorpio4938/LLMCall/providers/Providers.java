package com.scorpio4938.LLMCall.providers;

import io.github.cdimascio.dotenv.Dotenv;

import java.util.*;

/**
 * List of all the default llm providers (can add customs providers).
 *
 */
public class Providers {
    private List<Provider> providers = new ArrayList<>();

    public Providers() {
        this.config();
    }

    private void config() {
        this.addProvider("DEEPSEEK", "https://api.deepseek.com", "DEEPSEEK_API_KEY",
                List.of("deepseek-chat", "deepseek-coder"));
        this.addProviderWithV1("MOONSHOT", "https://api.moonshot.cn", "MOONSHOT_API_KEY",
                List.of("moonshot-v1-8k", "moonshot-v1-32k", "moonshot-v1-128k"));

        this.addProviderWithV1("OPENROUTER", "https://openrouter.ai", "OPEN_ROUTER_API_KEY",
                List.of("google/gemini-exp-1206:free", "google/gemini-2.0-flash-exp:free",

                        "meta-llama/llama-3.2-1b-instruct:free"));
        this.addProviderWithV1("OLLAMA", "http://localhost:11434", null,
                List.of("deepseek-r1:1.5b", "qwen2.5:0.5b", "qwen2.5-coder:3b", "llama3.2:3b"));

    }

    /**
     * Add a new provider and its corresponding API key.
     *
     * @param provider The name of the provider.
     * @param url      The URL of the provider's API.
     * @param keyName  The name of the environment variable that contains the API
     *                 key.
     * @param models   The list
     * 
     * @since 1.0.0
     */
    public void addProvider(String provider, String url, String keyName, List<String> models) {
        // System.out.println("Working directory: " + System.getProperty("user.dir"));
        Dotenv dotenv = Dotenv.configure().directory("./").ignoreIfMissing().load();
        this.providers.add(new Provider(provider, url, keyName != null ? dotenv.get(keyName) : null, models));
    }

    /**
     * Add a new provider with default v1/chat/completions endpoint.
     *
     * @param provider The name of the provider.
     * @param baseUrl  The base URL of the provider's API (without endpoint).
     * @param keyName  The name of the environment variable that contains the API
     *                 key.
     * @param models   The list of supported models.
     * 
     * @since 1.0.0
     */
    public void addProviderWithV1(String provider, String baseUrl, String keyName, List<String> models) {
        String fullUrl = baseUrl.endsWith("/") ? baseUrl + "v1/chat/completions" : baseUrl + "/v1/chat/completions";
        this.addProvider(provider, fullUrl, keyName, models);
    }

    /**
     * Set a specific provider without removing others.
     * 
     * @param provider The name of the provider to set
     * @param url      The API URL for the provider
     * @param keyName  The environment variable name for the API key
     * @param models   List of supported models
     * 
     * @since 1.0.0
     */
    public void setProvider(String provider, String url, String keyName, List<String> models) {
        // Remove existing provider if it exists
        providers.removeIf(p -> p.getProvider().equals(provider));
        // Add new provider
        this.addProvider(provider, url, keyName, models);
    }

    /**
     * Set a specific provider with v1 endpoint without removing others.
     * 
     * @param provider The name of the provider to set
     * @param baseUrl  The base URL of the provider's API (without endpoint)
     * @param keyName  The environment variable name for the API key
     * @param models   List of supported models
     * 
     * @since 1.0.0
     */
    public void setProviderWithV1(String provider, String baseUrl, String keyName, List<String> models) {
        // Remove existing provider if it exists
        providers.removeIf(p -> p.getProvider().equals(provider));
        // Add new provider with v1 endpoint
        this.addProviderWithV1(provider, baseUrl, keyName, models);
    }

    /**
     * Get the specified provider.
     *
     * @param provider The name of the provider.
     * @return The provider object.
     * @throws IllegalArgumentException If the provider is not supported.
     * 
     * @since 1.0.0
     */
    public Provider getProvider(String provider) {
        for (Provider provider1 : providers) {
            if (provider1.getProvider().equals(provider)) {
                return provider1;
            }
        }
        throw new IllegalArgumentException("Unsupported provider: " + provider);
    }

    /**
     * Get all available providers.
     *
     * @return A list of all available providers.
     * 
     * @since 1.0.0
     */
    public List<Provider> getProviders() {
        return this.providers;
    }

}
