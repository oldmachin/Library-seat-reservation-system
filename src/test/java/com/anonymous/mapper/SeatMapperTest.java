//package com.anonymous.mapper;
//
//import com.anonymous.model.Seat;
//import com.anonymous.model.enums.SeatStatus;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//
//import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
//
//import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
//
//@MybatisTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
//public class SeatMapperTest {
//
//    @Autowired
//    private SeatMapper seatMapper;
//
//    @Test
//    @DisplayName("应该成功插入座位并回填主键ID")
//    void shouldInsertAndReturnId() {
//        // --- 1. Arrange (准备) ---
//        Seat newSeat = new Seat();
//        newSeat.setRoomId(1L);
//        newSeat.setSeatCode("READER-001");
//        newSeat.setType(0);   // 普通座位
//        newSeat.setStatus(SeatStatus.AVAILABLE); // 空闲
//        newSeat.setXAxis(10);
//        newSeat.setYAxis(20);
//        // createTime 由 SQL 的 NOW() 生成，无需手动设置
//
//        // --- 2. Act (执行) ---
//        int rowsAffected = seatMapper.insert(newSeat);
//
//        // --- 3. Assert (断言) ---
//        // 验证数据库受影响行数
//        assertThat(rowsAffected).isEqualTo(1);
//
//        // 验证主键回填机制 (MyBatis 的 @Options 功能)
//        // 动机：后续业务逻辑通常需要拿到刚插入的 ID
//        assertThat(newSeat.getId()).isNotNull();
//    }
//
//    @Test
//    @DisplayName("应该能正确映射数据库下划线字段到 Java 驼峰字段")
//    void shouldMapUnderscoreToCamelCase() {
//        // --- 1. Arrange ---
//        // 先插入一条数据
//        Seat setupSeat = new Seat();
//        setupSeat.setRoomId(2L);
//        setupSeat.setSeatCode("TEST-CAMEL");
//        setupSeat.setType(1);
//        setupSeat.setStatus(SeatStatus.AVAILABLE);
//        setupSeat.setXAxis(99); // 测试重点：x_axis -> xAxis
//        setupSeat.setYAxis(88); // 测试重点：y_axis -> yAxis
//        seatMapper.insert(setupSeat);
//
//        // --- 2. Act ---
//        Seat retrievedSeat = seatMapper.findById(setupSeat.getId());
//
//        // --- 3. Assert ---
//        assertThat(retrievedSeat).isNotNull();
//
//        // 核心验证：验证 application.yml 中的 map-underscore-to-camel-case 是否生效
//        // 如果配置失败，这里的 getXAxis() 将会是 null
//        assertThat(retrievedSeat.getXAxis()).isEqualTo(99);
//        assertThat(retrievedSeat.getYAxis()).isEqualTo(88);
//
//        // 验证时间字段也被正确读取
//        assertThat(retrievedSeat.getCreateTime()).isNotNull();
//    }
//
//    @Test
//    @DisplayName("应该只更新指定座位的状态")
//    void shouldUpdateSeatStatus() {
//        // --- 1. Arrange ---
//        Seat seat = new Seat();
//        seat.setRoomId(1L);
//        seat.setSeatCode("BROKEN-01");
//        seat.setType(0);
//        seat.setStatus(SeatStatus.AVAILABLE); // 初始状态：正常
//        seatMapper.insert(seat);
//
//        // --- 2. Act ---
//        // 模拟业务：将座位标记为“维修中”(1)
//        seatMapper.updateStatus(seat.getId(), 1);
//
//        // 重新查询以验证
//        Seat updatedSeat = seatMapper.findById(seat.getId());
//
//        // --- 3. Assert ---
//        assertThat(updatedSeat.getStatus()).isEqualTo(SeatStatus.RESERVED);
//    }
//}
