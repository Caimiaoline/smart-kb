package com.example.smartkb.service;

public record ToolParameterDefinition(
        String name,
        String type,
        boolean required,
        String description,
        Object defaultValue
) {
    public ToolParameterDefinition(String name, String type, boolean required, String description) {
        this(name, type, required, description, null);
    }
}
