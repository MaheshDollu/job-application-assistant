package com.mahesh.jobassist.coverletter.nodes;

import com.mahesh.jobassist.common.JobAssistState;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Node: Drafts a tailored cover letter based on resume analysis.
 */
@Component
public class CoverLetterDraftNode implements NodeAction<JobAssistState> {

    private final ChatModel chatModel;

    public CoverLetterDraftNode(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public Map<String, Object> apply(JobAssistState state) throws Exception {
        String name = state.candidateName().orElse("The Candidate");
        String resume = state.resumeText().orElse("");
        String jobDesc = state.jobDescription().orElse("");
        String analysis = state.resumeAnalysis().orElse("");

        String prompt = """
                You are an expert cover letter writer. Write a compelling, personalized cover letter.
                
                Candidate Name: %s
                
                Resume Summary / Analysis:
                %s
                
                Job Description:
                %s
                
                Write a professional cover letter that:
                - Opens with a strong hook
                - Highlights 2-3 specific achievements relevant to the role
                - Shows genuine enthusiasm for the company/role
                - Closes with a confident call to action
                - Is between 250-350 words
                - Uses a professional but warm tone
                
                Write the letter only, no extra commentary.
                """.formatted(name, analysis, jobDesc);

        String letter = chatModel.call(new Prompt(prompt))
                .getResult().getOutput().getText();

        return Map.of("coverLetter", letter);
    }
}
