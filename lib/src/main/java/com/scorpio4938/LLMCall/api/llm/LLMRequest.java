package com.scorpio4938.LLMCall.api.llm;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LLMRequest {
    private String model;
    private List<Message> messages;
    private int maxTokens;

    public LLMRequest(String model, @Nullable List<Message> messages, @Nullable Integer maxTokens) {
        this.model = model;
        this.messages = messages;
        this.maxTokens = (maxTokens != null) ? maxTokens : 100;
    }

    public static Message createMessage(String role, String content) {
        return new Message(role, content);
    }

    public static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        // Getters and setters if needed
    }
}
