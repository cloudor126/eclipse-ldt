/*******************************************************************************
 * Copyright (c) 2009, 2012 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *     Kevin KIN-FOO <kkinfoo@sierrawireless.com>
 *******************************************************************************/
package org.eclipse.koneki.ldt.ui.internal.editor.completion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.dltk.codeassist.ScriptCompletionEngine;
import org.eclipse.dltk.compiler.env.IModuleSource;
import org.eclipse.dltk.core.CompletionProposal;
import org.eclipse.dltk.core.IMember;
import org.eclipse.dltk.core.IMethod;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.jface.text.Document;
import org.eclipse.koneki.ldt.core.internal.LuaLanguageToolkit;
import org.eclipse.koneki.ldt.core.internal.PreferenceInitializer;
import org.eclipse.koneki.ldt.core.internal.ast.models.LuaASTModelUtils;
import org.eclipse.koneki.ldt.core.internal.ast.models.LuaASTUtils;
import org.eclipse.koneki.ldt.core.internal.ast.models.LuaASTUtils.Definition;
import org.eclipse.koneki.ldt.core.internal.ast.models.LuaASTUtils.TypeResolution;
import org.eclipse.koneki.ldt.core.internal.ast.models.api.FunctionTypeDef;
import org.eclipse.koneki.ldt.core.internal.ast.models.api.Item;
import org.eclipse.koneki.ldt.core.internal.ast.models.api.Parameter;
import org.eclipse.koneki.ldt.core.internal.ast.models.api.RecordTypeDef;
import org.eclipse.koneki.ldt.core.internal.ast.models.common.LuaSourceRoot;
import org.eclipse.koneki.ldt.core.internal.ast.models.file.Identifier;
import org.eclipse.koneki.ldt.core.internal.ast.models.file.Index;
import org.eclipse.koneki.ldt.core.internal.ast.models.file.Invoke;
import org.eclipse.koneki.ldt.core.internal.ast.models.file.LuaExpression;
import org.eclipse.koneki.ldt.ui.internal.Activator;
import org.eclipse.koneki.ldt.ui.internal.editor.text.LuaHeuristicScanner;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class LuaCompletionEngine extends ScriptCompletionEngine {

	@Override
	public void complete(IModuleSource module, int position, int k) {
		// extract source module
		final IModelElement modelElement = module.getModelElement();
		if (!(modelElement instanceof ISourceModule)) {
			Activator.logWarning("Unable to perform completion proposal. Module [" + module.getFileName() + "] has not source module associated."); //$NON-NLS-1$//$NON-NLS-2$
			return;
		}
		ISourceModule sourceModule = (ISourceModule) modelElement;
		String sourceContent = module.getSourceContents();

		// For now, we does not match case where there are white space before the cursor
		if (Character.isWhitespace(sourceContent.charAt(position - 1))) {
			// Search local declaration in AST
			addLocalDeclarations(sourceModule, "", position); //$NON-NLS-1$

			// Search global declaration in DLTK model
			addGlobalDeclarations(sourceModule, "", position); //$NON-NLS-1$

			// Add keywords
			addKeywords("", position); //$NON-NLS-1$
			return;
		}

		// try to find the incomplete expression before the cursor
		LuaHeuristicScanner luaHeuristicScanner = new LuaHeuristicScanner(new Document(module.getSourceContents()));
		LuaExpression luaExpression = luaHeuristicScanner.guessLuaExpression(position);
		if (luaExpression == null)
			return;

		requestor.beginReporting();
		if (luaExpression instanceof Identifier) {
			// manage incomplete Identifier
			// ----------------------------
			Item definition = ((Identifier) luaExpression).getDefinition();
			String start = definition.getName();

			// Search local declaration in AST
			addLocalDeclarations(sourceModule, start, position);

			// Search global declaration in DLTK model
			addGlobalDeclarations(sourceModule, start, position);

			// Add keywords
			addKeywords(start, position);
		} else if (luaExpression instanceof Index) {
			// manage incomplete Index
			// ----------------------------

			// resolve type of left part
			LuaExpression left = ((Index) luaExpression).getLeft();
			TypeResolution resolveType = LuaASTUtils.resolveType(sourceModule, left);

			// find all field we start by the right part
			String right = ((Index) luaExpression).getRight();
			addFields(resolveType, right, position);
		} else if (luaExpression instanceof Invoke) {
			// manage incomplete Invoke
			// ----------------------------

			// resolve type of record
			LuaExpression record = ((Invoke) luaExpression).getRecord();
			TypeResolution resolveType = LuaASTUtils.resolveType(sourceModule, record);

			// find all field we start by the right part
			String right = ((Invoke) luaExpression).getFunctionName();
			// we do not manage complete invoke
			if (luaExpression.isIncomplete())
				addInvocableFields(resolveType, right, position);
		}
		// we do not complete call for now
		requestor.endReporting();
	}

	private void addGlobalDeclarations(ISourceModule sourceModule, String start, int cursorPosition) {
		// get all global variable which start by the string "start"
		// from current module
		List<Definition> globalvars = new ArrayList<Definition>();

		// from global.lua of the EE
		ISourceModule preloadedSourceModule = LuaASTUtils.getPreloadSourceModule(sourceModule);
		if (preloadedSourceModule != null) {
			globalvars.addAll(LuaASTUtils.getAllInternalGlobalVarsDefinition(preloadedSourceModule, start));
		}

		// for each global var, get the corresponding model element and create the proposal
		for (Definition definition : globalvars) {
			IMember member = LuaASTModelUtils.getIMember(definition.getModule(), definition.getItem());
			if (member != null)
				createMemberProposal(member, cursorPosition - start.length(), cursorPosition);
		}

		// Add globals other that preloaded but with a lower relevance
		ScopedPreferenceStore preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, LuaLanguageToolkit.getDefault()
				.getPreferenceQualifier());

		if (preferenceStore.getBoolean(PreferenceInitializer.USE_GLOBAL_VAR_IN_LDT)) {

			List<Definition> othersglobalvars = LuaASTUtils.getAllInternalGlobalVarsDefinition(sourceModule, start);
			othersglobalvars.addAll(LuaASTUtils.getAllExternalGlobalVarsDefinition(sourceModule, start));

			// for each global var, get the corresponding model element and create the proposal
			for (Definition definition : othersglobalvars) {
				IMember member = LuaASTModelUtils.getIMember(definition.getModule(), definition.getItem());
				if (member != null)
					createMemberProposal(member, cursorPosition - start.length(), cursorPosition, false, 25);
			}
		}

	}

	private void addKeywords(String start, int cursorPosition) {
		// TODO key word should be define in a static attribute
		String[] keywords = new String[] { "and", "break", "do", "else", "elseif", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				"end", "false", "for", "function", "if",//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				"in", "local", "nil", "not", "or",//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				"repeat", "return", "then", "true", "until", "while" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

		// create proposal for each keyword
		for (int j = 0; j < keywords.length; j++) {
			if (start.isEmpty() || keywords[j].startsWith(start)) {
				createKeyWordProposal(keywords[j], cursorPosition - start.length(), cursorPosition);
			}
		}
	}

	private void addLocalDeclarations(ISourceModule sourceModule, String start, int cursorPosition) {
		// get lua source root
		LuaSourceRoot luaSourceRoot = LuaASTModelUtils.getLuaSourceRoot(sourceModule);
		if (luaSourceRoot == null)
			return;

		// find all local vars and create corresponding proposal
		Collection<Item> localVars = LuaASTUtils.getLocalVars(luaSourceRoot, cursorPosition - start.length(), start);
		for (Item var : localVars) {
			IMember member = LuaASTModelUtils.getIMember(sourceModule, var);
			if (member != null)
				createMemberProposal(member, cursorPosition - start.length(), cursorPosition);
		}

	}

	private void addFields(TypeResolution recordTypeResolution, String fieldName, int position) {
		if (recordTypeResolution == null)
			return;

		if (recordTypeResolution.getTypeDef() instanceof RecordTypeDef) {
			RecordTypeDef currentRecordTypeDef = (RecordTypeDef) recordTypeResolution.getTypeDef();
			ISourceModule currentSourceModule = recordTypeResolution.getModule();
			// get available field for the type found.
			for (Entry<String, Item> entry : currentRecordTypeDef.getFields().entrySet()) {
				Item item = entry.getValue();
				final boolean goodStart = item.getName().toLowerCase().startsWith(fieldName.toLowerCase());
				final boolean nostart = fieldName.isEmpty();
				if (goodStart || nostart) {
					createMemberProposal(LuaASTModelUtils.getIMember(currentSourceModule, item), position - fieldName.length(), position, false);
				}
			}
		}
	}

	private void addInvocableFields(TypeResolution recordTypeResolution, String fieldName, int position) {
		if (recordTypeResolution == null)
			return;

		if (recordTypeResolution.getTypeDef() instanceof RecordTypeDef) {
			RecordTypeDef currentRecordTypeDef = (RecordTypeDef) recordTypeResolution.getTypeDef();
			ISourceModule currentSourceModule = recordTypeResolution.getModule();
			// get available field for the type found.
			for (Entry<String, Item> entry : currentRecordTypeDef.getFields().entrySet()) {
				Item item = entry.getValue();
				final boolean goodStart = item.getName().toLowerCase().startsWith(fieldName.toLowerCase());
				final boolean nostart = fieldName.isEmpty();
				if (goodStart || nostart) {
					// MANAGE INVOCATION :
					// resolve field type
					final TypeResolution fieldTypeResolution = LuaASTUtils.resolveType(currentSourceModule, item.getType());

					// invocation works only on method (already tested in the other model ... the joy to have 2 models...)
					if (fieldTypeResolution == null || !(fieldTypeResolution.getTypeDef() instanceof FunctionTypeDef))
						continue;

					// invocation works only if there are at least one parameter
					final List<Parameter> parameters = ((FunctionTypeDef) fieldTypeResolution.getTypeDef()).getParameters();
					if (parameters.size() == 0)
						continue;

					// get first parameter
					Parameter firstParamter = parameters.get(0);

					// invocation is ok if :
					// first parameter is named self
					if ("self".equals(firstParamter.getName()) && firstParamter.getType() == null) //$NON-NLS-1$
						createMemberProposal(LuaASTModelUtils.getIMember(currentSourceModule, item), position - fieldName.length(), position, true);
					// or
					// if the first parameter is of the same type as the type on which it is invoked : it's ok !
					final TypeResolution parameterTypeResolution = LuaASTUtils.resolveType(currentSourceModule, firstParamter.getType());
					if (recordTypeResolution.equals(parameterTypeResolution))
						createMemberProposal(LuaASTModelUtils.getIMember(currentSourceModule, item), position - fieldName.length(), position, true);
				}
			}
		}
	}

	private void createKeyWordProposal(String keyword, int startIndex, int endIndex) {
		CompletionProposal proposal = CompletionProposal.create(CompletionProposal.KEYWORD, 0);
		proposal.setRelevance(1);
		proposal.setName(keyword);
		proposal.setCompletion(keyword);
		proposal.setReplaceRange(startIndex, endIndex);
		this.requestor.accept(proposal);
	}

	private void createMemberProposal(IMember member, int startIndex, int endIndex) {
		createMemberProposal(member, startIndex, endIndex, false);
	}

	private void createMemberProposal(IMember member, int startIndex, int endIndex, boolean invocation) {
		createMemberProposal(member, startIndex, endIndex, invocation, 50);
	}

	private void createMemberProposal(IMember member, int startIndex, int endIndex, boolean invocation, int relevance) {
		try {
			CompletionProposal proposal = null;
			if (member == null) {
				NullPointerException e = new NullPointerException("the given IMember is null"); //$NON-NLS-1$
				Activator.logWarning("Unable to perform completion", e); //$NON-NLS-1$
				return;
			}
			switch (member.getElementType()) {
			case IModelElement.METHOD:
				// create method proposal
				proposal = CompletionProposal.create(CompletionProposal.METHOD_REF, 0);
				IMethod method = (IMethod) member;

				if (invocation) {
					// manage the invoke case
					String[] parameterNames = method.getParameterNames();

					if (parameterNames.length == 0)
						return;

					String[] parameterNamesWithoutFirstOne = Arrays.copyOfRange(parameterNames, 1, parameterNames.length);
					proposal.setParameterNames(parameterNamesWithoutFirstOne);
				} else {
					proposal.setParameterNames(method.getParameterNames());
				}
				break;
			case IModelElement.FIELD:
				proposal = CompletionProposal.create(CompletionProposal.FIELD_REF, 0);
				break;
			case IModelElement.TYPE:
				proposal = CompletionProposal.create(CompletionProposal.TYPE_REF, 0);
				break;
			default:
				return;
			}

			proposal.setFlags(member.getFlags());
			proposal.setModelElement(member);
			proposal.setName(member.getElementName());
			proposal.setCompletion(member.getElementName());
			proposal.setReplaceRange(startIndex, endIndex);
			proposal.setRelevance(relevance);
			this.requestor.accept(proposal);

		} catch (ModelException e) {
			Activator.logWarning(Messages.LuaCompletionEngineProblemProcessingGlobals, e);
			return;
		}
	}
}
