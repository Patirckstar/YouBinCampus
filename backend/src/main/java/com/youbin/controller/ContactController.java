package com.youbin.controller;

import com.youbin.common.Result;
import com.youbin.service.ContactService;
import com.youbin.util.UserContext;
import com.youbin.vo.ContactRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping("/{goodsId}")
    public Result<Void> contact(@PathVariable Long goodsId) {
        contactService.recordContact(UserContext.requireUserId(), goodsId);
        return Result.ok();
    }

    @GetMapping("/goods/{goodsId}")
    public Result<List<ContactRecordVO>> records(@PathVariable Long goodsId) {
        return Result.ok(contactService.listRecords(UserContext.requireUserId(), goodsId));
    }
}
