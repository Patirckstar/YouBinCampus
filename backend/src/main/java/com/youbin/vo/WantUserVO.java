package com.youbin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WantUserVO {

    private Long userId;

    private String nickname;

    private String avatar;

    private LocalDateTime createTime;
}
