package com.example.smartkb.service;

import java.util.List;
import java.util.Map;

public record ToolValidationResult(
        boolean valid,
        List<String> errors,
        Map<String, Object> normalizedArguments
) {
}
