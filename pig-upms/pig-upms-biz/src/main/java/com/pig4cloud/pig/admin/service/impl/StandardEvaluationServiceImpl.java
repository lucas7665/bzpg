package com.pig4cloud.pig.admin.service.impl;

import com.pig4cloud.pig.admin.config.AIModelConfig;
import com.pig4cloud.pig.admin.dto.StandardEvaluationRequest;
import com.pig4cloud.pig.admin.dto.StandardEvaluationResult;
import com.pig4cloud.pig.admin.service.StandardEvaluationService;
import com.pig4cloud.pig.admin.service.ai.AIModelFactory;
import com.pig4cloud.pig.admin.service.ai.AIModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 标准评估服务实现类
 *
 * @author pig
 * @date 2025/01/15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StandardEvaluationServiceImpl implements StandardEvaluationService {

    private final AIModelFactory aiModelFactory;
    private final AIModelConfig aiModelConfig;

    @Override
    public StandardEvaluationResult evaluateStandard(StandardEvaluationRequest request) {
        StandardEvaluationResult result = new StandardEvaluationResult();
        
        try {
            // 检查是否启用本地模型
            String currentModel = aiModelConfig.getCurrentModel();
            if (aiModelConfig.getLocal().isEnabled()) {
                currentModel = "local-deepseek";
                log.info("本地模型已启用，使用本地模型({})，标准名称：{}",
                    aiModelConfig.getLocal().getApi().getModel(), request.getTitle());
            } else {
                log.info("使用AI模型: {}，标准名称：{}", currentModel, request.getTitle());
            }
            
            // 获取对应的模型服务
            AIModelService modelService = aiModelFactory.getModelService(currentModel);
            
            // 调用模型进行评估
            String response = modelService.evaluate(request);
            
            // 解析响应
            parseResponse(response, result);
            result.setStatus("SUCCESS");
            
        } catch (Exception e) {
            log.error("标准评估失败", e);
            result.setStatus("FAILED");
            result.setErrorMessage("评估过程中发生错误：" + e.getMessage());
        }
        
        return result;
    }

    @Override
    public String getCurrentModel() {
        if (aiModelConfig.getLocal().isEnabled()) {
            return "local:" + aiModelConfig.getLocal().getApi().getModel();
        }
        return aiModelConfig.getCurrentModel();
    }

    @Override
    public void switchModel(String modelName) {
        // 检查是否是本地模型切换格式 local:<模型名>
        if (modelName.startsWith("local:")) {
            String localModelName = modelName.substring(6); // 去掉 "local:" 前缀
            // 启用本地模型并设置具体模型名
            aiModelConfig.getLocal().setEnabled(true);
            aiModelConfig.getLocal().getApi().setModel(localModelName);
            log.info("本地模型已切换为: {}", localModelName);
            return;
        }
        
        // 验证模型是否支持
        if (!aiModelFactory.getModelService(modelName).supports(modelName)) {
            throw new IllegalArgumentException("不支持的模型: " + modelName);
        }
        
        // 更新配置
        aiModelConfig.setCurrentModel(modelName);
        log.info("AI模型已切换为: {}", modelName);
    }

    @Override
    public List<String> getSupportedModels() {
        List<String> models = aiModelFactory.getSupportedModels();
        // 添加本地模型选项
        models.add("local:qwen2.5:7b-instruct");
        models.add("local:deepseek-r1:7b");
        return models;
    }

    /**
     * 解析AI响应
     */
    private void parseResponse(String response, StandardEvaluationResult result) {
        if (response == null || response.trim().isEmpty()) {
            result.setResultTable("暂无评估结果");
            result.setResult("评估失败");
            return;
        }
        
        // 直接返回markdown内容，让前端解析
        result.setResultTable(response);
        result.setResult("SUCCESS");
    }
}