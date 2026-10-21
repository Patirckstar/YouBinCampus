package com.youbin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConversationVO {

    private Long peerId;

    private String peerNickname;

    private String peerAvatar;

    private Long goodsId;

    private String goodsTitle;

    private String goodsImage;

    private String lastContent;

    private LocalDateTime lastTime;

    private Integer unreadCount;
}