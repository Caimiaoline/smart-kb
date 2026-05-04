package com.example.smartkb.service;

import java.util.List;

public record ToolDefinition(
        String name,
        String description,
        List<ToolParameterDefinition> parameters
) {
}
