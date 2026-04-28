package com.anonymous.mapper;

import com.anonymous.model.Announcement;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AnnouncementMapper {

    @Insert("INSERT INTO announcement " +
            "(title, content, status, is_pinned, publish_time, expire_time, creator_id, create_time, update_time) " +
            "VALUES " +
            "(#{title}, #{content}, #{status}, #{isPinned}, #{publishTime}, #{expireTime}, #{creatorId}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Announcement announcement);

    @Select("SELECT * FROM announcement WHERE id = #{id}")
    Announcement findById(@Param("id") Long id);

    @Select("SELECT * FROM announcement " +
            "WHERE status = 1 " +
            "AND (publish_time IS NULL OR publish_time <= NOW()) " +
            "AND (expire_time IS NULL OR expire_time > NOW()) " +
            "ORDER BY is_pinned DESC, publish_time DESC, id DESC")
    List<Announcement> findPublishedActive();

    @Select("SELECT * FROM announcement " +
            "WHERE id = #{id} " +
            "AND status = 1 " +
            "AND (publish_time IS NULL OR publish_time <= NOW()) " +
            "AND (expire_time IS NULL OR expire_time > NOW())")
    Announcement findPublishedActiveById(@Param("id") Long id);

    @Select("SELECT * FROM announcement " +
            "ORDER BY is_pinned DESC, update_time DESC, id DESC " +
            "LIMIT #{offset}, #{size}")
    List<Announcement> findAllForAdmin(@Param("offset") Integer offset,
                                       @Param("size") Integer size);

    @Select("SELECT COUNT(1) FROM announcement")
    Long countAllForAdmin();

    @Update("<script>" +
            "UPDATE announcement " +
            "<set>" +
            "  <if test='title != null and title != \"\"'>title = #{title},</if>" +
            "  <if test='content != null and content != \"\"'>content = #{content},</if>" +
            "  <if test='isPinned != null'>is_pinned = #{isPinned},</if>" +
            "  <if test='publishTime != null'>publish_time = #{publishTime},</if>" +
            "  <if test='expireTime != null'>expire_time = #{expireTime},</if>" +
            "  update_time = NOW()" +
            "</set>" +
            "WHERE id = #{id}" +
            "</script>")
    int updateAnnouncement(Announcement announcement);

    @Update("UPDATE announcement SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Delete("DELETE FROM announcement WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
