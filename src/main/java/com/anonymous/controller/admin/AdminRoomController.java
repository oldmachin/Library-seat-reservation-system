package com.anonymous.controller.admin;

import com.anonymous.common.Page;
import com.anonymous.common.Result;
import com.anonymous.dto.RoomStatusUpdateDTO;
import com.anonymous.dto.admin.RoomSeatAdminActionQueryDTO;
import com.anonymous.dto.admin.room.RoomCreateDTO;
import com.anonymous.dto.admin.room.RoomQueryDTO;
import com.anonymous.model.Room;
import com.anonymous.model.Seat;
import com.anonymous.service.AdminRoomService;
import com.anonymous.service.SeatService;
import com.anonymous.vo.RoomSeatAdminActionLogVO;
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

    @GetMapping("/actions")
    public Result<Page<RoomSeatAdminActionLogVO>> listRoomSeatActions(RoomSeatAdminActionQueryDTO queryDTO) {
        return Result.success(
                adminRoomService.listRoomSeatActions(queryDTO),
                "查询房间座位操作日志成功"
        );
    }

    @GetMapping("/{id}")
    public Result<RoomAdminVO> findRoom(@PathVariable Long id) {
        return Result.success(adminRoomService.findRoomById(id), "查询房间成功");
    }

    @PostMapping
    public Result<Boolean> addRoom(@RequestBody RoomCreateDTO request) {
        Boolean result = adminRoomService.addRoom(request);
        return Result.success(result, "新增房间成功");
    }

    @PutMapping("/{id}")
    public Result<Boolean> updateRoom(@PathVariable Long id, @RequestBody Room room) {
        room.setId(id);
        adminRoomService.updateRoom(room);
        return Result.success(true, "更新房间成功");
    }

    @PutMapping("/{id}/status")
    public Result<Boolean> updateRoomStatus(@PathVariable Long id,
                                            @RequestBody RoomStatusUpdateDTO request) {
        return Result.success(
                adminRoomService.updateRoomStatus(id, request.status()),
                "更新房间状态成功"
        );
    }

    @GetMapping("/{id}/seats")
    public Result<List<Seat>> getRoomSeats(@PathVariable Long id) {
        adminRoomService.findRoomById(id);
        return Result.success(seatService.getSeatByRoom(id), "查询房间座位成功");
    }
}
