package io.github.scorpio4938.LLMCall.providers;

import io.github.scorpio4938.LLMCall.service.exceptions.NotSupportException;

public class ModelNotSupportedException extends NotSupportException {
    public ModelNotSupportedException(String modelName) {
        super("Model not supported: " + modelName);
    }
}
