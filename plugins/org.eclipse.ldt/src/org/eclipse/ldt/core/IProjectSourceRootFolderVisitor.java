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
package org.eclipse.ldt.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @deprecated Use org.eclipse.ldt.core.IProjectSourceRootFolderVisitor2 instead.
 */
public interface IProjectSourceRootFolderVisitor {
	public abstract void processSourceRootFolder(final IPath absolutePath, final IProgressMonitor monitor) throws CoreException;
}
