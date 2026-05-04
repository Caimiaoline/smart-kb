package com.example.smartkb.service;

import java.util.List;

public record RouteDecision(
        String route,
        String reason,
        List<String> suggestedSteps,
        String preferredTool,
        boolean llmBased
) {
}
