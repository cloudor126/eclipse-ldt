/*******************************************************************************
 * Copyright (c) 2009, 2012 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.ldt.ui.internal.views;

import java.io.Reader;

import org.eclipse.dltk.core.IMember;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.internal.ui.text.HTMLPrinter;
import org.eclipse.dltk.ui.documentation.ScriptDocumentationAccess;
import org.eclipse.dltk.ui.infoviews.AbstractDocumentationView;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ldt.core.internal.LuaLanguageToolkit;
import org.eclipse.ldt.ui.internal.Activator;
import org.eclipse.ldt.ui.internal.LuaDocumentationHelper;
import org.eclipse.swt.graphics.Color;

/**
 * The view which show the documentation of a selected ModelElement
 */
// TODO avoid to access to internal class (open a bug ?)
@SuppressWarnings("restriction")
public class LuaDocView extends AbstractDocumentationView {

	public LuaDocView() {
	}

	@Override
	protected IPreferenceStore getPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	@Override
	protected String getNature() {
		return LuaLanguageToolkit.getDefault().getNatureId();
	}

	@Override
	protected void setBackground(Color color) {
		// Hack to change control color (because we can not override inititalizeColors()
		super.setBackground(LuaDocumentationHelper.getBackgroundColor());
	}

	@Override
	protected void setForeground(Color color) {
		// Hack to change control color (because we can not override inititalizeColors()
		super.setForeground(LuaDocumentationHelper.getForegroundColor());
	}

	/**
	 * @see org.eclipse.dltk.ui.infoviews.AbstractDocumentationView#computeInput(java.lang.Object)
	 */
	@Override
	protected Object computeInput(Object input) {
		if (getControl() != null) {
			if (input instanceof IModelElement) {
				final IModelElement model = (IModelElement) input;
				// get the html documentation
				String scriptdocHtml = getScriptdocHtml(model);
				if (scriptdocHtml == null)
					// generate default documentation if there no documentation attached
					scriptdocHtml = getDefaultDocumentation(model);
				return scriptdocHtml;
			} else {
				return null;
			}
		}
		return null;
	}

	/**
	 * @return the lua doc in HTML format for a given model Element or null if no documentation is attached
	 */
	private String getScriptdocHtml(IModelElement modelelement) {

		Reader reader = ScriptDocumentationAccess.getHTMLContentReader(getNature(), modelelement, true, true);
		if (reader != null) {
			return LuaDocumentationHelper.generatePage(HTMLPrinter.read(reader));
		}
		return null;
	}

	/**
	 * generate default documentation for a IModelElement
	 */
	private String getDefaultDocumentation(IModelElement modelElement) {
		if (modelElement instanceof ISourceModule || modelElement instanceof IMember) {
			return LuaDocumentationHelper.generatePage(Messages.LuaDocView_NoDocumentationFound);
		} else {
			return null;
		}
	}
}
