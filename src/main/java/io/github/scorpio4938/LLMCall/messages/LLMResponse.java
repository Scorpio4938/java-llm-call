package io.github.scorpio4938.LLMCall.messages;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a response from an LLM.
 * 
 * @since 1.0.0
 */
public class LLMResponse {
    @SerializedName("id")
    private String id;

    @SerializedName("choices")
    private Choice[] choices;

    /**
     * Represents a choice from an LLM.
     * 
     * @since 1.0.0
     */
    public static class Choice {
        @SerializedName("message")
        private Message message;

        /**
         * Represents a message from an LLM.
         * 
         * @since 1.0.0
         */

        public static class Message {
            @SerializedName("role")
            private String role;
            @SerializedName("content")
            private String content;

            public String getContent() {
                return content;
            }
        }

        public Message getMessage() {
            return message;
        }

    }

    /**
     * Gets the content of the first message in the response.
     *
     * @return The content of the first message in the response
     * 
     * @since 1.0.0
     */
    public String getFirstMessageContent() {
        return choices != null && choices.length > 0 && choices[0].getMessage() != null
                ? choices[0].getMessage().getContent()
                : null;
    }
}
