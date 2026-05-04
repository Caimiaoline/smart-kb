package com.example.smartkb.service;

import com.example.smartkb.config.ChatModelProperties;
import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnswerGenerationService {

    private static final Logger log = LoggerFactory.getLogger(AnswerGenerationService.class);

    private final ChatModelProperties chatModelProperties;
    private OpenAiChatModel chatModel;

    public AnswerGenerationService(ChatModelProperties chatModelProperties) {
        this.chatModelProperties = chatModelProperties;
    }

    @PostConstruct
    public void init() {
        if (chatModelProperties.getApiKey() == null || chatModelProperties.getApiKey().isBlank()) {
            log.warn("chat.model.api-key not set, answer generation will use extractive fallback");
            return;
        }
        this.chatModel = OpenAiChatModel.builder()
                .apiKey(chatModelProperties.getApiKey())
                .baseUrl(chatModelProperties.getBaseUrl())
                .modelName(chatModelProperties.getModelName())
                .temperature(chatModelProperties.getTemperature())
                .build();
    }

    public GeneratedAnswer generate(String query, List<SearchService.SegmentScore> contexts) {
        if (contexts == null || contexts.isEmpty()) {
            return new GeneratedAnswer(
                    "没有检索到足够相关的知识片段，建议补充知识库内容或降低检索阈值后重试。",
                    List.of(),
                    "fallback-no-context"
            );
        }

        List<String> citations = contexts.stream()
                .map(SearchService.SegmentScore::citation)
                .distinct()
                .toList();

        String prompt = buildPrompt(query, contexts);
        if (chatModel == null) {
            return new GeneratedAnswer(buildExtractiveFallback(contexts), citations, "extractive-fallback");
        }

        try {
            String answer = chatModel.generate(prompt);
            return new GeneratedAnswer(answer, citations, chatModelProperties.getModelName());
        } catch (Exception e) {
            log.warn("answer generation failed, fallback to extractive answer", e);
            return new GeneratedAnswer(buildExtractiveFallback(contexts), citations, "extractive-fallback");
        }
    }

    private String buildPrompt(String query, List<SearchService.SegmentScore> contexts) {
        String contextText = contexts.stream()
                .map(item -> "[" + item.citation() + "]\n" + item.content())
                .collect(Collectors.joining("\n\n"));

        return """
                你是一个企业知识库问答助手。请只根据给定知识片段回答用户问题。

                要求：
                1. 如果知识片段不足以回答，请明确说资料不足，不要编造。
                2. 回答要简洁、结构清晰。
                3. 关键结论后面尽量标注引用来源，例如：[知识库#1 / 文档#2 / xxx.docx / chunk-3]。

                用户问题：
                %s

                知识片段：
                %s
                """.formatted(query, contextText);
    }

    private String buildExtractiveFallback(List<SearchService.SegmentScore> contexts) {
        String bullets = contexts.stream()
                .limit(3)
                .map(item -> "- " + item.content() + "\n  来源：" + item.citation())
                .collect(Collectors.joining("\n"));
        return "基于当前检索到的知识片段，可以参考以下内容：\n" + bullets;
    }

    public record GeneratedAnswer(String answer,
                                  List<String> citations,
                                  String model) {
    }
}
