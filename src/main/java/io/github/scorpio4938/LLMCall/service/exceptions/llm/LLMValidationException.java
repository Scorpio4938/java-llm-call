package io.github.scorpio4938.LLMCall.service.exceptions.llm;

public class LLMValidationException extends LLMException {
    public LLMValidationException(String message) {
        super(LLMErrorCode.VALIDATION_ERROR, message);
    }

    public LLMValidationException(String message, Throwable cause) {
        super(LLMErrorCode.VALIDATION_ERROR, message, cause);
    }
}