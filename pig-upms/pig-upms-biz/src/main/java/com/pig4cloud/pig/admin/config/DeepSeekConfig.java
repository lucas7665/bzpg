package com.pig4cloud.pig.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * DeepSeek API 配置类
 *
 * @author pig
 * @date 2025/01/15
 */
@Data
@Component
@ConfigurationProperties(prefix = "deepseek")
public class DeepSeekConfig {

    private Api api = new Api();
    private Prompt prompt = new Prompt();

    @Data
    public static class Api {
        private String url = "https://api.deepseek.com/v1/chat/completions";
        private String key = "";
        private String model = "deepseek-chat";  // 使用正确的模型名称
        private Double temperature = 0.3;
        private Integer maxTokens = 3000;
        private Boolean stream = false;
        private Integer timeout = 300000; // 5分钟超时
    }

    @Data
    public static class Prompt {
        private String file = "classpath:prompts/evaluation-prompt.txt";  // 详细模式
        private String fastFile = "classpath:prompts/evaluation-prompt-fast.txt";  // 快速模式
    }

    /**
     * 获取快速模型配置
     */
    public Api getFastModelConfig() {
        Api fastConfig = new Api();
        fastConfig.setUrl(this.api.url);
        fastConfig.setKey(this.api.key);
        fastConfig.setModel("deepseek-chat"); // 使用deepseek-chat模型（已升级至V3）
        fastConfig.setTemperature(0.1);       // 最低随机性
        fastConfig.setMaxTokens(1500);        // 进一步减少token数
        fastConfig.setStream(false);
        fastConfig.setTimeout(45000);         // 45秒超时
        return fastConfig;
    }

    /**
     * 获取高质量模型配置
     */
    public Api getQualityModelConfig() {
        Api qualityConfig = new Api();
        qualityConfig.setUrl(this.api.url);
        qualityConfig.setKey(this.api.key);
        qualityConfig.setModel("deepseek-chat"); // 使用高质量模型
        qualityConfig.setTemperature(0.7);       // 较高随机性
        qualityConfig.setMaxTokens(4000);        // 更多token
        qualityConfig.setStream(false);
        qualityConfig.setTimeout(180000);        // 3分钟超时
        return qualityConfig;
    }
}
