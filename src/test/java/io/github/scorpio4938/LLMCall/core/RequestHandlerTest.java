package io.github.scorpio4938.LLMCall.core;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.scorpio4938.LLMCall.core.builder.LLMRequestBuilder;
import io.github.scorpio4938.LLMCall.core.messages.prompts.BasicPrompt;
import io.github.scorpio4938.LLMCall.core.providers.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RequestHandlerTest {
    private Provider testProvider;
    private RequestHandler handler;

    @BeforeEach
    void setUp() {
        testProvider = mock(Provider.class);
        when(testProvider.getUrl()).thenReturn("http://test-provider");
        when(testProvider.getKey()).thenReturn("test-key");
        when(testProvider.getModel("test-model")).thenReturn("test-model");
        
        handler = new RequestHandler(testProvider, HttpClient.newHttpClient());
    }

    @Test
    void shouldBuildValidRequestBody() {
        LLMRequestBuilder builder = new LLMRequestBuilder("test-model")
                .withData(Map.of("user", "Hello", "assistant", "Hi"))
                .withPrompt(new BasicPrompt())
                .withParams(Map.of("temperature", 0.7));

        String json = handler.buildRequest(builder);
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

        assertEquals("test-model", jsonObject.get("model").getAsString());
        assertTrue(jsonObject.get("messages").getAsJsonArray().size() > 0);
        assertEquals(0.7, jsonObject.get("temperature").getAsDouble());
    }

    @Test
    void shouldSortMessagesWithPromptFirst() {
        LLMRequestBuilder builder = new LLMRequestBuilder("test-model")
                .withData(Map.of("user", "Hello", "assistant", "Hi"))
                .withPrompt(new BasicPrompt("system", "Be helpful"));

        String json = handler.buildRequest(builder);
        JsonObject firstMessage = JsonParser.parseString(json)
                .getAsJsonObject()
                .get("messages").getAsJsonArray()
                .get(0).getAsJsonObject();

        assertEquals("system", firstMessage.get("role").getAsString());
        assertEquals("Be helpful", firstMessage.get("content").getAsString());
    }

    @Test
    void shouldThrowOnInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> 
            handler.buildRequest(new LLMRequestBuilder("")));
        
        assertThrows(IllegalArgumentException.class, () -> 
            handler.buildRequest(new LLMRequestBuilder("model").withData(null)));
    }
} 