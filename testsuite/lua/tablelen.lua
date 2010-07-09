local t = {}
for i = 5, 1000 do
	t[i] = i
	local len = #t
	assert(len == i or len == 0, "bad #t operator for table with keys only in range 5.." .. i)
end


