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

import org.eclipse.dltk.ast.ASTVisitor;
import org.eclipse.ldt.core.internal.ast.models.api.Item;
import org.eclipse.ldt.core.internal.ast.models.common.LuaASTNode;

public class LocalVar extends LuaASTNode {
	private final Item var;
	private int scopeMinOffset;
	private int scopeMaxOffset;

	public LocalVar(final Item var, final int scopeMinOffset, final int scopeMaxOffset) {
		this.var = var;
		this.scopeMinOffset = scopeMinOffset;
		this.scopeMaxOffset = scopeMaxOffset;
	}

	public Item getVar() {
		return var;
	}

	public int getScopeMinOffset() {
		return scopeMinOffset;
	}

	public int getScopeMaxOffset() {
		return scopeMaxOffset;
	}

	public void setScopeMinOffset(final int scopeMinOffset) {
		this.scopeMinOffset = scopeMinOffset;
	}

	public void setScopeMaxOffset(final int scopeMaxOffset) {
		this.scopeMaxOffset = scopeMaxOffset;
	}

	@Override
	public void traverse(final ASTVisitor visitor) throws Exception {
		if (visitor.visit(this)) {
			var.traverse(visitor);
			visitor.endvisit(this);
		}

	}
}
