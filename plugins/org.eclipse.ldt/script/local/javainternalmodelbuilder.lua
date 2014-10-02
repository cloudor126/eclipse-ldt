--------------------------------------------------------------------------------
--  Copyright (c) 2012 Sierra Wireless.
--  All rights reserved. This program and the accompanying materials
--  are made available under the terms of the Eclipse Public License v1.0
--  which accompanies this distribution, and is available at
--  http://www.eclipse.org/legal/epl-v10.html
--
--  Contributors:
--       Kevin KIN-FOO <kkinfoo@sierrawireless.com>
--           - initial API and implementation and initial documentation
--------------------------------------------------------------------------------
local J = {}
local javaapimodelbuilder = require 'javaapimodelbuilder'
local javaexpressionbuilder = require 'javaexpressionbuilder'

local javainternalmodelfactory = require 'javainternalmodelfactory'
local javaapimodelfactory      = require 'javaapimodelfactory'

--------------------------------------
-- create internal content java object
function J._internalcontent(_internalcontent,_file,handledexpr)

  -- Setting body
  local handledexpr = handledexpr or {}
  local jblock = J._block(_internalcontent.content,handledexpr)
  local jinternalcontent = javainternalmodelfactory.newinternalmodel(jblock)

  -- Appending unknown global variables
  for _, _item in ipairs(_internalcontent.unknownglobalvars) do
    local jitem = javaapimodelbuilder._item(_item,true,handledexpr)
    javainternalmodelfactory.addunknownglobalvar(jinternalcontent,jitem)

    -- add occurrences
    for _,_occurrence in ipairs(_item.occurrences) do
      local jidentifier = handledexpr[_occurrence]
      if jidentifier then
        javaapimodelfactory.addoccurrence(jitem,jidentifier)
      end
    end
  end

  -- Appending global variables
  for _, _item in pairs(_file.globalvars) do
    local jitem = handledexpr[_item]
    if _item.type and _item.type.tag == "exprtyperef" then
      javaapimodelfactory.setexpression(jitem,handledexpr[_item.type.expression])
    end

    -- add occurrences
    if jitem then
      for _,_occurrence in ipairs(_item.occurrences) do
        local jidentifier = handledexpr[_occurrence]
        if jidentifier then
          javaapimodelfactory.addoccurrence(jitem,jidentifier)
        end
      end
    end
  end


  return jinternalcontent
end

--------------------------------------
-- create block java object
function J._block(_block,handledexpr)
  -- Setting source range
  local jblock = javainternalmodelfactory.newblock(
    _block.sourcerange.min -1,
    _block.sourcerange.max
  )

  -- Append nodes to block
  for _, _expr in pairs(_block.content) do
    local jexpr = javaexpressionbuilder._expression(_expr,handledexpr)
    if not jexpr and _expr.tag == "MBlock" then
      jexpr = J._block(_expr,handledexpr)
    end
    javainternalmodelfactory.addcontent(jblock,jexpr)
  end

  for _, _localvar in pairs(_block.localvars) do
    -- Create Java item
    local jitem = javaapimodelbuilder._item(_localvar.item,true,handledexpr)

    -- add occurrence
    for _,_occurrence in ipairs(_localvar.item.occurrences) do
      local jidentifier = handledexpr[_occurrence]
      if jidentifier then
        javaapimodelfactory.addoccurrence(jitem,jidentifier)
      end
    end

    -- Append Java local variable definition
    local jlocalvar = javainternalmodelfactory.newlocalvar(
      jitem,
      _localvar.scope.min -1,
      _localvar.scope.max
    )
    javainternalmodelfactory.addlocalvar(jblock,jlocalvar)
  end
  return jblock
end
return J
