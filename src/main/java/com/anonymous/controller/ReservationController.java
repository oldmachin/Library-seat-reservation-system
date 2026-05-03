package com.anonymous.controller;

import com.anonymous.common.Page;
import com.anonymous.common.Result;
import com.anonymous.common.util.ReservationStatusValidator;
import com.anonymous.common.util.ReservationTimeValidator;
import com.anonymous.common.util.SecurityUtils;
import com.anonymous.dto.QuickReservationRequestDTO;
import com.anonymous.dto.ReservationRequestDTO;
import com.anonymous.dto.SeatActionRequestDTO;
import com.anonymous.model.Reservation;
import com.anonymous.service.ReservationService;
import com.anonymous.vo.QuickReservationResultVO;
import com.anonymous.vo.ReservationUserDetailVO;
import com.anonymous.vo.TimeSlotVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
        reservationService.cancelReservation(userId, request.seatId());
        return Result.success(true, "取消成功");
   }

   @PostMapping("/check-in")
   public Result<Boolean> checkInReservation(@RequestBody SeatActionRequestDTO request) {
       reservationService.checkIn(SecurityUtils.getCurrentUserId(), request.seatId());
       return Result.success(true, "签到成功");
   }

   @PostMapping("/check-out")
   public Result<Boolean> checkOutReservation() {
       reservationService.checkOut(SecurityUtils.getCurrentUserId());
       return Result.success(true, "签退成功");
   }

   @PostMapping("/leave-temp")
   public Result<Boolean> leaveTemporary() {
       reservationService.leaveTemp(SecurityUtils.getCurrentUserId());
       return Result.success(true, "暂离成功");
   }

   @PostMapping("/return-temp")
   public Result<Boolean> returnTemporary(@RequestBody SeatActionRequestDTO request) {
       reservationService.returnTemp(SecurityUtils.getCurrentUserId(), request.seatId());
       return Result.success(true, "返回成功");
   }

   @GetMapping("/my-current")
   public Result<Reservation> getCurrentReservation() {
       Long userId = SecurityUtils.getCurrentUserId();
       Reservation reservation = reservationService.getCurrent(userId);
       return Result.success(reservation, reservation == null ? "当前没有预约" : "查询成功");
   }

    @GetMapping("/{id}")
    public Result<ReservationUserDetailVO> getReservationDetail(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        ReservationUserDetailVO detail = reservationService.getDetail(userId, id);
        return Result.success(detail, "查询成功");
    }

   @GetMapping("/history")
   public Result<Page<Reservation>> getHistoryReservation(@RequestParam(defaultValue = "1") int pageNum,
                                                          @RequestParam(defaultValue = "10") int pageSize) {
       Long userId = SecurityUtils.getCurrentUserId();
       Page<Reservation> page = reservationService.getHistory(userId, pageNum, pageSize);
       return Result.success(page, "查询成功");
   }

   @GetMapping("/time-slots")
    public Result<List<TimeSlotVO>> getTimeSlots() {
       return Result.success(ReservationTimeValidator.getTimeSlotVOs(), "查询成功");
   }

   @PostMapping("/quick-book")
    public Result<QuickReservationResultVO> quickBook(@RequestBody QuickReservationRequestDTO request) {
       Long userId = SecurityUtils.getCurrentUserId();
       LocalDateTime startTime = LocalDateTime.parse(request.startTime(), FORMATTER); LocalDateTime endTime = LocalDateTime.parse(request.endTime(), FORMATTER);
       QuickReservationResultVO result = reservationService.quickBook(userId, startTime, endTime);
       return Result.success(result, "快捷选座成功");
   }
}
