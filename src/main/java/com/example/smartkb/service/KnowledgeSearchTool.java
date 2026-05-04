package com.example.smartkb.service;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class KnowledgeSearchTool implements AgentTool {

    private final SearchService searchService;

    public KnowledgeSearchTool(SearchService searchService) {
        this.searchService = searchService;
    }

    @Override
    public ToolDefinition definition() {
        return new ToolDefinition(
                "knowledge.search",
                "在指定知识库中执行 query rewrite、混合检索、轻量重排和 citation 返回",
                List.of(
                        new ToolParameterDefinition("query", "string", true, "用户查询文本"),
                        new ToolParameterDefinition("kbId", "long", true, "知识库 ID"),
                        new ToolParameterDefinition("topN", "int", false, "返回条数，默认 5", 5),
                        new ToolParameterDefinition("threshold", "double", false, "相似度阈值，默认 0.35", 0.35)
                )
        );
    }

    @Override
    public ToolExecutionResult execute(Map<String, Object> arguments) {
        Object queryObj = arguments.get("query");
        Object kbIdObj = arguments.get("kbId");
        if (queryObj == null || kbIdObj == null) {
            return new ToolExecutionResult(
                    definition().name(),
                    false,
                    "missing required arguments: query, kbId",
                    Map.of("required", List.of("query", "kbId")),
                    definition()
            );
        }

        String query = String.valueOf(queryObj);
        Long kbId = Long.valueOf(String.valueOf(kbIdObj));
        int topN = arguments.get("topN") == null ? 5 : Integer.parseInt(String.valueOf(arguments.get("topN")));
        double threshold = arguments.get("threshold") == null ? 0.35 : Double.parseDouble(String.valueOf(arguments.get("threshold")));

        List<SearchService.SegmentScore> results = searchService.search(query, kbId, topN, threshold);
        return new ToolExecutionResult(
                definition().name(),
                true,
                "knowledge search completed",
                Map.of(
                        "kbId", kbId,
                        "count", results.size(),
                        "topN", topN,
                        "threshold", threshold,
                        "results", results
                ),
                definition()
        );
    }
}
