/*******************************************************************************
 * Copyright (c) 2009, 2011 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.ldt.core.tests.internal.ast;

import org.eclipse.ldt.core.tests.internal.ast.utils.AbstractParserTest;
import org.junit.Test;

/**
 * Module Parsing Tests
 */

public class TestTables extends AbstractParserTest {

	/**
	 * parse table
	 */
	@Test
	public void testTableWithOneScalarField() {
		parse("local t = {} t.f1, t.f2 = 2,3"); //$NON-NLS-1$		
	}

	@Test
	public void testTableWithValues() {
		parse("local t = {0, '1', nil, {}, function() end}"); //$NON-NLS-1$		
	}

	@Test
	public void testTableWithKeys() {
		parse("local t = {['f']=function() end}"); //$NON-NLS-1$		
	}

	@Test
	public void testTableWithTrailingComma() {
		parse("local t = {0,}"); //$NON-NLS-1$		
	}

	@Test
	public void testTableWithTrailingSemiColon() {
		parse("local t = {0;}"); //$NON-NLS-1$		
	}

	@Test
	public void testSeveralValuesTableWithTrailingSemiColon() {
		parse("local t = {0;0,}"); //$NON-NLS-1$		
	}
}
