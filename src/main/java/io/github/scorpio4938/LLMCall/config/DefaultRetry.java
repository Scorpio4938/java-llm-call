package io.github.scorpio4938.LLMCall.config;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Configuration for HTTP request behavior including retries and timeouts.
 * 
 * @since 1.0.2
 */
public class DefaultRetry implements RetryConfig {
    private int maxRetries;
    private long retryDelayMillis;
    private Duration connectionTimeout;

    /**
     * Creates a default configuration.
     */
    public DefaultRetry() {
        this(3, 1000); // Default: 3 retries with 1 second delay
    }

    /**
     * Creates a retry configuration with specified parameters.
     *
     * @param maxRetries Number of retries
     * @param delay      Delay duration
     * @param unit       Time unit for delay
     */
    public DefaultRetry(int maxRetries, long delay, TimeUnit unit) {
        this(maxRetries, unit.toMillis(delay));
    }

    /**
     * Creates a retry configuration with specified parameters.
     *
     * @param maxRetries       Number of retries
     * @param retryDelayMillis Delay in milliseconds
     */
    public DefaultRetry(int maxRetries, long retryDelayMillis) {
        this.connectionTimeout = Duration.ofSeconds(30); // Default timeout
        update(maxRetries, retryDelayMillis);
    }

    /**
     * Updates retry configuration.
     *
     * @param maxRetries       Number of retries
     * @param retryDelayMillis Delay in milliseconds
     */
    public void update(int maxRetries, long retryDelayMillis) {
        validateParams(maxRetries, retryDelayMillis);
        this.maxRetries = maxRetries;
        this.retryDelayMillis = retryDelayMillis;
    }

    public void setConnectionTimeout(Duration timeout) {
        if (timeout == null || timeout.isNegative()) {
            throw new IllegalArgumentException("Invalid timeout value");
        }
        this.connectionTimeout = timeout;
    }

    private void validateParams(int maxRetries, long retryDelayMillis) {
        if (maxRetries < 0) {
            throw new IllegalArgumentException("Max retries cannot be negative");
        }
        if (retryDelayMillis < 0) {
            throw new IllegalArgumentException("Retry delay cannot be negative");
        }
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public long getRetryDelayMillis() {
        return retryDelayMillis;
    }

    public Duration getConnectionTimeout() {
        return connectionTimeout;
    }
}