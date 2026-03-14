package com.example.smartkb.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.List;

@Entity
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    private String icon;

    @Column(name = "app_id")
    private String appId;

    @Column(name = "app_secret")
    private String appSecret;

    /**
     * 发布状态，true 表示启用，false 表示停用
     */
    private Boolean status;

    /**
     * 智能体的完整配置，以 JSON 文本形式存储，便于后续动态调整（例如模型、工具、路由等）。
     */
    @Column(name = "definition_json", columnDefinition = "text")
    private String definitionJson;

    /**
     * 智能体的系统 Prompt 模板，用于指导大模型行为。
     */
    @Column(name = "prompt_template", columnDefinition = "text")
    private String promptTemplate;

    /** 绑定的知识库 ID 列表，JSON 数组存储，如 [1,2,3] */
    @Convert(converter = LongListJsonConverter.class)
    @Column(name = "bound_kb_ids", columnDefinition = "text")
    private List<Long> boundKbIds;

    public Application() {
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

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getDefinitionJson() {
        return definitionJson;
    }

    public void setDefinitionJson(String definitionJson) {
        this.definitionJson = definitionJson;
    }

    public String getPromptTemplate() {
        return promptTemplate;
    }

    public void setPromptTemplate(String promptTemplate) {
        this.promptTemplate = promptTemplate;
    }

    public List<Long> getBoundKbIds() {
        return boundKbIds;
    }

    public void setBoundKbIds(List<Long> boundKbIds) {
        this.boundKbIds = boundKbIds;
    }
}

