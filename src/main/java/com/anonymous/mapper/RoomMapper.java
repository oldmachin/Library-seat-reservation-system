package com.anonymous.mapper;

import com.anonymous.dto.admin.room.RoomQueryDTO;
import com.anonymous.model.Room;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RoomMapper {

    @Insert("INSERT INTO room (name, capacity, status, create_time, update_time) " +
            "VALUES (#{name}, #{capacity}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Room room);

    @Select("SELECT * FROM room WHERE id = #{id}")
    Room findById(Long id);

    @Select("SELECT * FROM room ORDER BY status ASC")
    List<Room> findAll();

    @Select("<script>" +
            "SELECT * FROM room " +
            "<where>" +
            "  <if test='statuses != null and !statuses.isEmpty()'>" +
            "    status IN " +
            "    <foreach item='s' collection='statuses' open='(' separator=',' close=')'>" +
            "      #{s}" +
            "    </foreach>" +
            "  </if>" +
            "</where>" +
            "</script>")
    List<Room> findAllByStatuses(@Param("statuses") List<Integer> statuses);

    @Select("<script>" +
            "SELECT * FROM room " +
            "<where>" +
            "  <if test='query.id != null'> AND id = #{query.id} </if>" +
            "  <if test='query.name != null and query.name != \"\"'> AND name LIKE CONCAT('%', #{query.name}, '%') </if>" +
            "  <if test='query.capacity != null'> AND capacity = #{query.capacity} </if>" +
            "  <if test='query.status != null'> AND status = #{query.status} </if>" +
            "</where>" +
            "ORDER BY id ASC " +
            "LIMIT #{offset}, #{size}" +
            "</script>")
    List<Room> findByCondition(@Param("query") RoomQueryDTO queryDTO,
                               @Param("offset") Integer offset,
                               @Param("size") Integer size);

    @Select("<script>" +
            "SELECT COUNT(1) FROM room " +
            "<where>" +
            "  <if test='query.id != null'> AND id = #{query.id} </if>" +
            "  <if test='query.name != null and query.name != \"\"'> AND name LIKE CONCAT('%', #{query.name}, '%') </if>" +
            "  <if test='query.capacity != null'> AND capacity = #{query.capacity} </if>" +
            "  <if test='query.status != null'> AND status = #{query.status} </if>" +
            "</where>" +
            "</script>")
    Long countByCondition(@Param("query") RoomQueryDTO queryDTO);

    @Update("<script>" +
            "UPDATE room " +
            "<set>" +
            "  <if test='name != null and name != \"\"'>name = #{name},</if>" +
            "  <if test='capacity != null'>capacity = #{capacity},</if>" +
            "  <if test='status != null'>status = #{status},</if>" +
            "  update_time = NOW()" +
            "</set>" +
            "WHERE id = #{id}" +
            "</script>")
    int updateRoom(Room room);

    @Update("UPDATE room SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int update(@Param("id") Long id, @Param("status") Integer status);
}
