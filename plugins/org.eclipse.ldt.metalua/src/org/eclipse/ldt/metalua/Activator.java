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

/**
 * @author	Kevin KIN-FOO <kkinfoo@anyware-tech.com>
 * @date 
 */
package org.eclipse.ldt.metalua;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 */
public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.eclipse.ldt.metalua"; //$NON-NLS-1$

	private static Activator plugin;

	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext )
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * build a Status
	 * 
	 * @param level
	 *            of the status, such as error or warning from {@link IStatus} constants
	 * @param thr
	 *            Raised to build status
	 * @return
	 */
	public static IStatus buildStatus(int level, final Throwable thr) {
		String defaultMsg = "No details available."; //$NON-NLS-1$
		String msg = thr.getMessage() == null ? defaultMsg : thr.getMessage();
		IStatus status = new Status(level, PLUGIN_ID, 0, msg, thr);
		return status;
	}

	/**
	 * Enable to log events such as {@link Exception} in plugin log
	 * 
	 * @param thr
	 */
	public static void log(final Throwable thr) {
		log(buildStatus(IStatus.ERROR, thr));
	}

	/**
	 * Enables to log statuses in plugin log
	 * 
	 * @param status
	 */
	public static void log(final IStatus status) {
		getDefault().getLog().log(status);
	}

	/**
	 * Log a error message caused by the given exception
	 * 
	 * @param message
	 *            message to log
	 * @param throwable
	 *            exception which causes the error
	 */
	public static void logError(final String message, final Throwable throwable) {
		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, message, throwable);
		getDefault().getLog().log(status);
	}

	/**
	 * Log a simple warning message
	 * 
	 * @param message
	 *            message to log
	 */
	public static void logWarning(final String message) {
		IStatus status = new Status(IStatus.WARNING, PLUGIN_ID, message);
		getDefault().getLog().log(status);
	}

	/**
	 * Log a warning message caused by the given exception
	 * 
	 * @param message
	 *            message to log
	 * @param throwable
	 *            exception which causes the warning
	 */
	public static void logWarning(final String message, final Throwable throwable) {
		IStatus status = new Status(IStatus.WARNING, PLUGIN_ID, message, throwable);
		getDefault().getLog().log(status);
	}

}
