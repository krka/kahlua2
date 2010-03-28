local t = {}
testAssert(#t == 0, "expected " .. 0 .. ", got " .. #t)
testCall(function()
	for i = 1, 100 do
		t[i] = i
		testAssert(#t == i, "expected " .. i .. ", got " .. #t)
	end
end)

setmetatable(t, {__len = function() return 123 end})
testAssert(#t == 100, "expected " .. 100 .. ", got " .. #t)

