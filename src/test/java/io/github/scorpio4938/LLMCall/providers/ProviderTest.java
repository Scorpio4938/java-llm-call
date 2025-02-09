package io.github.scorpio4938.LLMCall.providers;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ProviderTest {

    @Test
    void testConstructorAndGetters() {
        Provider provider = new Provider("TEST", "http://test.com", "test-key", List.of("model1", "model2"));
        
        assertEquals("TEST", provider.getProvider());
        assertEquals("http://test.com", provider.getUrl());
        assertEquals("test-key", provider.getKey());
        assertEquals(2, provider.getModels().size());
    }

    @Test
    void testGetModels() {
        List<String> models = List.of("modelA", "modelB", "modelC");
        Provider provider = new Provider("TEST", "", "", models);
        
        assertEquals(models, provider.getModels());
        assertThrows(UnsupportedOperationException.class, () -> 
            provider.getModels().add("new-model"));
    }

    @Test
    void testGetModelValid() {
        Provider provider = new Provider("TEST", "", "", List.of("llama3", "mistral"));
        
        assertEquals("llama3", provider.getModel("llama3"));
        assertEquals("mistral", provider.getModel("mistral"));
    }

    @Test
    void testGetModelInvalid() {
        Provider provider = new Provider("TEST", "", "", List.of("deepseek"));
        
        Exception exception = assertThrows(ModelNotSupportedException.class, () -> 
            provider.getModel("invalid-model"));
        
        assertEquals("Model not supported: invalid-model", exception.getMessage());
    }
} 