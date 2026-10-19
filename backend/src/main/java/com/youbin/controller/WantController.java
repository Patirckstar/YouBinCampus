package com.youbin.controller;

import com.youbin.common.Result;
import com.youbin.service.WantService;
import com.youbin.util.UserContext;
import com.youbin.vo.GoodsVO;
import com.youbin.vo.WantUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/want")
@RequiredArgsConstructor
public class WantController {

    private final WantService wantService;

    @PostMapping("/{goodsId}")
    public Result<Void> add(@PathVariable Long goodsId) {
        wantService.add(UserContext.requireUserId(), goodsId);
        return Result.ok();
    }

    @DeleteMapping("/{goodsId}")
    public Result<Void> remove(@PathVariable Long goodsId) {
        wantService.remove(UserContext.requireUserId(), goodsId);
        return Result.ok();
    }

    @GetMapping("/check/{goodsId}")
    public Result<Boolean> check(@PathVariable Long goodsId) {
        Long uid = UserContext.getUserId();
        boolean wanted = uid != null && wantService.isWanted(uid, goodsId);
        return Result.ok(wanted);
    }

    @GetMapping("/mine")
    public Result<List<GoodsVO>> mine() {
        return Result.ok(wantService.myWants(UserContext.requireUserId()));
    }

    @GetMapping("/goods/{goodsId}")
    public Result<List<WantUserVO>> wanters(@PathVariable Long goodsId) {
        return Result.ok(wantService.listWanters(UserContext.requireUserId(), goodsId));
    }
}
