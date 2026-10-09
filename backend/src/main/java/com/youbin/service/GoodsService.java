package com.youbin.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youbin.common.BizException;
import com.youbin.common.PageResult;
import com.youbin.common.ResultCode;
import com.youbin.dto.GoodsDTO;
import com.youbin.entity.Category;
import com.youbin.entity.Favorite;
import com.youbin.entity.Goods;
import com.youbin.entity.User;
import com.youbin.mapper.CategoryMapper;
import com.youbin.mapper.FavoriteMapper;
import com.youbin.mapper.GoodsMapper;
import com.youbin.mapper.UserMapper;
import com.youbin.vo.GoodsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoodsService {

    private final GoodsMapper goodsMapper;
    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;
    private final FavoriteMapper favoriteMapper;

    public GoodsVO publish(Long userId, GoodsDTO dto) {
        Goods g = new Goods();
        g.setUserId(userId);
        g.setCategoryId(dto.getCategoryId());
        g.setTitle(dto.getTitle());
        g.setDescription(dto.getDescription());
        g.setPrice(dto.getPrice());
        g.setImages(dto.getImages() == null ? null : JSONUtil.toJsonStr(dto.getImages()));
        g.setStatus(1);
        g.setViewCount(0);
        goodsMapper.insert(g);
        return detail(g.getId());
    }

    public GoodsVO update(Long userId, Long id, GoodsDTO dto) {
        Goods g = requireOwn(userId, id);
        g.setCategoryId(dto.getCategoryId());
        g.setTitle(dto.getTitle());
        g.setDescription(dto.getDescription());
        g.setPrice(dto.getPrice());
        g.setImages(dto.getImages() == null ? null : JSONUtil.toJsonStr(dto.getImages()));
        goodsMapper.updateById(g);
        return detail(id);
    }

    public void delete(Long userId, Long id) {
        requireOwn(userId, id);
        goodsMapper.deleteById(id);
    }

    public void markSold(Long userId, Long id) {
        Goods g = requireOwn(userId, id);
        g.setStatus(2);
        goodsMapper.updateById(g);
    }

    public PageResult<GoodsVO> page(int pageNum, int pageSize, Long categoryId, String keyword,
                                    BigDecimal priceMin, BigDecimal priceMax, String sort) {
        LambdaQueryWrapper<Goods> qw = new LambdaQueryWrapper<>();
        qw.eq(Goods::getStatus, 1);
        if (categoryId != null) {
            qw.eq(Goods::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w.like(Goods::getTitle, keyword).or().like(Goods::getDescription, keyword));
        }
        if (priceMin != null) {
            qw.ge(Goods::getPrice, priceMin);
        }
        if (priceMax != null) {
            qw.le(Goods::getPrice, priceMax);
        }
        if ("price_asc".equals(sort)) {
            qw.orderByAsc(Goods::getPrice);
        } else if ("price_desc".equals(sort)) {
            qw.orderByDesc(Goods::getPrice);
        } else {
            qw.orderByDesc(Goods::getId);
        }

        Page<Goods> p = goodsMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        return PageResult.of(toVOList(p.getRecords()), p.getTotal(), p.getCurrent(), p.getSize());
    }

    public GoodsVO detail(Long id) {
        Goods g = goodsMapper.selectById(id);
        if (g == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        Goods update = new Goods();
        update.setId(id);
        update.setViewCount((g.getViewCount() == null ? 0 : g.getViewCount()) + 1);
        goodsMapper.updateById(update);
        g.setViewCount(update.getViewCount());

        Category c = categoryMapper.selectById(g.getCategoryId());
        User seller = userMapper.selectById(g.getUserId());
        return toVO(g, c == null ? null : c.getName(), seller);
    }

    public List<GoodsVO> myGoods(Long userId) {
        List<Goods> list = goodsMapper.selectList(
                new LambdaQueryWrapper<Goods>().eq(Goods::getUserId, userId).orderByDesc(Goods::getId));
        return toVOList(list);
    }

    public List<GoodsVO> favoriteGoods(Long userId) {
        List<Favorite> fs = favoriteMapper.selectList(
                new LambdaQueryWrapper<Favorite>().eq(Favorite::getUserId, userId).orderByDesc(Favorite::getId));
        if (fs.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> goodsIds = fs.stream().map(Favorite::getGoodsId).collect(Collectors.toList());
        return toVOList(goodsMapper.selectBatchIds(goodsIds));
    }

    private Goods requireOwn(Long userId, Long id) {
        Goods g = goodsMapper.selectById(id);
        if (g == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        if (!g.getUserId().equals(userId)) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        return g;
    }

    private List<GoodsVO> toVOList(List<Goods> list) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, String> catNames = categoryMapper.selectBatchIds(
                        list.stream().map(Goods::getCategoryId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(Category::getId, Category::getName));
        Map<Long, User> users = userMapper.selectBatchIds(
                        list.stream().map(Goods::getUserId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(User::getId, u -> u));
        return list.stream()
                .map(g -> toVO(g, catNames.get(g.getCategoryId()), users.get(g.getUserId())))
                .collect(Collectors.toList());
    }

    private GoodsVO toVO(Goods g, String categoryName, User seller) {
        GoodsVO vo = new GoodsVO();
        vo.setId(g.getId());
        vo.setUserId(g.getUserId());
        vo.setCategoryId(g.getCategoryId());
        vo.setCategoryName(categoryName);
        vo.setTitle(g.getTitle());
        vo.setDescription(g.getDescription());
        vo.setPrice(g.getPrice());
        vo.setImages(parseImages(g.getImages()));
        vo.setStatus(g.getStatus());
        vo.setViewCount(g.getViewCount());
        vo.setCreateTime(g.getCreateTime());
        if (seller != null) {
            vo.setSellerNickname(seller.getNickname());
            vo.setSellerAvatar(seller.getAvatar());
            vo.setPhone(seller.getPhone());
        }
        return vo;
    }

    private List<String> parseImages(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            List<String> list = JSONUtil.toList(json, String.class);
            return list == null ? new ArrayList<>() : list;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}