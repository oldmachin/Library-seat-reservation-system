package com.anonymous.controller.admin;

import com.anonymous.common.Page;
import com.anonymous.common.Result;
import com.anonymous.dto.admin.reservation.ReservationQueryDTO;
import com.anonymous.service.AdminReservationService;
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

    @GetMapping("/{id}")
    public ReservationDetailVO getReservation(@PathVariable Long id) {
        return adminReservationService.getReservation(id);
    }

    @PostMapping("/{id}/cancel")
    public Result<Boolean> cancelReservation(@PathVariable Long id) {
        return Result.success(adminReservationService.cancelReservation(id), "修改预约成功");
    }

    @PostMapping("/{id}/complete")
    public void completeReservation(@PathVariable Long id) {
        adminReservationService.completeReservation(id);
    }

    @PostMapping("/{id}/violation")
    public void violationReservation(@PathVariable Long id) {
        adminReservationService.violationReservation(id);
    }

    @GetMapping("/current")
    public Result<Page<ReservationAdminVO>> findCurrent(ReservationQueryDTO queryDTO) {
        Page<ReservationAdminVO> result =  adminReservationService.findCurrent(queryDTO);
        return Result.success(result, "查询预约列表成功");
    }
}
