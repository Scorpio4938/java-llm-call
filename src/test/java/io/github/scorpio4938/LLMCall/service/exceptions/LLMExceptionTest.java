package io.github.scorpio4938.LLMCall.service.exceptions;

import io.github.scorpio4938.LLMCall.service.exceptions.llm.*;
import io.github.scorpio4938.LLMCall.service.exceptions.message.LLMResponseException;
import io.github.scorpio4938.LLMCall.service.exceptions.providers.ProviderNotSupportedException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LLMExceptionTest {

    @Test
    void testLLMValidationException() {
        String message = "Invalid parameter";
        LLMValidationException exception = new LLMValidationException(message);
        
        assertEquals(message, exception.getMessage());
        assertEquals(LLMErrorCode.VALIDATION_ERROR, exception.getErrorCode());
        assertEquals(LLMErrorCode.VALIDATION_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testModelNotSupportedException() {
        String message = "GPT-5 not supported";
        ModelNotSupportedException exception = new ModelNotSupportedException(message);
        
        assertEquals(message, exception.getMessage());
        assertEquals(LLMErrorCode.MODEL_NOT_SUPPORTED, exception.getErrorCode());
        assertEquals(LLMErrorCode.MODEL_NOT_SUPPORTED.getCode(), exception.getCode());
    }

    @Test
    void testProviderNotSupportedException() {
        String message = "Custom provider not supported";
        ProviderNotSupportedException exception = new ProviderNotSupportedException(message);
        
        assertEquals(message, exception.getMessage());
        assertEquals(LLMErrorCode.PROVIDER_NOT_SUPPORTED, exception.getErrorCode());
        assertEquals(LLMErrorCode.PROVIDER_NOT_SUPPORTED.getCode(), exception.getCode());
    }

    @Test
    void testLLMResponseException() {
        String message = "API request failed";
        int statusCode = 429;
        String responseBody = "Rate limit exceeded";
        
        LLMResponseException exception = new LLMResponseException(message, statusCode, responseBody);
        
        assertEquals(message, exception.getMessage());
        assertEquals(LLMErrorCode.RESPONSE_ERROR, exception.getErrorCode());
        assertEquals(LLMErrorCode.RESPONSE_ERROR.getCode(), exception.getCode());
        assertEquals(statusCode, exception.getStatusCode());
        assertEquals(responseBody, exception.getResponseBody());
    }

    @Test
    void testExceptionWithCause() {
        String message = "Validation failed";
        Throwable cause = new IllegalArgumentException("Original error");
        LLMValidationException exception = new LLMValidationException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals(LLMErrorCode.VALIDATION_ERROR, exception.getErrorCode());
    }
} 