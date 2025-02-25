package io.github.scorpio4938.LLMCall.demo;

import io.github.scorpio4938.LLMCall.LLMApiClient;
import io.github.scorpio4938.LLMCall.config.DefaultRetry;
import io.github.scorpio4938.LLMCall.core.builder.LLMRequestBuilder;
import io.github.scorpio4938.LLMCall.core.providers.Providers;

import java.util.Map;

public class RetryDemo {
    public static void main(String[] args) {
        // Create providers and client
        Providers providers = new Providers();
        var provider = providers.getProvider("OLLAMA");
        LLMApiClient client = new LLMApiClient(provider);

        // Create standard retry config
        DefaultRetry standardRetry = new DefaultRetry(3, 1000); // 3 retries, 1s delay

        // Create exponential backoff retry config
        // 3 retries, 500ms initial delay, doubling each time (500ms, 1000ms, 2000ms)
        DefaultRetry exponentialRetry = DefaultRetry.withExponentialBackoff(3, 500, 2.0);

        Map<String, String> message = Map.of(
                "role", "user",
                "content", "Hello, how are you?");

        try {
            // Try with standard fixed retry
            String response1 = client.directCallLLM(new LLMRequestBuilder("deepseek-r1:1.5b")
                    .withData(message)
                    .withRetryConfig(standardRetry));
            System.out.println("Fixed retry response: " + response1);

            // Try with exponential backoff retry
            String response2 = client.directCallLLM(new LLMRequestBuilder("deepseek-r1:1.5b")
                    .withData(message)
                    .withRetryConfig(exponentialRetry));
            System.out.println("Exponential backoff response: " + response2);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 