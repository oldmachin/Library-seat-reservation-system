local seatKey = KEYS[1]
local userId = ARGV[1]
local ttl = ARGV[2]

local currentOwner = redis.call('get', seatKey)

if currentOwner == userId then
    redis.call('expire', seatKey, ttl)
    return 1
elseif (not currentOwner) or (currentOwner == "0") then
    redis.call('setex', seatKey, ttl, userId)
    return 1
else
    return 0
end