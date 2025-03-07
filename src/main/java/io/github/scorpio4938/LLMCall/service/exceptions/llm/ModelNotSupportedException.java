package io.github.scorpio4938.LLMCall.service.exceptions.llm;

public class ModelNotSupportedException extends LLMException {
    private final String modelName;

    public ModelNotSupportedException(String modelName) {
        super(LLMErrorCode.MODEL_NOT_SUPPORTED, 
            String.format("Model '%s' is not supported", modelName));
        this.modelName = modelName;
    }

    public ModelNotSupportedException(String modelName, Throwable cause) {
        super(LLMErrorCode.MODEL_NOT_SUPPORTED, 
            String.format("Model '%s' is not supported", modelName), cause);
        this.modelName = modelName;
    }

    public String getModelName() { return modelName; }
}
