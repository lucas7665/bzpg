package com.pig4cloud.pig.admin.service.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AI模型工厂
 *
 * @author pig
 * @date 2025/01/15
 */
@Component
@RequiredArgsConstructor
public class AIModelFactory {
    
    private final List<AIModelService> modelServices;
    private Map<String, AIModelService> modelServiceMap;
    
    /**
     * 初始化模型服务映射
     */
    private void initModelServiceMap() {
        if (modelServiceMap == null) {
            modelServiceMap = modelServices.stream()
                .collect(Collectors.toMap(
                    AIModelService::getModelName,
                    Function.identity()
                ));
        }
    }
    
    /**
     * 获取模型服务
     *
     * @param modelName 模型名称
     * @return 模型服务
     */
    public AIModelService getModelService(String modelName) {
        initModelServiceMap();
        AIModelService service = modelServiceMap.get(modelName.toLowerCase());
        if (service == null) {
            throw new IllegalArgumentException("不支持的模型: " + modelName);
        }
        return service;
    }
    
    /**
     * 获取所有支持的模型名称
     *
     * @return 模型名称列表
     */
    public List<String> getSupportedModels() {
        initModelServiceMap();
        return modelServices.stream()
            .map(AIModelService::getModelName)
            .collect(Collectors.toList());
    }
}
