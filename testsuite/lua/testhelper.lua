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

function generatereport(tests)
	local freeId = 0

	local function getHeader(title, color, hidden, expandSign, okcount, failcount)
		freeId = freeId + 1
		local id = freeId
		local ok = (okcount > 0) and ("Ok: " .. okcount) or ""
		local fail = (failcount > 0) and ("Failed: " .. failcount) or ""
		if failcount > 0 and okcount > 0 then
			ok = ok .. ", "
		end
		return string.format([[
			<div class="testcontainer %s">
				%s%s
				<a href="javascript:toggle(%d)">
					<span id="span%d">[%s]</span>
				</a>
				<div id="div%d" class="%s">
					<div class="indent">
		]], color, ok, fail, id, id, expandSign, id, hidden)
	end
	
	local function printTest(test)
		local faillistbuffer = {}
		if test.fail then
			local failcount = 0
			local okcount = 0
			for k, v in ipairs(test.fail) do
				failcount = failcount + v.failcount
				okcount = okcount + v.okcount
			end
			table.insert(faillistbuffer, getHeader('Failures', 'red', '', 'hide', okcount, failcount))
			for k, v in ipairs(test.fail) do
				table.insert(faillistbuffer, printTest(v))
			end
			table.insert(faillistbuffer, "</div></div></div>")
		end
		local faillist = table.concat(faillistbuffer)
	
		local oklistbuffer = {}
		if test.ok then
			local failcount = 0
			local okcount = 0
			for k, v in ipairs(test.ok) do
				failcount = failcount + v.failcount
				okcount = okcount + v.okcount
			end
			table.insert(oklistbuffer, getHeader('Successes', 'green', 'hidden', 'show', okcount, failcount))
			for k, v in ipairs(test.ok) do
				table.insert(oklistbuffer, printTest(v))
			end
			table.insert(oklistbuffer, "</div></div></div>")
		end
		local oklist = table.concat(oklistbuffer)

		local error = test.error
		if not error then
			if test.failcount == 0 then
				error = "Ok"
			else
				error = "Failed subtests"
			end
		end
		
		local stacktrace = test.stacktrace
		if stacktrace and stacktrace ~= "" then
			stacktrace = "<h5>Stacktrace:</h5><pre>" .. stacktrace .. "</pre>"
		end
		local ret = template_test:
			gsubplain("@@TYPE@@", test.status and "green" or "red"):
			gsubplain("@@NAME@@", test.name or ""):
			gsubplain("@@ERRORMESSAGE@@", error):
			gsubplain("@@STACKTRACE@@", stacktrace or ""):
			gsubplain("@@FAIL_LIST@@", faillist):
			gsubplain("@@OK_LIST@@", oklist)
		return ret
	end

	local text = printTest(tests)
	local output = template:gsubplain("@@TEST@@", text)
	return output, tests.okcount, tests.failcount
end

template = [[
<html>
	<head>
		<style>
			div.green {background-color: #ddffdd;}
			div.red {background-color: #ffdddd;}
			div.testcontainer {border: 1px #777 solid; padding: 5px; margin: 5px;}
			div.indent {margin-left: 10px;}
			div.hidden {display: none;}
			pre { margin: 0; padding: 0; }
			p { margin: 0; padding: 0; }
			h4 { margin: 0; }
			h5 { margin-bottom: 1px; }
			a { color: #ff0000; }
		</style>
		<script>
			function toggle(id) {
				var object = document.getElementById('div' + id)
				var span = document.getElementById('span' + id)
				if (object.className == "hidden") {
					object.className = "";
					span.innerHTML = "[hide]";
				} else {
					object.className = "hidden";
					span.innerHTML = "[show]";
				}
			}
		</script>		
	</head>
	<body>
		<h1>Test results</h1>
		@@TEST@@
	</body>
</html>
]]

template_test = [[
	<div class="testcontainer @@TYPE@@">
		<div class="">
			<p>
				<b>@@NAME@@</b>: <code>@@ERRORMESSAGE@@</code>
				@@STACKTRACE@@
			</p>
		</div>
		@@FAIL_LIST@@
		@@OK_LIST@@
	</div>
]]

