package com.anonymous.mapper;

import com.anonymous.dto.ReservationAdminActionQueryDTO;
import com.anonymous.model.ReservationAdminAction;
import com.anonymous.vo.ReservationAdminActionLogVO;
import com.anonymous.vo.ReservationAdminActionVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ReservationAdminActionMapper {

     @Insert("""
        INSERT INTO reservation_admin_action
        (reservation_id, action_type, operator_id, reason, penalty_level, ban_days, create_time)
        VALUES
        (#{reservationId}, #{actionType}, #{operatorId}, #{reason}, #{penaltyLevel}, #{banDays}, NOW())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ReservationAdminAction action);

    @Select("""
        SELECT id, action_type, operator_id, reason, penalty_level, ban_days, create_time
        FROM reservation_admin_action
        WHERE reservation_id = #{reservationId}
        ORDER BY create_time DESC, id DESC
        """)
    List<ReservationAdminActionVO> findByReservationId(@Param("reservationId") Long reservationId);

    List<ReservationAdminActionLogVO> findPage(@Param("query")ReservationAdminActionQueryDTO query,
                                               @Param("offset") int offset,
                                               @Param("pageSize") int pageSize);

    long countPage(@Param("query") ReservationAdminActionQueryDTO query);
}
