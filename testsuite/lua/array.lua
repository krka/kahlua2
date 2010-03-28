a = array.new()
testAssert(a)
testAssert(#a == 0)

testAssert(getmetatable(a) == "restricted")
testAssert(array.push)
testAssert(a.push)

testAssert(type(a.push) == "function")
a:push(40)
a:push(50)
a:push(60)
a:push(70)

testAssert(#a == 4)

testAssert(a[2] == 60)
testAssert(a[3] == 70)
a[3] = 90

testAssert(a[3] == 90)

