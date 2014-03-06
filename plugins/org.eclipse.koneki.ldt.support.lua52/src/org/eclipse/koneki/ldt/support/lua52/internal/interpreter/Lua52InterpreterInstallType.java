package org.eclipse.koneki.ldt.support.lua52.internal.interpreter;

import org.eclipse.core.runtime.ILog;
import org.eclipse.dltk.launching.IInterpreterInstall;
import org.eclipse.koneki.ldt.debug.core.interpreter.AbstractLuaInterpreterInstallType;
import org.eclipse.koneki.ldt.debug.core.interpreter.ILuaInterpreterInstallType;
import org.eclipse.koneki.ldt.support.lua52.internal.Activator;

public class Lua52InterpreterInstallType extends AbstractLuaInterpreterInstallType implements ILuaInterpreterInstallType {

	@Override
	public String getName() {
		return "Lua 5.2"; //$NON-NLS-1$
	}

	@Override
	protected IInterpreterInstall doCreateInterpreterInstall(String id) {
		return new Lua52InterpreterInstall(this, id);
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
		return "Lua 5.2"; //$NON-NLS-1$
	}

	@Override
	public String getDefaultInterpreterArguments() {
		return "-e \"io.stdout:setvbuf('no')\""; //$NON-NLS-1$
	}

	@Override
	public String getDefaultEEName() {
		return "lua"; //$NON-NLS-1$
	}

	@Override
	public String getDefaultEEVersion() {
		return "5.2"; //$NON-NLS-1$
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
