package com.anonymous.controller.admin;

import com.anonymous.common.Page;
import com.anonymous.common.Result;
import com.anonymous.dto.ReservationAdminActionQueryDTO;
import com.anonymous.dto.admin.reservation.AdminCancelReservationDTO;
import com.anonymous.dto.admin.reservation.AdminViolationDTO;
import com.anonymous.dto.admin.reservation.ReservationQueryDTO;
import com.anonymous.service.AdminReservationService;
import com.anonymous.vo.ReservationAdminActionLogVO;
import com.anonymous.vo.admin.ReservationAdminVO;
import com.anonymous.vo.admin.ReservationDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/reservations")
public class AdminReservationController {
    @Autowired
    private AdminReservationService adminReservationService;

    @GetMapping
    public Result<Page<ReservationAdminVO>> findAll(ReservationQueryDTO queryDTO) {
        Page<ReservationAdminVO> result = adminReservationService.listReservations(queryDTO);
        return Result.success(result, "查询预约列表成功");
    }

    @GetMapping("/actions")
    public Result<Page<ReservationAdminActionLogVO>> listReservationActions(
            ReservationAdminActionQueryDTO queryDTO
    ) {
        Page<ReservationAdminActionLogVO> result = adminReservationService.listReservationActions(queryDTO);
        return Result.success(result, "查询管理员操作日志成功");
    }

    @GetMapping("/{id}")
    public ReservationDetailVO getReservation(@PathVariable Long id) {
        return adminReservationService.getReservation(id);
    }

    @PostMapping("/{id}/cancel")
    public Result<Boolean> cancelReservation(@PathVariable Long id,
                                             @RequestBody(required = false)AdminCancelReservationDTO request) {
        try {
            String reason = request == null ? null : request.reason();
            return Result.success(
                    adminReservationService.cancelReservation(id, reason),
                    "取消预约成功"
            );
        } catch (RuntimeException e) {
            return Result.fail(false, e.getMessage());
        }
    }

    @PostMapping("/{id}/complete")
    public Result<Boolean> completeReservation(@PathVariable Long id) {
        try {
            return Result.success(
                    adminReservationService.completeReservation(id),
                    "标记完成成功"
            );
        } catch (RuntimeException e) {
            return Result.fail(false, e.getMessage());
        }
    }

    @PostMapping("/{id}/violation")
    public Result<Boolean> violationReservation(@PathVariable Long id,
                                                @RequestBody(required = false) AdminViolationDTO request) {
        try {
            String reason = request == null ? null : request.reason();
            Integer penaltyLevel = request == null ? null : request.penaltyLevel();
            Integer banDays = request == null ? null : request.banDays();

            return Result.success(
                    adminReservationService.violationReservation(id, reason, penaltyLevel, banDays),
                    "标记违约成功"
            );
        } catch (RuntimeException e) {
            return Result.fail(false, e.getMessage());
        }
    }

    @GetMapping("/current")
    public Result<Page<ReservationAdminVO>> findCurrent(ReservationQueryDTO queryDTO) {
        Page<ReservationAdminVO> result =  adminReservationService.findCurrent(queryDTO);
        return Result.success(result, "查询预约列表成功");
    }
}
