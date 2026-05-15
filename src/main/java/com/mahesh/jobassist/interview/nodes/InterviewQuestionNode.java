package com.mahesh.jobassist.interview.nodes;

import com.mahesh.jobassist.common.JobAssistState;
import com.mahesh.jobassist.common.PromptOptions;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Interview Question Node
 *
 * Improvements:
 *  1. Temperature 0.1  — question generation should be grounded, not random
 *  2. Strict typed format: [BEHAVIORAL], [TECHNICAL], etc. — parseable by frontend
 *  3. Questions are grounded in the actual JD requirements, not generic
 *  4. Few-shot example shows the exact format expected
 */
@Component
public class InterviewQuestionNode implements NodeAction<JobAssistState> {

    private final ChatModel chatModel;

    public InterviewQuestionNode(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public Map<String, Object> apply(JobAssistState state) throws Exception {
        String jobDesc  = state.jobDescription().orElse("");
        String analysis = state.resumeAnalysis().orElse("");

        String prompt = """
                You are a senior hiring manager preparing a structured interview for a specific role.

                RULES:
                - Every question must relate directly to the job description or candidate's background.
                - Do NOT generate generic filler questions like "Tell me about yourself."
                - Use EXACTLY these type labels: [BEHAVIORAL], [TECHNICAL], [SITUATIONAL], [CULTURE], [MOTIVATION]
                - Output exactly 8 questions. No more, no less.
                - Respond ONLY in the format below. No preamble.

                --- EXAMPLE FORMAT ---
                [BEHAVIORAL] Describe a time you had to deliver a project under a tight deadline with incomplete requirements. How did you handle it?
                [TECHNICAL] Walk me through how you would design a rate-limiting system for a REST API handling 10k requests/second.
                [SITUATIONAL] Your team disagrees on a technical approach and the deadline is tomorrow. What do you do?
                [CULTURE] What does a healthy engineering team culture look like to you, and how have you contributed to it?
                [MOTIVATION] What specifically about this role drew you to apply over similar positions elsewhere?
                --- END EXAMPLE ---

                Now generate questions for the REAL role below:

                JOB DESCRIPTION:
                %s

                CANDIDATE BACKGROUND SUMMARY:
                %s

                Generate exactly 8 questions:
                - 2 [BEHAVIORAL]
                - 2 [TECHNICAL]
                - 2 [SITUATIONAL]
                - 1 [CULTURE]
                - 1 [MOTIVATION]
                """.formatted(jobDesc, analysis);

        String questions = chatModel.call(new Prompt(prompt, PromptOptions.analytical()))
                .getResult().getOutput().getText();

        return Map.of("interviewQuestions", questions);
    }
}
