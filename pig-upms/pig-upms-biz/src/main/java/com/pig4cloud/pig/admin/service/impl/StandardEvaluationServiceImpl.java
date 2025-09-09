package com.pig4cloud.pig.admin.service.impl;

import com.pig4cloud.pig.admin.config.DeepSeekConfig;
import com.pig4cloud.pig.admin.dto.StandardEvaluationRequest;
import com.pig4cloud.pig.admin.dto.StandardEvaluationResult;
import com.pig4cloud.pig.admin.service.StandardEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

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

    private final RestTemplate restTemplate;
    private final DeepSeekConfig deepSeekConfig;

    @Value("${deepseek.prompt.file:classpath:prompts/evaluation-prompt.txt}")
    private String promptFilePath;

    @Value("${deepseek.prompt.fast_file:classpath:prompts/evaluation-prompt-fast.txt}")
    private String fastPromptFilePath;


    @Override
    public StandardEvaluationResult evaluateStandard(StandardEvaluationRequest request) {
        StandardEvaluationResult result = new StandardEvaluationResult();
        
        try {
            // 构建prompt（使用详细模式）
            String prompt = buildPrompt(request.getTitle(),false);
            
            // 调用DeepSeek API（使用详细模式）
            String response = callDeepSeekApi(prompt, false);
            
            // 解析响应结果
            parseResponse(response, result);
            
            result.setStatus("SUCCESS");
            
        } catch (Exception e) {
            log.error("标准评估失败", e);
            result.setStatus("FAILED");
            result.setErrorMessage("评估过程中发生错误：" + e.getMessage());
        }
        
        return result;
    }

    /**
     * 构建prompt
     */
    private String buildPrompt(String title) {
        return buildPrompt(title, false);
    }

    /**
     * 构建prompt（支持快速模式）
     */
    private String buildPrompt(String title, boolean fastMode) {
        try {
            String promptTemplate = loadPromptTemplate(fastMode);
            return promptTemplate.replace("{TITLE}", title);
        } catch (Exception e) {
            log.error("加载提示词模板失败，使用默认模板", e);
            return getDefaultPrompt(title);
        }
    }

    /**
     * 加载提示词模板
     */
    private String loadPromptTemplate() throws IOException {
        return loadPromptTemplate(false);
    }

    /**
     * 加载提示词模板（支持快速模式）
     */
    private String loadPromptTemplate(boolean fastMode) throws IOException {
        String filePath = fastMode ? fastPromptFilePath : promptFilePath;
        
        if (filePath.startsWith("classpath:")) {
            String resourcePath = filePath.substring("classpath:".length());
            return new String(
                this.getClass().getClassLoader().getResourceAsStream(resourcePath).readAllBytes(),
                StandardCharsets.UTF_8
            );
        } else {
            return Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);
        }
    }

    /**
     * 获取默认提示词（备用）
     */
    private String getDefaultPrompt(String title) {
        return String.format("""
            请分析标准名称：%s
            
            请从技术成熟度和市场成熟度两个维度进行评估，并给出是否适合制定标准的建议。
            
            请按照以下格式输出：
            1. 技术成熟度分析
            2. 市场成熟度分析  
            3. 综合评估结论
            """, title);
    }

    /**
     * 调用DeepSeek API
     */
    private String callDeepSeekApi(String prompt) {
        return callDeepSeekApi(prompt, false);
    }

    /**
     * 调用DeepSeek API（支持快速模式）
     */
    private String callDeepSeekApi(String prompt, boolean fastMode) {
        DeepSeekConfig.Api apiConfig = fastMode ? 
            deepSeekConfig.getFastModelConfig() : 
            deepSeekConfig.getApi();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiConfig.getKey());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", apiConfig.getModel());
        requestBody.put("messages", new Object[]{
            Map.of("role", "user", "content", prompt)
        });
        requestBody.put("temperature", apiConfig.getTemperature());
        requestBody.put("max_tokens", apiConfig.getMaxTokens());
        requestBody.put("stream", apiConfig.getStream());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(apiConfig.getUrl(), entity, Map.class);
        
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
            if (responseBody.containsKey("choices")) {
                @SuppressWarnings("unchecked")
                java.util.List<Object> choicesList = (java.util.List<Object>) responseBody.get("choices");
                if (!choicesList.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> choice = (Map<String, Object>) choicesList.get(0);
                    @SuppressWarnings("unchecked")
                    Map<String, Object> message = (Map<String, Object>) choice.get("message");
                    return (String) message.get("content");
                }
            }
        }
        
        throw new RuntimeException("DeepSeek API调用失败");
    }

    /**
     * 解析响应结果
     */
    private void parseResponse(String response, StandardEvaluationResult result) {
        // 查找表格部分
        if (response.contains("| 维度 |")) {
            int tableStartIndex = response.indexOf("| 维度 |");
            
            // 查找表格结束位置（综合评估结论开始）
            int tableEndIndex = response.length();
            if (response.contains("### 综合评估结论")) {
                tableEndIndex = response.indexOf("### 综合评估结论");
            } else if (response.contains("- 建议：")) {
                tableEndIndex = response.indexOf("- 建议：");
            }
            
            // 提取表格部分
            String tableContent = response.substring(tableStartIndex, tableEndIndex).trim();
            result.setResultTable(tableContent);
            
            // 提取综合评估结论部分
            String conclusionContent = response.substring(tableEndIndex).trim();
            if (conclusionContent.startsWith("### 综合评估结论")) {
                // 移除标题，只保留内容
                conclusionContent = conclusionContent.replaceFirst("### 综合评估结论\\s*", "");
            }
            result.setResult(conclusionContent);
        } else {
            // 如果没有找到表格，将整个响应作为结果
            result.setResultTable(response);
            result.setResult("请查看详细分析结果");
        }
    }
}
