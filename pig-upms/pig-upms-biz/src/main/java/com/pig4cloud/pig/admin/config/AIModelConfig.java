package com.pig4cloud.pig.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI模型配置
 *
 * @author pig
 * @date 2025/01/15
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AIModelConfig {
    
    /**
     * 当前使用的模型
     */
    private String currentModel = "deepseek";
    
    /**
     * DeepSeek配置
     */
    private DeepSeekConfig deepseek = new DeepSeekConfig();
    
    /**
     * Kimi配置
     */
    private KimiConfig kimi = new KimiConfig();
    
    @Data
    public static class DeepSeekConfig {
        private ApiConfig api = new ApiConfig();
        private PromptConfig prompt = new PromptConfig();
        private boolean useMockData = false;
    }
    
    @Data
    public static class KimiConfig {
        private ApiConfig api = new ApiConfig();
        private PromptConfig prompt = new PromptConfig();
        private boolean useMockData = false;
    }
    
    @Data
    public static class ApiConfig {
        private String url;
        private String key;
        private String model;
        private double temperature = 0.2;
        private int maxTokens = 3000;
        private boolean stream = false;
        private int timeout = 300000;
    }
    
    @Data
    public static class PromptConfig {
        private String file = "classpath:prompts/evaluation-prompt.txt";
    }
}
