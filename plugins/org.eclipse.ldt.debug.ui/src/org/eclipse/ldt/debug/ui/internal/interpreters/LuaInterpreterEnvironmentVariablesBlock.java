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
package org.eclipse.ldt.debug.ui.internal.interpreters;

import org.eclipse.dltk.internal.debug.ui.interpreters.AbstractInterpreterEnvironmentVariablesBlock;
import org.eclipse.dltk.internal.debug.ui.interpreters.AddScriptInterpreterDialog;

public class LuaInterpreterEnvironmentVariablesBlock extends AbstractInterpreterEnvironmentVariablesBlock {

	protected LuaInterpreterEnvironmentVariablesBlock(AddScriptInterpreterDialog dialog) {
		super(dialog);
	}

}
