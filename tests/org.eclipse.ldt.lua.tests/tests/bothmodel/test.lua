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
require 'metalua.loader'
local pp=require'metalua.pprint'
local compiler =  require 'metalua.compiler'
local mlc = compiler.new()
local apimodelbuilder = require 'models.apimodelbuilder'
local internalmodelbuilder = require 'models.internalmodelbuilder'
local tablecompare         = require 'tablecompare'

local M = {}
function M.test(luasourcepath, serializedreferencepath)

	--
	-- Load provided source
	--
	local luafile, errormessage = io.open(luasourcepath, 'r')
	assert(
		luafile,
		string.format('Unable to read from %s.\n%s', luasourcepath, errormessage or '')
	)
	local luasource = luafile:read('*a')
	luafile:close()

	-- Check if an error occurred
	local sourceisvalid, msg = loadstring(luasource, 'AST source')
	assert(
    sourceisvalid,
		string.format('Generated AST contains an error.\n%s', msg or '')
	)

	-- Generate AST
	local ast, errormessage = mlc:src_to_ast( luasource )
	assert(
		ast,
		string.format('Unable to generate AST for %s.\n%s', luasourcepath, errormessage or '')
	)

	--
	-- Generate API model
	--
	local apimodel, comment2apiobj = apimodelbuilder.createmoduleapi(ast)

	--
	-- Generate internal model
	--
	local internalmodel = internalmodelbuilder.createinternalcontent(ast,apimodel,comment2apiobj, "modulename")
	
	-- strip tables
	internalmodel = tablecompare.stripfunctions(internalmodel)
	apimodel = tablecompare.stripfunctions(apimodel)

	--
	-- create table with the two models
	-- 
	local luasourceroot = {}
	luasourceroot.fileapi=apimodel
	luasourceroot.internalcontent=internalmodel
	
	--
	-- Load provided reference
	--
	local luareferenceloadingfunction = loadfile(serializedreferencepath)
	assert(
		luareferenceloadingfunction,
		string.format('Unable to load reference from %s.', serializedreferencepath)
	)
	local referencebothmodel = luareferenceloadingfunction()

	
	-- AST : Check that they are equivalent
	local equivalent = tablecompare.compare(luasourceroot, referencebothmodel)
	if #equivalent > 0 then

		-- Compute which keys differs
		local differentkeys = tablecompare.diff(luasourceroot, referencebothmodel)
		local differentkeysstring = pp.tostring(differentkeys, {line_max=1})

		-- Formalise first table output
		local _ = '_'
		local line = _:rep(80)
		local firstout  = string.format('%s\nGenerated table\n%s\n%s', line, line, pp.tostring(luasourceroot, {line_max=1}))
		local secondout = string.format('%s\nReference table\n%s\n%s', line, line, pp.tostring(referencebothmodel, {line_max=1}))
		return nil, string.format('Keys which differ are:\n%s\n%s\n%s', differentkeysstring, firstout, secondout)

	end
	return true
end
return M
