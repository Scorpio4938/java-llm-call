package io.github.scorpio4938.LLMCall.service.exceptions;

import io.github.scorpio4938.LLMCall.service.exceptions.llm.*;
import io.github.scorpio4938.LLMCall.service.exceptions.llm.LLMResponseException;
import io.github.scorpio4938.LLMCall.service.exceptions.providers.ProviderNotSupportedException;
import io.github.scorpio4938.LLMCall.service.exceptions.retry.RetryException;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LLMExceptionTest {

    private void assertNotSupportedException(NotSupportedException exception, LLMErrorCode expectedCode,
            String expectedName) {
        assertEquals(expectedName, exception.getUnsupportedValue());
        assertEquals(expectedCode, exception.getErrorCode());
        assertEquals(expectedCode.getCode(), exception.getCode());
    }

    private void assertValidationException(LLMValidationException exception, String expectedField,
            Object expectedValue) {
        assertEquals(expectedField, exception.getFieldName());
        assertEquals(expectedValue, exception.getInvalidValue());
        assertEquals(LLMErrorCode.VALIDATION_ERROR, exception.getErrorCode());
    }

    private void assertRetryExceptionProperties(RetryException exception, int expectedAttempts, long expectedDelay) {
        assertEquals(LLMErrorCode.RETRY_ERROR, exception.getErrorCode());
        assertEquals(expectedAttempts, exception.getRetryAttempts());
        assertEquals(expectedDelay, exception.getRetryDelay());
    }

    @Test
    void testLLMValidationException() {
        String field = "temperature";
        Object value = 2.5;
        assertValidationException(new LLMValidationException(field, value), field, value);
    }

    @Test
    void testModelNotSupportedException() {
        String model = "GPT-5";
        assertNotSupportedException(new ModelNotSupportedException(model),
                LLMErrorCode.MODEL_NOT_SUPPORTED, model);
    }

    @Test
    void testProviderNotSupportedException() {
        String provider = "CustomProvider";
        assertNotSupportedException(new ProviderNotSupportedException(provider),
                LLMErrorCode.PROVIDER_NOT_SUPPORTED, provider);
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

        assertValidationException(exception, field, value);
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testRetryException() {
        int attempts = 3;
        long delay = 1000;
        Throwable cause = new RuntimeException("Test cause");

        RetryException exception = new RetryException(attempts, delay);
        RetryException exceptionWithCause = new RetryException(attempts, delay, cause);

        assertRetryExceptionProperties(exception, attempts, delay);
        assertRetryExceptionProperties(exceptionWithCause, attempts, delay);
        assertEquals(cause, exceptionWithCause.getCause());
    }
}