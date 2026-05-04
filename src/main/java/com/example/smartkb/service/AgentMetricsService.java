package com.example.smartkb.service;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AgentMetricsService {

    private final AtomicLong totalTasks = new AtomicLong();
    private final AtomicLong successfulTasks = new AtomicLong();
    private final AtomicLong failedTasks = new AtomicLong();
    private final AtomicLong totalDurationMs = new AtomicLong();
    private final Map<String, AtomicLong> routeCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> toolCounts = new ConcurrentHashMap<>();

    public void record(String route, String toolName, boolean success, long durationMs) {
        totalTasks.incrementAndGet();
        if (success) {
            successfulTasks.incrementAndGet();
        } else {
            failedTasks.incrementAndGet();
        }
        totalDurationMs.addAndGet(Math.max(0, durationMs));
        routeCounts.computeIfAbsent(route == null ? "unknown" : route, key -> new AtomicLong()).incrementAndGet();
        toolCounts.computeIfAbsent(toolName == null ? "none" : toolName, key -> new AtomicLong()).incrementAndGet();
    }

    public Map<String, Object> snapshot() {
        long total = totalTasks.get();
        Map<String, Long> routes = new LinkedHashMap<>();
        routeCounts.forEach((key, value) -> routes.put(key, value.get()));
        Map<String, Long> tools = new LinkedHashMap<>();
        toolCounts.forEach((key, value) -> tools.put(key, value.get()));

        return Map.of(
                "totalTasks", total,
                "successfulTasks", successfulTasks.get(),
                "failedTasks", failedTasks.get(),
                "successRate", total == 0 ? 0.0 : (double) successfulTasks.get() / total,
                "avgDurationMs", total == 0 ? 0.0 : (double) totalDurationMs.get() / total,
                "routeCounts", routes,
                "toolCounts", tools
        );
    }
}
