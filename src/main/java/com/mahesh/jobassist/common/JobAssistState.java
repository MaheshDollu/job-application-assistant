package com.mahesh.jobassist.common;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Shared state object that flows through all job assistant graphs.
 * Single-value fields use plain map entries (last-write-wins).
 * List fields use Channels.appender() to accumulate values.
 */
public class JobAssistState extends AgentState {

    public static final Map<String, Channel<?>> SCHEMA = Map.of(
        "interviewQuestions", Channels.appender(ArrayList::new),
        "chatHistory",        Channels.appender(ArrayList::new)
    );

    public JobAssistState(Map<String, Object> initData) {
        super(initData);
    }

    public Optional<String> resumeText()         { return value("resumeText"); }
    public Optional<String> jobDescription()     { return value("jobDescription"); }
    public Optional<String> candidateName()      { return value("candidateName"); }
    public Optional<String> resumeAnalysis()     { return value("resumeAnalysis"); }
    public Optional<String> skillGaps()          { return value("skillGaps"); }
    public Optional<String> coverLetter()        { return value("coverLetter"); }
    public Optional<Boolean> approved()          { return value("approved"); }
    public Optional<String> approvalFeedback()   { return value("approvalFeedback"); }
    public Optional<List<String>> interviewQuestions() { return value("interviewQuestions"); }
    public Optional<List<String>> chatHistory()  { return value("chatHistory"); }
    public Optional<String> finalReport()        { return value("finalReport"); }
}
