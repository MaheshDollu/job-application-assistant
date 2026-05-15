package com.mahesh.jobassist.analysis.nodes;

import com.mahesh.jobassist.common.JobAssistState;
import com.mahesh.jobassist.common.PromptOptions;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Chat Agent Node
 *
 * Improvements:
 *  1. Temperature 0.3  — conversational but grounded
 *  2. Explicit grounding rule: "if not in the report, say so"
 *  3. Context window management — trims history to last 10 turns to avoid
 *     overflowing the context and getting degraded responses
 *  4. Clearer system role separation from conversation history
 */
@Component
public class ChatAgentNode implements NodeAction<JobAssistState> {

    private static final int MAX_HISTORY_TURNS = 10;

    private final ChatModel chatModel;

    public ChatAgentNode(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public Map<String, Object> apply(JobAssistState state) throws Exception {
        List<String> fullHistory = state.chatHistory().orElse(List.of());
        String report            = state.finalReport().orElse("");

        // Trim to last MAX_HISTORY_TURNS to avoid context overflow
        List<String> trimmedHistory = fullHistory.size() > MAX_HISTORY_TURNS
                ? fullHistory.subList(fullHistory.size() - MAX_HISTORY_TURNS, fullHistory.size())
                : fullHistory;

        String historyText = trimmedHistory.stream()
                .collect(Collectors.joining("\n"));

        String prompt = """
                You are a career advisor helping a candidate understand and act on their job application analysis.

                RULES:
                - Answer questions using the career report below as your primary source.
                - If the answer isn't in the report, say "That's not covered in your report, but generally..." and give your best general advice.
                - Be concise — 3–5 sentences per response unless a detailed breakdown is asked for.
                - Never make up specific numbers (salaries, timelines) unless they're in the report.

                CANDIDATE CAREER REPORT:
                %s

                CONVERSATION SO FAR:
                %s

                Respond to the candidate's latest message above.
                """.formatted(report, historyText);

        String answer = chatModel.call(new Prompt(prompt, PromptOptions.chat()))
                .getResult().getOutput().getText();

        return Map.of("chatHistory", "ASSISTANT: " + answer);
    }
}
