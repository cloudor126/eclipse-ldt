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
package org.eclipse.ldt.ui.wizards.pages;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ldt.core.internal.buildpath.LuaExecutionEnvironment;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * @since 1.3
 */
public class ConvertToLuaProjectMainPage extends WizardPage {

	private IProject project;
	private LuaExecutionEnvironmentGroup luaExecutionEnvironmentGroup;

	public ConvertToLuaProjectMainPage(String pageName, IProject project) {
		super(pageName);
		this.project = project;
		setTitle(NLS.bind(Messages.ConvertToLuaProjectMainPage_title, project.getName()));
		setMessage(Messages.ConvertToLuaProjectMainPage_defaultMessage);
	}

	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		// Create container
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());
		GridLayoutFactory.swtDefaults().applyTo(composite);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(composite);

		// Create Lua execution environment group
		luaExecutionEnvironmentGroup = new LuaExecutionEnvironmentGroup(composite, false);

		setControl(composite);
		Dialog.applyDialogFont(composite);
	}

	public LuaExecutionEnvironment getLuaExecutionEnvironement() {
		return luaExecutionEnvironmentGroup.getSelectedLuaExecutionEnvironment();
	}
}
