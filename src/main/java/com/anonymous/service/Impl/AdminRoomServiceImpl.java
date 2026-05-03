package com.anonymous.service.Impl;

import com.anonymous.common.Page;
import com.anonymous.common.exception.InvalidParameterException;
import com.anonymous.common.util.SecurityUtils;
import com.anonymous.dto.admin.RoomSeatAdminActionQueryDTO;
import com.anonymous.dto.admin.room.RoomCreateDTO;
import com.anonymous.dto.admin.room.RoomQueryDTO;
import com.anonymous.mapper.RoomMapper;
import com.anonymous.mapper.RoomSeatAdminActionMapper;
import com.anonymous.mapper.SeatMapper;
import com.anonymous.model.Room;
import com.anonymous.model.RoomSeatAdminAction;
import com.anonymous.model.Seat;
import com.anonymous.model.enums.AdminResourceType;
import com.anonymous.model.enums.RoomSeatAdminActionType;
import com.anonymous.model.enums.RoomStatus;
import com.anonymous.service.AdminRoomService;
import com.anonymous.service.RoomTemplateFactory;
import com.anonymous.vo.RoomSeatAdminActionLogVO;
import com.anonymous.vo.admin.RoomAdminVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminRoomServiceImpl implements AdminRoomService {

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private RoomSeatAdminActionMapper roomSeatAdminActionMapper;

    private void recordRoomAction(RoomSeatAdminActionType actionType,
                                  Room before,
                                  Room after,
                                  String reason) {
        RoomSeatAdminAction action = new RoomSeatAdminAction();
        action.setResourceType(AdminResourceType.ROOM.name());
        action.setActionType(actionType.name());
        action.setRoomId(after != null ? after.getId() : before.getId());
        action.setSeatId(null);

        action.setBeforeStatus(before == null ? null : before.getStatus());
        action.setAfterStatus(after == null ? null : after.getStatus());

        action.setBeforeName(before == null ? null : before.getName());
        action.setAfterName(after == null ? null : after.getName());

        action.setBeforeCapacity(before == null ? null : before.getCapacity());
        action.setAfterCapacity(after == null ? null : after.getCapacity());

        action.setBeforeTemplate(before == null ? null : before.getLayoutTemplate());
        action.setAfterTemplate(after == null ? null : after.getLayoutTemplate());

        action.setReason(reason == null ? "" : reason.trim());

        try {
            action.setOperatorId(SecurityUtils.getCurrentUserId());
        } catch (RuntimeException e) {
            action.setOperatorId(null);
        }

        roomSeatAdminActionMapper.insert(action);
    }

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
    @Transactional(rollbackFor = Exception.class)
    public Boolean addRoom(RoomCreateDTO request) {
        if (request == null || request.name() == null || request.name().trim().isEmpty()) {
            throw new InvalidParameterException("room.name");
        }

        String template = request.layoutTemplate() == null || request.layoutTemplate().isBlank()
                ? "CLASSROOM"
                : request.layoutTemplate().trim().toUpperCase();

        List<Seat> previewSeats = RoomTemplateFactory.createSeats(template, 0L);

        Room room = new Room();
        room.setName(request.name().trim());
        room.setCapacity(previewSeats.size());
        room.setStatus(request.status() == null ? RoomStatus.AVAILABLE.getCode() : request.status());
        room.setLayoutTemplate(template);

        roomMapper.insert(room);

        List<Seat> seats = RoomTemplateFactory.createSeats(template, room.getId());
        seatMapper.batchInsert(seats);

        recordRoomAction(RoomSeatAdminActionType.ROOM_CREATED, null, room, "管理员创建房间");

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
                && room.getStatus() == null
                && room.getLayoutTemplate() == null;
        if (noFieldToUpdate) {
            throw new RuntimeException("没有可更新的字段");
        }


        int rows = roomMapper.updateRoom(room);
        if (rows == 0) {
            throw new RuntimeException("房间更新失败");
        }

        Room afterRoom = roomMapper.findById(room.getId());
        recordRoomAction(RoomSeatAdminActionType.ROOM_UPDATED, existed, afterRoom, "管理员更新房间数据");
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateRoomStatus(Long id, Integer status) {
        if (id == null) {
            throw new InvalidParameterException("room.id");
        }
        if (status == null) {
            throw new InvalidParameterException("room.status");
        }

        RoomStatus.fromCode(status);

        Room room = roomMapper.findById(id);

        if (room == null) {
            throw new InvalidParameterException("room.id");
        }

        Room update = new Room();
        update.setId(id);
        update.setStatus(status);

        int rows = roomMapper.updateRoom(update);
        if (rows == 0) {
            throw new RuntimeException("房间更新失败");
        }

        Room afterRoom = roomMapper.findById(id);
        recordRoomAction(RoomSeatAdminActionType.ROOM_STATUS_UPDATED, room, afterRoom, "管理员修改房间状态");

        return true;
    }

    @Override
    public Page<RoomSeatAdminActionLogVO> listRoomSeatActions(RoomSeatAdminActionQueryDTO queryDTO) {
        RoomSeatAdminActionQueryDTO query = queryDTO == null
                ? new RoomSeatAdminActionQueryDTO(1, 10, null, null, null, null, null, null)
                : queryDTO;

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

        var records = roomSeatAdminActionMapper.findPage(query, offset, pageSize);
        long total = roomSeatAdminActionMapper.countPage(query);

        return new Page<>(records, total, pageNum, pageSize);
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
                statusText,
                room.getLayoutTemplate()
        );
    }
}
