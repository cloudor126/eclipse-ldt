-------------------------------------------------------------------------------
-- Copyright (c) 2011-2012 Sierra Wireless and others.
-- All rights reserved. This program and the accompanying materials
-- are made available under the terms of the Eclipse Public License v1.0
-- which accompanies this distribution, and is available at
-- http://www.eclipse.org/legal/epl-v10.html
--
-- Contributors:
--     Sierra Wireless - initial API and implementation
-------------------------------------------------------------------------------
-- Utility functions.
-------------------------------------------------------------------------------

local M = { }

-- log system
local LEVELS = { ERROR = 0, WARNING = 1, INFO = 2, DETAIL = 3, DEBUG = 4 }
local LOG_LEVEL = LEVELS.WARNING

-- Debugger features handling. Any feature can be get like any regular table, setting features result in
-- error for unknown or read-only features.
M.features = setmetatable({ }, {
  -- functions that format/validate data. If function is not provided, the feature cannot be modified.
  validators = {
    multiple_sessions = tonumber,
    encoding = tostring,
    max_children = tonumber,
    max_data = tonumber,
    max_depth = tonumber,
    show_hidden = tonumber,
    uri = tostring,
    log_level = function(level_name)
      -- set numerical index in internal var
      LOG_LEVEL = assert(LEVELS[level_name], "No such level")
      return level_name -- the displayed level is still the name
    end,
  },
  __index = {
    multiple_sessions = 0,
    encoding ="UTF-8",
    max_children = 32,
    max_data = 0xFFFF,
    max_depth = 1,
    show_hidden = 1,
    uri = "file",
    log_level = "WARNING",
    -- read only features
    language_supports_threads = 0,
    language_name = "Lua",
    language_version = _VERSION,
    protocol_version = 1,
    supports_async = 1,
    data_encoding = "base64",
    breakpoint_languages = "Lua",
    breakpoint_types = "line conditional",
  },
  __newindex = function(self, k, v)
    local mt = getmetatable(self)
    local values, validator = mt.__index, mt.validators[k]
    if values[k] == nil then error("No such feature " .. tostring(k)) end
    if not validator then error("The feature " .. tostring(k) .. " is read-only") end
    v = assert(validator(v))
    values[k] = v
  end,
})

-- Wraps debug function and an attached thread
-- also handle stack & coroutine management differencies between Lua versions
local getinfo, getlocal, setlocal = debug.getinfo, debug.getlocal, debug.setlocal

-- Foreign thread is used to debug paused thread
local ForeignThreadMT = {
  getinfo  = function(self, level, what)     return getinfo(self[1], level, what) end,
  getlocal = function(self, level, idx)      return getlocal(self[1], level, idx) end,
  setlocal = function(self, level, idx, val) return setlocal(self[1], level, idx, val) end,
}
ForeignThreadMT.__index = ForeignThreadMT
function M.ForeignThread(coro) return setmetatable({ coro }, ForeignThreadMT) end

-- Current thread is used to debug the thread that caused the hook
-- intended to be used *ONLY* in debug loop (executed in a new thread)
local CurrentThreadMT = {
  getinfo  = function(self, level, what)     return getinfo(self[1], level + 2, what) end,
  getlocal = function(self, level, idx)      return getlocal(self[1], level + 2, idx) end,
  setlocal = function(self, level, idx, val) return setlocal(self[1], level + 2, idx, val) end,
}
CurrentThreadMT.__index = CurrentThreadMT
function M.CurrentThread(coro) return setmetatable({ coro }, CurrentThreadMT) end


-- Some version dependant functions
if DBGP_CLIENT_LUA_VERSION == "Lua 5.1" then
  local loadstring, getfenv, setfenv, debug_getinfo, MainThread =
    loadstring, getfenv, setfenv, debug.getinfo, nil

  -- in 5.1 "t" flag does not exist and trigger an error so remove it from what
  CurrentThreadMT.getinfo = function(self, level, what) return getinfo(self[1], level + 2, what:gsub("t", "", 1)) end
  ForeignThreadMT.getinfo = function(self, level, what) return getinfo(self[1], level, what:gsub("t", "", 1)) end

  -- when we're forced to start debug loop on top of program stack (when on main coroutine)
  -- this requires some hackery to get right stack level

  -- Fallback method to inspect running thread (only for main thread in 5.1 or for conditional breakpoints)
  --- Gets a script stack level with additional debugger logic added
  -- @param l (number) stack level to get for debugged script (0 based)
  -- @return real Lua stack level suitable to be passed through deubg functions
  local function get_script_level(l)
    local hook = debug.gethook()
    for i=2, math.huge do
      if assert(debug.getinfo(i, "f")).func == hook then
        return i + l -- the script to level is just below, but because of the extra call to this function, the level is ok for callee
      end
    end
  end

  if rawget(_G, "jit") then
    MainThread = {
      [1] = "main", -- as the raw thread object is used as table keys, provide a replacement.
      -- LuaJIT completely eliminates tail calls from stack, so get_script_level retunrs wrong result in this case
      getinfo  = function(self, level, what)     return getinfo(get_script_level(level) - 1, what:gsub("t", "", 1)) end,
      getlocal = function(self, level, idx)      return getlocal(get_script_level(level) - 1, idx) end,
      setlocal = function(self, level, idx, val) return setlocal(get_script_level(level) - 1, idx, val) end,
    }
  else
    MainThread = {
      [1] = "main",
      getinfo  = function(self, level, what)     return getinfo(get_script_level(level) , what:gsub("t", "", 1)) end,
      getlocal = function(self, level, idx)      return getlocal(get_script_level(level), idx) end,
      setlocal = function(self, level, idx, val) return setlocal(get_script_level(level), idx, val) end,
    }
  end



  -- If the VM is vanilla Lua 5.1 or LuaJIT 2 without 5.2 compatibility, there is no way to get a reference to
  -- the main coroutine, so fall back to direct mode: the debugger loop is started on the top of main thread
  -- and the actual level is recomputed each time
  local oldCurrentThread = M.CurrentThread
  M.CurrentThread = function(coro) return coro and oldCurrentThread(coro) or MainThread end

  -- load a piece of code alog with its environment
  function M.loadin(code, env)
    local f,err = loadstring(code)
    if not f then
      return nil, err
    else
      return f and setfenv(f, env)
    end
  end

  -- table that maps [gs]et environment to index
  M.eval_env = setmetatable({ }, {
    __index = function(self, func) return getfenv(func) end,
    __newindex = function(self, func, env) return setfenv(func, env) end,
  })
elseif DBGP_CLIENT_LUA_VERSION == "Lua 5.2" then
  local load, debug_getinfo = load, debug.getinfo
  function M.getinfo(coro, level, what)
    if coro then return debug_getinfo(coro, level, what)
    else return debug_getinfo(level + 1, what) end
  end

  function M.loadin(code, env) return load(code, nil, nil, env) end

  -- no eval_env for 5.2 as functions does not have environments anymore
end

-- ----------------------------------------------------------------------------
-- Bare minimal log system.
-- ----------------------------------------------------------------------------
function M.log(level, msg, ...)
  if (LEVELS[level] or -1) > LOG_LEVEL then return end
  if select("#", ...) > 0 then msg = msg:format(...) end
  io.base.stderr:write(string.format("DEBUGGER\t%s\t%s\n", level, msg))
end

return M
