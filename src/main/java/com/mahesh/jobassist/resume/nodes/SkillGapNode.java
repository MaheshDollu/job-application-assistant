package com.mahesh.jobassist.resume.nodes;

import com.mahesh.jobassist.common.JobAssistState;
import com.mahesh.jobassist.common.PromptOptions;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Node 3 — Skill Gap Analyzer
 *
 * Improvements over original:
 *  1. Temperature 0.1  — deterministic gap detection
 *  2. Reads from verified analysis (post-reflection), not raw analyzer output
 *  3. Strict tabular format — easy to parse and display
 *  4. Few-shot example  — shows exactly what HIGH/MEDIUM/LOW means
 *  5. Explicit rule: only list gaps for skills mentioned in the JD
 */
@Component
public class SkillGapNode implements NodeAction<JobAssistState> {

    private final ChatModel chatModel;

    public SkillGapNode(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public Map<String, Object> apply(JobAssistState state) throws Exception {
        String resume   = state.resumeText().orElse("");
        String jobDesc  = state.jobDescription().orElse("");
        String analysis = state.resumeAnalysis().orElse("");

        String prompt = """
                You are a technical skills gap analyst.

                RULES:
                - Only list gaps for skills explicitly REQUIRED or PREFERRED in the job description.
                - Do NOT invent gaps for skills not mentioned in the JD.
                - Priority definition:
                    HIGH   = required by JD and completely absent from resume
                    MEDIUM = mentioned in JD and partially present or adjacent skill exists
                    LOW    = preferred/nice-to-have in JD and absent from resume
                - Respond ONLY in the exact format below. No extra text.

                --- EXAMPLE ---
                GAP: AWS
                PRIORITY: HIGH
                REASON: JD requires AWS; resume shows no cloud experience at all.
                FIX: Complete AWS Cloud Practitioner cert (2–4 weeks, free tier available).

                GAP: Docker
                PRIORITY: HIGH
                REASON: JD requires containerisation; not mentioned anywhere in resume.
                FIX: Docker Getting Started tutorial + build one containerised side project (1 week).

                GAP: GraphQL
                PRIORITY: LOW
                REASON: JD lists as "nice to have"; candidate has REST experience which is transferable.
                FIX: GraphQL official tutorial (2–3 days).
                --- END EXAMPLE ---

                Now analyze the REAL data:

                VERIFIED RESUME ANALYSIS:
                %s

                FULL RESUME TEXT (for cross-reference):
                %s

                JOB DESCRIPTION:
                %s

                List every skill gap in the EXACT format above, sorted HIGH → MEDIUM → LOW.
                """.formatted(analysis, resume, jobDesc);

        String gaps = chatModel.call(new Prompt(prompt, PromptOptions.analytical()))
                .getResult().getOutput().getText();

        return Map.of("skillGaps", gaps);
    }
}
