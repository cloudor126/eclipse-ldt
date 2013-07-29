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
package org.eclipse.koneki.ldt.core.internal.ast.models.api;

import org.eclipse.koneki.ldt.core.internal.ast.models.common.IDocumentationHolder;
import org.eclipse.koneki.ldt.core.internal.ast.models.common.LuaASTNode;

/**
 * a TypeDef is the definition of a kind of type.<br/>
 * User could use it define a kind of recordtype or a kind of functiontype.
 */
// CHECKSTYLE:OFF
public abstract class TypeDef extends LuaASTNode implements IDocumentationHolder {
	// CHECKSTYLE:ON

	private LuaASTNode parent;

	public LuaASTNode getParent() {
		return parent;
	}

	public void setParent(LuaASTNode parent) {
		this.parent = parent;
	}
}
