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
package org.eclipse.ldt.support.lua51.internal.interpreter;

import org.eclipse.ldt.debug.core.internal.interpreter.jnlua.JNLuaDebugginEngineCommandLineRenderer;
import org.eclipse.ldt.support.lua51.internal.Activator;
import org.osgi.framework.Bundle;

public class JNLua51DebugginEngineCommandLineRenderer extends JNLuaDebugginEngineCommandLineRenderer {

	@Override
	protected String getClassToRun() {
		return JNLua51DebugLauncher.class.getCanonicalName();
	}

	@Override
	protected String getJNLuaBundleVersion() {
		return JNLua51InterpreterCommandLineRenderer.JNLUA_BUNDLE_VERSION;
	}

	@Override
	protected Bundle getLauncherClassBundle() {
		return Activator.getDefault().getBundle();
	}
}
