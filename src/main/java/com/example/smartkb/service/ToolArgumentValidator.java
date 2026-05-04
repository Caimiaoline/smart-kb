package com.example.smartkb.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ToolArgumentValidator {

    public ToolValidationResult validate(ToolDefinition definition, Map<String, Object> arguments) {
        Map<String, Object> source = arguments == null ? Map.of() : arguments;
        Map<String, Object> normalized = new LinkedHashMap<>();
        List<String> errors = new ArrayList<>();

        for (ToolParameterDefinition parameter : definition.parameters()) {
            Object rawValue = source.get(parameter.name());
            if (rawValue == null) {
                if (parameter.defaultValue() != null) {
                    normalized.put(parameter.name(), parameter.defaultValue());
                } else if (parameter.required()) {
                    errors.add("missing required argument: " + parameter.name());
                }
                continue;
            }

            try {
                normalized.put(parameter.name(), normalizeValue(parameter.type(), rawValue));
            } catch (Exception e) {
                errors.add("invalid argument type: " + parameter.name() + " expected " + parameter.type());
            }
        }

        return new ToolValidationResult(errors.isEmpty(), errors, normalized);
    }

    private Object normalizeValue(String type, Object rawValue) {
        return switch (type) {
            case "string" -> String.valueOf(rawValue);
            case "int" -> Integer.parseInt(String.valueOf(rawValue));
            case "long" -> Long.parseLong(String.valueOf(rawValue));
            case "double" -> Double.parseDouble(String.valueOf(rawValue));
            case "boolean" -> Boolean.parseBoolean(String.valueOf(rawValue));
            default -> rawValue;
        };
    }
}
