package com.mahesh.jobassist.resume;

import com.mahesh.jobassist.common.JobAssistState;
import com.mahesh.jobassist.resume.nodes.ResumeAnalyzerNode;
import com.mahesh.jobassist.resume.nodes.ResumeReflectionNode;
import com.mahesh.jobassist.resume.nodes.SkillGapNode;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * Updated graph with self-critique reflection step:
 *
 * START → analyze_resume → reflect_resume → identify_skill_gaps → END
 *                          ↑
 *                    NEW: validates & corrects the analyzer output
 *                    before it propagates downstream
 */
@Configuration
public class ResumeGraphConfig {

    private final ResumeAnalyzerNode   analyzerNode;
    private final ResumeReflectionNode reflectionNode;
    private final SkillGapNode         skillGapNode;

    public ResumeGraphConfig(ResumeAnalyzerNode analyzerNode,
                             ResumeReflectionNode reflectionNode,
                             SkillGapNode skillGapNode) {
        this.analyzerNode   = analyzerNode;
        this.reflectionNode = reflectionNode;
        this.skillGapNode   = skillGapNode;
    }

    public StateGraph<JobAssistState> buildResumeStateGraph() throws Exception {
        return new StateGraph<>(JobAssistState.SCHEMA, JobAssistState::new)
                .addNode("analyze_resume",      node_async(analyzerNode))
                .addNode("reflect_resume",      node_async(reflectionNode))   // NEW
                .addNode("identify_skill_gaps", node_async(skillGapNode))
                .addEdge(START,                 "analyze_resume")
                .addEdge("analyze_resume",      "reflect_resume")              // NEW
                .addEdge("reflect_resume",      "identify_skill_gaps")         // NEW
                .addEdge("identify_skill_gaps", END);
    }

    @Bean
    public CompiledGraph<JobAssistState> resumeGraph() throws Exception {
        return buildResumeStateGraph().compile();
    }
}
