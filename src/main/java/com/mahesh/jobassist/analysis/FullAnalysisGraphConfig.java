package com.mahesh.jobassist.analysis;

import com.mahesh.jobassist.analysis.nodes.ChatAgentNode;
import com.mahesh.jobassist.analysis.nodes.SupervisorAgentNode;
import com.mahesh.jobassist.common.JobAssistState;
import com.mahesh.jobassist.coverletter.nodes.CoverLetterDraftNode;
import com.mahesh.jobassist.resume.nodes.ResumeAnalyzerNode;
import com.mahesh.jobassist.resume.nodes.ResumeReflectionNode;
import com.mahesh.jobassist.resume.nodes.SkillGapNode;
import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * Full Multi-Agent Pipeline (updated with reflection node):
 *
 *  START
 *    → resume_agent           (analyzes resume)
 *    → reflection_agent       (NEW: self-critiques and corrects the analysis)
 *    → skill_gap_agent        (finds gaps using verified analysis)
 *    → cover_letter_agent     (drafts letter)
 *    → supervisor_agent       (consolidates into Career Action Plan)
 *    → END
 *
 * Chat Graph (unchanged — checkpointed per threadId):
 *  START → chat_agent → END
 */
@Configuration
public class FullAnalysisGraphConfig {

    private final ResumeAnalyzerNode   resumeAnalyzerNode;
    private final ResumeReflectionNode resumeReflectionNode;
    private final SkillGapNode         skillGapNode;
    private final CoverLetterDraftNode coverLetterDraftNode;
    private final SupervisorAgentNode  supervisorAgentNode;
    private final ChatAgentNode        chatAgentNode;

    public FullAnalysisGraphConfig(ResumeAnalyzerNode resumeAnalyzerNode,
                                   ResumeReflectionNode resumeReflectionNode,
                                   SkillGapNode skillGapNode,
                                   CoverLetterDraftNode coverLetterDraftNode,
                                   SupervisorAgentNode supervisorAgentNode,
                                   ChatAgentNode chatAgentNode) {
        this.resumeAnalyzerNode   = resumeAnalyzerNode;
        this.resumeReflectionNode = resumeReflectionNode;
        this.skillGapNode         = skillGapNode;
        this.coverLetterDraftNode = coverLetterDraftNode;
        this.supervisorAgentNode  = supervisorAgentNode;
        this.chatAgentNode        = chatAgentNode;
    }

    @Bean
    public CompiledGraph<JobAssistState> fullAnalysisGraph() throws Exception {
        return new StateGraph<>(JobAssistState.SCHEMA, JobAssistState::new)
                .addNode("resume_agent",      node_async(resumeAnalyzerNode))
                .addNode("reflection_agent",  node_async(resumeReflectionNode))  // NEW
                .addNode("skill_gap_agent",   node_async(skillGapNode))
                .addNode("cover_letter_agent",node_async(coverLetterDraftNode))
                .addNode("supervisor_agent",  node_async(supervisorAgentNode))
                .addEdge(START,               "resume_agent")
                .addEdge("resume_agent",      "reflection_agent")                 // NEW
                .addEdge("reflection_agent",  "skill_gap_agent")                  // NEW
                .addEdge("skill_gap_agent",   "cover_letter_agent")
                .addEdge("cover_letter_agent","supervisor_agent")
                .addEdge("supervisor_agent",  END)
                .compile();
    }

    @Bean
    public MemorySaver chatMemorySaver() {
        return new MemorySaver();
    }

    @Bean
    public CompiledGraph<JobAssistState> chatGraph(MemorySaver chatMemorySaver) throws Exception {
        return new StateGraph<>(JobAssistState.SCHEMA, JobAssistState::new)
                .addNode("chat_agent", node_async(chatAgentNode))
                .addEdge(START,        "chat_agent")
                .addEdge("chat_agent", END)
                .compile(CompileConfig.builder()
                        .checkpointSaver(chatMemorySaver)
                        .build());
    }
}
