testCall(function()
	local t = {}
	for i = 1, 1e7, 10000 do
		t[i] = i
		t[i] = nil
	end
end)

