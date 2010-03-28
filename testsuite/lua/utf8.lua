utf8str = "Hellö wörld";
testAssert(utf8str:sub(4, 4) == "l")
testAssert(utf8str:sub(5, 5) == "ö")
testAssert(utf8str:sub(6, 6) == " ")
testAssert(utf8str:sub(7, 7) == "w")
testAssert(utf8str:sub(8, 8) == "ö")

