package com.youbin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDTO {

    /** 演示阶段：code 即稳定身份标识（openid） */
    @NotBlank(message = "code 不能为空")
    private String code;

    private String nickname;

    private String avatar;
}