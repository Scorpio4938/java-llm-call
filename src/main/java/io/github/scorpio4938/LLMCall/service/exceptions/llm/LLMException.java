package io.github.scorpio4938.LLMCall.service.exceptions.llm;

public class LLMException extends RuntimeException {
    private final LLMErrorCode errorCode;

    public LLMException(LLMErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public LLMException(LLMErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public LLMException(LLMErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public LLMErrorCode getErrorCode() {
        return errorCode;
    }

    public int getCode() {
        return errorCode.getCode();
    }

    @Override
    public String toString() {
        return String.format("[%d] %s: %s", 
            errorCode.getCode(), 
            this.getClass().getSimpleName(),
            getMessage());
    }
}