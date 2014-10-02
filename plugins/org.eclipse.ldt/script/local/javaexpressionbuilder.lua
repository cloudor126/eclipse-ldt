--------------------------------------------------------------------------------
--  Copyright (c) 2013 Sierra Wireless.
--  All rights reserved. This program and the accompanying materials
--  are made available under the terms of the Eclipse Public License v1.0
--  which accompanies this distribution, and is available at
--  http://www.eclipse.org/legal/epl-v10.html
--
--  Contributors:
--           - initial API and implementation and initial documentation
--------------------------------------------------------------------------------

local javainternalmodelfactory = require 'javainternalmodelfactory'

local J = {}

--------------------------------------
-- create expression java object
function J._expression(_expr,handledexpr)
  -- search if already handled
  if handledexpr and handledexpr[_expr] then return handledexpr[_expr] end

  -- else handle it
  local tag = _expr.tag
  if tag == "MIdentifier" then
    return J._identifier(_expr,handledexpr)
  elseif tag == "MIndex" then
    return J._index(_expr,handledexpr)
  elseif tag == "MCall" then
    return J._call(_expr,handledexpr)
  elseif tag == "MInvoke" then
    return J._invoke(_expr,handledexpr)
  end
  return nil
end

--------------------------------------
-- create identifier java object
function J._identifier(_identifier,handledexpr)
  local jidentifier = javainternalmodelfactory.newidentifier(
    _identifier.sourcerange.min - 1,
    _identifier.sourcerange.max
  )
  handledexpr[_identifier] =jidentifier
  return jidentifier
end

--------------------------------------
-- create index java object
function J._index(_index,handledexpr)
  local jindex = javainternalmodelfactory.newindex(
    _index.sourcerange.min -1,
    _index.sourcerange.max,
    J._expression(_index.left,handledexpr),
    _index.right
  )
  handledexpr[_index] =jindex
  return jindex
end

--------------------------------------
-- create call java object
function J._call(_call,handledexpr)
  local jcall = javainternalmodelfactory.newcall(
    _call.sourcerange.min - 1,
    _call.sourcerange.max,
    J._expression(_call.func,handledexpr)
  )
  handledexpr[_call] =jcall
  return jcall
end

--------------------------------------
-- create invoke java object
function J._invoke(_invoke,handledexpr)
  local jinvoke = javainternalmodelfactory.newinvoke(
    _invoke.sourcerange.min - 1,
    _invoke.sourcerange.max,
    _invoke.functionname,
    J._expression(_invoke.record,handledexpr)
  )
  handledexpr[_invoke] =jinvoke
  return jinvoke
end

return J
