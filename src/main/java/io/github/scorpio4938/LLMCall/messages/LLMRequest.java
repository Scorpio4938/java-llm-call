package io.github.scorpio4938.LLMCall.messages;

// import javax.annotation.Nullable;
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

    /**
     * Adds parameters to the request.
     *
     * @param params The parameters to add
     * 
     * @since 1.0.0
     */
    public void addParameters(Map<String, Object> params) {
        if (params != null) {
            parameters.putAll(params);
        }
    }

    /**
     * Creates a new message.
     *
     * @param role    The role of the message
     * @param content The content of the message
     * 
     * @since 1.0.0
     */
    public static Message createMessage(String role, String content) {
        return new Message(role, content);
    }

    public String getModel() {
        return model;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public static class Message {
        private final String role;
        private final String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public String getContent() {
            return content;
        }

        // Getters and setters if needed
    }
}
