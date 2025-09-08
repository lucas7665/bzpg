package com.pig4cloud.pig.assess;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeepSeekService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${deepseek.apiKey:${DEEPSEEK_API_KEY:}}")
    private String apiKey;

    @Value("${deepseek.model:${DEEPSEEK_MODEL:deepseek-chat}}")
    private String model;

    @Value("${deepseek.baseUrl:${DEEPSEEK_BASE_URL:https://api.deepseek.com}}")
    private String baseUrl;

    private RestTemplate restTemplate = new RestTemplate();

    public AssessController.AssessResponse assessStandard(AssessController.AssessRequest req) {
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("DeepSeek API key is not configured");
        }

        String prompt = buildPrompt(req.getContent(), req.getLanguage());

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("temperature", 0.3);
        body.put("messages", List.of(
                Map.of("role", "system", "content", "你是一个标准评估助手，请输出结构化结论(JSON)，字段包含: topic, summary, score(0-100), recommendations(数组)。"),
                Map.of("role", "user", "content", prompt)
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(body, headers);
        String url = baseUrl + "/v1/chat/completions";

        try {
            ResponseEntity<ChatCompletionResponse> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, ChatCompletionResponse.class);
            ChatCompletionResponse data = response.getBody();
            String content = (data != null && data.firstContent() != null) ? data.firstContent() : null;
            return mapToAssessResponse(req.getContent(), content);
        } catch (Exception e) {
            log.error("DeepSeek request failed", e);
            AssessController.AssessResponse fallback = new AssessController.AssessResponse();
            fallback.setTopic(req.getContent());
            fallback.setSummary("评估服务暂时不可用，请稍后重试。");
            fallback.setScore(0);
            fallback.setRecommendations(new String[]{"请稍后重试", "检查网络或配置"});
            return fallback;
        }
    }

    private String buildPrompt(String content, String language) {
        String lang = (language == null || language.isBlank()) ? "zh-cn" : language;
        return ("语言:" + lang + "\n主题:" + content + "\n请输出 JSON，包含: topic, summary, score(0-100), recommendations[]，不要包含多余文本。");
    }

    private AssessController.AssessResponse mapToAssessResponse(String topic, String llmText) {
        AssessController.AssessResponse res = new AssessController.AssessResponse();
        res.setTopic(topic);
        if (!StringUtils.hasText(llmText)) {
            res.setSummary("无返回内容");
            res.setScore(0);
            res.setRecommendations(new String[]{"请重试"});
            return res;
        }
        try {
            Map<?, ?> json = objectMapper.readValue(llmText, Map.class);
            Object summary = json.containsKey("summary") ? json.get("summary") : "";
            res.setSummary(String.valueOf(summary));
            Object score = json.get("score");
            if (score instanceof Number) {
                res.setScore(((Number) score).intValue());
            } else {
                res.setScore(0);
            }
            Object rec = json.get("recommendations");
            if (rec instanceof List) {
                List<?> list = (List<?>) rec;
                res.setRecommendations(list.stream().map(String::valueOf).toArray(String[]::new));
            } else {
                res.setRecommendations(new String[]{});
            }
        } catch (Exception ex) {
            // 如果模型未严格返回 JSON，则作为纯文本处理
            res.setSummary(llmText);
            res.setScore(0);
            res.setRecommendations(new String[]{});
        }
        return res;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChatCompletionResponse {
        public List<Choice> choices;
        public String firstContent() {
            if (choices == null || choices.isEmpty()) return null;
            Choice c = choices.get(0);
            if (c == null || c.message == null) return null;
            return c.message.content;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        public Message message;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        public String role;
        public String content;
    }
}


