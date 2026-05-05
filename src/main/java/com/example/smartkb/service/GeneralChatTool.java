package com.example.smartkb.service;

import com.example.smartkb.config.ChatModelProperties;
import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class GeneralChatTool implements AgentTool {

    private static final Logger log = LoggerFactory.getLogger(GeneralChatTool.class);

    private final ChatModelProperties chatModelProperties;
    private OpenAiChatModel chatModel;

    public GeneralChatTool(ChatModelProperties chatModelProperties) {
        this.chatModelProperties = chatModelProperties;
    }

    @PostConstruct
    public void init() {
        if (chatModelProperties.getApiKey() == null || chatModelProperties.getApiKey().isBlank()) {
            log.warn("chat.model.api-key not set, general chat will use local fallback");
            return;
        }
        this.chatModel = OpenAiChatModel.builder()
                .apiKey(chatModelProperties.getApiKey())
                .baseUrl(chatModelProperties.getBaseUrl())
                .modelName(chatModelProperties.getModelName())
                .temperature(chatModelProperties.getTemperature())
                .build();
    }

    @Override
    public ToolDefinition definition() {
        return new ToolDefinition(
                "general.chat",
                "通用对话工具，用于回答闲聊、能力介绍和不需要知识库上下文的问题",
                List.of(
                        new ToolParameterDefinition("message", "string", true, "用户消息")
                )
        );
    }

    @Override
    public ToolExecutionResult execute(Map<String, Object> arguments) {
        String message = String.valueOf(arguments.get("message"));
        String answer;
        String model;
        if (chatModel == null) {
            answer = "我现在可以对话，也可以通过 Agent 调用知识库问答、应用草案生成和工具调试能力。当前聊天模型未配置成功，所以这是本地 fallback 回复。";
            model = "local-fallback";
        } else {
            try {
                answer = chatModel.generate("""
                        你是 SmartKB 的 AI 助手。请用简洁、自然、专业的中文回复用户。
                        如果用户询问系统能力，可以说明你能进行普通对话、知识库问答、应用草案生成和工具调用。

                        用户消息：%s
                        """.formatted(message));
                model = chatModelProperties.getModelName();
            } catch (Exception e) {
                log.warn("general chat failed, fallback to local answer", e);
                answer = "我可以对话，但当前聊天模型调用失败。你仍然可以使用知识库问答、应用生成和工具调试功能。";
                model = "local-fallback";
            }
        }

        return new ToolExecutionResult(
                name(),
                true,
                "general chat completed",
                Map.of(
                        "answer", answer,
                        "model", model
                ),
                definition()
        );
    }
}
