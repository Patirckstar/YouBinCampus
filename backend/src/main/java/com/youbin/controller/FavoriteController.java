package com.youbin.controller;

import com.youbin.common.Result;
import com.youbin.service.FavoriteService;
import com.youbin.service.GoodsService;
import com.youbin.util.UserContext;
import com.youbin.vo.GoodsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/favorite")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final GoodsService goodsService;

    @PostMapping("/{goodsId}")
    public Result<Void> add(@PathVariable Long goodsId) {
        favoriteService.add(UserContext.requireUserId(), goodsId);
        return Result.ok();
    }

    @DeleteMapping("/{goodsId}")
    public Result<Void> remove(@PathVariable Long goodsId) {
        favoriteService.remove(UserContext.requireUserId(), goodsId);
        return Result.ok();
    }

    @GetMapping("/list")
    public Result<List<GoodsVO>> list() {
        return Result.ok(goodsService.favoriteGoods(UserContext.requireUserId()));
    }

    @GetMapping("/check/{goodsId}")
    public Result<Boolean> check(@PathVariable Long goodsId) {
        Long uid = UserContext.getUserId();
        boolean favorited = uid != null && favoriteService.isFavorite(uid, goodsId);
        return Result.ok(favorited);
    }
}