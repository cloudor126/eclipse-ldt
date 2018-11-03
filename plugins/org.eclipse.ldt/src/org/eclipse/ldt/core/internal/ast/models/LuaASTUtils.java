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
package org.eclipse.ldt.core.internal.ast.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dltk.ast.ASTNode;
import org.eclipse.dltk.ast.ASTVisitor;
import org.eclipse.dltk.core.IScriptFolder;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.ldt.core.IProjectSourceVisitor2;
import org.eclipse.ldt.core.LuaUtils;
import org.eclipse.ldt.core.LuaUtils.ProjectFragmentFilter;
import org.eclipse.ldt.core.internal.Activator;
import org.eclipse.ldt.core.internal.LuaLanguageToolkit;
import org.eclipse.ldt.core.internal.PreferenceInitializer;
import org.eclipse.ldt.core.internal.ast.models.api.ExprTypeRef;
import org.eclipse.ldt.core.internal.ast.models.api.ExternalTypeRef;
import org.eclipse.ldt.core.internal.ast.models.api.FunctionTypeDef;
import org.eclipse.ldt.core.internal.ast.models.api.InlineTypeRef;
import org.eclipse.ldt.core.internal.ast.models.api.InternalTypeRef;
import org.eclipse.ldt.core.internal.ast.models.api.Item;
import org.eclipse.ldt.core.internal.ast.models.api.KeyExprTypeRef;
import org.eclipse.ldt.core.internal.ast.models.api.LuaFileAPI;
import org.eclipse.ldt.core.internal.ast.models.api.MetaTypeRef;
import org.eclipse.ldt.core.internal.ast.models.api.ModuleTypeRef;
import org.eclipse.ldt.core.internal.ast.models.api.PrimitiveTypeRef;
import org.eclipse.ldt.core.internal.ast.models.api.RecordTypeDef;
import org.eclipse.ldt.core.internal.ast.models.api.Return;
import org.eclipse.ldt.core.internal.ast.models.api.TypeDef;
import org.eclipse.ldt.core.internal.ast.models.api.TypeRef;
import org.eclipse.ldt.core.internal.ast.models.api.UnknownItem;
import org.eclipse.ldt.core.internal.ast.models.api.ValueExprTypeRef;
import org.eclipse.ldt.core.internal.ast.models.common.LuaASTNode;
import org.eclipse.ldt.core.internal.ast.models.common.LuaSourceRoot;
import org.eclipse.ldt.core.internal.ast.models.file.Block;
import org.eclipse.ldt.core.internal.ast.models.file.Call;
import org.eclipse.ldt.core.internal.ast.models.file.Identifier;
import org.eclipse.ldt.core.internal.ast.models.file.Index;
import org.eclipse.ldt.core.internal.ast.models.file.Invoke;
import org.eclipse.ldt.core.internal.ast.models.file.LocalVar;
import org.eclipse.ldt.core.internal.ast.models.file.LuaExpression;
import org.eclipse.ldt.core.internal.ast.models.file.LuaInternalContent;

public final class LuaASTUtils {
	private LuaASTUtils() {
	}

	private static class ClosestItemVisitor extends ASTVisitor {
		private Item result = null;
		private int position;

		private String identifierName;

		public ClosestItemVisitor(int position, String identifierName) {
			super();
			this.position = position;
			this.identifierName = identifierName;
		}

		public Item getResult() {
			return result;
		}

		@Override
		public boolean visit(ASTNode node) throws Exception {
			if (node instanceof LocalVar)
				return false;

			// we go down util we found the closer block.
			if (node instanceof Block) {
				if (node.sourceStart() <= position && position <= node.sourceEnd()) {
					return true;
				}
				return false;
			}
			return false;
		}

		@Override
		public boolean endvisit(ASTNode node) throws Exception {
			if (result == null && node instanceof Block) {
				// we go up only on the parent block
				List<LocalVar> localVars = ((Block) node).getLocalVars();
				for (LocalVar localVar : localVars) {
					Item item = localVar.getVar();
					if (item.getName().equals(identifierName)) {
						result = item;
					}
				}
				return true;
			}
			return false;
		}
	};

	public static Item getClosestLocalVar(final LuaSourceRoot luaSourceRoot, final String identifierName, final int position) {
		// traverse the root block on the file with this visitor
		try {
			ClosestItemVisitor closestItemVisitor = new ClosestItemVisitor(position, identifierName);
			luaSourceRoot.getInternalContent().getContent().traverse(closestItemVisitor);
			return closestItemVisitor.getResult();
			// CHECKSTYLE:OFF
		} catch (Exception e) {
			// CHECKSTYLE:ON
			Activator.logError("unable to collect local var", e); //$NON-NLS-1$
		}
		return null;
	}

	public static TypeResolution resolveType(ISourceModule sourceModule, TypeRef typeRef) {
		if (typeRef instanceof PrimitiveTypeRef) {
			return resolveType(sourceModule, (PrimitiveTypeRef) typeRef);
		}

		if (typeRef instanceof InternalTypeRef) {
			return resolveType(sourceModule, (InternalTypeRef) typeRef);
		}

		if (typeRef instanceof ExternalTypeRef) {
			return resolveType(sourceModule, (ExternalTypeRef) typeRef);
		}

		if (typeRef instanceof ModuleTypeRef) {
			return resolveType(sourceModule, (ModuleTypeRef) typeRef);
		}

		if (typeRef instanceof ExprTypeRef) {
			return resolveType(sourceModule, (ExprTypeRef) typeRef);
		}

		if (typeRef instanceof InlineTypeRef) {
			return resolveType(sourceModule, (InlineTypeRef) typeRef);
		}

		if (typeRef instanceof KeyExprTypeRef) {
			return resolveType(sourceModule, (KeyExprTypeRef) typeRef);
		}

		if (typeRef instanceof ValueExprTypeRef) {
			return resolveType(sourceModule, (ValueExprTypeRef) typeRef);
		}

		return null;
	}

	public static TypeResolution resolveType(ISourceModule sourceModule, PrimitiveTypeRef primitiveTypeRef) {
		// matching the primitive type #string to string#string if any
		if (primitiveTypeRef.getTypeName().equals("string")) { //$NON-NLS-1$
			ISourceModule stringSourceModule = LuaUtils.getSourceModule("string", sourceModule.getScriptProject()); //$NON-NLS-1$
			if (stringSourceModule == null)
				return null;

			LuaSourceRoot luaSourceRoot = LuaASTModelUtils.getLuaSourceRoot(stringSourceModule);
			TypeDef typeDef = luaSourceRoot.getFileapi().getTypes().get("string"); //$NON-NLS-1$
			return new TypeResolution(stringSourceModule, typeDef);
		}
		return null;

	}

	public static TypeResolution resolveType(ISourceModule sourceModule, InternalTypeRef internalTypeRef) {
		LuaSourceRoot luaSourceRoot = LuaASTModelUtils.getLuaSourceRoot(sourceModule);
		TypeDef typeDef = luaSourceRoot.getFileapi().getTypes().get(internalTypeRef.getTypeName());
		return new TypeResolution(sourceModule, typeDef);
	}

	public static TypeResolution resolveType(ISourceModule sourceModule, ExternalTypeRef externalTypeRef) {
		ISourceModule externalSourceModule = LuaUtils.getSourceModule(externalTypeRef.getModuleName(), sourceModule.getScriptProject());
		if (externalSourceModule == null)
			return null;
		LuaSourceRoot luaSourceRoot = LuaASTModelUtils.getLuaSourceRoot(externalSourceModule);
		TypeDef typeDef = luaSourceRoot.getFileapi().getTypes().get(externalTypeRef.getTypeName());
		return new TypeResolution(externalSourceModule, typeDef);
	}

	public static TypeResolution resolveType(ISourceModule sourceModule, ModuleTypeRef moduleTypeRef) {
		ISourceModule referencedSourceModule = LuaUtils.getSourceModule(moduleTypeRef.getModuleName(), sourceModule.getScriptProject());
		if (referencedSourceModule == null)
			return null;

		LuaSourceRoot luaSourceRoot = LuaASTModelUtils.getLuaSourceRoot(referencedSourceModule);
		LuaFileAPI fileapi = luaSourceRoot.getFileapi();
		if (fileapi != null) {
			ArrayList<Return> returns = fileapi.getReturns();
			if (returns.size() > 0) {
				Return returnValues = returns.get(0);
				if (returnValues.getTypes().size() > moduleTypeRef.getReturnPosition() - 1) {
					TypeRef typeRef = returnValues.getTypes().get(moduleTypeRef.getReturnPosition() - 1);
					return resolveType(referencedSourceModule, typeRef);
				}
			}
		}
		return null;
	}

	public static TypeResolution resolveType(ISourceModule sourceModule, ExprTypeRef exprTypeRef) {
		LuaExpression expression = exprTypeRef.getExpression();
		if (expression == null)
			return null;

		return resolveType(sourceModule, expression, exprTypeRef.getReturnPosition());
	}

	public static TypeResolution resolveType(ISourceModule sourceModule, KeyExprTypeRef exprTypeRef) {
		LuaExpression expression = exprTypeRef.getExpression();
		if (expression == null)
			return null;

		TypeResolution typeRef = resolveType(sourceModule, expression);
		if (typeRef != null && typeRef.getTypeDef() instanceof RecordTypeDef) {
			RecordTypeDef typeDef = (RecordTypeDef) typeRef.getTypeDef();
			if (typeDef.getDefaultkeytyperef() != null) {
				return resolveType(sourceModule, typeDef.getDefaultkeytyperef());
			}
		}
		return null;
	}

	public static TypeResolution resolveType(ISourceModule sourceModule, ValueExprTypeRef exprTypeRef) {
		LuaExpression expression = exprTypeRef.getExpression();
		if (expression == null)
			return null;

		TypeResolution typeRef = resolveType(sourceModule, expression);
		if (typeRef != null && typeRef.getTypeDef() instanceof RecordTypeDef) {
			RecordTypeDef typeDef = (RecordTypeDef) typeRef.getTypeDef();
			if (typeDef.getDefaultvaluetyperef() != null) {
				return resolveType(sourceModule, typeDef.getDefaultvaluetyperef());
			}
		}
		return null;
	}

	public static TypeResolution resolveType(ISourceModule sourceModule, LuaExpression expr) {
		return resolveType(sourceModule, expr, 1);
	}

	public static TypeResolution getDefaultIndexType(ISourceModule sourceModule, RecordTypeDef recordTypeDef) {
		return getDefaultIndexType(new TypeResolution(sourceModule, recordTypeDef), new HashSet<TypeResolution>());
	}

	public static TypeResolution getDefaultIndexType(TypeResolution recordTypeResolution) {
		return getDefaultIndexType(recordTypeResolution, new HashSet<TypeResolution>());
	}

	private static TypeResolution getDefaultIndexType(TypeResolution recordTypeResolution, Set<TypeResolution> cache) {
		if (recordTypeResolution != null && recordTypeResolution.getTypeDef() instanceof RecordTypeDef) {
			// search the default index
			RecordTypeDef recordTypeDef = (RecordTypeDef) recordTypeResolution.getTypeDef();
			TypeRef defaultIndex = recordTypeDef.getDefaultvaluetyperef();
			if (defaultIndex != null)
				return resolveType(recordTypeResolution.getModule(), defaultIndex);

			// if not found we search in hierarchy
			// manage super-type fields
			// cache is used to avoid cycle
			cache.add(recordTypeResolution);
			TypeRef supertype = recordTypeDef.getSupertype();
			if (supertype != null) {
				TypeResolution superTypeResolution = LuaASTUtils.resolveType(recordTypeResolution.getModule(), supertype);
				if (!cache.contains(superTypeResolution)) {
					return getDefaultIndexType(superTypeResolution, cache);
				}
			}
		}
		return null;
	}

	public static TypeResolution resolveType(ISourceModule sourceModule, LuaExpression expr, int returnposition) {
		if (expr instanceof Identifier) {
			Definition definition = getDefinition(sourceModule, expr);
			// resolve the type of the definition
			if (definition == null || definition.getItem() == null || definition.getItem().getType() == null)
				return null;
			return resolveType(definition.getModule(), definition.getItem().getType());
		} else if (expr instanceof Index) {
			Definition definition = getDefinition(sourceModule, expr);
			if (definition != null && definition.getItem() != null && definition.getItem().getType() != null) {
				// resolve the type of the definition
				return resolveType(definition.getModule(), definition.getItem().getType());
			}
			// if not found we search the defaultindex
			TypeResolution resolveType = resolveType(sourceModule, ((Index) expr).getLeft());
			return getDefaultIndexType(resolveType);
		} else if (expr instanceof Call) {
			Call call = ((Call) expr);
			LuaExpression functionExpr = call.getFunction();
			// resolve the function which is called
			TypeResolution resolvedType = resolveType(sourceModule, functionExpr);
			if (resolvedType != null) {
				if (resolvedType.getModule().getElementName().startsWith("global.") && (functionExpr instanceof Identifier)) { //$NON-NLS-1$
					Identifier funcId = (Identifier) functionExpr;
					if ("select".equals(funcId.getDefinition().getName()) && call.getArgList().size() >= 2) { //$NON-NLS-1$
						try {
							LuaExpression paramExpression = call.getArgList().get(1);
							return resolveType(sourceModule, paramExpression);
						} catch (Exception e) {
							return null;
						}
					}
				}
				// we must found a function type.
				FunctionTypeDef functiontype = null;
				if (resolvedType.getTypeDef() instanceof FunctionTypeDef) {
					// if the resolved type is function, we get it.
					functiontype = (FunctionTypeDef) resolvedType.getTypeDef();
				} else if (resolvedType.getTypeDef() instanceof RecordTypeDef) {
					// if the resolved type is a record(a table) we get it if it is "callable"
					TypeResolution type = resolveType(resolvedType.getModule(), ((RecordTypeDef) resolvedType.getTypeDef()).getCallTypeRef());
					if (type != null && type.getTypeDef() instanceof FunctionTypeDef) {
						functiontype = (FunctionTypeDef) type.getTypeDef();
					}
				}

				if (functiontype != null && functiontype.getReturns().size() > 0) {
					List<TypeRef> types = functiontype.getReturns().get(0).getTypes();
					if (types.size() >= returnposition) {
						TypeRef type = types.get(returnposition - 1);
						if (type instanceof MetaTypeRef) {
							MetaTypeRef metaTypeRef = (MetaTypeRef) type;
							try {
								String callStr = sourceModule.getSource().substring(expr.start(), expr.end());
								String[] params = callStr.substring(1, callStr.length() - 1).split(","); //$NON-NLS-1$
								return resolveType(sourceModule, metaTypeRef, params);
							} catch (Exception e) {
								return null;
							}
						}
						return resolveType(resolvedType.getModule(), type);
					}
				}
			}
			return null;
		} else if (expr instanceof Invoke) {
			Definition definition = getDefinition(sourceModule, expr);
			if (definition == null || definition.getItem() == null || definition.getItem().getType() == null)
				return null;

			TypeResolution resolvedFunctionType = resolveType(definition.getModule(), definition.getItem().getType());
			if (resolvedFunctionType != null && resolvedFunctionType.getTypeDef() instanceof FunctionTypeDef) {
				FunctionTypeDef functiontype = (FunctionTypeDef) resolvedFunctionType.getTypeDef();
				if (functiontype.getReturns().size() > 0) {
					List<TypeRef> types = functiontype.getReturns().get(0).getTypes();
					if (types.size() >= returnposition) {
						TypeRef type = types.get(returnposition - 1);
						if (type instanceof MetaTypeRef) {
							MetaTypeRef metaTypeRef = (MetaTypeRef) type;
							try {
								String callStr = sourceModule.getSource().substring(expr.start(), expr.end());
								callStr = callStr.substring(callStr.indexOf('('));
								String[] params = ("self," + callStr.substring(1, callStr.length() - 1)).split(","); //$NON-NLS-1$ //$NON-NLS-2$
								return resolveType(sourceModule, metaTypeRef, params);
							} catch (Exception e) {
								return null;
							}
						}
						return resolveType(resolvedFunctionType.getModule(), type);
					}
				}
			}
		}
		return null;
	}

	/**
	 * @param sourceModule
	 * @param metaTypeRef
	 * @param call
	 */
	public static TypeResolution resolveType(ISourceModule sourceModule, MetaTypeRef metaTypeRef, String[] params) {
		int index = metaTypeRef.getIndex();
		if (index > 0 && index <= params.length) {
			String param = params[index - 1].trim();
			if (param.matches("\".+\"") || param.matches("'.+'")) { //$NON-NLS-1$ //$NON-NLS-2$
				String typeSpec = param.substring(1, param.length() - 1);
				int sharpIndex = typeSpec.indexOf('#');
				if (sharpIndex == 0) {
					TypeResolution result = resolveType(sourceModule, new InternalTypeRef(typeSpec.substring(1)));
					if (result == null || result.getTypeDef() == null) {
						result = resolveType(sourceModule, new PrimitiveTypeRef(typeSpec.substring(1)));
					}
					return result;
				} else if (sharpIndex > 0) {
					return resolveType(sourceModule, new ExternalTypeRef(typeSpec.substring(0, sharpIndex), typeSpec.substring(sharpIndex + 1)));
				}
				return null;
			}
		}
		return null;
	}

	public static TypeResolution resolveType(ISourceModule sourceModule, InlineTypeRef inlineTypeRef) {
		TypeDef typeDef = inlineTypeRef.getDefinition();
		return new TypeResolution(sourceModule, typeDef);
	}

	public static class TypeResolution {
		private ISourceModule module;
		private TypeDef typeDef;

		public TypeResolution(ISourceModule module, TypeDef typeDef) {
			super();
			this.module = module;
			this.typeDef = typeDef;
		}

		public ISourceModule getModule() {
			return module;
		}

		public TypeDef getTypeDef() {
			return typeDef;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;

			if (obj == this)
				return true;

			if (this.getClass() != obj.getClass())
				return false;

			// check content
			TypeResolution typeResolution = (TypeResolution) obj;

			boolean moduleEquality;
			if (this.module == null && typeResolution.module == null)
				moduleEquality = true;
			else if (this.module != null && this.module.equals(typeResolution.module))
				moduleEquality = true;
			else
				return false;

			boolean typeDefEquality;
			if (this.typeDef == null && typeResolution.typeDef == null)
				typeDefEquality = true;
			else if (this.typeDef != null && this.typeDef.equals(typeResolution.typeDef))
				typeDefEquality = true;
			else
				return false;

			return typeDefEquality && moduleEquality;
		}

		@Override
		public int hashCode() {
			int hash = 1;
			hash = 17 * hash + (null == module ? 0 : module.hashCode());
			hash = 31 * hash + (null == typeDef ? 0 : typeDef.hashCode());
			return hash;
		}
	}

	public static Collection<Item> getLocalVars(LuaSourceRoot luaSourceRoot, final int offset, final String start) {
		// the localVars collected, indexed by var name;
		final Map<String, Item> collectedLocalVars = new HashMap<String, Item>();

		// the visitor which will collect local vars and store it in the map.
		ASTVisitor localvarCollector = new ASTVisitor() {
			@Override
			public boolean visit(ASTNode node) throws Exception {
				// we go down util we found the closer block.
				if (node instanceof Block) {
					if (node.sourceStart() <= offset && offset <= node.sourceEnd()) {
						return true;
					}
					return false;
				}
				return false;
			}

			@Override
			public boolean endvisit(ASTNode node) throws Exception {
				if (node instanceof Block) {
					// we go up only on all the parent block which
					List<LocalVar> localVars = ((Block) node).getLocalVars();
					for (LocalVar localVar : localVars) {
						Item item = localVar.getVar();
						if (!collectedLocalVars.containsKey(item.getName())
								&& (start == null || item.getName().toLowerCase().startsWith(start.toLowerCase()))) {
							collectedLocalVars.put(item.getName(), item);
						}
					}
					return true;
				}
				return false;
			}
		};

		// traverse the root block on the file with this visitor
		try {
			luaSourceRoot.getInternalContent().getContent().traverse(localvarCollector);
			// CHECKSTYLE:OFF
		} catch (Exception e) {
			// CHECKSTYLE:ON
			Activator.logError("unable to collect local var", e); //$NON-NLS-1$
		}

		return collectedLocalVars.values();
	}

	public static LuaExpression getLuaExpressionAt(LuaSourceRoot luaSourceRoot, final int startOffset, final int endOffset) {
		// traverse the root block on the file with this visitor
		try {
			MatchNodeVisitor matchNodeVisitor = new MatchNodeVisitor(startOffset, endOffset, LuaExpression.class);
			luaSourceRoot.getInternalContent().getContent().traverse(matchNodeVisitor);
			return (LuaExpression) matchNodeVisitor.getNode();

			// CHECKSTYLE:OFF
		} catch (Exception e) {
			// CHECKSTYLE:ON
			Activator.logError("unable to get expression at", e); //$NON-NLS-1$
		}
		return null;
	}

	public static class Definition {
		private ISourceModule module;
		private Item item;

		public Definition(ISourceModule module, Item item) {
			super();
			this.module = module;
			this.item = item;
		}

		public ISourceModule getModule() {
			return module;
		}

		public Item getItem() {
			return item;
		}

	}

	public static Definition getDefinition(ISourceModule sourceModule, RecordTypeDef recordTypeDef, String fieldname) {
		return getDefinition(new TypeResolution(sourceModule, recordTypeDef), fieldname, new HashSet<TypeResolution>());
	}

	private static Definition getDefinition(TypeResolution recordTypeResolution, String fieldname, Set<TypeResolution> cache) {
		if (recordTypeResolution != null && recordTypeResolution.getTypeDef() instanceof RecordTypeDef) {
			// search field with the given field name
			RecordTypeDef recordtypedef = (RecordTypeDef) recordTypeResolution.getTypeDef();
			Item item = recordtypedef.getFields().get(fieldname);
			if (item != null)
				return new Definition(recordTypeResolution.getModule(), item);

			// if not found we search in hierarchy
			// manage super-type fields
			// cache is used to avoid cycle
			cache.add(recordTypeResolution);
			TypeRef supertype = recordtypedef.getSupertype();
			if (supertype != null) {
				TypeResolution superTypeResolution = LuaASTUtils.resolveType(recordTypeResolution.getModule(), supertype);
				if (!cache.contains(superTypeResolution)) {
					return getDefinition(superTypeResolution, fieldname, cache);
				}
			}
		}
		return null;
	}

	public static Definition getDefinition(ISourceModule sourceModule, LuaExpression luaExpression) {
		if (luaExpression instanceof Identifier) {
			Identifier identifier = (Identifier) luaExpression;
			if (identifier.getDefinition() != null) {
				Item definition = identifier.getDefinition();
				if (definition instanceof UnknownItem) {
					// if we don't know the definition try to guess it
					final String identifiername = definition.getName();
					final LuaSourceRoot luaSourceRoot = LuaASTModelUtils.getLuaSourceRoot(sourceModule);

					// search local variable at first
					Item localvarItem = LuaASTUtils.getClosestLocalVar(luaSourceRoot, identifiername, identifier.start());
					if (localvarItem != null) {
						return new Definition(sourceModule, localvarItem);
					}
					// if not found, try to find global
					Definition globalVarDefinition = LuaASTUtils.getGlobalVarDefinition(sourceModule, identifiername);
					if (globalVarDefinition != null)
						return globalVarDefinition;

					return null;
				}
				if (LuaASTUtils.isUnresolvedGlobal(definition)) {
					// in this case we have a unknown global var definition.
					// we will try to resolved it
					Definition globalVarDefinition = getGlobalVarDefinition(sourceModule, definition.getName());
					if (globalVarDefinition != null)
						return globalVarDefinition;
				}
				return new Definition(sourceModule, definition);
			}
		} else if (luaExpression instanceof Index) {
			Index index = (Index) luaExpression;
			TypeResolution resolveType = LuaASTUtils.resolveType(sourceModule, index.getLeft());
			if (resolveType != null && resolveType.getTypeDef() instanceof RecordTypeDef) {
				RecordTypeDef typeDef = (RecordTypeDef) resolveType.getTypeDef();
				return getDefinition(resolveType.getModule(), typeDef, index.getRight());
			}

		} else if (luaExpression instanceof Invoke) {
			Invoke invoke = (Invoke) luaExpression;
			TypeResolution resolveType = LuaASTUtils.resolveType(sourceModule, invoke.getRecord());
			if (resolveType != null && resolveType.getTypeDef() instanceof RecordTypeDef) {
				RecordTypeDef typeDef = (RecordTypeDef) resolveType.getTypeDef();
				return getDefinition(resolveType.getModule(), typeDef, invoke.getFunctionName());
			}
		} else if (luaExpression instanceof Call) {
			Call call = (Call) luaExpression;
			Definition definition = getDefinition(sourceModule, call.getFunction());
			return definition;
		}
		return null;
	}

	public static List<Definition> getAllGlobalVarsDefinition(final ISourceModule sourceModule, final String start) {
		final List<Definition> definitions = new ArrayList<Definition>();

		// SEARCH IN PRELOADED SOURCE MODULE
		ISourceModule preloadedSourceModule = getPreloadSourceModule(sourceModule);
		if (preloadedSourceModule != null) {
			definitions.addAll(getAllInternalGlobalVarsDefinition(preloadedSourceModule, start));
		}

		if (Platform.getPreferencesService().getBoolean(LuaLanguageToolkit.getDefault().getPreferenceQualifier(),
				PreferenceInitializer.USE_GLOBAL_VAR_IN_LDT, true, null)) {

			// SEARCH IN CURRENT SOURCE MODULE
			definitions.addAll(getAllInternalGlobalVarsDefinition(sourceModule, start));

			// SEARCH IN EXTERNAL SOURCE MODULE
			definitions.addAll(getAllExternalGlobalVarsDefinition(sourceModule, start));
		}

		return definitions;
	}

	public static List<Definition> getAllInternalGlobalVarsDefinition(final ISourceModule sourceModule, final String start) {
		final List<Definition> definitions = new ArrayList<Definition>();

		// global vars defined in current module.
		LuaSourceRoot currentluaSourceRoot = LuaASTModelUtils.getLuaSourceRoot(sourceModule);
		if (currentluaSourceRoot != null) {
			// global var which start with 'start'
			for (Item globalvar : currentluaSourceRoot.getFileapi().getGlobalvars().values()) {
				if (start == null || start.isEmpty() || globalvar.getName().toLowerCase().startsWith(start.toLowerCase()))
					definitions.add(new Definition(sourceModule, globalvar));
			}
		}
		return definitions;
	}

	public static List<Definition> getAllExternalGlobalVarsDefinition(final ISourceModule originalSourceModule, final String start) {
		return getExternalGlobalVarsDefinition(originalSourceModule, start, false);
	}

	private static List<Definition> getExternalGlobalVarsDefinition(final ISourceModule originalSourceModule, final String start,
			final boolean exactName) {
		final List<Definition> definitions = new ArrayList<Definition>();

		IProjectSourceVisitor2 visitor = new IProjectSourceVisitor2() {

			@Override
			public void processFile(ISourceModule sourceModule, IPath absolutePath, IPath relativePath, String charset, IProgressMonitor monitor)
					throws CoreException {
				if (sourceModule != null && !sourceModule.equals(originalSourceModule)) {
					if (exactName) {
						Definition globalVarDef = getInternalGlobalVarDefinition(sourceModule, start);
						if (globalVarDef != null)
							definitions.add(globalVarDef);
					} else {
						definitions.addAll(getAllInternalGlobalVarsDefinition(sourceModule, start));
					}
				}

			}

			@Override
			public void processDirectory(IScriptFolder sourceModule, IPath absolutePath, IPath relativePath, IProgressMonitor monitor)
					throws CoreException {
				// nothing to do
			}
		};

		try {
			LuaUtils.visitSourceFiles(originalSourceModule.getScriptProject(), EnumSet.of(ProjectFragmentFilter.DEPENDENT_PROJECT), visitor,
					new NullProgressMonitor());
		} catch (CoreException e) {
			Activator.logError("Unable to get external global for auto-complete", e); //$NON-NLS-1$
		}
		return definitions;
	}

	public static Definition getGlobalVarDefinition(ISourceModule sourceModule, String varname) {
		// SEARCH IN PRELOADED SOURCE MODULE
		Definition definition = getGlobalVarDefinitionInPreloadedSourceModule(sourceModule, varname);
		if (definition != null)
			return definition;

		if (Platform.getPreferencesService().getBoolean(LuaLanguageToolkit.getDefault().getPreferenceQualifier(),
				PreferenceInitializer.USE_GLOBAL_VAR_IN_LDT, true, null)) {

			// SEARCH IN CURRENT SOURCE MODULE
			definition = getInternalGlobalVarDefinition(sourceModule, varname);
			if (definition != null)
				return definition;

			// SEARCH IN EXTERNAL SOURCE MODULE
			definition = getExternalGlobalVarDefinition(sourceModule, varname);
			if (definition != null)
				return definition;
		}

		return null;
	}

	public static Definition getGlobalVarDefinitionInPreloadedSourceModule(ISourceModule sourceModule, String varname) {
		// get preloaded module
		ISourceModule preloadedSourceModule = getPreloadSourceModule(sourceModule);
		if (preloadedSourceModule == null)
			return null;

		// get luasourceroot
		LuaSourceRoot preloadedLuaSourceRoot = LuaASTModelUtils.getLuaSourceRoot(preloadedSourceModule);
		if (preloadedLuaSourceRoot == null)
			return null;

		// get a global var with this name
		Item item = preloadedLuaSourceRoot.getFileapi().getGlobalvars().get(varname);
		if (item == null)
			return null;

		return new Definition(preloadedSourceModule, item);
	}

	public static Definition getInternalGlobalVarDefinition(ISourceModule sourceModule, String varname) {
		// get luasourceroot
		LuaSourceRoot luaSourceRoot = LuaASTModelUtils.getLuaSourceRoot(sourceModule);
		if (luaSourceRoot == null)
			return null;

		// get a global var with this name
		Item item = luaSourceRoot.getFileapi().getGlobalvars().get(varname);
		if (item == null)
			return null;

		return new Definition(sourceModule, item);
	}

	public static Definition getExternalGlobalVarDefinition(final ISourceModule originalSourceModule, final String start) {
		List<Definition> definitions = getExternalGlobalVarsDefinition(originalSourceModule, start, true);
		if (!definitions.isEmpty()) {
			return definitions.get(0);
		}
		return null;
	}

	public static ISourceModule getPreloadSourceModule(ISourceModule sourceModule) {
		if (sourceModule != null && sourceModule.getScriptProject() != null) {
			return LuaUtils.getSourceModule("global", sourceModule.getScriptProject()); //$NON-NLS-1$
		}
		return null;
	}

	public static boolean isModule(LuaFileAPI luaFileAPI, RecordTypeDef recordTypeDef) {
		TypeRef moduleReturnTypeRef = getModuleReturnType(luaFileAPI);

		if (!(moduleReturnTypeRef instanceof InternalTypeRef))
			return false;
		String typename = ((InternalTypeRef) moduleReturnTypeRef).getTypeName();

		return luaFileAPI.getTypes().get(typename) == recordTypeDef;
	}

	public static boolean isInlineTypeDef(TypeDef typedef) {
		return typedef != null && typedef.getParent() instanceof Item;
	}

	public static boolean isInlineTypeField(Item item) {
		return item.getParent() instanceof RecordTypeDef && isInlineTypeDef((TypeDef) item.getParent());
	}

	public static boolean isLocalVariable(Item item) {
		return item.getParent() instanceof Block;
	}

	public static boolean isGlobalVariable(Item item) {
		return item.getParent() instanceof LuaFileAPI;
	}

	public static boolean isUnresolvedGlobal(Item item) {
		return item.getParent() instanceof LuaInternalContent;
	}

	public static boolean isTypeField(Item item) {
		return item.getParent() instanceof RecordTypeDef;
	}

	public static boolean isModuleTypeField(LuaFileAPI luaFileAPI, Item item) {
		LuaASTNode parent = item.getParent();
		if (parent instanceof RecordTypeDef) {
			return isModule(luaFileAPI, (RecordTypeDef) parent);
		}
		return false;
	}

	// return true is the element is part of a module declaration
	// a module declaration is considered as public too
	public static boolean isModule(LuaFileAPI luaFileAPI, LuaASTNode node) {
		if (node instanceof Item) {
			Item item = (Item) node;
			if (isInlineTypeField(item)) {
				// should be always true as item is from inline Type
				if (item.getParent() instanceof RecordTypeDef) {
					LuaASTNode parent = ((RecordTypeDef) item.getParent()).getParent();
					return isModule(luaFileAPI, parent);
				}
			}
			return isModuleTypeField(luaFileAPI, item);
		} else if (node instanceof TypeDef) {
			TypeDef typedef = (TypeDef) node;
			if (isInlineTypeDef(typedef)) {
				return isModule(luaFileAPI, ((TypeDef) node).getParent());
			}
		}
		return false;
	}

	// return true if the element is part of a public declaration (global var or type declaration)
	// a module declaration is considered as public too
	public static boolean isPublic(LuaASTNode node) {
		if (node instanceof Item) {
			Item item = (Item) node;
			if (isInlineTypeField(item)) {
				// should be always true as item is from inline Type
				if (item.getParent() instanceof RecordTypeDef) {
					LuaASTNode parent = ((RecordTypeDef) item.getParent()).getParent();
					return isPublic(parent);
				}
			}
			return isGlobalVariable(item) || isTypeField(item);
		} else if (node instanceof TypeDef) {
			TypeDef typedef = (TypeDef) node;
			if (isInlineTypeDef(typedef)) {
				return isPublic(((TypeDef) node).getParent());
			}
		}
		return false;
	}

	// return true if the element is part of a private declaration (local variable)
	public static boolean isPrivate(LuaASTNode node) {
		if (node instanceof Item) {
			Item item = (Item) node;
			if (isInlineTypeField(item)) {
				// should be always true as item is from inline Type
				if (item.getParent() instanceof RecordTypeDef) {
					LuaASTNode parent = ((RecordTypeDef) item.getParent()).getParent();
					return isPrivate(parent);
				}
			}
			return isLocalVariable(item);
		} else if (node instanceof TypeDef) {
			TypeDef typedef = (TypeDef) node;
			if (isInlineTypeDef(typedef)) {
				return isPrivate(((TypeDef) node).getParent());
			}
		}
		return false;
	}

	public static TypeDef resolveTypeLocaly(ISourceModule sourceModule, Item item) {
		LuaSourceRoot luaSourceRoot = LuaASTModelUtils.getLuaSourceRoot(sourceModule);
		if (luaSourceRoot != null)
			return resolveTypeLocaly(luaSourceRoot.getFileapi(), item);
		return null;
	}

	public static TypeDef resolveTypeLocaly(LuaFileAPI luaFileAPI, Item item) {
		if (item == null)
			return null;

		TypeRef typeref = item.getType();
		if (typeref instanceof InternalTypeRef) {
			if (luaFileAPI == null)
				return null;

			InternalTypeRef internaltyperef = (InternalTypeRef) typeref;
			TypeDef typeDef = luaFileAPI.getTypes().get(internaltyperef.getTypeName());
			return typeDef;
		} else if (typeref instanceof InlineTypeRef) {
			InlineTypeRef inlinetyperef = (InlineTypeRef) typeref;
			TypeDef typeDef = inlinetyperef.getDefinition();
			return typeDef;
		}

		return null;
	}

	public static TypeRef getModuleReturnType(LuaFileAPI luaFileAPI) {
		ArrayList<Return> returns = luaFileAPI.getReturns();
		if (returns.isEmpty())
			return null;

		Return returnValues = returns.get(0);
		if (returnValues.getTypes().isEmpty())
			return null;

		return returnValues.getTypes().get(0);
	}
}
