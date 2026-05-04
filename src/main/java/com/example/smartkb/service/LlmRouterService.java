package com.example.smartkb.service;

import com.example.smartkb.config.ChatModelProperties;
import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LlmRouterService {

    private static final Logger log = LoggerFactory.getLogger(LlmRouterService.class);

    private final ChatModelProperties chatModelProperties;
    private OpenAiChatModel chatModel;

    public LlmRouterService(ChatModelProperties chatModelProperties) {
        this.chatModelProperties = chatModelProperties;
    }

    @PostConstruct
    public void init() {
        if (chatModelProperties.getApiKey() == null || chatModelProperties.getApiKey().isBlank()) {
            log.warn("chat.model.api-key not set, llm router disabled");
            return;
        }
        this.chatModel = OpenAiChatModel.builder()
                .apiKey(chatModelProperties.getApiKey())
                .baseUrl(chatModelProperties.getBaseUrl())
                .modelName(chatModelProperties.getModelName())
                .temperature(0.0)
                .build();
    }

    public RouteDecision decide(String input) {
        if (chatModel == null || input == null || input.isBlank()) {
            return null;
        }
        try {
            String result = chatModel.generate("""
                    你是任务路由器。你只能在以下 route 中选一个：
                    - knowledge_qa
                    - app_builder
                    - knowledge_ops
                    - fallback

                    用户输入：%s

                    请只返回一行，格式：route=<route>;tool=<tool>;reason=<一句话>
                    tool 只允许：knowledge.answer, application.draft, knowledge.ops.guide, none
                    """.formatted(input));
            return parse(result);
        } catch (Exception e) {
            log.warn("llm router failed, fallback to rule router", e);
            return null;
        }
    }

    private RouteDecision parse(String raw) {
        if (raw == null) {
            return null;
        }
        String route = extract(raw, "route=");
        String tool = extract(raw, "tool=");
        String reason = extract(raw, "reason=");
        if (route == null || tool == null) {
            return null;
        }
        return new RouteDecision(
                route.trim(),
                reason == null || reason.isBlank() ? "LLM router selected route" : reason.trim(),
                List.of("LLM route decision", "select tool", "execute task"),
                tool.trim(),
                true
        );
    }

    private String extract(String raw, String key) {
        int idx = raw.indexOf(key);
        if (idx < 0) return null;
        int start = idx + key.length();
        int end = raw.indexOf(';', start);
        if (end < 0) {
            end = raw.length();
        }
        return raw.substring(start, end);
    }
}
