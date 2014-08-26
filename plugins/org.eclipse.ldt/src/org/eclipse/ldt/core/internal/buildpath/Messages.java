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
package org.eclipse.ldt.core.internal.buildpath;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {
	// CHECKSTYLE NLS: OFF
	private static final String BUNDLE_NAME = "org.eclipse.ldt.core.internal.buildpath.messages"; //$NON-NLS-1$

	public static String LuaExecutionEnvironmentBuildpathContainerEENotFound;

	public static String LuaExecutionEnvironmentBuildpathContainerNoDescriptionAvailable;

	public static String LuaExecutionEnvironmentManagerLegacyTemplateFolder;

	public static String LuaExecutionEnvironmentManagerNoDefaultTemplate;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	// CHECKSTYLE NLS: ON
	private Messages() {
	}
}
