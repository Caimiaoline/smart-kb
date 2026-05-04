package com.example.smartkb.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record ToolExecutionRequest(
        @NotBlank String toolName,
        Map<String, Object> arguments
) {
}
