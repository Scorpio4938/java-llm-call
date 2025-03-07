package io.github.scorpio4938.LLMCall.service.exceptions.llm;

public abstract class NotSupportedException extends LLMException {
    private final String unsupportedValue;

    public NotSupportedException(LLMErrorCode errorCode, String unsupportedValue) {
        super(errorCode, String.format("%s '%s' is not supported", 
            errorCode.name().split("_")[0], 
            unsupportedValue));
        this.unsupportedValue = unsupportedValue;
    }

    public NotSupportedException(LLMErrorCode errorCode, String unsupportedValue, Throwable cause) {
        super(errorCode, String.format("%s '%s' is not supported", 
            errorCode.name().split("_")[0], 
            unsupportedValue), 
            cause);
        this.unsupportedValue = unsupportedValue;
    }

    public String getUnsupportedValue() {
        return unsupportedValue;
    }
} 