package com.example.smartkb.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "embedding.model")
public class EmbeddingProperties {

    private String apiKey = "";
    private String baseUrl = "https://api.deepseek.com";
    private int dimensions = 1024;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int getDimensions() {
        return dimensions;
    }

    public void setDimensions(int dimensions) {
        this.dimensions = dimensions;
    }

    @Override
    public String toString() {
        return "EmbeddingProperties{" +
                "apiKey='****'" +
                ", baseUrl='" + baseUrl + '\'' +
                ", dimensions=" + dimensions +
                '}';
    }
}
