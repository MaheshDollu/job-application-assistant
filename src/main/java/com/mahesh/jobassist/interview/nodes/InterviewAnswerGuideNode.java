package com.mahesh.jobassist.interview.nodes;

import com.mahesh.jobassist.common.JobAssistState;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Node: Provides answer frameworks/tips for each interview question.
 */
@Component
public class InterviewAnswerGuideNode implements NodeAction<JobAssistState> {

    private final ChatModel chatModel;

    public InterviewAnswerGuideNode(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public Map<String, Object> apply(JobAssistState state) throws Exception {
        List<String> questions = state.interviewQuestions().orElse(List.of());
        String resume = state.resumeText().orElse("");

        String prompt = """
                You are an interview coach. For each interview question below, provide a concise answer guide
                tailored to the candidate's background.
                
                Candidate Resume Summary:
                %s
                
                Interview Questions:
                %s
                
                For each question provide:
                - Key points to mention
                - A sample opening line
                - What NOT to say
                
                Keep each answer guide to 3-4 lines.
                """.formatted(resume, String.join("\n", questions));

        String guide = chatModel.call(new Prompt(prompt))
                .getResult().getOutput().getText();

        // Appended to the list
        return Map.of("interviewQuestions", guide);
    }
}
