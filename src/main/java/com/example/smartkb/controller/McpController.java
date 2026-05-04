package com.example.smartkb.controller;

import com.example.smartkb.dto.ToolExecutionRequest;
import com.example.smartkb.service.McpToolBridgeService;
import com.example.smartkb.service.ToolExecutionResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/mcp")
@CrossOrigin
public class McpController {

    private final McpToolBridgeService mcpToolBridgeService;

    public McpController(McpToolBridgeService mcpToolBridgeService) {
        this.mcpToolBridgeService = mcpToolBridgeService;
    }

    @GetMapping("/tools")
    public Map<String, Object> listTools() {
        return Map.of(
                "protocol", "mcp-bridge",
                "tools", mcpToolBridgeService.listDescriptors()
        );
    }

    @PostMapping("/invoke")
    public ToolExecutionResult invoke(@Valid @RequestBody ToolExecutionRequest request) {
        return mcpToolBridgeService.invoke(request.toolName(), request.arguments());
    }
}
