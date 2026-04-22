-- KEYS: [1]lock_key, [2]room_hash
-- ARGV: [1]seat_id, [2]user_id

local owner = redis.call('GET', KEYS[1])

if owner and owner ~= ARGV[2] then
    return -1
end

redis.call('DEL', KEYS[1])
redis.call('HSET', KEYS[2], ARGV[1], '0')
return 1