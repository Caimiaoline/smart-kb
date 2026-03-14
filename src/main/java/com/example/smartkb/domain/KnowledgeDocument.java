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
 * 知识库文档表
 */
@Entity
@Table(name = "knowledge_document")
public class KnowledgeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kb_id", nullable = false)
    private Long kbId;

    @Column(name = "file_name", nullable = false, length = 512)
    private String fileName;

    @Column(name = "file_type", length = 64)
    private String fileType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DocumentStatus status;

    @Column(name = "char_count")
    private Integer charCount;

    @Column(name = "token_count")
    private Integer tokenCount;

    public KnowledgeDocument() {
    }

    public KnowledgeDocument(Long id, Long kbId, String fileName, String fileType,
                             DocumentStatus status, Integer charCount, Integer tokenCount) {
        this.id = id;
        this.kbId = kbId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.status = status;
        this.charCount = charCount;
        this.tokenCount = tokenCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getKbId() {
        return kbId;
    }

    public void setKbId(Long kbId) {
        this.kbId = kbId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentStatus status) {
        this.status = status;
    }

    public Integer getCharCount() {
        return charCount;
    }

    public void setCharCount(Integer charCount) {
        this.charCount = charCount;
    }

    public Integer getTokenCount() {
        return tokenCount;
    }

    public void setTokenCount(Integer tokenCount) {
        this.tokenCount = tokenCount;
    }

    @Override
    public String toString() {
        return "KnowledgeDocument{" +
                "id=" + id +
                ", kbId=" + kbId +
                ", fileName='" + fileName + '\'' +
                ", fileType='" + fileType + '\'' +
                ", status=" + status +
                ", charCount=" + charCount +
                ", tokenCount=" + tokenCount +
                '}';
    }

    public enum DocumentStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        ERROR
    }
}
