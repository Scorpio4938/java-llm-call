package io.github.scorpio4938.LLMCall.service.exceptions.llm;

public class LLMValidationException extends LLMException {
    private final String fieldName;
    private final Object invalidValue;

    public LLMValidationException(String fieldName, Object invalidValue) {
        super(LLMErrorCode.VALIDATION_ERROR,
                String.format("Validation failed for field '%s' with value: %s", fieldName, invalidValue));
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
    }

    public LLMValidationException(String fieldName, Object invalidValue, Throwable cause) {
        super(LLMErrorCode.VALIDATION_ERROR,
                String.format("Validation failed for field '%s' with value: %s", fieldName, invalidValue), cause);
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
    }

    // public LLMValidationException(String message) {
    // super(LLMErrorCode.VALIDATION_ERROR, message);
    // }

    public String getFieldName() {
        return fieldName;
    }

    public Object getInvalidValue() {
        return invalidValue;
    }
}