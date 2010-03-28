local ok, ret = pcall(function() return t.a.b end)
testAssert(not ok)

ok, ret = pcall(error)
testAssert(ok)

ok, ret = pcall(error, "")
testAssert(not ok)

local ok, msg, stacktrace = pcall(function() assert(false, "errmsg") end)
testAssert(not ok)
testAssert(msg == "errmsg")
testAssert(type(stacktrace) == "string")

testAssert(select(2, 4,5,6) == 5)
testAssert(select("#") == 0)
testAssert(select("#",7,8,9,10) == 4)
testAssert(select("#", select(2, 4,5,6,7,8)) == 4)

local t = {10,20,30,40}
testAssert(select("#", unpack(t)) == 4)
testAssert(select("#", unpack(t, 1, #t)) == 4)
testAssert(select("#", unpack(t, 1, 3)) == 3)
testAssert(select("#", unpack(t, 1, 10)) == 10)
testAssert(select("#", unpack(t, -10, 10)) == 21)

