/*******************************************************************************
 * Copyright (c) 2013 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.ldt.debug.ui.internal;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementEditor;

@SuppressWarnings("restriction")
public class LuaDebugElementAdapterFactory implements IAdapterFactory {

	private IElementEditor editor = new LuaVariableEditor();

	@Override
	public Object getAdapter(Object adaptableObject, @SuppressWarnings("rawtypes") Class adapterType) {
		if (adapterType.equals(IElementEditor.class)) {
			if (adaptableObject instanceof IVariable) {
				return editor;
			}
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class[] { IElementEditor.class };
	}
}
