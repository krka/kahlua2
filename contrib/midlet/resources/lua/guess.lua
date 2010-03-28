local query = query
local STR_CORRECT = "You guessed right"
local STR_LOWER = "My number is lower!"
local STR_HIGHER = "My number is higher!"

local response = query("", "Think of an integer between 1 and 100, I will guess which it is!", "Ready", "Quit")
if response == "Quit" then
	return
end

local low, high = 1, 100
local attempt = 1
for tmp = 1, 1e6 do
	if high < low then
		query("", "You are cheating! I dont want to play with you.", "Ok, I admit it!")
		return
	end
	local guess = math.floor((low + high) / 2)
	if high - low > 10 then
		guess = math.random(guess - 3, guess + 3)
	end
	local response = query("", "My guess #" .. attempt .. " is " .. guess, STR_CORRECT, STR_LOWER, STR_HIGHER)
	if response == STR_CORRECT then
		query("", "I am the greatest!", "Ok")
		return
	end
	if response == STR_LOWER then
		high = guess - 1
	elseif response == STR_HIGHER then
		low = guess + 1
	end
	attempt = attempt + 1
end

