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
local formatter = require 'formatter'
local compiler = require 'metalua.compiler'
local mlc = compiler.new()
local Q = require 'metalua.treequery'
local M = {}
local cache
local function buildcache(str)

	-- Generate AST
	local ast = mlc:src_to_ast(str)
	local status, astvalid, errormsg = pcall(compiler.check_ast, ast)
	if not astvalid then
		return nil, string.format("Unable to build AST.%s", errormsg)
	end

	-- Cache string nodes
	cache = {}
  Q(ast):filter('String') :foreach(function(node)
    if node.tag == 'String' then
      table.insert(cache, node)
    end
  end)
	return true
end
local function mustbeignored(offset)
	if not cache then
		return nil, 'Cache is not available.'
	end
	for _, node in ipairs(cache) do
		if node.lineinfo.first.offset <= offset and offset <= node.lineinfo.last.offset then
			return true
		end
	end
	return false
end
function M.prettify(serializedtablestring)

	-- Check input
	if type(serializedtablestring) ~= 'string' then
		return nil, 'String expected.'
	end

	local chartoindent = {
		[','] = ',\n',
		[';'] = ';\n',
		['{'] = '{\n',
		['}'] = '\n}\n'
	}

	--
	-- Replace provided characters
	--
	for char, replacement in pairs(chartoindent) do

		-- Refresh cache as offset of analyzed string change at each loop
		local status, error = buildcache(serializedtablestring)
		if not status then
			return nil, error
		end

		--
		-- Seek for character to replace
		--
		local buffer = {}
		local searchstart= 1
		local charposition = serializedtablestring:find(char, searchstart, true)
		while charposition ~= nil do

			-- Bufferize code before character
			table.insert(buffer, serializedtablestring:sub(searchstart, charposition - 1))

			-- Ensure that we can replace this character, avoiding thoses in strings
			local ignoreposition, errormessage = mustbeignored(charposition)
			if errormessage then
				return nil, string.format('Unable to know if offset %d has to be formated.%s.', charposition, errormessage)
			elseif ignoreposition then
				-- Ignore character
				table.insert(buffer, char)
			else
				-- Actual character replacement
				table.insert(buffer, replacement)
			end

			-- Move to next replacement
			searchstart = charposition + 1
			charposition = serializedtablestring:find(char, searchstart, true)
		end

		-- Append remaining string
		table.insert(buffer, serializedtablestring:sub(searchstart))

		-- Replace original string
		serializedtablestring = table.concat(buffer)
	end

	-- Format resulting code
	return formatter.indentcode(serializedtablestring, '\n', true, '\t')
end
return M
