package io.github.scorpio4938.LLMCall.service.exceptions.llm;

public class NotSupportedException extends LLMException {
    private final String name;

    public NotSupportedException(LLMErrorCode errorCode, String name) {
        super(errorCode, String.format("%s '%s' is not supported", errorCode.name().split("_")[0], name));
        this.name = name;
    }

    public NotSupportedException(LLMErrorCode errorCode, String name, Throwable cause) {
        super(errorCode, String.format("%s '%s' is not supported", errorCode.name().split("_")[0], name), cause);
        this.name = name;
    }

    public String getName() {
        return name;
    }
} 