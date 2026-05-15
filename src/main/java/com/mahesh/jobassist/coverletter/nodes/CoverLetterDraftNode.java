package com.mahesh.jobassist.coverletter.nodes;

import com.mahesh.jobassist.common.JobAssistState;
import com.mahesh.jobassist.common.PromptOptions;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Cover Letter Draft Node
 *
 * Improvements:
 *  1. Temperature 0.6  — creative writing benefits from some variation
 *  2. Explicit length constraint enforced in prompt (250–350 words)
 *  3. Grounding rule: achievements must come from the resume, not be invented
 *  4. Structure template with named sections — reduces hallucinated formatting
 */
@Component
public class CoverLetterDraftNode implements NodeAction<JobAssistState> {

    private final ChatModel chatModel;

    public CoverLetterDraftNode(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public Map<String, Object> apply(JobAssistState state) throws Exception {
        String name     = state.candidateName().orElse("The Candidate");
        String resume   = state.resumeText().orElse("");
        String jobDesc  = state.jobDescription().orElse("");
        String analysis = state.resumeAnalysis().orElse("");

        String prompt = """
                You are an expert cover letter writer.

                RULES — follow strictly:
                - Every achievement or skill you mention MUST be explicitly present in the resume.
                - Do NOT invent projects, titles, or metrics not in the resume.
                - Length: 250–350 words ONLY. Count before responding.
                - Write the letter body only — no "Subject:" line, no date, no address block.
                - Tone: professional but warm, confident but not arrogant.

                CANDIDATE: %s

                VERIFIED RESUME ANALYSIS:
                %s

                FULL RESUME:
                %s

                JOB DESCRIPTION:
                %s

                Write the cover letter using this structure:
                [HOOK — 2 sentences. Reference the specific role and one thing that genuinely excites you about it.]
                [BODY PARAGRAPH 1 — Your strongest relevant achievement from the resume, with specifics.]
                [BODY PARAGRAPH 2 — Second achievement or skill that directly addresses a JD requirement.]
                [CLOSING — Confident call to action. One sentence.]

                Output the letter text only. No labels, no commentary.
                """.formatted(name, analysis, resume, jobDesc);

        String letter = chatModel.call(new Prompt(prompt, PromptOptions.creative()))
                .getResult().getOutput().getText();

        return Map.of("coverLetter", letter);
    }
}
