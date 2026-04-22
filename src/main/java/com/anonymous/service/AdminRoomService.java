package com.anonymous.service;

import com.anonymous.common.Page;
import com.anonymous.dto.admin.room.RoomQueryDTO;
import com.anonymous.model.Room;
import com.anonymous.vo.admin.RoomAdminVO;

public interface AdminRoomService {

    Page<RoomAdminVO> findRoomCondition(RoomQueryDTO roomQueryDTO);

    RoomAdminVO findRoomById(Long id);

    Boolean addRoom(Room room);

    Boolean updateRoom(Room room);
}
