CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(10) NOT NULL,
    password VARCHAR(20) NOT NULL,
    status TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS seat (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT NOT NULL,
    seat_code VARCHAR(50) NOT NULL,
    type TINYINT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 0,
    x_axis INT,
    y_axis INT,
    create_time TIMESTAMP,
    update_time TIMESTAMP
);

CREATE TABLE IF NOT EXISTS room (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    capacity INT NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    create_time TIMESTAMP,
    update_time TIMESTAMP
);

CREATE TABLE IF NOT EXISTS reservation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    actual_start_time TIMESTAMP NULL,
    actual_end_time TIMESTAMP NULL,
    status TINYINT NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 1,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO user (username, password, status) VALUES
('20260001', '123456', 0), -- 核心测试账号
('20260002', '123456', 0), -- 备用账号
('20260003', '123456', 0);

-- 插入阅览室
-- 包含一个开放、一个维护中的房间
INSERT INTO room (id, name, capacity, status) VALUES
(1, '法兰要塞自习室', 50, 0), -- AVAILABLE
(2, '传火祭祀场休息室', 20, 1); -- MAINTAINING

-- 插入座位 (以 Room 1 为例)
-- 模拟不同状态的座位以测试前端渲染
INSERT INTO seat (id, room_id, seat_code, status, x_axis, y_axis) VALUES
(1, 1, 'A-01', 0, 10, 10), -- 空闲 (前端应显示绿色)
(2, 1, 'A-02', 1, 10, 20), -- 已预约 (前端应显示橙色)
(3, 1, 'A-03', 2, 20, 10), -- 使用中 (前端应显示红色)
(4, 1, 'A-04', 4, 20, 20), -- 不可用 (前端应显示灰色)
(5, 1, 'B-01', 0, 30, 10); -- 另一个空闲座位

-- 插入测试预约记录
-- 1. 为用户 20260001 插入一条待签到的记录，测试 checkIn 功能
INSERT INTO reservation (user_id, seat_id, start_time, end_time, status) VALUES
(1, 2, '2026-03-20 08:00:00', '2026-03-20 12:00:00', 0);

-- 2. 为用户 20260002 插入一条使用中的记录，测试 checkOut/leaveTemp 功能
INSERT INTO reservation (user_id, seat_id, start_time, end_time, status) VALUES
(2, 3, '2026-03-19 14:00:00', '2026-03-19 18:00:00', 1);

-- 3. 插入一些历史数据，用于测试 getHistory 分页功能
INSERT INTO reservation (user_id, seat_id, start_time, end_time, status) VALUES
(1, 1, '2026-03-18 08:00:00', '2026-03-18 10:00:00', 2), -- 已完成
(1, 5, '2026-03-17 08:00:00', '2026-03-17 10:00:00', 4); -- 已违约
