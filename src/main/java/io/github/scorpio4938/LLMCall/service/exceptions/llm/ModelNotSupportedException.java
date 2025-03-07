package io.github.scorpio4938.LLMCall.service.exceptions.llm;

public class ModelNotSupportedException extends NotSupportedException {
    public ModelNotSupportedException(String modelName) {
        super(LLMErrorCode.MODEL_NOT_SUPPORTED, modelName);
    }

    public ModelNotSupportedException(String modelName, Throwable cause) {
        super(LLMErrorCode.MODEL_NOT_SUPPORTED, modelName, cause);
    }
    
    public String getModelName() { 
        return super.getUnsupportedValue(); 
    }
}
