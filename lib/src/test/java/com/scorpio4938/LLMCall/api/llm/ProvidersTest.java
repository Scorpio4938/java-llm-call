package com.scorpio4938.LLMCall.api.llm;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ProvidersTest {

    @Test
    void shouldContainDefaultProviders() {
        Providers providers = new Providers();
        List<Provider> providerList = providers.getProviders();

        assertFalse(providerList.isEmpty(), "Providers list should not be empty");
        assertTrue(providerList.size() >= 4, "Should contain at least 4 default providers");
    }

    @Test
    void shouldRetrieveProviderByName() {
        Providers providers = new Providers();

        Provider ollama = providers.getProvider("OLLAMA");
        assertNotNull(ollama, "OLLAMA provider should exist");
        assertEquals("http://localhost:11434", ollama.getUrl());
        assertNull(ollama.getKey(), "OLLAMA key should be null");

        Provider deepseek = providers.getProvider("DEEPSEEK");
        assertNotNull(deepseek, "DEEPSEEK provider should exist");
        assertEquals("https://api.deepseek.com", deepseek.getUrl());
    }

    @Test
    void shouldThrowForInvalidProvider() {
        Providers providers = new Providers();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            providers.getProvider("INVALID_PROVIDER");
        });

        assertEquals("Unsupported provider: INVALID_PROVIDER", exception.getMessage());
    }

    @Test
    void shouldContainCorrectModelsForOllama() {
        Providers providers = new Providers();
        Provider ollama = providers.getProvider("OLLAMA");

        List<String> models = ollama.getModels();
        assertTrue(models.contains("deepseek-r1:1.5b"));
        assertTrue(models.contains("qwen2.5:0.5b"));
        assertTrue(models.contains("qwen2.5-coder:3b"));
        assertTrue(models.contains("llama3.2:3b"));
    }

    @Test
    void shouldAddCustomProvider() {
        System.out.println("Working dir: " + System.getProperty("user.dir"));
        Providers providers = new Providers();
        String customProviderName = "CUSTOM_LLM";

        providers.addProvider(customProviderName,
                "http://custom-llm.com",
                "CUSTOM_KEY",
                List.of("custom-model"));

        Provider customProvider = providers.getProvider(customProviderName);
        assertEquals(customProviderName, customProvider.getProvider());
        assertEquals("http://custom-llm.com", customProvider.getUrl());
        // assertEquals("test_custom_key_value", customProvider.getKey());
        assertTrue(customProvider.getModels().contains("custom-model"));
    }

    @Test
    void shouldAddProviderWithV1Endpoint() {
        Providers providers = new Providers();
        String customProviderName = "CUSTOM_V1";

        providers.addProviderWithV1(customProviderName,
                "http://custom-v1.com",
                "CUSTOM_V1_KEY",
                List.of("v1-model"));

        Provider customProvider = providers.getProvider(customProviderName);
        assertEquals(customProviderName, customProvider.getProvider());
        assertEquals("http://custom-v1.com/v1/chat/completions", customProvider.getUrl());
        // assertEquals("test_custom_key_value", customProvider.getKey());
        assertTrue(customProvider.getModels().contains("v1-model"));
    }
}