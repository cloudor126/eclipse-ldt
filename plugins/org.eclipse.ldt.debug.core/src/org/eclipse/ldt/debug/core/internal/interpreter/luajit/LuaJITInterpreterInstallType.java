package org.eclipse.ldt.debug.core.internal.interpreter.luajit;

import org.eclipse.core.runtime.ILog;
import org.eclipse.dltk.launching.IInterpreterInstall;
import org.eclipse.ldt.core.internal.Activator;
import org.eclipse.ldt.debug.core.interpreter.AbstractLuaInterpreterInstallType;
import org.eclipse.ldt.debug.core.interpreter.ILuaInterpreterInstallType;

public class LuaJITInterpreterInstallType extends AbstractLuaInterpreterInstallType implements ILuaInterpreterInstallType {

	@Override
	public String getName() {
		return "Lua JIT 2.0"; //$NON-NLS-1$
	}

	@Override
	protected IInterpreterInstall doCreateInterpreterInstall(String id) {
		return new LuaJITInterpreterInstall(this, id);
	}

	@Override
	protected String getPluginId() {
		return Activator.PLUGIN_ID;
	}

	@Override
	protected ILog getLog() {
		return Activator.getDefault().getLog();
	}

	@Override
	public String getDefaultInterpreterName() {
		return "Lua JIT"; //$NON-NLS-1$
	}

	@Override
	public String getDefaultInterpreterArguments() {
		return "-e \"io.stdout:setvbuf('no'); if os.getenv('DEBUG_MODE') then require 'debugger' ; require 'debugger.plugins.ffi'end\""; //$NON-NLS-1$
	}

	@Override
	public String getDefaultEEName() {
		return "lua"; //$NON-NLS-1$
	}

	@Override
	public String getDefaultEEVersion() {
		return "5.1"; //$NON-NLS-1$
	}

	@Override
	public boolean isEmbeddedInterpreter() {
		return false;
	}

	@Override
	public boolean handleExecuteOption() {
		return true;
	}

	@Override
	public boolean handleFilesAsArgument() {
		return true;
	}

	@Override
	public boolean handleInterpreterArguments() {
		return true;
	}
}
