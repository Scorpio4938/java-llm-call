package com.scorpio4938.LLMCall.api.llm;

import com.scorpio4938.LLMCall.service.utils.debug.Debugger;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This test uses an embedded HTTP server to simulate the LLM API.
 */
public class LLMApiClientTest {

    private HttpServer server;
    private int port;

    @BeforeEach
    public void setUp() throws IOException {
        // Create an HTTP server on a random available port.
        server = HttpServer.create(new InetSocketAddress(0), 0);
        port = server.getAddress().getPort();
        // Create a context to handle POST requests at the root path
        server.createContext("/", (HttpExchange exchange) -> {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                // Define a fixed JSON response that the LLMResponse parser is expected to handle.
                String response = "{\"choices\": [{\"message\": {\"content\": \"Hello, test!\"}}]}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    public void tearDown() {
        server.stop(0);
    }

    // Dummy Provider implementation for the test.
    private static class DummyProvider extends Provider {
        private final String url;

        DummyProvider(String url) {
            // Call the Provider constructor with required dummy values.
            super("dummy-provider", url, "dummy-key", java.util.List.of("test-model"));
            this.url = url;
        }

        @Override
        public String getModel(String model) {
            // For testing, just return the given model.
            return model;
        }

        @Override
        public String getUrl() {
            return url;
        }

        @Override
        public String getKey() {
            // Return a dummy key.
            return "dummy-key";
        }
    }

    @Test
    public void testCallLLM() throws Exception {
        // Create a provider targeting our local test HTTP server.
        String serverUrl = "http://localhost:" + port + "/";
        Provider dummyProvider = new DummyProvider(serverUrl);

        // Create an instance of LLMApiClient with a test maxTokens value.
        LLMApiClient client = new LLMApiClient(dummyProvider, 50);

        // Create a dummy data map. The keys/values here will be sorted in the request.
        Map<String, String> data = Map.of(
                "role", "system",
                "content", "Test content"
        );

        // Call the LLM client which sends the HTTP request and parse the JSON response.
        String result = client.callLLM("test-model", data);

        // Verify that the first message's content is as expected.
        assertEquals("Hello, test!", result);
        // Debugger.log("Call LLM Test Response: " + result);
    }

    @Test
    public void testCallLLM_Ollama() throws Exception {
        // Create a provider targeting our local test HTTP server.
        Providers providers = new Providers();
        Provider ollamaProvider = providers.getProvider("OLLAMA");

        // Create an instance of LLMApiClient with a test maxTokens value.
        LLMApiClient client = new LLMApiClient(ollamaProvider, 50);

        // Create a dummy data map. The keys/values here will be sorted in the request.
        Map<String, String> data = Map.of(
                "role", "user",
                "content", "hello"
        );

        // Call the LLM client which sends the HTTP request and parse the JSON response.
        String result = client.callLLM("deepseek-r1:1.5b", data);

        // Log the result using the Debugger class.
        Debugger.log("OLLAMA Response: " + result);
    }
} 