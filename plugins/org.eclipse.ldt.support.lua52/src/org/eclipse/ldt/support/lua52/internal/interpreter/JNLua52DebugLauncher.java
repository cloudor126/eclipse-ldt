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
package org.eclipse.ldt.support.lua52.internal.interpreter;

import com.naef.jnlua.LuaState;

public class JNLua52DebugLauncher extends JNLua52Launcher {

	protected void loadlibraries(LuaState l) {
		super.loadlibraries(l);
		TransportLayerModule.registerModelFactory(l);
	}

	public static void main(String[] args) {
		JNLua52DebugLauncher jnLuaLauncher = new JNLua52DebugLauncher();
		jnLuaLauncher.run(args);
	}
}
