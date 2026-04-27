package com.anonymous.mapper;

import com.anonymous.model.ReservationSlot;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ReservationSlotMapper {

    @Insert({
        "<script>",
        "INSERT INTO reservation_slot",
        "(reservation_id, user_id, room_id, seat_id, reserve_date, slot_code, slot_start_time, slot_end_time, create_time)",
        "VALUES",
        "<foreach collection='items' item='item' separator=','>",
        "(#{item.reservationId}, #{item.userId}, #{item.roomId}, #{item.seatId},",
        "#{item.reserveDate}, #{item.slotCode}, #{item.slotStartTime}, #{item.slotEndTime}, NOW())",
        "</foreach>",
        "</script>"
    })
    int batchInsert(@Param("items") List<ReservationSlot> items);

    @Delete("DELETE FROM reservation_slot WHERE reservation_id = #{reservationId}")
    int deleteByReservationId(@Param("reservationId") Long reservationId);

    @Select({
            "<script>",
            "SELECT DISTINCT seat_id FROM reservation_slot",
            "WHERE room_id = #{roomId}",
            "AND reserve_date = #{reserveDate}",
            "AND slot_code IN",
            "<foreach collection='slotCodes' item='slotCode' open='(' separator=',' close=')'>",
            "#{slotCode}",
            "</foreach>",
            "</script>"
    })
    List<Long> findOccupiedSeatIdsByRoomAndDateAndSlots(
            @Param("roomId") Long roomId,
            @Param("reserveDate") LocalDate reserveDate,
            @Param("slotCodes") List<String> slotCodes
    );

}
