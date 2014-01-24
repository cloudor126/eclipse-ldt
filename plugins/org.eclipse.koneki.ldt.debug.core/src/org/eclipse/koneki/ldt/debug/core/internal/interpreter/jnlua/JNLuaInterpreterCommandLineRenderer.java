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
package org.eclipse.koneki.ldt.debug.core.internal.interpreter.jnlua;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dltk.launching.ExecutionArguments;
import org.eclipse.dltk.launching.IInterpreterInstall;
import org.eclipse.dltk.launching.InterpreterConfig;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

public abstract class JNLuaInterpreterCommandLineRenderer {

	public static final String JNLUA_BUNDLE_ID = "com.naef.jnlua"; //$NON-NLS-1$

	public String[] renderCommandLine(InterpreterConfig config, IInterpreterInstall install) {
		final List<String> items = new ArrayList<String>();
		// add java bin
		items.add(getJavaBinPath());

		// add classPath
		final List<String> classPath = getClassPath();
		if (!classPath.isEmpty()) {
			items.add("-cp"); //$NON-NLS-1$

			StringBuilder libpath = new StringBuilder();
			for (final String libraryPathElement : classPath) {
				libpath.append(libraryPathElement);
				libpath.append(File.pathSeparatorChar);
			}
			// remove last path separator char
			libpath.deleteCharAt(libpath.length() - 1);
			items.add(libpath.toString());
		}

		// add java library path
		final List<String> libraryPath = getLibraryPath();
		if (!classPath.isEmpty()) {
			StringBuilder libpath = new StringBuilder();
			libpath.append("-Djava.library.path="); //$NON-NLS-1$
			for (final String libraryPathElement : libraryPath) {
				libpath.append(libraryPathElement);
				libpath.append(File.pathSeparatorChar);
			}
			// remove last path separator char
			libpath.deleteCharAt(libpath.length() - 1);

			items.add(libpath.toString());
		}

		// add class name to run
		items.add(getClassToRun());

		// add interpreter arg
		// final String[] interpreterOwnArgs =
		// install.getInterpreterArguments();
		// if (interpreterOwnArgs != null) {
		// items.addAll(Arrays.asList(interpreterOwnArgs));
		// }

		// TODO BUG_ECLIPSE 390358
		String args = install.getInterpreterArgs();
		if (args != null && !args.isEmpty()) {
			ExecutionArguments ex = new ExecutionArguments(args, ""); //$NON-NLS-1$
			final String[] interpreterOwnArgs = ex.getInterpreterArgumentsArray();
			if (interpreterOwnArgs != null) {
				items.addAll(Arrays.asList(interpreterOwnArgs));
			}
		}
		// end BUG_ECLIPSE 390358

		items.addAll(config.getInterpreterArgs());

		// add script to
		items.add(config.getScriptFilePath().toOSString());

		// add script arguments
		items.addAll(config.getScriptArgs());

		return items.toArray(new String[items.size()]);
	}

	protected abstract String getClassToRun();

	protected List<String> getLibraryPath() {
		final ArrayList<String> libraryPaths = new ArrayList<String>();

		// get class of jnlua bundle
		Bundle jNLuaBundle = getJNLuaBundle();

		// get fragments for this bundle.
		final Bundle[] fragments = Platform.getFragments(jNLuaBundle);

		// collect native code in all fragments
		for (Bundle fragment : fragments) {
			final String nativeCode = fragment.getHeaders().get(Constants.BUNDLE_NATIVECODE);
			if (nativeCode != null) {
				final String[] split = nativeCode.split("\\s*;\\s*"); //$NON-NLS-1$
				for (final String library : split) {
					try {
						final URL entry = fragment.getEntry(library);
						if (entry != null) {
							final URL libraryURL = FileLocator.toFileURL(entry);
							final String libraryPath = new Path(libraryURL.getFile()).removeLastSegments(1).toOSString();
							libraryPaths.add(libraryPath);
						}
					} catch (IOException e) {
						throw new RuntimeException("Unable to set java.library.path.", e); //$NON-NLS-1$
					}
				}
			}
		}

		return libraryPaths;
	}

	private Bundle getJNLuaBundle() {
		final Bundle[] bundles = Platform.getBundles(JNLUA_BUNDLE_ID, getJNLuaBundleVersion());

		// bundle must be present as we have a strong dependencies on it.
		if (bundles == null || bundles.length == 0)
			throw new RuntimeException(MessageFormat.format("Unable to resolve {0} bundle in version {1}.", JNLUA_BUNDLE_ID, //$NON-NLS-1$
					getJNLuaBundleVersion()));

		// because Platform.getBundles return all the bundles with a greater or equal version, we need to find the exact version
		Version expectedVersion = new Version(getJNLuaBundleVersion());

		for (Bundle aBundle : bundles) {
			Version bundleVersion = aBundle.getVersion();

			// compare version without qualifier
			if (bundleVersion.getMajor() == expectedVersion.getMajor() && bundleVersion.getMinor() == expectedVersion.getMinor()
					&& bundleVersion.getMicro() == expectedVersion.getMicro()) {
				return aBundle;
			}
		}

		throw new RuntimeException(MessageFormat.format("Unable to resolve {0} bundle for the version {1}.", JNLUA_BUNDLE_ID, //$NON-NLS-1$
				getJNLuaBundleVersion()));
	}

	protected abstract String getJNLuaBundleVersion();

	protected String getJavaBinPath() {
		final String javaHome = System.getProperty("java.home"); //$NON-NLS-1$
		final String javaBin = javaHome + File.separator + "bin" + File.separator + "java"; //$NON-NLS-1$//$NON-NLS-2$
		return javaBin;
	}

	protected List<String> getClassPath() {
		final ArrayList<String> classpath = new ArrayList<String>();

		classpath.add(getJNLuaClassPath());
		classpath.add(getLauncherClassPath());
		return classpath;

	}

	protected String getLauncherClassPath() {
		final Bundle launcherBundle = getLauncherClassBundle();

		// get folder which contains the ".class"
		final URL entry = launcherBundle.getResource("/"); //$NON-NLS-1$

		URL resolvedEntry;
		try {
			resolvedEntry = FileLocator.toFileURL(entry);
		} catch (IOException e) {
			throw new RuntimeException("Unable to resolve Launcher classpath.", e); //$NON-NLS-1$
		}

		return resolvedEntry.getFile();
	}

	protected abstract Bundle getLauncherClassBundle();

	protected String getJNLuaClassPath() {
		final Bundle jNLuaBundle = getJNLuaBundle();

		// get folder which contains the ".class"
		final URL entry = jNLuaBundle.getResource("/"); //$NON-NLS-1$

		final URL resolvedEntry;
		try {
			resolvedEntry = FileLocator.toFileURL(entry);
		} catch (IOException e) {
			throw new RuntimeException(MessageFormat.format("Unable to resolve class path for {0} bundle.", JNLUA_BUNDLE_ID), e); //$NON-NLS-1$
		}
		return resolvedEntry.getFile();
	}
}
