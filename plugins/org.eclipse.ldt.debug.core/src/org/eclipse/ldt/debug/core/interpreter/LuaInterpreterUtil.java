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
package org.eclipse.ldt.debug.core.interpreter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dltk.launching.IInterpreterInstall;
import org.eclipse.dltk.launching.IInterpreterInstallType;
import org.eclipse.dltk.launching.ScriptRuntime;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.ldt.debug.core.internal.model.interpreter.Info;
import org.eclipse.ldt.debug.core.internal.model.interpreter.InterpreterPackage;
import org.eclipse.ldt.core.LuaNature;

/**
 * @since 1.2
 */
public final class LuaInterpreterUtil {

	private LuaInterpreterUtil() {
	}

	public static IInterpreterInstall getDefaultInterpreter() {
		return ScriptRuntime.getDefaultInterpreterInstall(LuaNature.ID, null);
	}

	public static List<IInterpreterInstall> getInterpreters() {
		List<IInterpreterInstall> interpreters = new ArrayList<IInterpreterInstall>();

		IInterpreterInstallType[] types = ScriptRuntime.getInterpreterInstallTypes(LuaNature.ID);
		for (int i = 0; i < types.length; i++) {
			IInterpreterInstallType type = types[i];
			IInterpreterInstall[] installs = type.getInterpreterInstalls();
			if (installs != null)
				for (int j = 0; j < installs.length; j++) {
					IInterpreterInstall install = installs[j];
					interpreters.add(install);
				}
		}

		return interpreters;
	}

	public static boolean isEmbedded(final IInterpreterInstall interpreter) {
		IInterpreterInstallType interpreterInstallType = interpreter.getInterpreterInstallType();
		return interpreterInstallType instanceof ILuaInterpreterInstallType
				&& ((ILuaInterpreterInstallType) interpreterInstallType).isEmbeddedInterpreter();
	}

	public static boolean interpreterHandlesExecuteOption(final IInterpreterInstall interpreter) {

		// Fetch interpreter option
		if (interpreter != null) {
			final Info info = getInfoFromInterpreter(interpreter);
			if (info != null)
				return info.isExecuteOptionCapable();
		}

		// Use default option value
		return (Boolean) InterpreterPackage.eINSTANCE.getInfo_ExecuteOptionCapable().getDefaultValue();
	}

	public static boolean interpreterHandlesFilesAsArgument(final IInterpreterInstall interpreter) {

		// Fetch interpreter option
		if (interpreter != null) {
			final Info info = getInfoFromInterpreter(interpreter);
			if (info != null)
				return info.isFileAsArgumentsCapable();
		}

		// Use default option value
		return (Boolean) InterpreterPackage.eINSTANCE.getInfo_FileAsArgumentsCapable().getDefaultValue();
	}

	public static String linkedExecutionEnvironmentName(final IInterpreterInstall interpreter) {

		// Fetch interpreter option
		if (interpreter != null) {
			final Info info = getInfoFromInterpreter(interpreter);
			if (info != null)
				return info.getLinkedExecutionEnvironmentName();
			else {
				IInterpreterInstallType interpreterInstallType = interpreter.getInterpreterInstallType();
				if (interpreterInstallType instanceof ILuaInterpreterInstallType)
					return ((ILuaInterpreterInstallType) interpreterInstallType).getDefaultEEName();
			}

		}

		// Use default option value
		return null;
	}

	public static String linkedExecutionEnvironmentVersion(final IInterpreterInstall interpreter) {

		// Fetch interpreter option
		if (interpreter != null) {
			final Info info = getInfoFromInterpreter(interpreter);
			if (info != null)
				return info.getLinkedExecutionEnvironmentVersion();
			else {
				IInterpreterInstallType interpreterInstallType = interpreter.getInterpreterInstallType();
				if (interpreterInstallType instanceof ILuaInterpreterInstallType)
					return ((ILuaInterpreterInstallType) interpreterInstallType).getDefaultEEVersion();
			}
		}

		// Use default option value
		return null;
	}

	public static boolean isExecutionEnvironmentCompatible(IInterpreterInstall interpreter, String eeName, String eeVersion) {
		String linkedEEName = LuaInterpreterUtil.linkedExecutionEnvironmentName(interpreter);
		String linkedEEVersion = LuaInterpreterUtil.linkedExecutionEnvironmentVersion(interpreter);
		return linkedEEName != null && linkedEEVersion != null && linkedEEName.equals(eeName) && linkedEEVersion.equals(eeVersion);
	}

	private static Info getInfoFromInterpreter(final IInterpreterInstall interpreter) {
		for (final EObject extension : interpreter.getExtensions())
			if (extension instanceof Info)
				return (Info) extension;
		return null;
	}
}
