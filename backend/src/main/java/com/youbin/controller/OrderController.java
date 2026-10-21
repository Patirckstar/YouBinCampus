package com.youbin.controller;

import com.youbin.common.Result;
import com.youbin.service.OrderService;
import com.youbin.util.UserContext;
import com.youbin.vo.GoodsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/mine")
    public Result<List<GoodsVO>> mine() {
        return Result.ok(orderService.myOrders(UserContext.requireUserId()));
    }
}