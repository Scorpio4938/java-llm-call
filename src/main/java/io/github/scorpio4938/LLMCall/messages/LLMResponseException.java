package io.github.scorpio4938.LLMCall.messages;

import java.net.http.HttpResponse;

public class LLMResponseException extends RuntimeException {
    private HttpResponse<String> response;
    private int statusCode;
    private String responseBody;

    public LLMResponseException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public LLMResponseException(String message, HttpResponse<String> response) {
        super(message);
        this.response = response;
        this.statusCode = response.statusCode();
        this.responseBody = response.body();
    }

    public LLMResponseException(int statusCode) {
        super("LLM Request Failed with status code: " + statusCode);
        this.statusCode = statusCode;
    }

    public LLMResponseException(HttpResponse<String> response) {
        super("LLM Request Failed with status code: " + response.statusCode());
        this.response = response;
        this.responseBody = response.body();
        this.statusCode = response.statusCode();
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
