/*******************************************************************************
 * Copyright (c) 2012, 2013 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.ldt.debug.core.internal.interpreter.jnlua;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dltk.core.environment.IFileHandle;
import org.eclipse.dltk.launching.EnvironmentVariable;
import org.eclipse.dltk.launching.LibraryLocation;
import org.eclipse.ldt.debug.core.internal.Activator;
import org.eclipse.ldt.debug.core.interpreter.AbstractLuaInterpreterInstallType;
import org.eclipse.ldt.debug.core.interpreter.ILuaInterpreterInstallType;

public abstract class AbstractJNLuaInterpreterInstallType extends AbstractLuaInterpreterInstallType implements ILuaInterpreterInstallType {

	@Override
	public String getName() {
		return "JNLua 5.1"; //$NON-NLS-1$
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
	public IStatus validateInstallLocation(IFileHandle installLocation) {
		// No validation on install location as interpreter is embedded.
		return Status.OK_STATUS;
	}

	@Override
	public IStatus validateInstallLocation(IFileHandle installLocation, EnvironmentVariable[] variables, LibraryLocation[] libraryLocations,
			IProgressMonitor monitor) {
		// No validation on install location as interpreter is embedded.
		return Status.OK_STATUS;
	}

	@Override
	public IStatus validatePossiblyName(IFileHandle installLocation) {
		// No validation
		return Status.OK_STATUS;
	}

	@Override
	public String getDefaultInterpreterName() {
		return null;
	}

	@Override
	public String getDefaultInterpreterArguments() {
		return null;
	}

	@Override
	public boolean isEmbeddedInterpreter() {
		return true;
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
