package io.github.scorpio4938.LLMCall.service.exceptions.message;

import io.github.scorpio4938.LLMCall.service.exceptions.llm.LLMException;
import io.github.scorpio4938.LLMCall.service.exceptions.llm.LLMErrorCode;

public class LLMResponseException extends LLMException {
    private final int statusCode;
    private final String responseBody;

    public LLMResponseException(String message, int statusCode, String responseBody) {
        super(LLMErrorCode.RESPONSE_ERROR, message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public LLMResponseException(String message, int statusCode, String responseBody, Throwable cause) {
        super(LLMErrorCode.RESPONSE_ERROR, message, cause);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
