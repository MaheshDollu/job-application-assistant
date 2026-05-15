package com.mahesh.jobassist.coverletter;

import com.mahesh.jobassist.common.JobAssistState;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.RunnableConfig;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Human-in-the-Loop Cover Letter endpoints:
 *   POST /cover-letter/draft   → generates draft, pauses for review
 *   POST /cover-letter/approve → human approves or rejects with feedback
 */
@RestController
@RequestMapping("/cover-letter")
public class CoverLetterController {

    private final CompiledGraph<JobAssistState> coverLetterGraph;

    public CoverLetterController(CompiledGraph<JobAssistState> coverLetterGraph) {
        this.coverLetterGraph = coverLetterGraph;
    }

    // Step 1: Generate a draft and pause for human review
    @PostMapping("/draft")
    public Map<String, Object> draft(@RequestBody DraftRequest request) throws Exception {
        String threadId = UUID.randomUUID().toString();

        var config = RunnableConfig.builder()
                .threadId(threadId)
                .build();

        // Run until interrupt (after draft is generated)
        var result = coverLetterGraph.invoke(
                Map.of(
                    "candidateName",  request.candidateName(),
                    "resumeText",     request.resumeText(),
                    "jobDescription", request.jobDescription(),
                    "resumeAnalysis", request.resumeAnalysis()
                ),
                config
        );

        var state = result.orElseThrow();
        return Map.of(
            "threadId",    threadId,
            "coverLetter", state.coverLetter().orElse(""),
            "status",      "AWAITING_YOUR_REVIEW",
            "message",     "Review the cover letter and call /cover-letter/approve to approve or reject."
        );
    }

    // Step 2: Human approves or rejects (with optional feedback)
    @PostMapping("/approve")
    public Map<String, Object> approve(@RequestBody ApproveRequest request) throws Exception {
        var config = RunnableConfig.builder()
                .threadId(request.threadId())
                .build();

        // Inject human decision into the paused graph
        coverLetterGraph.updateState(config,
                Map.of(
                    "approved",         request.approved(),
                    "approvalFeedback", request.feedback() != null ? request.feedback() : ""
                ),
                null
        );

        // Resume from where it left off
        var result = coverLetterGraph.invoke(null, config);
        var state = result.orElseThrow();

        return Map.of(
            "approved",    request.approved(),
            "coverLetter", state.coverLetter().orElse(""),
            "status",      request.approved() ? "APPROVED — Ready to send!" : "REVISED — Please review again."
        );
    }

    public record DraftRequest(String candidateName, String resumeText,
                               String jobDescription, String resumeAnalysis) {}
    public record ApproveRequest(String threadId, boolean approved, String feedback) {}
}
