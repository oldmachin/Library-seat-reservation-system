package com.anonymous.controller;

import com.anonymous.common.Page;
import com.anonymous.common.Result;
import com.anonymous.common.util.SecurityUtils;
import com.anonymous.common.util.JwtUtil;
import com.anonymous.dto.ChangePasswordDTO;
import com.anonymous.dto.UserProfileUpdateDTO;
import com.anonymous.model.ReputationRecord;
import com.anonymous.model.User;
import com.anonymous.service.ReputationService;
import com.anonymous.service.UserService;
import com.anonymous.vo.ReputationRecordVO;
import com.anonymous.vo.UserReputationVO;
import com.anonymous.websocket.ReservationWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private static final String AUTH_USER_PREFIX = "auth:user:";

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ReputationService reputationService;

    @Autowired
    private ReservationWebSocketHandler reservationWebSocketHandler;

    private Map<String, Object> buildUserInfo(User user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("name", user.getName());
        userInfo.put("username", user.getUsername());
        userInfo.put("role", user.getRole());
        userInfo.put("status", user.getStatus());
        userInfo.put("reputationScore", user.getReputationScore());
        userInfo.put("blacklistUntil", user.getBlacklistUntil());
        return userInfo;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody User loginUser) {
        User user = userService.login(loginUser.getUsername(), loginUser.getPassword());

        String token = jwtUtil.createToken(user.getId(), user.getUsername());

        redisTemplate.opsForValue().set(AUTH_USER_PREFIX + user.getId(), token, 1, TimeUnit.HOURS);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", buildUserInfo(user));

        return Result.success(data, "欢迎回来：" + user.getName());
    }

    @PostMapping("/logout")
    public Result<Boolean> logout() {
        Long userId = SecurityUtils.getCurrentUserId();
        redisTemplate.delete(AUTH_USER_PREFIX + userId);
        return Result.success(true, "退出登录成功");
    }

    @GetMapping("/me")
    public Result<Map<String, Object>> getCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userService.findById(userId);

        return Result.success(buildUserInfo(user), "查询成功");
    }

    @PutMapping("/me")
    public Result<Boolean> updateProfile(@RequestBody UserProfileUpdateDTO request) {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.updateProfile(userId, request.name());
        return Result.success(true, "个人资料更新成功");
    }

    @PostMapping("/change-password")
    public Result<Boolean> changePassword(@RequestBody ChangePasswordDTO request) {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.changePassword(userId, request.oldPassword(), request.newPassword());
        redisTemplate.delete(AUTH_USER_PREFIX + userId);
        reservationWebSocketHandler.closeUserSessions(userId, "密码已修改，请重新登录");
        return Result.success(true, "密码修改成功");
    }

    @GetMapping("/reputation")
    public Result<UserReputationVO> getReputation() {
        Long userId = SecurityUtils.getCurrentUserId();
        UserReputationVO data = userService.getUserReputation(userId);
        return Result.success(data, "查询成功");
    }

    @GetMapping("/reputation-records")
    public Result<Page<ReputationRecordVO>> getReputationRecords(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        Page<ReputationRecordVO> data = userService.getReputationRecords(userId, pageNum, pageSize);
        return Result.success(data, "查询成功");
    }
}
