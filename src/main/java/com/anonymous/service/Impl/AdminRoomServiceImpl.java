package com.anonymous.service.Impl;

import com.anonymous.common.Page;
import com.anonymous.common.exception.InvalidParameterException;
import com.anonymous.dto.admin.room.RoomQueryDTO;
import com.anonymous.mapper.RoomMapper;
import com.anonymous.model.Room;
import com.anonymous.model.enums.RoomStatus;
import com.anonymous.service.AdminRoomService;
import com.anonymous.vo.admin.RoomAdminVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminRoomServiceImpl implements AdminRoomService {
    @Autowired
    private RoomMapper roomMapper;

    @Override
    public Page<RoomAdminVO> findRoomCondition(RoomQueryDTO roomQueryDTO) {
        RoomQueryDTO query = roomQueryDTO == null
                ? new RoomQueryDTO(1, 10, null, null, null, null)
                : roomQueryDTO;

        Integer pageNum = query.page();
        Integer pageSize = query.size();

        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        if (pageSize > 100) {
            pageSize = 100;
        }

        int offset = (pageNum - 1) * pageSize;

        List<Room> rooms = roomMapper.findByCondition(query, offset, pageSize);
        Long total = roomMapper.countByCondition(query);

        List<RoomAdminVO> result = rooms.stream()
                .map(this::toRoomAdminVO)
                .toList();

        return new Page<>(result, total == null ? 0 : total, pageNum, pageSize);
    }

    @Override
    public RoomAdminVO findRoomById(Long id) {
        Room room = roomMapper.findById(id);
        if (room == null) {
            throw new InvalidParameterException("room.id");
        }
        return toRoomAdminVO(room);
    }

    @Override
    public Boolean addRoom(Room room) {
        if (room == null) {
            throw new InvalidParameterException("room");
        }
        if (room.getStatus() == null) {
            room.setStatus(RoomStatus.AVAILABLE.getCode());
        }
        return roomMapper.insert(room) > 0;
    }

    @Override
    public Boolean updateRoom(Room room) {
        if (room == null || room.getId() == null) {
            throw new InvalidParameterException("room.id");
        }

        Room existed = roomMapper.findById(room.getId());
        if (existed == null) {
            throw new InvalidParameterException("room.id");
        }

        boolean noFieldToUpdate = room.getName() == null
                && room.getCapacity() == null
                && room.getStatus() == null;
        if (noFieldToUpdate) {
            return false;
        }

        return roomMapper.updateRoom(room) > 0;
    }

    private RoomAdminVO toRoomAdminVO(Room room) {
        String statusText = "未知";
        if (room.getStatus() != null) {
            try {
                statusText = RoomStatus.fromCode(room.getStatus()).getDescription();
            } catch (IllegalArgumentException ignored) {
                statusText = "未知";
            }
        }

        return new RoomAdminVO(
                room.getId(),
                room.getName(),
                room.getCapacity(),
                room.getStatus(),
                statusText
        );
    }
}
