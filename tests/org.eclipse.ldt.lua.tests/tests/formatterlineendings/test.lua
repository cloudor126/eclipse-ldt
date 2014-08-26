--------------------------------------------------------------------------------
-- Copyright (c) 2014 Sierra Wireless and others.
-- All rights reserved. This program and the accompanying materials
-- are made available under the terms of the Eclipse Public License v1.0
-- which accompanies this distribution, and is available at
-- http://www.eclipse.org/legal/epl-v10.html
--
-- Contributors:
--     Sierra Wireless - initial API and implementation
--------------------------------------------------------------------------------
local formatter =  require 'formatter'
local string =     require 'string'
local javaassert = java.require 'org.junit.Assert'

local M = {}
function M.test(luainputpath, luareferencepath)

  -- Load provided source
  local luafile, errormessage = io.open(luainputpath, 'r')
  assert(luafile, string.format('Unable to read from %s.\n%s',
    luainputpath, errormessage or '')
  )
  local luasource = luafile:read('*a')
  luafile:close()

  -- Extract filename
  local lastseparator = luainputpath:reverse():find('[/\\]') - 1
  local filename = luainputpath:sub(-lastseparator)

  -- Choose delimiter from file name
  local delimiters = {
    ["mac.lua"] = '\r',
    ["unix.lua"] = '\n',
    ["windows.lua"] = '\r\n'
  }
  local delimiter = delimiters[ filename ]
  assert(delimiter, string.format(
    'Could not determine line ending to check for %s.', filename))

  -- Format code
  local formattedCode = formatter.indentcode(luasource, delimiter, true, '  ')
  assert(formattedCode,
    string.format('Unable to format %s.', luainputpath)
  )

  -- Load provided reference
  local referenceFile, errormessage = io.open(luareferencepath)
  assert(referenceFile, string.format('Unable to read reference from %s.\n%s',
    luareferencepath, errormessage or '')
  )
  local referenceCode = referenceFile:read('*a')

  -- Check equality by catching assertException from assertEquals
  local status, errormessage = pcall( function()
    javaassert:assertEquals('Unexpected line endings', referenceCode,
      formattedCode)
  end)
  return status or nil, tostring(errormessage)
end
return M
