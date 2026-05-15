package com.mahesh.jobassist.analysis.nodes;

import com.mahesh.jobassist.common.JobAssistState;
import com.mahesh.jobassist.common.PromptOptions;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Supervisor Agent Node
 *
 * Improvements:
 *  1. Temperature 0.1  — final report must be consistent and factual
 *  2. Strict section format — each section has a defined structure
 *  3. Final recommendation is forced to one of 3 options with reasoning
 *  4. 30-day plan is concretely structured by week
 *  5. Grounding rule: every point must trace back to previous agent outputs
 */
@Component
public class SupervisorAgentNode implements NodeAction<JobAssistState> {

    private final ChatModel chatModel;

    public SupervisorAgentNode(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public Map<String, Object> apply(JobAssistState state) throws Exception {
        String name       = state.candidateName().orElse("Candidate");
        String analysis   = state.resumeAnalysis().orElse("Not available");
        String gaps       = state.skillGaps().orElse("Not available");
        String coverLetter = state.coverLetter().orElse("Not generated");

        String prompt = """
                You are a senior career coach writing a final Career Action Plan.
                You have received verified reports from three specialist AI agents.

                RULES:
                - Every claim must trace back to the agent reports below. Do not add information.
                - Final Recommendation must be EXACTLY one of: "APPLY NOW" / "APPLY AFTER IMPROVEMENTS" / "NOT A GOOD FIT"
                - Be direct and specific — no filler phrases like "it's important to note that..."
                - Follow the exact section format below.

                --- AGENT REPORTS ---

                [RESUME ANALYSIS]
                %s

                [SKILL GAP ANALYSIS]
                %s

                [COVER LETTER DRAFT]
                %s

                --- END REPORTS ---

                Write the Career Action Plan for %s using EXACTLY this structure:

                ## Executive Summary
                [2–3 sentences: overall readiness, strongest asset, biggest blocker]

                ## Top 3 Strengths to Lead With
                - [Strength 1: specific skill/experience from resume + why it matters for this role]
                - [Strength 2]
                - [Strength 3]

                ## Priority Action Items Before Applying
                1. [Most urgent — HIGH priority gap from skill gap report]
                2. [Second most urgent]
                3. [Third]

                ## 30-Day Preparation Plan
                Week 1: [Specific tasks — what to learn/do]
                Week 2: [Specific tasks]
                Week 3: [Specific tasks]
                Week 4: [Specific tasks — final prep, application submission]

                ## Final Recommendation
                VERDICT: [APPLY NOW / APPLY AFTER IMPROVEMENTS / NOT A GOOD FIT]
                REASONING: [2 sentences explaining the verdict based solely on the reports above]
                """.formatted(analysis, gaps, coverLetter, name);

        String report = chatModel.call(new Prompt(prompt, PromptOptions.analytical()))
                .getResult().getOutput().getText();

        return Map.of("finalReport", report);
    }
}
