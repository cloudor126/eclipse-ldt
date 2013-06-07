/*******************************************************************************
 * Copyright (c) 2012, 2013 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.koneki.ldt.ui.internal.editor.text;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.dltk.ast.ASTNode;
import org.eclipse.dltk.ast.ASTVisitor;
import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
import org.eclipse.dltk.ast.parser.IModuleDeclaration;
import org.eclipse.dltk.compiler.env.IModuleSource;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.core.SourceParserUtil;
import org.eclipse.dltk.ui.editor.highlighting.AbortSemanticHighlightingException;
import org.eclipse.dltk.ui.editor.highlighting.ISemanticHighlighter;
import org.eclipse.dltk.ui.editor.highlighting.ISemanticHighlighterExtension;
import org.eclipse.dltk.ui.editor.highlighting.ISemanticHighlightingRequestor;
import org.eclipse.dltk.ui.editor.highlighting.SemanticHighlighting;
import org.eclipse.koneki.ldt.core.LuaNature;
import org.eclipse.koneki.ldt.core.internal.ast.models.LuaASTUtils;
import org.eclipse.koneki.ldt.core.internal.ast.models.api.Item;
import org.eclipse.koneki.ldt.core.internal.ast.models.api.LuaFileAPI;
import org.eclipse.koneki.ldt.core.internal.ast.models.common.LuaSourceRoot;
import org.eclipse.koneki.ldt.core.internal.ast.models.file.Identifier;
import org.eclipse.koneki.ldt.ui.internal.Activator;

public class LuaSemanticUpdateWorker extends ASTVisitor implements ISemanticHighlighter, ISemanticHighlighterExtension {

	private static final String HL_LOCAL_VARIABLE = ILuaColorConstants.LUA_LOCAL_VARIABLE;
	private static final String HL_GLOBAL_VARIABLE = ILuaColorConstants.LUA_GLOBAL_VARIABLE;

	private ISemanticHighlightingRequestor requestor;

	public SemanticHighlighting[] getSemanticHighlightings() {
		return new SemanticHighlighting[] { new LuaSemanticHighlighting(ILuaColorConstants.LUA_LOCAL_VARIABLE, null),
				new LuaSemanticHighlighting(ILuaColorConstants.LUA_GLOBAL_VARIABLE, null) };
	}

	public boolean visitGeneral(ASTNode node) throws Exception {
		if (node instanceof LuaSourceRoot) {
			// no semantic highlighting if the code is in error.
			return !((LuaSourceRoot) node).hasError();
		}
		if (node instanceof LuaFileAPI) {
			// no need to try to do semantic highlighting for APIs...
			return false;
		}
		if (node instanceof Identifier) {
			// TODO BUG ECLIPSE 381703 every node should have a definition
			final Item item = ((Identifier) node).getDefinition();
			if (item != null) {
				if (LuaASTUtils.isLocal(item)) {
					requestor.addPosition(node.sourceStart(), node.sourceEnd(), HL_LOCAL_VARIABLE);
				} else if (LuaASTUtils.isUnresolvedGlobal(item) || LuaASTUtils.isGlobal(item)) {
					requestor.addPosition(node.sourceStart(), node.sourceEnd(), HL_GLOBAL_VARIABLE);
				}
			} else {
				final String message = "{0} starting at offset {1} with length {2} has no definition."; //$NON-NLS-1$
				final String formattedMessage = MessageFormat.format(message, node.getClass().getName(), node.matchStart(), node.matchLength());
				Activator.logWarning(formattedMessage);
			}
		}
		return true;
	}

	public String[] getHighlightingKeys() {
		final Set<String> result = new HashSet<String>();
		for (SemanticHighlighting highlighting : getSemanticHighlightings()) {
			result.add(highlighting.getPreferenceKey());
		}
		return result.toArray(new String[result.size()]);
	}

	public void process(IModuleSource code, ISemanticHighlightingRequestor req) {
		this.requestor = req;
		try {
			((ModuleDeclaration) parseCode(code)).traverse(this);
		} catch (ModelException e) {
			throw new AbortSemanticHighlightingException();
			// CHECKSTYLE:OFF
		} catch (Exception e) {
			// CHECKSTYLE:ON
			throw new AbortSemanticHighlightingException();
		}
	}

	/**
	 * @param code
	 * @return
	 * @throws ModelException
	 */
	protected IModuleDeclaration parseCode(IModuleSource code) throws ModelException {
		if (code instanceof ISourceModule) {
			return parseSourceModule((ISourceModule) code);
		} else {
			return parseSourceCode(code);
		}
	}

	private IModuleDeclaration parseSourceCode(IModuleSource code) {
		return SourceParserUtil.parse(code, LuaNature.ID, null);
	}

	private IModuleDeclaration parseSourceModule(final ISourceModule sourceModule) {
		return SourceParserUtil.parse(sourceModule, null);
	}

}
