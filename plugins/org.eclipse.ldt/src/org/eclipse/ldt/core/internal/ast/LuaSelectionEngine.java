/*******************************************************************************
 * Copyright (c) 2009, 2013 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 ******************************************************************************/
package org.eclipse.ldt.core.internal.ast;

import org.eclipse.dltk.codeassist.ScriptSelectionEngine;
import org.eclipse.dltk.compiler.env.IModuleSource;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.ldt.core.internal.ast.models.LuaASTModelUtils;
import org.eclipse.ldt.core.internal.ast.models.LuaASTUtils;
import org.eclipse.ldt.core.internal.ast.models.LuaASTUtils.Definition;
import org.eclipse.ldt.core.internal.ast.models.common.LuaSourceRoot;
import org.eclipse.ldt.core.internal.ast.models.file.LuaExpression;

public class LuaSelectionEngine extends ScriptSelectionEngine {

	@Override
	public IModelElement[] select(IModuleSource module, int start, int end) {
		// get the corresponding ISourceModule
		if (!(module instanceof ISourceModule))
			return new IModelElement[0];
		ISourceModule sourceModule = (ISourceModule) module;

		// get luaSourceRoot
		LuaSourceRoot luaSourceRoot = LuaASTModelUtils.getLuaSourceRoot(sourceModule);
		if (luaSourceRoot == null)
			return null;

		// get selected object
		// HACK when there are no selection end = (start - 1) that's why we use Math.max.
		LuaExpression luaExpression = LuaASTUtils.getLuaExpressionAt(luaSourceRoot, start, end + 1);
		if (luaExpression == null)
			return null;

		// get definition
		Definition definition = LuaASTUtils.getDefinition(sourceModule, luaExpression);
		if (definition == null)
			return null;

		// get the corresponding IModelElement
		IModelElement result = LuaASTModelUtils.getIModelElement(definition.getModule(), definition.getItem());
		if (result == null)
			return null;

		return new IModelElement[] { result };
	}
}
