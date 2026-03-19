local seatKey = KEYS[1]
local userId = ARGV[1]

local currentOwner = redis.call('get', seatKey)

if currentOwner == userId then
    return redis.call('del', seatKey)
else
    return 0
end