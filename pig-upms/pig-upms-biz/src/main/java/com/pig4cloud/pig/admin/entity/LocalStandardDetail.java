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
 * 地方标准详情表
 *
 * @author pig
 * @date 2025-10-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("local_standard_detail")
public class LocalStandardDetail extends Model<LocalStandardDetail> {

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
    @TableField("code")
    private String code;

    /**
     * 标准名称
     */
    @TableField("ch_name")
    private String chName;

    /**
     * 城市代码，如bjzjj
     */
    @TableField("city_code")
    private String cityCode;

    /**
     * 所属城市
     */
    @TableField("city")
    private String city;

    /**
     * 负责部门
     */
    @TableField("charge_dept")
    private String chargeDept;

    /**
     * 标准状态
     */
    @TableField("status")
    private String status;

    /**
     * 批准日期（时间戳）
     */
    @TableField("issue_date")
    private Long issueDate;

    /**
     * 实施日期（时间戳）
     */
    @TableField("act_date")
    private Long actDate;

    /**
     * 备案日期（时间戳）
     */
    @TableField("record_date")
    private Long recordDate;

    /**
     * 备案号
     */
    @TableField("record_no")
    private String recordNo;

    /**
     * 修订标准号
     */
    @TableField("revise_std_codes")
    private String reviseStdCodes;

    /**
     * 是否为空
     */
    @TableField("`empty`")
    private Boolean empty;

    /**
     * 其他结果列（JSON格式）
     */
    @TableField("other_result_columns")
    private String otherResultColumns;

    /**
     * 废止日期（时间戳）
     */
    @TableField("fz_date")
    private Long fzDate;

    /**
     * 关联的城市分类ID
     */
    @TableField("city_category_id")
    private Long cityCategoryId;

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
