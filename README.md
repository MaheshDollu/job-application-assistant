# 🧑‍💼 Job Application Assistant

> **AI-powered job prep — resume analysis, cover letters, interview coaching, and a career advisor chat — all running locally with no API key, no cost, and no cloud.**

Built with **Spring Boot 3**, **Spring AI**, **LangGraph4J**, and **Ollama**, this project is a complete showcase of every major agentic AI pattern: tool calling, human-in-the-loop approval, real-time streaming, multi-agent pipelines, and persistent memory — applied to a real-world problem most developers face every day.

---

## ✨ What It Does

Paste in your resume and a job description. The assistant does the rest:

| Feature | What you get |
|---|---|
| 📄 **Resume Analyzer** | Strengths, keyword matches, and a fit score (1–10) against the job description |
| 🔍 **Skill Gap Analysis** | Missing skills ranked HIGH / MEDIUM / LOW with recommended courses |
| ✉️ **Cover Letter (Human-in-the-Loop)** | AI drafts a tailored letter → you approve or give feedback → AI revises |
| 🎤 **Interview Prep (Live Streaming)** | 8 likely questions + answer guides stream to your screen in real time via SSE |
| 🤖 **Full Multi-Agent Pipeline** | All four agents run in sequence and a Supervisor consolidates a final Career Action Plan |
| 💬 **Persistent Chat** | Ask follow-up questions about your report across multiple turns — the AI remembers everything |
| 🖥️ **Studio UI** | Visual graph debugger at `http://localhost:8080/studio` — see your agent graphs animate live |

---

## 🏗️ Architecture

```
                    ┌─────────────────────────────────────────┐
                    │         Job Application Assistant        │
                    └─────────────────────────────────────────┘
                                        │
         ┌──────────────┬───────────────┼─────────────────┬─────────────────┐
         │              │               │                 │                 │
   Resume Analyzer  Cover Letter   Interview Prep    Full Pipeline    Persistent Chat
   (Tool Calling)  (Human-in-Loop)  (SSE Streaming) (Multi-Agent)  (Checkpointing)
         │              │               │                 │                 │
    2 LangGraph    2-step HitL     SSE SseEmitter    4 chained        MemorySaver
      nodes         + MemorySaver     streaming        agents         per threadId
```

### Agent Graph — Full Pipeline

```
START
  │
  ▼
resume_agent          ← Analyzes resume vs. job description
  │
  ▼
skill_gap_agent       ← Identifies gaps, suggests courses
  │
  ▼
cover_letter_agent    ← Drafts a personalized cover letter
  │
  ▼
supervisor_agent      ← Reads all 3 reports → writes Career Action Plan
  │
  ▼
END
```

### Cover Letter — Human-in-the-Loop Flow

```
START
  │
  ▼
draft_cover_letter    ← AI generates draft
  │
  ■ INTERRUPT ■       ← Graph pauses here — you review the draft
  │
  ▼ (human resumes with approved=true/false)
  │
  ├─ approved → END
  │
  └─ rejected → revise_cover_letter → END
```

---

## 🛠️ Tech Stack

| Layer | Technology | Why |
|---|---|---|
| **Runtime** | Java 17 | LTS, records, sealed classes |
| **Web Framework** | Spring Boot 3.4 | Production-ready REST + SSE |
| **AI Orchestration** | LangGraph4J 1.6.0-beta5 | LangGraph patterns in Java — stateful agent graphs |
| **AI Abstraction** | Spring AI 1.0.0-M5 | Unified `ChatModel` API across any LLM |
| **LLM** | Ollama (llama3) | 100% local — no API key, no cost, no data leaving your machine |
| **Persistence** | LangGraph4J `MemorySaver` | In-memory checkpoint store for HitL and chat memory |
| **Studio** | LangGraph4J Studio | Visual debugger for agent graphs |
| **Build** | Maven 3.8 | Standard Java build |
| **Boilerplate** | Lombok | `@Component`, clean node classes |

### LangGraph4J Concepts Demonstrated

| Concept | Where |
|---|---|
| **Stateful Graphs** | `JobAssistState` flows through every graph — channels accumulate list data |
| **Tool Calling** | `ResumeAnalyzerNode` + `SkillGapNode` — sequential LLM chain with state passing |
| **Human-in-the-Loop** | `CoverLetterGraphConfig` — `interruptBefore`, `updateState`, then resume |
| **Streaming (SSE)** | `InterviewController` — `graph.stream()` piped to `SseEmitter` |
| **Multi-Agent** | `FullAnalysisGraphConfig` — four specialist agents + a supervisor |
| **Checkpointing** | `ChatGraph` — `MemorySaver` + `threadId` for persistent sessions |
| **Studio Integration** | `StudioConfig` — exposes graph to the visual debugger |

---

## 🚀 Quick Start

### Prerequisites

- Java 17+
- Maven 3.8+
- [Ollama](https://ollama.com/download) installed

### 1. Start Ollama

```bash
ollama pull llama3
ollama serve        # keep this running
```

### 2. Clone & Run

```bash
git clone https://github.com/YOUR_USERNAME/job-application-assistant.git
cd job-application-assistant
mvn spring-boot:run
```

App starts at **http://localhost:8080**

You'll see this in the console:

```
╔══════════════════════════════════════════════════════════╗
║       Job Application Assistant — Running!               ║
╠══════════════════════════════════════════════════════════╣
║  Studio UI   : http://localhost:8080/studio              ║
║  POST /resume/analyze       → Resume Analyzer            ║
║  POST /cover-letter/draft   → Cover Letter (HitL)        ║
║  POST /cover-letter/approve → Approve/Reject Letter      ║
║  POST /interview/prep       → Interview Prep (Streaming) ║
║  POST /analysis/full        → Full Multi-Agent Pipeline  ║
║  POST /analysis/chat        → Persistent Chat Session    ║
╚══════════════════════════════════════════════════════════╝
```

---

## 📡 API Reference

### 1 — Resume Analyzer

```bash
curl -X POST http://localhost:8080/resume/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "candidateName": "Mahesh Dollu",
    "resumeText": "5 years Java developer. Spring Boot, REST APIs, MySQL, Docker. Led team of 3.",
    "jobDescription": "Senior Java Engineer. Need Spring Boot, Kubernetes, AWS, CI/CD."
  }'
```

**Returns:** key strengths, keyword matches, fit score 1–10, and skill gaps with priority levels.

---

### 2 — Cover Letter (Human-in-the-Loop)

**Step 1 — Generate draft:**

```bash
curl -X POST http://localhost:8080/cover-letter/draft \
  -H "Content-Type: application/json" \
  -d '{
    "candidateName": "Mahesh Dollu",
    "resumeText": "5 years Java developer...",
    "jobDescription": "Senior Java Engineer...",
    "resumeAnalysis": "Strong Spring Boot background..."
  }'
# 👉 Save the "threadId" from the response
```

**Step 2 — Approve or reject:**

```bash
# Approve — ready to send
curl -X POST http://localhost:8080/cover-letter/approve \
  -H "Content-Type: application/json" \
  -d '{"threadId": "PASTE-ID-HERE", "approved": true, "feedback": ""}'

# Reject with feedback — AI revises automatically
curl -X POST http://localhost:8080/cover-letter/approve \
  -H "Content-Type: application/json" \
  -d '{"threadId": "PASTE-ID-HERE", "approved": false, "feedback": "Make it shorter and punchier"}'
```

---

### 3 — Interview Prep (Live Streaming)

```bash
curl -N -X POST http://localhost:8080/interview/prep \
  -H "Content-Type: application/json" \
  -d '{
    "candidateName": "Mahesh Dollu",
    "resumeText": "5 years Java developer...",
    "jobDescription": "Senior Java Engineer...",
    "resumeAnalysis": "Strong background in Spring Boot..."
  }'
```

Watch 8 interview questions and personalized answer guides stream to your terminal in real time.

---

### 4 — Full Multi-Agent Pipeline

```bash
curl -X POST http://localhost:8080/analysis/full \
  -H "Content-Type: application/json" \
  -d '{
    "candidateName": "Mahesh Dollu",
    "resumeText": "5 years Java developer. Spring Boot, REST APIs, MySQL, Docker.",
    "jobDescription": "Senior Java Engineer. Need Spring Boot, Kubernetes, AWS."
  }'
```

**Returns:** resume analysis + skill gaps + cover letter + a full 30-day Career Action Plan from the Supervisor agent.

---

### 5 — Persistent Chat

```bash
# Turn 1 — start a session
curl -X POST http://localhost:8080/analysis/chat \
  -H "Content-Type: application/json" \
  -d '{"threadId": "my-session-123", "message": "What are my biggest skill gaps?", "reportContext": "PASTE FINAL REPORT HERE"}'

# Turn 2 — AI remembers the whole conversation
curl -X POST http://localhost:8080/analysis/chat \
  -H "Content-Type: application/json" \
  -d '{"threadId": "my-session-123", "message": "How long would it take me to learn Kubernetes?"}'
```

---

## 📁 Project Structure

```
src/main/java/com/mahesh/jobassist/
├── JobApplicationAssistantApp.java       # Entry point + startup banner
├── common/
│   └── JobAssistState.java              # Shared state flowing through all graphs
├── resume/
│   ├── ResumeGraphConfig.java           # START → analyze_resume → identify_skill_gaps → END
│   ├── ResumeController.java            # POST /resume/analyze
│   └── nodes/
│       ├── ResumeAnalyzerNode.java      # LLM: resume vs. job description analysis
│       └── SkillGapNode.java            # LLM: gap identification with priority levels
├── coverletter/
│   ├── CoverLetterGraphConfig.java      # HitL graph with MemorySaver + interrupt
│   ├── CoverLetterController.java       # POST /cover-letter/draft + /approve
│   └── nodes/
│       ├── CoverLetterDraftNode.java    # LLM: tailored cover letter (250–350 words)
│       └── CoverLetterReviseNode.java   # LLM: revision based on human feedback
├── interview/
│   ├── InterviewGraphConfig.java        # START → generate_questions → generate_answer_guide → END
│   ├── InterviewController.java         # POST /interview/prep (SSE)
│   └── nodes/
│       ├── InterviewQuestionNode.java   # LLM: 8 questions (behavioral, technical, situational)
│       └── InterviewAnswerGuideNode.java# LLM: per-question answer frameworks
├── analysis/
│   ├── FullAnalysisGraphConfig.java     # 4-agent pipeline + checkpointed chat graph
│   ├── FullAnalysisController.java      # POST /analysis/full + /chat
│   └── nodes/
│       ├── SupervisorAgentNode.java     # LLM: synthesizes all reports → Career Action Plan
│       └── ChatAgentNode.java           # LLM: persistent Q&A with full session memory
└── studio/
    └── StudioConfig.java               # Exposes resume graph to LangGraph4J Studio UI
```

---

## ⚙️ Configuration

`src/main/resources/application.yml`

```yaml
spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        options:
          model: llama3      # swap to mistral, gemma2, etc.
          temperature: 0.7

server:
  port: 8080
```

Want to use a different model? Just `ollama pull <model>` and update the config.

---

## 🤝 Contributing

Pull requests are welcome. For major changes, open an issue first.

---

## 📄 License

MIT

---

*Built by [Mahesh Dollu](https://github.com/maheshdollu) — feedback and stars welcome!* ⭐
