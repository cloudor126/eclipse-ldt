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
package org.eclipse.ldt.ui.internal.editor.templates;

import org.eclipse.dltk.ui.templates.ScriptTemplateAccess;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.ldt.core.LuaNature;
import org.eclipse.ldt.ui.internal.Activator;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;

/**
 * 
 * @author Kevin KIN-FOO <kkin-foo@sierrawireless.com>
 * 
 */
public class LuaTemplateAccess extends ScriptTemplateAccess {
	private static final String CUSTOM_TEMPLATES_KEY = LuaNature.ID + ".Templates"; //$NON-NLS-1$
	private static LuaTemplateAccess instance;

	public static LuaTemplateAccess getInstance() {
		if (instance == null) {
			instance = new LuaTemplateAccess();
		}

		return instance;
	}

	@Override
	protected String getContextTypeId() {
		return LuaUniversalTemplateContextType.CONTEXT_TYPE_ID;
	}

	@Override
	protected String getCustomTemplatesKey() {
		return CUSTOM_TEMPLATES_KEY;
	}

	@Override
	protected ContextTypeRegistry createContextTypeRegistry() {
		final ContributionContextTypeRegistry registry = (ContributionContextTypeRegistry) super.createContextTypeRegistry();
		registry.addContextType(LuaDocumentorTemplateContextType.CONTEXT_TYPE_ID);
		return registry;
	}

	@Override
	protected IPreferenceStore getPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

}
