package com.example.smartkb.controller;

import com.example.smartkb.dto.AgentTaskRequest;
import com.example.smartkb.service.AgentOrchestratorService;
import com.example.smartkb.service.AgentOrchestratorService.AgentTaskResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent")
@CrossOrigin
public class AgentController {

    private final AgentOrchestratorService agentOrchestratorService;

    public AgentController(AgentOrchestratorService agentOrchestratorService) {
        this.agentOrchestratorService = agentOrchestratorService;
    }

    @PostMapping("/route")
    public AgentTaskResult route(@Valid @RequestBody AgentTaskRequest request) {
        return agentOrchestratorService.route(request);
    }
}
