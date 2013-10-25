/*******************************************************************************
 * Copyright (c) 2009, 2012 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 ******************************************************************************/
package org.eclipse.koneki.ldt.ui.internal.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.dltk.ui.util.SWTFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.koneki.ldt.core.internal.LuaLanguageToolkit;
import org.eclipse.koneki.ldt.core.internal.PreferenceInitializer;
import org.eclipse.koneki.ldt.ui.internal.Activator;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class GlobalLuaPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private BooleanFieldEditor useGlobalVars;

	protected String getHelpId() {
		return null;
	}

	protected void setDescription() {
		setDescription(Messages.GlobalLuaPreferencePage_description);
	}

	protected void setPreferenceStore() {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_HORIZONTAL);

		useGlobalVars = new BooleanFieldEditor(PreferenceInitializer.USE_GLOBAL_VAR_IN_LDT, Messages.GlobalLuaPreferencePage_use_global_vars,
				composite);
		useGlobalVars.setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, LuaLanguageToolkit.getDefault().getPreferenceQualifier()));
		useGlobalVars.load();

		return composite;
	}

	@Override
	public void init(IWorkbench workbench) {

	}

	@Override
	public boolean performOk() {
		useGlobalVars.store();
		return super.performOk();
	}

}