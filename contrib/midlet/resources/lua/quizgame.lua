local t = {
	{topic = "Lua", question = "What is the best programming language?", correct = "Lua", wrong = {"Basic", "Lisp", "Java"}},
	{topic = "Lua", question = "What is the worst programming language?", correct = "Basic", wrong = {"Lua", "Lisp", "Java", "Pascal", "Haskell"}},
}

local score, total = 0, 0
while true do
	local q = t[math.random(1, #t)]
	local correct = q.correct
	local topic = q.topic
	local question = q.question
	local answers = {q.correct, unpack(q.wrong)}
	for i = 1, 3 * #answers do
		local a, b = math.random(1, #answers), math.random(1, #answers)
		answers[a], answers[b] = answers[b], answers[a]
	end
	local response = query(topic .. ": ", question, unpack(answers))
	total = total + 1
	local s = "Wrong! "
	if response == tostring(correct) then
		score = score + 1
		s = "Correct! "
	end
	response = query(s, "Current score: " .. score .. "/" .. total, "Next question", "Quit")
	if response == "Quit" then
		return
	end
end

