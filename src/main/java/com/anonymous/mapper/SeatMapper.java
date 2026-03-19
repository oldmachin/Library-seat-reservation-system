package com.anonymous.mapper;

import com.anonymous.model.Seat;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface SeatMapper {

    @Insert("INSERT INTO seat (room_id, seat_code, type, status, x_axis, y_axis, create_time, update_time) " + "VALUES (#{roomId}, #{seatCode}, #{type}, #{status}, #{xAxis}, #{yAxis}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Seat seat);

    @Update("UPDATE seat SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Update("UPDATE seat SET status = #{status}, maintenance_note = #{maintenanceNote} WHERE id = #{id}")
    int updateStatusAndNote(@Param("id") Long id, @Param("status") Integer status, @Param("maintenanceNote") String maintenanceNote);

    @Select("SELECT status, COUNT(*) FROM seat WHERE room_id = #{roomId} GROUP BY status")
    List<Map<String, Object>> countStatusByRoom(Long roomId);

    @Select("SELECT * FROM seat WHERE id = #{id}")
    Seat findById(Long id);

    @Select("SELECT * FROM seat WHERE room_id = #{roomId}")
    List<Seat> findByRoomId(Long roomId);
}
