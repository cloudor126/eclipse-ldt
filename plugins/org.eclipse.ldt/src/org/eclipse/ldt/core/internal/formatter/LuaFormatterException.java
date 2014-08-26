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
package org.eclipse.ldt.core.internal.formatter;

public class LuaFormatterException extends Exception {
	private static final long serialVersionUID = -6874143023474176808L;

	protected LuaFormatterException(final String errorMessage) {
		super(errorMessage);
	}
}
