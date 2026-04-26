package com.anonymous.controller;

import com.anonymous.common.Page;
import com.anonymous.common.Result;
import com.anonymous.common.util.SecurityUtils;
import com.anonymous.dto.ReservationRequestDTO;
import com.anonymous.dto.SeatActionRequestDTO;
import com.anonymous.model.Reservation;
import com.anonymous.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {

   private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

   @Autowired
   private ReservationService reservationService;

   @PostMapping("/book")
   public Result<Long> bookSeat(@RequestBody ReservationRequestDTO request) {
       Long userId = SecurityUtils.getCurrentUserId();
       Long seatId = request.seatId();
       LocalDateTime startTime = LocalDateTime.parse(request.startTime(), FORMATTER);
       LocalDateTime endTime = LocalDateTime.parse(request.endTime(), FORMATTER);

       Long reservationId = reservationService.bookSeat(userId, seatId, startTime, endTime);
       return Result.success(reservationId, "预约已受理");
   }

   @PostMapping("/cancel")
   public Result<Boolean> cancelReservation(@RequestBody SeatActionRequestDTO request) {
        Long userId = SecurityUtils.getCurrentUserId();
        boolean result = reservationService.cancelReservation(userId, request.seatId());
        if (result) {
            return Result.success(true, "取消成功");
        }
        return Result.fail(false, "取消失败");
   }

   @PostMapping("/check-in")
   public Result<Boolean> checkInReservation(@RequestBody SeatActionRequestDTO request) {
       boolean result = reservationService.checkIn(SecurityUtils.getCurrentUserId(), request.seatId());

       if (result) {
           return Result.success(true, "签到成功");
       }
       return Result.fail(false, "签到失败");
   }

   @PostMapping("/check-out")
   public Result<Boolean> checkOutReservation() {
       boolean result = reservationService.checkOut(SecurityUtils.getCurrentUserId());

       if (result) {
           return Result.success(true, "签退成功");
       }
       return Result.fail(false, "签退失败");
   }

   @PostMapping("/leave-temp")
   public Result<Boolean> leaveTemporary() {
       boolean result = reservationService.leaveTemp(SecurityUtils.getCurrentUserId());
       if (result) {
           return Result.success(true, "暂离成功");
       }
       return Result.fail(false, "暂离失败");
   }

   @PostMapping("/return-temp")
   public Result<Boolean> returnTemporary(@RequestBody SeatActionRequestDTO request) {
       boolean result = reservationService.returnTemp(SecurityUtils.getCurrentUserId(), request.seatId());
       if (result) {
           return Result.success(true, "返回成功");
       }
       return Result.fail(false, "返回失败");
   }

   @GetMapping("/my-current")
   public Result<Reservation> getCurrentReservation() {
        Long userId = SecurityUtils.getCurrentUserId();
       Reservation reservation = reservationService.getCurrent(userId);
       if (reservation != null) {
           return Result.success(reservation, "查询成功");
       }
       return Result.fail(null, "当前没有预约");
   }

   @GetMapping("/history")
   public Result<Page<Reservation>> getHistoryReservation(@RequestParam(defaultValue = "1") int pageNum,
                                                          @RequestParam(defaultValue = "10") int pageSize) {
       Long userId = SecurityUtils.getCurrentUserId();
       Page<Reservation> page = reservationService.getHistory(userId, pageNum, pageSize);
       return Result.success(page, "查询成功");
   }
}
