package com.anonymous.service.Impl;

import com.anonymous.common.Page;
import com.anonymous.dto.UserCreateDTO;
import com.anonymous.dto.admin.reservation.ReservationQueryDTO;
import com.anonymous.dto.admin.user.UserQueryDTO;
import com.anonymous.dto.admin.user.UserUpdateDTO;
import com.anonymous.mapper.ReservationMapper;
import com.anonymous.mapper.UserMapper;
import com.anonymous.model.User;
import com.anonymous.service.AdminUserService;
import com.anonymous.service.ReputationService;
import com.anonymous.service.UserService;
import com.anonymous.dto.ReputationAdjustDTO;
import com.anonymous.vo.UserReputationVO;
import com.anonymous.vo.admin.ReservationAdminVO;
import com.anonymous.vo.admin.UserAdminVO;
import com.anonymous.websocket.ReservationWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    private final static String AUTH_USER_PREFIX = "auth:user:";

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private ReputationService reputationService;

    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ReservationWebSocketHandler reservationWebSocketHandler;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Page<UserAdminVO> listUsers(UserQueryDTO queryDTO) {
        Integer pageNum = queryDTO.page();
        Integer pageSize = queryDTO.size();

        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        if (pageSize > 100) {
            pageSize = 100;
        }

        int offset = (pageNum - 1) * pageSize;

        List<UserAdminVO> rawRecords = userMapper.findUsersByCondition(queryDTO, offset, pageSize);

        long total = userMapper.countUsersByCondition(queryDTO);

        return new Page<>(rawRecords, total, pageNum, pageSize);
    }

    @Override
    public UserAdminVO findUser(Long id) {
        return userMapper.findUserById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean disableUser(UserUpdateDTO query) {
        boolean updated = userMapper.updateUser(query) > 0;
        if (updated && query.id() != null && query.status() != null && query.status() == 1) {
            redisTemplate.delete(AUTH_USER_PREFIX + query.id());
            reservationWebSocketHandler.closeUserSessions(query.id(), "账号已被禁用");
        }
        return updated;
    }

    @Override
    public Page<ReservationAdminVO> findUserReservation(ReservationQueryDTO queryDTO) {
        Integer pageNum = queryDTO.page();
        Integer pageSize = queryDTO.size();

        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        if (pageSize > 100) {
            pageSize = 100;
        }

        int offset = (pageNum - 1) * pageSize;
        long total = reservationMapper.countByUserId(queryDTO.userId());

        List<ReservationAdminVO> result = reservationMapper.findReservationsByCondition(queryDTO, offset, pageSize);

        return new Page<>(result, total, pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserReputationVO adjustUserReputation(Long userId, ReputationAdjustDTO request, Long operatorId) {
        if (request.delta() == null || request.delta() == 0) {
            throw new RuntimeException("调整分值不能为空且不能为0");
        }
        if (Math.abs(request.delta()) > 30) {
            throw new RuntimeException("单次调整分值不能超过30");
        }
        if (request.reason() == null || request.reason().trim().isEmpty()) {
            throw new RuntimeException("调整原因不能为空");
        }

        reputationService.onAdminAdjusted(userId, request.delta(), request.reason().trim(), operatorId);
        return userService.getUserReputation(userId);
    }

    @Override
    public UserReputationVO getUserReputation(Long userId) {
        return userService.getUserReputation(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createUser(UserCreateDTO request) {
        if (request == null) {
            throw new RuntimeException("用户信息不能为空");
        }

        String name = request.name() == null ? "" : request.name().trim();
        String username = request.username() == null ? "" : request.username().trim();
        String password = request.password() == null ? "" : request.password().trim();
        String role = request.role() == null ? "USER" : request.role().trim().toUpperCase();

        if (name.isEmpty()) {
            throw new RuntimeException("姓名不能为空");
        }
        if (username.isEmpty()) {
            throw new RuntimeException("账号不能为空");
        }
        if (password.length() < 6) {
            throw new RuntimeException("密码长度不能少于6位");
        }
        if (!"USER".equals(role) && !"ADMIN".equals(role)) {
            throw new RuntimeException("角色只能是 USER 或 ADMIN");
        }
        if (userMapper.findByUsername(username) != null) {
            throw new RuntimeException("账号已存在");
        }

        String encodedPassword = passwordEncoder.encode(password);
        return userMapper.insertAdminUser(name, username, encodedPassword, role) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean enableUser(Long id) {
        if (id == null) {
            throw new RuntimeException("用户ID不能为空");
        }

        User user = userMapper.findById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        UserUpdateDTO dto = new UserUpdateDTO(id, null, 0);
        return userMapper.updateUser(dto) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean resetPassword(Long id, String password) {
        if (id == null) {
            throw new RuntimeException("用户ID不能为空");
        }

        String rawPassword = password == null || password.trim().isEmpty()
                ? "123456"
                : password.trim();

        if (rawPassword.length() < 6) {
            throw new RuntimeException("密码长度不能少于6位");
        }

        User user = userMapper.findById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);
        boolean updated = userMapper.updateUser(new UserUpdateDTO(id, encodedPassword, null)) > 0;

        if (updated) {
            redisTemplate.delete(AUTH_USER_PREFIX + id);
            reservationWebSocketHandler.closeUserSessions(id, "密码已被管理员重置，请重新登录");
        }

        return updated;
    }

}
