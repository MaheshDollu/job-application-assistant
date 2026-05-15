package com.mahesh.jobassist.studio;

import com.mahesh.jobassist.common.JobAssistState;
import com.mahesh.jobassist.resume.ResumeGraphConfig;
import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.bsc.langgraph4j.studio.springboot.AbstractLangGraphStudioConfig;
import org.bsc.langgraph4j.studio.springboot.LangGraphFlow;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StudioConfig extends AbstractLangGraphStudioConfig implements WebMvcConfigurer {

    private final LangGraphFlow flow;

    public StudioConfig(ResumeGraphConfig resumeGraphConfig) throws Exception {
        // Build the StateGraph (not compiled) so Studio can compile it with its own config
        var stateGraph = resumeGraphConfig.buildResumeStateGraph();

        this.flow = LangGraphFlow.builder()
                .title("Job Application Assistant")
                .addInputStringArg("resumeText",     true)
                .addInputStringArg("jobDescription", false)
                .addInputStringArg("candidateName",  false)
                .stateGraph(stateGraph)
                .compileConfig(CompileConfig.builder()
                        .checkpointSaver(new MemorySaver())
                        .build())
                .build();
    }

    @Override
    public LangGraphFlow getFlow() {
        return this.flow;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedOrigins("*").allowedMethods("*");
    }
}
