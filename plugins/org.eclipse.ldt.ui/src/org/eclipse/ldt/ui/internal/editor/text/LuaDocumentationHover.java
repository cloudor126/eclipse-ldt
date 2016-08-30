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
package org.eclipse.ldt.ui.internal.editor.text;

import java.io.IOException;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.dltk.annotations.Internal;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.internal.ui.text.hover.DocumentationHover;
import org.eclipse.dltk.ui.ScriptElementImageProvider;
import org.eclipse.dltk.ui.ScriptElementLabels;
import org.eclipse.dltk.ui.documentation.IDocumentationResponse;
import org.eclipse.dltk.ui.documentation.IScriptDocumentationTitleAdapter;
import org.eclipse.dltk.ui.documentation.ScriptDocumentationAccess;
import org.eclipse.dltk.ui.text.completion.HTMLPrinter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.ldt.ui.internal.LuaDocumentationHelper;

@SuppressWarnings("restriction")
public class LuaDocumentationHover extends DocumentationHover {

	private static final long LABEL_FLAGS = ScriptElementLabels.ALL_FULLY_QUALIFIED | ScriptElementLabels.M_APP_RETURNTYPE
			| ScriptElementLabels.F_APP_TYPE_SIGNATURE | ScriptElementLabels.M_PARAMETER_TYPES | ScriptElementLabels.M_PARAMETER_NAMES
			| ScriptElementLabels.M_EXCEPTIONS | ScriptElementLabels.F_PRE_TYPE_SIGNATURE | ScriptElementLabels.M_PRE_TYPE_PARAMETERS
			| ScriptElementLabels.T_TYPE_PARAMETERS | ScriptElementLabels.USE_RESOLVED;
	private static final long LOCAL_VARIABLE_FLAGS = LABEL_FLAGS & ~ScriptElementLabels.F_FULLY_QUALIFIED | ScriptElementLabels.F_POST_QUALIFIED;

	private static final ScriptDocumentationTitleAdapter titleAdapter = new ScriptDocumentationTitleAdapter();

	@Internal
	static class ScriptDocumentationTitleAdapter extends PlatformObject implements IScriptDocumentationTitleAdapter {

		private ScriptElementImageProvider fImageProvider;

		public String getTitle(Object element) {
			if (element instanceof IModelElement) {
				IModelElement member = (IModelElement) element;
				long flags = member.getElementType() == IModelElement.LOCAL_VARIABLE ? LOCAL_VARIABLE_FLAGS : LABEL_FLAGS;
				String label = ScriptElementLabels.getDefault().getElementLabel(member, flags);
				return label;
			} else {
				return null;
			}
		}

		public ImageDescriptor getImage(Object element) {
			if (element instanceof IModelElement) {
				final IModelElement modelElement = (IModelElement) element;
				if (fImageProvider == null) {
					fImageProvider = new ScriptElementImageProvider();
				}
				return fImageProvider.getScriptImageDescriptor(modelElement,
						ScriptElementImageProvider.OVERLAY_ICONS | ScriptElementImageProvider.SMALL_ICONS);
			}
			return null;
		}
	}

	private IInformationControlCreator fHoverControlCreator;

	@Override
	protected String getHoverInfo(String nature, Object[] result) {
		String htmlContent = null;

		// no result
		int nResults = result.length;
		if (nResults == 0)
			return null;

		if (nResults > 0) {
			// handle only the first result
			Object element = result[0];

			// try to get documentation
			IDocumentationResponse response = ScriptDocumentationAccess.getDocumentation(nature, element, titleAdapter);
			if (response != null) {
				try {
					htmlContent = HTMLPrinter.read(response.getReader());
				} catch (IOException e) {
					return null;
				}
			}

			// if no documentation, don't display any tooltip
			if (htmlContent == null || htmlContent.isEmpty()) {
				// TODO BUG_ECLIPSE 399414 and/or 399468
				// Because DLTK have a default tooltip really hard to remove, the only way to
				// don't have a tooltip is to kill the thread responsible to display the hover by throwing an exception.
				// This could be cleaner to raise a RuntimeException but pratically throwing ArrayIndexOutOfBounds avoid unneeded log because,
				// this is nicely handle here : org.eclipse.jface.text.TextViewerHoverManager.computeInformation()
				throw new ArrayIndexOutOfBoundsException("Exception to avoid to create a tooltip, currently a workaround for dltk"); //$NON-NLS-1$
				// return null;
			}
			return LuaDocumentationHelper.generatePage(htmlContent);
		}

		return null;
	}

	@Override
	public IInformationControlCreator getHoverControlCreator() {
		if (fHoverControlCreator == null)
			fHoverControlCreator = new LuaHoverControlCreator(getInformationPresenterControlCreator());
		return fHoverControlCreator;
	}

}
