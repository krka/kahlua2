testAssert = testAssert or assert

local function count(t)
	local n = 0
	for k in next, t do
		n = n + 1
	end
	return n
end

local t = {}

t["a"] = 1
t["b"] = 1
t["c"] = 1
testAssert(count(t) == 3)

setmetatable(t, {__mode = "k"})

testAssert(count(t) == 3)
t["d"] = 1
testAssert(count(t) == 4)

local t2 = {}
t[t2] = 1
testAssert(count(t) == 5)

t2 = nil
collectgarbage();
collectgarbage();
collectgarbage();
testAssert(count(t) == 4)

local t3 = {}
t[t3] = 1
testAssert(count(t) == 5)
collectgarbage();
collectgarbage();
collectgarbage();
testAssert(count(t) == 5)
setmetatable(t, {__mode = "v"})
t3 = nil
collectgarbage();
collectgarbage();
collectgarbage();
testAssert(count(t) == 5)
local t4 = {}
t[1] = t4
testAssert(count(t) == 6)
collectgarbage();
collectgarbage();
collectgarbage();
t4 = nil
collectgarbage();
collectgarbage();
collectgarbage();
testAssert(count(t) == 5)

do
	local t = setmetatable({}, {__mode = "kv"})
	for i = 1, 10 do
		for i = 1, 10 do
			t[{}] = {}
		end
		testAssert(count(t) == 10)
		collectgarbage()
		testAssert(count(t) == 0)
	end
end


do
	local t = setmetatable({}, {__mode = "kv"})
	local key = {}
	t[key] = key
	assert(next(t) == key)
	key = nil
	collectgarbage()
	assert(next(t) == nil)
end

