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
local mlc = compiler.new()

local javamodelfactory = require 'javamodelfactory'

-- Just redefining classic print, as there is a flush problem calling it from Java
local print = function(...) print(...) io.flush() end

local M = {}

---
-- Build Java Model from source code
--
-- @param #string source Code to parse
-- @return  LuaSourceRoot, DLTK node, root of DLTK AST
function M.build(source, modulename)
  -- create root object
  local root = javamodelfactory.newsourceroot(#source)

  -- TODO clean perf profiling
  -- local s = os.clock()

  -- manage shebang
  if source then source = source:gsub("^(#.-\n)", function (s) return string.rep(' ',string.len(s)) end) end

  -- check for errors
  local f, err = loadstring(source,'source_to_check')
  if not f then
    local lineindex, errmessage = string.match(err,"%[string \"source_to_check\"%]:(%d+):(.*)")
    errmessage = errmessage or err
    lineindex = lineindex and tonumber(lineindex) or 1
    javamodelfactory.setproblem(root,lineindex-1 , -1, -1, -1, errmessage)

    -- -------------------------------------------------
    -- EXPERIMENTAL CODE : we try to remove faulty code
    -- -------------------------------------------------
    if source then
      -- define function that replace all character of a given line in space characters
      local function cleanline (source, linetoclean)
      	local cleanedsource
      	local iscleaned = false
        if linetoclean == 1 then
          -- manage first line
          cleanedsource = source:gsub('^(.-)\n',function (firstline)
         	  iscleaned = true
            return string.rep(' ',string.len(firstline)) .. "\n"
          end)
        elseif linetoclean > 1 then
          -- manage other case
            cleanedsource = source:gsub('^('..string.rep(".-\n",linetoclean-1)..')(.-)\n',function (start,faultyline)
            iscleaned = true
            return start..string.rep(' ',string.len(faultyline)) .. "\n"
          end)
        end
        return cleanedsource, iscleaned
      end
	
			local cleanedsource
     	local iscleaned = false
      if lineindex == 1 then
        -- FIRST LINE CASE : error is on line 1, just clean this line and check for errors
        cleanedsource, iscleaned = cleanline(source,1)
        f, _ = loadstring(cleanedsource,'source_to_check')
      else
        -- OTHER CASES: first, cleaning ...
        -- if something is not closed we try to remove the line where it is opened.
        local linestart = string.match(err,"%(to close .* at line (%d+)%)")
        if linestart then
          cleanedsource, iscleaned = cleanline(source,tonumber(linestart))
        elseif lineindex > 1 then
          -- in other case, we try to remove the "real" code line before the error
          -- so, we start by finding the "real" line:
          local realcodelineindex = nil
          for i=lineindex-1,1,-1  do
            -- we go from the line just before the error to the first line, searching a "real" code line.
            -- (no empty line or single comment line, we do not manage multiline comment)
            local codeline = source:match('^'..string.rep(".-\n",i-1)..'(.-)\n')
            if codeline and not codeline:find('^%s*$') and not codeline:find('^%s*%-%-.*$')   then
              realcodelineindex = i
              break
            end
          end
          if realcodelineindex then
            cleanedsource, iscleaned = cleanline(source,realcodelineindex)
          end
        end
      
	      -- after cleaning, recheck hoping there are no errors.
	      if iscleaned then
	        f, _ = loadstring(cleanedsource,'source_to_check')
	        -- if it fail, we try to remove the line in error
	        if not f then
	          cleanedsource = cleanline(source,lineindex)
	          f, _ = loadstring(cleanedsource,'source_to_check')
	        end
	      end
      end
      
      -- take cleaned source as source
      if f then
      	source = cleanedsource
      end
	      
      -- TODO clean perf profiling
      -- local e = os.clock()
      -- print ('error time', (e*1000-s*1000))     
    end
    -- ------------------------------------------------
    -- END OF EXPERIMENTAL CODE
    -- -------------------------------------------------
  end

  if not f then return root end

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
  return root
end

return M
