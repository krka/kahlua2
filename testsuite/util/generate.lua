print[[
function testformat(expected, template, ...)
	local t = {...}
	testCall("string.format: " .. template, function()
		local output = string.format(template, unpack(t))

		local inputs = ""
		for i = 1, #t do
			inputs = string.format("%s, %q", inputs, t[i])
		end

		local msg = string.format("string.format(%q%s) == %q, expected %q", template, inputs, output, expected)
		assert(output == expected, msg)
	end)
end
]]

function sortstring(s)
	local t = {string.byte(s, 1, #s)}
	table.sort(t)
	return string.char(unpack(t))
end

local parameters = {"0", "1", "123", "123.456", "12345678", "0.00000000000123456", "0.123456", "0.00001", "0.99", "2.5", "3.5", "nan", "inf"}
local widths = {"", "1", "2", "6", "50"}
local precisions = {"", ".", ".0", ".1", ".2", ".6", ".10"}
local specifiers = {"d", "e", "g", "f", "u", "o", "x"}

local floatspecifier = {}
floatspecifier.f = true
floatspecifier.e = true
floatspecifier.g = true

local flags = {"0", "#", "+", "-"}

local flagpermutations = {[""] = true}
while true do
	local done = true
	for permutation in next, flagpermutations do
		for j, v in pairs(flags) do
			local newpermutation = sortstring(permutation .. v)
			if (not permutation:find(v)) and not flagpermutations[newpermutation] then
				flagpermutations[newpermutation] = true
				done = false
			end
		end
	end
	if done then
		break
	end
end
local flags = {}
for k, v in next, flagpermutations do
	flags[#flags + 1] = k
end
table.sort(flags)
for i, v in ipairs(flags) do
	--print(v)
end


function addtest(fmt, parameter, negative)
	local result = string.format(fmt, parameter)
	print(string.format("testformat(%q, %q, %q)", result, fmt, parameter))
	if negative then
		addtest(fmt, "-" .. parameter)
	end
end

-------------------------
-- DONE WITH SETUP
-------------------------

print("--Testing widths")
for _, width in ipairs(widths) do
	for _, parameter in ipairs(parameters) do
		addtest("%" .. width .. "e", parameter, true)
	end
end

print("--Testing precisions")
for _, precision in ipairs(precisions) do
	for _, parameter in ipairs(parameters) do
		addtest("%" .. precision .. "f", parameter, true)
		addtest("%" .. precision .. "e", parameter, true)
		addtest("%" .. precision .. "g", parameter, true)
	end
end

print("--Testing %d")
for _, parameter in ipairs(parameters) do
	if (parameter ~= "nan") and (parameter ~= "inf") and (tonumber(parameter) < 1e14) then
		addtest("%d", parameter, true)
	end
end

print("--Testing flags and specifiers")
for _, flag in ipairs(flags) do
	for _, parameter in ipairs(parameters) do
		if (parameter ~= "nan") and (parameter ~= "inf") and (tonumber(parameter) < 1e14) then
			for _, specifier in ipairs(specifiers) do
				addtest("%" .. flag .. specifier, parameter)
			end
		end
	end
end

print("--Testing large number")
for _, flag in ipairs(flags) do
	for _, precision in ipairs(precisions) do
		local parameter = "12345678e20"
		addtest("%" .. flag .. precision .. "e", parameter)
		addtest("%" .. flag .. precision .. "g", parameter)
	end
end

