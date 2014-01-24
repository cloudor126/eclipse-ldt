/*******************************************************************************
 * Copyright (c) 2011, 2013 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/eplv10.html
 *
 * Contributors:
 *     Sierra Wireless  initial API and implementation
 *******************************************************************************/
package org.eclipse.koneki.ldt.core.internal.ast.parser;

import java.util.List;

import org.eclipse.dltk.ast.ASTNode;
import org.eclipse.dltk.ast.declarations.Declaration;
import org.eclipse.dltk.compiler.IElementRequestor;
import org.eclipse.dltk.compiler.IElementRequestor.FieldInfo;
import org.eclipse.dltk.compiler.IElementRequestor.MethodInfo;
import org.eclipse.dltk.compiler.IElementRequestor.TypeInfo;
import org.eclipse.dltk.compiler.ISourceElementRequestor;
import org.eclipse.dltk.compiler.SourceElementRequestVisitor;
import org.eclipse.koneki.ldt.core.internal.ast.models.LuaASTUtils;
import org.eclipse.koneki.ldt.core.internal.ast.models.api.ExternalTypeRef;
import org.eclipse.koneki.ldt.core.internal.ast.models.api.FunctionTypeDef;
import org.eclipse.koneki.ldt.core.internal.ast.models.api.InternalTypeRef;
import org.eclipse.koneki.ldt.core.internal.ast.models.api.Item;
import org.eclipse.koneki.ldt.core.internal.ast.models.api.LuaFileAPI;
import org.eclipse.koneki.ldt.core.internal.ast.models.api.Parameter;
import org.eclipse.koneki.ldt.core.internal.ast.models.api.PrimitiveTypeRef;
import org.eclipse.koneki.ldt.core.internal.ast.models.api.RecordTypeDef;
import org.eclipse.koneki.ldt.core.internal.ast.models.api.TypeDef;
import org.eclipse.koneki.ldt.core.internal.ast.models.file.Block;

/**
 * traverse the Lua AST of a file to extract the DLTK model.
 */
public class LuaSourceElementRequestorVisitor extends SourceElementRequestVisitor {
	private LuaFileAPI luafileapi = null;
	private Block firstBlock = null;

	public LuaSourceElementRequestorVisitor(ISourceElementRequestor requesor) {
		super(requesor);
	}

	/**
	 * @see org.eclipse.dltk.ast.ASTVisitor#visit(org.eclipse.dltk.ast.ASTNode)
	 */
	@Override
	public boolean visit(ASTNode s) throws Exception {
		if (s instanceof Item)
			return visit((Item) s);
		else if (s instanceof RecordTypeDef)
			return visit((RecordTypeDef) s);
		else if (s instanceof LuaFileAPI)
			return visit((LuaFileAPI) s);
		else if (s instanceof Block)
			return visit((Block) s);
		return super.visit(s);
	}

	/**
	 * @see org.eclipse.dltk.ast.ASTVisitor#visit(org.eclipse.dltk.ast.ASTNode)
	 */
	@Override
	public boolean endvisit(ASTNode s) throws Exception {
		if (s instanceof RecordTypeDef)
			return endvisit((RecordTypeDef) s);
		else if (s instanceof LuaFileAPI)
			return endvisit((LuaFileAPI) s);
		else if (s instanceof Item)
			return endvisit((Item) s);
		return super.endvisit(s);
	}

	public boolean visit(LuaFileAPI luaAPI) throws Exception {
		luafileapi = luaAPI;
		return true;
	}

	public boolean endvisit(LuaFileAPI luaAPI) throws Exception {
		// HACK we need the lua fileapi when we traverse the internal content
		// so we do no set it to null at end visiting anymore.
		// see LuaSourceRoot.LuaFile.traverse(ASTVisitor)
		// luafileapi = null;
		return true;
	}

	public boolean visit(Block block) throws Exception {
		if (firstBlock == null) {
			firstBlock = block;
			return true;
		}
		return false;
	}

	public boolean visit(Item item) throws Exception {
		// an item could be transform in a method or a field
		TypeDef resolvedType = LuaASTUtils.resolveTypeLocaly(luafileapi, item);

		if (resolvedType instanceof FunctionTypeDef) {
			// we manage a METHOD
			MethodInfo methodInfo = new ISourceElementRequestor.MethodInfo();
			FunctionTypeDef funtionTypeDef = (FunctionTypeDef) resolvedType;

			// extract parameters
			List<Parameter> params = funtionTypeDef.getParameters();
			String[] parametersName = new String[params.size()];
			for (int i = 0; i < params.size(); i++) {
				Parameter param = (Parameter) params.get(i);
				parametersName[i] = param.getName();
			}

			// set METHOD information
			methodInfo.name = item.getName();
			methodInfo.parameterNames = parametersName;
			methodInfo.nameSourceStart = item.sourceStart();
			methodInfo.nameSourceEnd = item.sourceEnd() - 1;
			methodInfo.declarationStart = item.sourceStart();

			// calculate modifiers
			int modifiers = 0; // define kind modifier
			// define visibility
			if (LuaASTUtils.isPrivate(item)) {
				modifiers |= Declaration.AccPrivate;
			} else if (LuaASTUtils.isModule(luafileapi, item)) {
				modifiers |= Declaration.AccModule;
			} else if (LuaASTUtils.isPublic(item)) {
				modifiers |= Declaration.AccPublic;
			}
			methodInfo.modifiers = modifiers;

			// store method info
			this.fRequestor.enterMethod(methodInfo);
			return true;
		} else {
			// we manage a FIELD
			FieldInfo fieldinfo = new IElementRequestor.FieldInfo();

			// set FIELD information
			fieldinfo.name = item.getName();
			fieldinfo.nameSourceStart = item.sourceStart();
			fieldinfo.nameSourceEnd = item.sourceEnd() - 1;
			fieldinfo.declarationStart = item.sourceStart();

			// calculate modifiers
			int modifiers = 0; // define kind modifier
			// define visibility
			if (LuaASTUtils.isPrivate(item)) {
				modifiers |= Declaration.AccPrivate;
			} else if (LuaASTUtils.isModule(luafileapi, item)) {
				modifiers |= Declaration.AccModule;
			} else if (LuaASTUtils.isPublic(item)) {
				modifiers |= Declaration.AccPublic;
			}
			if (resolvedType instanceof RecordTypeDef)
				modifiers |= Declaration.AccInterface;

			fieldinfo.modifiers = modifiers;

			// calculate type
			if (item.getType() instanceof PrimitiveTypeRef || item.getType() instanceof InternalTypeRef || item.getType() instanceof ExternalTypeRef)
				fieldinfo.type = item.getType().toReadableString();

			// store field info
			this.fRequestor.enterField(fieldinfo);
			return true;
		}
	}

	public boolean endvisit(Item item) throws Exception {
		int declarationEnd = item.sourceEnd();
		this.fRequestor.exitField(declarationEnd);
		return true;
	}

	public boolean visit(RecordTypeDef type) throws Exception {
		if (!LuaASTUtils.isInlineTypeDef(type)) {
			RecordTypeDef recordtype = (RecordTypeDef) type;

			// set TYPE Information
			TypeInfo typeinfo = new IElementRequestor.TypeInfo();
			typeinfo.name = recordtype.getName();
			typeinfo.declarationStart = type.sourceStart();
			typeinfo.nameSourceStart = type.sourceStart();
			typeinfo.nameSourceEnd = type.sourceEnd() - 1;

			if (type.getSupertype() != null) {
				typeinfo.superclasses = new String[] { type.getSupertype().toReadableString() };
			}

			// calculate modifiers
			int modifiers = 0; // define kind modifier
			// define visibility
			if (LuaASTUtils.isModule(luafileapi, recordtype)) {
				modifiers |= Declaration.AccModule;
			} else {
				modifiers |= Declaration.AccPublic;
			}
			typeinfo.modifiers = modifiers;

			this.fRequestor.enterType(typeinfo);
		}
		return true;
	}

	public boolean endvisit(RecordTypeDef type) throws Exception {
		if (!LuaASTUtils.isInlineTypeDef(type)) {
			int declarationEnd = type.sourceEnd();
			this.fRequestor.exitType(declarationEnd);
		}
		return true;
	}
}
