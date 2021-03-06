--------------------------------------------------------------------------------
--  Copyright (c) 2011-2012 Sierra Wireless.
--  All rights reserved. This program and the accompanying materials
--  are made available under the terms of the Eclipse Public License v1.0
--  which accompanies this distribution, and is available at
--  http://www.eclipse.org/legal/epl-v10.html
--
--  Contributors:
--       Simon BERNARD <sbernard@sierrawireless.com>
--           - initial API and implementation and initial documentation
--------------------------------------------------------------------------------
local apimodel = require "models.apimodel"
local ldp      = require "models.ldparser"
local Q        = require "metalua.treequery"

local M = {}

local handledcomments={} -- cache to know the comment already handled

----
-- UTILITY METHODS
local primitivetypes = {
  ['boolean']  = true,
  ['function'] = true,
  ['nil']      = true,
  ['number']   = true,
  ['string']   = true,
  ['thread']   = true,
  ['userdata'] = true,
  ['list']     = true,
  ['map']      = true,
  ['any']      = true

}

-- get or create the typedef with the name "name"
local function gettypedef(_file,name,kind,sourcerangemin,sourcerangemax)
  local kind = kind or "recordtypedef"
  local _typedef = _file.types[name]
  if _typedef then
    if _typedef.tag == kind then return _typedef end
  else
    if kind == "recordtypedef" and name ~= "global" then
      local _recordtypedef = apimodel._recordtypedef(name)

      -- define sourcerange
      _recordtypedef.sourcerange.min = sourcerangemin
      _recordtypedef.sourcerange.max = sourcerangemax

      -- add to file if a name is defined
      if _recordtypedef.name then _file:addtype(_recordtypedef) end
      return _recordtypedef
    elseif kind == "functiontypedef" then
      -- TODO support function
      return nil
    else
      return nil
    end
  end
  return nil
end

local _createreturn = {}

-- create a typeref from the typref doc_tag
local function createtyperef(dt_typeref,_file,sourcerangemin,sourcerangemax)
  local _typeref
  if dt_typeref.tag == "typeref" then
    if dt_typeref.module then
      -- manage external type
      _typeref = apimodel._externaltypref()
      _typeref.modulename = dt_typeref.module
      _typeref.typename = dt_typeref.type
    else
      if dt_typeref.type == "table" then
        -- manage special table type
        _typeref = apimodel._inlinetyperef(apimodel._recordtypedef("table"))
      elseif dt_typeref.type == "list" or dt_typeref.type == "map" then
        -- manage structures
        local structuretypedef = apimodel._recordtypedef(dt_typeref)
        structuretypedef.defaultvaluetyperef = createtyperef(dt_typeref.valuetype)
        if dt_typeref.type == "map" then
          structuretypedef.defaultkeytyperef = createtyperef(dt_typeref.keytype)
        end
        structuretypedef.structurekind = dt_typeref.type
        structuretypedef.name = dt_typeref.type
        _typeref = apimodel._inlinetyperef(structuretypedef)
      elseif dt_typeref.type == "function" then
        local functiontypedef = apimodel._functiontypedef()
        -- manage params
        if dt_typeref["parameters"] then
          for _, dt_param in ipairs(dt_typeref["parameters"]) do
            local _param = apimodel._parameter(dt_param.name or dt_param.type.._)
            _param.type = createtyperef(dt_param,_file,dt_param.lineinfo.first.offset, dt_param.lineinfo.last.offset)
            table.insert(functiontypedef.params,_param)
          end
        end
        -- manage returns
        if dt_typeref["returns"] then
          for _, dt_return in ipairs(dt_typeref["returns"]) do
            local _return = _createreturn[0](dt_return,_file,sourcerangemin,sourcerangemax)
            table.insert(functiontypedef.returns,_return)
          end
        end

        -- add type name
        functiontypedef.name = M.generatefunctiontypename(functiontypedef)
        _typeref = apimodel._inlinetyperef(functiontypedef)
      elseif dt_typeref.type == "meta" then
        _typeref = apimodel._metatyperef(dt_typeref["index"])
      elseif primitivetypes[dt_typeref.type] then
        -- manage primitive types
        _typeref = apimodel._primitivetyperef()
        _typeref.typename = dt_typeref.type
      else
        -- manage internal type
        _typeref = apimodel._internaltyperef()
        _typeref.typename = dt_typeref.type
        if _file then
          gettypedef(_file, _typeref.typename, "recordtypedef", sourcerangemin,sourcerangemax)
        end
      end
    end
  end
  return _typeref
end

-- create a return from the return doc_tag
local function createreturn(dt_return,_file,sourcerangemin,sourcerangemax)
  local _return = apimodel._return()

  _return.description = dt_return.description

  -- manage typeref
  if dt_return.types then
    for _, dt_typeref in ipairs(dt_return.types) do
      local _typeref = createtyperef(dt_typeref,_file,sourcerangemin,sourcerangemax)
      if _typeref then
        table.insert(_return.types,_typeref)
      end
    end
  end
  return _return
end

_createreturn[0] = createreturn

-- create a item from the field doc_tag
local function createfield(dt_field,_file,sourcerangemin,sourcerangemax)
  local _item = apimodel._item(dt_field.name)

  if dt_field.shortdescription then
    _item.shortdescription = dt_field.shortdescription
    _item.description = dt_field.description
  else
    _item.shortdescription = dt_field.description
  end

  -- manage typeref
  local dt_typeref = dt_field.type
  if dt_typeref then
    _item.type =  createtyperef(dt_typeref,_file,sourcerangemin,sourcerangemax)
  end
  return _item
end

-- create a param from the param doc_tag
local function createparam(dt_param,_file,sourcerangemin,sourcerangemax)
  if not dt_param.name then return nil end

  local _parameter = apimodel._parameter(dt_param.name)
  _parameter.description = dt_param.description

  -- manage typeref
  local dt_typeref = dt_param.type
  if dt_typeref then
    _parameter.type =  createtyperef(dt_typeref,_file,sourcerangemin,sourcerangemax)
  end
  return _parameter
end

-- get or create the typedef with the name "name"
function M.additemtoparent(_file,_item,scope,sourcerangemin,sourcerangemax)
  if scope and not scope.module then
    if _item.name then
      if scope.type == "global" then
        _file:addglobalvar(_item)
      else
        local _recordtypedef = gettypedef (_file, scope.type ,"recordtypedef",sourcerangemin,sourcerangemax)
        _recordtypedef:addfield(_item)
      end
    else
      -- if no item name precise we store the scope in the item to be able to add it to the right parent later
      _item.scope = scope
    end
  end
end

-- Function type counter
local i = 0

-- Reset function type counter
local function resetfunctiontypeidgenerator()
  i = 0
end

-- Provides an unique index for a function type
local function generatefunctiontypeid()
  i = i + 1
  return i
end

-- generate a function type name
local function generatefunctiontypename(_functiontypedef)
  local name = {"__"}
  if _functiontypedef.returns and _functiontypedef.returns[1] then
    local ret =  _functiontypedef.returns[1]
    for _, type in ipairs(ret.types) do
      if type.typename then
        if type.modulename then
          table.insert(name,type.modulename)
        end
        table.insert(name,"#")
        table.insert(name,type.typename)
      end
    end

  end
  table.insert(name,"=")
  if _functiontypedef.params then
    for _, param in ipairs(_functiontypedef.params) do
      local type =  param.type
      if type then
        if type.typename then
          if type.modulename then
            table.insert(name,type.modulename)
          end
          table.insert(name,"#")
          table.insert(name,type.typename)
        else
          table.insert(name,"#unknown")
        end
      end
      table.insert(name,"[")
      table.insert(name,param.name)
      table.insert(name,"]")
    end
  end
  table.insert(name,"__")
  table.insert(name, generatefunctiontypeid())
  return table.concat(name)
end

--
-- Store user defined tags
--
local function attachmetadata(apiobj, parsedcomment)
  local thirdtags = parsedcomment and parsedcomment.unknowntags
  if thirdtags  then
    -- Define a storage index for user defined tags on current API element
    if not apiobj.metadata then apiobj.metadata = {} end

    -- Loop over user defined tags
    for usertag, taglist in pairs(thirdtags) do
      if not apiobj.metadata[ usertag ] then
        apiobj.metadata[ usertag ] = {
          tag = usertag
        }
      end
      for _, tag in ipairs( taglist ) do
        table.insert(apiobj.metadata[usertag], tag)
      end
    end
  end
end


------------------------------------------------------
-- create the module api
function M.createmoduleapi(ast,modulename)

  -- Initialise function type naming
  resetfunctiontypeidgenerator()

  local _file = apimodel._file()

  local _comment2apiobj = {}

  local function handlecomment(comment)

    -- Extract information from tagged comments
    local parsedcomment = ldp.parse(comment[1])
    if not parsedcomment then return nil end

    -- Get tags from the languages
    local regulartags = parsedcomment.tags

    -- Will contain last API object generated from comments
    local _lastapiobject

    -- if comment is an ld comment
    if regulartags then
      -- manage "module" comment
      if regulartags["module"] then
        -- get name
        _file.name = regulartags["module"][1].name or modulename
        _lastapiobject = _file

        -- manage descriptions
        _file.shortdescription = parsedcomment.shortdescription
        _file.description = parsedcomment.description

        local sourcerangemin = comment.lineinfo.first.offset
        local sourcerangemax = comment.lineinfo.last.offset

        -- manage returns
        if regulartags ["return"] then
          for _, dt_return in ipairs(regulartags ["return"]) do
            local _return = createreturn(dt_return,_file,sourcerangemin,sourcerangemax)
            table.insert(_file.returns,_return)
          end
        end
        -- if no returns on module create a defaultreturn of type #modulename
        if #_file.returns == 0 and _file.name then
          -- create internal type ref
          local _typeref = apimodel._internaltyperef()
          _typeref.typename = _file.name

          -- create return
          local _return = apimodel._return()
          table.insert(_return.types,_typeref)

          -- add return
          table.insert(_file.returns,_return)

          --create recordtypedef is not define
          local _moduletypedef = gettypedef(_file,_typeref.typename,"recordtypedef",sourcerangemin,sourcerangemax)

          -- manage extends (inheritance) and structure tags
          if _moduletypedef and _moduletypedef.tag == "recordtypedef" then
            if regulartags["extends"] and regulartags["extends"][1] then
              local supertype = regulartags["extends"][1].type
              if supertype then _moduletypedef.supertype = createtyperef(supertype) end
            end
            if regulartags["map"] and regulartags["map"][1] then
              local keytype = regulartags["map"][1].keytype
              local valuetype = regulartags["map"][1].valuetype
              if keytype and valuetype then
                _moduletypedef.defaultkeytyperef = createtyperef(keytype)
                _moduletypedef.defaultvaluetyperef = createtyperef(valuetype)
                _moduletypedef.structurekind = "map"
                _moduletypedef.structuredescription = regulartags["map"][1].description
              end
            elseif regulartags["list"] and regulartags["list"][1] then
              local type = regulartags["list"][1].type
              if type then
                _moduletypedef.defaultvaluetyperef = createtyperef(type)
                _moduletypedef.structurekind = "list"
                _moduletypedef.structuredescription = regulartags["list"][1].description
              end
            end
          end
        end
        -- manage "type" comment
      elseif regulartags["type"] and regulartags["type"][1].name ~= "global" then
        local dt_type = regulartags["type"][1];
        -- create record type if it doesn't exist
        local sourcerangemin = comment.lineinfo.first.offset
        local sourcerangemax = comment.lineinfo.last.offset
        local _recordtypedef = gettypedef (_file, dt_type.name ,"recordtypedef",sourcerangemin,sourcerangemax)
        _lastapiobject = _recordtypedef

        -- re-set sourcerange in case the type was created before the type tag
        _recordtypedef.sourcerange.min = sourcerangemin
        _recordtypedef.sourcerange.max = sourcerangemax

        -- manage description
        _recordtypedef.shortdescription = parsedcomment.shortdescription
        _recordtypedef.description = parsedcomment.description

        -- manage fields
        if regulartags["field"] then
          for _, dt_field in ipairs(regulartags["field"]) do
            local _item = createfield(dt_field,_file,sourcerangemin,sourcerangemax)
            -- define sourcerange only if we create it
            _item.sourcerange.min = sourcerangemin
            _item.sourcerange.max = sourcerangemax
            if _item and _item.name then
              _recordtypedef:addfield(_item) end
          end
        end

        -- manage extends (inheritance)
        if regulartags["extends"] and regulartags["extends"][1] then
          local supertype = regulartags["extends"][1].type
          if supertype then _recordtypedef.supertype = createtyperef(supertype) end
        end

        -- manage structure tag
        if regulartags["map"] and regulartags["map"][1] then
          local keytype = regulartags["map"][1].keytype
          local valuetype = regulartags["map"][1].valuetype
          if keytype and valuetype then
            _recordtypedef.defaultkeytyperef = createtyperef(keytype)
            _recordtypedef.defaultvaluetyperef = createtyperef(valuetype)
            _recordtypedef.structurekind = "map"
            _recordtypedef.structuredescription = regulartags["map"][1].description
          end
        elseif regulartags["list"] and regulartags["list"][1] then
          local type = regulartags["list"][1].type
          if type then
            _recordtypedef.defaultvaluetyperef = createtyperef(type)
            _recordtypedef.structurekind = "list"
            _recordtypedef.structuredescription = regulartags["list"][1].description
          end
        end
      elseif regulartags["field"] then
        local dt_field = regulartags["field"][1]

        -- create item
        local sourcerangemin = comment.lineinfo.first.offset
        local sourcerangemax = comment.lineinfo.last.offset
        local _item = createfield(dt_field,_file,sourcerangemin,sourcerangemax)
        _item.shortdescription = parsedcomment.shortdescription
        _item.description = parsedcomment.description
        _lastapiobject = _item

        -- define sourcerange
        _item.sourcerange.min = sourcerangemin
        _item.sourcerange.max = sourcerangemax

        -- add item to its parent
        local scope = regulartags["field"][1].parent
        M.additemtoparent(_file,_item,scope,sourcerangemin,sourcerangemax)
      elseif regulartags["function"] or regulartags["param"] or regulartags["return"] or regulartags["callof"] then
        -- create item
        local _item = apimodel._item()
        _item.shortdescription = parsedcomment.shortdescription
        _item.description = parsedcomment.description
        _lastapiobject = _item

        -- set name
        if regulartags["function"] then _item.name =  regulartags["function"][1].name end

        -- define sourcerange
        local sourcerangemin = comment.lineinfo.first.offset
        local sourcerangemax = comment.lineinfo.last.offset
        _item.sourcerange.min = sourcerangemin
        _item.sourcerange.max = sourcerangemax


        -- create function type
        local _functiontypedef = apimodel._functiontypedef()
        _functiontypedef.shortdescription = parsedcomment.shortdescription
        _functiontypedef.description = parsedcomment.description


        -- manage params
        if regulartags["param"] then
          for _, dt_param in ipairs(regulartags["param"]) do
            local _param = createparam(dt_param,_file,sourcerangemin,sourcerangemax)
            table.insert(_functiontypedef.params,_param)
          end
        end

        -- manage returns
        if regulartags["return"] then
          for _, dt_return in ipairs(regulartags["return"]) do
            local _return = createreturn(dt_return,_file,sourcerangemin,sourcerangemax)
            table.insert(_functiontypedef.returns,_return)
          end
        end

        -- add type name
        _functiontypedef.name = generatefunctiontypename(_functiontypedef)
        attachmetadata(_functiontypedef, parsedcomment)
        _file:addtype(_functiontypedef)

        -- create ref to this type
        local _internaltyperef = apimodel._internaltyperef()
        _internaltyperef.typename = _functiontypedef.name
        _item.type=_internaltyperef

        -- add item to its parent
        local sourcerangemin = comment.lineinfo.first.offset
        local sourcerangemax = comment.lineinfo.last.offset
        local scope = (regulartags["function"] and regulartags["function"][1].parent) or nil
        M.additemtoparent(_file,_item,scope,sourcerangemin,sourcerangemax)

        -- manage callof
        if regulartags["callof"] and regulartags["callof"][1] and regulartags["callof"][1].type then
          -- get the type which will be callable !
          local _internaltyperef = createtyperef(regulartags["callof"][1].type)
          if _internaltyperef and _internaltyperef.tag == "internaltyperef" then
            local _typedeftypedef = gettypedef(_file,_internaltyperef.typename,"recordtypedef",sourcerangemin,sourcerangemax)
            if _typedeftypedef then
              -- refer the function used when the type is called
              local _internaltyperef = apimodel._internaltyperef()
              _internaltyperef.typename = _functiontypedef.name
              _typedeftypedef.call =_internaltyperef
            end
          end
        end
      end
    end

    -- when we could not know which type of api object it is, we suppose this is an item
    if not _lastapiobject then
      _lastapiobject = apimodel._item()
      _lastapiobject.shortdescription = parsedcomment.shortdescription
      _lastapiobject.description = parsedcomment.description
      _lastapiobject.sourcerange.min =  comment.lineinfo.first.offset
      _lastapiobject.sourcerange.max = comment.lineinfo.last.offset
    end

    attachmetadata(_lastapiobject, parsedcomment)

    -- if we create an api object linked it to
    _comment2apiobj[comment] =_lastapiobject
  end

  local function parsecomment(node, parent, ...)
    -- check for comments before this node
    if node.lineinfo and node.lineinfo.first.comments then
      local comments = node.lineinfo.first.comments
      -- check all comments
      for _,comment in ipairs(comments) do
        -- if not already handled
        if not handledcomments[comment] then
          handlecomment(comment)
          handledcomments[comment]=true
        end
      end
    end
    -- check for comments after this node
    if node.lineinfo and node.lineinfo.last.comments then
      local comments = node.lineinfo.last.comments
      -- check all comments
      for _,comment in ipairs(comments) do
        -- if not already handled
        if not handledcomments[comment] then
          handlecomment(comment)
          handledcomments[comment]=true
        end
      end
    end
  end
  Q(ast):filter(function(x) return x.tag~=nil end):foreach(parsecomment)
  return _file, _comment2apiobj
end


function M.extractlocaltype ( commentblock,_file)
  if not commentblock then return nil end

  local stringcomment = commentblock[1]

  local parsedtag = ldp.parseinlinecomment(stringcomment)
  if parsedtag then
    local sourcerangemin = commentblock.lineinfo.first.offset
    local sourcerangemax = commentblock.lineinfo.last.offset

    return createtyperef(parsedtag,_file,sourcerangemin,sourcerangemax), parsedtag.description
  end

  return nil, stringcomment
end

M.generatefunctiontypename = generatefunctiontypename

return M
