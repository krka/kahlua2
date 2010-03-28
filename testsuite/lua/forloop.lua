local assert = assert
local pairs = pairs
local setmetatable = setmetatable
local type = type

local mt = {}
function mt.__index(t, k)
	if type(k) == "string" then
		return k .. k
	end
end

local t = setmetatable({}, mt)
testCall(function()
	for key, value in pairs{1} do
		assert(true)
		assert(t.hello == "hellohello")
	end
end)

