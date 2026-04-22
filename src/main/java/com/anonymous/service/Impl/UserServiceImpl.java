package com.anonymous.service.Impl;

import com.anonymous.mapper.UserMapper;
import com.anonymous.mapper.ReservationMapper;
import com.anonymous.model.User;
import com.anonymous.service.UserService;
import com.anonymous.vo.UserReputationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ReservationMapper reservationMapper;

    @Override
    public User login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        throw new RuntimeException("用户名或密码错误。");
    }

    @Override
    public User findById(Long id) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user;
    }

    @Override
    public boolean updateProfile(Long id, String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("姓名不能为空");
        }

        String trimmedName = name.trim();
        if (trimmedName.length() > 20) {
            throw new RuntimeException("姓名长度不能超过20个字符");
        }

        int rows = userMapper.updateNameById(id, trimmedName);
        return rows > 0;
    }

    @Override
    public boolean changePassword(Long id, String oldPassword, String newPassword) {
        if (oldPassword == null || oldPassword.isEmpty()) {
            throw new RuntimeException("旧密码不能为空");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            throw new RuntimeException("新密码不能为空");
        }
        if (newPassword.length() < 6) {
            throw new RuntimeException("新密码长度不能小于6位");
        }
        if (oldPassword.equals(newPassword)) {
            throw new RuntimeException("新旧密码不能相同");
        }

        User user = findById(id);
        if (!oldPassword.equals(user.getPassword())) {
            throw new RuntimeException("旧密码输入错误");
        }

        int rows = userMapper.updatePasswordById(id, newPassword);
        return rows > 0;
    }

    @Override
    public UserReputationVO getUserReputation(Long id) {
        int completedCount = reservationMapper.countByUserIdAndStatus(id, 2);
        int userCancelledCount = reservationMapper.countByUserIdAndStatus(id, 3);
        int adminCancelledCount = reservationMapper.countByUserIdAndStatus(id, 5);
        int expiredCount = reservationMapper.countByUserIdAndStatus(id, 4);
        int violatedCount = reservationMapper.countByUserIdAndStatus(id, 6);

        int cancelledCount = userCancelledCount + adminCancelledCount;
        int totalViolatedCount = expiredCount + violatedCount;

        int score = 100 + completedCount - cancelledCount * 2 - totalViolatedCount * 10;
        if (score < 0) {
            score = 0;
        }
        if (score > 120) {
            score = 120;
        }

        String level;
        if (score >= 105) {
            level = "优秀";
        } else if (score >= 90) {
            level = "良好";
        } else if (score >= 75) {
            level = "一般";
        } else {
            level = "较低";
        }

        return new UserReputationVO(
                score,
                level,
                completedCount,
                cancelledCount,
                totalViolatedCount
        );
    }
}
