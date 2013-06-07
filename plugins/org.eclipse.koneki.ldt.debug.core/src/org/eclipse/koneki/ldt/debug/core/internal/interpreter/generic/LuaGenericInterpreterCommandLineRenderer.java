/*******************************************************************************
 * Copyright (c) 2012, 2013 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.koneki.ldt.debug.core.internal.interpreter.generic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.dltk.launching.ExecutionArguments;
import org.eclipse.dltk.launching.IInterpreterInstall;
import org.eclipse.dltk.launching.InterpreterConfig;

public class LuaGenericInterpreterCommandLineRenderer {

	public String[] renderCommandLine(final InterpreterConfig config, final IInterpreterInstall install) {
		final List<String> items = new ArrayList<String>();

		items.add(install.getInstallLocation().toOSString());

		/*                                                              */
		/* This code must be used when work around below become useless */
		/*                                                              */
		// final String[] interpreterOwnArgs =
		// install.getInterpreterArguments();
		// if (interpreterOwnArgs != null) {
		// items.addAll(Arrays.asList(interpreterOwnArgs));
		// }

		// TODO BUG_ECLIPSE 390358
		final String args = install.getInterpreterArgs();
		if (args != null && !args.isEmpty()) {
			final ExecutionArguments ex = new ExecutionArguments(args, ""); //$NON-NLS-1$
			final String[] interpreterOwnArgs = ex.getInterpreterArgumentsArray();
			if (interpreterOwnArgs != null) {
				items.addAll(Arrays.asList(interpreterOwnArgs));
			}
		}
		// end BUG_ECLIPSE 390358
		items.addAll(config.getInterpreterArgs());

		// Precise script and argument only when interpreter handles script as arguments
		if (LuaGenericInterpreterUtil.interpreterHandlesFilesAsArgument(install)) {
			items.add(install.getEnvironment().convertPathToString(config.getScriptFilePath()));
			items.addAll(config.getScriptArgs());
		}

		return items.toArray(new String[items.size()]);
	}
}
