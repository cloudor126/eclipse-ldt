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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ldt.core.internal.grammar.LuaGrammarManager;
import org.eclipse.ldt.ui.internal.grammar.GrammarContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

public class GrammarGroup {
	private ComboViewer availableGrammarComboViewer;
	private Button customGrammarRadio;
	private Button defaultGrammarRadio;
	private String grammar;

	private SelectionListener grammarChoiceListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(final SelectionEvent e) {
			availableGrammarComboViewer.getCombo().setEnabled(customGrammarRadio.getSelection());
			defaultGrammarLabel.setEnabled(defaultGrammarRadio.getSelection());
		}
	};
	private Label defaultGrammarLabel;

	public GrammarGroup(final Composite parent) {
		// Create group
		final Group group = new Group(parent, SWT.NONE);
		group.setText(Messages.GrammarGroup_group_name);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(group);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(group);

		// Grammar combo viewer
		defaultGrammarRadio = new Button(group, SWT.RADIO);
		defaultGrammarRadio.setText(Messages.GrammarGroup_defaultgrammar);
		defaultGrammarRadio.addSelectionListener(grammarChoiceListener);
		defaultGrammarLabel = new Label(group, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(defaultGrammarLabel);

		customGrammarRadio = new Button(group, SWT.RADIO);
		customGrammarRadio.setText(Messages.GrammarGroup_customgrammar);
		customGrammarRadio.addSelectionListener(grammarChoiceListener);

		availableGrammarComboViewer = new ComboViewer(group, SWT.READ_ONLY | SWT.BORDER);
		availableGrammarComboViewer.setContentProvider(new GrammarContentProvider());
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false).applyTo(availableGrammarComboViewer.getControl());

		initializeGroup();
	}

	public String getSelectedGrammar() {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				grammar = null;
				if (customGrammarRadio != null && availableGrammarComboViewer != null && defaultGrammarLabel != null) {
					if (customGrammarRadio.getSelection()) {
						ISelection selection = availableGrammarComboViewer.getSelection();
						if (selection instanceof IStructuredSelection) {
							Object firstElement = ((IStructuredSelection) selection).getFirstElement();
							if (firstElement instanceof String)
								grammar = (String) firstElement;
						}
					} else {
						grammar = defaultGrammarLabel.getText();
					}
				}
			}
		});

		return grammar;
	}

	private void initializeGroup() {
		if (availableGrammarComboViewer == null || availableGrammarComboViewer.getControl().isDisposed())
			return;

		if (defaultGrammarRadio == null || defaultGrammarRadio.isDisposed())
			return;

		// Refresh list
		List<String> availableGrammars = LuaGrammarManager.getAvailableGrammars();
		availableGrammarComboViewer.setInput(availableGrammars);
		availableGrammarComboViewer.setSelection(new StructuredSelection(LuaGrammarManager.getDefaultGrammar().getName()));

		defaultGrammarRadio.setSelection(true);
		availableGrammarComboViewer.getControl().setEnabled(false);
	}

	public void setDefaultGrammar(final String defaultGrammar) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				defaultGrammarLabel.setText(defaultGrammar);
			}
		});
	}
}
