function isprime(n)
    for i = 3, math.sqrt(n), 2 do
        if n % i == 0 then
            return false
        end
    end
    return true
end

function getprimes()
    coroutine.yield(2)
    for i = 3, math.huge, 2 do
        if isprime(i) then
            coroutine.yield(i)
        end
    end
end

local primegenerator = coroutine.wrap(getprimes)

for i = 1, 1e6, 10 do
	local s = ""
	for j = 1, 10 do
		s = s .. primegenerator()
		if j < 10 then
			s = s .. ", "
		end
	end
	local response = query("Primes " .. i .. " to " .. (i + 9) .. ": ", s, "Next 10 primes", "Quit")
	if response == "Quit" then
		return
	end
end

