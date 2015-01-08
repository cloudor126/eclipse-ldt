/*******************************************************************************
 * Copyright (c) 2015 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.ldt.ui.wizards.pages;

import java.util.List;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ldt.core.internal.LuaLanguageToolkit;
import org.eclipse.ldt.core.internal.PreferenceInitializer;
import org.eclipse.ldt.core.internal.grammar.LuaGrammarManager;
import org.eclipse.ldt.ui.internal.grammar.GrammarContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;

public class GrammarGroup {

	private ComboViewer availableGrammarComboViewer;
	private ISelection selection;

	public GrammarGroup(final Composite parent) {
		// Create group
		final Group group = new Group(parent, SWT.NONE);
		group.setText(Messages.GrammarGroup_group_name);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(group);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(group);

		// Grammar combo viewer
		availableGrammarComboViewer = new ComboViewer(group, SWT.READ_ONLY | SWT.BORDER);
		availableGrammarComboViewer.setContentProvider(new GrammarContentProvider());
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.BEGINNING).grab(true, false).applyTo(availableGrammarComboViewer.getControl());

		initializeGroup();
	}

	public String getSelectedGrammar() {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				if (availableGrammarComboViewer != null) {
					selection = availableGrammarComboViewer.getSelection();
				} else {
					selection = null;
				}
			}
		});

		if (selection instanceof IStructuredSelection) {
			Object firstElement = ((IStructuredSelection) selection).getFirstElement();
			if (firstElement instanceof String)
				return (String) firstElement;
		}

		return null;
	}

	private void initializeGroup() {
		if (availableGrammarComboViewer == null || availableGrammarComboViewer.getControl().isDisposed())
			return;

		// Refresh list
		List<String> availableGrammars = LuaGrammarManager.getAvailableGrammars();
		availableGrammarComboViewer.setInput(availableGrammars);

		// Set default interpreter
		String defaultGrammar = InstanceScope.INSTANCE.getNode(LuaLanguageToolkit.getDefault().getPreferenceQualifier()).get(
				PreferenceInitializer.GRAMMAR_DEFAULT_ID, null);
		availableGrammarComboViewer.setSelection(new StructuredSelection(defaultGrammar));
	}
}
