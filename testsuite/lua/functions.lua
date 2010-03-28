function a(x,y,z)
   return x, y, z
end

local x,y,z = a(5,7,9)
testAssert(x == 5)
testAssert(y == 7)
testAssert(z == 9)

x, y = a(6,8,10)
testAssert(x == 6)
testAssert(y == 8)
testAssert(z == 9)

local z2 = 0
x,y,z,z2 = a(10, 20, 30)
testAssert(x == 10)
testAssert(y == 20)
testAssert(z == 30)
testAssert(z2 == nil)

