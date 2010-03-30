local t = table.newarray()
testAssert(#t == 0)
t[10] = 1
testAssert(#t == 10)
t[10] = nil
testAssert(#t == 0, "was " .. #t)
t[1] = 1
testAssert(#t == 1, "was " .. #t)
t[2] = 2
testAssert(#t == 2, "was " .. #t)
t[1] = nil
testAssert(#t == 2, "was " .. #t)
t[2] = nil
testAssert(#t == 0, "was " .. #t)
t[1000] = nil
testAssert(#t == 0, "was " .. #t)
t[1e10] = nil
testAssert(#t == 0, "was " .. #t)

local t = table.newarray(123)

testAssert(#t == 1)
testAssert(t[1] == 123)

t[3] = 46
testAssert(t[3] == 46)
t[3] = nil
testAssert(t[3] == nil)
t[400] = "test"
testAssert(#t == 400)
testAssert(t[400] == "test")
t[10] = "t10"
t[400] = nil
testAssert(#t == 10)
testAssert(t[10] == "t10")
testAssert(t[400] == nil)


function verifyIter(iter, key, value)
	local k, v = iter()
	assert(key == k, "expected key " .. tostring(key) .. " but got " .. tostring(k))
	assert(value == v, "expected value " .. tostring(value) .. " but got " .. tostring(v))
end


function verify(t)
    testAssert(t[1] == 40, tostring(t[1]))
    testAssert(t[2] == 50, tostring(t[2]))
    testAssert(t[3] == 60, tostring(t[3]))
    testAssert(t[4] == nil)
    testAssert(#t == 3, tostring(#t))

    local iter = pairs(t)
    verifyIter(iter, 1, 40)
    verifyIter(iter, 2, 50)
    verifyIter(iter, 3, 60)
    verifyIter(iter, nil, nil)
    verifyIter(iter, nil, nil)
end

verify(table.newarray{40, 50, 60})
verify(table.newarray(40, 50, 60))
