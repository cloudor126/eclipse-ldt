/*******************************************************************************
 * Copyright (c) 2009, 2013 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/

package org.eclipse.ldt.metalua.tests.internal.cases;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import junit.framework.TestCase;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ldt.metalua.internal.Metalua;
import org.eclipse.ldt.metalua.tests.AllMetaluaTests;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Bundle;

import com.naef.jnlua.LuaException;
import com.naef.jnlua.LuaState;

/**
 * Make sure that calls to Metalua work
 * 
 * @author kkinfoo
 * 
 */
public class TestMetalua extends TestCase {
	private static final Bundle BUNDLE;
	static {
		BUNDLE = Platform.getBundle(AllMetaluaTests.PLUGIN_ID);
	}

	private LuaState state = null;

	public void setUp() {
		try {
			this.state = Metalua.newState();
		} catch (LuaException e) {
			assert false : "Unable to load Metalua " + e.getMessage(); //$NON-NLS-1$
		}
	}

	/** Make sure that syntax errors are catchable by Lua exception */
	public void testHandleErrors() {
		boolean error = false;
		String message = ""; //$NON-NLS-1$
		try {
			LuaState s = Metalua.newState();
			s.load("for", "badForStatement"); //$NON-NLS-1$ //$NON-NLS-2$
			s.call(0, 0);
		} catch (LuaException e) {
			error = true;
			message = e.getMessage();
		}
		assertTrue(message, error);
	}

	/** Run from source */
	public void testRunLuaCode() {

		// Proofing valid code
		try {
			state.load("var = 1+1", "regularAddition);"); //$NON-NLS-1$ //$NON-NLS-2$
			state.call(0, 0);
		} catch (LuaException e) {
			fail(e.getMessage());
		}

		// Proofing wrong code
		try {
			String invalidCode = "var local = 'trashed'"; //$NON-NLS-1$
			state.load(invalidCode, "regularAssignment"); //$NON-NLS-1$
			state.call(0, 0);
			fail("Able to load invalid code: " + invalidCode); //$NON-NLS-1$
		} catch (LuaException e) {
			assertTrue(true);
		}
	}

	/** Run Lua source file */
	public void testRunLuaFile() {

		// Proofing valid file
		try {
			File file = new File(path("/scripts/assignment.lua")); //$NON-NLS-1$
			FileInputStream input = new FileInputStream(file);
			state.load(input, "readingAssignmentFile"); //$NON-NLS-1$
			state.call(0, 0);
			input.close();
		} catch (IOException e) {
			fail(e.getMessage());
		} catch (LuaException e) {
			fail(e.getMessage());
		}
	}

	/** Run from source */
	public void testRunMetaluaCode() {
		// Proofing valid code
		try {
			final StringBuffer sb = new StringBuffer("local mlc = require ('metalua.compiler').new()\n"); //$NON-NLS-1$ 
			sb.append("ast = mlc:src_to_ast( 'var = 1 + 2 * 3' )"); //$NON-NLS-1$ 
			state.load(sb.toString(), "metaluaCode"); //$NON-NLS-1$ 
			state.call(0, 0);
		} catch (LuaException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Run Metalua source file
	 */
	public void testRunMetaluaFile() throws IOException {
		// Proofing valid file
		FileInputStream input = null;
		try {
			File file = new File(path("/scripts/introspection.mlua")); //$NON-NLS-1$
			input = new FileInputStream(file);
			state.load(input, "metaluaFile"); //$NON-NLS-1$
			state.call(0, 0);
		} catch (LuaException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		} finally {
			if (input != null)
				input.close();
		}
	}

	/** Run from source */
	public void testlua52() {
		try {
			final StringBuffer sb = new StringBuffer("local mlc = require ('metalua.compiler').new()\n"); //$NON-NLS-1$ 
			sb.append("ast = mlc:src_to_ast[[goto a\nprint 'hello'\n::a::]]"); //$NON-NLS-1$ 
			state.load(sb.toString(), "metaluaCode"); //$NON-NLS-1$ 
			state.call(0, 0);
		} catch (LuaException e) {
			fail(e.getMessage());
		}
	}

	public void testSourcesPath() {
		String path = Metalua.path();
		assertFalse("Metalua sources path is not definded.", path.isEmpty());//$NON-NLS-1$
		File directory = new File(path);
		assertTrue("Metalua sources path does not redirect to directory.", directory.isDirectory());//$NON-NLS-1$
	}

	/** Ensure access to portable file locations */
	private String path(final String uri) throws IOException {

		final StringBuffer sourcePath = new StringBuffer(FileLocator.getBundleFile(BUNDLE).getPath());
		final URL url = BUNDLE.getEntry(uri);
		if (url != null)
			sourcePath.append(new File(url.getFile()));
		else
			fail(NLS.bind("Unable to find {0}.", uri)); ////$NON-NLS-1$
		return sourcePath.toString();
	}
}
