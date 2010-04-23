local function desFun(index, t)
   local old = _G[index]
   if old then
      if t then
	 for k, v in pairs(t) do
	    old[k] = v
	 end
      end
      return old
   else
      t = t or {}
      _G[index] = t
      return t
   end
end

local desEnv = {}
setmetatable(desEnv, {__newindex = function() end})
local next = next

function deserialize(str)
   -- for lua 5.0 (and 5.1)
   local f = assert(loadstring("return function(_) return " .. str .. " end"))
   setfenv(f, desEnv)
   return f()(desFun)

   -- for lua 5.1
--[[
   local f = assert(loadstring("local _ = ... return " .. str .. " end"))
   setfenv(f, desEnv)
   return f(desFun)
]]
end

local serialize_pass2

local typeSerialize = {}
local function nilSerialize(value)
    return tostring(value)
end
typeSerialize["nil"] = nilSerialize

local function numberSerialize(value)
   return tostring(value)
end
typeSerialize["number"] = numberSerialize

local function stringSerialize(value)
   return string.format("%q", value)
end
typeSerialize["string"] = stringSerialize

local keywords = {}
keywords["and"] = 1
keywords["or"] = 1
keywords["not"] = 1
keywords["do"] = 1
keywords["end"] = 1
keywords["if"] = 1
keywords["then"] = 1
keywords["while"] = 1
keywords["repeat"] = 1
keywords["local"] = 1
keywords["function"] = 1
keywords["break"] = 1
keywords["return"] = 1
keywords["else"] = 1
keywords["elseif"] = 1
keywords["until"] = 1
keywords["true"] = 1
keywords["false"] = 1
keywords["nil"] = 1
keywords["in"] = 1
keywords["for"] = 1

local function tableKey(key, vars, multiline)
   if type(key) ~= "string" then
      return "[" .. serialize_pass2(key, vars, multiline) .."]"
   end
   if not keywords[key] and string.find(key, "^[%a_][%a%d_]*$") then
      return key
   else
      return "[" .. serialize_pass2(key, vars, multiline) .."]"
   end
end

local function tableSerialize(value, vars, multiline, indent)
   local var = vars[value]

   if var then
      local touched = vars[var]
      if touched then
	 return "_(" .. var .. ")"
      else
	 vars[var] = true
      end
   end

   local res = (multiline and "\n" or "") .. indent
   if var then
      res = "_(" .. var .. ",{"
   else
      res = "{"
   end
   local wantComma
   local lastInt = 0
   local nextIndent = indent .. " "
   for k, v in ipairs(value) do
      local vs = serialize_pass2(v, vars, multiline, nextIndent)
      if vs == "nil" then
	 -- abort, since this will mean the end of integer keys
	 break
      end
      if wantComma then
	 res = res .. ","
      end
        res = res .. (multiline and "\n" or "") .. nextIndent
      res = res .. vs
      wantComma = true

      lastInt = k
   end
   for k, v in pairs(value) do
      if type(k) == "number" and k == math.floor(k) and 1 <= k and k <= lastInt then
	 -- ignore this, it's already in the table
      else
	 local ks = tableKey(k, vars, multiline, nextIndent)
	 local vs = serialize_pass2(v, vars, multiline, nextIndent)
	 if ks ~= "nil" and vs ~= "nil" then
	    if wantComma then
	       res = res .. ","
	    end
        res = res .. (multiline and "\n" or "") .. nextIndent
	    res = res .. ks .. "=" .. vs
	    wantComma = true
	 end
      end
   end
   res = res .. (multiline and "\n" or "") .. indent
   if var then
      res = res .. "})"
   else
      res = res .. "}"
   end
   return res
end
typeSerialize["table"] = tableSerialize


serialize_pass2 = function(value, vars, multiline, indent)
   local f = typeSerialize[type(value)] or nilSerialize
   return f(value, vars, multiline, indent)
end

local function getVars(value, t, curVar)
   t = t or {}
   if type(value) == "table" then
      if t[value] then
	 if t[value] == 0 then
	    t[value] = curVar
	    curVar = curVar + 1
	 end
      else
	 t[value] = 0
	 for k, v in pairs(value) do
	    local t2, curVar2 = getVars(k, t, curVar)
	    curVar = curVar2
	    local t2, curVar2 = getVars(v, t, curVar)
	    curVar = curVar2
	 end
      end
   end
   return t, curVar
end

function serialize(value, multiline, indent)
   indent = indent or ""
   local vars = getVars(value, nil, 1)
   for k, v in pairs(vars) do
      if v == 0 then
	 vars[k] = nil
      end
   end
   return serialize_pass2(value, vars, multiline, indent)
end

function pp(value)
   print(serialize(value, true))
end
