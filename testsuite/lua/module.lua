--[[
testCall(function()
	local oldEnv = getfenv(1)
	module("module1", package.seeall)
	local newEnv = getfenv(1)
	assert(oldEnv ~= newEnv)
	assert(getmetatable(newEnv).__index == oldEnv)
	assert(type(module1) == "table")
	assert(type(newEnv.module1) == "table")

	module1Global = "Test global variable"
	assert(module1Global == "Test global variable")
	assert(newEnv.module1Global == "Test global variable")
	assert(oldEnv.module1Global == nil)

end)

testAssert(module1Global == nil)
testAssert(type(module1) == "table")

testCall(function()
	assert(module2 == nil)
	
	local oldEnv = getfenv(1)
	module("module2.a.b.c.d", package.seeall)
	local newEnv = getfenv(1)
	assert(oldEnv ~= newEnv)
	assert(getmetatable(newEnv).__index == oldEnv)
	assert(type(module2) == "table")
	assert(type(module2.a) == "table")
	assert(type(module2.a.b) == "table")
	assert(type(module2.a.b.c) == "table")
	assert(type(module2.a.b.c.d) == "table")
	assert(type(package.loaded["module2.a.b.c.d"]) == "table")
	
	assert(module2.a.b.c.d._NAME == "module2.a.b.c.d")
	assert(module2.a.b.c.d._M == module2.a.b.c.d)
	assert(module2.a.b.c.d._PACKAGE == "module2.a.b.c.", module2.a.b.c.d._PACKAGE .. " = " .. "module2.a.b.c.")
end)
--]]
