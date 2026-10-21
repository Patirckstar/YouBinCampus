package com.youbin.controller;

import com.youbin.common.Result;
import com.youbin.dto.LoginDTO;
import com.youbin.dto.UserUpdateDTO;
import com.youbin.service.GoodsService;
import com.youbin.service.UserService;
import com.youbin.util.UserContext;
import com.youbin.vo.GoodsVO;
import com.youbin.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final GoodsService goodsService;

    @PostMapping("/login")
    public Result<UserVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.ok(userService.login(dto));
    }

    @GetMapping("/info")
    public Result<UserVO> info() {
        return Result.ok(userService.info(UserContext.requireUserId()));
    }

    @PutMapping("/info")
    public Result<UserVO> updateInfo(@RequestBody UserUpdateDTO dto) {
        return Result.ok(userService.updateInfo(UserContext.requireUserId(), dto));
    }

    @GetMapping("/my-goods")
    public Result<List<GoodsVO>> myGoods() {
        return Result.ok(goodsService.myGoods(UserContext.requireUserId()));
    }
}