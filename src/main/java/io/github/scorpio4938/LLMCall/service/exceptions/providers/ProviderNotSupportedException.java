package io.github.scorpio4938.LLMCall.service.exceptions.providers;

import io.github.scorpio4938.LLMCall.service.exceptions.llm.LLMException;
import io.github.scorpio4938.LLMCall.service.exceptions.llm.LLMErrorCode;

public class ProviderNotSupportedException extends LLMException {
    private final String providerName;

    public ProviderNotSupportedException(String providerName) {
        super(LLMErrorCode.PROVIDER_NOT_SUPPORTED, 
            String.format("Provider '%s' is not supported", providerName));
        this.providerName = providerName;
    }

    public ProviderNotSupportedException(String providerName, Throwable cause) {
        super(LLMErrorCode.PROVIDER_NOT_SUPPORTED, 
            String.format("Provider '%s' is not supported", providerName), cause);
        this.providerName = providerName;
    }

    public String getProviderName() { return providerName; }
}
