package com.anonymous.controller.admin;

import com.anonymous.common.Result;
import com.anonymous.dto.SeatStatusUpdateDTO;
import com.anonymous.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/seat")
public class AdminSeatController {

    @Autowired
    private SeatService seatService;

    @PutMapping("/{id}/status")
    public Result<Boolean> updateSeatStatus(@PathVariable Long id,
                                            @RequestBody SeatStatusUpdateDTO request) {
        try {
            return Result.success(
                    seatService.updateSeatStatusByAdmin(id, request.status(), request.maintenanceNote()),
                    "更新座位状态成功"
            );
        } catch (RuntimeException e) {
            return Result.fail(false, e.getMessage());
        }
    }
}
