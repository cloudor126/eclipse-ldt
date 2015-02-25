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
package org.eclipse.ldt.ui.wizards.pages;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ldt.core.buildpath.LuaExecutionEnvironment;
import org.eclipse.ldt.core.grammar.IGrammar;
import org.eclipse.ldt.core.internal.grammar.LuaGrammarManager;
import org.eclipse.ldt.ui.internal.Activator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * @since 1.3
 */
public class ConvertToLuaProjectMainPage extends WizardPage {

	private boolean isKonekiMigration = false;
	private LuaExecutionEnvironmentGroup luaExecutionEnvironmentGroup;
	private GrammarGroup grammarGroup;

	public ConvertToLuaProjectMainPage(String pageName, IProject project) {
		super(pageName);
		try {
			isKonekiMigration = project.hasNature("org.eclipse.koneki.ldt.nature"); //$NON-NLS-1$
		} catch (CoreException e) {
			Activator.log(e.getStatus());
		}
		setTitle(NLS.bind(Messages.ConvertToLuaProjectMainPage_title, project.getName()));

		if (isKonekiMigration) {
			setMessage(Messages.ConvertToLuaProjectMainPage_migrationMessage, IMessageProvider.WARNING);
		} else {
			setMessage(Messages.ConvertToLuaProjectMainPage_defaultMessage);
		}

	}

	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		// Create container
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());
		GridLayoutFactory.swtDefaults().applyTo(composite);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(composite);

		// Manage Koneki project
		if (isKonekiMigration) {
			Link link = new Link(composite, WARNING);
			link.setText(Messages.ConvertToLuaProjectMainPage_linkToMigrationPage);
			link.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					try {
						PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(event.text));
					} catch (PartInitException e) {
						Activator.logWarning("Unable to open migration koneki/ldt wiki page", e); //$NON-NLS-1$
					} catch (MalformedURLException e) {
						Activator.logWarning("Unable to open migration koneki/ldt wiki page", e); //$NON-NLS-1$
					}
				}
			});
		}

		// Create Lua execution environment group
		luaExecutionEnvironmentGroup = new LuaExecutionEnvironmentGroup(composite, false);
		luaExecutionEnvironmentGroup.addObserver(new Observer() {

			@Override
			public void update(Observable o, Object arg) {
				LuaExecutionEnvironment ee = luaExecutionEnvironmentGroup.getSelectedLuaExecutionEnvironment();
				grammarGroup.setDefaultGrammar(LuaGrammarManager.getDefaultGrammarFor(ee).getName());
			}
		});

		// Create Grammar group
		grammarGroup = new GrammarGroup(composite);
		IGrammar grammar = LuaGrammarManager.getDefaultGrammarFor(getLuaExecutionEnvironement());
		if (grammar != null)
			grammarGroup.setDefaultGrammar(grammar.getName());

		setControl(composite);
		Dialog.applyDialogFont(composite);
	}

	public LuaExecutionEnvironment getLuaExecutionEnvironement() {
		return luaExecutionEnvironmentGroup.getSelectedLuaExecutionEnvironment();
	}

	public String getGrammar() {
		return grammarGroup.getSelectedGrammar();
	}

	public boolean isKonekiMigration() {
		return isKonekiMigration;
	}
}
