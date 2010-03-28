function foo()
   return 1,2,3
end

local a,b,c,d = foo()
testAssert(a == 1)
testAssert(b == 2)
testAssert(c == 3)
testAssert(d == nil)

