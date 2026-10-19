package com.youbin.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class GoodsVO {

    private Long id;

    private Long userId;

    private Long categoryId;

    private String categoryName;

    private String title;

    private String description;

    private BigDecimal price;

    private List<String> images;

    private Integer status;

    private Integer viewCount;

    private LocalDateTime createTime;

    private String sellerNickname;

    private String sellerAvatar;

    private String phone;

    /** 想要人数（仅在商品详情 detail() 中填充） */
    private Integer wantCount;
}