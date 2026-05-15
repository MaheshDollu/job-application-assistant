package com.mahesh.jobassist.interview;

import com.mahesh.jobassist.common.JobAssistState;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.RunnableConfig;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Streams interview prep results via Server-Sent Events.
 * Client sees each node's output as it completes in real time.
 */
@RestController
@RequestMapping("/interview")
public class InterviewController {

    private final CompiledGraph<JobAssistState> interviewGraph;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public InterviewController(CompiledGraph<JobAssistState> interviewGraph) {
        this.interviewGraph = interviewGraph;
    }

    @PostMapping(value = "/prep", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter prepareInterview(@RequestBody PrepRequest request) {
        SseEmitter emitter = new SseEmitter(120_000L); // 2 min timeout

        executor.submit(() -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("status")
                        .data("Starting interview preparation..."));

                var stream = interviewGraph.stream(
                        Map.of(
                            "candidateName",  request.candidateName(),
                            "resumeText",     request.resumeText(),
                            "jobDescription", request.jobDescription(),
                            "resumeAnalysis", request.resumeAnalysis()
                        ),
                        RunnableConfig.builder().build()
                );

                stream.stream().forEach(nodeOutput -> {
                    try {
                        String nodeName = nodeOutput.node();
                        JobAssistState state = nodeOutput.state();

                        switch (nodeName) {
                            case "generate_questions" -> {
                                List<String> questions = state.interviewQuestions().orElse(List.of());
                                emitter.send(SseEmitter.event()
                                        .name("generate_questions")
                                        .data(Map.of(
                                            "node", "generate_questions",
                                            "questions", questions
                                        )));
                            }
                            case "generate_answer_guide" -> {
                                List<String> all = state.interviewQuestions().orElse(List.of());
                                emitter.send(SseEmitter.event()
                                        .name("generate_answer_guide")
                                        .data(Map.of(
                                            "node", "generate_answer_guide",
                                            "answerGuide", all.size() > 1 ? all.get(all.size() - 1) : ""
                                        )));
                            }
                        }
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                });

                emitter.send(SseEmitter.event()
                        .name("complete")
                        .data(Map.of("status", "Interview prep complete!")));
                emitter.complete();

            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    public record PrepRequest(String candidateName, String resumeText,
                              String jobDescription, String resumeAnalysis) {}
}
