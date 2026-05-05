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
    private boolean embeddingAvailable;

    public EmbeddingManager(EmbeddingProperties embeddingProperties) {
        this.embeddingProperties = embeddingProperties;
    }

    @PostConstruct
    public void init() {
        if (embeddingProperties.getApiKey() == null || embeddingProperties.getApiKey().isBlank()) {
            log.warn("embedding.model.api-key not set, embedding will be disabled and fallback vectors will be used");
            this.embeddingAvailable = false;
            return;
        }
        this.embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(embeddingProperties.getApiKey())
                .baseUrl(embeddingProperties.getBaseUrl())
                .modelName(embeddingProperties.getModelName())
                .build();
        this.embeddingAvailable = true;
    }

    /**
     * 单条文本转向量
     */
    public float[] embed(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text cannot be blank");
        }
        if (!embeddingAvailable || embeddingModel == null) {
            return fallbackVector(text);
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
        if (!embeddingAvailable || embeddingModel == null) {
            return texts.stream().map(this::fallbackVector).toList();
        }
        List<TextSegment> segments = texts.stream().map(TextSegment::from).collect(Collectors.toList());
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        return embeddings.stream()
                .map(e -> e.vector() != null ? e.vector() : new float[0])
                .collect(Collectors.toList());
    }

    private float[] fallbackVector(String text) {
        int dim = Math.max(8, dimensions());
        float[] vector = new float[dim];
        if (text == null || text.isBlank()) {
            return vector;
        }
        int limit = Math.min(text.length(), 256);
        for (int i = 0; i < limit; i++) {
            int index = Math.abs((text.charAt(i) + i * 31) % dim);
            vector[index] += 1.0f;
        }
        return vector;
    }

    public int dimensions() {
        return embeddingProperties.getDimensions();
    }
}
