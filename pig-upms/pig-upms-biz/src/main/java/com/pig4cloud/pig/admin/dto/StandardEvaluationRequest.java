package com.pig4cloud.pig.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 标准评估请求DTO
 *
 * @author pig
 * @date 2025/01/15
 */
@Data
@Schema(description = "标准评估请求")
public class StandardEvaluationRequest {

    @NotBlank(message = "标准名称不能为空")
    @Schema(description = "标准中文名称", example = "元宇宙技术要求")
    private String title;

}
