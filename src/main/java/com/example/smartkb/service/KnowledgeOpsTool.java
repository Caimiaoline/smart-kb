package com.example.smartkb.service;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class KnowledgeOpsTool implements AgentTool {

    @Override
    public ToolDefinition definition() {
        return new ToolDefinition(
                "knowledge.ops.guide",
                "返回知识库管理相关的建议动作和接口说明，为后续 MCP/Workflow 扩展预留入口",
                List.of(new ToolParameterDefinition("input", "string", false, "原始知识库操作描述"))
        );
    }

    @Override
    public ToolExecutionResult execute(Map<String, Object> arguments) {
        return new ToolExecutionResult(
                name(),
                true,
                "knowledge operation guide generated",
                Map.of(
                        "suggestions", List.of(
                                "上传文档：POST /api/knowledge/{kbId}/upload",
                                "查看文档：GET /api/knowledge/{kbId}/documents",
                                "测试检索：POST /api/knowledge/{kbId}/hit-test"
                        )
                ),
                definition()
        );
    }
}
