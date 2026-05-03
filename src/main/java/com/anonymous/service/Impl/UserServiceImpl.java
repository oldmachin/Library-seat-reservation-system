package com.anonymous.service.Impl;

import com.anonymous.common.Page;
import com.anonymous.common.exception.BusinessException;
import com.anonymous.common.exception.InvalidOperationStatusException;
import com.anonymous.common.exception.InvalidParameterException;
import com.anonymous.mapper.ReputationRecordMapper;
import com.anonymous.mapper.UserMapper;
import com.anonymous.mapper.ReservationMapper;
import com.anonymous.model.User;
import com.anonymous.service.ReputationService;
import com.anonymous.service.UserService;
import com.anonymous.vo.ReputationRecordVO;
import com.anonymous.vo.UserReputationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private ReputationRecordMapper reputationRecordMapper;

    @Autowired
    private ReputationService reputationService;

    private boolean isBcryptPassword(String password) {
        return password != null
                && (password.startsWith("$2a$")
                || password.startsWith("$2b$")
                || password.startsWith("$2y$"));
    }

    private boolean passwordMatches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }

        if (isBcryptPassword(storedPassword)) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }

        return  rawPassword.equals(storedPassword);
    }

    private void upgradePasswordIfNecessary(User user, String rawPassword) {
        if (!isBcryptPassword(user.getPassword())) {
            String encodedPassword = passwordEncoder.encode(rawPassword);
            userMapper.updatePasswordById(user.getId(), encodedPassword);
        }
    }

    @Override
    public User login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new InvalidParameterException("user.username");
        }
        if (password == null || password.isEmpty()) {
            throw new InvalidParameterException("user.password");
        }

        User user = userMapper.findByUsername(username.trim());
        if (user == null || !passwordMatches(password, user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误。");
        }
        if (user.getStatus() == null || user.getStatus() != 0) {
            throw new BusinessException(403, "账号已被禁用，请联系管理员");
        }
        upgradePasswordIfNecessary(user, password);
        user.setPassword(null);
        return user;
    }

    @Override
    public User findById(Long id) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new InvalidParameterException("user.id");
        }
        return user;
    }

    @Override
    public boolean updateProfile(Long id, String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidParameterException("user.name");
        }

        String trimmedName = name.trim();
        if (trimmedName.length() > 20) {
            throw new InvalidParameterException("user.name");
        }

        int rows = userMapper.updateNameById(id, trimmedName);
        if (rows == 0) {
            throw new InvalidOperationStatusException("个人资料更新失败");
        }
        return rows > 0;
    }

    @Override
    public boolean changePassword(Long id, String oldPassword, String newPassword) {
        if (oldPassword == null || oldPassword.isEmpty()) {
            throw new InvalidParameterException("user.oldPassword");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            throw new InvalidParameterException("user.newPassword");
        }
        if (newPassword.length() < 6) {
            throw new InvalidParameterException("user.newPassword");
        }
        if (oldPassword.equals(newPassword)) {
            throw new InvalidParameterException("user.newPassword");
        }

        User user = findById(id);
        if (!passwordMatches(oldPassword, user.getPassword())) {
            throw new BusinessException(401, "旧密码输入错误");
        }
        String encodedPassword = passwordEncoder.encode(newPassword);
        int rows = userMapper.updatePasswordById(id, encodedPassword);
        if (rows == 0) {
            throw new InvalidOperationStatusException("密码修改失败");
        }
        return true;
    }

    @Override
    public UserReputationVO getUserReputation(Long id) {
        User user = reputationService.refreshBlacklistIfNeeded(id);

        int completedCount = reservationMapper.countByUserIdAndStatus(id, 2);
        int userCancelledCount = reservationMapper.countByUserIdAndStatus(id, 3);
        int adminCancelledCount = reservationMapper.countByUserIdAndStatus(id, 5);
        int expiredCount = reservationMapper.countByUserIdAndStatus(id, 4);
        int violatedCount = reservationMapper.countByUserIdAndStatus(id, 6);

        int cancelledCount = userCancelledCount + adminCancelledCount;
        int totalViolatedCount = expiredCount + violatedCount;

        int score = user.getReputationScore() == null ? 100 : user.getReputationScore();

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

        String accessMode;
        if (user.getBlacklistUntil() != null && user.getBlacklistUntil().isAfter(java.time.LocalDateTime.now())) {
            accessMode = "BLOCKED";
        } else if (score >= 80) {
            accessMode = "NORMAL";
        } else if (score >= 60) {
            accessMode = "QUICK_ONLY";
        } else {
            accessMode = "BLOCKED";
        }

        return new UserReputationVO(
                score,
                level,
                completedCount,
                cancelledCount,
                totalViolatedCount,
                user.getBlacklistUntil(),
                accessMode
        );
    }

    @Override
    public Page<ReputationRecordVO> getReputationRecords(Long userId, int pageNum, int pageSize) {
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }

        int offset = (pageNum - 1) * pageSize;
        long total = reputationRecordMapper.countByUserId(userId);

        List<ReputationRecordVO> records = reputationRecordMapper.findPageByUserId(userId, pageSize, offset)
                .stream()
                .map(record -> new ReputationRecordVO(
                        record.getId(),
                        record.getReservationId(),
                        record.getEventType(),
                        record.getScoreDelta(),
                        record.getScoreAfter(),
                        record.getReason(),
                        record.getCreateTime()
                ))
                .toList();

        return new Page<>(records, total, pageNum, pageSize);
    }
}
