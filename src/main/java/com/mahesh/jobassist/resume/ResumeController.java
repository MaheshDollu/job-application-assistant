package com.mahesh.jobassist.resume;

import com.mahesh.jobassist.common.JobAssistState;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.RunnableConfig;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/resume")
public class ResumeController {

    private final CompiledGraph<JobAssistState> resumeGraph;

    public ResumeController(CompiledGraph<JobAssistState> resumeGraph) {
        this.resumeGraph = resumeGraph;
    }

    @PostMapping("/analyze")
    public Map<String, Object> analyzeResume(@RequestBody ResumeRequest request) throws Exception {
        var result = resumeGraph.invoke(
                Map.of(
                    "resumeText",     request.resumeText(),
                    "jobDescription", request.jobDescription(),
                    "candidateName",  request.candidateName()
                ),
                RunnableConfig.builder().build()
        );

        var state = result.orElseThrow();
        return Map.of(
            "candidateName",  state.candidateName().orElse("Unknown"),
            "resumeAnalysis", state.resumeAnalysis().orElse(""),
            "skillGaps",      state.skillGaps().orElse("")
        );
    }

    public record ResumeRequest(String candidateName, String resumeText, String jobDescription) {}
}
