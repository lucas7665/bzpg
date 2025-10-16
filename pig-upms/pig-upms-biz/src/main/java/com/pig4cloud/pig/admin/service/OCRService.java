package com.pig4cloud.pig.admin.service;

import cn.hutool.http.HttpUtil;
import com.mmg.ddddocr4j.utils.DDDDOcrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * OCR验证码识别服务
 *
 * @author pig
 * @date 2025-10-15
 */
@Slf4j
@Service
public class OCRService {

    /**
     * 识别验证码图片（从URL）
     * @param imageUrl 图片URL
     * @return 识别结果
     */
    public String recognizeCaptcha(String imageUrl) {
        try {
            log.debug("开始识别验证码，URL: {}", imageUrl);
            byte[] imageBytes = HttpUtil.downloadBytes(imageUrl);
            return recognizeCaptcha(imageBytes);
        } catch (Exception e) {
            log.error("从URL识别验证码失败: {}", imageUrl, e);
            return null;
        }
    }

    /**
     * 识别验证码图片（从字节数组）
     * @param imageBytes 图片字节数组
     * @return 识别结果
     */
    public String recognizeCaptcha(byte[] imageBytes) {
        try {
            log.debug("开始识别验证码，图片大小: {} bytes", imageBytes.length);
            
            // 调用DDDDOcrUtil识别验证码
            String result = DDDDOcrUtil.getCode(imageBytes);
            
            if (result != null && !result.trim().isEmpty()) {
                log.debug("验证码识别成功: {}", result);
                return result.trim();
            } else {
                log.warn("验证码识别结果为空");
                return null;
            }
        } catch (Exception e) {
            log.error("OCR识别验证码失败", e);
            return null;
        }
    }

    /**
     * 识别验证码图片（从文件路径）
     * @param imagePath 图片文件路径
     * @return 识别结果
     */
    public String recognizeCaptchaFromFile(String imagePath) {
        try {
            log.debug("开始识别验证码，文件路径: {}", imagePath);
            byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
            return recognizeCaptcha(imageBytes);
        } catch (IOException e) {
            log.error("读取图片文件失败: {}", imagePath, e);
            return null;
        }
    }

    /**
     * 保存图片到临时文件并识别
     * @param imageBytes 图片字节数组
     * @param prefix 文件名前缀
     * @return 识别结果
     */
    public String recognizeCaptchaWithTempFile(byte[] imageBytes, String prefix) {
        Path tempFile = null;
        try {
            // 创建临时文件
            tempFile = Files.createTempFile(prefix + "_", ".png");
            Files.write(tempFile, imageBytes);
            
            log.debug("临时文件创建成功: {}", tempFile.toString());
            
            // 识别验证码
            String result = recognizeCaptcha(tempFile.toString());
            
            return result;
        } catch (Exception e) {
            log.error("使用临时文件识别验证码失败", e);
            return null;
        } finally {
            // 清理临时文件
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    log.warn("删除临时文件失败: {}", tempFile, e);
                }
            }
        }
    }
}
