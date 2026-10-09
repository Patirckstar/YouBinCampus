package com.youbin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.youbin.common.BizException;
import com.youbin.common.ResultCode;
import com.youbin.dto.LoginDTO;
import com.youbin.entity.User;
import com.youbin.mapper.UserMapper;
import com.youbin.util.JwtUtil;
import com.youbin.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public UserVO login(LoginDTO dto) {
        // 演示阶段：code 作为稳定身份标识，未接真实微信开放平台
        String openid = dto.getCode();
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getOpenid, openid));
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setNickname(dto.getNickname() == null || dto.getNickname().isBlank()
                    ? "校园用户" : dto.getNickname());
            user.setAvatar(dto.getAvatar());
            userMapper.insert(user);
        }
        return toVO(user, true);
    }

    public UserVO info(Long userId) {
        User user = getUser(userId);
        return toVO(user, false);
    }

    public User getUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        return user;
    }

    private UserVO toVO(User u, boolean withToken) {
        UserVO vo = new UserVO();
        vo.setId(u.getId());
        vo.setNickname(u.getNickname());
        vo.setAvatar(u.getAvatar());
        vo.setPhone(u.getPhone());
        if (withToken) {
            vo.setToken(jwtUtil.createToken(u.getId()));
        }
        return vo;
    }
}