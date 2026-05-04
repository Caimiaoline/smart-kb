package com.example.smartkb.service;

import java.util.Map;

public record ToolExecutionResult(
        String toolName,
        boolean success,
        String message,
        Map<String, Object> data,
        ToolDefinition definition
) {
}
