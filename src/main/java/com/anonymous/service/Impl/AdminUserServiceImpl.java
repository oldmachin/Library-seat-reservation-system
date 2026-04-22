package com.anonymous.service.Impl;

import com.anonymous.common.Page;
import com.anonymous.dto.admin.reservation.ReservationQueryDTO;
import com.anonymous.dto.admin.user.UserQueryDTO;
import com.anonymous.dto.admin.user.UserUpdateDTO;
import com.anonymous.mapper.ReservationMapper;
import com.anonymous.mapper.UserMapper;
import com.anonymous.service.AdminUserService;
import com.anonymous.vo.admin.ReservationAdminVO;
import com.anonymous.vo.admin.UserAdminVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminUserServiceImpl implements AdminUserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ReservationMapper reservationMapper;

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
}
