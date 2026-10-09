package com.youbin.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AgentGoodsResult {

    private String title;

    private String description;

    private String categoryName;

    private Long categoryId;

    private BigDecimal price;
}