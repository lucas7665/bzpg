package com.pig4cloud.pig.admin.service.ai.impl;

import com.pig4cloud.pig.admin.dto.StandardEvaluationRequest;
import com.pig4cloud.pig.admin.service.ai.AIModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Paths;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.HashMap;
import java.util.Map;

/**
 * 本地DeepSeek模型服务实现（通过Ollama）
 *
 * @author pig
 * @date 2025/01/15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocalDeepSeekModelService implements AIModelService {
    
    private final RestTemplate restTemplate;
    
    @Value("${ai.local.api.url:http://localhost:11434/api/generate}")
    private String apiUrl;
    
    @Value("${ai.local.api.model:deepseek-coder}")
    private String model;
    
    @Value("${ai.local.api.temperature:0.2}")
    private double temperature;
    
    @Value("${ai.local.api.max_tokens:3000}")
    private int maxTokens;
    
    @Value("${ai.local.api.timeout:300000}")
    private int timeout;
    
    @Value("${ai.local.prompt.file:classpath:prompts/evaluation-prompt.txt}")
    private String promptFile;
    
    @Value("${ai.local.use_mock_data:false}")
    private boolean useMockData;
    
    private static final String MOCK_RESPONSE = "| 维度 | 子维度 | 维度具体情况 | 结论 | 相关说明 |\n|------|--------|----------------|------|----------|\n| 技术 | 技术成熟度 | 技术成熟度是几级？ | TRL 4-6 | 元宇宙技术处于技术验证和原型开发阶段，部分组件（如VR/AR设备）达到TRL 7-8，但整体系统集成和互操作性仍不成熟 |\n|      | 系统背景   | 该技术是否属于一个系统级技术的一部分？ | 是 | 元宇宙是融合VR/AR、区块链、AI、网络通信、物联网等多种技术的复杂系统 |\n|      |            | 系统级技术的成熟度如何？ | 低至中等 | 整体系统仍处于概念验证和早期开发阶段，缺乏大规模集成应用 |\n|      |            | 如果是系统级技术，其组件技术成熟度如何？ | 不均衡 | VR/AR硬件较成熟（TRL 7-8），但沉浸式交互、去中心化经济、跨平台互操作等关键组件仍不成熟（TRL 4-6） |\n|      |            | 在此技术成熟度下制定标准可能带来哪些风险？ | 高风险 | 可能过早固化技术路线，抑制创新；标准可能因技术快速迭代而迅速过时 |\n|      | 相邻及竞争技术 | 是否存在可能取代或与该技术共存的其他技术？ | 是 | Web3.0、数字孪生、扩展现实（XR）等技术与元宇宙存在部分重叠和竞争关系 |\n|      |                | 该技术是否依赖其他技术、组件或系统？其成熟度如何？ | 高度依赖 | 依赖5G/6G（TRL 7-9）、AI（TRL 7-9）、区块链（TRL 6-8）等，但成熟度不一 |\n|      |                | 是否可能制定一个与具体技术无关的标准，以实现竞争技术之间的互操作性？ | 是 | 可制定接口、数据格式、安全等基础性标准，但技术路线未稳定，难度较大 |\n|      | 计量成熟度 | 测什么：是否已就需测量的属性达成共识？ | 否 | 沉浸感、延迟、并发用户数等关键指标尚未形成统一测量标准 |\n|      |            | 测得到：是否存在相关的测量手段 | 部分存在 | 部分性能指标（如渲染帧率）可测量，但用户体验、经济系统稳定性等缺乏可靠测量方法 |\n|      |            | 测差距：能否测量出可区分性能的属性，从而值得纳入标准？ | 部分能 | 硬件性能指标可区分，但整体体验和互操作性等核心价值难以量化比较 |\n|      | 现有标准   | 是否有相关标准？ | 是 | 已有部分VR/AR、网络传输、数据格式等领域标准（如MPEG-I），但缺乏元宇宙特定标准 |\n| 市场 | 产品 | 有多少公司推出了多少产品？其成熟度如何？ | 有限 | Meta、微软、英伟达等推出部分平台和硬件，但产品形态多样，成熟度不一，未形成稳定市场 |\n|      |      | 是否存在竞争技术？ | 是 | 不同企业采用不同技术路线（如中心化vs去中心化），存在显著竞争 |\n|      |      | 标准会促进竞争还是导致淘汰？ | 可能促进竞争但风险高 | 早期标准可能帮助中小企业降低门槛，但也可能被大企业利用形成垄断 |\n|      |      | 消费者信心如何？ | 中等偏低 | 公众对元宇宙概念认知度高，但对隐私、安全、健康等存在顾虑，实际采用率低 |\n|      |      | 消费者采用产品面临哪些风险？ | 高风险 | 隐私泄露、网络安全、经济欺诈、健康影响等风险尚未有效解决 |\n|      | 供应链 | 是否存在供应链？ | 部分存在 | VR/AR硬件供应链较成熟，但元宇宙专属内容、服务供应链仍在形成中 |\n|      | 用例   | 是否存在用例？   | 是 | 游戏、虚拟会议、数字展厅等早期用例存在，但规模和应用深度有限 |\n|      | 市场认知度 | 是否有市场认知度？ | 高 | 概念认知度高，但实际理解和接受程度参差不齐 |\n|      | 政策考量   | 是否存在相关政策？ | 是 | 中国及多国出台元宇宙支持政策，但监管框架（如数据、虚拟资产）尚未完善 |\n|      | 基础设施   | 是否有相关基础设施？ | 部分具备 | 5G、云计算等基础较好，但边缘计算、低延迟网络等仍需升级 |\n|      | 知识产权（IP） | 是否有相关知识产权？ | 是 | 各大企业积极布局专利，但存在碎片化和潜在专利冲突风险 |\n\n### 综合评估结论\n- 建议：适合研究课题\n- 理由：元宇宙技术整体处于TRL 4-6的技术验证阶段，关键组件成熟度不均衡，系统集成和互操作性未解决，测量方法和市场应用尚未成熟。当前更适合通过研究课题（如技术报告、白皮书）探索方向，而非制定强制性国家标准。可优先开展基础接口、安全、伦理等指导性标准研究，待技术路线和市场进一步成熟后再考虑正式标准制定。";

    @Override
    public String getModelName() {
        return "local-deepseek";
    }

    @Override
    public String evaluate(StandardEvaluationRequest request) {
        if (useMockData) {
            log.info("使用本地DeepSeek模拟数据");
            return MOCK_RESPONSE;
        }
        
        try {
            String prompt = buildPrompt(request.getTitle());
            return callLocalOllamaApi(prompt);
        } catch (Exception e) {
            log.error("本地Ollama API调用失败", e);
            throw new RuntimeException("本地Ollama API调用失败: " + e.getMessage());
        }
    }

    @Override
    public boolean supports(String modelName) {
        return "local-deepseek".equalsIgnoreCase(modelName);
    }
    
    private String buildPrompt(String title) throws Exception {
        String promptTemplate = loadPromptTemplate();
        return promptTemplate.replace("{TITLE}", title);
    }
    
    private String loadPromptTemplate() throws Exception {
        String filePath = promptFile.replace("classpath:", "");
        return Files.readString(Paths.get(getClass().getClassLoader().getResource(filePath).toURI()));
    }
    
    private String callLocalOllamaApi(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("prompt", prompt);
        requestBody.put("stream", false);
        requestBody.put("options", Map.of(
            "temperature", temperature,
            "num_predict", maxTokens
        ));
        
        // Ollama API不需要认证头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(apiUrl, entity, Map.class);
        
        if (response != null && response.containsKey("response")) {
            return (String) response.get("response");
        }
        
        throw new RuntimeException("本地Ollama API返回格式异常");
    }
}
