package com.mahesh.jobassist.resume.nodes;

import com.mahesh.jobassist.common.JobAssistState;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Node 2: Identifies skill gaps between resume and job description.
 * Uses tool calling pattern — simulates checking a skills database.
 */
@Component
public class SkillGapNode implements NodeAction<JobAssistState> {

    private final ChatModel chatModel;

    public SkillGapNode(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public Map<String, Object> apply(JobAssistState state) throws Exception {
        String resume = state.resumeText().orElse("");
        String jobDesc = state.jobDescription().orElse("");
        String analysis = state.resumeAnalysis().orElse("");

        String prompt = """
                Based on this resume analysis:
                %s
                
                And the job description:
                %s
                
                Identify skill gaps and provide:
                1. Missing Technical Skills (specific tools/technologies not in resume)
                2. Missing Soft Skills
                3. Experience gaps (years, domain, seniority)
                4. Recommended courses or certifications to fill each gap
                5. Priority level for each gap: HIGH / MEDIUM / LOW
                
                Format as a clear, actionable list.
                """.formatted(analysis, jobDesc);

        String gaps = chatModel.call(new Prompt(prompt))
                .getResult().getOutput().getText();

        return Map.of("skillGaps", gaps);
    }
}
