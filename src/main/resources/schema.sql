DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS seat;
DROP TABLE IF EXISTS room;
DROP TABLE IF EXISTS user;

CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(20) NOT NULL,
    username VARCHAR(20) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(10) NOT NULL DEFAULT 'USER',
    status TINYINT NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS room (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    capacity INT NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS seat (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT NOT NULL,
    seat_code VARCHAR(50) NOT NULL,
    type TINYINT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 0,
    maintenance_note VARCHAR(255) DEFAULT '',
    x_axis INT,
    y_axis INT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_seat_room FOREIGN KEY (room_id) REFERENCES room(id)
);

CREATE TABLE IF NOT EXISTS reservation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    actual_start_time TIMESTAMP NULL,
    actual_end_time TIMESTAMP NULL,
    status TINYINT NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 1,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_reservation_user FOREIGN KEY (user_id) REFERENCES user(id),
    CONSTRAINT fk_reservation_room FOREIGN KEY (room_id) REFERENCES room(id),
    CONSTRAINT fk_reservation_seat FOREIGN KEY (seat_id) REFERENCES seat(id)
);

-- 用户
-- 注意：如果你登录逻辑已经严格使用 BCrypt，这里的密码必须换成 BCrypt 密文
INSERT INTO user (id, name, username, password, role, status) VALUES
(1, '系统管理员', 'admin', '123456', 'ADMIN', 0),
(2, '张三', '20260001', '123456', 'USER', 0),
(3, '李四', '20260002', '123456', 'USER', 0),
(4, '王五', '20260003', '123456', 'USER', 1);

-- 房间
-- RoomStatus: 0 AVAILABLE, 1 MAINTAINING, 2 DISCARD
INSERT INTO room (id, name, capacity, status) VALUES
(1, '一楼自习室', 50, 0),
(2, '二楼阅览室', 80, 0),
(3, '三楼研讨区', 20, 1),
(4, '四楼封闭区', 30, 2);

-- 座位
-- SeatStatus: 0 AVAILABLE, 1 RESERVED, 2 OCCUPIED, 3 AWAY, 4 UNAVAILABLE
INSERT INTO seat (id, room_id, seat_code, type, status, maintenance_note, x_axis, y_axis) VALUES
(1, 1, 'A-01', 0, 0, '', 10, 10),
(2, 1, 'A-02', 0, 1, '', 20, 10),
(3, 1, 'A-03', 1, 2, '', 30, 10),
(4, 1, 'A-04', 0, 4, '设备损坏，暂停使用', 40, 10),
(5, 2, 'B-01', 0, 0, '', 10, 20),
(6, 2, 'B-02', 1, 0, '', 20, 20),
(7, 2, 'B-03', 0, 1, '', 30, 20),
(8, 3, 'C-01', 0, 4, '房间维护中', 10, 30);

-- 预约
-- 这里按你当前管理端代码使用的状态码来放数据：
-- 0 已预约
-- 1 使用中
-- 2 已完成
-- 3 用户取消
-- 4 已过期
-- 5 管理员取消
-- 6 违约
INSERT INTO reservation (
    id, user_id, room_id, seat_id, start_time, end_time,
    actual_start_time, actual_end_time, status, version
) VALUES
(1, 2, 1, 2, '2026-04-05 09:00:00', '2026-04-05 12:00:00', NULL, NULL, 0, 1),
(2, 3, 1, 3, '2026-04-05 08:30:00', '2026-04-05 11:30:00', '2026-04-05 08:35:00', NULL, 1, 1),
(3, 2, 1, 1, '2026-04-03 09:00:00', '2026-04-03 11:00:00', '2026-04-03 09:02:00', '2026-04-03 10:50:00', 2, 1),
(4, 3, 2, 5, '2026-04-02 14:00:00', '2026-04-02 16:00:00', NULL, NULL, 3, 1),
(5, 2, 2, 7, '2026-04-04 08:00:00', '2026-04-04 10:00:00', NULL, NULL, 4, 1),
(6, 3, 2, 6, '2026-04-01 13:00:00', '2026-04-01 15:00:00', NULL, NULL, 5, 1),
(7, 2, 2, 5, '2026-04-04 15:00:00', '2026-04-04 17:00:00', '2026-04-04 15:05:00', '2026-04-04 15:40:00', 6, 1);