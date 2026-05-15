package com.mahesh.jobassist.resume.nodes;

import com.mahesh.jobassist.common.JobAssistState;
import com.mahesh.jobassist.common.PromptOptions;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Format Enforcer Node (replaces the aggressive self-critique node)
 *
 * The previous reflection node rewrote scores and added verbose commentary
 * ("Corrected changes: 1. 2. 3...") which broke downstream parsing.
 *
 * This version does ONE thing only:
 *   - If output already starts with FIT_SCORE → pass it through unchanged.
 *   - If output has preamble/commentary wrapped around it → strip to clean block.
 *   - Content (scores, keywords, strengths) is NEVER altered.
 *
 * Graph: analyze_resume → enforce_format → identify_skill_gaps
 */
@Component
public class ResumeReflectionNode implements NodeAction<JobAssistState> {

    private final ChatModel chatModel;

    public ResumeReflectionNode(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public Map<String, Object> apply(JobAssistState state) throws Exception {
        String analysis = state.resumeAnalysis().orElse("");

        // Fast path — already clean, skip the LLM call entirely
        if (analysis.trim().startsWith("FIT_SCORE")) {
            return Map.of("resumeAnalysis", analysis);
        }

        // Slow path — strip preamble/commentary without changing values
        String prompt = """
                The text below is a resume analysis that contains extra commentary, preamble,
                or numbered explanations wrapped around the actual analysis block.

                Your ONLY job: extract the block that starts with FIT_SCORE: and ends after
                KEYWORD_GAPS:. Return that block exactly as written — do not change any values,
                scores, keywords, or wording. Remove everything outside the block.

                TEXT:
                %s
                """.formatted(analysis);

        String cleaned = chatModel.call(new Prompt(prompt, PromptOptions.analytical()))
                .getResult().getOutput().getText();

        return Map.of("resumeAnalysis", cleaned.trim());
    }
}
