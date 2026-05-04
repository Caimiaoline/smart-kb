package com.example.smartkb.service;

import java.time.Instant;
import java.util.Map;

public record AgentTraceEvent(
        String stage,
        String status,
        Instant timestamp,
        Map<String, Object> detail
) {
}
