package com.example.smartkb.service;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ToolRegistryService {

    private final Map<String, AgentTool> tools = new LinkedHashMap<>();
    private final ToolArgumentValidator argumentValidator;

    public ToolRegistryService(List<AgentTool> agentTools, ToolArgumentValidator argumentValidator) {
        this.argumentValidator = argumentValidator;
        agentTools.stream()
                .sorted(Comparator.comparing(AgentTool::name))
                .forEach(tool -> tools.put(tool.name(), tool));
    }

    public List<ToolDefinition> listTools() {
        return tools.values().stream()
                .map(AgentTool::definition)
                .toList();
    }

    public Optional<AgentTool> findTool(String toolName) {
        return Optional.ofNullable(tools.get(toolName));
    }

    public ToolExecutionResult execute(String toolName, Map<String, Object> arguments) {
        AgentTool tool = tools.get(toolName);
        if (tool == null) {
            return new ToolExecutionResult(
                    toolName,
                    false,
                    "tool not found: " + toolName,
                    Map.of("availableTools", listTools()),
                    null
            );
        }
        ToolValidationResult validation = argumentValidator.validate(tool.definition(), arguments);
        if (!validation.valid()) {
            return new ToolExecutionResult(
                    toolName,
                    false,
                    "tool argument validation failed",
                    Map.of(
                            "errors", validation.errors(),
                            "receivedArguments", arguments == null ? Map.of() : arguments
                    ),
                    tool.definition()
            );
        }
        try {
            return tool.execute(validation.normalizedArguments());
        } catch (Exception e) {
            return new ToolExecutionResult(
                    toolName,
                    false,
                    "tool execution failed: " + e.getMessage(),
                    Map.of("normalizedArguments", validation.normalizedArguments()),
                    tool.definition()
            );
        }
    }
}
