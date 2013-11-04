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

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.koneki.ldt.core.internal.LuaLanguageToolkit;
import org.eclipse.koneki.ldt.core.internal.PreferenceInitializer;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class GlobalLuaPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	protected String getHelpId() {
		return null;
	}

	protected void setDescription() {
		setDescription(Messages.GlobalLuaPreferencePage_description);
	}

	@Override
	protected void createFieldEditors() {
		BooleanFieldEditor useGlobalVarField = new BooleanFieldEditor(PreferenceInitializer.USE_GLOBAL_VAR_IN_LDT,
				Messages.GlobalLuaPreferencePage_use_global_vars, getFieldEditorParent());
		addField(useGlobalVarField);
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(new ScopedPreferenceStore(ConfigurationScope.INSTANCE, LuaLanguageToolkit.getDefault().getPreferenceQualifier()));
	}
}