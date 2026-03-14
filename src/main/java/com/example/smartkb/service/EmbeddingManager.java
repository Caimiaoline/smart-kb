package com.example.smartkb.service;

import com.example.smartkb.config.EmbeddingProperties;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 基于 LangChain4j OpenAiEmbeddingModel 将文本转为向量（兼容 DeepSeek 等 OpenAI 兼容 API）。
 */
@Service
public class EmbeddingManager {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingManager.class);

    private final EmbeddingProperties embeddingProperties;
    private EmbeddingModel embeddingModel;

    public EmbeddingManager(EmbeddingProperties embeddingProperties) {
        this.embeddingProperties = embeddingProperties;
    }

    @PostConstruct
    public void init() {
        if (embeddingProperties.getApiKey() == null || embeddingProperties.getApiKey().isBlank()) {
            log.warn("embedding.model.api-key not set, embedding will fail at runtime");
        }
        this.embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(embeddingProperties.getApiKey())
                .baseUrl(embeddingProperties.getBaseUrl())
                .build();
    }

    /**
     * 单条文本转向量
     */
    public float[] embed(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text cannot be blank");
        }
        Embedding embedding = embeddingModel.embed(TextSegment.from(text)).content();
        float[] v = embedding.vector();
        return v != null ? v : new float[0];
    }

    /**
     * 批量文本转向量（减少 API 调用次数）
     */
    public List<float[]> embed(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return List.of();
        }
        List<TextSegment> segments = texts.stream().map(TextSegment::from).collect(Collectors.toList());
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        return embeddings.stream()
                .map(e -> e.vector() != null ? e.vector() : new float[0])
                .collect(Collectors.toList());
    }

    public int dimensions() {
        return embeddingProperties.getDimensions();
    }
}
