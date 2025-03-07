package io.github.scorpio4938.LLMCall.service.exceptions.llm;

public class LLMValidationException extends LLMException {
    private final String fieldName;
    private final Object invalidValue;

    public LLMValidationException(String fieldName, Object invalidValue) {
        this(fieldName, invalidValue, null);
    }

    public LLMValidationException(String message, String fieldName, Object invalidValue) {
        super(LLMErrorCode.VALIDATION_ERROR, message);
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
    }

    public LLMValidationException(String fieldName, Object invalidValue, Throwable cause) {
        super(LLMErrorCode.VALIDATION_ERROR, 
            String.format("Validation failed for field '%s' with value: %s", fieldName, invalidValue), 
            cause);
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
    }

    public String getFieldName() { return fieldName; }
    public Object getInvalidValue() { return invalidValue; }
}