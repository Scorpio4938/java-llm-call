package io.github.scorpio4938.LLMCall.demo;

import io.github.scorpio4938.LLMCall.LLMApiClient;
import io.github.scorpio4938.LLMCall.providers.Providers;

import java.util.Map;

public class OllamaDemo {
    public static void main(String[] args) {
        // Create providers and get Ollama
        Providers providers = new Providers();
        var ollamaProvider = providers.getProvider("OLLAMA");
        
        // Create client with Ollama provider
        LLMApiClient client = new LLMApiClient(ollamaProvider);
        
        try {
            // Simple single call
            Map<String, String> message = Map.of(
                "role", "user",
                "content", "Hello, how are you?"
            );
            
            String response = client.directCallLLM("qwen2.5:0.5b", message);
            System.out.println("LLM Response: " + response);
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 