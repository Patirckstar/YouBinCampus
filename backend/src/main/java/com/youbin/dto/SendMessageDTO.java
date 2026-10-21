package com.youbin.dto;

import lombok.Data;

@Data
public class SendMessageDTO {

    private Long goodsId;

    private Long toUser;

    private String content;
}