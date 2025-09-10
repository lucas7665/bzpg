package com.pig4cloud.pig.admin.service;

import com.pig4cloud.pig.admin.dto.StandardEvaluationRequest;
import com.pig4cloud.pig.admin.dto.StandardEvaluationResult;

import java.util.List;

/**
 * 标准评估服务接口
 *
 * @author pig
 * @date 2025/01/15
 */
public interface StandardEvaluationService {

    /**
     * 评估标准
     *
     * @param request 评估请求
     * @return 评估结果
     */
    StandardEvaluationResult evaluateStandard(StandardEvaluationRequest request);

    /**
     * 获取当前使用的AI模型
     *
     * @return 当前模型名称
     */
    String getCurrentModel();

    /**
     * 切换AI模型
     *
     * @param modelName 模型名称
     */
    void switchModel(String modelName);

    /**
     * 获取支持的AI模型列表
     *
     * @return 支持的模型列表
     */
    List<String> getSupportedModels();

}
