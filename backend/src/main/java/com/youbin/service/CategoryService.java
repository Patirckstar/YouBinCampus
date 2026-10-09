package com.youbin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.youbin.entity.Category;
import com.youbin.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;

    public List<Category> listAll() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSort).orderByAsc(Category::getId));
    }

    public Category getById(Long id) {
        return id == null ? null : categoryMapper.selectById(id);
    }

    /**
     * 将分类名（可能来自大模型）匹配为分类 ID，匹配不到返回 null
     */
    public Long matchCategoryId(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        for (Category c : listAll()) {
            if (c.getName().equals(name) || name.contains(c.getName()) || c.getName().contains(name)) {
                return c.getId();
            }
        }
        return null;
    }
}