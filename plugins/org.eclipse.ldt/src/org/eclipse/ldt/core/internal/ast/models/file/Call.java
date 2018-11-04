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
package org.eclipse.ldt.core.internal.ast.models.file;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dltk.ast.ASTVisitor;

public class Call extends LuaExpression {
	private List<LuaExpression> argList = new ArrayList<LuaExpression>();
	private LuaExpression function;

	public void addArg(LuaExpression arg) {
		argList.add(arg);
	}

	public List<LuaExpression> getArgList() {
		return argList;
	}

	public LuaExpression getFunction() {
		return function;
	}

	public void setFunction(final LuaExpression function) {
		this.function = function;
	}

	@Override
	public void traverse(ASTVisitor visitor) throws Exception {
		if (visitor.visit(this)) {
			// traverse function
			function.traverse(visitor);
			// traverse arg
			for (LuaExpression arg : argList) {
				arg.traverse(visitor);
			}
			visitor.endvisit(this);
		}
	}
}
