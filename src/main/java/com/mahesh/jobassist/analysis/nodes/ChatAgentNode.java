package com.mahesh.jobassist.analysis.nodes;

import com.mahesh.jobassist.common.JobAssistState;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Chat Agent: Persistent conversational agent that remembers the full session.
 * Uses checkpointing so conversations persist across API calls.
 */
@Component
public class ChatAgentNode implements NodeAction<JobAssistState> {

    private final ChatModel chatModel;

    public ChatAgentNode(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public Map<String, Object> apply(JobAssistState state) throws Exception {
        List<String> history = state.chatHistory().orElse(List.of());
        String report = state.finalReport().orElse("");

        // Build context from history
        StringBuilder historyContext = new StringBuilder();
        for (String msg : history) {
            historyContext.append(msg).append("\n");
        }

        // Last message in history is the new user question
        String latestMessage = history.isEmpty() ? "" : history.get(history.size() - 1);

        String prompt = """
                You are a helpful career advisor AI. You have access to the candidate's full analysis report.
                
                CANDIDATE REPORT:
                %s
                
                CONVERSATION HISTORY:
                %s
                
                Answer the candidate's question concisely and helpfully.
                If they ask something not covered in the report, use your general career knowledge.
                """.formatted(report, historyContext.toString());

        String answer = chatModel.call(new Prompt(prompt))
                .getResult().getOutput().getText();

        // Append the assistant's reply to chat history
        return Map.of("chatHistory", "ASSISTANT: " + answer);
    }
}
