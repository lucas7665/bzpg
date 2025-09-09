package com.pig4cloud.pig.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 标准评估模块Web配置
 *
 * @author pig
 * @date 2025/01/15
 */
@Configuration
public class EvaluationWebConfig implements WebMvcConfigurer {

    /**
     * 配置静态资源映射
     * 将 /evaluation/static/** 路径映射到 classpath:/static/evaluation/ 目录
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/evaluation/static/**")
                .addResourceLocations("classpath:/static/evaluation/");
    }
}
