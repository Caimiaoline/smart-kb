package com.example.smartkb.service;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class McpToolAdapter {

    public McpToolDescriptor adapt(ToolDefinition definition) {
        Map<String, Object> properties = new LinkedHashMap<>();
        for (ToolParameterDefinition parameter : definition.parameters()) {
            Map<String, Object> property = new LinkedHashMap<>();
            property.put("type", parameter.type());
            property.put("description", parameter.description());
            if (parameter.defaultValue() != null) {
                property.put("default", parameter.defaultValue());
            }
            properties.put(parameter.name(), property);
        }

        List<String> required = definition.parameters().stream()
                .filter(ToolParameterDefinition::required)
                .map(ToolParameterDefinition::name)
                .toList();

        return new McpToolDescriptor(
                definition.name(),
                definition.description(),
                Map.of(
                        "type", "object",
                        "properties", properties
                ),
                required
        );
    }
}
