package io.github.scorpio4938.LLMCall.core.messages.prompts;

// Basic implementation with default behavior guidelines
public class BasicPrompt implements Prompt {
    private final String content;
    private final String role;

    // Default constructor with basic guidelines
    public BasicPrompt() {
        this("system", "Always respond in a simple, concise, and legitimate manner");
    }

    public BasicPrompt(String content) {
        this("system", content);
    }

    public BasicPrompt(String role, String content) {
        this.role = role;
        this.content = content;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public String getRole() {
        return role;
    }
}