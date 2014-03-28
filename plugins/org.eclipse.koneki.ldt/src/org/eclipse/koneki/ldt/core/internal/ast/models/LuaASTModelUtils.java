/*******************************************************************************
 * Copyright (c) 2011, 2013 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.koneki.ldt.core.internal.ast.models;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dltk.ast.ASTNode;
import org.eclipse.dltk.ast.declarations.Declaration;
import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
import org.eclipse.dltk.core.Flags;
import org.eclipse.dltk.core.IField;
import org.eclipse.dltk.core.IMember;
import org.eclipse.dltk.core.IMethod;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.core.SourceParserUtil;
import org.eclipse.koneki.ldt.core.internal.Activator;
import org.eclipse.koneki.ldt.core.internal.ast.models.LuaASTUtils.TypeResolution;
import org.eclipse.koneki.ldt.core.internal.ast.models.api.ExternalTypeRef;
import org.eclipse.koneki.ldt.core.internal.ast.models.api.FunctionTypeDef;
import org.eclipse.koneki.ldt.core.internal.ast.models.api.InternalTypeRef;
import org.eclipse.koneki.ldt.core.internal.ast.models.api.Item;
import org.eclipse.koneki.ldt.core.internal.ast.models.api.LuaFileAPI;
import org.eclipse.koneki.ldt.core.internal.ast.models.api.PrimitiveTypeRef;
import org.eclipse.koneki.ldt.core.internal.ast.models.api.RecordTypeDef;
import org.eclipse.koneki.ldt.core.internal.ast.models.api.TypeDef;
import org.eclipse.koneki.ldt.core.internal.ast.models.common.LuaASTNode;
import org.eclipse.koneki.ldt.core.internal.ast.models.common.LuaSourceRoot;
import org.eclipse.koneki.ldt.core.internal.ast.models.dltk.FakeField;
import org.eclipse.koneki.ldt.core.internal.ast.models.dltk.FakeMethod;
import org.eclipse.koneki.ldt.core.internal.ast.models.dltk.IFakeElement;
import org.eclipse.koneki.ldt.core.internal.ast.models.file.LocalVar;

public final class LuaASTModelUtils {
	private LuaASTModelUtils() {
	}

	/**
	 * Get LuaSourceRoot from ISourceModule <br/>
	 * 
	 * DLTK Model => AST
	 */
	public static LuaSourceRoot getLuaSourceRoot(ISourceModule module) {
		ModuleDeclaration moduleDeclaration = SourceParserUtil.getModuleDeclaration(module);
		if (moduleDeclaration instanceof LuaSourceRoot)
			return (LuaSourceRoot) moduleDeclaration;
		return null;
	}

	/**
	 * Get LuaSourceRoot from ISourceModule <br/>
	 * 
	 * DLTK Model => AST
	 */
	public static ASTNode getASTNode(final IModelElement modelElement) {
		if (modelElement instanceof IFakeElement)
			return ((IFakeElement) modelElement).getLuaASTNode();
		if (modelElement instanceof ISourceModule)
			return getLuaSourceRoot((ISourceModule) modelElement);
		if (modelElement instanceof IType)
			return getTypeDef((IType) modelElement);
		if (modelElement instanceof IField)
			return getItem((IField) modelElement);
		if (modelElement instanceof IMethod)
			return getItem((IMethod) modelElement);
		return null;
	}

	/**
	 * Get Record type def from {@link ISourceModule} <br/>
	 * 
	 * DLTK Model => AST
	 */
	public static TypeDef getTypeDef(IType type) {
		LuaSourceRoot luaSourceRoot = getLuaSourceRoot(type.getSourceModule());
		LuaFileAPI fileapi = luaSourceRoot.getFileapi();
		return fileapi.getTypes().get(type.getElementName());
	}

	/**
	 * Get Item from {@link IField} <br/>
	 * 
	 * DLTK Model => AST
	 */
	public static Item getItem(IField field) {
		IModelElement parent = field.getParent();
		if (parent instanceof IType) {
			RecordTypeDef typeDef = (RecordTypeDef) getTypeDef((IType) parent);
			return typeDef.getFields().get(field.getElementName());
		} else if (parent instanceof ISourceModule) {
			LuaSourceRoot luaSourceRoot = getLuaSourceRoot((ISourceModule) parent);
			try {
				if (Flags.isPrivate(field.getFlags())) {
					List<LocalVar> localVars = luaSourceRoot.getInternalContent().getContent().getLocalVars();
					for (LocalVar localVar : localVars) {
						if (localVar.getVar().getName().equals(field.getElementName()))
							return localVar.getVar();
					}
				} else {

					return luaSourceRoot.getFileapi().getGlobalvars().get(field.getElementName());
				}
			} catch (ModelException e) {
				Activator.logError("unable to get item from field " + field, e); //$NON-NLS-1$
				return null;
			}
		} else if (parent instanceof IField) {
			// we does not manage inline type.
			Activator.logWarning("inline type is not managed by LuaASTModelUtils.getItem(IField) : unable to find item for " + field, null); //$NON-NLS-1$
			return null;
		}
		return null;
	}

	/**
	 * Get Item from IMethod <br/>
	 * 
	 * DLTK Model => AST
	 */
	public static Item getItem(IMethod method) {
		IModelElement parent = method.getParent();
		if (parent instanceof IType) {
			RecordTypeDef typeDef = (RecordTypeDef) getTypeDef((IType) parent);
			return typeDef.getFields().get(method.getElementName());
		} else if (parent instanceof ISourceModule) {
			LuaSourceRoot luaSourceRoot = getLuaSourceRoot((ISourceModule) parent);
			try {
				if (Flags.isPrivate(method.getFlags())) {
					List<LocalVar> localVars = luaSourceRoot.getInternalContent().getContent().getLocalVars();
					for (LocalVar localVar : localVars) {
						if (localVar.getVar().getName().equals(method.getElementName()))
							return localVar.getVar();
					}
				} else {

					return luaSourceRoot.getFileapi().getGlobalvars().get(method.getElementName());
				}
			} catch (ModelException e) {
				Activator.logError("unable to get item from method " + method, e); //$NON-NLS-1$
				return null;
			}
		} else if (parent instanceof IField) {
			// we does not manage inline type.
			Activator.logWarning("inline type is not managed by LuaASTModelUtils.getItem(IMethod) : unable to find item for " + method, null); //$NON-NLS-1$
			return null;
		}
		return null;
	}

	/**
	 * Get IType from RecordTypeDef <br/>
	 * 
	 * AST => DLTK Model
	 */
	public static IType getIType(ISourceModule module, RecordTypeDef recordtypeDef) {
		if (LuaASTUtils.isInlineTypeDef(recordtypeDef)) {
			// we does not manage inline type.
			Activator.logWarning("inline type is not managed by LuaASTModelUtils.getIType() : unable to find IType for " + recordtypeDef, null); //$NON-NLS-1$
			return null;
		} else {
			IType type = module.getType(recordtypeDef.getName());
			return type;
		}
	}

	/**
	 * Get IMembers from Item <br/>
	 * 
	 * AST => DLTK Model
	 */
	public static List<IMember> getIMembers(ISourceModule sourceModule, Item item) {
		ArrayList<IMember> results = new ArrayList<IMember>();
		LuaASTNode parent = item.getParent();
		// we don't no manage inline type we create fake model element for now ... :/
		if (LuaASTUtils.isTypeField(item) && !LuaASTUtils.isInlineTypeField(item)) {
			// support record field
			IType iType = getIType(sourceModule, (RecordTypeDef) parent);
			if (iType != null) {
				try {
					for (IModelElement child : iType.getChildren()) {
						if (child.getElementName().equals(item.getName()) && child instanceof IMember) {
							results.add((IMember) child);
							return results;
						}
					}
				} catch (ModelException e) {
					Activator.logWarning("unable to get IMember corresponding to the given item " + item, e); //$NON-NLS-1$
				}
			}
		} else if (LuaASTUtils.isLocalVariable(item) || LuaASTUtils.isInlineTypeField(item) || LuaASTUtils.isGlobalVariable(item)) {
			// TODO retrieve local var which are in the model (so the local var in the first block)
			// support local variable
			// ------------------------------------------------------------------------------------

			// get modifier
			LuaSourceRoot luaSourceRoot = LuaASTModelUtils.getLuaSourceRoot(sourceModule);
			int modifier = 0;
			if (LuaASTUtils.isModule(luaSourceRoot.getFileapi(), item))
				modifier |= Flags.AccModule;
			else if (LuaASTUtils.isPublic(item))
				modifier |= Flags.AccPublic;
			else if (LuaASTUtils.isPrivate(item))
				modifier |= Flags.AccPrivate;

			// manage method
			// TODO should we resolved this localy ?
			// TypeDef resolvedtype = LuaASTUtils.resolveTypeLocaly(sourceModule, item);
			TypeResolution resolvedtype = LuaASTUtils.resolveType(sourceModule, item.getType());
			if (resolvedtype != null && resolvedtype.getTypeDef() instanceof FunctionTypeDef) {
				FunctionTypeDef functionResolvedType = (FunctionTypeDef) resolvedtype.getTypeDef();
				String[] parametersName = new String[functionResolvedType.getParameters().size()];
				for (int i = 0; i < parametersName.length; i++) {
					parametersName[i] = functionResolvedType.getParameters().get(i).getName();
				}
				results.add(new FakeMethod(sourceModule, item.getName(), item.sourceStart(), item.getName().length(), parametersName, modifier, item));
				return results;
			}

			// get type
			String type = null;
			if (item.getType() instanceof PrimitiveTypeRef || item.getType() instanceof InternalTypeRef || item.getType() instanceof ExternalTypeRef)
				type = item.getType().toReadableString();

			TypeResolution typeResolution = LuaASTUtils.resolveType(sourceModule, item.getType());
			if (typeResolution != null && typeResolution.getTypeDef() instanceof RecordTypeDef) {
				modifier |= Flags.AccInterface;
				results.add(new FakeField(sourceModule, item.getName(), type, item.sourceStart(), item.getName().length(), modifier, item));

				// check if members is callable
				TypeResolution resolvedCallType = LuaASTUtils.resolveType(typeResolution.getModule(),
						((RecordTypeDef) typeResolution.getTypeDef()).getCallTypeRef());
				if (resolvedCallType != null && resolvedCallType.getTypeDef() instanceof FunctionTypeDef) {
					// if yes create another member corresponding to this item
					FunctionTypeDef functionResolvedType = (FunctionTypeDef) resolvedCallType.getTypeDef();
					if (functionResolvedType.getParameters().size() > 1) {
						String[] parametersName = new String[functionResolvedType.getParameters().size() - 1];
						// we must ignore the first parameter.
						for (int i = 0; i < parametersName.length; i++) {
							parametersName[i] = functionResolvedType.getParameters().get(i + 1).getName();
						}
						results.add(new FakeMethod(resolvedCallType.getModule(), item.getName(), item.sourceStart(), item.getName().length(),
								parametersName, modifier, functionResolvedType));
					} else {
						results.add(new FakeMethod(resolvedCallType.getModule(), item.getName(), item.sourceStart(), item.getName().length(),
								new String[0], modifier, functionResolvedType));
					}
				}
				return results;
			} else {
				results.add(new FakeField(sourceModule, item.getName(), type, item.sourceStart(), item.getName().length(), modifier, item));
				return results;
			}
		} else if (LuaASTUtils.isUnresolvedGlobal(item)) {
			results.add(new FakeField(sourceModule, item.getName(), null, item.sourceStart(), item.getName().length(), Declaration.AccGlobal, item));
			return results;
		}
		return null;
	}

	/**
	 * Get IMember from Item <br/>
	 * 
	 * AST => DLTK Model
	 */
	public static IMember getIMember(ISourceModule sourceModule, Item item) {
		List<IMember> iMembers = getIMembers(sourceModule, item);
		if (iMembers != null && iMembers.size() > 0)
			return iMembers.get(0);
		return null;
	}

	/**
	 * Get IModelElement from ASTNode <br/>
	 * 
	 * AST => DLTK Model
	 */
	public static IModelElement getIModelElement(ISourceModule sourcemodule, LuaASTNode astNode) {
		if (astNode instanceof RecordTypeDef) {
			return getIType(sourcemodule, (RecordTypeDef) astNode);
		} else if (astNode instanceof Item) {
			return getIMember(sourcemodule, (Item) astNode);
		}
		return null;
	}
}
