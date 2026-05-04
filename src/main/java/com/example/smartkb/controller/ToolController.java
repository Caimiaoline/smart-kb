package com.example.smartkb.controller;

import com.example.smartkb.dto.ToolExecutionRequest;
import com.example.smartkb.service.ToolDefinition;
import com.example.smartkb.service.McpToolAdapter;
import com.example.smartkb.service.ToolExecutionResult;
import com.example.smartkb.service.ToolRegistryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tools")
@CrossOrigin
public class ToolController {

    private final ToolRegistryService toolRegistryService;
    private final McpToolAdapter mcpToolAdapter;

    public ToolController(ToolRegistryService toolRegistryService,
                          McpToolAdapter mcpToolAdapter) {
        this.toolRegistryService = toolRegistryService;
        this.mcpToolAdapter = mcpToolAdapter;
    }

    @GetMapping
    public Map<String, Object> list() {
        List<ToolDefinition> tools = toolRegistryService.listTools();
        return Map.of("count", tools.size(), "tools", tools);
    }

    @GetMapping("/mcp")
    public Map<String, Object> listMcpStyleTools() {
        return Map.of(
                "protocol", "mcp-style-tool-schema",
                "tools", toolRegistryService.listTools().stream()
                        .map(mcpToolAdapter::adapt)
                        .toList()
        );
    }

    @PostMapping("/execute")
    public ToolExecutionResult execute(@Valid @RequestBody ToolExecutionRequest request) {
        return toolRegistryService.execute(request.toolName(), request.arguments());
    }
}
