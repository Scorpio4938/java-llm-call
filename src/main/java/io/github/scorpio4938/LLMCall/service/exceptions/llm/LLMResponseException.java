package io.github.scorpio4938.LLMCall.service.exceptions.llm;

public class LLMResponseException extends LLMException {
    private final int statusCode;
    private final String responseBody;

    public LLMResponseException(String message, int statusCode, String responseBody) {
        this(message, statusCode, responseBody, null);
    }

    public LLMResponseException(String message, int statusCode, String responseBody, Throwable cause) {
        super(LLMErrorCode.RESPONSE_ERROR, message, cause);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() { return statusCode; }
    public String getResponseBody() { return responseBody; }
} 