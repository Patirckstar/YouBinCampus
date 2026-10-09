package com.youbin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.youbin.entity.Favorite;
import com.youbin.mapper.FavoriteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteMapper favoriteMapper;

    public void add(Long userId, Long goodsId) {
        Long count = favoriteMapper.selectCount(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .eq(Favorite::getGoodsId, goodsId));
        if (count != null && count > 0) {
            return;
        }
        Favorite f = new Favorite();
        f.setUserId(userId);
        f.setGoodsId(goodsId);
        favoriteMapper.insert(f);
    }

    public void remove(Long userId, Long goodsId) {
        favoriteMapper.delete(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getGoodsId, goodsId));
    }

    public boolean isFavorite(Long userId, Long goodsId) {
        Long count = favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getGoodsId, goodsId));
        return count != null && count > 0;
    }
}