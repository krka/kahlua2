local function assertEquals(a, b)
	assert(a == b, "Expected " .. a .. " == " .. b)
end

function testAssertEquals(a, b)
	testCall(function() assertEquals(a, b) end)
end



local tbl = "*t"
local str = "%c"

local christmas = {year = 2008, month = 12, day = 25,}
local newyearseve = {year = 2008, month = 12, day = 31,}
local oct25 = {year = 2008, month = 10, day = 25, hour = 2, min = 8, sec = 0, wday = 7, yday = 298,}
local nov1 = {year = 2008, month = 11, day = 1, hour = 2, min = 8, sec = 0, wday = 7, yday = 306,}

local halloween = 1225440000  -- seconds since the epoch for halloween 2008, 8am in UTC

do
	local res = os.date(tbl, halloween)
	testAssertEquals(res.year, 2008)
	testAssertEquals(res.month, 10)
	testAssertEquals(res.day, 31)
	testAssertEquals(res.hour, 8)
	testAssertEquals(res.min, 0)
	testAssertEquals(res.sec, 0)
	testAssertEquals(res.wday, 6)
	testAssertEquals(res.yday, 304)
	
	testAssertEquals(res.year, tonumber(os.date("%Y", halloween)))
	testAssertEquals(res.month, tonumber(os.date("%m", halloween)))
	testAssertEquals(res.day, tonumber(os.date("%d", halloween)))
	testAssertEquals(res.yday, tonumber(os.date("%j", halloween)))
	testAssertEquals(res.hour, tonumber(os.date("%H", halloween)))
	testAssertEquals(res.min, tonumber(os.date("%M", halloween)))
	testAssertEquals(res.sec, tonumber(os.date("%S", halloween)))
end

testAssert(os.difftime(os.time(christmas), os.time(newyearseve)) < 0,1)
testAssert(os.difftime(os.time(newyearseve), os.time(christmas)) > 0,2)
testAssertEquals(os.difftime(os.time(christmas), os.time(christmas)),0)

testAssertEquals(os.difftime(os.time(newyearseve), os.time(christmas)),6*24*60*60)

local nowtime = os.time()

testAssertEquals(os.time(os.date(tbl, nowtime)),nowtime)

do
	local res = os.date(tbl, os.time(oct25))
	testAssertEquals(res.year, oct25.year)
	testAssertEquals(res.month, oct25.month)
	testAssertEquals(res.day, oct25.day)
	testAssertEquals(res.hour, oct25.hour)
	testAssertEquals(res.min, oct25.min)
	testAssertEquals(res.sec, oct25.sec)
	testAssertEquals(res.wday, oct25.wday)
	testAssertEquals(res.yday, oct25.yday)
end

-- Locale dependant test, so comment it out
-- testAssertEquals(os.date(str,halloween), "Fri Oct 31 04:00:00 EDT 2008")
