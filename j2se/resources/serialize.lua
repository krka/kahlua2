local function append(t, a, b, c)
  if a and a ~= "" then
    t[#t + 1] = a
  end
  if b and b ~= "" then
    t[#t + 1] = b
  end
  if c and c ~= "" then
    t[#t + 1] = c
  end
end

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
local function nilSerialize(buf, value)
  append(buf, tostring(value))
end

local function stringSerialize(buf, value)
   append(buf, string.format("%q", value))
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

local function tableKey2(buf, key, vars, multiline, indent)
      append(buf, "[")
      serialize_pass2(buf, key, vars, multiline, indent)
      append(buf, "]")
end

local function tableKey(buf, key, vars, multiline, indent)
   if type(key) ~= "string" then
      return tableKey2(buf, key, vars, multiline, indent)
   end
   if not keywords[key] and string.find(key, "^[%a_][%a%d_]*$") then
      return append(buf, key)
   else
      return tableKey2(buf, key, vars, multiline, indent)
   end
end

local function tableSerialize(buf, value, vars, multiline, indent)
   local var = vars[value]

   if var then
      local touched = vars[var]
      if touched then
          return append(buf, "_(", var, ")")
      else
	     vars[var] = true
      end
   end

   if var then
      append(buf, "_(", var, ",{")
   else
      append(buf, "{")
   end
   local wantComma
   local lastInt = 0
   local nextIndent = indent .. " "
   for k, v in ipairs(value) do
      if v == nil then
        break
      end
      if wantComma then
         append(buf, ",")
      end
      append(buf, multiline, nextIndent)
      serialize_pass2(buf, v, vars, multiline, nextIndent)
      wantComma = true

      lastInt = k
   end
   for k, v in pairs(value) do
      if type(k) == "number" and k == math.floor(k) and 1 <= k and k <= lastInt then
    	 -- ignore this, it's already in the table
      else
         if k ~= nil and v ~= nil then
              if wantComma then
                append(buf, ",")
              end
              append(buf, multiline, nextIndent)
              tableKey(buf, k, vars, multiline, nextIndent)
              append(buf, "=")
              serialize_pass2(buf, v, vars, multiline, nextIndent)
              wantComma = true
          end
       end
   end
   append(buf, multiline, indent)
   if var then
       append(buf, "})")
   else
       append(buf, "}")
   end
end
typeSerialize["table"] = tableSerialize


serialize_pass2 = function(buf, value, vars, multiline, indent)
   local f = typeSerialize[type(value)] or nilSerialize
   f(buf, value, vars, multiline, indent)
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
   multiline = multiline or ""
   indent = indent or ""
   local vars = getVars(value, nil, 1)
   for k, v in pairs(vars) do
      if v == 0 then
	 vars[k] = nil
      end
   end
   local buf = {}
   serialize_pass2(buf, value, vars, multiline, indent)
   return table.concat(buf)
end

function pp(value)
    return serialize(value, "\n")
end
