/*******************************************************************************
 * Copyright (c) 2012 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.koneki.ldt.debug.ui.internal.launchconfiguration.local.tab;

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.dltk.compiler.util.Util;
import org.eclipse.dltk.internal.debug.ui.launcher.WorkingDirectoryBlock;
import org.eclipse.dltk.launching.ScriptLaunchConfigurationConstants;
import org.eclipse.koneki.ldt.core.internal.Activator;
import org.eclipse.koneki.ldt.debug.core.internal.LuaDebugConstants;

public class LuaArgumentsTabWorkingDirectoryBlock extends WorkingDirectoryBlock {

	@Override
	protected void setDefaultWorkingDir() {

		// Default parent working directory should be project root
		final ILaunchConfiguration config = getCurrentLaunchConfiguration();
		if (config != null) {
			try {
				String projectName = config.getAttribute(ScriptLaunchConfigurationConstants.ATTR_PROJECT_NAME, Util.EMPTY_STRING);
				String path = MessageFormat.format(LuaDebugConstants.LAUNCH_CONFIGURATION_WORKING_DIRECTORY, projectName);
				setDefaultWorkingDirectoryText(path);
				return;
			} catch (final CoreException ce) {
				Activator.logWarning("Unable to retrive current project name from environment to compute parent working directory.", ce); //$NON-NLS-1$
			}
		}

		// When project name is not available, we will use the workspace
		setDefaultWorkingDirectoryText("${workspace_loc}"); //$NON-NLS-1$
	}
}
