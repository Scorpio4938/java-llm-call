package io.github.scorpio4938.LLMCall.service.exceptions.llm;

public class RetryException extends LLMException {
    private final int retryAttempts;
    private final long retryDelay; // in milliseconds

    public RetryException(int retryAttempts, long retryDelay) {
        super(LLMErrorCode.RETRY_ERROR,
                String.format("Maximum %d retry attempts exceeded with %dms delay", 
                             retryAttempts, retryDelay));
        this.retryAttempts = retryAttempts;
        this.retryDelay = retryDelay;
    }

    public RetryException(int retryAttempts, long retryDelay, Throwable cause) {
        super(LLMErrorCode.RETRY_ERROR,
                String.format("Maximum %d retry attempts exceeded with %dms delay", 
                             retryAttempts, retryDelay),
                cause);
        this.retryAttempts = retryAttempts;
        this.retryDelay = retryDelay;
    }

    public int getRetryAttempts() {
        return retryAttempts;
    }

    public long getRetryDelay() {
        return retryDelay;
    }
} 