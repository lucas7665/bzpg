package com.pig4cloud.pig.admin.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.pig4cloud.pig.admin.entity.LocalStandardDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 地方标准文档下载服务
 *
 * @author pig
 * @date 2025-10-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocalStandardDocumentDownloadService {

    private static final String VALIDATE_CODE_URL = "https://dbba.sacinfo.org.cn/portal/validate-code";
    private static final String VALIDATE_CAPTCHA_URL = "https://dbba.sacinfo.org.cn/portal/validate-captcha/down";
    private static final String DOWNLOAD_URL = "https://dbba.sacinfo.org.cn/portal/download/";

    private final OCRService ocrService;

    @Value("${local-standard.download.dir:/tmp/local-standards/}")
    private String downloadDir;

    /**
     * 下载单个地方标准文档
     */
    public boolean downloadDocument(LocalStandardDetail detail) {
        String pk = detail.getPk();
        log.info("开始下载地方标准文档，PK: {}, 标准号: {}", pk, detail.getChName());

        try {
            // 1. 获取验证码图片
            String imageUrl = VALIDATE_CODE_URL + "?pk=" + pk + "&t=" + System.currentTimeMillis();
            log.debug("获取验证码图片: {}", imageUrl);
            
            byte[] imageBytes = HttpUtil.downloadBytes(imageUrl);
            if (imageBytes == null || imageBytes.length == 0) {
                log.error("获取验证码图片失败");
                return false;
            }

            // 2. OCR识别验证码
            String captchaCode = ocrService.recognizeCaptcha(imageBytes);
            if (StrUtil.isBlank(captchaCode)) {
                log.error("验证码识别失败");
                return false;
            }
            log.debug("地方标准 {} 识别验证码: {}", pk, captchaCode);

            // 3. 验证码校验
            Map<String, Object> params = new HashMap<>();
            params.put("captcha", captchaCode);
            params.put("pk", pk);

            String response = HttpUtil.post(VALIDATE_CAPTCHA_URL, params);
            log.debug("验证码校验响应: {}", response);

            JSONObject jsonResponse = JSONUtil.parseObj(response);
            Integer code = jsonResponse.getInt("code");

            if (code != null && code == 0) {
                // 4. 获取下载token
                String downloadToken = jsonResponse.getStr("msg");
                if (StrUtil.isBlank(downloadToken)) {
                    log.error("获取下载token失败");
                    return false;
                }
                log.debug("地方标准 {} 获取下载token: {}", pk, downloadToken);

                // 5. 下载PDF文件
                String fileName = generateFileName(detail.getCode());
                String filePath = downloadDir + fileName;
                
                // 确保下载目录存在
                File dir = new File(downloadDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                String downloadUrl = DOWNLOAD_URL + downloadToken;
                log.debug("开始下载PDF文件: {}", downloadUrl);
                
                HttpUtil.downloadFile(downloadUrl, filePath);
                
                // 检查文件是否下载成功
                File downloadedFile = new File(filePath);
                if (!downloadedFile.exists() || downloadedFile.length() == 0) {
                    log.error("PDF文件下载失败或文件为空");
                    return false;
                }

                log.info("地方标准 {} 下载成功: {}", pk, filePath);
                return true;
            } else {
                String errorMsg = jsonResponse.getStr("msg");
                log.warn("地方标准 {} 验证码校验失败: {}", pk, errorMsg);
                return false;
            }
        } catch (Exception e) {
            log.error("地方标准 {} 下载失败", pk, e);
            return false;
        }
    }

    /**
     * 带重试的下载方法
     */
    public boolean downloadDocumentWithRetry(LocalStandardDetail detail, int maxRetries) {
        String pk = detail.getPk();
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.info("地方标准 {} 第 {} 次下载尝试", pk, attempt);
                
                if (downloadDocument(detail)) {
                    return true;
                }
                
                if (attempt < maxRetries) {
                    // 延迟重试
                    int delaySeconds = attempt * 30; // 递增延迟：30s, 60s, 90s
                    log.info("地方标准 {} 下载失败，{} 秒后重试", pk, delaySeconds);
                    Thread.sleep(delaySeconds * 1000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("下载任务被中断");
                break;
            } catch (Exception e) {
                log.error("地方标准 {} 第 {} 次下载异常", pk, attempt, e);
            }
        }
        
        log.error("地方标准 {} 下载失败，已达到最大重试次数 {}", pk, maxRetries);
        return false;
    }

    /**
     * 生成文件名
     */
    private String generateFileName(String standardCode) {
        if (StrUtil.isBlank(standardCode)) {
            return "unknown_" + System.currentTimeMillis() + ".pdf";
        }
        // 清理文件名中的特殊字符
        String cleanCode = standardCode.replaceAll("[\\\\/:*?\"<>|]", "_");
        return cleanCode + ".pdf";
    }
}
