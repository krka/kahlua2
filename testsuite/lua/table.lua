local t = {}

for i = 1, 100 do
   t[i] = i * 3
end

testAssert(next(t), "next must not be nil")

do
   local c = 0
   local s = ""
   for k, v in pairs(t) do
   	s = s .. ", " .. k .. "=" .. v
      c = c + 1
   end
   testAssert(c == 100, "wrong number of elements in table: was " .. c .. s)
end

testCall(function()
	for k, v in pairs(t) do
	   assert(t[k] == v)
	   assert(t[k] == 3 * k)
	end
end)

local a, b = -1, 0
t[a * b] = -1
t[b] = 1

testAssert(t[a * b] == 1)


rawset(t, "hello", "world")
testAssert(rawget(t, "hello") == "world")
setmetatable(t, {__index = function() return nil end, __newindex = function() end})
testAssert(rawget(t, "hello") == "world")
rawset(t, "hello", "WORLD")
testAssert(rawget(t, "hello") == "WORLD")


do
	local t = {}
	testCall(function()
		for i = 1, 6 do
			for j = 1, 2^i do
				t[i] = i^2
			end
			for k, v in next, t do
				assert(k^2 == v)
				t[k] = nil
			end
		end
	end)
end

function endswith(s1, s2)
	return s1:sub(-#s2, -1) == s2
end


local status, errmsg = pcall(function() local t = {} t[0/0] = 1 end)
testAssert(status == false)
testAssert(endswith(errmsg, "table index is NaN"))

local status, errmsg = pcall(function() local t = {} t[nil] = 1 end)
testAssert(status == false, "status was " .. tostring(status))
testAssert(endswith(errmsg, "table index is nil"))

local status, errmsg = pcall(function() local t = {} next(t, "bad key") end)
testAssert(not status)
testAssert(endswith(errmsg, "invalid key to 'next'"))

do
	t = {1, 2, 3, 4, 5, 6, 7}
	testAssert(#t == 7)
	
	t = {math.cos(1)}
	testAssert(#t == 1)
	
	function f() return 1 end
	t = {f()}
	testAssert(#t == 1)
	
	function f() return 1, 2, 3, 4, 5 end
	t = {f()}
	testAssert(#t == 5)

	t = {1, 2, 3, f()}
	testAssert(#t == 8)

	t = {f(), 1, 2, 3}
	testAssert(#t == 4)

	t = {f(), nil}
	testAssert(#t == 1)
end

do
    if tableconcat==nil then
        tableconcat = table.concat
    end
	local t = {"Hello", "World"}
	testAssert(tableconcat(t) == "HelloWorld")

	t = {"Hello", "World"}
	testAssert(tableconcat(t, " ") == "Hello World")
	
	t = {"Hello", "World"}
	testAssert(tableconcat(t, 1.5) == "Hello1.5World")

	t = {"a", "b", "c"}
	testAssert(tableconcat(t, " ") == "a b c")
	
	t = {"a", "b", "c"}
	testAssert(tableconcat(t, " ", 1, 3) == "a b c")
	
	t = {"a", "b", "c"}
	testAssert(tableconcat(t, " ", 2, 3) == "b c")
	
	t = {"a", "b", "c"}
	testAssert(tableconcat(t, " ", 1, 2) == "a b")
	
	t = {"a", "b", "c"}
	testAssert(tableconcat(t, " ", 1, 1) == "a")
	
	t = {"a", "b", "c"}
	testAssert(tableconcat(t, " ", 100, 99) == "")	
end

do
	local function sortAndVerify(t)
		testCall(function()
			local len = #t
			table.sort(t)
			assert(len == #t)
			if len > 1 then
				local prev = t[1]
				for i = 2, #t do
					local cur = t[i]
					assert(not (cur < prev))
					prev = cur
				end
			end
		end)
	end
	sortAndVerify{1000, 55, [0] = 0}
	sortAndVerify{1000, 55, 10}
	sortAndVerify{1000}
	sortAndVerify{1000, 100, 2000, 200}
	sortAndVerify{1000, 100, 2000, 200, 150}
	sortAndVerify{1, 2, 3, 4, 5, 6}
	sortAndVerify{6, 5, 4, 3, 2, 1}
end

do
	local t = {}
	table.insert(t, 1)
	testAssert(t[1] == 1)
	testAssert(t[2] == nil)

	table.insert(t, 1, 2)
	testAssert(t[1] == 2)
	testAssert(t[2] == 1)
	testAssert(t[3] == nil)

	table.insert(t, 2, 3)
	testAssert(t[1] == 2)
	testAssert(t[2] == 3)
	testAssert(t[3] == 1)
	testAssert(t[4] == nil)

	local v = table.remove(t, 1)
	testAssert(t[1] == 3, "t[1] is "..tostring(t[1])..", expected is 3")
	testAssert(t[2] == 1, "t[2] is "..tostring(t[2])..", expected is 1")
	testAssert(t[3] == nil, "t[3] is "..tostring(t[3])..", expected is nil")
	testAssert(v == 2, "returned value is "..tostring(v)..", expected is 2")
end

