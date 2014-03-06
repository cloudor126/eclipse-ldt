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
package org.eclipse.koneki.ldt.debug.core.interpreter;

import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dltk.core.environment.IDeployment;
import org.eclipse.dltk.core.environment.IFileHandle;
import org.eclipse.dltk.internal.launching.AbstractInterpreterInstallType;
import org.eclipse.dltk.launching.EnvironmentVariable;
import org.eclipse.dltk.launching.LibraryLocation;
import org.eclipse.koneki.ldt.core.LuaNature;

/**
 * @since 1.2
 */
public abstract class AbstractLuaInterpreterInstallType extends AbstractInterpreterInstallType {

	@Override
	public String getNatureId() {
		return LuaNature.ID;
	}

	@Override
	protected String[] getPossibleInterpreterNames() {
		// TODO verify if it's useful
		return null;
	}

	@Override
	public synchronized LibraryLocation[] getDefaultLibraryLocations(final IFileHandle installLocation, EnvironmentVariable[] variables,
			IProgressMonitor monitor) {
		return new LibraryLocation[0];
	}

	@Override
	protected IPath createPathFile(IDeployment deployment) throws IOException {
		// TODO verify if it's useful
		return null;
	}
}
