package com.anonymous.service;

import com.anonymous.mapper.UserMapper;
import com.anonymous.model.User;
import com.anonymous.vo.UserReputationVO;
import org.springframework.beans.factory.annotation.Autowired;

public interface UserService {
    public User login(String username, String password);

    public User findById(Long id);

    public boolean updateProfile(Long id, String name);

    public boolean changePassword(Long id, String oldPassword, String newPassword);

    public UserReputationVO getUserReputation(Long id);
}
