package io.github.scorpio4938.LLMCall.service.exceptions.providers;

import io.github.scorpio4938.LLMCall.service.exceptions.llm.LLMException;
import io.github.scorpio4938.LLMCall.service.exceptions.llm.LLMErrorCode;

public class ProviderNotSupportedException extends LLMException {
    public ProviderNotSupportedException(String message) {
        super(LLMErrorCode.PROVIDER_NOT_SUPPORTED, message);
    }

    public ProviderNotSupportedException(String message, Throwable cause) {
        super(LLMErrorCode.PROVIDER_NOT_SUPPORTED, message, cause);
    }
}
