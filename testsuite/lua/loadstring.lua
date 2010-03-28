local f = loadstring[[
local function fac(n)
	if n <= 0 then
		return 1
	end
	return n * fac(n - 1)
end
return fac(7)
]]
testAssert(f() == 5040)

