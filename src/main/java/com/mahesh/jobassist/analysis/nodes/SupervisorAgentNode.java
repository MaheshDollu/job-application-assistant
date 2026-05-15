package com.mahesh.jobassist.analysis.nodes;

import com.mahesh.jobassist.common.JobAssistState;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Supervisor Agent: Reads all previous agent outputs and produces a
 * consolidated final report with an action plan for the candidate.
 */
@Component
public class SupervisorAgentNode implements NodeAction<JobAssistState> {

    private final ChatModel chatModel;

    public SupervisorAgentNode(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public Map<String, Object> apply(JobAssistState state) throws Exception {
        String name = state.candidateName().orElse("Candidate");
        String analysis = state.resumeAnalysis().orElse("Not available");
        String gaps = state.skillGaps().orElse("Not available");
        String coverLetter = state.coverLetter().orElse("Not generated");

        String prompt = """
                You are a senior career coach and supervisor. You have received reports from three specialist AI agents:
                
                1. RESUME ANALYST REPORT:
                %s
                
                2. SKILL GAP ANALYST REPORT:
                %s
                
                3. COVER LETTER (draft):
                %s
                
                Your job is to synthesize all of this into a final Career Action Plan for %s.
                
                Your report must include:
                ## Executive Summary
                (2-3 sentences on overall readiness for this role)
                
                ## Top 3 Strengths to Highlight
                (bullet points)
                
                ## Priority Action Items Before Applying
                (numbered list, most urgent first)
                
                ## 30-Day Preparation Plan
                (Week 1, Week 2, Week 3, Week 4 breakdown)
                
                ## Final Recommendation
                (Apply now / Apply after improvements / Not a good fit — with reasoning)
                
                Be direct, specific, and encouraging.
                """.formatted(analysis, gaps, coverLetter, name);

        String report = chatModel.call(new Prompt(prompt))
                .getResult().getOutput().getText();

        return Map.of("finalReport", report);
    }
}
