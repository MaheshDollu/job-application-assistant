package com.mahesh.jobassist.coverletter.nodes;

import com.mahesh.jobassist.common.JobAssistState;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Node: Revises the cover letter based on human feedback.
 * Only runs if approved=false (rejected with feedback).
 */
@Component
public class CoverLetterReviseNode implements NodeAction<JobAssistState> {

    private final ChatModel chatModel;

    public CoverLetterReviseNode(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public Map<String, Object> apply(JobAssistState state) throws Exception {
        String originalLetter = state.coverLetter().orElse("");
        String feedback = state.approvalFeedback().orElse("Please improve the letter.");

        String prompt = """
                You are an expert cover letter editor.
                
                Original Cover Letter:
                %s
                
                Human Reviewer Feedback:
                %s
                
                Please revise the cover letter based on the feedback above.
                Keep what works, fix what was flagged.
                Return only the revised letter, no commentary.
                """.formatted(originalLetter, feedback);

        String revised = chatModel.call(new Prompt(prompt))
                .getResult().getOutput().getText();

        return Map.of("coverLetter", revised);
    }
}
