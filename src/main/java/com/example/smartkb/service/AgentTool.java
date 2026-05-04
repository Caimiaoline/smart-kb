package com.example.smartkb.service;

import java.util.Map;

public interface AgentTool {

    ToolDefinition definition();

    default String name() {
        return definition().name();
    }

    default String description() {
        return definition().description();
    }

    ToolExecutionResult execute(Map<String, Object> arguments);
}
