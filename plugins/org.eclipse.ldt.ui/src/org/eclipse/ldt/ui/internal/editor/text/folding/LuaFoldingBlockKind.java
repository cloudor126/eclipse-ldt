/*******************************************************************************
 * Copyright (c) 2012 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.ldt.ui.internal.editor.text.folding;

import org.eclipse.dltk.ui.text.folding.IFoldingBlockKind;

public enum LuaFoldingBlockKind implements IFoldingBlockKind {
	COMMENT(true), DOC(true);

	private final boolean isComment;

	private LuaFoldingBlockKind(boolean isComment) {
		this.isComment = isComment;
	}

	public boolean isComment() {
		return isComment;
	}

}