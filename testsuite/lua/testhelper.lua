function mergetests(name, tests)
	local parent = {name = name, failcount = 0, okcount = 0}
	for k, testcase in ipairs(tests) do
		local status = testcase.status
		local category = status and "ok" or "fail"
		parent[category] = parent[category] or {}
		table.insert(parent[category], testcase)
		parent.failcount = parent.failcount + testcase.failcount
		parent.okcount = parent.okcount + testcase.okcount
	end
	parent.status = parent.failcount == 0
	return parent
end

local parent

local function createTestcase(name)
	if type(name) ~= "string" then
		name = debugstacktrace(nil, 3, 1):match("([a-z0-9%.]+:[0-9]+)")
	end
	local testcase = {name = name, okcount = 0, failcount = 0}
	return testcase
end

local function storeTestcase(testcase, status, errormessage, stacktrace)
	testcase.okcount = testcase.okcount + (status and 1 or 0)
	testcase.failcount = testcase.failcount + (status and 0 or 1)

	local category = "ok"
	if status and testcase.fail then
		category = "fail"
		status = false
		errormessage, stacktrace = nil
	elseif not status then
		category = "fail"
		testcase.error = errormessage
		testcase.stacktrace = stacktrace
	end
	testcase.status = status
	
	if parent then
		parent[category] = parent[category] or {}
		table.insert(parent[category], testcase)
		parent.okcount = parent.okcount + testcase.okcount
		parent.failcount = parent.failcount + testcase.failcount
	end
	assert(type(testcase) == "table")
	return testcase
end


function testCall(name, f, ...)
	if type(name) == "function" then
		name, f = f, name
	end
	assert(type(f) == "function", "expected a function, but got " .. tostring(f))

	local testcase = createTestcase(name)
	
	local oldParent = parent
	parent = testcase

	local status, errormessage, stacktrace = pcall(f, ...)
	if status then
		errormessage, stacktrace = nil, nil
	end
	parent = oldParent
	
	local stacktrace2 = debugstacktrace(nil, 3, nil, 1)
	assert(stacktrace == nil or type(stacktrace) == "string", type(stacktrace))
	assert(type(stacktrace2) == "string")
	storeTestcase(testcase, status, errormessage, (stacktrace or "") .. stacktrace2)

	assert(type(testcase) == "table")
	return testcase
end

function testAssert(name, condition, errormessage)
	if type(name) ~= "string" then
		condition, errormessage = name, condition
	end
	if not errormessage then
		errormessage = "Assertion failed"
	end
	local testcase = createTestcase(name)
	storeTestcase(testcase, condition, errormessage, debugstacktrace(nil, 2, nil, 1))
end

local template, template_test

function string:gsubplain(pattern, repl, n)
	return self:gsub(pattern, string.gsub(repl, "%%", "%%%%"), n)
end

local append = table.insert

local function indentstring(indent, s)
	local tmp = string.gsub(s, "\n", "\n" .. indent)
	return indent .. tmp:sub(1, #tmp - #indent)
end

function generatereport(tests)	
	local printTest
	local function printSubtests(test, output, indent)
		if test.fail then
			for k, v in ipairs(test.fail) do
				printTest(v, output, indent)
			end
		end
		return output
	end
	
	function printTest(test, output, indent)
		if test.status then
			return output
		end

		local error = test.error
		if test.fail then
			error = ""
		end
		if error then
			append(output, indent)
			append(output, test.name or "<nameless>")
			append(output, ": ")
			append(output, error)
			append(output, "\n")
			if test.stacktrace then
				append(output, indentstring(indent .. " ", test.stacktrace))
			end
		end
		return printSubtests(test, output, indent .. " ")
	end

	local output = printSubtests(tests, {}, "")
	local text = table.concat(output)
	return text, tests.okcount, tests.failcount
end

