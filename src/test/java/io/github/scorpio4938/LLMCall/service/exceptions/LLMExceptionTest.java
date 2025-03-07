package io.github.scorpio4938.LLMCall.service.exceptions;

import io.github.scorpio4938.LLMCall.service.exceptions.llm.*;
import io.github.scorpio4938.LLMCall.service.exceptions.llm.LLMResponseException;
import io.github.scorpio4938.LLMCall.service.exceptions.providers.ProviderNotSupportedException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LLMExceptionTest {

    @Test
    void testLLMValidationException() {
        String field = "temperature";
        Object value = 2.5;
        LLMValidationException exception = new LLMValidationException(field, value);

        assertEquals(field, exception.getFieldName());
        assertEquals(value, exception.getInvalidValue());
        assertEquals(LLMErrorCode.VALIDATION_ERROR, exception.getErrorCode());
    }

    @Test
    void testModelNotSupportedException() {
        String model = "GPT-5";
        NotSupportedException exception = new NotSupportedException(LLMErrorCode.MODEL_NOT_SUPPORTED, model);

        assertEquals(model, exception.getName());
        assertEquals(LLMErrorCode.MODEL_NOT_SUPPORTED, exception.getErrorCode());
    }

    @Test
    void testProviderNotSupportedException() {
        String provider = "CustomProvider";
        NotSupportedException exception = new NotSupportedException(LLMErrorCode.PROVIDER_NOT_SUPPORTED, provider);

        assertEquals(provider, exception.getName());
        assertEquals(LLMErrorCode.PROVIDER_NOT_SUPPORTED, exception.getErrorCode());
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
        String field = "maxTokens";
        Object value = -1;
        Throwable cause = new IllegalArgumentException();
        LLMValidationException exception = new LLMValidationException(field, value, cause);

        assertEquals(field, exception.getFieldName());
        assertEquals(value, exception.getInvalidValue());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testRetryException() {
        int attempts = 3;
        long delay = 1000;
        Throwable cause = new RuntimeException("Test cause");

        RetryException exception = new RetryException(attempts, delay);
        RetryException exceptionWithCause = new RetryException(attempts, delay, cause);

        assertEquals(LLMErrorCode.RETRY_ERROR, exception.getErrorCode());
        assertEquals(attempts, exception.getRetryAttempts());
        assertEquals(delay, exception.getRetryDelay());
        assertEquals(cause, exceptionWithCause.getCause());
    }
}