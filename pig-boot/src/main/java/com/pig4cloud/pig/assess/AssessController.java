package com.pig4cloud.pig.assess;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.NotBlank;

/**
 * 标准评估接口（MVP 简化版）
 */
@RestController
@RequestMapping("/assess")
@RequiredArgsConstructor
public class AssessController {

    private final DeepSeekService deepSeekService;

    @PostMapping(value = "/standard", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AssessResponse assessStandard(@Validated @RequestBody AssessRequest request) {
        return deepSeekService.assessStandard(request);
    }

    @Data
    public static class AssessRequest {
        @NotBlank
        private String content;
        private String language = "zh-cn";
    }

    @Data
    public static class AssessResponse {
        private String topic;
        private String summary;
        private Integer score;
        private String[] recommendations;
    }
}


