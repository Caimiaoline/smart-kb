package com.example.smartkb.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record AgentTrace(
        Instant startedAt,
        Instant finishedAt,
        long durationMs,
        List<String> plan,
        String selectedTool,
        Map<String, Object> toolArguments,
        boolean success,
        List<AgentTraceEvent> events
) {
}
