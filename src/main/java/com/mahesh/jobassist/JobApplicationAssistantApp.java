package com.mahesh.jobassist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JobApplicationAssistantApp {

    public static void main(String[] args) {
        SpringApplication.run(JobApplicationAssistantApp.class, args);
        System.out.println("""
                
                ╔══════════════════════════════════════════════════════════╗
                ║       Job Application Assistant — Running!               ║
                ╠══════════════════════════════════════════════════════════╣
                ║  Studio UI   : http://localhost:8080/studio              ║
                ║                                                          ║
                ║  Endpoints:                                              ║
                ║  POST /resume/analyze       → Resume Analyzer            ║
                ║  POST /cover-letter/draft   → Cover Letter (HitL)        ║
                ║  POST /cover-letter/approve → Approve/Reject Letter      ║
                ║  POST /interview/prep       → Interview Prep (Streaming) ║
                ║  POST /analysis/full        → Full Multi-Agent Pipeline  ║
                ║  POST /analysis/chat        → Persistent Chat Session    ║
                ╚══════════════════════════════════════════════════════════╝
                """);
    }
}
