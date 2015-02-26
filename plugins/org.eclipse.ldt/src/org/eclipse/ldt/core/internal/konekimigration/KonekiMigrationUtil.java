/*******************************************************************************
 * Copyright (c) 2015 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.ldt.core.internal.konekimigration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IBuildpathEntry;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.ldt.core.buildpath.LuaExecutionEnvironment;
import org.eclipse.ldt.core.internal.Activator;
import org.eclipse.ldt.core.internal.buildpath.LuaExecutionEnvironmentManager;

public final class KonekiMigrationUtil {

	private static final Object KONEKI_CONTAINER_PATH_START = "org.eclipse.koneki.ldt.ExecutionEnvironmentContainer"; //$NON-NLS-1$

	private KonekiMigrationUtil() {
	}

	public static boolean isKonekiProject(IProject project) {
		try {
			return project.hasNature("org.eclipse.koneki.ldt.nature"); //$NON-NLS-1$
		} catch (CoreException e) {
			Activator.log(e.getStatus());
			return false;
		}
	}

	public static boolean isValidKonekiExecutionEnvironmentBuildPath(final IPath eePath) {
		if (eePath == null)
			return false;

		final String[] segments = eePath.segments();
		return (segments.length == 3) && KONEKI_CONTAINER_PATH_START.equals(segments[0]);
	}

	public static LuaExecutionEnvironment getKonekiExecutionEnvironment(IProject project) {
		try {
			IScriptProject scriptProject = DLTKCore.create(project);

			IBuildpathEntry[] rawBuildpath = scriptProject.getRawBuildpath();
			for (int i = 0; i < rawBuildpath.length; i++) {
				// convert koneki lua execution environment path to ldt environment path
				IPath konekiPath = rawBuildpath[i].getPath();
				if (isValidKonekiExecutionEnvironmentBuildPath(konekiPath)) {
					Pattern p = Pattern.compile("org\\.eclipse\\.koneki\\.ldt\\.ExecutionEnvironmentContainer/(.*)/(.*)"); //$NON-NLS-1$
					Matcher m = p.matcher(konekiPath.toString());
					if (m.find()) {
						return LuaExecutionEnvironmentManager.getAvailableExecutionEnvironment(m.group(1), m.group(2));
					}
				}
			}
		} catch (ModelException e) {
			Activator.logWarning("unable to get koneki buildpath for project", e); //$NON-NLS-1$
		} catch (CoreException e) {
			Activator.logWarning("unable to ExecutionEnvironment for project", e); //$NON-NLS-1$
		}
		return null;
	}
}
