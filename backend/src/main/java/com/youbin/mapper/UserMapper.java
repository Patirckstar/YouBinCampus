package com.youbin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youbin.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}