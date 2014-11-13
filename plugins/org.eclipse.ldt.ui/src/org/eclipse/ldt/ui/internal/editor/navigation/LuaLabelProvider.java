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
package org.eclipse.ldt.ui.internal.editor.navigation;

import org.eclipse.dltk.core.IField;
import org.eclipse.dltk.core.IMember;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.ui.ScriptElementLabels;
import org.eclipse.dltk.ui.viewsupport.AppearanceAwareLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.ldt.core.internal.ast.models.LuaDLTKModelUtils;
import org.eclipse.ldt.ui.internal.Activator;
import org.eclipse.ldt.ui.internal.ImageConstants;
import org.eclipse.swt.graphics.Image;

public class LuaLabelProvider extends LabelProvider implements IStyledLabelProvider {

	@Override
	public String getText(final Object element) {
		StyledString styledText = getStyledText(element);
		if (styledText != null)
			return styledText.toString();
		return null;
	}

	@Override
	public Image getImage(final Object element) {
		final IMember member = element instanceof IMember ? (IMember) element : null;
		if (member == null)
			return null;
		try {
			// Special icon for private type
			if (member.exists()) {
				if (LuaDLTKModelUtils.isModule(member)) {
					return Activator.getDefault().getImageRegistry().get(ImageConstants.MODULE_OBJ16);
				} else if (LuaDLTKModelUtils.isType(member)) {
					return Activator.getDefault().getImageRegistry().get(ImageConstants.TYPE_OBJ16);
				} else if (LuaDLTKModelUtils.isModuleFunction(member)) {
					return Activator.getDefault().getImageRegistry().get(ImageConstants.MODULE_FUNCTION_OBJ16);
				} else if (LuaDLTKModelUtils.isModuleField(member)) {
					return Activator.getDefault().getImageRegistry().get(ImageConstants.MODULE_FIELD_OBJ16);
				} else if (LuaDLTKModelUtils.isModuleTable(member)) {
					return Activator.getDefault().getImageRegistry().get(ImageConstants.MODULE_TABLE_OBJ16);
				} else if (LuaDLTKModelUtils.isPrivateFunction(member)) {
					return Activator.getDefault().getImageRegistry().get(ImageConstants.PRIVATE_FUNCTION_OBJ16);
				} else if (LuaDLTKModelUtils.isPrivateField(member)) {
					return Activator.getDefault().getImageRegistry().get(ImageConstants.PRIVATE_FIELD_OBJ16);
				} else if (LuaDLTKModelUtils.isPrivateTable(member)) {
					return Activator.getDefault().getImageRegistry().get(ImageConstants.PRIVATE_TABLE_OBJ16);
				} else if (LuaDLTKModelUtils.isPublicFunction(member)) {
					return Activator.getDefault().getImageRegistry().get(ImageConstants.PUBLIC_FUNCTION_OBJ16);
				} else if (LuaDLTKModelUtils.isPublicField(member)) {
					return Activator.getDefault().getImageRegistry().get(ImageConstants.PUBLIC_FIELD_OBJ16);
				} else if (LuaDLTKModelUtils.isPublicTable(member)) {
					return Activator.getDefault().getImageRegistry().get(ImageConstants.PUBLIC_TABLE_OBJ16);
				}
			}
		} catch (ModelException e) {
			Activator.logError(Messages.LuaCompletionProvidersFlags, e);
		}
		// DLTK default behavior
		return null;
	}

	@Override
	public StyledString getStyledText(Object element) {
		if (element instanceof IModelElement) {// get name
			StyledString result = new StyledString();
			StringBuffer buf = new StringBuffer(61);
			ScriptElementLabels.getDefault().getElementLabel((IModelElement) element,
					AppearanceAwareLabelProvider.DEFAULT_TEXTFLAGS | ScriptElementLabels.ALL_CATEGORY | ScriptElementLabels.M_APP_RETURNTYPE, buf);
			result.append(buf.toString());

			// get field type
			if (element instanceof IField) {
				try {
					if (((IField) element).getType() != null)
						result.append(new StyledString(" : " + ((IField) element).getType(), StyledString.DECORATIONS_STYLER)); //$NON-NLS-1$
					// CHECKSTYLE:OFF
				} catch (ModelException e) {
					// do nothing, we just not be able to get the type
					// CHECKSTYLE:ON
				}
			}
			return result;
		}
		return null;
	}
}
