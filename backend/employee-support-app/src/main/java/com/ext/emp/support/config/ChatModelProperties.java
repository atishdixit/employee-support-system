package com.ext.emp.support.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Application-level chat configuration, sourced from .env / profile-specific YAML
 * (see application*.yml). Kept separate from the raw spring.ai.ollama.* connection
 * properties so the portable ChatOptions built in ChatClientConfig has one clear owner,
 * and so token limits can be validated at startup regardless of which model provider
 * is wired underneath.
 */
@Component
@ConfigurationProperties(prefix = "app.chat")
public class ChatModelProperties {

    /** Label only today (a single Ollama starter is on the classpath) — see docs/SETUP.md
     *  "Adding a new LLM provider" for how this becomes a real switch later. */
    private String provider = "ollama";
    private String model = "llama3.2:3b";
    private Double temperature = 0.4;
    private Integer maxTokens = 300;
    private Integer minTokens = 20;

    @PostConstruct
    void validate() {
        if (minTokens != null && maxTokens != null && maxTokens < minTokens) {
            throw new IllegalStateException(
                    "app.chat.max-tokens (%d) must be >= app.chat.min-tokens (%d)".formatted(maxTokens, minTokens));
        }
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public Integer getMinTokens() {
        return minTokens;
    }

    public void setMinTokens(Integer minTokens) {
        this.minTokens = minTokens;
    }
}
