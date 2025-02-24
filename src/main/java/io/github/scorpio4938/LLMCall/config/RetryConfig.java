package io.github.scorpio4938.LLMCall.config;

import java.time.Duration;

/**
 * Interface defining retry configuration behavior.
 * 
 * @since 1.0.2
 */
public interface RetryConfig {
    int getMaxRetries();

    long getRetryDelayMillis();

    Duration getConnectionTimeout();

    void setConnectionTimeout(Duration timeout);

    void update(int maxRetries, long retryDelayMillis);
}