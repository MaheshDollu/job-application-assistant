package com.mahesh.jobassist.interview;

import com.mahesh.jobassist.common.JobAssistState;
import com.mahesh.jobassist.interview.nodes.InterviewAnswerGuideNode;
import com.mahesh.jobassist.interview.nodes.InterviewQuestionNode;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * Graph: START → generate_questions → generate_answer_guide → END
 * Exposed via SSE streaming so client sees each step as it completes.
 */
@Configuration
public class InterviewGraphConfig {

    private final InterviewQuestionNode questionNode;
    private final InterviewAnswerGuideNode answerGuideNode;

    public InterviewGraphConfig(InterviewQuestionNode questionNode,
                                InterviewAnswerGuideNode answerGuideNode) {
        this.questionNode = questionNode;
        this.answerGuideNode = answerGuideNode;
    }

    @Bean
    public CompiledGraph<JobAssistState> interviewGraph() throws Exception {
        return new StateGraph<>(JobAssistState.SCHEMA, JobAssistState::new)
                .addNode("generate_questions",    node_async(questionNode))
                .addNode("generate_answer_guide", node_async(answerGuideNode))
                .addEdge(START,                   "generate_questions")
                .addEdge("generate_questions",    "generate_answer_guide")
                .addEdge("generate_answer_guide", END)
                .compile();
    }
}
