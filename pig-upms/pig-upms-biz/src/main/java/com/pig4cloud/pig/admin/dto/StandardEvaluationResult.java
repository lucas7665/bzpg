package com.pig4cloud.pig.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 标准评估结果DTO
 *
 * @author pig
 * @date 2025/01/15
 */
@Data
@Schema(description = "标准评估结果")
public class StandardEvaluationResult {

    @Schema(description = "评估表格（Markdown格式）")
    private String resultTable;

    @Schema(description = "综合评估结论")
    private String result;

    @Schema(description = "评估状态：SUCCESS-成功，FAILED-失败")
    private String status;

    @Schema(description = "错误信息")
    private String errorMessage;

}
