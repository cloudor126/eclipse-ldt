/*******************************************************************************
 * Copyright (c) 2014 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.ldt.core.internal.grammar;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ldt.core.grammar.IGrammar;
import org.eclipse.ldt.core.grammar.ILuaSourceValidator;

public final class LuaGrammarManager {

	private static final String EXTENSION_POINT_ID = "org.eclipse.ldt.luaGrammar"; //$NON-NLS-1$
	private static final String ATTRIBUTE_NAME = "name"; //$NON-NLS-1$
	private static final String ATTRIBUTE_VALIDATOR = "validator"; //$NON-NLS-1$

	private LuaGrammarManager() {
	}

	private static IConfigurationElement getGrammarContribution(String name) throws CoreException {
		// search plug-in contribution
		IConfigurationElement[] contributions = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT_ID);
		for (int i = 0; i < contributions.length; i++) {
			String nameAttribute = contributions[i].getAttribute(ATTRIBUTE_NAME);

			if (name != null && name.equals(nameAttribute)) {
				return contributions[i];
			}
		}
		return null;
	}

	private static IGrammar getGrammarFromContribution(final String name) throws CoreException {
		// search plug-in contribution
		IConfigurationElement grammarContribution = getGrammarContribution(name);
		if (grammarContribution != null) {

			final ILuaSourceValidator validator = (ILuaSourceValidator) grammarContribution.createExecutableExtension(ATTRIBUTE_VALIDATOR);

			return new IGrammar() {

				@Override
				public ILuaSourceValidator getValidator() {
					return validator;
				}

				@Override
				public String getName() {
					return name;
				}
			};
		}
		return null;
	}

	public static IGrammar getAvailableGrammar(String name) throws CoreException {
		return getGrammarFromContribution(name);
	}
}
