package com.youbin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AgentSearchDTO {

    @NotBlank(message = "query 不能为空")
    private String query;

    private int pageNum = 1;

    private int pageSize = 10;
}