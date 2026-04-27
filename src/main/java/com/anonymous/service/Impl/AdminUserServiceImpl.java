package com.anonymous.service.Impl;

import com.anonymous.common.Page;
import com.anonymous.dto.admin.reservation.ReservationQueryDTO;
import com.anonymous.dto.admin.user.UserQueryDTO;
import com.anonymous.dto.admin.user.UserUpdateDTO;
import com.anonymous.mapper.ReservationMapper;
import com.anonymous.mapper.UserMapper;
import com.anonymous.service.AdminUserService;
import com.anonymous.service.ReputationService;
import com.anonymous.service.UserService;
import com.anonymous.vo.ReputationAdjustDTO;
import com.anonymous.vo.UserReputationVO;
import com.anonymous.vo.admin.ReservationAdminVO;
import com.anonymous.vo.admin.UserAdminVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminUserServiceImpl implements AdminUserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private ReputationService reputationService;

    @Autowired
    private UserService userService;

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
    public Boolean disableUser(UserUpdateDTO query) {
        return (userMapper.updateUser(query) > 0);
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
}
