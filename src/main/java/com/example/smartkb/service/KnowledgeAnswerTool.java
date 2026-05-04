package com.example.smartkb.service;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class KnowledgeAnswerTool implements AgentTool {

    private final SearchService searchService;
    private final AnswerGenerationService answerGenerationService;

    public KnowledgeAnswerTool(SearchService searchService,
                               AnswerGenerationService answerGenerationService) {
        this.searchService = searchService;
        this.answerGenerationService = answerGenerationService;
    }

    @Override
    public ToolDefinition definition() {
        return new ToolDefinition(
                "knowledge.answer",
                "先执行专业 RAG 检索，再基于检索片段生成带 citation 的最终回答",
                List.of(
                        new ToolParameterDefinition("query", "string", true, "用户问题"),
                        new ToolParameterDefinition("kbId", "long", true, "知识库 ID"),
                        new ToolParameterDefinition("topN", "int", false, "检索片段数量，默认 5", 5),
                        new ToolParameterDefinition("threshold", "double", false, "相似度阈值，默认 0.35", 0.35)
                )
        );
    }

    @Override
    public ToolExecutionResult execute(Map<String, Object> arguments) {
        String query = String.valueOf(arguments.get("query"));
        Long kbId = Long.valueOf(String.valueOf(arguments.get("kbId")));
        int topN = Integer.parseInt(String.valueOf(arguments.get("topN")));
        double threshold = Double.parseDouble(String.valueOf(arguments.get("threshold")));

        List<SearchService.SegmentScore> contexts = searchService.search(query, kbId, topN, threshold);
        AnswerGenerationService.GeneratedAnswer generatedAnswer = answerGenerationService.generate(query, contexts);

        return new ToolExecutionResult(
                name(),
                true,
                "knowledge answer generated",
                Map.of(
                        "kbId", kbId,
                        "query", query,
                        "answer", generatedAnswer.answer(),
                        "citations", generatedAnswer.citations(),
                        "model", generatedAnswer.model(),
                        "contexts", contexts
                ),
                definition()
        );
    }
}
