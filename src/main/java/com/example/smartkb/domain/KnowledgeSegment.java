package com.example.smartkb.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 知识切片表（含向量列，维度需与 Embedding 模型一致，如 1024）
 */
@Entity
@Table(name = "knowledge_segment")
public class KnowledgeSegment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "doc_id", nullable = false)
    private Long docId;

    @Column(name = "kb_id", nullable = false)
    private Long kbId;

    @Column(columnDefinition = "text", nullable = false)
    private String content;

    @Convert(converter = VectorAttributeConverter.class)
    @Column(columnDefinition = "vector(1024)")
    private float[] vector;

    @Column(name = "hit_count")
    private Integer hitCount;

    @Column(name = "order_num")
    private Integer orderNum;

    public KnowledgeSegment() {
    }

    public KnowledgeSegment(Long id, Long docId, Long kbId, String content,
                            float[] vector, Integer hitCount, Integer orderNum) {
        this.id = id;
        this.docId = docId;
        this.kbId = kbId;
        this.content = content;
        this.vector = vector;
        this.hitCount = hitCount;
        this.orderNum = orderNum;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDocId() {
        return docId;
    }

    public void setDocId(Long docId) {
        this.docId = docId;
    }

    public Long getKbId() {
        return kbId;
    }

    public void setKbId(Long kbId) {
        this.kbId = kbId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public float[] getVector() {
        return vector;
    }

    public void setVector(float[] vector) {
        this.vector = vector;
    }

    public Integer getHitCount() {
        return hitCount;
    }

    public void setHitCount(Integer hitCount) {
        this.hitCount = hitCount;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    @Override
    public String toString() {
        return "KnowledgeSegment{" +
                "id=" + id +
                ", docId=" + docId +
                ", kbId=" + kbId +
                ", content='" + content + '\'' +
                ", hitCount=" + hitCount +
                ", orderNum=" + orderNum +
                '}';
    }
}
