package com.mahesh.jobassist.resume.nodes;

import com.mahesh.jobassist.common.JobAssistState;
import com.mahesh.jobassist.common.PromptOptions;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Resume Analyzer Node
 *
 * Strict output format with colons enforced on every field.
 * Few-shot example is the first thing the model sees — it copies the pattern exactly.
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
                You are a resume analyst. Analyze the resume against the job description.

                RULES:
                - Only mention skills EXPLICITLY present in the resume text — no inference.
                - Every field below is REQUIRED. Every field MUST have a colon after the label.
                - Output ONLY the block below — no preamble, no commentary, no explanation after.
                - STRENGTHS must be a bullet list using "* " prefix.

                --- EXAMPLE OUTPUT (copy this format exactly) ---
                FIT_SCORE: 6/10
                JUSTIFICATION: Strong Python/Django match. Missing AWS and Docker which are required.
                STRENGTHS:
                * Python + Django: directly matches core requirement
                * REST API experience: relevant to the role
                * PostgreSQL: useful backend skill
                KEYWORD_MATCHES: Python, Django, REST APIs, PostgreSQL
                KEYWORD_GAPS: AWS, Docker
                --- END EXAMPLE ---

                RESUME:
                %s

                JOB DESCRIPTION:
                %s

                Output the analysis block now. Start with FIT_SCORE: and end after KEYWORD_GAPS:
                """.formatted(resume, jobDesc);

        String analysis = chatModel.call(new Prompt(prompt, PromptOptions.analytical()))
                .getResult().getOutput().getText();

        return Map.of("resumeAnalysis", analysis);
    }
}
