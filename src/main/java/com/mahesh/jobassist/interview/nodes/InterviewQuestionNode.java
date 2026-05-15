package com.mahesh.jobassist.interview.nodes;

import com.mahesh.jobassist.common.JobAssistState;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Node: Generates likely interview questions based on the job description and resume.
 */
@Component
public class InterviewQuestionNode implements NodeAction<JobAssistState> {

    private final ChatModel chatModel;

    public InterviewQuestionNode(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public Map<String, Object> apply(JobAssistState state) throws Exception {
        String jobDesc = state.jobDescription().orElse("");
        String analysis = state.resumeAnalysis().orElse("");

        String prompt = """
                You are a senior hiring manager. Based on this job description and candidate analysis,
                generate 8 interview questions the candidate is likely to face.
                
                Job Description:
                %s
                
                Candidate Analysis:
                %s
                
                Generate:
                - 2 behavioral questions (STAR format expected)
                - 2 technical/skill-based questions
                - 2 situational questions
                - 1 culture fit question
                - 1 "why this company/role" question
                
                Format each as: [TYPE] Question
                """.formatted(jobDesc, analysis);

        String questions = chatModel.call(new Prompt(prompt))
                .getResult().getOutput().getText();

        return Map.of("interviewQuestions", questions);
    }
}
