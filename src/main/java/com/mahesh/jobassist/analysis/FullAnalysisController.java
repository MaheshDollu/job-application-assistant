package com.mahesh.jobassist.analysis;

import com.mahesh.jobassist.common.JobAssistState;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.RunnableConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Two endpoints:
 *  POST /analysis/full  → runs all 4 agents and returns complete report
 *  POST /analysis/chat  → persistent chat with memory about your job application
 */
@RestController
@RequestMapping("/analysis")
public class FullAnalysisController {

    private final CompiledGraph<JobAssistState> fullAnalysisGraph;
    private final CompiledGraph<JobAssistState> chatGraph;

    public FullAnalysisController(
            @Qualifier("fullAnalysisGraph") CompiledGraph<JobAssistState> fullAnalysisGraph,
            @Qualifier("chatGraph") CompiledGraph<JobAssistState> chatGraph) {
        this.fullAnalysisGraph = fullAnalysisGraph;
        this.chatGraph = chatGraph;
    }

    // Full multi-agent pipeline
    @PostMapping("/full")
    public Map<String, Object> runFullAnalysis(@RequestBody FullAnalysisRequest request) throws Exception {
        var result = fullAnalysisGraph.invoke(
                Map.of(
                    "candidateName",  request.candidateName(),
                    "resumeText",     request.resumeText(),
                    "jobDescription", request.jobDescription()
                ),
                RunnableConfig.builder().build()
        );

        var state = result.orElseThrow();
        return Map.of(
            "candidateName",  state.candidateName().orElse(""),
            "resumeAnalysis", state.resumeAnalysis().orElse(""),
            "skillGaps",      state.skillGaps().orElse(""),
            "coverLetter",    state.coverLetter().orElse(""),
            "finalReport",    state.finalReport().orElse(""),
            "agentFlow",      List.of("resume_agent", "skill_gap_agent", "cover_letter_agent", "supervisor_agent")
        );
    }

    // Persistent chat (uses threadId for memory across turns)
    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody ChatRequest request) throws Exception {
        var config = RunnableConfig.builder()
                .threadId(request.threadId())
                .build();

        var result = chatGraph.invoke(
                Map.of(
                    "chatHistory",  "USER: " + request.message(),
                    "finalReport",  request.reportContext() != null ? request.reportContext() : ""
                ),
                config
        );

        var state = result.orElseThrow();
        List<String> history = state.chatHistory().orElse(List.of());
        String lastReply = history.isEmpty() ? "" : history.get(history.size() - 1);

        return Map.of(
            "threadId", request.threadId(),
            "reply",    lastReply.replace("ASSISTANT: ", ""),
            "turns",    history.size()
        );
    }

    public record FullAnalysisRequest(String candidateName, String resumeText, String jobDescription) {}
    public record ChatRequest(String threadId, String message, String reportContext) {}
}
