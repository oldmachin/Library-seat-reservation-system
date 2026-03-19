package com.anonymous.service;

import com.anonymous.model.Room;

import java.util.List;

public interface RoomService {

    void updateRoomStatus(Long id, Integer status);

    double getOccupancyRate(Long id);

    List<Room> getRoomsByCondition(List<Integer> statues);

    Room getRoomById(Long id);
}
