function assert(c, ...)
	if c then
		return c, ...
	end
	error(... or "assertion failed!")
end

local function ipairs_iterator(t, index)
	local nextIndex = index + 1
	local nextValue = t[nextIndex]
	if nextValue then
		return nextIndex, nextValue
	end
end

function ipairs(t)
	return ipairs_iterator, t, 0
end

function pairs(t)
	return next, t, nil
end

do
	local function partition_nocomp(tbl, left, right, pivot)
		local pval = tbl[pivot]
		tbl[pivot], tbl[right] = tbl[right], pval
		local store = left
		for v = left, right - 1, 1 do
			local vval = tbl[v]
			if vval < pval then
				tbl[v], tbl[store] = tbl[store], vval
				store = store + 1
			end
		end
		tbl[store], tbl[right] = tbl[right], tbl[store]
		return store
	end
	local function quicksort_nocomp(tbl, left, right)
		if right > left then
			local pivot = left
			local newpivot = partition_nocomp(tbl,left,right,pivot)
			quicksort_nocomp(tbl,left,newpivot-1)
			return quicksort_nocomp(tbl,newpivot+1,right)
		end
		return tbl
	end

	local function partition_comp(tbl, left, right, pivot, comp)
		local pval = tbl[pivot]
		tbl[pivot], tbl[right] = tbl[right], pval
		local store = left
		for v = left, right - 1, 1 do
			local vval = tbl[v]
			if comp(vval, pval) then
				tbl[v], tbl[store] = tbl[store], vval
				store = store + 1
			end
		end
		tbl[store], tbl[right] = tbl[right], tbl[store]
		return store
	end
	local function quicksort_comp(tbl, left, right, comp)
		if right > left then
			local pivot = left
			local newpivot = partition_comp(tbl,left,right,pivot, comp)
			quicksort_comp(tbl,left,newpivot-1, comp)
			return quicksort_comp(tbl,newpivot+1,right, comp)
		end
		return tbl
	end

	function table.sort(tbl, comp) -- quicksort
	    if comp then
		    return quicksort_comp(tbl,1, #tbl, comp)
	    end
    	return quicksort_nocomp(tbl, 1, #tbl)
	end
end

function string.len(s)
	return #s
end

local tableconcat = table.concat

function string.rep(s, n)
	local t = {}
	for i = 1, n do
		t[i] = s
	end
	return tableconcat(t)
end

function string.gmatch(str, pattern)
	local init = 1
	local function gmatch_it()
		if init <= str:len() then 
			local s, e = str:find(pattern, init)
			if s then
				local oldInit = init
				init = e+1
				return str:match(pattern, oldInit)
			end
		end
	end
	return gmatch_it
end

function math.max(max, ...)
	local select = select
	for i = 1, select("#", ...) do
		local v = select(i, ...)
		max = (max < v) and v or max
	end
	return max
end

function math.min(min, ...)
	local select = select
	for i = 1, select("#", ...) do
		local v = select(i, ...)
		min = (min > v) and v or min
	end
	return min
end


do
	local error = error
	local ccreate = coroutine.create
	local cresume = coroutine.resume

	local function wrap_helper(status, ...)
		if status then
			return ...
		end
		error(...)
	end

	function coroutine.wrap(f)
		local coro = ccreate(f)
		return function(...)
			return wrap_helper(
				cresume(
					coro, ...
				)
			)
		end
	end
end


package = {}
package.loaded = {
	string = string,
	table = table,
	math = math,
	package = package,
	os = os,
	math = math,
	coroutine = coroutine
}
package.loaders = {}
package.path = "/"
table.insert(package.loaders, bytecodeloader)
bytecodeloader = nil

function require(modname)
	local m = package.loaded[modname]
	if m ~= nil then
		return m
	end
	
	local loaders = package.loaders
	local errormessage = ""
	for i = 1, #loaders do
		local loader = loaders[i]
		local loader2 = loader(modname)
		if type(loader2) == "function" then
			m = loader2(modname)
			if m == nil then
				m = true
			end
			package.loaded[modname] = m
			return m
		elseif type(loader2) == "string" then
			errormessage = errormessage .. loader2
		end
	end
	error("Module '" .. modname .. "' not found:\n" .. errormessage)
end

function package.seeall(module)
	local mt = getmetatable(module) or {}
	mt.__index = getfenv(0)
	setmetatable(module, mt)
end

local function apply(obj, n, f, ...)
	if n > 0 then
		f(obj)
		return apply(obj, n - 1, ...)
	end	
end

function module(name, ...)
	local env = getfenv(0)
	local t = package.loaded[name] or env[name]
	if not t then
		t = {}
		package.loaded[name] = t
	end
	t._NAME = name
	t._M = t

	local packagename, lastname = name:match("^(.*%.)([^.]*)$")
	t._PACKAGE = packagename
	if name:find(".", 1, true) then
		local chain = env
		for partial in name:gmatch("([^%.]*)%.") do
			chain[partial] = chain[partial] or {}
			chain = chain[partial]
		end
		chain[lastname] = t
	else
		env[name] = t
	end
	apply(t, select("#", ...), ...)
	setfenv(2, t)
end


do
	local properties = {}
	setmetatable(properties, {__mode = "k"})

	function withproperties(obj)
		local old = getmetatable(obj)
		local oldindex = old and old.__index
		local oldisfun = type(oldindex) == "function"
		local function index(t, k)
			local p = properties[t]
			if p then
				local value = p[k]
				if value then
					return value
				end
			end
			if oldindex then
				if oldisfun then
					return oldindex(t, k)
				end
				return oldindex[k]
			end
		end
		local function newindex(t, k, v)
			local p = properties[t]
			if not p then
				p = {}
				properties[t] = p
			end
			p[k] = v
		end
		setmetatable(obj, {__index = index, __newindex = newindex})
		return obj
	end
end

