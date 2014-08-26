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
package org.eclipse.ldt.ui.internal.preferences;

import org.eclipse.dltk.ui.formatter.AbstractFormatterPreferencePage;
import org.eclipse.dltk.ui.preferences.AbstractConfigurationBlockPropertyAndPreferencePage;
import org.eclipse.dltk.ui.preferences.PreferenceKey;
import org.eclipse.dltk.ui.text.IColorManager;
import org.eclipse.dltk.ui.text.ScriptSourceViewerConfiguration;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ldt.core.LuaNature;
import org.eclipse.ldt.ui.internal.Activator;
import org.eclipse.ldt.ui.internal.editor.formatter.LuaFormatterPreferenceConstants;
import org.eclipse.ldt.ui.internal.editor.text.ILuaPartitions;
import org.eclipse.ldt.ui.internal.editor.text.LuaSourceViewerConfiguration;
import org.eclipse.ui.texteditor.ITextEditor;

public class LuaFormatterPreferencePage extends AbstractFormatterPreferencePage {

	static final String ID = "org.eclipse.ldt.formatter.preferences"; //$NON-NLS-1$ 

	/**
	 * @see AbstractFormatterPreferencePage#getNatureId()
	 */
	@Override
	protected String getNatureId() {
		return LuaNature.ID;
	}

	/**
	 * @see org.eclipse.dltk.ui.formatter.AbstractFormatterPreferencePage#getFormatterPreferenceKey()
	 */
	@Override
	protected PreferenceKey getFormatterPreferenceKey() {
		return new PreferenceKey(Activator.PLUGIN_ID, LuaFormatterPreferenceConstants.FORMATTER_ID);
	}

	/**
	 * @see AbstractFormatterPreferencePage#getDialogSettings()
	 */
	@Override
	protected IDialogSettings getDialogSettings() {
		return Activator.getDefault().getDialogSettings();
	}

	/**
	 * @see AbstractConfigurationBlockPropertyAndPreferencePage#setPreferenceStore()
	 */
	@Override
	protected void setPreferenceStore() {
		super.setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	/**
	 * @see org.eclipse.dltk.ui.formatter.AbstractFormatterPreferencePage#createSimpleSourceViewerConfiguration(org.eclipse.dltk.ui.text.IColorManager,
	 *      org.eclipse.jface.preference.IPreferenceStore, org.eclipse.ui.texteditor.ITextEditor, boolean)
	 */
	@Override
	protected ScriptSourceViewerConfiguration createSimpleSourceViewerConfiguration(IColorManager colorManager, IPreferenceStore preferenceStore,
			ITextEditor editor, boolean configureFormatter) {
		return new LuaSourceViewerConfiguration(colorManager, preferenceStore, editor, ILuaPartitions.LUA_PARTITIONING);
	}
}
