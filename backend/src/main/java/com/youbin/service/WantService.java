package com.youbin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.youbin.common.BizException;
import com.youbin.common.ResultCode;
import com.youbin.entity.Goods;
import com.youbin.entity.User;
import com.youbin.entity.Want;
import com.youbin.mapper.GoodsMapper;
import com.youbin.mapper.UserMapper;
import com.youbin.mapper.WantMapper;
import com.youbin.vo.GoodsVO;
import com.youbin.vo.WantUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WantService {

    private final WantMapper wantMapper;
    private final GoodsMapper goodsMapper;
    private final UserMapper userMapper;
    /**
     * 复用 GoodsService 已有的 toVO 拼装能力（见 GoodsService#listVOByIds）。
     * GoodsService 仅依赖 WantMapper（非 WantService），故此处不构成循环依赖。
     */
    private final GoodsService goodsService;

    /** 标记想要（幂等） */
    public void add(Long userId, Long goodsId) {
        Goods g = goodsMapper.selectById(goodsId);
        if (g == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        if (g.getUserId().equals(userId)) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "不能标记自己的商品");
        }
        if (g.getStatus() == null || g.getStatus() != 1) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "商品已售或已下架，无法标记想要");
        }
        Long count = wantMapper.selectCount(new LambdaQueryWrapper<Want>()
                .eq(Want::getUserId, userId)
                .eq(Want::getGoodsId, goodsId));
        if (count != null && count > 0) {
            return;
        }
        Want w = new Want();
        w.setUserId(userId);
        w.setGoodsId(goodsId);
        try {
            wantMapper.insert(w);
        } catch (DuplicateKeyException e) {
            // 唯一键冲突：并发场景下已被标记，兜底视为幂等成功
        }
    }

    /** 取消标记（不受商品状态限制） */
    public void remove(Long userId, Long goodsId) {
        Goods g = goodsMapper.selectById(goodsId);
        if (g == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        wantMapper.delete(new LambdaQueryWrapper<Want>()
                .eq(Want::getUserId, userId)
                .eq(Want::getGoodsId, goodsId));
    }

    public boolean isWanted(Long userId, Long goodsId) {
        Long count = wantMapper.selectCount(new LambdaQueryWrapper<Want>()
                .eq(Want::getUserId, userId)
                .eq(Want::getGoodsId, goodsId));
        return count != null && count > 0;
    }

    /** 我标记想要过的商品（按标记时间倒序，即 id 倒序） */
    public List<GoodsVO> myWants(Long userId) {
        List<Want> wants = wantMapper.selectList(new LambdaQueryWrapper<Want>()
                .eq(Want::getUserId, userId)
                .orderByDesc(Want::getId));
        if (wants.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> goodsIds = wants.stream().map(Want::getGoodsId).collect(Collectors.toList());
        return goodsService.listVOByIds(goodsIds);
    }

    /** 某商品的想要用户列表（仅商品卖家可调） */
    public List<WantUserVO> listWanters(Long userId, Long goodsId) {
        Goods g = goodsMapper.selectById(goodsId);
        if (g == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        if (!g.getUserId().equals(userId)) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        List<Want> wants = wantMapper.selectList(new LambdaQueryWrapper<Want>()
                .eq(Want::getGoodsId, goodsId)
                .orderByDesc(Want::getId));
        if (wants.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, User> users = userMapper.selectBatchIds(
                        wants.stream().map(Want::getUserId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(User::getId, u -> u));
        return wants.stream().map(w -> {
            WantUserVO vo = new WantUserVO();
            vo.setUserId(w.getUserId());
            User u = users.get(w.getUserId());
            if (u != null) {
                vo.setNickname(u.getNickname());
                vo.setAvatar(u.getAvatar());
            }
            vo.setCreateTime(w.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
    }
}
