package com.mahesh.jobassist.common;

import org.springframework.ai.ollama.api.OllamaOptions;

/**
 * Centralised temperature constants.
 * - ANALYTICAL (0.1) : resume scoring, skill gaps, reflection — needs consistency
 * - CREATIVE   (0.6) : cover letters, interview guides — benefits from variety
 * - CHAT       (0.3) : conversational Q&A — balanced
 */
public final class PromptOptions {

    private PromptOptions() {}

    /** For resume analysis, skill gap, reflection — deterministic scoring */
    public static OllamaOptions analytical() {
        return OllamaOptions.builder()
                .temperature(0.1)
                .build();
    }

    /** For cover letters, interview answer guides — some creativity is good */
    public static OllamaOptions creative() {
        return OllamaOptions.builder()
                .temperature(0.6)
                .build();
    }

    /** For persistent chat sessions — balanced */
    public static OllamaOptions chat() {
        return OllamaOptions.builder()
                .temperature(0.3)
                .build();
    }
}
