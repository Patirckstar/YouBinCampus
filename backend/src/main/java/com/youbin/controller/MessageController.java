package com.youbin.controller;

import com.youbin.common.Result;
import com.youbin.dto.SendMessageDTO;
import com.youbin.service.MessageService;
import com.youbin.util.UserContext;
import com.youbin.vo.ConversationVO;
import com.youbin.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/conversations")
    public Result<List<ConversationVO>> conversations() {
        return Result.ok(messageService.conversations(UserContext.requireUserId()));
    }

    @GetMapping("/with/{goodsId}/{peerId}")
    public Result<List<MessageVO>> with(@PathVariable Long goodsId, @PathVariable Long peerId) {
        return Result.ok(messageService.with(UserContext.requireUserId(), goodsId, peerId));
    }

    @PostMapping("/send")
    public Result<MessageVO> send(@RequestBody SendMessageDTO dto) {
        return Result.ok(messageService.send(UserContext.requireUserId(), dto));
    }

    @GetMapping("/unread")
    public Result<Integer> unread() {
        return Result.ok(messageService.unreadCount(UserContext.requireUserId()));
    }
}