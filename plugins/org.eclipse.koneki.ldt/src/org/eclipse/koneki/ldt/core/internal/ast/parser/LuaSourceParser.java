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
package org.eclipse.koneki.ldt.core.internal.ast.parser;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.dltk.ast.ASTNode;
import org.eclipse.dltk.ast.parser.AbstractSourceParser;
import org.eclipse.dltk.ast.parser.IModuleDeclaration;
import org.eclipse.dltk.compiler.env.IModuleSource;
import org.eclipse.dltk.compiler.problem.DefaultProblem;
import org.eclipse.dltk.compiler.problem.IProblemReporter;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.ElementChangedEvent;
import org.eclipse.dltk.core.IElementChangedListener;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IModelElementDelta;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.koneki.ldt.core.LuaUtils;
import org.eclipse.koneki.ldt.core.internal.Activator;
import org.eclipse.koneki.ldt.core.internal.ast.models.LuaDLTKModelUtils;
import org.eclipse.koneki.ldt.core.internal.ast.models.api.LuaFileAPI;
import org.eclipse.koneki.ldt.core.internal.ast.models.common.LuaSourceRoot;
import org.eclipse.koneki.ldt.core.internal.ast.models.file.LuaInternalContent;
import org.eclipse.osgi.util.NLS;

/**
 * Generates AST from Metalua analysis, {@link ASTNode}s are created straight from Lua
 * 
 * @author Kevin KIN-FOO <kkinfoo@sierrawireless.com>
 */
public class LuaSourceParser extends AbstractSourceParser {

	private static ModelsBuilderLuaModule astBuilder = new ModelsBuilderLuaModule();

	// BEGIN CACHE MANAGEMENT
	// TODO DLTK has already a cache system but it can be used to keep the last valid AST.
	// so we have to duplicate the cache system.
	// Ideally, the parser should manage file with syntax errors..
	private static Map<IModelElement, IModuleDeclaration> cache = new Hashtable<IModelElement, IModuleDeclaration>();
	private static IElementChangedListener changedListener = new IElementChangedListener() {
		public void elementChanged(ElementChangedEvent event) {
			synchronized (LuaSourceParser.class) {
				IModelElementDelta delta = event.getDelta();
				processDelta(delta);
			}
		}

		private void processDelta(IModelElementDelta delta) {
			IModelElement element = delta.getElement();
			if (element.getElementType() == IModelElement.SOURCE_MODULE) {
				if (delta.getKind() == IModelElementDelta.REMOVED) {
					cache.remove(element);
				} else if (delta.getKind() == IModelElementDelta.CHANGED && delta.getFlags() == IModelElementDelta.F_PRIMARY_WORKING_COPY) {
					cache.remove(element);
				}
			}
			if (delta.getFlags() == IModelElementDelta.F_REMOVED_FROM_BUILDPATH) {
				if (delta.getAffectedChildren().length == 0) {
					for (IModelElement sourcemodule : new ArrayList<IModelElement>(cache.keySet())) {
						if (LuaDLTKModelUtils.isAncestor(sourcemodule, element)) {
							cache.remove(sourcemodule);
						}
					}
				}
			}
			if ((delta.getFlags() & IModelElementDelta.F_CHILDREN) != 0) {
				IModelElementDelta[] affectedChildren = delta.getAffectedChildren();
				for (int i = 0; i < affectedChildren.length; i++) {
					IModelElementDelta child = affectedChildren[i];
					processDelta(child);
				}
			}
		}
	};
	static {
		DLTKCore.addElementChangedListener(changedListener);
	}

	// END CACHE MANAGEMENT

	public LuaSourceParser() {
	}

	/**
	 * Generate DLTK AST straight from Lua
	 * 
	 * @param input
	 *            Source to parse
	 * @param reporter
	 *            Enable to report errors in parsed source code
	 */
	@Override
	public IModuleDeclaration parse(IModuleSource input, IProblemReporter reporter) {
		final String source = input.getSourceContents();
		final String moduleName = LuaUtils.getModuleFullName(input);
		LuaSourceRoot module = new LuaSourceRoot(source.length());
		final OffsetFixer fixer = new OffsetFixer(source);

		synchronized (LuaSourceParser.class) {
			try {
				// Build AST
				module = astBuilder.buildAST(source, moduleName);

				// Fix AST
				if (module != null)
					module.traverse(new EncodingVisitor(fixer));
			}
			// CHECKSTYLE:OFF
			catch (final Exception e) {
				// CHECKSTYLE:ON
				Activator.logWarning(NLS.bind("Unable to parse file {0}.", input.getFileName()), e); //$NON-NLS-1$
				// the module is probably on error.
				if (module == null)
					module = new LuaSourceRoot(source.length());
				module.setProblem(1, 1, 0, 0, "This file probably contains a syntax error."); //$NON-NLS-1$
			}

			// Deal with errors on Lua side
			if (module != null) {
				// if module contains a syntax error
				if (module.hasError()) {
					// add error to reporter
					final DefaultProblem problem = module.getProblem();
					problem.setOriginatingFileName(input.getFileName());
					reporter.reportProblem(problem);

					// -- TODO ECLIPSE 411238
					// -- we must calculate offset because DLTK does not support 'line' positionning
					if (problem.getSourceEnd() < 0) {
						try {
							final int line = problem.getSourceLineNumber();
							final Document document = new Document(source);
							int endLineOffset = document.getLineOffset(line) + document.getLineLength(line) - 1;
							problem.setSourceStart(fixer.getCharacterPosition(problem.getSourceStart()));
							problem.setSourceEnd(endLineOffset);
						} catch (BadLocationException e) {
							Activator.logWarning("Unable to retrive error offset", e); //$NON-NLS-1$
						}
					} else {
						// Handle encoding shifts
						problem.setSourceStart(fixer.getCharacterPosition(problem.getSourceStart()));
						problem.setSourceEnd(fixer.getCharacterPosition(problem.getSourceEnd()));
					}

					// use AST in cache, we don't have a "well built" module (module with a fileapi and an internalcontent)
					if (input.getModelElement() != null) {
						if (module.getFileapi() == null || module.getInternalContent() == null) {
							final LuaSourceRoot cached = (LuaSourceRoot) cache.get(input.getModelElement());
							if (cached != null) {
								cached.setError(true);
								return cached;
							} else {
								module.setLuaFileApi(new LuaFileAPI());
								module.setInternalContent(new LuaInternalContent());
							}
						} else {
							cache.put(input.getModelElement(), module);
						}
					}
				} else if (input.getModelElement() != null) {
					// if there are no error, put the new AST in cache
					cache.put(input.getModelElement(), module);
				}
			}
		}
		return module;
	}
}
