/*******************************************************************************
 * Copyright (c) 2014 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.ldt.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ldt.ui.wizards.ConvertToLuaProjectWizard;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Launch wizard to convert a project in Lua Project.
 */
public class ConvertToLuaProjectHandler extends AbstractHandler {

	/**
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Get selected project
		IProject project = null;
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection) {
			Object firstElement = ((IStructuredSelection) selection).getFirstElement();
			if (firstElement instanceof IAdaptable) {
				project = (IProject) ((IAdaptable) firstElement).getAdapter(IProject.class);
			}
		}

		// Project selection should not be null
		if (project == null) {
			return null;
		}

		// Launch wizard
		ConvertToLuaProjectWizard wizard = new ConvertToLuaProjectWizard(project);
		WizardDialog dialog = new WizardDialog(HandlerUtil.getActiveShell(event), wizard);
		dialog.open();
		return null;
	}
}
