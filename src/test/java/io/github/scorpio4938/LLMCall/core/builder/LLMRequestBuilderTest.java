package io.github.scorpio4938.LLMCall.core.builder;

import io.github.scorpio4938.LLMCall.core.messages.prompts.BasicPrompt;
import io.github.scorpio4938.LLMCall.core.messages.prompts.Prompt;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LLMRequestBuilderTest {
    private final Prompt testPrompt = new BasicPrompt();
    
    @Test
    void shouldBuildValidRequest() {
        LLMRequestBuilder builder = new LLMRequestBuilder("test-model")
                .withData(Map.of("key", "value"))
                .withPrompt(testPrompt)
                .withMaxTokens(100)
                .withTemperature(0.7)
                .withTopP(0.9);

        assertEquals("test-model", builder.getModel());
        assertEquals("value", builder.getData().get("key"));
        assertEquals(testPrompt, builder.getPrompt());
        assertEquals(100, builder.getParams().get("max_tokens"));
        assertEquals(0.7, builder.getParams().get("temperature"));
        assertEquals(0.9, builder.getParams().get("top_p"));
    }

    @Test
    void shouldThrowOnInvalidModel() {
        assertThrows(NullPointerException.class, () -> new LLMRequestBuilder(null));
    }

    @Test
    void shouldHandleCustomParameters() {
        LLMRequestBuilder builder = new LLMRequestBuilder("model")
                .withCustomParam("frequency_penalty", 0.5)
                .withParams(Map.of("presence_penalty", 0.2));

        assertEquals(0.5, builder.getParams().get("frequency_penalty"));
        assertEquals(0.2, builder.getParams().get("presence_penalty"));
    }

    @Test
    void shouldValidateParameterRanges() {
        assertAll(
            () -> assertThrows(IllegalArgumentException.class, () -> 
                new LLMRequestBuilder("model").withMaxTokens(0)),
            () -> assertThrows(IllegalArgumentException.class, () -> 
                new LLMRequestBuilder("model").withTemperature(2.1)),
            () -> assertThrows(IllegalArgumentException.class, () -> 
                new LLMRequestBuilder("model").withTopP(1.1))
        );
    }

    @Test
    void shouldReturnImmutableParamsCopy() {
        LLMRequestBuilder builder = new LLMRequestBuilder("model");
        Map<String, Object> params = builder.getParams();
        
        assertThrows(UnsupportedOperationException.class, () -> 
            params.put("test", "value"));
    }
} 