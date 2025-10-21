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
 * 地方标准详细信息表
 *
 * @author pig
 * @date 2025-10-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("local_standard_detail_info")
public class LocalStandardDetailInfo extends Model<LocalStandardDetailInfo> {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 标准唯一标识
     */
    @TableField("pk")
    private String pk;

    /**
     * 标准号
     */
    @TableField("standard_code")
    private String standardCode;

    /**
     * 标准名称
     */
    @TableField("ch_name")
    private String chName;

    /**
     * 发布日期
     */
    @TableField("publish_date")
    private String publishDate;

    /**
     * 实施日期
     */
    @TableField("implement_date")
    private String implementDate;

    /**
     * 废止状态
     */
    @TableField("abolish_status")
    private String abolishStatus;

    /**
     * 制修订类型
     */
    @TableField("revision_type")
    private String revisionType;

    /**
     * 代替标准
     */
    @TableField("replace_standard")
    private String replaceStandard;

    /**
     * 中国标准分类号
     */
    @TableField("china_classification")
    private String chinaClassification;

    /**
     * 国际标准分类号
     */
    @TableField("international_classification")
    private String internationalClassification;

    /**
     * 技术归口
     */
    @TableField("technical_committee")
    private String technicalCommittee;

    /**
     * 批准发布部门
     */
    @TableField("approval_department")
    private String approvalDepartment;

    /**
     * 城市分类
     */
    @TableField("city_classification")
    private String cityClassification;

    /**
     * 标准类别
     */
    @TableField("standard_category")
    private String standardCategory;

    /**
     * 备案号
     */
    @TableField("record_number")
    private String recordNumber;

    /**
     * 备案日期
     */
    @TableField("record_date")
    private String recordDate;

    /**
     * 备案月报
     */
    @TableField("record_bulletin")
    private String recordBulletin;

    /**
     * 适用范围
     */
    @TableField("scope")
    private String scope;

    /**
     * 起草单位
     */
    @TableField("drafting_units")
    private String draftingUnits;

    /**
     * 起草人
     */
    @TableField("drafting_persons")
    private String draftingPersons;

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
     * 备注
     */
    @TableField("remark")
    private String remark;
}
