local coro = coroutine.create(function()
	for i = 1, 10 do
		coroutine.yield("a", "b")
	end
end)
for i = 1, 10 do
	local ok, a, b = coroutine.resume(coro)
	assert(ok == true)
	assert(a == "a")
	assert(b == "b")
end

