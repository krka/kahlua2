local sum = 0
testCall(function()
	for i = 1, 10 do
		assert(i > 0)
		sum = sum + i
	end
end)

testAssert(sum == 55)

