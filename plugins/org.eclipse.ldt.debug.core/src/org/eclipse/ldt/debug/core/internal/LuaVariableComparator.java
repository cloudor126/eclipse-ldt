/*******************************************************************************
 * Copyright (c) 2011, 2012 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.ldt.debug.core.internal;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Comparator;

import org.apache.commons.lang.math.NumberUtils;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;

public class LuaVariableComparator implements Comparator<IVariable>, Serializable {
	private static final long serialVersionUID = -5828968181211469862L;

	private int category(IVariable var) throws DebugException {
		return var.getReferenceTypeName().equals(LuaDebugConstants.TYPE_SPECIAL) ? 0 : 1;
	}

	private String extractValueFromBracket(String key) {
		int keyLenght = key.length();

		if (key.charAt(0) == '[' && key.charAt(keyLenght - 1) == ']') {
			return key.substring(1, keyLenght - 1);
		}
		return null;
	}

	private Double getArrayVarNumberIndex(IVariable var) throws DebugException {
		if (NumberUtils.isNumber(extractValueFromBracket(var.getName().trim()))) {
			try {
				return Double.valueOf(extractValueFromBracket(var.getName().trim()));
			} catch (NumberFormatException e) {
				Activator.logWarning(MessageFormat.format("[VariableView] Unable to retreive table Index for varialble {0}", var.getName()), e); //$NON-NLS-1$
			}
		}
		return null;
	}

	@Override
	public int compare(IVariable v1, IVariable v2) {
		try {

			// handle if something is null
			if (v1 == null && v2 == null) {
				return 0;
			} else if (v1 == null) {
				return 1;
			} else if (v2 == null) {
				return -1;
			}

			// TODO Does the categories are still used ?
			int cat1 = category(v1);
			int cat2 = category(v2);

			if (cat1 != cat2) {
				return cat1 - cat2;
			}

			// Bug 386149 - [debugger] Debug Inspector window showing wrong order of arrays
			// In arrays and tables key are also sorted but keys can be numbers and strings
			// Instead to always sort my variable name, check if both var name is numbers, and compare by numbers
			Double numberIndex2 = getArrayVarNumberIndex(v2);
			Double numberIndex1 = getArrayVarNumberIndex(v1);

			// both number
			if (numberIndex1 != null && numberIndex2 != null) {
				return NumberUtils.compare(numberIndex1, numberIndex2);
			}
			// if just one of the var name is a number, put numbers first
			else if (numberIndex1 != null) {
				return -1;
			} else if (numberIndex2 != null) {
				return 1;
			}
			// if none of the vars name are numbers, compare name using String method
			else {
				return v1.getName().compareTo(v2.getName());
			}

		} catch (DebugException e) {
			return 0;
		}
	}
}
