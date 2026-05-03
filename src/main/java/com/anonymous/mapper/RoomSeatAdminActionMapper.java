package com.anonymous.mapper;

import com.anonymous.dto.admin.RoomSeatAdminActionQueryDTO;
import com.anonymous.model.RoomSeatAdminAction;
import com.anonymous.vo.RoomSeatAdminActionLogVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface RoomSeatAdminActionMapper {
    @Insert("""
    INSERT INTO room_seat_admin_action
    (resource_type, action_type, operator_id, room_id, seat_id,
     before_status, after_status,
     before_name, after_name,
     before_capacity, after_capacity,
     before_template, after_template,
     before_note, after_note,
     reason, create_time)
    VALUES
    (#{resourceType}, #{actionType}, #{operatorId}, #{roomId}, #{seatId},
     #{beforeStatus}, #{afterStatus},
     #{beforeName}, #{afterName},
     #{beforeCapacity}, #{afterCapacity},
     #{beforeTemplate}, #{afterTemplate},
     #{beforeNote}, #{afterNote},
     #{reason}, NOW())
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RoomSeatAdminAction action);

    @Select("""
    <script>
    SELECT
        a.id,
        a.resource_type AS resourceType,
        a.action_type AS actionType,
        a.operator_id AS operatorId,
        u.name AS operatorName,
        a.room_id AS roomId,
        r.name AS roomName,
        a.seat_id AS seatId,
        s.seat_code AS seatCode,
        a.before_status AS beforeStatus,
        a.after_status AS afterStatus,
        a.before_name AS beforeName,
        a.after_name AS afterName,
        a.before_capacity AS beforeCapacity,
        a.after_capacity AS afterCapacity,
        a.before_template AS beforeTemplate,
        a.after_template AS afterTemplate,
        a.before_note AS beforeNote,
        a.after_note AS afterNote,
        a.reason,
        a.create_time AS createTime
    FROM room_seat_admin_action a
    LEFT JOIN user u ON a.operator_id = u.id
    LEFT JOIN room r ON a.room_id = r.id
    LEFT JOIN seat s ON a.seat_id = s.id
    <where>
        <if test="query.resourceType != null and query.resourceType != ''">
            AND a.resource_type = #{query.resourceType}
        </if>
        <if test="query.actionType != null and query.actionType != ''">
            AND a.action_type = #{query.actionType}
        </if>
        <if test="query.roomId != null">
            AND a.room_id = #{query.roomId}
        </if>
        <if test="query.seatId != null">
            AND a.seat_id = #{query.seatId}
        </if>
        <if test="query.operatorId != null">
            AND a.operator_id = #{query.operatorId}
        </if>
        <if test="query.keyword != null and query.keyword != ''">
            AND (
                r.name LIKE CONCAT('%', #{query.keyword}, '%')
                OR s.seat_code LIKE CONCAT('%', #{query.keyword}, '%')
                OR a.reason LIKE CONCAT('%', #{query.keyword}, '%')
                OR u.name LIKE CONCAT('%', #{query.keyword}, '%')
            )
        </if>
    </where>
    ORDER BY a.create_time DESC, a.id DESC
    LIMIT #{offset}, #{pageSize}
    </script>
""")
    List<RoomSeatAdminActionLogVO> findPage(
            @Param("query") RoomSeatAdminActionQueryDTO query,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    @Select("""
    <script>
    SELECT COUNT(*)
    FROM room_seat_admin_action a
    LEFT JOIN user u ON a.operator_id = u.id
    LEFT JOIN room r ON a.room_id = r.id
    LEFT JOIN seat s ON a.seat_id = s.id
    <where>
        <if test="query.resourceType != null and query.resourceType != ''">
            AND a.resource_type = #{query.resourceType}
        </if>
        <if test="query.actionType != null and query.actionType != ''">
            AND a.action_type = #{query.actionType}
        </if>
        <if test="query.roomId != null">
            AND a.room_id = #{query.roomId}
        </if>
        <if test="query.seatId != null">
            AND a.seat_id = #{query.seatId}
        </if>
        <if test="query.operatorId != null">
            AND a.operator_id = #{query.operatorId}
        </if>
        <if test="query.keyword != null and query.keyword != ''">
            AND (
                r.name LIKE CONCAT('%', #{query.keyword}, '%')
                OR s.seat_code LIKE CONCAT('%', #{query.keyword}, '%')
                OR a.reason LIKE CONCAT('%', #{query.keyword}, '%')
                OR u.name LIKE CONCAT('%', #{query.keyword}, '%')
            )
        </if>
    </where>
    </script>
""")
    long countPage(@Param("query") RoomSeatAdminActionQueryDTO query);
}
