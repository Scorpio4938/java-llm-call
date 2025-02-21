package io.github.scorpio4938.LLMCall.messages.prompts;

/**
 * Interface for prompts used in LLM calls.
 * 
 * @since 1.0.2
 */
public interface Prompt {
    String getContent();

    String getRole();
}