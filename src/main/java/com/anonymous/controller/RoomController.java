package com.anonymous.controller;

import com.anonymous.common.Result;
import com.anonymous.model.Room;
import com.anonymous.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/room")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @GetMapping("/occupancy-rate/{roomId}")
    public double getRoomOccupancyRate(@PathVariable Long roomId) {
        return roomService.getOccupancyRate(roomId);
    }

    @GetMapping
    public Result<List<Room>> getRooms(@RequestParam(required = false) List<Integer> statuses) {
        List<Room> rooms = roomService.getRoomsByCondition(statuses);
        return Result.success(rooms, "查询成功");
    }
}
