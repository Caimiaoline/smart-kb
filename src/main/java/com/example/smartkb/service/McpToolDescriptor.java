package com.example.smartkb.service;

import java.util.List;
import java.util.Map;

public record McpToolDescriptor(
        String name,
        String description,
        Map<String, Object> inputSchema,
        List<String> required
) {
}
