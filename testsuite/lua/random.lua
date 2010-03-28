-- test ranges
testCall(function()
	for i = 1, 1000 do
		local r = math.random()
		assert(r >= 0)
		assert(r <= 1)
	end
end)

testCall(function()
	for i = 1, 1000 do
		local r = math.random(12)
		assert(r >= 1)
		assert(r <= 12)
		assert(math.floor(r) == r)
	end
end)

testCall(function()
	for i = 1, 1000 do
		local r = math.random(123, 128)
		assert(r >= 123)
		assert(r <= 128)
		assert(math.floor(r) == r)
	end
end)

-- test distribution
testCall(function()
	local t = {}
	for i = 1, 1000 do
		local r = math.random(1, 10)
		t[r] = (t[r] or 0) + 1
	end
	local count = 0
	for i = 1, 10 do
		assert(t[i] > 50)
		count = count + (t[i] or 0)
	end
	assert(count == 1000)
end)

-- test seeding
testCall(function()
	for seed = 1, 1e4, 123 do
		math.randomseed(seed)
		local sequence1 = {}
		for i = 1, 100 do
			sequence1[i] = math.random()
		end
	
		math.randomseed(seed)
		local sequence2 = {}
		for i = 1, 100 do
			sequence2[i] = math.random()
		end
	
		for i = 1, 100 do
			assert(sequence1[i] == sequence2[i])
		end

	end
end)
