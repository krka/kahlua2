mt = {}
mt.__tostring = function() return "hi" end
function mt.__concat(a, b)
   if type(a) == "table" then
      a = a[1]
   end
   if type(b) == "table" then
      b = b[1]
   end
   return a .. b
end
t1 = {"t1"}
t2 = {"t2"}
testCall(function()
	for k, v in pairs(t1) do
		assert(k == 1)
		assert(v == "t1")
	end
end)
setmetatable(t1, mt)
setmetatable(t2, mt)


testAssert(tostring(t1) == "hi")
--[[
assert(tostring("1.2") == "1.2")
assert(tostring(1.2) == "1.2")
assert(tostring(true) == "true")
assert(tostring(false) == "false")


local a = 1.2
local b = 3.4

assert(a .. "x" == "1.2x")
assert(a .. b == "1.23.4")
assert(a .. b .. "x" == "1.23.4x")
assert(a .. "x" .. b == "1.2x3.4")
assert(a .. b .. a .. b == "1.23.41.23.4")
assert(t1 .. t2 == "t1t2")
assert("x" .. t1 .. t2 == "xt1t2")
assert("x" .. t1 .. t2 .. "y" == "xt1t2y")
assert("x" .. t1 .. "z" .. t2 .. "y" == "xt1zt2y")

--]]
