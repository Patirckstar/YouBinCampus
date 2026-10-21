package com.youbin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageVO {

    private Long id;

    private Long fromUserId;

    private Long toUserId;

    private String content;

    private Integer isRead;

    private LocalDateTime createTime;
}