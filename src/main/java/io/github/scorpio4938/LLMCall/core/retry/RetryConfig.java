package io.github.scorpio4938.LLMCall.core.retry;

import java.time.Duration;

/**
 * Interface defining retry configuration behavior.
 * 
 * @since 1.0.2
 */
public interface RetryConfig {

    /**
     * Get the maximum number of retries.
     * 
     * @return The maximum number of retries.
     * 
     * @since 1.0.2
     */
    int getMaxRetries();

    /**
     * Get the retry delay in milliseconds.
     * 
     * @return The retry delay in milliseconds.
     * 
     * @since 1.0.2
     */
    long getRetryDelayMillis();

    /**
     * Get the connection timeout.
     * 
     * @return The connection timeout.
     * 
     * @since 1.0.2
     */
    Duration getConnectionTimeout();

    /**
     * Set the connection timeout.
     * 
     * @param timeout The connection timeout.
     * 
     * @since 1.0.2
     */
    void setConnectionTimeout(Duration timeout);

    /**
     * Update the retry configuration.
     * 
     * @param maxRetries       The maximum number of retries.
     * @param retryDelayMillis The retry delay in milliseconds.
     * 
     * @since 1.0.2
     */
    void update(int maxRetries, long retryDelayMillis);
}