local f = function(a, b, c)
    return nil, a, b, c
end

local value = f(1, 2, 3, 4)
testAssert(value == nil) -- Fail: returns 4
testAssert(f(1, 2, 3) == nil) --ok

