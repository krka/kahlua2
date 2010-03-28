local meta = {}
function meta.__add(a, b)
	local c = {}
	for i = 1, math.max(#a, #b) do
		c[i] = (a[i] or 0) + (b[i] or 0)
	end
	return c
end

function meta.__sub(a, b)
	local c = {}
	for i = 1, math.max(#a, #b) do
		c[i] = (a[i] or 0) - (b[i] or 0)
	end
	return c
end

local a = setmetatable({}, meta)
local b = setmetatable({}, meta)
a[1] = 15
a[2] = 30
a[3] = 20

b[1] = 9
b[2] = 51

local c = a + b
testAssert(type(c) == "table")
testAssert(c[1] == 24)
testAssert(c[2] == 81)
testAssert(c[3] == 20)

local c = a - b
testAssert(type(c) == "table")
testAssert(c[1] == 6)
testAssert(c[2] == -21)
testAssert(c[3] == 20)

function endswith(s1, s2)
	return s1:sub(-#s2, -1) == s2
end

do
	local meta = {}
	meta.__index = meta
	meta.__newindex = meta

	local t = setmetatable(meta, meta)
	local ok, errmsg = pcall(function() return t.hello end)
	testAssert(not ok, "expected recursive metatable error")
	testAssert(endswith(errmsg, "loop in gettable"), "wrong error message: " .. errmsg)
	
	local ok, errmsg = pcall(function() t.hello = "world" end)
	testAssert(not ok, "expected recursive metatable error")
	testAssert(endswith(errmsg, "loop in settable"), "wrong error message: " .. errmsg)
end

do
	local t1, t2 = {}, {}
	local ok, errmsg = pcall(function() return t1 + t2 end)
	testAssert(not ok)
	--assert(endswith(errmsg, "no meta function was found for __add"))
	
	local ok, errmsg = pcall(function() local x = (-t1) end)
	testAssert(not ok)

	local ok, errmsg = pcall(function() return t1 <= t2 end)
	testAssert(not ok)
	
	local ok, errmsg = pcall(function() return t1 == t2 end)
	testAssert(ok)
end


do
	local meta = {__lt = function(a, b) return true end}
	local t1 = setmetatable({}, meta)
	local t2 = setmetatable({}, meta)
	testAssert(t1 < t2)
	testAssert(t2 < t1)
	testAssert(not (t1 <= t2))
	testAssert(not (t2 <= t1))
end

do
	local meta1 = {__lt = function(a, b) return true end}
	local meta2 = {__lt = function(a, b) return false end}
	local t1 = setmetatable({}, meta1)
	local t2 = setmetatable({}, meta2)
	local ok, errmsg = pcall(function() assert(t1 < t2) end)
	testAssert(not ok)
end

do
	local meta = {__unm = function(a) return {-a[1]} end}
	local t1 = setmetatable({12}, meta)
	local t2
	local ok, errmsg = pcall(function() t2 = -t1 end)
	testAssert(ok)
	testAssert(t2[1] == -12)
end

do
	local meta = {__eq = function(a, b) return rawequal(a, b) end}
	local t1 = setmetatable({}, meta)
	local t2 = setmetatable({}, meta)
	testAssert(t1 == t1)
	testAssert(not (t1 == t2))
	testAssert(not (t1 ~= t1))
	testAssert(t1 ~= t2)
end

do
	testAssert("a" ~= nil)
end

do
	local meta1 = {__eq = function(a, b) return true end}
	local meta2 = {__eq = function(a, b) return false end}
	local t1 = setmetatable({}, meta1)
	local t2 = nil
	testAssert(t1 ~= t2)
	testAssert(t2 ~= t1)
	t2 = {}
	testAssert(t1 ~= t2)
	testAssert(t2 ~= t1)
	t2 = setmetatable(t2, meta2)
	testAssert(t1 ~= t2)
	testAssert(t2 ~= t1)
end

