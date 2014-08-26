/*******************************************************************************
 * Copyright (c) 2012, 2013	 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.ldt.ui.internal.preferences;

import org.eclipse.osgi.util.NLS;

// CHECKSTYLE NLS: OFF
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ldt.ui.internal.preferences.messages"; //$NON-NLS-1$
	public static String GlobalLuaPreferencePage_description;
	public static String GlobalLuaPreferencePage_use_global_vars;
	public static String LuaExecutionEnvironmentPreferencePage_addEESupportTitle;
	public static String LuaExecutionEnvironmentPreferencePage_addEESupportMessage;
	public static String LuaExecutionEnvironmentPreferencePage_addbutton;
	public static String LuaExecutionEnvironmentPreferencePage_availableEELink;
	public static String LuaExecutionEnvironmentPreferencePage_removeButton;
	public static String LuaExecutionEnvironmentPreferencePage_warning_nodefault;
	public static String LuaExecutionEnvironmentPreferencePageTitle;
	public static String LuaTodoTaskPreferencePage_description;
	public static String LuaSmartConfigurationBlockBraces;
	public static String LuaSmartConfigurationBlockBrackets;
	public static String LuaSmartConfigurationBlockStrings;
	public static String LuaSmartConfigurationBlockTitle;
	public static String LuaSmartTypingPreferencePageDescription;
	public static String LuaSmartTypingPreferencePageDescriptionLabel;
	public static String LuaEditorPreferencePageDescription;
	public static String LuaEditorAssistancePreferencePageDescription;
	public static String LuaEditorColoringConfigurationBlock_multiLineComment;
	public static String LuaEditorColoringConfigurationBlock_localVariable;
	public static String LuaEditorColoringConfigurationBlock_globalVariable;
	public static String LuaEditorColoringConfigurationBlock_luaDocumentor;
	public static String LuaEditorColoringConfigurationBlock_luaDocumentorTags;
	public static String LuaFormatterIndentationTabPageIndentTableValues;
	public static String LuaFormatterIndentationTabPageTableIndentationPolicy;
	public static String LuaFormatterModifyDialogIndentation;
	public static String LuaFoldingPreferencePage_initiallyFoldLevelOneBlocks;
	public static String LuaFoldingPreferencePage_initiallyFoldDoc;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
// CHECKSTYLE NLS: ON
