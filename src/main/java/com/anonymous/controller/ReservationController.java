package com.anonymous.controller;

import com.anonymous.common.Page;
import com.anonymous.common.Result;
import com.anonymous.planned.dto.ReservationRequestDTO;
import com.anonymous.planned.dto.SeatActionReqtDTO;
import com.anonymous.model.Reservation;
import com.anonymous.service.ReservationService;
import com.anonymous.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("api/v1/reservations")
public class ReservationController {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private SeatService seatService;

    @PostMapping("/book")
    public Result<Long> bookSeat(@RequestBody ReservationRequestDTO request) {
        Long userId = request.userId();
        Long seatId = request.seatId();
        LocalDateTime startTime = LocalDateTime.parse(request.startTime(), FORMATTER);
        LocalDateTime endTime = LocalDateTime.parse(request.endTime(), FORMATTER);

        Long reservationId = reservationService.bookSeat(userId, seatId, startTime, endTime);
        return Result.success(reservationId, "预约已受理");
    }

    @PostMapping("/cancel")
    public Result<Boolean> cancelReservation(@RequestBody SeatActionReqtDTO request) {
        boolean success = reservationService.cancelReservation(request.userId(), request.seatId());
        if (success) {
            return Result.success(true, "取消成功");
        }
        return Result.fail(false, "取消失败");
    }

    @PostMapping("/check-in")
    public Result<Boolean> checkInReservation(@RequestBody SeatActionReqtDTO request) {
        boolean success = reservationService.checkIn(request.userId(), request.seatId());

        if (success) {
            return Result.success(true, "签到成功");
        }
        return Result.fail(false, "签到失败");
    }

    @PostMapping("/check-out")
    public Result<Boolean> checkOutReservation(@RequestBody SeatActionReqtDTO request) {
        boolean success = reservationService.checkOut(request.userId());
        if (success) {
            return Result.success(true, "签退成功");
        }
        return Result.fail(false, "签退失败");
    }

    @PostMapping("/leave-temp")
    public Result<Boolean> retainReservation(@RequestBody SeatActionReqtDTO request) {
        boolean success = reservationService.leaveTemp(request.userId());
        if (success) {
            return Result.success(true, "暂离成功");
        }
        return Result.fail(false, "暂离失败");
    }

    @PostMapping("/return-temp")
    public Result<Boolean> recoverReservation(@RequestBody SeatActionReqtDTO request) {
        boolean success = reservationService.returnTemp(request.userId(), request.seatId());
        if (success) {
            return Result.success(true, "返回成功");
        }
        return Result.fail(false, "返回失败");
    }

    @GetMapping("/my-current/{userId}")
    public Result<Reservation> getCurrentReservation(@PathVariable Long userId) {
        Reservation reservation = reservationService.getCurrent(userId);
        if (reservation != null) {
            return Result.success(reservation, "查询成功");
        }
        return Result.fail(null, "当前没有预约");
    }
    
    @GetMapping("/history")
    public Result<Page<Reservation>> getHistoryReservation(@RequestParam Long userId,
                                                           @RequestParam(defaultValue = "1") int pageNum,
                                                           @RequestParam(defaultValue = "10") int pageSize) {
        Page<Reservation> page = reservationService.getHistory(userId, pageNum, pageSize);
        return Result.success(page, "查询成功");
    }
}
