package com.example.smartkb.dto;

import jakarta.validation.constraints.NotBlank;

public record AgentTaskRequest(
        @NotBlank String input,
        Long applicationId,
        Long kbId
) {
}
