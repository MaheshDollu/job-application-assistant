package com.mahesh.jobassist.resume.nodes;

import com.mahesh.jobassist.common.JobAssistState;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Node 1: Analyzes the resume and extracts key skills, experience, and strengths.
 */
@Component
public class ResumeAnalyzerNode implements NodeAction<JobAssistState> {

    private final ChatModel chatModel;

    public ResumeAnalyzerNode(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public Map<String, Object> apply(JobAssistState state) throws Exception {
        String resume = state.resumeText().orElse("");
        String jobDesc = state.jobDescription().orElse("Not provided");

        String prompt = """
                You are an expert resume analyst. Analyze the following resume against the job description.
                
                RESUME:
                %s
                
                JOB DESCRIPTION:
                %s
                
                Provide a structured analysis with:
                1. Key Strengths (3-5 bullet points)
                2. Relevant Experience matches
                3. Overall fit score (1-10) with justification
                4. Top 3 keywords from the job description present in the resume
                
                Be concise and actionable.
                """.formatted(resume, jobDesc);

        String analysis = chatModel.call(new Prompt(prompt))
                .getResult().getOutput().getText();

        return Map.of("resumeAnalysis", analysis);
    }
}
