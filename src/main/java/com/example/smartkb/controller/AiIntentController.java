package com.example.smartkb.controller;

import com.example.smartkb.service.IntentAnalysisService;
import com.example.smartkb.service.IntentAnalysisService.IntentRequest;
import com.example.smartkb.service.IntentAnalysisService.IntentResponse;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin
public class AiIntentController {

    private final IntentAnalysisService intentAnalysisService;

    public AiIntentController(IntentAnalysisService intentAnalysisService) {
        this.intentAnalysisService = intentAnalysisService;
    }

    @PostMapping("/analyze-intent")
    public IntentResponse analyze(@RequestBody IntentRequest request) {
        return intentAnalysisService.analyze(request.getText());
    }
}

