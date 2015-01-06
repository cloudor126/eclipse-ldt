/*******************************************************************************
 * Copyright (c) 2009, 2012 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 ******************************************************************************/
package org.eclipse.ldt.ui.internal.preferences;

import java.util.List;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.ldt.core.internal.LuaLanguageToolkit;
import org.eclipse.ldt.core.internal.PreferenceInitializer;
import org.eclipse.ldt.core.internal.grammar.LuaGrammarManager;
import org.eclipse.ldt.ui.internal.grammar.GrammarContentProvider;
import org.eclipse.ldt.ui.internal.grammar.GrammarLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class LuaGrammarPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private CheckboxTreeViewer eeTreeViewer;

	protected String getHelpId() {
		return null;
	}

	@Override
	public void init(IWorkbench workbench) {
		setDescription("Select the default Lua grammar.");
		setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, LuaLanguageToolkit.getDefault().getPreferenceQualifier()));
	}

	/**
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		// ----------------
		// CREATE CONTROL
		// create container composite
		Composite containerComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(2).applyTo(containerComposite);

		eeTreeViewer = new CheckboxTreeViewer(containerComposite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		eeTreeViewer.setContentProvider(new GrammarContentProvider());
		eeTreeViewer.setLabelProvider(new GrammarLabelProvider());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(eeTreeViewer.getControl());

		// add a listener to allow only one default EE
		eeTreeViewer.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				String grammar = (String) event.getElement();
				if (event.getChecked()) {
					// allow to check only one element of the table
					eeTreeViewer.setCheckedElements(new Object[] { grammar });
					getPreferenceStore().setValue(PreferenceInitializer.GRAMMAR_DEFAULT_ID, grammar);
				} else {
					// removing the default ee from pref
					getPreferenceStore().setValue(PreferenceInitializer.GRAMMAR_DEFAULT_ID, "none"); //$NON-NLS-1$
				}
				validateGrammar();
			}
		});

		// ----------------
		// Initialize UI
		initializePage();
		return containerComposite;
	}

	private void initializePage() {
		if (eeTreeViewer == null || eeTreeViewer.getControl().isDisposed())
			return;

		// Refresh list
		List<String> availableGrammars = LuaGrammarManager.getAvailableGrammars();
		eeTreeViewer.setInput(availableGrammars);

		// Set default interpreter
		String defaultGrammar = getPreferenceStore().getString(PreferenceInitializer.GRAMMAR_DEFAULT_ID);
		for (String grammar : availableGrammars) {
			eeTreeViewer.setChecked(grammar, grammar.equals(defaultGrammar));
		}
	}

	public void validateGrammar() {
		if (eeTreeViewer.getCheckedElements().length == 0) {
			setMessage("No default Grammar.", WARNING);
			return;
		}
		setMessage(null);
	}
}