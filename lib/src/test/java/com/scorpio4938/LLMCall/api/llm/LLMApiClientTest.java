package com.scorpio4938.LLMCall.api.llm;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.time.Duration;
import java.net.http.HttpClient;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This test uses an embedded HTTP server to simulate the LLM API.
 */
public class LLMApiClientTest {

    private HttpServer server;
    private int port;
    private LLMApiClient client;

    @BeforeEach
    public void setUp() throws IOException {
        // Start a simple HTTP server
        server = HttpServer.create(new InetSocketAddress(0), 0);
        port = server.getAddress().getPort();

        // Setup a basic response handler
        server.createContext("/", exchange -> {
            String response = "{\"choices\": [{\"message\": {\"content\": \"Hello!\"}}]}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
            exchange.close();
        });
        server.start();

        // Create test client
        client = new LLMApiClient(new TestProvider("http://localhost:" + port + "/"));
    }

    @AfterEach
    public void tearDown() {
        server.stop(0);
    }

    // Simple test provider
    private static class TestProvider extends Provider {
        TestProvider(String url) {
            super("test-provider", url, "test-key", java.util.List.of("test-model"));
        }

        @Override
        public String getModel(String model) {
            return model;
        }
    }

    @Test
    public void testBasicCall() throws Exception {
        Map<String, String> data = Map.of("role", "user", "content", "Hi");
        String result = client.callLLM("test-model", data);
        assertEquals("Hello!", result);
    }

    @Test
    public void testCallWithParameters() throws Exception {
        Map<String, String> data = Map.of("role", "user", "content", "Hi");
        Map<String, Object> params = Map.of("max_tokens", 50, "temperature", 0.7);

        String result = client.callLLM("test-model", data, params);
        assertEquals("Hello!", result);
    }

    @Test
    public void testInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> {
            client.callLLM("", Map.of("role", "user"));
        });
    }

    @Test
    public void testRealOllamaCall() throws Exception {
        // Verify Ollama is running
        // try {
        //     HttpClient.newHttpClient().send(
        //         HttpRequest.newBuilder()
        //             .uri(URI.create("http://localhost:11434"))
        //             .GET()
        //             .build(),
        //         HttpResponse.BodyHandlers.ofString()
        //     );
        // } catch (Exception e) {
        //     System.err.println("Ollama is not running. Please start it with 'ollama serve'");
        //     return; // Skip test if Ollama isn't running
        // }

        // Create the Ollama provider
        Providers providers = new Providers();
        Provider ollamaProvider = providers.getProvider("OLLAMA");
        
        // Create client with default timeout
        LLMApiClient client = new LLMApiClient(ollamaProvider, HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build());

        // Create a simple message
        Map<String, String> messages = Map.of(
            "role", "user",
            "content", "Hello, how are you?"
        );

        try {
            // Call the LLM with default parameters
            String response = client.callLLM("deepseek-r1:1.5b", messages);
            
            // Basic validation of the response
            assertNotNull(response);
            assertFalse(response.isEmpty());
            System.out.println("Ollama response: " + response);
        } catch (Exception e) {
            throw e;
        }
    }

    @Test
    public void testRealOpenRouterCall() throws Exception {
        // Create the OpenRouter provider
        Providers providers = new Providers();
        Provider openRouterProvider = providers.getProvider("OPENROUTER");

        // Create client with default timeout
        LLMApiClient client = new LLMApiClient(openRouterProvider, HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build());

        // Create a simple message
        Map<String, String> messages = Map.of(
                "role", "user",
                "content", "Hello, how are you?");

        try {
            // Call the LLM with default parameters
            String response = client.callLLM("meta-llama/llama-3.2-1b-instruct:free", messages);

            // Basic validation of the response
            assertNotNull(response);
            assertFalse(response.isEmpty());
            System.out.println("OpenRouter response: " + response);
        } catch (Exception e) {
            throw e;
        }
    }
}