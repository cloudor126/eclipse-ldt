local javamodelfactory = require 'javamodelfactory'

local M = {}

function M.valid (source, root)
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
    local nbline = select(2, source:gsub('\n', '\n'))+1
    if source and nbline > 1 then
      -- define function that replace all character of a given line in space characters
      local function cleanline (source, linetoclean,nbline)
        local cleanedsource
        local iscleaned = false
        if linetoclean == nbline then
          -- manage last line
          cleanedsource = source:gsub('([^\n]-)$',function (lastline)
            iscleaned = true
            return string.rep(' ',string.len(lastline))
          end)
        elseif linetoclean == 1 then
          -- manage first line
          cleanedsource = source:gsub('^(.-)\n',function (firstline)
            iscleaned = true
            return string.rep(' ',string.len(firstline)).."\n"
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
        cleanedsource, iscleaned = cleanline(source,1,nbline)
        f, _ = loadstring(cleanedsource,'source_to_check')
      else
        -- OTHER CASES: first, cleaning ...
        -- if something is not closed we try to remove the line where it is opened.
        local linestart = string.match(err,"%(to close .* at line (%d+)%)")
        if linestart then
          cleanedsource, iscleaned = cleanline(source,tonumber(linestart),nbline)
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
            cleanedsource, iscleaned = cleanline(source,realcodelineindex,nbline)
          end
        end

        -- after cleaning, recheck hoping there are no errors.
        if iscleaned then
          f, _ = loadstring(cleanedsource,'source_to_check')
          -- if it fail, we try to remove the line in error
          if not f then
            cleanedsource = cleanline(source,lineindex,nbline)
            f, _ = loadstring(cleanedsource,'source_to_check')
          end
        end
      end

      -- take cleaned source as source
      if f then
        source = cleanedsource
      end
    end
    -- ------------------------------------------------
    -- END OF EXPERIMENTAL CODE
    -- -------------------------------------------------
  end
  if f then return source end
end

return M