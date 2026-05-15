package com.mahesh.jobassist.coverletter;

import com.mahesh.jobassist.common.JobAssistState;
import com.mahesh.jobassist.coverletter.nodes.CoverLetterDraftNode;
import com.mahesh.jobassist.coverletter.nodes.CoverLetterReviseNode;
import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * Human-in-the-Loop graph:
 * START → draft_cover_letter → [INTERRUPT for human review]
 *       → (approved?) → END  OR  revise_cover_letter → END
 */
@Configuration
public class CoverLetterGraphConfig {

    private final CoverLetterDraftNode draftNode;
    private final CoverLetterReviseNode reviseNode;

    public CoverLetterGraphConfig(CoverLetterDraftNode draftNode, CoverLetterReviseNode reviseNode) {
        this.draftNode = draftNode;
        this.reviseNode = reviseNode;
    }

    @Bean
    public MemorySaver coverLetterCheckpointSaver() {
        return new MemorySaver();
    }

    @Bean
    public CompiledGraph<JobAssistState> coverLetterGraph(MemorySaver coverLetterCheckpointSaver) throws Exception {
        return new StateGraph<>(JobAssistState.SCHEMA, JobAssistState::new)
                .addNode("draft_cover_letter",  node_async(draftNode))
                .addNode("revise_cover_letter", node_async(reviseNode))
                .addEdge(START, "draft_cover_letter")
                .addConditionalEdges("draft_cover_letter",
                        state -> CompletableFuture.completedFuture(
                                Boolean.FALSE.equals(state.approved().orElse(null)) ? "revise" : "done"
                        ),
                        Map.of(
                            "revise", "revise_cover_letter",
                            "done",   END
                        )
                )
                .addEdge("revise_cover_letter", END)
                .compile(CompileConfig.builder()
                        .checkpointSaver(coverLetterCheckpointSaver)
                        .interruptBefore("draft_cover_letter")
                        .build());
    }
}
