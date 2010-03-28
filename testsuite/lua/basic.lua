function foo()
   testAssert(1.2 == 1.2)
end

function bar()
   testAssert(4.2 == 4.2)
end


foo()
bar()

testAssert("hej" == "hej")

do
	local t = {}
	t.a = true
	testAssert(t.a == true)
	t.a = false
	testAssert(t.a == false)
	t.a = nil
	testAssert(t.a == nil)
end

do
	local function foo(...)
		return select("#", ...)
	end
	testAssert(foo(1, 2, 3) == 3)
end

do
	local a, b = nil, nil
	testAssert(a == b)
	testAssert(rawequal(a, b))
	a = 1.0

	testAssert(not (a == b))
	testAssert(not rawequal(a, b))
	b = 1.0
	testAssert(a == b)
	testAssert(rawequal(a, b))
	b = 2.0
	testAssert(not (a == b))
	testAssert(not rawequal(a, b))
end

do
	local x = 1.2
	local ok, errmsg = pcall(function() (1.2).x = y end)
	testAssert(not ok)
end

do
	function f()
		f()
	end

	local ok, errorMsg = pcall(f)
	testAssert(not ok)
end

do
	local ok, errmsg = pcall(function() error(nil) end)
	testAssert(not ok)
	testAssert(errmsg == nil)
end

do
	local ok, errmsg = pcall(function() error() end)
	testAssert(ok)
end

do
	local t = {}
	local ok, errmsg = pcall(function() error(t) end)
	testAssert(not ok)
	testAssert(errmsg == t)
end



do
	local function test(a, b)
		-- test OP_LT
		if a < b then
			testAssert(true)
		else
			testAssert(false)
		end
		if not (a < b) then
			testAssert(false)
		else
			testAssert(true)
		end
		if b < a then
			testAssert(false)
		else
			testAssert(true)
		end
		if not (b < a) then
			testAssert(true)
		else
			testAssert(false)
		end

		-- test OP_LE
		if a <= b then
			testAssert(true)
		else
			testAssert(false)
		end
		if not (a <= b) then
			testAssert(false)
		else
			testAssert(true)
		end
		if b <= a then
			testAssert(false)
		else
			testAssert(true)
		end
		if not (b <= a) then
			testAssert(true)
		else
			testAssert(false)
		end
	end
	test(1, 2)
	test("1", "2")
end

do
	local ok, errmsg = pcall(function() (nil)() end)
	testAssert(not ok)

	local ok, errmsg = pcall(function() return (nil)() end)
	testAssert(not ok)
end

