package com.youbin.util;

import com.youbin.common.BizException;
import com.youbin.common.ResultCode;

/**
 * 当前登录用户上下文（ThreadLocal）
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static Long requireUserId() {
        Long id = USER_ID.get();
        if (id == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        return id;
    }

    public static void clear() {
        USER_ID.remove();
    }
}