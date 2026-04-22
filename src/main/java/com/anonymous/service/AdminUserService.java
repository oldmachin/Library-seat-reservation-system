package com.anonymous.service;

import com.anonymous.common.Page;
import com.anonymous.dto.admin.reservation.ReservationQueryDTO;
import com.anonymous.dto.admin.user.UserQueryDTO;
import com.anonymous.dto.admin.user.UserUpdateDTO;
import com.anonymous.vo.admin.ReservationAdminVO;
import com.anonymous.vo.admin.UserAdminVO;

public interface AdminUserService {
    Page<UserAdminVO> listUsers(UserQueryDTO queryDTO);

    UserAdminVO findUser(Long id);

    Boolean disableUser(UserUpdateDTO query);

    Page<ReservationAdminVO> findUserReservation(ReservationQueryDTO queryDTO);
}
