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

    @SerializedName("object")
    private String objectType;

    @SerializedName("created")
    private long created;

    @SerializedName("model")
    private String model;

    @SerializedName("usage")
    private Usage usage;

    @SerializedName("response_format")
    private ResponseFormat responseFormat;

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

    public String getObjectType() {
        return objectType;
    }

    public long getCreated() {
        return created;
    }

    public String getModel() {
        return model;
    }

    public Usage getUsage() {
        return usage;
    }

    public ResponseFormat getResponseFormat() {
        return responseFormat;
    }

    /**
     * Represents the usage of an LLM.
     * 
     * @since 1.0.2
     */
    public static class Usage {
        @SerializedName("prompt_tokens")
        private int promptTokens;
        @SerializedName("completion_tokens")
        private int completionTokens;
        @SerializedName("total_tokens")
        private int totalTokens;

        public int getPromptTokens() {
            return promptTokens;
        }

        public int getCompletionTokens() {
            return completionTokens;
        }

        public int getTotalTokens() {
            return totalTokens;
        }
    }

    public static class ResponseFormat {
        @SerializedName("type")
        private String type;

        public String getType() {
            return type;
        }
    }
}
