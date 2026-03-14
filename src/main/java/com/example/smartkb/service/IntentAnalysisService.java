package com.example.smartkb.service;

import org.springframework.stereotype.Service;

/**
 * 意图解析服务：接收自然语言描述，返回结构化的应用配置草案。
 * 当前实现为本地 Mock，后续可替换为实际 DeepSeek API 调用。
 */
@Service
public class IntentAnalysisService {

    public IntentResponse analyze(String text) {
        // 简单的规则 Mock，可以根据关键词构造不同的配置
        String lower = text == null ? "" : text.toLowerCase();

        String name = "智能助手";
        String description = "面向通用场景的智能体应用。";
        String icon = "bot";
        String model = "DeepSeek-V3";
        String prompt = "你是一名通用领域的智能助手，能够根据组织内的知识库回答用户的问题。";

        if (lower.contains("财务") || lower.contains("报销")) {
            name = "财务报销助手";
            description = "专门回答财务报销相关问题的智能体，对接企业报销流程与制度。";
            icon = "wallet";
            prompt = "你是一名资深财务专家，熟悉公司的报销制度、费用科目和审批流程，请用简洁、友好的语气回答员工的报销相关问题，并在必要时给出风险提示。";
        }

        if (lower.contains("deepseek")) {
            model = "DeepSeek-V3";
        }

        String definitionJson = """
                {
                  "model": "%s",
                  "capabilities": ["qa", "knowledge-base", "lark-bot"],
                  "channels": ["lark"],
                  "metadata": {
                    "created_by": "intent-analyzer",
                    "source": "natural-language",
                    "version": "v1"
                  }
                }
                """.formatted(model);

        IntentData data = new IntentData();
        data.setName(name);
        data.setDescription(description);
        data.setIcon(icon);
        data.setModel(model);
        data.setPrompt(prompt);
        data.setDefinitionJson(definitionJson);

        IntentResponse response = new IntentResponse();
        response.setAction("CREATE");
        response.setData(data);
        return response;
    }

    public static class IntentRequest {
        private String text;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    public static class IntentResponse {
        private String action;
        private IntentData data;

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public IntentData getData() {
            return data;
        }

        public void setData(IntentData data) {
            this.data = data;
        }
    }

    public static class IntentData {
        private String name;
        private String description;
        private String icon;
        private String model;
        private String prompt;
        private String definitionJson;

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

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }

        public String getDefinitionJson() {
            return definitionJson;
        }

        public void setDefinitionJson(String definitionJson) {
            this.definitionJson = definitionJson;
        }
    }
}

