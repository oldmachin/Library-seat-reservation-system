package com.anonymous.mapper;

import com.anonymous.dto.admin.reservation.ReservationQueryDTO;
import com.anonymous.model.Reservation;
import com.anonymous.vo.admin.ReservationAdminVO;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ReservationMapper {
    @Insert("INSERT INTO reservation (user_id, room_id, seat_id, start_time, end_time, status, version, create_time, update_time)" +
            " VALUES (#{userId}, #{roomId}, #{seatId}, #{startTime}, #{endTime}, #{status}, 1, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Reservation reservation);

    @Select("SELECT COUNT(*) FROM reservation WHERE seat_id = #{seatId} " +
            "AND status IN (0, 1) " +
            "AND (#{start} < end_time AND #{end} > start_time)")
    int countOverlap(@Param("seatId") Long seatId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Select("SELECT COUNT(*) FROM reservation WHERE user_id = #{userId} AND seat_id = #{seatId} AND status IN (0, 1) AND start_time = #{startTime} AND end_time = #{endTime}")
    int checkDuplicate(@Param("userId") Long userId, @Param("seatId") Long seatId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Select("SELECT * FROM reservation WHERE id = #{reservationId}")
    Reservation findById(Long reservationId);

    @Select("SELECT * FROM reservation WHERE user_id = #{userId} AND seat_id = #{seatId}")
    Reservation findByUserIdAndSeatId(@Param("userId") Long userId, @Param("seatId") Long seatId);

    @Select("SELECT * FROM reservation WHERE user_id = #{userId} AND seat_id = #{seatId} AND status = #{status}")
    List<Reservation> findByUserId(@Param("userId") Long userId, @Param("seatId") Long seatId, @Param("status") Integer status);

    @Select("SELECT COUNT(*) FROM reservation WHERE user_id = #{userId}")
    long countByUserId(Long userId);

    @Select("SELECT COUNT(*) FROM reservation WHERE user_id = #{userId} AND status = #{status}")
    int countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Integer status);

    @Select("SELECT COUNT(*) FROM reservation WHERE user_id = #{userId} AND status IN (0, 1)")
    int countActiveReservationsByUserId(Long userId);

    @Select("SELECT * FROM reservation WHERE user_id = #{userId} AND status IN (0, 1)")
    Reservation findCurrent(@Param("userId") Long userId);

    @Select("SELECT * FROM reservation WHERE user_id = #{userId} AND status = 0")
    Reservation findPending(@Param("userId") Long userId);

    @Select("SELECT * FROM reservation WHERE user_id = #{userId} AND status = 1")
    Reservation findInUse(@Param("userId") Long userId);

    @Select("SELECT * FROM reservation WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{limit} OFFSET #{offset}")
    List<Reservation> findPageByUserId(@Param("userId") Long userId, @Param("limit") int limit, @Param("offset") int offset);

    @Update("UPDATE reservation SET status = #{toStatus}, update_time = NOW() WHERE id = #{id} AND status = #{fromStatus}")
    int updateStatus(@Param("id") Long id, @Param("fromStatus") Integer fromStatus, @Param("toStatus") Integer toStatus);

    @Update("UPDATE reservation SET status = #{status}, version = version + 1, update_time = NOW() " +
            "WHERE id = #{id} AND version = #{version}")
    int updateWithVersion(Reservation reservation);

    List<ReservationAdminVO> findReservationsByCondition(@Param("query")ReservationQueryDTO queryDTO,
                                                        @Param("offset") Integer offset,
                                                        @Param("size") Integer size);

    Long countReservationsByCondition(@Param("query") ReservationQueryDTO queryDTO);

    List<ReservationAdminVO> findAllCurrent(@Param("offset") Integer offset, @Param("size") Integer size);

    Long countReservationsCurrent();
}
