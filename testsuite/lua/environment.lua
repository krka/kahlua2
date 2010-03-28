local meta = {}
meta.__index = _G

env1 = setmetatable({}, meta)
function f1()
	x = "f1"
	testAssert(getfenv() == env1)
	
	testAssert(getfenv(0) == _G)
	testAssert(getfenv(1) == env1)
	testAssert(getfenv(2) == env2)
	testAssert(getfenv(3) == env3)
	testAssert(getfenv(4) == env4)
	
	testAssert(getfenv(f1) == env1)
	testAssert(getfenv(f2) == env2)
	testAssert(getfenv(f3) == env3)
	testAssert(getfenv(f4) == env4)
end
setfenv(f1, env1)

env2 = setmetatable({}, meta)
function f2()
	x = "f2"
	f1()
	testAssert(x == "f2")
end
setfenv(f2, env2)

env3 = setmetatable({}, meta)
function f3()
	x = "f3"
	f2()
	testAssert(x == "f3")
end
setfenv(f3, env3)

env4 = setmetatable({}, meta)
function f4()
	x = "f4"
	f3()
	testAssert(x == "f4")
end
setfenv(f4, env4)

testAssert(x == nil)
f4()
testAssert(x == nil)

