package com.mahesh.jobassist.common;

import org.springframework.ai.openai.OpenAiChatOptions;

/**
 * Centralised temperature constants.
 * - ANALYTICAL (0.1) : resume scoring, skill gaps, reflection — needs consistency
 * - CREATIVE   (0.6) : cover letters, interview guides — benefits from variety
 * - CHAT       (0.3) : conversational Q&A — balanced
 */
public final class PromptOptions {

    private PromptOptions() {}

    /** For resume analysis, skill gap, reflection — deterministic scoring */
    public static OpenAiChatOptions analytical() {
        return OpenAiChatOptions.builder()
                .temperature(0.1)
                .build();
    }

    /** For cover letters, interview answer guides — some creativity is good */
    public static OpenAiChatOptions creative() {
        return OpenAiChatOptions.builder()
                .temperature(0.6)
                .build();
    }

    /** For persistent chat sessions — balanced */
    public static OpenAiChatOptions chat() {
        return OpenAiChatOptions.builder()
                .temperature(0.3)
                .build();
    }
}
// TODO: test change for code review agent
