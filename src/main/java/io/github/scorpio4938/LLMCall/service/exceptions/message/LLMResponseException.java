package io.github.scorpio4938.LLMCall.service.exceptions.message;

import java.net.http.HttpResponse;

import io.github.scorpio4938.LLMCall.service.exceptions.llm.LLMException;

public class LLMResponseException extends LLMException {
    private final HttpResponse<String> response;
    private final int statusCode;
    private final String responseBody;

    public LLMResponseException(String message, int statusCode) {
        super(formatMessage(message, statusCode));
        this.statusCode = statusCode;
        this.response = null;
        this.responseBody = null;
    }

    public LLMResponseException(HttpResponse<String> response) {
        super(formatMessage("LLM Request Failed", response.statusCode()));
        this.response = response;
        this.responseBody = response.body();
        this.statusCode = response.statusCode();
    }

    private static String formatMessage(String message, int statusCode) {
        return String.format("%s (Status: %d)", message, statusCode);
    }

    public HttpResponse<String> getResponse() {
        return response;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
