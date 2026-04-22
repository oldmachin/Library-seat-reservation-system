-- KEYS: [1]lock_key, [2]room_hash
-- ARGV: [1]action, [2]seat_id, [3]user_id, [4]expire_time

local lock_key = KEYS[1]
local room_hash = KEYS[2]
local action = ARGV[1]
local seat_id = ARGV[2]
local user_id = ARGV[3]
local expire_time = ARGV[4]

-- 1. 函数定义必须置顶 (Lua Scope Requirement)
local function verify(target_key, expected_user)
    local current_user = redis.call('GET', target_key)
    if not current_user then
        return 0 -- 无人占用
    end
    if current_user == expected_user then
        return 1 -- 本人占用
    end
    return -1 -- 他人占用
end

-- 2. 状态预读取
local result = verify(lock_key, user_id)
local status = redis.call('HGET', room_hash, seat_id)

-- 3. 动作分发逻辑
if action == "RESERVE" then
    -- 允许条件：(无人锁且空闲) OR (本人已锁且正在操作)
    if (result == 0 and (not status or status == '0')) or (result == 1) then
        redis.call('SETEX', lock_key, expire_time, user_id)
        redis.call('HSET', room_hash, seat_id, '1') -- 状态1: 已预约
        return 1
    elseif result == -1 then
        return -1 -- 被他人锁定
    else
        return -2 -- 席位非空闲
    end

elseif action == "CHECK_IN" then
    -- 签到：必须是本人锁且状态为“已预约”
    if result == 1 and status == '1' then
        redis.call('HSET', room_hash, seat_id, '2') -- 状态2: 使用中
        -- 签到后可选择删除临时 lock_key，或保留直至离座
        return 1
    end
    return -1

elseif action == "LEAVE_TEMP" then
    -- 暂离：必须是正在使用中
    if status == '2' then
        redis.call('HSET', room_hash, seat_id, '3') -- 状态3: 暂离
        -- 重新设定暂离锁，防止他人插队
        redis.call('SETEX', lock_key, expire_time, user_id)
        return 1
    end
    return -1

elseif action == "RELEASE" then
    -- 释放：校验身份，确保不会误删他人的锁
    if result == 1 or (not result and status ~= '0') then
        redis.call('DEL', lock_key)
        redis.call('HSET', room_hash, seat_id, '0')
        return 1
    end
    return -1
end

return -99 -- 未知动作响应