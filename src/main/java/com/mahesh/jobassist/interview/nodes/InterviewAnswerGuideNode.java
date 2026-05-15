package com.mahesh.jobassist.interview.nodes;

import com.mahesh.jobassist.common.JobAssistState;
import com.mahesh.jobassist.common.PromptOptions;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Interview Answer Guide Node
 *
 * Improvements:
 *  1. Temperature 0.6  — coaching tone benefits from some warmth/variety
 *  2. Strict per-question format — easy to render in a UI question-by-question
 *  3. AVOID section — what NOT to say is highly actionable and often skipped
 *  4. Grounds advice in candidate's actual resume, not generic tips
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
        String resume          = state.resumeText().orElse("");

        String prompt = """
                You are an expert interview coach tailoring advice to a specific candidate.

                RULES:
                - Advice must draw from the candidate's actual resume — do not invent experiences.
                - For each question, follow the EXACT format below.
                - Keep each section concise: KEY_POINTS max 3 bullets, OPENING max 1 sentence, AVOID max 2 bullets.

                --- FORMAT PER QUESTION ---
                QUESTION: [repeat the question here]
                KEY_POINTS:
                - [most important thing to mention, tied to candidate's background]
                - [second point]
                - [third point]
                OPENING: [a strong first sentence the candidate could actually say]
                AVOID:
                - [common mistake or irrelevant tangent]
                - [second thing to avoid]
                ---

                CANDIDATE RESUME:
                %s

                INTERVIEW QUESTIONS:
                %s

                Generate a guide for EACH question in the format above.
                """.formatted(resume, String.join("\n", questions));

        String guide = chatModel.call(new Prompt(prompt, PromptOptions.creative()))
                .getResult().getOutput().getText();

        return Map.of("interviewQuestions", guide);
    }
}
