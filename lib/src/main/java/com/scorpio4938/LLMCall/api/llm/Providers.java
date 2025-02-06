package com.scorpio4938.LLMCall.api.llm;

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
        this.addProviderWithV1("OLLAMA", "http://192.168.3.113:11434", null,
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
     */
    public void addProviderWithV1(String provider, String baseUrl, String keyName, List<String> models) {
        String fullUrl = baseUrl.endsWith("/") ? baseUrl + "v1/chat/completions" : baseUrl + "/v1/chat/completions";
        this.addProvider(provider, fullUrl, keyName, models);
    }

    /**
     * Get the specified provider.
     *
     * @param provider The name of the provider.
     * @return The provider object.
     * @throws IllegalArgumentException If the provider is not supported.
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
     */
    public List<Provider> getProviders() {
        return this.providers;
    }
}
