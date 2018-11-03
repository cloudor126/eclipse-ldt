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
package org.eclipse.ldt.core.internal.ast.models.api;

import org.eclipse.ldt.core.internal.ast.models.file.LuaExpression;

public class KeyExprTypeRef extends LazyTypeRef {

	private LuaExpression expression;

	public KeyExprTypeRef() {
		super();
		this.expression = null;
	}

	public LuaExpression getExpression() {
		return expression;
	}

	public void setExpression(LuaExpression expression) {
		this.expression = expression;
	}

}
