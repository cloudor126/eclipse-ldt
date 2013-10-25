-------------------------------------------------------------------------------
-- Copyright (c) 2012-2013 Julien Desgats
-- All rights reserved. This program and the accompanying materials
-- are made available under the terms of the Eclipse Public License v1.0
-- which accompanies this distribution, and is available at
-- http://www.eclipse.org/legal/epl-v10.html
--
-- Contributors:
--     Julien Desgats - initial API and implementation
-------------------------------------------------------------------------------
-- LuaJIT cdata introspection library.
-------------------------------------------------------------------------------

-- known issues:
--  * references are de-referenced event if inspect_references is unset
--  * is automatic pointer and reference de-referencing is possible ?
--    (only for first item in case of arrays). Possible leads:
--    http://stackoverflow.com/questions/7134590/how-to-test-if-an-address-is-readable-in-linux-userspace-app
--    http://www.softwareverify.com/blog/?p=319
--  * when setting a value from Eclipse, the type is sometimes changed (e.g. int => number)

local introspection = require "debugger.introspection"
local reflect = require "debugger.plugins.ffi.reflect"
local ffi = require "ffi"

local tostring, tonumber, type, assert, sformat, tconcat = tostring, tonumber, type, assert, string.format, table.concat

local M = { }

--- Whether the reference types are inspected. Usually references should be safe (at least a bit
-- safer than pointers) so they are inspected. If a reference points to unsafe memory, the whole
-- program could crash !
-- If this feature is disabled, deeply nested C types will not be displayed correctly as evaluation
-- has a recursion limit, any further evaluation is done through references.
M.inspect_references = true

local function make_typename(refct)
    local t = refct.what
    if t == "int" then
        if refct.bool then t = "bool"
        else
            -- use C99 type notation to give more details about acutal type
            t = (refct.unsigned and "uint" or "int") .. tostring(refct.size * 8) .. "_t"
        end
    elseif t == "float" then
        -- assume IEEE754
        if     refct.size ==  8 then t = "double"
        elseif refct.size == 16 then t = "long double" -- not really sure this one is always true
        end
    elseif t == "struct" or t == "enum" or t == "union" then
        t = refct.name and (t .. " " .. refct.name) or ("anonymous "..t)
    elseif t == "func" then
        t = "function (FFI)"
    elseif t == "ptr" then
        t = make_typename(refct.element_type) .. "*"
    elseif t == "ref" then
        t = make_typename(refct.element_type) .. "&"
    elseif t == "field" then
        return make_typename(refct.type)
    elseif t == "bitfield" then
        t = (refct.type.unsigned and "unsigned" or "signed") .. ":" .. tostring(refct.size * 8)
        refct = refct.type
    end
    
    if refct.const then t = "const " .. t end
    if refct.volatile then t = "volatile " .. t end
    return t
end

-- if cdatakind is unknown, this one will be called
local default_inspector = introspection.inspectors.number
local inspect

-- recursion must be handled with some care: if we call regular introspection.inspect
-- we may create boxed references or Lua native objects which will be inspected as such
-- (leading to wrong type names).
local function recurse(name, value, parent, fullname, refct)
    if type(value) == "cdata" then
        return inspect(name, value, parent, fullname, refct)
    else
        local prop = introspection.inspect(name, value, parent, fullname)
        if prop then
            prop.attr.type = make_typename(refct)
        end
        return prop
    end
end

-- cdata specific inspectors
local inspectors = {
    struct = function(name, value, parent, fullname, refct)
        local prop = introspection.property(name, make_typename(refct), tostring(value), parent, fullname)

        -- inspect children, if needed
        if prop then
            for member in refct:members() do
                local mname = member.name
                recurse(mname, value[mname], prop, fullname .. sformat('[%q]', mname), member)
            end
        end
        return prop
    end,

    array = function(name, value, parent, fullname, refct)
        local etype = refct.element_type
        -- for VLAs, reflect does not give size
        local size = refct.size ~= "none" and refct.size or ffi.sizeof(value)
        size = size and (size / etype.size) -- we've got the byte size, not element count
        
        local typename = make_typename(etype)
        local prop = introspection.property(name, typename .. "[" .. (tostring(size) or "") .. "]", tostring(value), parent, fullname)
        
        if prop and size then
            for i=0, size-1 do
                local idx = "["..tostring(i).."]"
                recurse(idx, value[i], prop, fullname .. idx, etype)
            end
        end
        return prop
    end,

    func = function(name, value, parent, fullname, refct)
        local args = { }
        for arg in refct:arguments() do
            args[#args + 1] = make_typename(arg.type) .. " " .. arg.name
        end
        
        if refct.vararg then
            args[#args + 1] = "..."
        end
        
        local repr = make_typename(refct.return_type) .. " " .. refct.name .. "(" .. tconcat(args, ", ") .. ")"
        return introspection.property(name, make_typename(refct), repr, parent, fullname)
    end,

    enum = function(name, value, parent, fullname, refct)
        local repr = tonumber(value)
        -- try to convert numeric value into enum name
        --TODO: is there a faster method to make it ?
        for val in refct:values() do
            if val.value == repr then
                repr = val.name
                break
            end
        end
        
        return introspection.property(name, make_typename(refct), tostring(repr), parent, fullname)
    end,
    
    ref = function(name, value, parent, fullname, refct)
        -- this may be unsafe, see inspect_references setting
        local typename = make_typename(refct)
        if not M.inspect_references then
            return introspection.property(name, typename, tostring(value), parent, fullname)
        end
        
        local prop = recurse(name, value, parent, fullname, refct.element_type) 
        if prop then
            prop.attr.type = typename
        end
        return prop
    end,
    
    int = function(name, value, parent, fullname, refct)
        return introspection.property(name, make_typename(refct), tostring(tonumber(value)), parent, fullname)
    end,
    
    -- pointers are too unsafe, do not inspect them
    ptr = function(name, value, parent, fullname, refct)
        return introspection.property(name, make_typename(refct), tostring(value), parent, fullname)
    end,
}

inspectors.union = inspectors.struct
inspectors.float = inspectors.int

-- for struct/union fields, the actual type is nested into the refct 
inspectors.field = function(name, value, parent, fullname, refct)
    return inspect(name, value, parent, fullname, refct.type)
end
inspectors.bitfield = inspectors.field

inspect = function(name, value, parent, fullname, refct)
    -- inspect only values, not ctypes
    --FIXME: this cause references to be dereferenced and crash the process if they are wrong !
    if ffi.typeof(value) ~= value then
        refct = refct or reflect.typeof(value)
        return (inspectors[refct.what] or default_inspector)(name, value, parent, fullname, refct) 
    end
    
    -- return a simple property for ctypes
    return introspection.property(name, "ctype", tostring(value), parent, fullname)
end

introspection.inspectors.cdata = inspect

return M
