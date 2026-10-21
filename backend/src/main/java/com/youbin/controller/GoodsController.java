package com.youbin.controller;

import com.youbin.common.PageResult;
import com.youbin.common.Result;
import com.youbin.dto.GoodsDTO;
import com.youbin.service.ContactService;
import com.youbin.service.GoodsService;
import com.youbin.util.UserContext;
import com.youbin.vo.ContactVO;
import com.youbin.vo.GoodsVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/goods")
@RequiredArgsConstructor
public class GoodsController {

    private final GoodsService goodsService;
    private final ContactService contactService;

    @GetMapping("/page")
    public Result<PageResult<GoodsVO>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal priceMin,
            @RequestParam(required = false) BigDecimal priceMax,
            @RequestParam(required = false) String sort) {
        return Result.ok(goodsService.page(pageNum, pageSize, categoryId, keyword, priceMin, priceMax, sort));
    }

    @GetMapping("/{id}")
    public Result<GoodsVO> detail(@PathVariable Long id) {
        return Result.ok(goodsService.detail(id));
    }

    @GetMapping("/{id}/contact")
    public Result<ContactVO> contact(@PathVariable Long id) {
        return Result.ok(contactService.contactInfo(UserContext.requireUserId(), id));
    }

    @PostMapping
    public Result<GoodsVO> publish(@Valid @RequestBody GoodsDTO dto) {
        return Result.ok(goodsService.publish(UserContext.requireUserId(), dto));
    }

    @PutMapping("/{id}")
    public Result<GoodsVO> update(@PathVariable Long id, @Valid @RequestBody GoodsDTO dto) {
        return Result.ok(goodsService.update(UserContext.requireUserId(), id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        goodsService.delete(UserContext.requireUserId(), id);
        return Result.ok();
    }

    @PutMapping("/{id}/sold")
    public Result<Void> markSold(@PathVariable Long id, @RequestParam(required = false) Long buyerId) {
        goodsService.markSold(UserContext.requireUserId(), id, buyerId);
        return Result.ok();
    }

    @PutMapping("/{id}/offline")
    public Result<Void> offline(@PathVariable Long id) {
        goodsService.offline(UserContext.requireUserId(), id);
        return Result.ok();
    }

    @PutMapping("/{id}/online")
    public Result<Void> online(@PathVariable Long id) {
        goodsService.online(UserContext.requireUserId(), id);
        return Result.ok();
    }
}