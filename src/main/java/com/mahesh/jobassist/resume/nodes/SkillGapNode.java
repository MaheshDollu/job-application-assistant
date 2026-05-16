package com.mahesh.jobassist.resume.nodes;

import com.mahesh.jobassist.common.JobAssistState;
import com.mahesh.jobassist.common.PromptOptions;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SkillGapNode implements NodeAction<JobAssistState> {

    private final ChatModel chatModel;

    public SkillGapNode(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public Map<String, Object> apply(JobAssistState state) throws Exception {
        String resume   = state.resumeText().orElse("");
        String jobDesc  = state.jobDescription().orElse("");
        String analysis = state.resumeAnalysis().orElse("");

        String prompt = """
                You are a senior engineering hiring manager identifying skill gaps for a candidate.

                STRICT OUTPUT RULES:
                - Output ONLY gap blocks. No preamble, headers, or trailing commentary.
                - If there are zero gaps, output exactly: NO_GAPS
                - Every gap MUST include ALL FOUR fields, each on its own line, each with a colon:
                    GAP: [2–4 word skill name ONLY — e.g. "CUDA Kernels", "PyTorch JIT", "Distributed Inference"]
                    PRIORITY: [HIGH | MEDIUM | LOW]
                    REASON: [one sentence — cite exact JD requirement and what the resume lacks]
                    FIX: [one concrete action with timeline — specific course, project, or cert]
                - GAP name rules:
                    * Maximum 4 words
                    * Use the SHORT common name: "CUDA Kernels" not "CUDA kernels or equivalent ML or low-level kernels"
                    * Never write "None", "N/A", "No gaps"
                - ALWAYS include an "Experience Level" gap if the JD requires 5+ years and the
                  candidate has fewer than 4 years of professional (non-internship) experience.
                - Sort: HIGH → MEDIUM → LOW

                Priority definitions:
                  HIGH   = explicitly REQUIRED by JD, completely absent from resume
                  MEDIUM = mentioned in JD, transferable skill exists but not direct experience
                  LOW    = preferred/nice-to-have in JD, not present in resume

                --- CORRECT EXAMPLE ---
                GAP: Experience Level
                PRIORITY: HIGH
                REASON: JD requires 5+ years professional experience; resume shows ~1-2 years (student assistant + internships).
                FIX: Target associate/mid-level roles first to build 3-5 years experience before re-applying.

                GAP: CUDA Kernels
                PRIORITY: MEDIUM
                REASON: JD prefers CUDA/low-level kernel experience; resume shows high-level ML frameworks only.
                FIX: Complete NVIDIA DLI CUDA course (free, 2 weeks) and implement one custom kernel on GitHub.

                GAP: Distributed Inference
                PRIORITY: HIGH
                REASON: JD requires distributed inference optimization; resume shows no multi-GPU or distributed work.
                FIX: Run Llama 2 with torch.distributed across 2 GPUs on Colab, document throughput numbers.

                GAP: PyTorch JIT
                PRIORITY: LOW
                REASON: JD lists PyTorch JIT/AOT tracing as preferred; resume mentions PyTorch but not compilation.
                FIX: Complete PyTorch TorchScript tutorial (free, 3 days) and trace one existing model.
                --- END EXAMPLE ---

                RESUME ANALYSIS:
                %s

                FULL RESUME:
                %s

                JOB DESCRIPTION:
                %s

                List ALL gaps now in the exact format above.
                """.formatted(analysis, resume, jobDesc);

        String gaps = chatModel.call(new Prompt(prompt, PromptOptions.analytical()))
                .getResult().getOutput().getText();

        if (gaps.trim().equals("NO_GAPS")) gaps = "";

        return Map.of("skillGaps", gaps);
    }
}
