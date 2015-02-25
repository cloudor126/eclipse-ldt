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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ldt.core.buildpath.LuaExecutionEnvironment;
import org.eclipse.ldt.core.grammar.IGrammar;
import org.eclipse.ldt.core.grammar.ILuaSourceValidator;
import org.eclipse.ldt.core.internal.Activator;
import org.eclipse.ldt.core.internal.LuaLanguageToolkit;
import org.eclipse.ldt.core.internal.PreferenceInitializer;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public final class LuaGrammarManager {

	private static final String EXTENSION_POINT_ID = "org.eclipse.ldt.luaGrammar"; //$NON-NLS-1$
	private static final String ATTRIBUTE_NAME = "name"; //$NON-NLS-1$
	private static final String ATTRIBUTE_VALIDATOR = "validator"; //$NON-NLS-1$
	private static final String ATTRIBUTE_KEYWORDS = "keywords"; //$NON-NLS-1$
	private static final String KEYWORDS_SEPARATOR = ","; //$NON-NLS-1$

	private LuaGrammarManager() {
	}

	private static IConfigurationElement getGrammarContribution(String name) throws CoreException {
		if (name == null)
			return null;

		// search plug-in contribution
		IConfigurationElement[] contributions = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT_ID);
		for (int i = 0; i < contributions.length; i++) {
			String nameAttribute = contributions[i].getAttribute(ATTRIBUTE_NAME);

			if (name.equals(nameAttribute)) {
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

			final String keywordsAttribute = grammarContribution.getAttribute(ATTRIBUTE_KEYWORDS);
			final List<String> keywords = new ArrayList<String>();
			if (keywordsAttribute != null) {
				for (String word : keywordsAttribute.split(KEYWORDS_SEPARATOR)) {
					// TODO validate the keyword ? no special char, ...
					String cleanWord = word.trim();
					if (!cleanWord.isEmpty())
						keywords.add(cleanWord);
				}
			}

			return new IGrammar() {

				@Override
				public ILuaSourceValidator getValidator() {
					return validator;
				}

				@Override
				public String getName() {
					return name;
				}

				@Override
				public List<String> getKeywords() {
					return keywords;
				}
			};
		}
		return null;
	}

	public static IGrammar getAvailableGrammar(String name) throws CoreException {
		return getGrammarFromContribution(name);
	}

	public static List<String> getAvailableGrammars() {
		List<String> grammars = new ArrayList<String>();

		// search plug-in contribution
		IConfigurationElement[] contributions = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT_ID);
		for (int i = 0; i < contributions.length; i++) {
			String nameAttribute = contributions[i].getAttribute(ATTRIBUTE_NAME);

			if (nameAttribute != null && !nameAttribute.isEmpty()) {
				grammars.add(nameAttribute);
			}
		}

		return grammars;
	}

	private static IGrammar getGrammarWithLua51GrammarFallback(String name) {
		// check the grammar is available
		try {
			IGrammar defaultGrammar = getAvailableGrammar(name);
			if (defaultGrammar != null)
				return defaultGrammar;
		} catch (CoreException e) {
			String message = String.format("The default grammar %s is not available.", name);//$NON-NLS-1$
			Activator.logWarning(message, e);
		}

		// use grammar 5.1 by default
		try {
			return getAvailableGrammar("lua-5.1"); //$NON-NLS-1$
		} catch (CoreException e) {
			String message = "The lua 5.1 grammar must be available. Check if you have the org.eclise.ldt.support.lua51 installed!"; //$NON-NLS-1$
			Activator.logError(message, e);
			throw new RuntimeException(message, e);
		}
	}

	public static IGrammar getDefaultGrammar() {
		// get in preference the default grammar
		ScopedPreferenceStore preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, LuaLanguageToolkit.getDefault()
				.getPreferenceQualifier());
		String defaultGrammarID = preferenceStore.getString(PreferenceInitializer.GRAMMAR_DEFAULT_ID);

		return getGrammarWithLua51GrammarFallback(defaultGrammarID);
	}

	public static IGrammar getDefaultGrammarFor(IProject project) {
		// Create context
		IScopeContext[] context;
		if (project != null)
			context = new IScopeContext[] { new ProjectScope(project), InstanceScope.INSTANCE };
		else
			context = new IScopeContext[] { InstanceScope.INSTANCE };

		// Get grammarName
		String grammarName = Platform.getPreferencesService().getString(LuaLanguageToolkit.getDefault().getPreferenceQualifier(),
				PreferenceInitializer.GRAMMAR_DEFAULT_ID, PreferenceInitializer.GRAMMAR_DEFAULT_ID_VALUE, context);

		// Get grammar
		return getGrammarWithLua51GrammarFallback(grammarName);
	}

	public static IGrammar getDefaultGrammarFor(LuaExecutionEnvironment ee) {
		// if ee has no grammar defined use the default one
		if (ee == null || ee.getLuaGrammar() == null)
			return getDefaultGrammar();

		// check the grammar is available
		try {
			IGrammar availableGrammar = getAvailableGrammar(ee.getLuaGrammar());
			if (availableGrammar != null)
				return availableGrammar;
		} catch (CoreException e) {
			String message = String.format(
					"The default grammar %s for the execution environment %s is not available.", ee.getLuaGrammar(), ee.getEEIdentifier());//$NON-NLS-1$
			Activator.logWarning(message, e);
			// use default grammar instead...
		}
		return getDefaultGrammar();
	}
}
