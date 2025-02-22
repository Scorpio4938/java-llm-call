package io.github.scorpio4938.LLMCall.core.messages;

// import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LLMRequest {
    private final String model;
    private final List<Message> messages;

    public LLMRequest(String model, List<Message> messages) {
        this.model = model;
        this.messages = messages;
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

    public static class Message {
        private final String role;
        private final String content;

        public Message(String role, String content) {
            // Validate role/content ordering
            if (role == null || role.isEmpty()) {
                throw new IllegalArgumentException("Role cannot be empty");
            }
            if (content == null || content.isEmpty()) {
                throw new IllegalArgumentException("Content cannot be empty");
            }
            
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
