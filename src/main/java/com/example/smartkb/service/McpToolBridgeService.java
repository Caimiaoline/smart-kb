package com.example.smartkb.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class McpToolBridgeService {

    private final ToolRegistryService toolRegistryService;
    private final McpToolAdapter mcpToolAdapter;

    public McpToolBridgeService(ToolRegistryService toolRegistryService,
                                McpToolAdapter mcpToolAdapter) {
        this.toolRegistryService = toolRegistryService;
        this.mcpToolAdapter = mcpToolAdapter;
    }

    public List<McpToolDescriptor> listDescriptors() {
        return toolRegistryService.listTools().stream()
                .map(mcpToolAdapter::adapt)
                .toList();
    }

    public ToolExecutionResult invoke(String toolName, Map<String, Object> arguments) {
        return toolRegistryService.execute(toolName, arguments);
    }
}
