local two = 2
local fivehalfs = 2.5
testAssert(4 % two == 0)
testAssert(3 % two == 1)
testAssert(3 % fivehalfs == 0.5)

testCall(function()
   local c = 0
   for i = 1, 100 do
      c = c + 1
      if c == 10 then
	 c = 0
      end
      assert(i % 10 == c)
   end
end)
