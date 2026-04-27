package com.anonymous.mapper;

import com.anonymous.model.ReputationRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ReputationRecordMapper {

    @Insert("INSERT INTO reputation_record " +
        "(user_id, reservation_id, operator_id, event_type, score_before, score_delta, score_after, reason, blacklist_until, create_time) " +
        "VALUES " +
        "(#{userId}, #{reservationId}, #{operatorId}, #{eventType}, #{scoreBefore}, #{scoreDelta}, #{scoreAfter}, #{reason}, #{blacklistUntil}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ReputationRecord record);


    @Select("SELECT * FROM reputation_record WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{limit} OFFSET #{offset}")
    List<ReputationRecord> findPageByUserId(@Param("userId") Long userId,
                                            @Param("limit") int limit,
                                            @Param("offset") int offset);

    @Select("SELECT COUNT(*) FROM reputation_record WHERE user_id = #{userId}")
    long countByUserId(@Param("userId") Long userId);
}
