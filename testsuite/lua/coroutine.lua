testCall("stacktrace should pass through coroutines [coroutine.resume]", function()
	local coro = coroutine.create(function()
		local function foo()
			error("Hello world")
		end
		foo()
	end)
	local status, errormessage, stacktrace = coroutine.resume(coro)
	assert(status == false)
	assert(errormessage == "Hello world")
	assert(stacktrace, "stacktrace was nil")
	assert(type(stacktrace), "string")
	-- Note the exact line number needs to be changed if editing above this line
	assert(stacktrace:find("coroutine.lua:4", 1, 1), "expected to find coroutine.lua:5 in stacktrace: " .. stacktrace) -- should match the line with error("Hello world")
	assert(stacktrace:find("coroutine.lua:6", 1, 1), "expected to find coroutine.lua:6 in stacktrace: " .. stacktrace) -- should match the line with foo()
	assert(not stacktrace:find("stdlib.lua", 1, 1), "expected to NOT find stdlib.lua in stacktrace: " .. stacktrace)
end)

testCall("stacktrace should pass through coroutines [coroutine.wrap]", function()
	local coro = coroutine.wrap(function()
		local function foo()
			error("Hello world")
		end
		foo()
	end)
	local status, errormessage, stacktrace = pcall(coro)
	assert(status == false)
	assert(errormessage == "Hello world")
	assert(stacktrace, "stacktrace was nil")
	assert(type(stacktrace), "string")
	-- Note the exact line number needs to be changed if editing above this line
	assert(stacktrace:find("coroutine.lua:22", 1, 1), "expected to find coroutine.lua:22 in stacktrace: " .. stacktrace) -- should match the line with error("Hello world")
	assert(stacktrace:find("coroutine.lua:24", 1, 1), "expected to find coroutine.lua:24 in stacktrace: " .. stacktrace) -- should match the line with foo()
end)

testCall("illegal yield", function()
	local coro = coroutine.create(function()
		local status, errormessage = pcall(coroutine.yield)
		assert(status == false)
		assert(errormessage:find("Can not yield"), errormessage)
		return "the end"
	end)
	local status, errormessage = coroutine.resume(coro)
	assert(status == true)
	assert(errormessage == "the end")
end)

testCall("coroutine fail", function()
	local coro = coroutine.create(function() error("bye cruel world") end)
	local status1, status2, errormsg = pcall(coroutine.resume, coro)
	assert(status1 == true)
	assert(status2 == false)
	assert(errormsg:find("bye cruel world"))
end)

testCall("coroutine fail tailcall", function()
	local coro = coroutine.create(function() error("bye cruel world") end)
	local function foo()
		return pcall(coroutine.resume, coro)
	end
	local status1, status2, errormsg = foo()
	assert(status1 == true)
	assert(status2 == false)
	assert(errormsg:find("bye cruel world"))
end)

testCall("pcall resume", function()
	local coro = coroutine.create(function() end)
	local status1, status2 = pcall(coroutine.resume, coro)
	assert(status1 == true)
	assert(status2 == true)
end)

testCall("tailcall return to java 1", function()
	local status, status2, a, b, c = pcall(function()
		local coro = coroutine.create(function(a, b, c)
			assert(a == 10)		
			assert(b == 20)		
			assert(c == 30)		
			return 100, 200, 300
		end)
	
		return coroutine.resume(coro, 10, 20, 30)
	end)
	assert(status == true)
	assert(status2 == true)
	assert(a == 100)
	assert(b == 200)
	assert(c == 300)
end)
testCall("tailcall return to java 2", function()
	local status, status2, a, b, c = pcall(function()
		local coro = coroutine.create(function(a, b, c)
			assert(a == 10)		
			assert(b == 20)		
			assert(c == 30)		
			coroutine.yield(100, 200, 300)
		end)
	
		return coroutine.resume(coro, 10, 20, 30)
	end)
	assert(status == true)
	assert(status2 == true)
	assert(a == 100)
	assert(b == 200)
	assert(c == 300)
end)

testCall("tailcall return to java 3", function()
	local status, status2, a, b, c = pcall(function()
		local coro = coroutine.create(function(a, b, c)
			assert(a == 10)		
			assert(b == 20)		
			assert(c == 30)		
			return coroutine.yield(100, 200, 300)
		end)
	
		return coroutine.resume(coro, 10, 20, 30)
	end)
	assert(status == true)
	assert(status2 == true)
	assert(a == 100)
	assert(b == 200)
	assert(c == 300)
end)


testCall("tailcall yield", function()
	local coro = coroutine.create(function()
		local function foo()
		end
		foo()
		return coroutine.yield(1, 2, 3)
	end)
	local status, a, b, c = coroutine.resume(coro)
	assert(status == true)
	assert(a == 1)
	assert(b == 2)
	assert(c == 3)
end)

testCall("tailcall yield + tailcall resume", function()
	local coro2 = coroutine.create(function()
		return coroutine.yield(4, 5, 6)
	end)

	local coro = coroutine.create(function()
		return coroutine.resume(coro2, 1, 2, 3)
	end)
	local status, a, b, c, d, e = coroutine.resume(coro)
	assert(status == true)
	assert(a == true, tostring(a))
	assert(b == 4, tostring(b))
	assert(c == 5, tostring(c))
	assert(d == 6, tostring(d))
	assert(e == nil, tostring(e))
end)

testCall("implicit yield + tailcall resume", function()
	local coro2 = coroutine.create(function()
		return 4, 5, 6
	end)

	local coro = coroutine.create(function()
		return coroutine.resume(coro2, 1, 2, 3)
	end)
	local status, a, b, c, d, e = coroutine.resume(coro)
	assert(status == true)
	assert(a == true, tostring(a))
	assert(b == 4, tostring(b))
	assert(c == 5, tostring(c))
	assert(d == 6, tostring(d))
	assert(e == nil, tostring(e))
end)

testCall("error + tailcall resume", function()
	local coro2 = coroutine.create(function()
		error("error!")
	end)

	local coro = coroutine.create(function()
		return coroutine.resume(coro2, 1, 2, 3)
	end)
	local status, a, b, c, d, e = coroutine.resume(coro)
	assert(status == true)
	assert(a == false, tostring(a))
	assert(type(b) == "string")
	assert(b:find("error!"))
end)

testCall("resume/yield", function()
	local coro = coroutine.create(function()
		local function foo(x)
			return coroutine.yield(x, x + 1, x + 2)
		end
		local a, b, c = foo(1)
		assert(a == 4, "a == 4")
		assert(b == 5, "b == 5")
		assert(c == 6, "c == 6")
		local a, b, c = foo(11)
		assert(a == 14, "a == 14")
		assert(b == 15, "b == 15")
		assert(c == 16, "c == 16")
		return 1, 2, 3
	end)
	assert(coroutine.status(coro) == "suspended")
	
	local status, a, b, c = coroutine.resume(coro)
	assert(status == true, a)
	assert(a == 1)
	assert(b == 2)
	assert(c == 3)
	assert(coroutine.status(coro) == "suspended")
	
	local status, a, b, c = coroutine.resume(coro, 4, 5, 6)
	assert(status == true, a)
	assert(a == 11)
	assert(b == 12)
	assert(c == 13)
	assert(coroutine.status(coro) == "suspended")
	
	local status, a, b, c = coroutine.resume(coro, 14, 15, 16)
	assert(status == true, a)
	assert(a == 1)
	assert(b == 2)
	assert(c == 3)
	assert(coroutine.status(coro) == "dead")

end)

local sqrt = math.sqrt
local function isprime(n)
	if n % 2 == 0 then
		return false
	end
	local max = sqrt(n)
	for i = 3, max, 2 do
		if n % i == 0 then
			return false
		end
	end
	return true
end

function getprimes()
	coroutine.yield(2)
	for i = 3, 1e10, 2 do
		if isprime(i) then
			coroutine.yield(i)
		end
	end
end

local generator = coroutine.wrap(getprimes)
local t = {2,3,5,7,11,13,17,19,23}

--[[
for i = 1, #t do
	local p1 = t[i]
	local p2 = generator()
	assert(p1 == p2, p1 .. " = " .. p2)
end
do return end
--]]

testCall(function()
	local i = 1
	for p in generator do
		local correct = t[i]
		i = i + 1
		assert(p == correct, p .. " ~= " .. correct)
		if p > 10 then
			break
		end
	end
	for p in generator do
		local correct = t[i]
		i = i + 1
		assert(p == correct, p .. " ~= " .. correct)
		if p > 20 then
			break
		end
	end
end)
testCall(function()
	local ok, err, stacktrace = pcall(function()
		local f = coroutine.wrap(function(...)
			assert(select("#", ...) == 3)
			error("test")
		end)
		f(11,22,33)
	end)

	assert(ok == false)
	assert(err:sub(-4, -1) == "test", err)

	local coro = coroutine.create(function()
		error("test")
	end)
	assert(coroutine.status(coro) == "suspended")

	local status, err = coroutine.resume(coro)

	assert(status == false)
	assert(err:sub(-4, -1) == "test")

	assert(coroutine.status(coro) == "dead")
end)

testCall(function()
	local coro = coroutine.create(function(a,b,c)
		assert(a == 11)
		assert(b == 22)
		assert(c == 33)
		return 55, 66, 77
	end)
	assert(coroutine.status(coro) == "suspended")

	local ok, x, y, z = coroutine.resume(coro, 11, 22, 33)
	assert(ok == true)
	assert(x == 55)
	assert(y == 66)
	assert(z == 77)

	assert(coroutine.status(coro) == "dead")
end)

testCall(function()
	local coro = coroutine.create(function(a,b,c)
		assert(a == 11)
		assert(b == 22)
		assert(c == 33)
		coroutine.yield(55, 66, 77)
		return 1, 2, 3
	end)
	local ok, x, y, z = coroutine.resume(coro, 11, 22, 33)
	assert(ok == true)
	assert(x == 55)
	assert(y == 66)
	assert(z == 77)

	assert(coroutine.status(coro) == "suspended")

	ok, x, y, z = coroutine.resume(coro)
	assert(ok == true)
	assert(x == 1)
	assert(y == 2)
	assert(z == 3)

	assert(coroutine.status(coro) == "dead")
end)

testCall(function()
	local coro = coroutine.create(function()
		coroutine.yield()
		coroutine.yield()
	end)
	assert(coroutine.status(coro) == "suspended", "got status: " .. coroutine.status(coro) .. ", expected suspended")
	coroutine.resume(coro)
	assert(coroutine.status(coro) == "suspended", "got status: " .. coroutine.status(coro) .. ", expected suspended")
	coroutine.resume(coro)
	assert(coroutine.status(coro) == "suspended", "got status: " .. coroutine.status(coro) .. ", expected suspended")
	coroutine.resume(coro)
	assert(coroutine.status(coro) == "dead", "got status: " .. coroutine.status(coro) .. ", expected dead")
end)

testCall(function()
	local coro = coroutine.create(function()
		local yield = coroutine.yield
		setfenv(0, {})
		yield()
	end)
	assert(coroutine.status(coro) == "suspended", "got status: " .. coroutine.status(coro) .. ", expected suspended")
	coroutine.resume(coro)
	assert(coroutine.status(coro) == "suspended", "got status: " .. coroutine.status(coro) .. ", expected suspended")
	coroutine.resume(coro)
	assert(coroutine.status(coro) == "dead", "got status: " .. coroutine.status(coro) .. ", expected dead")
end)

testCall(function()
	local t = setmetatable(getfenv(0), {__tostring = function() return "original" end})
	assert(tostring(t) == "original")
	local coro = coroutine.create(function()
		assert(t == getfenv(0))
		assert(tostring(t) == "original")
		setfenv(0, setmetatable({}, {__index = t, __tostring = function() return "coro" end}))
		assert(t ~= getfenv(0))
		assert(tostring(t) == "original")
		assert(tostring(getfenv(0)) == "coro")
		coroutine.yield()
		assert(t ~= getfenv(0))
		assert(tostring(t) == "original")
		assert(tostring(getfenv(0)) == "coro")
	end)
	assert(coroutine.status(coro) == "suspended", "got status: " .. coroutine.status(coro) .. ", expected suspended")
	coroutine.resume(coro)
	assert(coroutine.status(coro) == "suspended", "got status: " .. coroutine.status(coro) .. ", expected suspended")
	assert(getfenv(0) == t)
	assert(tostring(getfenv(0)) == "original")
	coroutine.resume(coro)
	assert(coroutine.status(coro) == "dead", "got status: " .. coroutine.status(coro) .. ", expected dead")
	assert(getfenv(0) == t)
	assert(tostring(getfenv(0)) == "original")
end)

testCall(function()

    local f = function()
        return 1, 2
    end
    local co = coroutine.create(function() return f() end)
    local status, a, b = coroutine.resume(co)
    assert(status == true)
    assert(a == 1, "expected 1 but got " .. a)
    assert(b == 2, "expected 2 but got " .. a)
end)

