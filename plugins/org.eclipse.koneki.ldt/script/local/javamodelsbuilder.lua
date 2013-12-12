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

require 'metalua.package'
local compiler = require 'metalua.compiler'
local mlc = compiler.new()

local javamodelfactory = require 'javamodelfactory'

-- Just redefining classic print, as there is a flush problem calling it from Java
local print = function(...) print(...) io.flush() end

local M = {}

---
-- Build Java Model from source code
--
-- @param	source Code to parse
-- @return	LuaSourceRoot, DLTK node, root of DLTK AST
function M.build(source, modulename)
	-- create root object
	local root = javamodelfactory.newsourceroot(#source)
	
	-- manage shebang
	if source then source = source:gsub("^(#.-\n)", function (s) return string.rep(' ',string.len(s)) end) end
	-- check for errors
	local f, err = loadstring(source,'source_to_check')
	if not f then
		local line, errmessage = string.match(err,"%[string \"source_to_check\"%]:(%d+):(.*)")
		errmessage = errmessage or err
		line = line and tonumber(line)-1 or 0
		
		-- calculate the start of some errors
		-- NOT USED FOR NOW
--		 local linestart = string.match(err,"%(to close .* at line (%d+)%)") 
--		 if linestart then
--			local _, startoffset = string.find(source,string.rep("[^\n]*\n",linestart-1))
--			javamodelfactory.setproblem(root, line , -1, startoffset, endoffset, err)
--		else
--			javamodelfactory.setproblem(root, line , -1, -1, endoffset, err)
--		end
		javamodelfactory.setproblem(root, line , -1, -1, -1, errmessage)
		return root
	end
	
	
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
	return root
end

return M
