local x = newobject();
local y = newobject();

testAssert(getmetatable(x) == nil)
testAssert(getmetatable(y) == nil)

--[[
-- test properties
local foo = withproperties(newobject())
testAssert(foo.foo == nil)
foo.foo = "hello"
testAssert(foo.foo == "hello")

-- test recursive properties
local bar = newobject()
setmetatable(bar, {__index = foo})
bar = withproperties(bar)
testAssert(bar.bar == nil)
testAssert(bar.foo == "hello")
bar.foo = "goodbye"
testAssert(bar.foo == "goodbye")
testAssert(foo.foo == "hello")

-- test one metatable per object
local mt = {}
local x2 = setmetatable(x, mt)
testAssert(x == x2)
testAssert(getmetatable(x) == mt)
testAssert(getmetatable(y) == nil)
--]]



-- test Java calls
do
	local t = {}
	local a, b, c, tt, na = luareturnparam(t, 1, 2, 3)
	testAssert(a == 3 and b == 2 and c == 1 and tt == t and na == 4)
	
    local mt = {__call = luareturnparam}
	setmetatable(t, mt)
	a, b, c, tt, na = t(1, 2, 3)
	testAssert(a == 3 and b == 2 and c == 1 and tt == t and na == 4)
end
