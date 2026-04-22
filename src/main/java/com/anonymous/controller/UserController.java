package com.anonymous.controller;

import com.anonymous.common.Result;
import com.anonymous.common.util.SecurityUtils;
import com.anonymous.common.util.JwtUtil;
import com.anonymous.dto.ChangePasswordDTO;
import com.anonymous.dto.UserProfileUpdateDTO;
import com.anonymous.model.User;
import com.anonymous.service.UserService;
import com.anonymous.vo.UserReputationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody User loginUser) {
        User user = userService.login(loginUser.getUsername(), loginUser.getPassword());

        String token = jwtUtil.createToken(user.getId(), user.getUsername());

//        redisTemplate.opsForValue().set("auth:user:" + user.getId(), token, 1, TimeUnit.HOURS);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);

        return Result.success(data, "欢迎回来：" + user.getName());
    }

    @GetMapping("/me")
    public Result<Map<String, Object>> getCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userService.findById(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("name", user.getName());
        data.put("username", user.getUsername());
        data.put("role", user.getRole());
        data.put("status", user.getStatus());
        return Result.success(data, "查询成功");
    }

    @PutMapping("/me")
    public Result<Boolean> updateProfile(@RequestBody UserProfileUpdateDTO request) {
        Long userId = SecurityUtils.getCurrentUserId();
        boolean updated = userService.updateProfile(userId, request.name());
        if (updated) {
            return Result.success(true, "个人资料更新成功");
        }
        return Result.fail(false, "个人资料更新失败");
    }

    @PostMapping("/change-password")
    public Result<Boolean> changePassword(@RequestBody ChangePasswordDTO request) {
        Long userId = SecurityUtils.getCurrentUserId();
        boolean changed = userService.changePassword(userId, request.oldPassword(), request.newPassword());
        if (changed) {
            return Result.success(true, "密码修改成功");
        }
        return Result.fail(false, "密码修改失败");
    }

    @GetMapping("/reputation")
    public Result<UserReputationVO> getReputation() {
        Long userId = SecurityUtils.getCurrentUserId();
        UserReputationVO data = userService.getUserReputation(userId);
        return Result.success(data, "查询成功");
    }
}
