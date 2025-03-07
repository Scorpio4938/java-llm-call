package io.github.scorpio4938.LLMCall.service.exceptions.llm;

public enum LLMErrorCode {
    VALIDATION_ERROR(1000, "Validation error occurred"),
    MODEL_NOT_SUPPORTED(1001, "Model is not supported"),
    PROVIDER_NOT_SUPPORTED(1002, "Provider is not supported"),
    RESPONSE_ERROR(1003, "Error in LLM response"),
    RETRY_ERROR(1004, "Maximum retry attempts exceeded"),
    GENERAL_ERROR(1999, "General LLM error");

    private final int code;
    private final String defaultMessage;

    LLMErrorCode(int code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public int getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
} 