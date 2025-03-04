package io.github.scorpio4938.LLMCall.core.builder;

import io.github.scorpio4938.LLMCall.core.messages.prompts.BasicPrompt;
import io.github.scorpio4938.LLMCall.core.messages.prompts.Prompt;
import io.github.scorpio4938.LLMCall.core.retry.DefaultRetry;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class LLMRequestBuilderTest {
    private final Prompt testPrompt = new BasicPrompt();
    private final DefaultRetry testRetry = new DefaultRetry(3, 100, TimeUnit.MILLISECONDS);
    
    @Test
    void shouldBuildValidRequest() {
        LLMRequestBuilder builder = new LLMRequestBuilder("test-model")
                .withData(Map.of("key", "value"))
                .withPrompt(testPrompt)
                .withMaxTokens(100)
                .withTemperature(0.7)
                .withTopP(0.9)
                .withRetryConfig(testRetry)
                .withFallback("fallback-model-1", "fallback-model-2")
                .build();

        assertEquals("test-model", builder.getModel());
        assertEquals("value", builder.getData().get("key"));
        assertEquals(testPrompt, builder.getPrompt());
        assertEquals(100, builder.getParams().get("max_tokens"));
        assertEquals(0.7, builder.getParams().get("temperature"));
        assertEquals(0.9, builder.getParams().get("top_p"));
        assertEquals(testRetry, builder.getRetryConfig());
        assertEquals(3, builder.getModels().size());
    }

    @Test
    void shouldThrowOnInvalidModel() {
        assertThrows(NullPointerException.class, () -> new LLMRequestBuilder(null));
    }

    @Test
    void shouldHandleCustomParameters() {
        LLMRequestBuilder builder = new LLMRequestBuilder("model")
                .withCustomParam("frequency_penalty", 0.5)
                .withParams(Map.of("presence_penalty", 0.2))
                .build();

        assertEquals(0.5, builder.getParams().get("frequency_penalty"));
        assertEquals(0.2, builder.getParams().get("presence_penalty"));
    }

    @Test
    void shouldValidateParameterRanges() {
        assertAll(
            () -> assertThrows(IllegalArgumentException.class, () -> 
                new LLMRequestBuilder("model").withMaxTokens(0).build()),
            () -> assertThrows(IllegalArgumentException.class, () -> 
                new LLMRequestBuilder("model").withTemperature(2.1).build()),
            () -> assertThrows(IllegalArgumentException.class, () -> 
                new LLMRequestBuilder("model").withTopP(1.1).build())
        );
    }

    @Test
    void shouldReturnImmutableParamsCopy() {
        LLMRequestBuilder builder = new LLMRequestBuilder("model").build();
        Map<String, Object> params = builder.getParams();
        
        assertThrows(UnsupportedOperationException.class, () -> 
            params.put("test", "value"));
    }

    @Test
    void shouldHandleFallbackModels() {
        LLMRequestBuilder builder = new LLMRequestBuilder("primary")
                .withFallback("fallback1", "fallback2")
                .build();

        assertEquals(3, builder.getModels().size());
        assertEquals("primary", builder.getModels().get(0));
        assertEquals("fallback1", builder.getModels().get(1));
        assertEquals("fallback2", builder.getModels().get(2));
    }

    @Test
    void shouldCloneWithNewModel() {
        LLMRequestBuilder original = new LLMRequestBuilder("model1")
                .withData(Map.of("key", "value"))
                .withPrompt(testPrompt)
                .withMaxTokens(100)
                .withFallback("fallback1")
                .build();

        LLMRequestBuilder clone = original.cloneWithModel("model2");

        assertEquals("model2", clone.getModel());
        assertEquals("value", clone.getData().get("key"));
        assertEquals(testPrompt, clone.getPrompt());
        assertEquals(100, clone.getParams().get("max_tokens"));
        assertEquals(2, clone.getModels().size());
        assertEquals("fallback1", clone.getModels().get(1));
    }

    @Test
    void shouldPreventModificationAfterBuild() {
        LLMRequestBuilder builder = new LLMRequestBuilder("model").build();
        
        assertAll(
            () -> assertThrows(IllegalStateException.class, () -> 
                builder.withData(Map.of("key", "value"))),
            () -> assertThrows(IllegalStateException.class, () -> 
                builder.withPrompt(testPrompt)),
            () -> assertThrows(IllegalStateException.class, () -> 
                builder.withMaxTokens(100))
        );
    }

    @Test
    void shouldHandleRetryConfiguration() {
        DefaultRetry customRetry = new DefaultRetry(5, 200, TimeUnit.MILLISECONDS);
        LLMRequestBuilder builder = new LLMRequestBuilder("model")
                .withRetryConfig(customRetry)
                .build();

        assertEquals(customRetry, builder.getRetryConfig());
    }

    @Test
    void shouldHandleEmptyFallbackModels() {
        LLMRequestBuilder builder = new LLMRequestBuilder("model")
                .withFallback()
                .build();

        assertEquals(1, builder.getModels().size());
    }

    @Test
    void shouldHandleNullFallbackModels() {
        assertThrows(NullPointerException.class, () -> 
            new LLMRequestBuilder("model").withFallback((String[]) null));
    }
} 