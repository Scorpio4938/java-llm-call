package io.github.scorpio4938.LLMCall.core.messages;

import org.junit.jupiter.api.Test;

import io.github.scorpio4938.LLMCall.core.messages.LLMRequest;

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
        // Remove this test completely as parameters are now handled differently
        // Test coverage moved to RequestHandler tests
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