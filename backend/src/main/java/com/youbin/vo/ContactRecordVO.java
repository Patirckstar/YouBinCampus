package com.youbin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ContactRecordVO {

    private Long id;

    private Long fromUserId;

    private String fromNickname;

    private String fromAvatar;

    private String content;

    private Integer isRead;

    private LocalDateTime createTime;
}
