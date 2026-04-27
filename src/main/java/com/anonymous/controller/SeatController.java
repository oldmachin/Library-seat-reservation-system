package com.anonymous.controller;

import com.anonymous.common.Result;
import com.anonymous.model.Seat;
import com.anonymous.service.SeatService;
import com.anonymous.vo.SeatAvailabilityVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.DateFormatter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/v1/seat")
public class SeatController {

    private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private SeatService seatService;

    @GetMapping("/room/{roomId}")
    Result<List<Seat>> getSeatsByRoom(@PathVariable Long roomId) {
        List<Seat> seats = seatService.getSeatByRoom(roomId);
        return Result.success(seats, "查询成功");
    }

    @PostMapping("/{seatId}/defective")
    Result<Boolean> markDefective(@PathVariable Long seatId, @RequestBody String reason) {
        try {
            seatService.markDefective(seatId, reason);
            return Result.success(true, "座位已经成功标记为损坏");
        } catch (RuntimeException e) {
            return Result.fail(false, e.getMessage());
        }
    }

    @GetMapping("/room/{roomId}/availability")
    Result<List<SeatAvailabilityVO>> getSeatAvailabilityByRoom(
            @PathVariable Long roomId,
            @RequestParam String startTime,
        @RequestParam String endTime) {
    LocalDateTime start = LocalDateTime.parse(startTime, FORMATTER);
    LocalDateTime end = LocalDateTime.parse(endTime, FORMATTER);
    return Result.success(seatService.getSeatAvailabilityByRoom(roomId, start, end), "查询成功");
}

}
