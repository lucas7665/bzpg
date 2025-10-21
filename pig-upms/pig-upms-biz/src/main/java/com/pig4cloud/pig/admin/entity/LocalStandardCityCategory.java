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
 * 地方标准城市分类表
 *
 * @author pig
 * @date 2025-10-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("local_standard_city_category")
public class LocalStandardCityCategory extends Model<LocalStandardCityCategory> {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 城市代码，如bjzjj
     */
    @TableField("city_code")
    private String cityCode;

    /**
     * 城市名称，如北京市
     */
    @TableField("city_name")
    private String cityName;

    /**
     * 该城市下的标准数量
     */
    @TableField("standard_count")
    private Integer standardCount;

    /**
     * 完整标题，如北京市(2,374)
     */
    @TableField("title")
    private String title;

    /**
     * data-trade属性值
     */
    @TableField("data_trade")
    private String dataTrade;

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
