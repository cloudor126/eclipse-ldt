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
package org.eclipse.ldt.debug.core.internal.interpreter.generic;

import org.eclipse.core.runtime.ILog;
import org.eclipse.dltk.launching.IInterpreterInstall;
import org.eclipse.ldt.debug.core.internal.Activator;
import org.eclipse.ldt.debug.core.interpreter.AbstractLuaInterpreterInstallType;

public class LuaGenericInterpreterInstallType extends AbstractLuaInterpreterInstallType {

	@Override
	public String getName() {
		return "Generic Lua"; //$NON-NLS-1$
	}

	@Override
	protected IInterpreterInstall doCreateInterpreterInstall(String id) {
		return new LuaGenericInterpreterInstall(this, id);
	}

	@Override
	protected String getPluginId() {
		return Activator.PLUGIN_ID;
	}

	@Override
	protected ILog getLog() {
		return Activator.getDefault().getLog();
	}
}
