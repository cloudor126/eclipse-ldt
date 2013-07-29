/*******************************************************************************
 * Copyright (c) 2009, 2012 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.koneki.ldt.ui.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.koneki.ldt.ui.internal.editor.text.LuaTextTools;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.koneki.ldt.ui"; //$NON-NLS-1$

	//
	public static final String LUA_DOC_VIEWID = "org.eclipse.koneki.ldt.ui.luadoc"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	// TODO not sure lua Text tools should be an activator attribute.
	private LuaTextTools fLuaTextTools;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	public synchronized LuaTextTools getTextTools() {
		if (fLuaTextTools == null) {
			fLuaTextTools = new LuaTextTools(true);
		}
		return fLuaTextTools;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeImageRegistry(org.eclipse.jface.resource.ImageRegistry)
	 */
	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		reg.put(ImageConstants.LUA_WIZARD_BAN, AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, ImageConstants.LUA_WIZARD_BAN));
		reg.put(ImageConstants.LUA_FILE_WIZARD_BAN, AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, ImageConstants.LUA_FILE_WIZARD_BAN));
		reg.put(ImageConstants.NEW_FILE, AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, ImageConstants.NEW_FILE));
		reg.put(ImageConstants.TEMPLATE_LUADOC, AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, ImageConstants.TEMPLATE_LUADOC));

		reg.put(ImageConstants.MODULE_OBJ16, AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, ImageConstants.MODULE_OBJ16));
		reg.put(ImageConstants.TYPE_OBJ16, AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, ImageConstants.TYPE_OBJ16));

		reg.put(ImageConstants.MODULE_FUNCTION_OBJ16, AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, ImageConstants.MODULE_FUNCTION_OBJ16));
		reg.put(ImageConstants.MODULE_FIELD_OBJ16, AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, ImageConstants.MODULE_FIELD_OBJ16));
		reg.put(ImageConstants.MODULE_TABLE_OBJ16, AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, ImageConstants.MODULE_TABLE_OBJ16));

		reg.put(ImageConstants.PRIVATE_FUNCTION_OBJ16, AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, ImageConstants.PRIVATE_FUNCTION_OBJ16));
		reg.put(ImageConstants.PRIVATE_FIELD_OBJ16, AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, ImageConstants.PRIVATE_FIELD_OBJ16));
		reg.put(ImageConstants.PRIVATE_TABLE_OBJ16, AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, ImageConstants.PRIVATE_TABLE_OBJ16));

		reg.put(ImageConstants.PUBLIC_FUNCTION_OBJ16, AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, ImageConstants.PUBLIC_FUNCTION_OBJ16));
		reg.put(ImageConstants.PUBLIC_FIELD_OBJ16, AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, ImageConstants.PUBLIC_FIELD_OBJ16));
		reg.put(ImageConstants.PUBLIC_TABLE_OBJ16, AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, ImageConstants.PUBLIC_TABLE_OBJ16));
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

	/**
	 * Log the given status
	 * 
	 * @param status
	 *            status to log
	 */
	public static void log(final IStatus status) {
		getDefault().getLog().log(status);
	}

}
