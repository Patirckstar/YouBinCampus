package com.youbin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("goods")
public class Goods {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long categoryId;

    private String title;

    private String description;

    private BigDecimal price;

    /** 图片 URL 数组的 JSON 字符串，如 ["/upload/a.jpg","/upload/b.jpg"] */
    private String images;

    /** 状态：0下架 1在售 2已售 */
    private Integer status;

    private Integer viewCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}