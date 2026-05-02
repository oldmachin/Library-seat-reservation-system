package com.anonymous.controller.admin;

import com.anonymous.common.Page;
import com.anonymous.common.Result;
import com.anonymous.dto.RoomStatusUpdateDTO;
import com.anonymous.dto.admin.room.RoomCreateDTO;
import com.anonymous.dto.admin.room.RoomQueryDTO;
import com.anonymous.model.Room;
import com.anonymous.model.Seat;
import com.anonymous.service.AdminRoomService;
import com.anonymous.service.SeatService;
import com.anonymous.vo.admin.RoomAdminVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/room")
public class AdminRoomController {
    @Autowired
    private AdminRoomService adminRoomService;

    @Autowired
    private SeatService seatService;

    @GetMapping
    public Result<Page<RoomAdminVO>> findAllRooms(RoomQueryDTO query) {
        return Result.success(adminRoomService.findRoomCondition(query), "查询房间列表成功");
    }

    @GetMapping("/{id}")
    public Result<RoomAdminVO> findRoom(@PathVariable Long id) {
        return Result.success(adminRoomService.findRoomById(id), "查询房间成功");
    }

    @PostMapping
    public Result<Boolean> addRoom(@RequestBody RoomCreateDTO request) {
        try {
            Boolean result = adminRoomService.addRoom(request);
            return Result.success(result, "新增房间成功");
        } catch(Exception e) {
            return Result.fail(false, "房间新增失败");
        }
    }

    @PutMapping("/{id}")
    public Result<Boolean> updateRoom(@PathVariable Long id, @RequestBody Room room) {
        room.setId(id);
        Boolean result = adminRoomService.updateRoom(room);
        if (result) {
            return Result.success(true, "更新房间成功");
        }
        return Result.fail(false, "没有可更新的字段");
    }

    @PutMapping("/{id}/status")
    public Result<Boolean> updateRoomStatus(@PathVariable Long id,
                                            @RequestBody RoomStatusUpdateDTO request) {
        try {
            return Result.success(
                    adminRoomService.updateRoomStatus(id, request.status()),
                    "更新房间状态成功"
            );
        } catch (RuntimeException e) {
            return Result.fail(false, e.getMessage());
        }
    }

    @GetMapping("/{id}/seats")
    public Result<List<Seat>> getRoomSeats(@PathVariable Long id) {
        try {
            adminRoomService.findRoomById(id);
            return Result.success(seatService.getSeatByRoom(id), "查询房间座位成功");
        } catch (RuntimeException e) {
            return Result.fail(null, e.getMessage());
        }
    }
}
