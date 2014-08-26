/*******************************************************************************
 * Copyright (c) 2013 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.ldt.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dltk.core.IScriptFolder;
import org.eclipse.dltk.core.ISourceModule;

/**
 * @since 1.1
 */
public interface IProjectSourceVisitor2 {

	public abstract void processFile(final ISourceModule sourceModule, final IPath absolutePath, final IPath relativePath, final String charset,
			final IProgressMonitor monitor) throws CoreException;

	public abstract void processDirectory(final IScriptFolder scriptFolder, final IPath absolutePath, final IPath relativePath,
			final IProgressMonitor monitor) throws CoreException;

}
