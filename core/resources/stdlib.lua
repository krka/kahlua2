function assert(c, ...)
	if c then
		return c, ...
	end
	if select("#", ...) == 0 then
		error("assertion failed")
	end
	error(...)
end

local function ipairs_iterator(t, index)
	local nextIndex = index + 1
	local nextValue = t[nextIndex]
	if nextValue ~= nil then
		return nextIndex, nextValue
	end
end

function ipairs(t)
	return ipairs_iterator, t, 0
end
pairs = table.pairs

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

