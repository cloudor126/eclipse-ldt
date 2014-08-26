#!/usr/bin/lua
-------------------------------------------------------------------------------
-- Copyright (c) 2012 Sierra Wireless and others.
-- All rights reserved. This program and the accompanying materials
-- are made available under the terms of the Eclipse Public License v1.0
-- which accompanies this distribution, and is available at
-- http://www.eclipse.org/legal/epl-v10.html
--
-- Contributors:
--     Sierra Wireless - initial API and implementation
-------------------------------------------------------------------------------
 
-- Fetch libraries form current plugin
package.path = './?.lua;../lib/?.lua;../../../libraries/metalua/?.lua;../../../libraries/modelsbuilder/?.lua;../../../libraries/luaformatter/?.lua;../../../libraries/templateengine/?.lua;../../../libraries/penlight/?.lua;../../../libraries/doctemplates/?.lua;../../../libraries/markdown/?.lua;'
package.mpath = '../../../libraries/metalua/?.mlua;../../../libraries/modelsbuilder/?.mlua;'
require 'metalua.loader'


local compiler = require 'metalua.compiler'
local mlc = compiler.new()
local apimodelbuilder      = require 'models.apimodelbuilder'
local modeltransformations = require 'modeltransformations'
local templateengine       = require 'templateengine'
for key, value in pairs(require 'template.utils') do
	templateengine.env[key] = value
end
if #arg < 1 then
	print 'No file to serialize.'
	return
end

for k = 1, #arg do

	-- Load source to serialize
	local filename = arg[k]
	
	-- Load file
	local luafile = io.open(filename, 'r')
	local luasource = luafile:read('*a')
	luafile:close()

	-- Generate AST
	local ast = mlc:src_to_ast(luasource)
	local status, astvalid, errormsg = pcall(compiler.check_ast, ast)
	if not astvalid then
		return nil, string.format('Unable to generate AST for %s.\n%s', filename, errormsg)
	end

	--Generate  API model
	local apimodel = apimodelbuilder.createmoduleapi(ast)
	
	-- Generate html form API Model
	local htmlcode, errormessage = templateengine.applytemplate(apimodel)
	if not htmlcode then
		print( string.format('Unable to generate html for %s.\n%s', luasourcepath, errormessage) )
	end

	-- Generate html form API Model
	local htmlcode, errormessage = templateengine.applytemplate(apimodel)
	if not htmlcode then
		print( string.format('Unable to generate html for %s.\n%s', luasourcepath, errormessage) )
	end
	
	local htmlfilename = filename:gsub('([%w%-_/\\]+)%.lua$', '%1.html')

	-- Save serialized model
	local htmlfile = io.open(htmlfilename, 'w')
	htmlfile:write( htmlcode )
	htmlfile:close()

	-- This a success
	print( string.format('%s serialized to %s.', filename, htmlfilename) )
end
