package com.mahesh.jobassist.resume.nodes;

import com.mahesh.jobassist.common.JobAssistState;
import com.mahesh.jobassist.common.PromptOptions;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Skill Gap Node — fixed to enforce strict GAP:/PRIORITY:/REASON:/FIX: format
 * and never emit "None mentioned..." as a gap entry.
 */
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
                You are a technical skills gap analyst.

                STRICT OUTPUT RULES — read carefully before responding:
                - Output ONLY skill gap blocks. Nothing else. No preamble, no summary, no trailing sentences.
                - If there are NO skill gaps, output exactly: NO_GAPS
                - Each gap MUST use ALL FOUR fields below, each on its own line, each with a colon:
                    GAP: [skill name only — max 5 words, no "None", no "N/A"]
                    PRIORITY: [HIGH | MEDIUM | LOW]
                    REASON: [one sentence — why this specific skill is missing]
                    FIX: [one concrete action — course, project, or certification]
                - Do NOT write "None mentioned", "No additional gaps", "N/A" or any variation as a GAP entry.
                - Do NOT add numbered explanations, headers, or commentary.
                - Sort output: HIGH gaps first, then MEDIUM, then LOW.

                Priority definitions:
                  HIGH   = explicitly required in JD and completely absent from resume
                  MEDIUM = mentioned in JD, partially present or transferable skill exists
                  LOW    = preferred/nice-to-have in JD and absent from resume

                --- EXAMPLE OF CORRECT OUTPUT ---
                GAP: C++
                PRIORITY: HIGH
                REASON: JD requires 2 years C++ experience; resume shows no C++ at all.
                FIX: Complete LearnCpp.com free tutorial and build one C++ project (4–6 weeks).

                GAP: Reinforcement Learning
                PRIORITY: MEDIUM
                REASON: JD prefers RL experience; candidate has ML background but not RL specifically.
                FIX: Complete HuggingFace Deep RL Course (free, 2–3 weeks).
                --- END EXAMPLE ---

                RESUME ANALYSIS (verified):
                %s

                FULL RESUME:
                %s

                JOB DESCRIPTION:
                %s

                List every skill gap now using the exact format above.
                """.formatted(analysis, resume, jobDesc);

        String gaps = chatModel.call(new Prompt(prompt, PromptOptions.analytical()))
                .getResult().getOutput().getText();

        // If model returned NO_GAPS sentinel, store empty string
        if (gaps.trim().equals("NO_GAPS")) {
            gaps = "";
        }

        return Map.of("skillGaps", gaps);
    }
}
