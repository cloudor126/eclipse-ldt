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

public class Literal extends LuaExpression {
	private String literal;

	/**
	 * @return the literal
	 */
	public String getLiteral() {
		return literal;
	}

	/**
	 * @param literal
	 *            the literal to set
	 */
	public void setLiteral(final String literal) {
		this.literal = literal;
	}

	@Override
	public void traverse(ASTVisitor visitor) throws Exception {
		if (visitor.visit(this)) {
			visitor.endvisit(this);
		}
	}
}
