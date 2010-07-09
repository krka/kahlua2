local ok = pcall(function()
	coro = coroutine.create(function()
		coroutine.yield(123)
	end)
end)
assert(ok == true)

--print"created"

ok, err = pcall(function()
	local ok, value = coroutine.resume(coro)
	--print("after resume", ok, value)
	assert(ok == true)
	assert(value == 123)
end)

--print(ok, err)
assert(ok == true)


