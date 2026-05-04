package com.example.smartkb.controller;

import com.example.smartkb.service.AgentMetricsService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
@CrossOrigin
public class MetricsController {

    private final AgentMetricsService agentMetricsService;

    public MetricsController(AgentMetricsService agentMetricsService) {
        this.agentMetricsService = agentMetricsService;
    }

    @GetMapping("/agent")
    public Map<String, Object> agentMetrics() {
        return agentMetricsService.snapshot();
    }
}
