local mt = {}
local t = setmetatable({}, mt)

function mt.__call(caller, ...)
	assert(caller == t)
	local a, b, c, d = ...
	assert(a == 1)
	assert(b == 2)
	assert(c == 3)
	assert(d == nil)
	return 123
end

testCall(function()
	assert(t(1, 2, 3) == 123)
end)

function foo()
	return t(1, 2, 3)
end

testCall(function()
	assert(foo() == 123)
end)
