package com.anonymous.service.Impl;

import com.anonymous.mapper.UserMapper;
import com.anonymous.model.User;
import com.anonymous.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public User login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        throw new RuntimeException("用户名或密码错误。");
    }
}
