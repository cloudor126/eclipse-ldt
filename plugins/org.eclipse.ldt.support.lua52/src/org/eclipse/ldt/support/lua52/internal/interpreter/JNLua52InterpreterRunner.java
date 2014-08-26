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

import org.eclipse.dltk.launching.IInterpreterInstall;
import org.eclipse.ldt.debug.core.internal.interpreter.jnlua.JNLuaInterpreterCommandLineRenderer;
import org.eclipse.ldt.debug.core.internal.interpreter.jnlua.JNLuaInterpreterRunner;

public class JNLua52InterpreterRunner extends JNLuaInterpreterRunner {

	public JNLua52InterpreterRunner(IInterpreterInstall install) {
		super(install);
	}

	@Override
	protected JNLuaInterpreterCommandLineRenderer createRenderCommandLine() {
		return new JNLua52InterpreterCommandLineRenderer();
	}
}
