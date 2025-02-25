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
    private BackoffStrategy backoffStrategy = null;

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

    /**
     * Creates a retry configuration with exponential backoff.
     *
     * @param maxRetries Number of retries
     * @param initialDelayMillis Initial delay in milliseconds
     * @param backoffFactor Factor to multiply delay by after each attempt
     * @return A new retry configuration with exponential backoff
     */
    public static DefaultRetry withExponentialBackoff(int maxRetries, long initialDelayMillis, double backoffFactor) {
        if (backoffFactor <= 1.0) {
            throw new IllegalArgumentException("Backoff factor must be greater than 1.0");
        }
        
        DefaultRetry config = new DefaultRetry(maxRetries, initialDelayMillis);
        config.setBackoffStrategy(new ExponentialBackoffStrategy(initialDelayMillis, backoffFactor));
        return config;
    }

    /**
     * Sets the backoff strategy for retries
     * 
     * @param strategy The strategy to use for calculating retry delays
     */
    public void setBackoffStrategy(BackoffStrategy strategy) {
        this.backoffStrategy = strategy;
    }

    /**
     * Gets the delay for a specific retry attempt
     * 
     * @param attempt The current attempt number (1-based)
     * @return The delay in milliseconds
     */
    public long getDelayForAttempt(int attempt) {
        if (backoffStrategy == null) {
            return retryDelayMillis;
        }
        return backoffStrategy.getDelayMillis(attempt);
    }

    /**
     * Interface for retry backoff strategies
     */
    public interface BackoffStrategy {
        long getDelayMillis(int attempt);
    }

    /**
     * Implements exponential backoff for retries
     */
    public static class ExponentialBackoffStrategy implements BackoffStrategy {
        private final long initialDelayMillis;
        private final double factor;
        
        public ExponentialBackoffStrategy(long initialDelayMillis, double factor) {
            this.initialDelayMillis = initialDelayMillis;
            this.factor = factor;
        }
        
        @Override
        public long getDelayMillis(int attempt) {
            return (long)(initialDelayMillis * Math.pow(factor, attempt - 1));
        }
    }
}