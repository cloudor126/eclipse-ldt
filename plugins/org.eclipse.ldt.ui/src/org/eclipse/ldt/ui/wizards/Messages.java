/*******************************************************************************
 * Copyright (c) 2011, 2012 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.ldt.ui.wizards;

import org.eclipse.osgi.util.NLS;

//CHECKSTYLE NLS: OFF
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ldt.ui.wizards.messages"; //$NON-NLS-1$

	/** @since 1.3 */
	public static String ConvertToLuaProjectWizard_convertFailedMessage;
	public static String ConvertToLuaProjectWizard_wizardTitle;
	public static String ConvertToLuaProjectWizard_ConvertFailedDialogTitle;

	/** @since 1.2 */
	public static String DocLuaNewFileWizard_title;

	public static String LuaNewFileWizardTitle;
	public static String LuaProjectCreatorInitializingSourceFolder;
	public static String LuaProjectWizard_warning_noSourceFolder;
	public static String LuaProjectWizardProjectWindowTitle;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
// CHECKSTYLE NLS: ON
