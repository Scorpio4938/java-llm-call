package io.github.scorpio4938.LLMCall;

import io.github.scorpio4938.LLMCall.core.providers.IProvider;
import io.github.scorpio4938.LLMCall.core.retry.DefaultRetry;

import java.time.Duration;

public class LLMApiClientBuilder {
    private IProvider provider;
    private Duration timeout = Duration.ofSeconds(30);
    private DefaultRetry retryConfig = new DefaultRetry();

    public LLMApiClientBuilder withProvider(IProvider provider) {
        this.provider = provider;
        return this;
    }

    public LLMApiClientBuilder withTimeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    public LLMApiClientBuilder withRetry(DefaultRetry retryConfig) {
        this.retryConfig = retryConfig;
        return this;
    }

    public LLMApiClient build() {
        return new LLMApiClient(provider, timeout, retryConfig);
    }
} 