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
package org.eclipse.ldt.lua.tests.internal.formatter;

import org.eclipse.ldt.lua.tests.internal.utils.AbstractLuaTestSuite;

public class LineEndingTestSuite extends AbstractLuaTestSuite {

	public LineEndingTestSuite(final boolean ignore) {
		super("Line endings", "tests/formatterlineendings", "lua", ignore); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
