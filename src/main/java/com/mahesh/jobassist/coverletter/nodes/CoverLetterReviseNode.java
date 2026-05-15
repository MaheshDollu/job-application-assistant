package com.mahesh.jobassist.coverletter.nodes;

import com.mahesh.jobassist.common.JobAssistState;
import com.mahesh.jobassist.common.PromptOptions;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Cover Letter Revise Node
 *
 * Improvements:
 *  1. Temperature 0.6  — still creative but slightly lower than draft
 *  2. Forces model to acknowledge each piece of feedback explicitly
 *  3. Prevents wholesale rewrites — "keep what works, fix what was flagged"
 *  4. Preserves grounding rule — no invented content even during revision
 */
@Component
public class CoverLetterReviseNode implements NodeAction<JobAssistState> {

    private final ChatModel chatModel;

    public CoverLetterReviseNode(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public Map<String, Object> apply(JobAssistState state) throws Exception {
        String original = state.coverLetter().orElse("");
        String feedback = state.approvalFeedback().orElse("Please improve the letter.");
        String resume   = state.resumeText().orElse("");

        String prompt = """
                You are an expert cover letter editor.

                RULES:
                - Address EVERY point in the feedback. Do not ignore any of it.
                - Keep sections that were NOT criticised — do not rewrite the whole letter.
                - Do NOT add achievements or skills not present in the resume.
                - Output the revised letter only. No commentary, no list of changes.
                - Keep length between 250–350 words.

                ORIGINAL LETTER:
                %s

                HUMAN REVIEWER FEEDBACK:
                %s

                RESUME (for grounding — do not invent content outside this):
                %s

                Output the revised letter text only.
                """.formatted(original, feedback, resume);

        String revised = chatModel.call(new Prompt(prompt, PromptOptions.creative()))
                .getResult().getOutput().getText();

        return Map.of("coverLetter", revised);
    }
}
