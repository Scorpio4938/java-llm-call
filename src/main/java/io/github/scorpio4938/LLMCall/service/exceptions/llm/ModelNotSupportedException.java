package io.github.scorpio4938.LLMCall.service.exceptions.llm;

public class ModelNotSupportedException extends LLMException {
    public ModelNotSupportedException(String message) {
        super(LLMErrorCode.MODEL_NOT_SUPPORTED, message);
    }

    public ModelNotSupportedException(String message, Throwable cause) {
        super(LLMErrorCode.MODEL_NOT_SUPPORTED, message, cause);
    }
}
