package com.mahesh.jobassist.resume;

import com.mahesh.jobassist.common.JobAssistState;
import com.mahesh.jobassist.resume.nodes.ResumeAnalyzerNode;
import com.mahesh.jobassist.resume.nodes.SkillGapNode;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * Graph: START → analyze_resume → identify_skill_gaps → END
 */
@Configuration
public class ResumeGraphConfig {

    private final ResumeAnalyzerNode analyzerNode;
    private final SkillGapNode skillGapNode;

    public ResumeGraphConfig(ResumeAnalyzerNode analyzerNode, SkillGapNode skillGapNode) {
        this.analyzerNode = analyzerNode;
        this.skillGapNode = skillGapNode;
    }

    // Exposed for Studio to use the uncompiled StateGraph
    public StateGraph<JobAssistState> buildResumeStateGraph() throws Exception {
        return new StateGraph<>(JobAssistState.SCHEMA, JobAssistState::new)
                .addNode("analyze_resume",      node_async(analyzerNode))
                .addNode("identify_skill_gaps", node_async(skillGapNode))
                .addEdge(START,                 "analyze_resume")
                .addEdge("analyze_resume",      "identify_skill_gaps")
                .addEdge("identify_skill_gaps", END);
    }

    @Bean
    public CompiledGraph<JobAssistState> resumeGraph() throws Exception {
        return buildResumeStateGraph().compile();
    }
}
