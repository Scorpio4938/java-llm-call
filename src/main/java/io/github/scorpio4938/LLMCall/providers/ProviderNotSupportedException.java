package io.github.scorpio4938.LLMCall.providers;

import io.github.scorpio4938.LLMCall.service.exceptions.NotSupportException;

public class ProviderNotSupportedException extends NotSupportException {
    public ProviderNotSupportedException(String providerName) {
        super("Provider not supported: " + providerName);
    }

    public ProviderNotSupportedException(Provider provider) {
        super("Provider not supported: " + provider.getProvider());
    }

}
