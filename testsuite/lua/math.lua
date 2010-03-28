function isInf(x)
	return x + 1 == x
end

function isPosInf(x)
	return isInf(x) and x > 0
end

function isNegInf(x)
	return isInf(x) and x < 0
end

function isNaN(x)
	return not (x == x) and not isInf(x)
end
	

testCall(function() assert(tonumber("12345678e20") > 10e20, tonumber("12345678e20") .. " was not larger than " .. 10e20) end)

testCall(function() assert(tonumber("12345678e20") > 10e20) end)

testAssert(1 < 2, "1 must be less than 2")
testAssert(not (2 < 1), "2 must not be less than 1")

do
	local value1 = 1.234567
	local value2 = 1.234567
	value2 = value2 * 2
	value2 = value2 / 2

	testAssert(value1 == value2)
	testAssert(rawequal(value1, value2))
	
	local t = {}
	t[value1] = 1
	t[value2] = 2
	testAssert(t[value1] == 2)
	testAssert(t[value2] == 2)

	local zero = 0
	value1 = 1 * zero
	value2 = -1 * zero
	t[value1] = 1
	t[value2] = 2
	testAssert(t[value1] == 2)
	testAssert(t[value2] == 2)
end

function assertEquals(a, b)
	local errMsg = "expected " .. tostring(a) .. " = " .. tostring(b)
	local assert = assert
	local type = type
	assert(type(a) == type(b), "not same type")
	if (type(a) == "number") then
		local diff = math.abs(a - b)
		local max = math.max(a, b)
		assert((diff / max < 1e-8) or (diff < 1e-7), errMsg)
	else
		assert(a == b, errMsg)
	end
end

function testAssertEquals(a, b)
	testCall(function() assertEquals(a, b) end)
end

testAssertEquals(1, 1)
testAssertEquals(2.123, 2.123)

testAssertEquals(math.cos(1), 0.540302306)
testAssertEquals(math.cos(0.234), 0.972746698)


testAssertEquals(math.sin(0.234), 0.231870355)

do
   local x = 123
   testAssertEquals(-x, -123)
end

testAssertEquals(true, not not true)
testAssertEquals(true, not not 1)
testAssertEquals(false, not 0)


do
   local a = 1
   local b = 2
   local c = 0
   testAssert(a / c == b / c, "+inf broken")
   testAssert(-a / c == -b / c, "-inf broken")

   local s = tostring(c % c):lower()
   testAssert(s == "nan", "0 % 0 was: " .. s)

end

do
	testCall(function()
		for i = 1, 10, 0.1 do
			assertEquals(math.sqrt(i^2), i)
		end
	end)	
end

do
	testCall(function()
		for i = 1, 10, 0.1 do
			assertEquals(3^(-i), 1/(3^i))
		end
	end)	
	
	testAssert(isNaN((-123)^1.1))

	testCall(function()
		local mult = 1	
		for i = 0, 20 do
			local x = 1.123
			assertEquals(x ^ i, mult)
			mult = mult * x;
		end
	end)	
end

do
	testAssert(isPosInf(math.floor(1/0)))
	testAssert(isNegInf(math.floor(-1/0)))
	testAssert(isNaN(math.floor(0/0)))
	testAssert(isPosInf(math.ceil(1/0)))
	testAssert(isNegInf(math.ceil(-1/0)))
	testAssert(isNaN(math.ceil(0/0)))

	testCall(function()
		for i = -2, 2, 0.01 do
			local f = math.floor(i)
			local c = math.ceil(i)
			assert(c >= f)
			if c == f then
				assert(i == c)
				assert(i == f)
			end
		end
	end)
end

do	
	local a, b
	a, b = math.modf(1 / 0)
	testAssert(isPosInf(a))
	testAssert(b == 0)
	
	a, b = math.modf(-1 / 0)
	testAssert(isNegInf(a))
	testAssert(b == 0)

	a, b = math.modf(0 / 0)
	testAssert(isNaN(a))
	testAssert(isNaN(b))

	a, b = math.modf(-2.5)
	testAssert(a == -2)
	testAssert(b == -.5)

	testCall(function()
		for i = -2, 2, 0.01 do
			local ipart, fpart = math.modf(i)
			assert(ipart + fpart == i)
		end
	end)
end

local function assertInterval(value, low, high)
	assert(value >= low)
	assert(value <= high)
end

testCall(function()
	for i = -10, 10, 0.01 do
		assertInterval(math.cos(i), -1, 1)
		assertInterval(math.sin(i), -1, 1)
	end
end)

testCall(function()
	for i = -1, 1, 0.01 do
		assertInterval(math.acos(i), -math.pi, math.pi)
		assertInterval(math.asin(i), -math.pi, math.pi)
	end
end)
do
	local v
	v = math.acos(1.01) testAssert(isNaN(v), "expected NaN, got " .. v)
	v = math.acos(-1.01) testAssert(isNaN(v), "expected NaN, got " .. v)
	v = math.asin(1.01) testAssert(isNaN(v), "expected NaN, got " .. v)
	v = math.asin(-1.01) testAssert(isNaN(v), "expected NaN, got " .. v)
end

testCall(function()
	for i = -10, 10, 0.01 do
		local v1 = math.cos(i) ^ 2 + math.sin(i) ^ 2
		assertEquals(v1, 1)
	end
end)

testCall(function()
	for i = 0, math.pi, 0.01 do
		assertEquals(math.acos(math.cos(i)), i)
	end
end)

testCall(function()
	for i = -math.pi / 2, math.pi / 2, 0.01 do
		assertEquals(math.asin(math.sin(i)), i)
		assertEquals(math.atan(math.tan(i)), i)
	end
end)

testCall(function()
	for i = -100, 100, 0.5 do
		assertEquals(math.tan(math.atan(i)), i)
	end
end)

testAssert(math.atan(0) == 0)
testAssertEquals(math.atan(1 / 0), math.pi/2)
testAssertEquals(math.atan(-1 / 0), -math.pi/2)
testAssertEquals(math.atan2(1 / 0, 123), math.pi/2)
testAssertEquals(math.atan2(-1 / 0, 123), -math.pi/2)

testAssertEquals(math.atan2(2, 123), 0.01625872980513)
testAssertEquals(math.atan2(2, -123), 3.1253339237847)

testAssertEquals(math.atan2(2, 3), 0.58800260354757)
testAssertEquals(math.atan2(2, -3), 2.5535900500422)
testAssertEquals(math.atan2(-2, 3), -0.58800260354757)
testAssertEquals(math.atan2(-2, -3), -2.5535900500422)

testAssert(isNaN(math.sin(1 / 0)))
testAssert(isNaN(math.cos(1 / 0)))
testAssert(isNaN(math.tan(1 / 0)))

do
	local v = math.atan2(1, 0)
	testCall(function()
		for i = 0, 10 do
			assert(0 == math.atan2(0, i))
		end
	end)
	testCall(function()
		for i = 1, 20 do
			assertEquals(v, math.atan2(i, 0))
			assert(math.atan2(i, 2) == math.atan(i / 2))
		end
	end)
end

do
	testCall(function()
		for i = -10, 10, 0.1 do
			assertEquals(math.cosh(i)^2 - math.sinh(i)^2, 1)
			assertEquals(math.sinh(-i), -math.sinh(i))
			assertEquals(math.cosh(-i), math.cosh(i))
			assertEquals(math.tanh(i), math.sinh(i) / math.cosh(i))
		end
	end)
end

do
	testAssert(isNaN(math.rad(0/0)))
	testAssert(isNaN(math.deg(0/0)))
	testAssert(isPosInf(math.rad(1/0)))
	testAssert(isPosInf(math.deg(1/0)))
	testAssert(isNegInf(math.rad(-1/0)))
	testAssert(isNegInf(math.deg(-1/0)))
	
	testAssertEquals(math.pi, math.rad(180))
	testAssertEquals(360, math.deg(2 * math.pi))
	
	testCall(function()
		for i = 1, 100, 0.1 do
			assertEquals(i, math.rad(math.deg(i)))
			assertEquals(i, math.deg(math.rad(i)))
		end
	end)
end

do
	local a, b
	a, b = math.frexp(0/0)
	testAssert(isNaN(a) and b == 0)
	a, b = math.frexp(1/0)
	testAssert(isPosInf(a) and b == 0)
	a, b = math.frexp(-1/0)
	testAssert(isNegInf(a) and b == 0)

	testAssert(isPosInf(math.ldexp(1/0, 1)))
	testAssert(isNegInf(math.ldexp(-1/0, 1)))
	testAssert(isNaN(math.ldexp(0/0, 1)))

	testCall(function()
		for i = -10, 10, 0.1 do
			assertEquals(i, math.ldexp(math.frexp(i)))
			assertEquals(i, math.ldexp(i, 0/0))
			assertEquals(i, math.ldexp(i, 1/0))
			assertEquals(i, math.ldexp(i, -1/0))
		end
	end)
end

do
	testAssert(isNaN(math.fmod(0/0, 0/0)))
	testAssert(isNaN(math.fmod(0/0, 2)))
	testAssert(isNaN(math.fmod(2, 0/0)))
	testAssert(isNaN(math.fmod(1/0, 0/0)))
	testAssert(isNaN(math.fmod(1/0, 1/0)))
	testAssert(isNaN(math.fmod(-1/0, 0/0)))
	testAssert(isNaN(math.fmod(-1/0, 1/0)))
	
	testCall(function()
		for i = 1, 10 do
			assert(i == math.fmod(i, 1/0))
			assert(i == math.fmod(i, -1/0))
		end
	end)
	
	testCall(function()
		for i = 0.1, 10, 0.5 do
			assertEquals(math.fmod(i, 0.5), 0.1)
		end
	end)
	testCall(function()
		for i = -10.1, 0, 0.5 do
			assertEquals(math.fmod(i, 0.5), -0.1)
		end
	end)
	testCall(function()
		for i = 0.1, 10, 0.5 do
			assertEquals(math.fmod(i, -0.5), 0.1)
		end
	end)
	testCall(function()
		for i = -10.1, 0, 0.5 do
			assertEquals(math.fmod(i, -0.5), -0.1)
		end
	end)
end

-- test exp
do
	local e = math.exp(1)
	local x = 1
	for i = 1, 10 do
		x = x * e
		assertEquals(math.exp(i), x)
	end
end

-- test log
do
	testAssert(isNaN(math.log(-1)))
	testAssert(isNegInf(math.log(0)))
	testAssert(isPosInf(math.log(1/0)))
	
	local x = 1
	local e = math.exp(1)
	for i = 1, 10 do
		x = x * e
		assertEquals(math.log(x), i)
		assertEquals(math.log(1 / x), -i)
		
		assertEquals(math.log10(x), math.log(x) / math.log(10))
	end
end

testAssertEquals(math.pow(1.234, 10.170355), 8.48608917)


