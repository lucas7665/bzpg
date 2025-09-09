package com.pig4cloud.pig.admin.controller;

import com.pig4cloud.pig.admin.dto.StandardEvaluationRequest;
import com.pig4cloud.pig.admin.dto.StandardEvaluationResult;
import com.pig4cloud.pig.admin.service.StandardEvaluationService;
import com.pig4cloud.pig.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;

/**
 * 标准评估控制器
 *
 * @author pig
 * @date 2025/01/15
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/evaluation")
@Tag(description = "evaluation", name = "标准评估模块")
public class StandardEvaluationController {

    private final StandardEvaluationService standardEvaluationService;

    /**
     * 标准评估页面
     *
     * @return 评估页面
     */
    @PermitAll
    @GetMapping(value = "", produces = "text/html;charset=UTF-8")
    public String evaluationPage() {
        log.info("访问标准评估页面");
        // 返回静态HTML页面，让Spring Boot自动处理静态资源
        return "evaluation/index";
    }
    
    /**
     * 测试接口
     */
    @PermitAll
    @GetMapping("/test")
    @ResponseBody
    public String test() {
        return "测试成功";
    }

    /**
     * 标准评估
     *
     * @param request 评估请求
     * @return 评估结果
     */
    @PermitAll
    @Operation(description = "标准评估", summary = "标准评估")
    @PostMapping("/assess")
    @ResponseBody
    public R<StandardEvaluationResult> assessStandard(@Valid @RequestBody StandardEvaluationRequest request) {
        log.info("开始评估标准：{}", request.getTitle());
        
        StandardEvaluationResult result = standardEvaluationService.evaluateStandard(request);
        
        if ("SUCCESS".equals(result.getStatus())) {
            log.info("标准评估完成：{}", request.getTitle());
            return R.ok(result);
        } else {
            log.error("标准评估失败：{}，错误信息：{}", request.getTitle(), result.getErrorMessage());
            return R.failed(result.getErrorMessage());
        }
    }

}
