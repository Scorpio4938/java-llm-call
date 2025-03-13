package io.github.scorpio4938.LLMCall.core.providers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProviderRegistry {
    private static final Map<String, IProvider> providers = new ConcurrentHashMap<>();
    
    private ProviderRegistry() {}
    
    public static void registerProvider(IProvider provider) {
        providers.put(provider.getProvider().toLowerCase(), provider);
    }
    
    public static IProvider getProvider(String name) {
        IProvider provider = providers.get(name.toLowerCase());
        if (provider == null) {
            throw new IllegalArgumentException("No provider registered with name: " + name);
        }
        return provider;
    }
    
    public static boolean isProviderRegistered(String name) {
        return providers.containsKey(name.toLowerCase());
    }
} 