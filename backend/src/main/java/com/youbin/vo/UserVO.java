package com.youbin.vo;

import lombok.Data;

@Data
public class UserVO {

    private Long id;

    private String nickname;

    private String avatar;

    private String phone;

    /** 仅登录时返回 */
    private String token;
}