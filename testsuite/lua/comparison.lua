testCall(function() assert("a" ~= nil) end)
testCall(function() assert("a" ~= 1) end)

metaA = {}
function metaA.__eq(a,b)
	return a == b
end

metaB = {}
function metaB.__eq(a,b)
	return true
end

metaD = {}
function metaD.__eq(a,b)
	return false
end

ta = setmetatable({}, metaA)
tb = setmetatable({}, metaB)
tc = setmetatable({}, metaB)
td = setmetatable({}, metaD)

testAssert(ta ~= tb, "same metatable for __eq")
testAssert(tb == tc, "different metatables for __eq")
testAssert(td == td, "not using metatable")

