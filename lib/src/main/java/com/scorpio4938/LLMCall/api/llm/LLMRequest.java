package com.scorpio4938.LLMCall.api.llm;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LLMRequest {
    private final String model;
    private final List<Message> messages;
    private final Map<String, Object> parameters;

    public LLMRequest(String model, List<Message> messages) {
        this.model = model;
        this.messages = messages;
        this.parameters = new HashMap<>();
    }

    public void addParameters(Map<String, Object> params) {
        parameters.putAll(params);
    }

    public static Message createMessage(String role, String content) {
        return new Message(role, content);
    }

    public static class Message {
        private final String role;
        private final String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        // Getters and setters if needed
    }
}
