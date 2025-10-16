package com.pig4cloud.pig.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 标准文档下载记录表
 *
 * @author pig
 * @date 2025-10-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("standard_document")
public class StandardDocument extends Model<StandardDocument> {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 标准PK
     */
    @TableField("pk")
    private String pk;

    /**
     * 标准号
     */
    @TableField("standard_code")
    private String standardCode;

    /**
     * 文件路径
     */
    @TableField("file_path")
    private String filePath;

    /**
     * 文件大小(字节)
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * 下载状态：PENDING,SUCCESS,FAILED
     */
    @TableField("download_status")
    private String downloadStatus;

    /**
     * 识别的验证码
     */
    @TableField("captcha_text")
    private String captchaText;

    /**
     * 下载token
     */
    @TableField("download_token")
    private String downloadToken;

    /**
     * 重试次数
     */
    @TableField("retry_count")
    private Integer retryCount;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 下载时间
     */
    @TableField("download_time")
    private LocalDateTime downloadTime;

    /**
     * 创建人
     */
    @TableField("create_by")
    private String createBy;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    @TableField("update_by")
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 下载状态枚举
     */
    public enum DownloadStatus {
        PENDING("PENDING", "待下载"),
        SUCCESS("SUCCESS", "下载成功"),
        FAILED("FAILED", "下载失败");

        private final String code;
        private final String desc;

        DownloadStatus(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
}
