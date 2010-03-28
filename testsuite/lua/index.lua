t1 = {}
t2 = {}
setmetatable(t1, {__index = t2})
setmetatable(t2, {__index = function() return "the value" end})
testAssert(t1.key == t2.key)
testAssert(t1.key == "the value")

