package com.anonymous.service.Impl;

import com.anonymous.mapper.RoomMapper;
import com.anonymous.mapper.SeatMapper;
import com.anonymous.model.Room;
import com.anonymous.model.enums.SeatStatus;
import com.anonymous.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RoomServiceImpl implements RoomService {

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private SeatMapper seatMapper;

    @Override
    public void updateRoomStatus(Long id, Integer status) {
        Room room = getRoomById(id);
        if (room == null) {
            throw new RuntimeException("阅览室不存在，ID: " + id);
        }
        roomMapper.update(id, status);
    }

    @Override
    public double getOccupancyRate(Long id) {
        List<Map<String, Object>> stats = seatMapper.countStatusByRoom(id);
        long reserved = 0, occupied = 0, away = 0, available = 0;

        for (Map<String, Object> row : stats) {
            long status = (long) row.get("status");
            long count = (long) row.get("count");

            if (status == SeatStatus.RESERVED.getCode()) reserved = count;
            else if (status == SeatStatus.OCCUPIED.getCode()) occupied = count;
            else if (status == SeatStatus.AWAY.getCode()) away = count;
            else if (status == SeatStatus.AVAILABLE.getCode()) available = count;
        }

        long numerator = reserved + occupied + away;
        long denominator = numerator + available;

        if (denominator == 0) return 0.0;
        return (double) numerator / denominator;
    }

    @Override
    public Room getRoomById(Long id) {
        Room room = roomMapper.findById(id);
        if (room == null) {
            throw new RuntimeException("阅览室不存在，ID: " + id);
        }
        return room;
    }

    @Override
    public List<Room> getRoomsByCondition(List<Integer> statues) {
        if (statues == null) {
            return roomMapper.findAll();
        } else {
            return roomMapper.findAllByStatuses(statues);
        }
    }
}
