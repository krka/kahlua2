local function tobytes(s)
	local t = {string.byte(s)}
	return table.concat(t, ",")
end

function assertEqual(actual, expected, msg)
	if expected == actual then
		return
	end
	error(msg or string.format("expected: %s (%s), actual: %s (%s)",
			tostring(expected), tobytes(tostring(expected)),
			tostring(actual), tobytes(tostring(actual))))
end

function testAssertEqual(a, b, msg, name)
	testCall(name, function() assertEqual(a, b, msg) end)
end

testAssertEqual("\0", string.char(0))
testAssertEqual(#("\0"), 1)
testAssertEqual(#("\0\0"), 2)


-- test concatenation
do
	local s1, s2 = "hello", "world"
	local s = s1 .. s2
	testAssertEqual(s,"helloworld")
	testAssertEqual(s:rep(1), "helloworld")
	testAssertEqual(s:rep(5), "helloworldhelloworldhelloworldhelloworldhelloworld")
	testAssertEqual(s:rep(0), "")
end

do
	local s1, s2, s3, s4 = "this", "is", "a", "test"
	local s = s1 .. s2 .. s3 .. s4
	testAssertEqual(s,"thisisatest")
end

do
	local meta = {__concat = function(a, b)
		if type(a) == "table" then
			a = a[1]
		end
		if type(b) == "table" then
			b = b[1]
		end
		return a .. b
	end}
	local t1 = setmetatable({"hello"}, meta)
	local t2 = setmetatable({" "}, meta)
	local t3 = setmetatable({"world"}, meta)
	local s = t1 .. t2 .. t3
	testAssertEqual(s,"hello world")
end

do
	local t1 = {"hello"}
	local t2 = {" "}
	local t3 = {"world"}
	local s
	local ok, errmsg = pcall(function() s = t1 .. t2 .. t3 end)
	testAssert(not ok)
end

do
	testAssertEqual(type(string),"table")
	testAssertEqual(type(string.sub),"function")
	testAssertEqual(type(string.byte),"function")
	testAssertEqual(type(string.len),"function")
	testAssertEqual(type(string.char),"function")
	testAssertEqual(type(string.lower),"function")
	testAssertEqual(type(string.upper),"function")
	testAssertEqual(type(string.reverse),"function")

	-- testing handling of not enough parameters
	testAssert(not pcall(string.sub))
	testAssert(not pcall(string.byte))
	testAssert(not pcall(string.len))
	testAssert(not pcall(string.lower))
	testAssert(not pcall(string.upper))
	testAssert(not pcall(string.reverse))

end


do
	local a,b,c,d,e,f,g = string.byte("Hello world", 1, 5)
	testAssert(a == string.byte("H"))
	testAssert(b == string.byte("e"))
	testAssert(c == string.byte("l"))
	testAssert(d == string.byte("l"))
	testAssert(e == string.byte("o"))
	testAssert(f == nil)
	testAssert(g == nil)
	
	testAssert(string.byte("123", 1) == 49)
	testAssert(string.byte("123", 2) == 50)
	testAssert(string.byte("123", 3) == 51)
	testAssert(string.byte("123", 4) == nil)

	testAssert(string.byte("123", 0) == nil)
	testAssert(string.byte("123", -1) == 51)
	testAssert(string.byte("123", -2) == 50)
	testAssert(string.byte("123", -3) == 49)
	testAssert(string.byte("123", -4) == nil)
	testAssert(select("#", string.byte("123", -4, 3)) == 3)
	testAssert(select("#", string.byte("123", -2, 10)) == 2)	
end

do
	local function testReverse(s)
		testCall(function()
			assertEqual(#(s:reverse()),#s)
			assertEqual(s:reverse():reverse(),s)
		end)
	end 
	testReverse"hello world"
	testReverse""
	testReverse"a"
	testReverse"ÅÄÖ"
	testReverse"adolf i paris rapar sirap i floda"
end

s = "Hello world"

testAssertEqual(#s,11)
testAssertEqual(s:sub(-1, 5),"")
testAssertEqual(s:sub(0, 5),"Hello")
testAssertEqual(s:sub(1, 5),"Hello")
testAssertEqual(s:sub(2, 5),"ello")
testAssertEqual(s:sub(1, -1),"Hello world")
testAssertEqual(s:sub(1, -5),"Hello w")
testAssertEqual(s:sub(1, -7),"Hello")
testAssertEqual(s:sub(1, 0),"")
testAssertEqual(s:sub(1, -25),"")

testAssertEqual(s:sub(-5, -1),"world")
testAssertEqual(s:sub(-500,500),"Hello world")
testAssertEqual(s:sub(500,-500),"")
testAssertEqual(s:sub(#s + 1, 1000),"")

testAssertEqual(s.sub,string.sub)

testAssert("ape" < "banana")

testAssert("xyz" <= "xyz")

testAssertEqual(s:byte(1),72)

testAssertEqual(string.char(65),"A")

testAssertEqual(s:lower(),"hello world")
testAssertEqual(s:upper(),"HELLO WORLD")

testAssertEqual(s:match("Hello"),"Hello")
testAssertEqual(s:match("H.l%D%w"),"Hello")

testAssertEqual(s:match("H[e][lo][lo][lo]"),"Hello")

do
	testAssert(string.match("q", "^[%a_]$") == "q")
	testAssert(string.match("_", "^[%a_]$") == "_")
	testAssert(string.match("1", "^[%w]$") == "1")
	testAssert(string.match("1", "^[%w]$") == "1")
	testAssert(string.match("1", "^[%a_%w]$") == "1")
	testAssert(string.match("1", "^[%a_%w]$") == "1")

	local a, b = string.match("break", "^([%a_][%w_]*)()")
	testAssert(a == "break", (a or "nil") .. (b or "nil"))
	testAssert(b == 6, (a or "nil") .. (b or "nil"))
end


testAssertEqual(s:find("[Hello][Hello][Hello]"),1)
testAssertEqual(s:find("[hello][hello][hello]"),2)

testAssertEqual(s:find("worl"),7)
testAssertEqual(s:find("worlld"),nil)

testAssertEqual(s:find("%w%w%w%w%w"),1)
testAssertEqual(s:find("%w%w%w%w%w",5),7)
testAssertEqual(s:find("%w%w%w%w%w%w"),nil)
testAssertEqual(s:find("%w%w%w%w%w",8),nil)

testAssertEqual(s:gsub("(%w+)","la"),"la la")
testAssertEqual(s:gsub("(%w+)", "%1 %1"),"Hello Hello world world")
testAssertEqual(string.gsub(s, "%w+", "%0 %0", 1), "Hello Hello world")
testAssertEqual(string.gsub("Hello world from Lua", "(%w+)%s*(%w+)", "%2 %1"),"world Hello Lua from")

do
	local function f(s) 
		return s..s, 1, 2, 3 
	end
	testAssertEqual(string.gsub("$repeatme$", "%$(.-)%$", f),"repeatmerepeatme")
     
	local t = {name="lua", version="5.1"}
	testAssertEqual(string.gsub("$name-$version.tar.gz", "%$(%w+)", t),"lua-5.1.tar.gz")
	
	testAssertEqual(string.gsub("a", "a", "%%"), "%", nil, "gsub %%")
	testAssertEqual(string.gsub("a", "a", "%"), "\0", nil, "gsub %")
end

do
	-- "when no captures are present, pass the whole match"
	-- if the function/table returns something other than string, no replacement is performed
	local function f(s)
		if (s == "a") then
			return "A"
		elseif (s == "b") then
			return 6
		end
	end
	testAssertEqual(string.gsub("ahoj babi", ".", f), "Ahoj 6A6i")

	local tbl = { a='A', b=6 }
	testAssertEqual(string.gsub("ahoj babi", ".", tbl), "Ahoj 6A6i")
	
	testAssertEqual(string.gsub("ahoj babi", "[ab]", 1), "1hoj 111i")
	testAssertEqual(string.gsub("ahoj babi", "[ab]", "XY"), "XYhoj XYXYXYi")
end

do
	local s2 = "abcdabcd"
	testAssertEqual(s2:find("bc"),2)
	testAssertEqual(s2:match("bc"),"bc")
	testAssertEqual(s2:match("%wc"),"bc")
	testAssertEqual(s2:find("cd",10),nil)
	testAssertEqual(s2:find("cd",-4),7)
	testAssertEqual(s2:find("cd",-8),3)
	
	testAssertEqual(s2:find("bcd$"),6)
	testAssertEqual(s2:find("abc$"),nil)
	testAssertEqual(s2:find("^abcdabcd$"),1)
	testAssertEqual(s2:find("^abcd$"),nil)
	
	local s3 = "123$^xy"
	testAssertEqual(s3:find("3$^x"),3)
end	

do
	local s = "12345abcdef"
	local si, ei, cap1 = s:find("(45ab)")
	testAssertEqual(si,4)
	testAssertEqual(ei,7)
	testAssertEqual(cap1,"45ab")
	testAssertEqual(s:match("(45ab)"),"45ab")
	
	si, ei, cap1 = s:find("cd()")
	testAssertEqual(si,8)
	testAssertEqual(ei,9)
	testAssertEqual(cap1,10)
	testAssertEqual(s:match("cd()"),10)
	
	local cap2, cap3, cap4 = nil, nil, nil
	si, ei, cap1, cap2 = s:find("(23)%d%d%a(bc)")
	testAssertEqual(si,2)
	testAssertEqual(ei,8)
	testAssertEqual(cap1,"23")
	testAssertEqual(cap2,"bc")
	cap1, cap2 = s:match("(23)%d%d%a(bc)")
	testAssertEqual(cap1,"23")
	testAssertEqual(cap2,"bc")
	
	si,ei,cap1,cap2 = s:find("%d(%d%d(%a%a)%a)%a")
	testAssertEqual(si,3)
	testAssertEqual(ei,9)
	testAssertEqual(cap1,"45abc")
	testAssertEqual(cap2,"ab")
	cap1,cap2 = s:match("%d(%d%d(%a%a)%a)%a")
	testAssertEqual(cap1,"45abc")
	testAssertEqual(cap2,"ab")
	
	si,ei,cap1,cap2,cap3,cap4 = s:find("%d(%d%a(%a)())%a(%x%a)")
	testAssertEqual(si,4)
	testAssertEqual(ei,10)
	testAssertEqual(cap1,"5ab")
	testAssertEqual(cap2,"b")
	testAssertEqual(cap3,8)
	testAssertEqual(cap4,"de")
	cap1,cap2,cap3,cap4 = s:match("%d(%d%a(%a)())%a(%x%a)")
	testAssertEqual(cap1,"5ab")
	testAssertEqual(cap2,"b")
	testAssertEqual(cap3,8)
	testAssertEqual(cap4,"de")
	
	testAssertEqual(s:find("%d(%u%l)"),nil)
	testAssertEqual(s:match("%d(%u%l)"),nil)
end
do
	local s = "wxyzabcd1111;.,"
    local si,ei = s:find("cd1*")
    testAssertEqual(si,7)
    testAssertEqual(ei,12)
    
	local si,ei = s:find("bce?d11")
	testAssertEqual(si,6)
	testAssertEqual(ei,10)
	
	si, ei = s:find("1-")
	testAssertEqual(si,1)
	testAssertEqual(ei,0)
	
    si, ei = s:find("1-1")
    testAssertEqual(si,9)
    testAssertEqual(ei,9)
    
    si, ei = s:find("1*1")
    testAssertEqual(si,9)
    testAssertEqual(ei,12)
    
    si, ei = s:find("1+1")
    testAssertEqual(si,9)
    testAssertEqual(ei,12)
end

local b = "6 - (x + (y^2 - 3z) / 7xy)"
testAssertEqual(b:find("%b()"),5)
testAssertEqual(b:find("%b)("),nil)

do
	local s2 = "hello world from Lua"
	local t2 = {}
    for w in string.gmatch(s2, "%w+") do
        --table.insert(t2,w) -- table.insert doesnt work atm
        t2[#t2+1] = w
    end
    
    testAssertEqual(#t2,4)
    testAssertEqual(t2[1],"hello")
    testAssertEqual(t2[2],"world")
    testAssertEqual(t2[3],"from")
    testAssertEqual(t2[4],"Lua")
	
	local t = {}
    local s = "from=world, to=Lua"
    for k, v in string.gmatch(s, "(%w+)=(%w+)") do
        t[k] = v
    end
	testAssertEqual(t.from, "world")
	testAssertEqual(t.to, "Lua")
end

function concattest(...)
	local t = {test = "world"}
	local tmp = ...
	local s = "hello" .. t.test
	testAssertEqual(s,"helloworld")
end
concattest()

function concattest2(...)
	local function t() return "world" end
	local tmp = ...
	local s = "hello" .. t()
	testAssertEqual(s,"helloworld")
end
concattest2()

function concattest3(...)
	local t = setmetatable({}, {__index = function() return "world" end})
	local tmp = ...
	local s = "hello" .. t.test
	testAssertEqual(s,"helloworld")
end
concattest3()

function concattest4(...)
	local t = setmetatable({}, {__index = function() return "world" end})
	local tmp = ...
	local s = tmp .. t.test
	testAssertEqual(s,"helloworld")
end
concattest4("hello")

