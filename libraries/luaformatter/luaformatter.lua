--------------------------------------------------------------------------------
-- Copyright (c) 2011, 2013 Sierra Wireless and others.
-- All rights reserved. This program and the accompanying materials
-- are made available under the terms of the Eclipse Public License v1.0
-- which accompanies this distribution, and is available at
-- http://www.eclipse.org/legal/epl-v10.html
--
-- Contributors:
--     Sierra Wireless - initial API and implementation
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
-- Uses Metalua capabilities to indent code and provide source code offset
-- semantic depth.
--
-- @module luaformatter
-- 
--------------------------------------------------------------------------------

-- -----------------------------------------------------------------------------
-- This module is still work in progress, here some point to work on:
-- * Due to the walker some node are treated 2times. (see Call nodes)
-- * Avoid to go forward when obiously to identation have to be done (e.g. when to parameters to a function, or a call one line line)
-- * Get out the helper functions form the walker table.
-- * Re-work the indent function, especially the way to restore indentation.
-- -----------------------------------------------------------------------------

local M = {}
require 'metalua.package'
local math = require 'math'
local mlc  = require 'metalua.compiler'.new()

--
-- Define AST walker
--
local walker = {
  block = {},
  -- Indentations line number
  indentation = {},
  indenttable = true,
  -- Key:   Line number to indent back.
  -- Value: Previous line number, it has the indentation depth wanted.
  reference = {},
  source = ""
}

local INDENT = true

function walker.block.down(node, parent)
  -- Ignore empty node
  if #node == 0 or not parent then
    return
  end
  walker.indentchunk(node, parent)
end

--------------------------------------------------------------------------------
-- Format walking utilities
--------------------------------------------------------------------------------

---
-- Indents `Local and `Set
local function assignments(node)

  -- Indent only when node spreads across several lines
  local nodestart = walker.getfirstline(node, true)
  local nodeend = walker.getlastline(node)
  if nodestart >= nodeend then
    return
  end

  -- Format it
  local lhs, exprs = unpack(node)
  if #exprs == 0 then
    -- Regular `Local handling
    walker.indentexprlist(lhs, node)
    -- Avoid problems and format functions later.
  elseif not (#exprs == 1 and exprs[1].tag == 'Function') then

    -- for local, indent lhs
    if node.tag == 'Local' then

      -- Else way, indent LHS and expressions like a single chunk.
      local endline = walker.getlastline(exprs)
      local startline, startindex = walker.getfirstline(lhs, true)
      walker.indent(startline, startindex, endline, node)

    end

    -- In this chunk indent expressions one more.
    walker.indentexprlist(exprs, node)
  end
end

---
-- Indents parameters
--
-- @param callable  Node containing the params
-- @param firstparam first parameter of the given callable
local function indentparams(firstparam, lastparam, parent)

  -- Determine parameters first line
  local paramstartline,paramstartindex = walker.getfirstline(firstparam)

  -- Determine parameters last line
  local paramlastline = walker.getlastline(lastparam)

  -- indent
  walker.indent(paramstartline, paramstartindex, paramlastline, parent)
end

---
-- Comment adjusted first line and first offset of a node.
--
-- @return #int, #int
function walker.getfirstline(node, ignorecomments)
  -- Consider preceding comments as part of current chunk
  -- WARNING: This is NOT the default in Metalua
  local first, offset
  local offsets = node.lineinfo
  if offsets.first.comments and not ignorecomments then
    first = offsets.first.comments.lineinfo.first.line
    offset = offsets.first.comments.lineinfo.first.offset
  else
    -- Regular node
    first = offsets.first.line
    offset = offsets.first.offset
  end
  return first, offset
end

---
-- Last line of a node.
--
-- @return #int
function walker.getlastline(node)
  return node.lineinfo.last.line , node.lineinfo.last.offset
end

function walker.indent(startline, startindex, endline, parent)

  -- Indent following lines when current one does not start with first statement
  -- of current block.
  if not walker.source:sub(1,startindex-1):find("[\r\n]%s*$") then
    startline = startline + 1
  end

  -- Nothing interesting to do
  if endline < startline then
    return
  end

  -- Indent block first line
  walker.indentation[startline] = INDENT

  -- Restore indentation
  if not walker.reference[endline+1] then
    -- Only when not performed by a higher node
    walker.reference[endline+1] = walker.getfirstline(parent)
  end
end

---
-- Indent all lines of a chunk.
function walker.indentchunk(node, parent)

  -- Get regular start
  local startline, startindex = walker.getfirstline(node[1])

  -- Handle trailing comments as they were statements
  local endline
  local lastnode = node[#node]
  if lastnode.lineinfo.last.comments then
    endline = lastnode.lineinfo.last.comments.lineinfo.last.line
  else
    endline = lastnode.lineinfo.last.line
  end

  walker.indent(startline, startindex, endline, parent)
end

---
-- Indent all lines of an expression list.
function walker.indentexprlist(node, parent, ignorecomments)
  local endline = walker.getlastline(node)
  local startline, startindex = walker.getfirstline(node, ignorecomments)
  walker.indent(startline, startindex, endline, parent)
end

--------------------------------------------------------------------------------
-- Expressions formatters
--------------------------------------------------------------------------------
function walker.String(node)
  local firstline, _ = walker.getfirstline(node,true)
  local lastline = walker.getlastline(node)
  for line=firstline+1, lastline do
    walker.indentation[line]=false
  end
end

function walker.Table(node, parent)

  if not walker.indenttable then
    return
  end

  -- Format only inner values across several lines
  local firstline, firstindex = walker.getfirstline(node,true)
  local lastline = walker.getlastline(node)
  if #node > 0 and firstline < lastline then

    -- Determine first line to format
    local firstnode = unpack(node)
    local childfirstline, childfirstindex = walker.getfirstline(firstnode)

    -- Determine last line to format
    local lastnode = #node == 1 and firstnode or node[ #node ]
    local childlastline = walker.getlastline(lastnode)

    -- Actual formating
    walker.indent(childfirstline, childfirstindex, childlastline, node)
  end
end

--------------------------------------------------------------------------------
-- Statements formatters
--------------------------------------------------------------------------------
function walker.Call(node, parent)
  local expr, firstparam = unpack(node)
  if firstparam then
    indentparams(firstparam, node[#node], node)
  end
end


function walker.Forin(node)
  local ids, iterator, _ = unpack(node)
  walker.indentexprlist(ids, node)
  walker.indentexprlist(iterator, node)
end

function walker.Fornum(node)
  -- Format from variable name to last expressions
  local var, init, limit, range = unpack(node)
  local startline, startindex   = walker.getfirstline(var)

  -- Take range as last expression, when not available limit will do
  local lastexpr = range.tag and range or limit
  walker.indent(startline, startindex, walker.getlastline(lastexpr), node)
end

function walker.Function(node)
  local params, chunk = unpack(node)
  walker.indentexprlist(params, node)
end

function walker.Index(node,parent)

  -- Bug 422778 - [ast] Missing a lineinfo attribute on one Index 
  -- the following if is a workaround avoid a nil exception but the formatting of the current node is avoided.
  if not node.lineinfo then
    return
  end
  -- avoid indent if the index is on one line
  local nodestartline = node.lineinfo.first.line
  local nodeendline = node.lineinfo.last.line
  if nodeendline == nodestartline then
    return
  end


  local left, right = unpack(node)
  -- Bug 422778 [ast] Missing a lineinfo attribute on one Index
  -- the following line is a workaround avoid a nil exception but the formatting of the current node is avoided.
  if left.lineinfo then
    local leftendline, leftendoffset = walker.getlastline(left)
    -- For Call,Set and Local nodes we want to indent to end of the parent node not only the index itself
    if (parent[1] == node and parent.tag == 'Call') or
      (parent[1] and #parent[1] ==  1 and parent[1][1] == node and (parent.tag == 'Set' or parent.tag == 'Local')) then
      
      local parentendline = walker.getlastline(parent)
      walker.indent(leftendline, leftendoffset+1, parentendline, parent)
    else
      local rightendline = walker.getlastline(right)
      walker.indent(leftendline, leftendoffset+1, rightendline, node)
    end
  end

end

function walker.If(node)
  -- Indent only conditions, chunks are already taken care of.
  local nodesize = #node
  for conditionposition=1, nodesize-(nodesize%2), 2 do
    walker.indentexprlist(node[conditionposition], node)
  end
end

function walker.Invoke(node, parent)
  local expr, str, firstparam = unpack(node)

  --indent str
  local exprendline, exprendoffset = walker.getlastline(expr)
  local nodeendline = walker.getlastline(node)
  walker.indent(exprendline, exprendoffset+1, nodeendline, node)

  --indent parameters
  if firstparam then
    indentparams(firstparam, node[#node], str)
  end

end

walker.Local = assignments

function walker.Repeat(node)
  local _, expr = unpack(node)
  walker.indentexprlist(expr, node)
end

function walker.Return(node, parent)
  if #node > 0 then
    walker.indentchunk(node, parent)
  end
end

walker.Set = assignments

function walker.While(node)
  local expr, _ = unpack(node)
  walker.indentexprlist(expr, node)
end

--------------------------------------------------------------------------------
-- Calculate all indent level
-- @param Source code to analyze
-- @return #table {linenumber = indentationlevel}
-- @usage local depth = format.indentLevel("local var")
--------------------------------------------------------------------------------
local function getindentlevel(source, indenttable)

  if not loadstring(source, 'CheckingFormatterSource') then
    return
  end

  -- ---------------------------------------------------------------------------
  -- Walk through AST
  --
  -- Walking the AST, we store which lines deserve one and always one
  -- indentation.
  --
  -- We will not indent back. To obtain a smaller indentation, we will refer to
  -- a less indented preceding line.
  --
  -- Why so complicated?
  -- We use two tables simply for handling the case of the one line indentation.
  -- We choose to use reference to preceding line to avoid handle indent back
  -- computation and mistakes. When leaving a node after formatting it, we
  -- simply uses indentation of before entering this node.
  -- ---------------------------------------------------------------------------
  local walk = require 'metalua.walk'
  local ast = mlc:src_to_ast(source)
  walker.indenttable = indenttable
  walker.indentation = {}
  walker.reference = {}
  walker.source = source
  walk.block(walker, ast)

  -- Built depth table
  local currentdepth = 0
  local depthtable = {}
  for line=1, walker.getlastline(ast[#ast]) do

    -- Restore depth
    if walker.reference[line] then
      currentdepth = depthtable[walker.reference[line]]
    end

    -- Indent
    if walker.indentation[line] then
      currentdepth = currentdepth + 1
      depthtable[line] = currentdepth
    elseif walker.indentation[line] == false then
      -- Ignore any kind of indentation
      depthtable[line] = false
    else
      -- Use current indentation
      depthtable[line] = currentdepth
    end

  end
  return depthtable
end

--------------------------------------------------------------------------------
-- Trim white spaces before and after given string
--
-- @usage local trimmedstr = trim('          foo')
-- @param #string string to trim
-- @return #string string trimmed
--------------------------------------------------------------------------------
local function trim(string)
  local pattern = "^(%s*)(.*)"
  local _, strip =  string:match(pattern)
  if not strip then return string end
  local restrip
  _, restrip = strip:reverse():match(pattern)
  return restrip and restrip:reverse() or strip
end

--------------------------------------------------------------------------------
-- Indent Lua Source Code.
--
-- @function [parent=#luaformatter] indentcode
-- @param source source code to format
-- @param delimiter line delimiter to use
-- @param indenttable true if you want to indent in table
-- @param ...
-- @return #string formatted code
-- @usage indentCode('local var', '\n', true, '\t',)
-- @usage indentCode('local var', '\n', true, --[[tabulationSize]]4, --[[indentationSize]]2)
--------------------------------------------------------------------------------
function M.indentcode(source, delimiter,indenttable, ...)
  --
  -- Create function which will generate indentation
  --
  local tabulation
  if select('#', ...) > 1 then
    local tabSize = select(1, ...)
    local indentationSize = select(2, ...)
    -- When tabulation size and indentation size is given, tabulation is
    -- composed of tabulation and spaces
    tabulation = function(depth)
      local range = depth * indentationSize
      local tabCount = math.floor(range / tabSize)
      local spaceCount = range % tabSize
      local tab = '\t'
      local space = ' '
      return tab:rep(tabCount) .. space:rep(spaceCount)
    end
  else
    local char = select(1, ...)
    -- When tabulation character is given, this character will be duplicated
    -- according to length
    tabulation = function (depth) return char:rep(depth) end
  end

  -- Delimiter position table
  -- Initialization represent string start offset
  local delimiterLength = delimiter:len()
  local positions = {1-delimiterLength}

  --
  -- Seek for delimiters
  --
  local i = 1
  local delimiterPosition = nil
  repeat
    delimiterPosition = source:find(delimiter, i, true)
    if delimiterPosition then
      positions[#positions + 1] = delimiterPosition
      i = delimiterPosition + 1
    end
  until not delimiterPosition
  -- No need for indentation, while no delimiter has been found
  if #positions < 2 then
    return source
  end

  -- calculate indentation
  local linetodepth = getindentlevel(source,indenttable)

  -- Concatenate string with right indentation
  local indented = {}
  for  position=1, #positions do
    -- Extract source code line
    local offset = positions[position]
    -- Get the interval between two positions
    local rawline
    if positions[position + 1] then
      rawline = source:sub(offset + delimiterLength, positions[position + 1] -1)
    else
      -- From current position to end of line
      rawline = source:sub(offset + delimiterLength)
    end

    -- Trim white spaces
    local indentcount = linetodepth[position]
    if not indentcount then
      indented[#indented+1] = rawline
    else
      local line = trim(rawline)
      -- Append right indentation
      -- Indent only when there is code on the line
      if line:len() > 0 then
        -- Compute next real depth related offset
        -- As is offset is pointing a white space before first statement
        -- of block,
        -- We will work with parent node depth
        indented[#indented+1] = tabulation( indentcount )
        -- Append trimmed source code
        indented[#indented+1] = line
      end
    end
    -- Append carriage return
    -- While on last character append carriage return only if at end of
    -- original source
    local endofline = source:sub(source:len()-delimiterLength, source:len())
    if position < #positions or endofline == delimiter then
      indented[#indented+1] = delimiter
    end
  end

  return table.concat(indented)
end

return M
