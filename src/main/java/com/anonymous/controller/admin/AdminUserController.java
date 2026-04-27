package com.anonymous.controller.admin;

import com.anonymous.common.Page;
import com.anonymous.common.Result;
import com.anonymous.common.util.SecurityUtils;
import com.anonymous.dto.admin.reservation.ReservationQueryDTO;
import com.anonymous.dto.admin.user.UserQueryDTO;
import com.anonymous.dto.admin.user.UserUpdateDTO;
import com.anonymous.service.AdminUserService;
import com.anonymous.vo.ReputationAdjustDTO;
import com.anonymous.vo.UserReputationVO;
import com.anonymous.vo.admin.ReservationAdminVO;
import com.anonymous.vo.admin.UserAdminVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/user")
public class AdminUserController {
    @Autowired
    private AdminUserService adminUserService;

    @GetMapping
    public Result<Page<UserAdminVO>> findAll(UserQueryDTO queryDTO) {
        return Result.success(adminUserService.listUsers(queryDTO), "查询用户列表成功");
    }

    @GetMapping("/{id}")
    public Result<UserAdminVO> findUser(@PathVariable Long id) {
        return Result.success(adminUserService.findUser(id), "查询用户成功");
    }

    @PutMapping("/{id}/status")
    public Result<Boolean> disableUser(@PathVariable Long id) {
        UserUpdateDTO dto = new UserUpdateDTO(
                id,
                null,
                1
        );
        return Result.success(adminUserService.disableUser(dto), "禁用用户成功");
    }

    @GetMapping("/{id}/reservations")
    public Result<Page<ReservationAdminVO>> getUserReservations(@PathVariable Long id) {
        ReservationQueryDTO dto = new ReservationQueryDTO(
                1,
                10,
                null,
                id,
                null,
                null,
                null,
                null,
                null,
                null

        );
        return Result.success(adminUserService.findUserReservation(dto), "查询用户预约成功");
    }

    @PostMapping("/{id}/reputation-adjust")
    public Result<UserReputationVO> adjustUserReputation(@PathVariable Long id,
                                                         @RequestBody ReputationAdjustDTO request) {
        Long operatorId = SecurityUtils.getCurrentUserId();
        UserReputationVO data = adminUserService.adjustUserReputation(id, request, operatorId);
        return Result.success(data, "调整用户信誉分成功");
    }

    @GetMapping("/{id}/reputation")
    public Result<UserReputationVO> getUserReputation(@PathVariable Long id) {
        return Result.success(adminUserService.getUserReputation(id), "查询用户信誉分成功");
    }
}
