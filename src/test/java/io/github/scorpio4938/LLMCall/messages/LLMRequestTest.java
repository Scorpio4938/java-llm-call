package io.github.scorpio4938.LLMCall.messages;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class LLMRequestTest {

    @Test
    void testRequestCreation() {
        List<LLMRequest.Message> messages = List.of(
            LLMRequest.createMessage("user", "Hello"),
            LLMRequest.createMessage("assistant", "Hi there!")
        );
        
        LLMRequest request = new LLMRequest("test-model", messages);
        
        assertEquals("test-model", request.getModel());
        assertEquals(2, request.getMessages().size());
    }

    @Test
    void testAddParameters() {
        LLMRequest request = new LLMRequest("model", List.of());
        Map<String, Object> params = Map.of(
            "temperature", 0.7,
            "max_tokens", 100
        );
        
        request.addParameters(params);
        
        Map<String, Object> result = request.getParameters();
        assertEquals(0.7, result.get("temperature"));
        assertEquals(100, result.get("max_tokens"));
    }

    @Test
    void testCreateMessage() {
        LLMRequest.Message message = LLMRequest.createMessage("system", "You are helpful");
        
        assertEquals("system", message.getRole());
        assertEquals("You are helpful", message.getContent());
    }

    @Test
    void testMessageClass() {
        LLMRequest.Message message = new LLMRequest.Message("user", "What's the weather?");
        
        assertNotNull(message);
        assertEquals("user", message.getRole());
        assertEquals("What's the weather?", message.getContent());
    }

    // Add getters in LLMRequest class for test access
    // (These should be added to the main LLMRequest.java class)
} 