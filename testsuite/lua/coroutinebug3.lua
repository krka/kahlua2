pcall(
	function()
		coro = coroutine.create(
			function()
				coroutine.yield(123)
			end)
	end)

local ok1, ok2, val = pcall(function()
	return coroutine.resume(coro)
end)

print(ok1, ok2, val)
assert(ok1 == true)
assert(ok2 == true)
assert(val == 123)

