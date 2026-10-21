package com.youbin.dto;

import lombok.Data;

/**
 * 编辑个人信息请求体，字段均可选，但至少一项非空
 */
@Data
public class UserUpdateDTO {

    private String nickname;

    private String avatar;

    private String phone;
}