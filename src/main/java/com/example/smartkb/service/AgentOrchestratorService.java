package com.example.smartkb.service;

import com.example.smartkb.dto.AgentTaskRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AgentOrchestratorService {

    private final ToolRegistryService toolRegistryService;
    private final LlmRouterService llmRouterService;
    private final AgentMetricsService agentMetricsService;

    public AgentOrchestratorService(ToolRegistryService toolRegistryService,
                                    LlmRouterService llmRouterService,
                                    AgentMetricsService agentMetricsService) {
        this.toolRegistryService = toolRegistryService;
        this.llmRouterService = llmRouterService;
        this.agentMetricsService = agentMetricsService;
    }

    public AgentTaskResult route(AgentTaskRequest request) {
        String taskId = UUID.randomUUID().toString();
        Instant startedAt = Instant.now();
        List<AgentTraceEvent> events = new ArrayList<>();
        String input = request.input() == null ? "" : request.input().trim();
        String lower = input.toLowerCase();
        TaskStatus status = TaskStatus.PENDING;
        events.add(new AgentTraceEvent("input", "completed", Instant.now(), Map.of("input", input, "taskId", taskId)));

        status = TaskStatus.PLANNING;
        RouteDecision llmDecision = llmRouterService.decide(input);
        AgentPlan plan = buildPlan(request, input, lower, llmDecision);
        events.add(new AgentTraceEvent("planning", "completed", Instant.now(), Map.of(
                "route", plan.route(),
                "selectedTool", plan.toolName() == null ? "none" : plan.toolName(),
                "llmBased", plan.llmBased(),
                "steps", plan.steps()
        )));

        ToolExecutionResult executionResult;
        if (plan.toolName() == null) {
            status = TaskStatus.FALLBACK;
            executionResult = new ToolExecutionResult("none", false, "no executable tool selected", Map.of(), null);
            events.add(new AgentTraceEvent("tool_execution", "skipped", Instant.now(), Map.of("reason", "no executable tool selected")));
        } else {
            status = TaskStatus.RUNNING;
            events.add(new AgentTraceEvent("tool_execution", "started", Instant.now(), Map.of(
                    "tool", plan.toolName(),
                    "arguments", plan.arguments()
            )));
            executionResult = toolRegistryService.execute(plan.toolName(), plan.arguments());
            status = executionResult.success() ? TaskStatus.SUCCEEDED : TaskStatus.FAILED;
            events.add(new AgentTraceEvent("tool_execution", executionResult.success() ? "completed" : "failed", Instant.now(), Map.of(
                    "tool", plan.toolName(),
                    "message", executionResult.message()
            )));
        }
        Instant finishedAt = Instant.now();
        events.add(new AgentTraceEvent("finish", "completed", finishedAt, Map.of("success", executionResult.success(), "status", status.name())));

        AgentTrace trace = new AgentTrace(
                startedAt,
                finishedAt,
                Math.max(0, finishedAt.toEpochMilli() - startedAt.toEpochMilli()),
                plan.steps(),
                plan.toolName(),
                plan.arguments(),
                executionResult.success(),
                events
        );

        agentMetricsService.record(plan.route(), plan.toolName(), executionResult.success(), trace.durationMs());

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("taskId", taskId);
        metadata.put("status", status.name());
        metadata.put("availableTools", toolRegistryService.listTools());
        metadata.put("input", input);
        metadata.put("trace", trace);
        metadata.put("toolSchema", executionResult.definition());
        metadata.put("llmRouterUsed", plan.llmBased());
        metadata.put("metrics", agentMetricsService.snapshot());

        return new AgentTaskResult(
                plan.route(),
                plan.reason(),
                plan.steps(),
                plan.toolName(),
                executionResult,
                metadata
        );
    }

    private AgentPlan buildPlan(AgentTaskRequest request, String input, String lower, RouteDecision llmDecision) {
        if (llmDecision != null) {
            return toAgentPlan(request, input, llmDecision);
        }

        if (shouldRouteToAppBuilder(lower)) {
            return new AgentPlan(
                    "app_builder",
                    "识别到创建应用/助手意图，选择应用草案生成工具",
                    List.of("解析自然语言需求", "选择 application.draft 工具", "生成应用草案", "返回名称、Prompt 和定义 JSON"),
                    "application.draft",
                    Map.of("text", input),
                    false
            );
        }

        if (shouldRouteToKnowledgeQa(request, lower)) {
            Long kbId = request.kbId();
            if (kbId == null) {
                return new AgentPlan(
                        "fallback",
                        "识别到知识问答意图，但缺少 kbId，无法执行知识检索工具",
                        List.of("识别知识问答", "校验知识库上下文", "提示补充 kbId"),
                        null,
                        Map.of(),
                        false
                );
            }
            return new AgentPlan(
                    "knowledge_qa",
                    "识别到知识问答意图，选择 knowledge.answer 工具",
                    List.of("改写查询", "选择 knowledge.answer 工具", "混合检索召回", "轻量重排", "生成最终回答", "返回带引用的答案"),
                    "knowledge.answer",
                    Map.of(
                            "query", input,
                            "kbId", kbId,
                            "topN", 5,
                            "threshold", 0.35
                    ),
                    false
            );
        }

        if (shouldRouteToKnowledgeOps(lower)) {
            return new AgentPlan(
                    "knowledge_ops",
                    "识别到知识库管理意图，选择知识库操作引导工具",
                    List.of("识别知识库操作类型", "选择 knowledge.ops.guide 工具", "返回接口和操作建议"),
                    "knowledge.ops.guide",
                    Map.of("input", input),
                    false
            );
        }

        return new AgentPlan(
                "general_chat",
                "当前输入未命中专业任务路由，选择通用对话工具",
                List.of("识别为通用对话", "选择 general.chat 工具", "生成自然语言回复"),
                "general.chat",
                Map.of("message", input),
                false
        );
    }

    private AgentPlan toAgentPlan(AgentTaskRequest request, String input, RouteDecision decision) {
        return switch (decision.preferredTool()) {
            case "knowledge.answer" -> request.kbId() == null
                    ? new AgentPlan("fallback", "LLM 识别到知识问答，但缺少 kbId", List.of("LLM 路由", "检查上下文", "返回缺失提示"), null, Map.of(), true)
                    : new AgentPlan(decision.route(), decision.reason(), decision.suggestedSteps(), "knowledge.answer", Map.of(
                    "query", input,
                    "kbId", request.kbId(),
                    "topN", 5,
                    "threshold", 0.35
            ), true);
            case "application.draft" -> new AgentPlan(decision.route(), decision.reason(), decision.suggestedSteps(), "application.draft", Map.of("text", input), true);
            case "knowledge.ops.guide" -> new AgentPlan(decision.route(), decision.reason(), decision.suggestedSteps(), "knowledge.ops.guide", Map.of("input", input), true);
            case "general.chat" -> new AgentPlan(decision.route(), decision.reason(), decision.suggestedSteps(), "general.chat", Map.of("message", input), true);
            default -> new AgentPlan("general_chat", decision.reason(), decision.suggestedSteps(), "general.chat", Map.of("message", input), true);
        };
    }

    private boolean shouldRouteToAppBuilder(String lower) {
        return containsAny(lower,
                "创建应用", "生成应用", "创建助手", "智能体", "报销助手", "客服助手", "知识助手");
    }

    private boolean shouldRouteToKnowledgeQa(AgentTaskRequest request, String lower) {
        return request.kbId() != null || containsAny(lower,
                "怎么", "如何", "为什么", "报错", "失败", "异常", "配置", "参数", "设置");
    }

    private boolean shouldRouteToKnowledgeOps(String lower) {
        return containsAny(lower,
                "上传", "导入", "知识库", "文档列表", "查看文档", "管理文档");
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    public record AgentPlan(String route,
                            String reason,
                            List<String> steps,
                            String toolName,
                            Map<String, Object> arguments,
                            boolean llmBased) {
    }

    public record AgentTaskResult(String route,
                                  String reason,
                                  List<String> plan,
                                  String selectedTool,
                                  ToolExecutionResult execution,
                                  Map<String, Object> metadata) {
    }
}
