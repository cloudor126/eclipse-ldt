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
package org.eclipse.ldt.ui.wizards.pages;

import org.eclipse.osgi.util.NLS;

//CHECKSTYLE NLS: OFF
public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.ldt.ui.wizards.pages.messages"; //$NON-NLS-1$

	/** @since 1.3 */
	public static String ConvertToLuaProjectMainPage_title;
	public static String ConvertToLuaProjectMainPage_defaultMessage;

	public static String ConvertToLuaProjectMainPage_linkToMigrationPage;

	public static String ConvertToLuaProjectMainPage_migrationMessage;

	/** @since 1.2 */
	public static String DocLuaFilePage_description;

	/** @since 1.2 */
	public static String DocLuaFilePage_title;

	/** @since 1.1 */
	public static String LuaExecutionEnvironmentGroupTemplateLabel;

	public static String LuaExecutionEnvironmentGroupMainLabel;
	public static String LuaExecutionEnvironmentGroupManageExecutionEnvironment;
	public static String LuaExecutionEnvironmentGroupNoEEForProjectCreation;
	public static String LuaExecutionEnvironmentGroupSelectEE;
	public static String LuaExecutionEnvironmentGroupTitle;
	public static String LuaFilePageDescription;
	public static String LuaFilePageTitle;
	public static String LuaProjecSettingsPageLabel;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
// CHECKSTYLE NLS: ON
