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

testformat("-0.0", "%.1f", tonumber("-0"))
testformat("-inf", "%.1f", tonumber("-inf"))
testformat("inf", "%.1f", tonumber("inf"))
testformat("-inf", "%.1f", tonumber("-inF"))
testformat("inf", "%.1f", tonumber("iNf"))
testformat("nan", "%.1f", tonumber("nan"))

testformat("5,8,13,21", "%d,%d,%d,%d", 5,8,13,21)
testformat("     hello", "%10s", "hello")
testformat("     hello", "%010s", "hello") -- zero padding only for numbers!
testformat("hello", "%1s", "hello")
testformat("00012", "%05d", 12)
testformat("12   ", "%-05d", 12)
testformat("12   ", "%-5d", 12)
testformat("a    ", "%-5s", "a")
testformat("  +12", "%+5d", 12)
testformat("+12", "%+1d", 12)
testformat("12.1", "%1.1f", 12.125)
testformat("12.12", "%1.2f", 12.125)
testformat("12.125", "%1.3f", 12.125)
testformat("12.1250", "%1.4f", 12.125)
testformat(" 12.1250", "%8.4f", 12.125)
testformat("12.1250 ", "%-8.4f", 12.125)
testformat("+12.1250 ", "%-+9.4f", 12.125)
testformat("'                hell'", "'%20.4s'", "hello")

testformat("!12.!", "!%#.0f!", 12.3456)
testformat("!12!", "!%.0f!", 12.3456)
testformat("!12.3!", "!%.1f!", 12.3456)
testformat("!12.35!", "!%.2f!", 12.3456)
testformat("!12.346!", "!%.3f!", 12.3456)
testformat("!12.3456!", "!%.4f!", 12.3456)
testformat("!12.34560!", "!%.5f!", 12.3456)
testformat("!12.345600!", "!%.6f!", 12.3456)

testformat("0173", "%.4o", 123)
testformat("2322", "%.4o", 1234)
testformat("0173", "%#.4o", 123)
testformat("02322", "%#.4o", 1234)

testformat("-0.1", "%+1.1f", -0.1)

testformat("+0.1", "%+1.1f", 0.1)
testformat(" 0.1", "% 1.1f", 0.1)

testformat("-123", "% 1.0f", -123)
testformat(" 123", "% 1.0f", 123)
testformat("+000012.23", "%+010.2f", 12.23)
testformat("0.0000001000", "%.10f", 0.0000001)
testformat("0.0000001235", "%.10f", 0.00000012347)

testformat("1.e+00", "%#.e", 1.0)
testformat("1.235e+00", "%#.3e", 1.23456)
testformat("1.235E+03", "%#.3E", 1234.56)
testformat("+00000000001.235E+17", "%+020.3E", 123456789123456789)
testformat("+00000000001.235E-14", "%+020.3E", 0.0000000000000123456789123456789)

testformat("0.0001", "%g", 0.0001)
testformat("0.000100000", "%#g", 0.0001)
testformat("-0.000100000", "%#g", -0.0001)
testformat("1e-05", "%g", 0.00001)
testformat("1.00000e-05", "%#g", 0.00001)
testformat("-1.00000e-05", "%#g", -0.00001)

testformat("1.", "%#.0g", 1.2345678)
testformat("1.", "%#.1g", 1.2345678)
testformat("1.2", "%#.2g", 1.2345678)
testformat("1.23", "%#.3g", 1.2345678)
testformat("1.", "%#.0g", 1)
testformat("1.", "%#.1g", 1)
testformat("1.0", "%#.2g", 1)
testformat("1.00", "%#.3g", 1)
testformat("100.", "%#.3g", 100)
testformat("100.0", "%#.4g", 100)
testformat("100.00", "%#.5g", 100)
testformat("100", "%.4g", 100)
testformat("1e+07", "%.4g", 10000000)
testformat(" 1e+07", "% .4g", 10000000)
testformat("+1e+07", "%+.4g", 10000000)
testformat("1.000e+07", "%#.4g", 10000000)

testformat("1.09692", "%#g", 1.0969153711619073)
testformat("5.1", "%#.2g", 5.061974531368081)
testformat("1.23e+06", "%g", 1230000)
testformat("1.23000e+06", "%#g", 1230000)

testformat("                -025", "%#20.3d", -25.66494878296039)
testformat("             -1.e+02", "%#20.0e", -95.05946110773807)
testformat("2", "%.0f", 2.5)
testformat("4", "%.0f", 3.5)
testformat("4", "%.0f", 4.5)
testformat("6", "%.0f", 5.5)

testformat("0", "%d", 0.2)
testformat("0", "%d", 0.9)
testformat("1", "%d", 1.9)
testformat(" 19", "% 3.2d", 19)
testformat("", "%.0u", 0.4)
testformat("", "%.0o", 0.4)
testformat("", "%.0x", 0.4)
testformat("", "%.0X", 0.4)
testformat("", "%.0d", 0.4)
testformat("", "%.0i", 0.4)
testformat("0", "%#.0o", 0)
testformat("", "%#.0x", 0)
testformat("", "%#.0X", 0)

testformat("1", "%#.0u", 1.4)
testformat("01", "%#.0o", 1.4)
testformat("0x1", "%#.0x", 1.4)
testformat("0X1", "%#.0X", 1.4)
testformat("1", "%#.0d", 1.4)
testformat("1", "%#.0i", 1.4)

testformat("0x07", "%#.2x", 7)
testformat("0X07", "%#.2X", 7)
testformat("07", "%#.2o", 7)

testformat("0x7", "%#x", 7)
testformat("0X7", "%#X", 7)
testformat("07", "%#o", 7)
testformat("000", "%#.3x", 0)

testformat("0000000001", "%#010o", 1)
testformat("        01", "%#010.0o", 1)
testformat("     00001", "%#010.5o", 1)

testformat("   0x00001", "%#010.5x", 1)
testformat("   0x00001", "%#10.5x", 1)
testformat("     00001", "%010.5x", 1)
testformat("     00001", "%10.5x", 1)

assert(string.format("%.0x", -1):sub(1, 8) == "ffffffff")

do
	local zero = 0
	local one = 1
	local inf = one / zero
	local plusinf = inf
	local neginf = -plusinf
	local nan = zero / zero
	assert(tostring(plusinf) == "inf", "expected inf, got " .. tostring(plusinf))
	assert(tostring(neginf) == "-inf", "expected -inf, got " .. tostring(neginf))
	assert(tostring(nan) == "nan", "expected nan, got " .. tostring(nan))
	testformat("inf", "%f", plusinf)
	testformat("-inf", "%f", neginf)
	testformat("nan", "%f", nan)

	testformat("+inf", "%+f", plusinf)
	testformat("+inf", "%+e", plusinf)
	testformat("+INF", "%+E", plusinf)
	testformat("+inf", "%+g", plusinf)
	testformat("+INF", "%+G", plusinf)

	testformat("-inf", "%+f", neginf)
	testformat("-inf", "%+e", neginf)
	testformat("-INF", "%+E", neginf)
	testformat("-inf", "%+g", neginf)
	testformat("-INF", "%+G", neginf)

end

-- this fails in some libc implementations
testformat("1.0e+02", "%#.2g", 99.9)


-- %, s, q, c, d, E, e, f, g, G, i, o, u, X, and x
testcases = {
	["%"]={"%"},
	["c"]={[255]=string.char(255), [120]='x'},
	["d"]={[-1]='-1', [1]='1', ["1"]='1', [1.1]='1', [10]='10', [255]='255'},
	["e"]={[-1]="-1.000000e+00", [1]="1.000000e+00", ["1"]="1.000000e+00", [1.1]="1.100000e+00", 
				[10]="1.000000e+01", [255]="2.550000e+02"},
	["E"]={[-1]="-1.000000E+00", [1]="1.000000E+00", ["1"]="1.000000E+00", [1.1]="1.100000E+00", 
				[10]="1.000000E+01", [255]="2.550000E+02"},
	["f"]={[-1]="-1.000000", [1]="1.000000", ["1"]="1.000000", [1.1]="1.100000", 
				[10]="10.000000", [255]="255.000000"},
	["g"]={[-1]='-1', [1]='1', ["1"]='1', [1.1]='1.1', [10]='10', [255]='255'},
	["G"]={[-1]='-1', [1]='1', ["1"]='1', [1.1]='1.1', [10]='10', [255]='255'},
	["i"]={[-1]='-1', [1]='1', ["1"]='1', [1.1]='1', [10]='10', [255]='255'},
	["o"]={[1]='1', ["1"]='1', [1.1]='1', [10]='12', [255]="377"},
	["q"]={["\n"]='"\\\n"', ["\r"]='"\\r"', ["\""]='"\\""', ["\t"]='"\t"'},
	["s"]={[""]="", ["abc"]="abc", [1]="1"},
	["u"]={[1]='1', ["1"]='1', [1.1]='1', [10]='10', [255]='255'},
	["x"]={[1]='1', ["1"]='1', [1.1]='1', [10]='a', [255]="ff"},
	["X"]={[1]='1', ["1"]='1', [1.1]='1', [10]='A', [255]='FF'},
}

for t, c in pairs(testcases) do
	local template = "%" .. t
	for k, v in pairs(c) do
		testformat(v, template, k)
		--print(string.format("string.format(%q, %q) == %q", template, k, result))
	end
end
function verifyinvalidpattern(pattern)
	local status, err = pcall(function() string.format(pattern) end)
	assert(not status)
end

function verifyinvalidpatterns(first, ...)
	if first then
		verifyinvalidpattern(first)
		return verifyinvalidpatterns(...)
	end
end

verifyinvalidpatterns("%", "% ", "%.", "%..f", "%...f", "%111", "%111.111")

