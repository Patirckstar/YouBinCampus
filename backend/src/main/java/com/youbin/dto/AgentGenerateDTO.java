package com.youbin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AgentGenerateDTO {

    @NotBlank(message = "text 不能为空")
    private String text;
}