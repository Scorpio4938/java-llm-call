package io.github.scorpio4938.LLMCall;

import com.sun.net.httpserver.HttpServer;

import io.github.scorpio4938.LLMCall.LLMApiClient;
import io.github.scorpio4938.LLMCall.providers.Provider;
import io.github.scorpio4938.LLMCall.providers.Providers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.time.Duration;
import java.net.http.HttpClient;

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
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            String response;
            int statusCode = 200;

            if (requestBody.contains("\"model\":\"bad-model\"")) {
                statusCode = 500;
                response = "{\"error\": \"Internal server error\"}";
            } else {
                response = "{\"choices\": [{\"message\": {\"content\": \"Hello!\"}}]}";
            }

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
            exchange.close();
        });
        server.start();

        // Create test client
        client = new LLMApiClient(new TestProvider("http://localhost:" + port + "/") {
            @Override
            public String getModel(String model) {
                return "/" + model; // Models map to different endpoints
            }
        });
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
        String result = client.directCallLLM("test-model", data);
        assertEquals("Hello!", result);
    }

    @Test
    public void testCallWithParameters() throws Exception {
        Map<String, String> data = Map.of("role", "user", "content", "Hi");
        Map<String, Object> params = Map.of("max_tokens", 50, "temperature", 0.7);

        String result = client.directCallLLM("test-model", data, params);
        assertEquals("Hello!", result);
    }

    @Test
    public void testInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> {
            client.directCallLLM("", Map.of("role", "user"));
        });
    }

    @Test
    public void testRealOllamaCall() throws Exception {
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
                "content", "Hello, how are you?");

        try {
            // Call the LLM with default parameters
            String response = client.directCallLLM("deepseek-r1:1.5b", messages);

            // Basic validation of the response
            assertNotNull(response);
            assertFalse(response.isEmpty());
            System.out.println("Ollama response: " + response);
        } catch (Exception e) {
            throw e;
        }
    }

    @Test
    public void testModelChainFallback() throws Exception {
        Map<String, String> data = Map.of("role", "user", "content", "Hi");
        String result = client.callLLM("bad-model", data)
                .withFallback("good-model")
                .execute();

        assertEquals("Hello!", result);
    }

    // @Test
    // public void testRealOpenRouterCall() throws Exception {
    // // Create the OpenRouter provider
    // Providers providers = new Providers();
    // Provider openRouterProvider = providers.getProvider("OPENROUTER");

    // // Create client with default timeout
    // LLMApiClient client = new LLMApiClient(openRouterProvider,
    // HttpClient.newBuilder()
    // .connectTimeout(Duration.ofSeconds(30))
    // .build());

    // // Create a simple message
    // Map<String, String> messages = Map.of(
    // "role", "user",
    // "content", "Hello, how are you?");

    // try {
    // // Call the LLM with default parameters
    // String response = client.callLLM("meta-llama/llama-3.2-1b-instruct:free",
    // messages);

    // // Basic validation of the response
    // assertNotNull(response);
    // assertFalse(response.isEmpty());
    // System.out.println("OpenRouter response: " + response);
    // } catch (Exception e) {
    // throw e;
    // }
    // }
}