package com.ink.common.service;

import com.ink.common.model.User;

public interface UserService {

    /**
     * 获取用户
     * @param user 用户对象
     * @return 获取到的用户对象
     */
    User getUser(User user);

}
