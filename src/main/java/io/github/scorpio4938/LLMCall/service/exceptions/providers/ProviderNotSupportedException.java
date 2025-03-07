package io.github.scorpio4938.LLMCall.service.exceptions.providers;

import io.github.scorpio4938.LLMCall.service.exceptions.llm.LLMErrorCode;
import io.github.scorpio4938.LLMCall.service.exceptions.llm.NotSupportedException;

public class ProviderNotSupportedException extends NotSupportedException {
    public ProviderNotSupportedException(String providerName) {
        super(LLMErrorCode.PROVIDER_NOT_SUPPORTED, providerName);
    }

    public ProviderNotSupportedException(String providerName, Throwable cause) {
        super(LLMErrorCode.PROVIDER_NOT_SUPPORTED, providerName, cause);
    }
    
    public String getProviderName() { 
        return super.getName(); 
    }
}
