--------------------------------------------------------------------------------
--  Copyright (c) 2012-2013 Sierra Wireless.
--  All rights reserved. This program and the accompanying materials
--  are made available under the terms of the Eclipse Public License v1.0
--  which accompanies this distribution, and is available at
--  http://www.eclipse.org/legal/epl-v10.html
--
--  Contributors:
--       Kevin KIN-FOO <kkinfoo@sierrawireless.com>
--           - initial API and implementation and initial documentation
--------------------------------------------------------------------------------

require 'metalua.loader'
local compiler = require 'metalua.compiler'
local javamodelfactory = require 'javamodelfactory'

local mlc51, mlc52

-- Just redefining classic print, as there is a flush problem calling it from Java
local print = function(...) print(...) io.flush() end

local M = {}

function M.newMLC51()
  local function lexer51()
    local generic_lexer = require 'metalua.grammar.lexer'
    local lexer = generic_lexer.lexer :clone()

    local keywords = {
      "and", "break", "do", "else", "elseif",
      "end", "false", "for", "function",
      "if",
      "in", "local", "nil", "not", "or", "repeat",
      "return", "then", "true", "until", "while",
      "...", "..", "==", ">=", "<=", "~=",
      "+{", "-{"
    }

    for _, w in ipairs(keywords) do lexer :add (w) end

    return lexer
  end

  local newmlc = compiler.new()
  newmlc.parser.lexer = lexer51()
  newmlc.parser.stat:del("goto")
  newmlc.parser.stat:del("::")

  return newmlc
end

---
-- Build Java Model from source code
--
-- @param #string source Code to parse
-- @param LuaSourceRoot, DLTK node, root of DLTK AST
function M.build(source, modulename, root, luaGrammar)

  local mlc
  if (luaGrammar == "lua-5.1") then
    if (not mlc51) then
      mlc51 = M.newMLC51()
    end

    mlc = mlc51
  else
    if (not mlc52) then
      mlc52 = compiler.new()
    end

    mlc = mlc52
  end

  -- manage shebang
  if source then source = source:gsub("^(#.-\n)", function (s) return string.rep(' ',string.len(s)) end) end

  -- if no errors, check AST
  local ast = mlc:src_to_ast( source )

  -- Create api model
  local apimodelbuilder = require 'models.apimodelbuilder'
  local _file, comment2apiobj = apimodelbuilder.createmoduleapi(ast,modulename)

  -- create internal model
  local internalmodelbuilder = require "models.internalmodelbuilder"
  local _internalcontent = internalmodelbuilder.createinternalcontent(ast,_file,comment2apiobj,modulename)

  -- Converting api model to java
  local javaapimodelbuilder = require 'javaapimodelbuilder'
  local jfile, handledexpr = javaapimodelbuilder._file(_file)

  -- Converting internal model to java
  local javainternalmodelbuilder = require 'javainternalmodelbuilder'
  local jinternalcontent = javainternalmodelbuilder._internalcontent(_internalcontent,_file, handledexpr)

  -- Append information from documentation
  javamodelfactory.addcontent(root,jfile,jinternalcontent)

  local handledcomments={}

  -- TODO clean perf profiling
  -- local e = os.clock()
  -- print ('global time', type(e), type(s),(e*1000-s*1000))
end

return M
