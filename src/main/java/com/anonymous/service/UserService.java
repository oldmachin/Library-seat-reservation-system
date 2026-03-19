package com.anonymous.service;

import com.anonymous.mapper.UserMapper;
import com.anonymous.model.User;
import org.springframework.beans.factory.annotation.Autowired;

public interface UserService {
    public User login(String username, String password);
}
