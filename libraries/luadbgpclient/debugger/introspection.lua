-- ----------------------------------------------------------------------------
-- Copyright (c) 2011-2012 Sierra Wireless and others.
-- All rights reserved. This program and the accompanying materials
-- are made available under the terms of the Eclipse Public License v1.0
-- which accompanies this distribution, and is available at
-- http://www.eclipse.org/legal/epl-v10.html
--
-- Contributors:
--     Julien Desgats - initial API and implementation
-- ----------------------------------------------------------------------------
-- Properties generation. Generate a LOM table with data from introspection.
-- ----------------------------------------------------------------------------

local debug = require "debug"
local platform = require "debugger.platform"
local util = require "debugger.util"

local tostring, type, assert, next, rawget, getmetatable, setmetatable, getfenv, select,         coyield,         cocreate,         costatus,         coresume,       sformat,      tconcat =
      tostring, type, assert, next, rawget, getmetatable, setmetatable, getfenv, select, coroutine.yield, coroutine.create, coroutine.status, coroutine.resume, string.format, table.concat

local MULTIVAL_MT = { __tostring = function() return "" end }
local probes = { }

-- ---------- --
-- Public API --
-- ---------- --

---
-- Introspection logic. This module implements Lua objects introspection and
-- generates a [DBGP](http://xdebug.org/docs-dbgp.php) compatible 
-- [LOM](http://matthewwild.co.uk/projects/luaexpat/lom.html) data scructure.
-- @module debugger.introspection
local M = { }

---
-- Represent the actual data to send to the debugger.
-- Full XML specification can be found in [DBGP specification](http://xdebug.org/docs-dbgp.php#properties-variables-and-values).
-- Modifying properties after their generation is possible (as actual data serialization/sending is delayed)
-- but should be used with care. The XML structure uses the [LOM](http://matthewwild.co.uk/projects/luaexpat/lom.html)
-- format, refer to these documents to get more informations about fields.
-- 
-- In addition to table fields, it has an array part, `[1]` being the string representation (base64 encoded),
-- possibly followed by chlid properties (@{#DBGPProperty} themselves)
-- 
-- @field #string tag Always "property"
-- @field #table attr XML attributes, see DBGP specification
-- @type DBGPProperty

---
-- Inpectors table, contain all inspector functions.
-- Keys are either type names (`string`, `number`, ...) or metatables
-- that have a custom inspector attached.
-- @field [parent=#debugger.introspection] #table inspectors
M.inspectors = { }

---
-- Generate a DBGP property if needed. If data is in data pagination and recursion depth ranges,
-- and send a property to the debugger, otherwise drop current property.
-- @param #string name Property name (displayed in IDE)
-- @param #string typename Type name (displayed in IDE)
-- @param #string repr Value string representation
-- @param #DBGPProperty parent Parent property
-- @param #string fullname Lua expression used to get value back in further calls
-- @return #table description
-- @function [parent=#debugger.introspection] property
M.property = coyield

---
-- Adds a probe that will be called for every unknown table/userdata.
-- @param #function probe Inspector function to call.
-- @function [parent=#debugger.introspection] add_probe
M.add_probe = function(probe) probes[#probes + 1] = probe end

---
-- Inspects a Lua value by dispatching it to correct inspector. Inspector functions have the same API.
-- @param #string name Property name (will be displayed by IDE)
-- @param value Value to inspect
-- @param #table parent Parent property (LOM table of the )
-- @param #string fullname Expression used to retrieve `value` for further debugger calls
-- @return #DBGPProperty The inspected value as returned by @{debugger.introspection#debugger.introspection.property}.
-- @return #nil If the value has not been inspected
-- @function [parent=#debugger.introspection] inspect
M.inspect = function(name, value, parent, fullname)
    return (M.inspectors[type(value)] or M.inspectors.default)(name, value, parent, fullname)
end

-- ----------------- --
-- Utility functions --
-- ----------------- --

local function default_inspector(name, value, parent, fullname)
    return M.property(name, type(value), tostring(value), parent, fullname)
end

-- Inspects types that can have a metatable (table and userdata). Returns
--   1) generated property
--   2) boolean indicating whether a custom inspector has been called (in that case, do not process value any further)
local function metatable_inspector(name, value, parent, fullname)
    local mt = getmetatable(value)
    do
        -- find  by metatable
        local custom = M.inspectors[mt]
        if custom then return custom(name, value, parent, fullname), true end
        -- or else call probes
        for i=1, #probes do
          local prop = probes[i](name, value, parent, fullname)
          if prop then return prop, true end
        end
    end

    local prop = default_inspector(name, value, parent, fullname)
    if mt and prop then
        local mtprop = M.inspect("metatable", mt, prop, "metatable["..prop.attr.fullname.."]")
        if mtprop then mtprop.attr.type = "special" end
    end
    return prop, false
end

local function fancy_func_repr(f, info)
    local args = {}
    for i=1, info.nparams do
        args[i] = debug.getlocal(f, i)
    end

    if info.isvararg then
        args[#args+1] = "..."
    end

    return "function(" .. tconcat(args, ", ") .. ")"
end

--- Generate a name siutable for table index syntax
-- @param name Key name 
-- @return #string A table index style index
-- @usage generate_printable_key('foo') => '["foo"]'
-- @usage generate_printable_key(12)    => '[12]'
-- @usage generate_printable_key({})    => '[table: 0x12345678]
-- @function [parent=#debugger.introspection] generate_printable_key
local function generate_printable_key(name)
    return "[" .. (type(name) == "string" and sformat("%q", name) or tostring(name)) .. "]"
end
M.generate_printable_key = generate_printable_key

-- Used to store complex keys (other than string and number) as they cannot be passed in text
-- For these keys, the resulting expression will not be the key itself but "key_cache[...]"
-- where key_cache must be mapped to this table to resolve key correctly.
M.key_cache = setmetatable({ n=0 }, { __mode = "v" })

local function generate_key(name)
    local tname = type(name)
    if tname == "string" then return sformat("%q", name)
    elseif tname == "number" or tname == "boolean" then return tostring(name)
    else -- complex key, use key_cache for lookup
        local i = M.key_cache.n
        M.key_cache[i] = name
        M.key_cache.n = i+1
        return "key_cache["..tostring(i).."]"
    end
end

--- Generate a usable fullname for a value.
-- Based on parent fullname and key value, return a valid Lua expression.
-- Key can be any value (as anything can act as table key). If it cannot
-- be serialized (only string, number and boolean can), it will be temporarly
-- stored in an internal cache to be retrieved later.
-- @param #string parent Parent fullname
-- @param key The child key to generate fullname for
-- @return #string A valid fullname expression
-- @function [parent=#debugger.introspection] make_fullname
local function make_fullname(parent, key)
    return parent .. "[" .. generate_key(key) .. "]"
end
M.make_fullname = make_fullname

-- ---------- --
-- Inspectors --
-- ---------- --

M.inspectors.number   = default_inspector
M.inspectors.boolean  = default_inspector
M.inspectors["nil"]   = default_inspector
M.inspectors.userdata = default_inspector
M.inspectors.thread   = default_inspector
M.inspectors.default  = default_inspector -- allows 3rd party inspectors to use the default inspector if needed

M.inspectors.userdata = function(name, value, parent, fullname)
    return (metatable_inspector(name, value, parent, fullname)) -- drop second return value
end

M.inspectors.string = function(name, value, parent, fullname)
    -- escape linebreaks as \n and not as \<0x0A> like %q does
    return M.property(name, "string", sformat("%q", value):gsub("\\\n", "\\n"), parent, fullname)
end

M.inspectors["function"] = function(name, value, parent, fullname)
    local info = debug.getinfo(value, "nSflu")
    local prop
    if info.what ~= "C" then
        -- try to create a fancy representation if possible
        local repr = info.nparams and fancy_func_repr(value, info) or tostring(value)
        if info.source:sub(1,1) == "@" then
            repr = repr .. "\n" .. platform.get_uri("@" .. info.source) .. "\n" .. tostring(info.linedefined)
        end
        prop = M.property(name, "function (Lua)", repr, parent, fullname)
    else
        prop = M.property(name, "function", tostring(value), parent, fullname)
    end
    if not prop then return nil end
    
    -- (5.1 only) environment is dumped only if it is different from global environment
    -- TODO: this is not a correct behavior: environment should be dumped if is different from current stack level one
    local fenv = getfenv and getfenv(value)
    if fenv and fenv ~= getfenv(0) then
        local fenvprop = M.inspect("environment", fenv, prop, "environment["..prop.attr.fullname.."]")
        if fenvprop then fenvprop.attr.type = "special" end
    end
    
    return prop
end


M.inspectors.table = function(name, value, parent, fullname)
    local prop, iscustom = metatable_inspector(name, value, parent, fullname)
    if not prop or iscustom then return prop end
    
    -- iterate over table values and detect arrays at the same time
    -- next is used to circumvent __pairs metamethod in 5.2
    local isarray, i = true, 1
    for k,v in next, value, nil do
        M.inspect(generate_printable_key(k), v, prop, make_fullname(fullname, k))
        -- array detection: keys should be accessible by 1..n keys
        isarray = isarray and rawget(value, i) ~= nil
        i = i + 1
    end
    -- empty tables are considered as tables
    if isarray and i > 1 then prop.attr.type = "sequence" end
    
    return prop
end

M.inspectors[MULTIVAL_MT] = function(name, value, parent, fullname)
    if value.n == 1 then
        -- return directly the value as result
        return M.inspect(name, value[1], parent, fullname)
    else
        -- wrap values inside a multival container
        local prop = M.property(name, "multival", "", parent, fullname)
        if not prop then return nil end
        for i=1, value.n do
            M.inspect(generate_printable_key(i), value[i], prop, fullname .. "[" .. i .. "]")
        end
        return prop
    end
end

-- ------------ --
-- Internal API --
-- ------------ --

-- Used to inspect "multival" or "vararg" values. The typical use is to pack function result(s) in a single
-- value to inspect. The Multival instances can be passed to make_property as a single value, they will be
-- correctly reported to debugger
function M.Multival(...)
    return setmetatable({ n=select("#", ...), ... }, MULTIVAL_MT)
end

--- Makes a property form a name/value pair (and fullname). This is an **internal** function, and should not be used by 3rd party inspectors.
-- @param #number cxt_id Context ID in which this value resides (workaround bug 352316)
-- @param value The value to debug
-- @param name The name associated with value, passed through tostring, so it can be anything
-- @param #string fullname A Lua expression to eval to get that property again (if nil, computed automatically)
-- @param #number depth The maximum property depth (recursive calls)
-- @param #number pagesize maximum children to include
-- @param #number page The page to generate (0 based)
-- @param #number size_limit Optional, if set, the maximum size of the string representation (in bytes)
-- @param #boolean safe_name If true, does not encode the name as table key
-- @return #DBGPProperty root property
-- @function [parent=#debugger.introspection] make_property
--TODO BUG ECLIPSE TOOLSLINUX-99 352316 : as a workaround, context is encoded into the fullname property
M.make_property = function(cxt_id, value, name, fullname, depth, pagesize, page, size_limit, safe_name)
    fullname = fullname or "(...)[" .. generate_key(name) .. "]"
    if not safe_name then name = generate_printable_key(name) end

    local generator = cocreate(function() return M.inspect(name, value, nil, fullname) end)
    local propstack = { }
    local rootnode
    local catchthis = true
    local nodestoskip = page * pagesize -- nodes to skip at root level to respect pagination
    local fullname_prefix = tostring(cxt_id).."|"

    while true do
        local succes, name, datatype, repr, parent, fullname = assert(coresume(generator, catchthis and propstack[#propstack] or nil))
        -- finalize and pop all finished properties
        while propstack[#propstack] ~= parent do
            local topop = propstack[#propstack]
            topop.attr.fullname = util.rawb64(fullname_prefix .. topop.attr.fullname)
            propstack[#propstack] = nil
        end
        if costatus(generator) == "dead" then break end

        local prop = {
          tag = "property",
          attr = {
            children = 0,
            pagesize = pagesize,
            page = parent and 0 or page,
            type = datatype,
            name = name,
            fullname = fullname,
            encoding = "base64",
            size = #repr,
          },
          util.b64(size_limit and repr:sub(1, size_limit) or repr)
        }
        
        if parent then
            parent.attr.children = 1
            parent.attr.numchildren = (parent.attr.numchildren or 0) + 1
            -- take pagination into accont to know if node needs to be catched
            catchthis = #parent <= pagesize and #propstack <= depth
            if parent == rootnode then
                catchthis = catchthis and nodestoskip <= 0
                nodestoskip = nodestoskip - 1
            end
            -- add node to tree
            if catchthis then
              parent[#parent + 1] = prop
              propstack[#propstack + 1] = prop
            end
        else
            rootnode = prop
            catchthis = true
            propstack[#propstack + 1] = prop
        end
    end

    return rootnode
end

return M
