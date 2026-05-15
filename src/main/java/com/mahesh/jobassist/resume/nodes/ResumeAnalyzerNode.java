package com.mahesh.jobassist.resume.nodes;

import com.mahesh.jobassist.common.JobAssistState;
import com.mahesh.jobassist.common.PromptOptions;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Node 1 — Resume Analyzer
 *
 * Improvements over original:
 *  1. Temperature 0.1  — analytical task needs consistency, not creativity
 *  2. Strict output format with exact section headers the next node can parse
 *  3. Few-shot example  — shows the model exactly what a good response looks like
 *  4. Explicit grounding rules  — "only cite skills that are literally present"
 */
@Component
public class ResumeAnalyzerNode implements NodeAction<JobAssistState> {

    private final ChatModel chatModel;

    public ResumeAnalyzerNode(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public Map<String, Object> apply(JobAssistState state) throws Exception {
        String resume  = state.resumeText().orElse("");
        String jobDesc = state.jobDescription().orElse("Not provided");

        String prompt = """
                You are an expert resume analyst. Your job is to objectively evaluate a resume
                against a job description.

                RULES — follow these strictly:
                - Only mention skills that are EXPLICITLY present in the resume text.
                - Do NOT infer, assume, or hallucinate experience that isn't written.
                - Fit score must be justified by specific evidence from both documents.
                - Respond ONLY in the exact format shown below. No preamble, no extra text.

                --- EXAMPLE (do not use this data in your answer) ---
                RESUME EXAMPLE:
                "3 years Python developer. Django, PostgreSQL, Redis. Built REST APIs."

                JOB DESCRIPTION EXAMPLE:
                "Backend Engineer — Python, Django, AWS, Docker required."

                CORRECT OUTPUT FORMAT:
                FIT_SCORE: 6/10
                JUSTIFICATION: Strong Python/Django match (explicitly listed). Missing AWS and Docker which are required — significant gaps.
                STRENGTHS:
                - Python + Django: directly matches core requirement
                - REST API experience: relevant to the role
                - PostgreSQL: useful backend skill
                KEYWORD_MATCHES: Python, Django, REST APIs, PostgreSQL
                KEYWORD_GAPS: AWS, Docker
                --- END EXAMPLE ---

                Now analyze the REAL data below:

                RESUME:
                %s

                JOB DESCRIPTION:
                %s

                Respond in the EXACT format above. No extra commentary.
                """.formatted(resume, jobDesc);

        String analysis = chatModel.call(new Prompt(prompt, PromptOptions.analytical()))
                .getResult().getOutput().getText();

        return Map.of("resumeAnalysis", analysis);
    }
}
