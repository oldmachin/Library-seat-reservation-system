package com.anonymous.mapper;

import com.anonymous.model.Room;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RoomMapper {

    @Insert("INSERT INTO room (name, capacity, status, create_time, update_time)" + " values (#{name}, #{capacity}, #{status}, #{createTime}, #{updateTime})")
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

    @Update("UPDATE room SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int update(@Param("id") Long id, @Param("status") Integer status);
}
