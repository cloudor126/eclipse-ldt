/*******************************************************************************
 * Copyright (c) 2011, 2012 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.ldt.debug.core.internal.local;

import java.text.MessageFormat;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.Launch;
import org.eclipse.dltk.compiler.util.Util;
import org.eclipse.dltk.debug.core.DLTKDebugLaunchConstants;
import org.eclipse.dltk.internal.launching.DLTKLaunchingPlugin;
import org.eclipse.dltk.launching.AbstractScriptLaunchConfigurationDelegate;
import org.eclipse.dltk.launching.IInterpreterInstall;
import org.eclipse.dltk.launching.ScriptLaunchConfigurationConstants;
import org.eclipse.ldt.core.LuaNature;
import org.eclipse.ldt.debug.core.internal.LuaDebugConstants;

public class LuaLaunchConfigurationDelegate extends AbstractScriptLaunchConfigurationDelegate {

	@Override
	public String getLanguageId() {
		return LuaNature.ID;
	}

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		super.launch(configuration, mode, launch, monitor);
	}

	@Override
	protected void validateLaunchConfiguration(ILaunchConfiguration configuration, String mode, IProject project) throws CoreException {
		// TODO reactivate validation ?
	}

	@Override
	public IInterpreterInstall verifyInterpreterInstall(ILaunchConfiguration configuration) throws CoreException {
		// TODO reactivate verification
		return getInterpreterInstall(configuration);
	}

	// TODO Workaround for BUG ECLIPSE 419273
	@Override
	protected void setDebugConsoleAttributes(Launch launch, ILaunchConfiguration configuration) throws CoreException {
		if (!configuration.getAttribute(ScriptLaunchConfigurationConstants.ATTR_DEBUG_CONSOLE, true)) {
			launch.setAttribute(DLTKDebugLaunchConstants.ATTR_DEBUG_CONSOLE, DLTKDebugLaunchConstants.FALSE);
		} else {
			launch.setAttribute(DLTKDebugLaunchConstants.ATTR_DEBUG_CONSOLE, DLTKDebugLaunchConstants.TRUE);
		}
	}

	/**
	 * Returns the default working directory for the given launch configuration, or <code>null</code> if none. Subclasses may override as necessary.
	 * 
	 * @param configuration
	 * @return default working directory or <code>null</code> if none
	 * @throws CoreException
	 *             if an exception occurs computing the default working directory
	 * 
	 */
	protected IPath getDefaultWorkingDirectory(ILaunchConfiguration configuration) throws CoreException {
		if (configuration != null) {
			try {
				String projectName = configuration.getAttribute(ScriptLaunchConfigurationConstants.ATTR_PROJECT_NAME, Util.EMPTY_STRING);
				String path = MessageFormat.format(LuaDebugConstants.LAUNCH_CONFIGURATION_WORKING_DIRECTORY, projectName);
				IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
				path = manager.performStringSubstitution(path, false);
				return new Path(path);
			} catch (CoreException e) {
				DLTKLaunchingPlugin.log(e);
			}
		}
		return null;
	}
}
