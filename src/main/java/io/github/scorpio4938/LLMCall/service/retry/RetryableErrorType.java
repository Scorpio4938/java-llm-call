package io.github.scorpio4938.LLMCall.service.retry;

/**
 * Enum defining types of errors that can be retried
 * 
 * @since 1.0.2
 */
public enum RetryableErrorType {
    /**
     * Rate limiting errors (HTTP 429)
     */
    RATE_LIMIT,
    
    /**
     * Server-side errors (HTTP 5xx)
     */
    SERVER_ERROR,
    
    /**
     * Network communication errors
     */
    NETWORK_ERROR,
    
    /**
     * Timeout errors
     */
    TIMEOUT,
    
    /**
     * Other retryable errors
     */
    OTHER;
    
    /**
     * Whether this error type should be retried by default
     * 
     * @return true if this error type should be retried
     */
    public boolean isRetryable() {
        return this != OTHER; // All errors except OTHER are retryable by default
    }
} 