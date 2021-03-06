/*******************************************************************************
 * Copyright (c) 2014 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.ldt.debug.core.internal.interpreter.luajit;

import org.eclipse.dltk.launching.IInterpreterInstallType;
import org.eclipse.ldt.debug.core.interpreter.AbstractLuaInterpreterInstall;

public class LuaJITInterpreterInstall extends AbstractLuaInterpreterInstall {

	public LuaJITInterpreterInstall(final IInterpreterInstallType type, final String id) {
		super(type, id);
	}
}
