package com.example.smartkb.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 知识库主表
 */
@Entity
@Table(name = "knowledge_base")
public class KnowledgeBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private KnowledgeBaseType type;

    @Column(name = "embedding_model_name", length = 128)
    private String embeddingModelName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private KnowledgePermission permission;

    public KnowledgeBase() {
    }

    public KnowledgeBase(Long id, String name, String description,
                         KnowledgeBaseType type, String embeddingModelName,
                         KnowledgePermission permission) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.embeddingModelName = embeddingModelName;
        this.permission = permission;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public KnowledgeBaseType getType() {
        return type;
    }

    public void setType(KnowledgeBaseType type) {
        this.type = type;
    }

    public String getEmbeddingModelName() {
        return embeddingModelName;
    }

    public void setEmbeddingModelName(String embeddingModelName) {
        this.embeddingModelName = embeddingModelName;
    }

    public KnowledgePermission getPermission() {
        return permission;
    }

    public void setPermission(KnowledgePermission permission) {
        this.permission = permission;
    }

    @Override
    public String toString() {
        return "KnowledgeBase{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", type=" + type +
                ", embeddingModelName='" + embeddingModelName + '\'' +
                ", permission=" + permission +
                '}';
    }

    public enum KnowledgeBaseType {
        DOC,
        WEB
    }

    public enum KnowledgePermission {
        admin,
        user
    }
}
