package com.example.smartkb.dto;

import com.example.smartkb.domain.KnowledgeBase;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record KnowledgeBaseCreateRequest(
        @NotBlank String name,
        String description,
        @NotNull KnowledgeBase.KnowledgeBaseType type,
        String embeddingModelName,
        KnowledgeBase.KnowledgePermission permission
) {
    public KnowledgeBase toEntity() {
        KnowledgeBase kb = new KnowledgeBase();
        kb.setName(name());
        kb.setDescription(description());
        kb.setType(type());
        kb.setEmbeddingModelName(embeddingModelName());
        kb.setPermission(permission != null ? permission() : KnowledgeBase.KnowledgePermission.user);
        return kb;
    }
}
