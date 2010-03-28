local t = true
local f = false

testAssert(t == t)
testAssert(f == f)
testAssert(not not t == t)
testAssert(not not f == f)
testAssert(t ~= f)

testAssert(t and t)
testAssert(not(t and f))
testAssert(not(f and t))

testAssert(f or t)
testAssert(t or f)

local v0 = 1
local v1 = 1
local v2 = 2

testAssert(v0 <= v1)

testAssert(v1 < v2)
testAssert(v1 <= v2)

