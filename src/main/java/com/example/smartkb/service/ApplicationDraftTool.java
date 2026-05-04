package com.example.smartkb.service;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ApplicationDraftTool implements AgentTool {

    private final IntentAnalysisService intentAnalysisService;

    public ApplicationDraftTool(IntentAnalysisService intentAnalysisService) {
        this.intentAnalysisService = intentAnalysisService;
    }

    @Override
    public ToolDefinition definition() {
        return new ToolDefinition(
                "application.draft",
                "根据自然语言需求生成智能体应用草案，包括名称、Prompt 和 definitionJson",
                List.of(new ToolParameterDefinition("text", "string", true, "自然语言应用需求"))
        );
    }

    @Override
    public ToolExecutionResult execute(Map<String, Object> arguments) {
        Object textObj = arguments.get("text");
        if (textObj == null) {
            return new ToolExecutionResult(
                    name(),
                    false,
                    "missing required argument: text",
                    Map.of("required", List.of("text")),
                    definition()
            );
        }
        IntentAnalysisService.IntentResponse draft = intentAnalysisService.analyze(String.valueOf(textObj));
        return new ToolExecutionResult(
                name(),
                true,
                "application draft generated",
                Map.of("draft", draft),
                definition()
        );
    }
}
