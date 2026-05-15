package com.mahesh.jobassist.resume.nodes;

import com.mahesh.jobassist.common.JobAssistState;
import com.mahesh.jobassist.common.PromptOptions;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * NEW — Reflection / Self-Critique Node
 *
 * Sits between ResumeAnalyzerNode and SkillGapNode.
 * Reviews the analyzer's output and corrects any hallucinations,
 * unjustified scores, or missing grounding before passing downstream.
 *
 * Graph position: analyze_resume → [reflect_resume] → identify_skill_gaps
 */
@Component
public class ResumeReflectionNode implements NodeAction<JobAssistState> {

    private final ChatModel chatModel;

    public ResumeReflectionNode(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public Map<String, Object> apply(JobAssistState state) throws Exception {
        String resume   = state.resumeText().orElse("");
        String jobDesc  = state.jobDescription().orElse("");
        String analysis = state.resumeAnalysis().orElse("");

        String prompt = """
                You are a critical reviewer checking an AI resume analysis for accuracy.

                ORIGINAL RESUME:
                %s

                JOB DESCRIPTION:
                %s

                AI ANALYSIS TO REVIEW:
                %s

                Your task — check each claim in the analysis:
                1. Is the FIT_SCORE justified by actual evidence? If not, correct it.
                2. Are all STRENGTHS actually present word-for-word in the resume? Remove any that are not.
                3. Are KEYWORD_MATCHES genuinely in the resume? Remove false matches.
                4. Are KEYWORD_GAPS actually required by the job description? Remove ones that aren't.

                If the analysis is accurate, return it unchanged.
                If there are errors, return the corrected version in the EXACT SAME FORMAT.
                No commentary — just the (possibly corrected) analysis block.
                """.formatted(resume, jobDesc, analysis);

        String verified = chatModel.call(new Prompt(prompt, PromptOptions.analytical()))
                .getResult().getOutput().getText();

        // Overwrite resumeAnalysis with the verified (possibly corrected) version
        return Map.of("resumeAnalysis", verified);
    }
}
