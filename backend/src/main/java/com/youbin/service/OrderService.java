package com.youbin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.youbin.entity.Order;
import com.youbin.mapper.OrderMapper;
import com.youbin.vo.GoodsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    /**
     * 复用 GoodsService 已有的 toVO 拼装能力（见 GoodsService#listVOByIds）。
     * GoodsService 仅依赖 OrderMapper（非 OrderService），故此处不构成循环依赖。
     */
    private final GoodsService goodsService;

    /** 我买到的商品（按成交时间倒序，即 id 倒序） */
    public List<GoodsVO> myOrders(Long userId) {
        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getBuyerId, userId)
                .orderByDesc(Order::getId));
        if (orders.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> goodsIds = orders.stream().map(Order::getGoodsId).collect(Collectors.toList());
        return goodsService.listVOByIds(goodsIds);
    }
}