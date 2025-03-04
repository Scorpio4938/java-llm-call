package io.github.scorpio4938.LLMCall.service.exceptions.llm;

public class LLMException extends RuntimeException {
    public LLMException(String message) {
        super(message);
    }

    public LLMException(String message, Throwable cause) {
        super(message, cause);
    }
}