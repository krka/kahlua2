local coro = coroutine.create(function()
	error"foo"
end)
coroutine.resume(coro)

