t1 = {}
t2 = {}
setmetatable(t1, {__call = t2})
setmetatable(t2, {__call = function() return "hello world" end})
testAssert(pcall(t1) == false)
testAssert(t2() == "hello world")

testCall(function()
	local function bar(a,b,c,d,e)
		return a+b+c+d+e, a+b+c+d, a+b+c, a+b, a
	end
	
	local function foo(a,b,c,d,e)
		return bar(10*e,10*d,10*c,10*b,10*a)
	end
	
	local a,b,c,d,e = foo(1,2,3,4,5)
	assert(a == 150)
	assert(b == 140)
	assert(c == 120)
	assert(d == 90)
	assert(e == 50)
end)

testCall(function()
	function foo()
		return select(1, foo, 1, 2, 3)
	end
	local a,b,c,d,e = foo()
	assert(a == foo)
	assert(b == 1)
	assert(c == 2)
	assert(d == 3)
	assert(e == nil)
	assert(foo() == foo)
end)

testCall(function()
	local t = {}
	function t:foo()
		return select(1, self, 1, 2, 3)
	end
	local a,b,c,d,e = t:foo()
	assert(a == t)
	assert(b == 1)
	assert(c == 2)
	assert(d == 3)
	assert(e == nil)
	assert(t:foo() == t)
	assert(t:foo():foo() == t)
	assert(t:foo():foo():foo() == t)
end)

