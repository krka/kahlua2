function assertEqual(actual, expected, msg)
	assert(expected == actual, msg or "expected " .. tostring(expected) .. ", actual " .. tostring(actual)) 
end

function testAssertEqual(a, b, msg, name)
	testCall(name, function() assertEqual(a, b, msg) end)
end

local num = 1234.56789
local str = '3'
local str1,str2,str3 = '65','66','67'

--[[
functions not tested here are implemented 
in lua and do not need to wory about explicit
type coercion.
]]--

--[[
Test string lib functions
]]--
testCall('string.sub',string.sub,num,1,3)
--testAssertEqual("123",string.sub(num,1,3))

testCall('string.char',string.char,str1,str2,str3)
--testAssertEqual("ABC",string.char(str1,str2,str3))

testAssertEqual(string.byte('6'),string.byte(6))

testCall("string.lower",string.lower,num)
testCall("string.upper",string.upper,num)

testCall("string.reverse",string.reverse,num)
--testAssertEqual('98765.4321',string.reverse(num))

testCall('string.format',string.format,'%d',num)
--testAssertEqual(tostring(num),string.format('%d',num))

testCall('string.find',string.find,num,34)
--testAssertEqual(3,string.find(num,34))

testCall('string.gsub',string.gsub,num,123,456,'1')
testAssertEqual('4564.56789',string.gsub(num,123,456,'1'))

--[[
Test math lib functions
]]--

testCall('math.abs',math.abs,str)
testCall('math.asin',math.asin,str)
testCall('math.acos',math.acos,str)
testCall('math.atan',math.atan,str)
testCall('math.atan2',math.atan2,str,str)
testCall('math.ceil',math.ceil,str)
testCall('math.cos',math.cos,str)
testCall('math.cosh',math.cosh,str)
testCall('math.deg',math.deg,str)
testCall('math.exp',math.exp,str)
testCall('math.floor',math.floor,str)
testCall('math.fmod',math.fmod,str,str)
testCall('math.frexp',math.frexp,str)
testCall('math.ldexp',math.ldexp,str,str)
testCall('math.log',math.log,str)
testCall('math.log10',math.log10,str)
testCall('math.modf',math.modf,str)
testCall('math.pow',math.pow,str,str)
testCall('math.rad',math.rad,str)
testCall('math.random',math.random,str,str)
testCall('math.randomseed',math.randomseed,str)
testCall('math.sin',math.sin,str)
testCall('math.sinh',math.sinh,str)
testCall('math.sqrt',math.sqrt,str)
testCall('math.tan',math.tan,str)
testCall('math.tanh',math.tanh,str)


--==================================================
-- test normal arithmetic/concatenation operations.
--==================================================
testCall("4=='2'+'2'",function() assert(4=='2'+'2') end)
testCall("0=='2'-'2'",function() assert(0=='2'-'2') end)
testCall("1=='2'/'2'",function() assert(1=='2'/'2') end)
testCall("4=='2'*'2'",function() assert(4=='2'*'2') end)

testCall("0=='2'%'2'",function() assert(0=='2'%'2') end)
testCall("4=='2'^'2'",function() assert(4=='2'^'2') end)
testCall("-5==-'5'",function() assert(-5==-'5') end)

testCall('"1234"=="12"..34',function() assert("1234"=='12'..34) end)