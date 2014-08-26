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
package org.eclipse.ldt.ui.internal.buildpath;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IBuildpathEntry;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.internal.ui.wizards.IBuildpathContainerPage;
import org.eclipse.dltk.internal.ui.wizards.IBuildpathContainerPageExtension2;
import org.eclipse.dltk.ui.wizards.IBuildpathContainerPageExtension;
import org.eclipse.dltk.ui.wizards.NewElementWizardPage;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ldt.core.internal.buildpath.LuaExecutionEnvironment;
import org.eclipse.ldt.core.internal.buildpath.LuaExecutionEnvironmentBuildpathUtil;
import org.eclipse.ldt.core.internal.buildpath.LuaExecutionEnvironmentConstants;
import org.eclipse.ldt.ui.LuaExecutionEnvironmentUIManager;
import org.eclipse.ldt.ui.SWTUtil;
import org.eclipse.ldt.ui.internal.Activator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.PreferencesUtil;

@SuppressWarnings("restriction")
public class LuaExecutionEnvironmentWizardPage extends NewElementWizardPage implements IBuildpathContainerPage, IBuildpathContainerPageExtension,
		IBuildpathContainerPageExtension2 {

	private TreeViewer eeTreeViewer;
	private Button configureEE;
	private ArrayList<LuaExecutionEnvironment> currentEEs;

	public LuaExecutionEnvironmentWizardPage() {
		super("LuaExecutionEnvironmentWizardPage"); //$NON-NLS-1$
		setTitle(Messages.LuaExecutionEnvironmentWizardPageTitle);
		setDescription(Messages.LuaExecutionEnvironmentWizardPageDescription);
	}

	@Override
	public void createControl(final Composite parent) {
		initializeDialogUnits(parent);

		// Define a composite for list and label
		final Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);

		// Define Execution Environment list
		eeTreeViewer = new TreeViewer(composite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		eeTreeViewer.setContentProvider(new LuaExecutionEnvironmentContentProvider());
		eeTreeViewer.setLabelProvider(new DelegatingStyledCellLabelProvider(new LuaExecutionEnvironmentLabelProvider()));

		updateExecutionEnvironmentList();
		GridDataFactory.fillDefaults().grab(true, true).applyTo(eeTreeViewer.getControl());

		configureEE = new Button(composite, SWT.PUSH);
		configureEE.setText(Messages.LuaExecutionEnvironmentWizardPageConfigureButtonLabel);
		final int horizontalHint = SWTUtil.getButtonWidthHint(configureEE);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.BEGINNING).hint(horizontalHint, SWT.DEFAULT).applyTo(configureEE);

		init();

		addListeners();

		setPageComplete(validatePage());

		setControl(composite);
	}

	private void init() {
		updateExecutionEnvironmentList();
	}

	private void addListeners() {
		configureEE.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				widgetDefaultSelected(e);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				final String pageId = LuaExecutionEnvironmentConstants.PREFERENCE_PAGE_ID;
				PreferencesUtil.createPreferenceDialogOn(getShell(), pageId, new String[] { pageId }, null).open();
				updateExecutionEnvironmentList();
				setPageComplete(validatePage());
			}
		});

		eeTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				setPageComplete(validatePage());
			}
		});
	}

	@Override
	public IBuildpathEntry[] getNewContainers() {
		LuaExecutionEnvironment selectedExecutionEnvironment = getSelectedExecutionEnvironment();
		if (selectedExecutionEnvironment != null) {
			/*
			 * Generate BuildPathContainer from selected Execution Environment
			 */
			final IPath path = LuaExecutionEnvironmentBuildpathUtil.getLuaExecutionEnvironmentContainerPath(selectedExecutionEnvironment);
			final IBuildpathEntry buildpathContainerEntry = DLTKCore.newContainerEntry(path);
			return new IBuildpathEntry[] { buildpathContainerEntry };
		} else { // No path available or no selection
			return new IBuildpathEntry[0];
		}

	}

	@Override
	public boolean finish() {
		return true;
	}

	private void updateExecutionEnvironmentList() {
		if (eeTreeViewer == null) {
			return;
		}
		// get old input and old selection
		List<?> oldInput = null;
		if (eeTreeViewer.getInput() instanceof List<?>) {
			oldInput = (List<?>) eeTreeViewer.getInput();
		}

		// get new input
		final List<LuaExecutionEnvironment> newInput = LuaExecutionEnvironmentUIManager.getAvailableExecutionEnvironments();
		if (currentEEs != null)
			newInput.removeAll(currentEEs);

		eeTreeViewer.setInput(newInput);

		// try to guess the better new selection
		if (oldInput == null || oldInput.isEmpty()) {
			// no input before we try to select the first EE
			if (newInput != null && !newInput.isEmpty()) {
				eeTreeViewer.setSelection(new StructuredSelection(newInput.get(0)));
			}
		} else {
			if (!oldInput.equals(newInput)) {
				// select one of the new ExecutionEnvironment.
				for (LuaExecutionEnvironment ee : newInput) {
					if (!oldInput.contains(ee)) {
						eeTreeViewer.setSelection(new StructuredSelection(ee));
						break;
					}
				}
			}
		}
	}

	private LuaExecutionEnvironment getSelectedExecutionEnvironment() {
		if (eeTreeViewer == null) {
			return null;
		}

		final ISelection selection = eeTreeViewer.getSelection();
		if ((selection == null) || selection.isEmpty() || !(selection instanceof IStructuredSelection))
			return null;

		// Extract Execution Environment from selection
		return (LuaExecutionEnvironment) ((IStructuredSelection) selection).getFirstElement();
	}

	private boolean validatePage() {
		return getSelectedExecutionEnvironment() != null;
	}

	/**
	 * @see org.eclipse.dltk.ui.wizards.NewElementWizardPage#updateStatus(org.eclipse.core.runtime.IStatus)
	 */
	@Override
	protected void updateStatus(IStatus status) {
		// unplugged dltk mechanism
	}

	/**
	 * @see org.eclipse.dltk.internal.ui.wizards.IBuildpathContainerPage#getSelection()
	 */
	@Override
	public IBuildpathEntry getSelection() {
		// not used
		return null;
	}

	/**
	 * @see org.eclipse.dltk.internal.ui.wizards.IBuildpathContainerPage#setSelection(org.eclipse.dltk.core.IBuildpathEntry)
	 */
	@Override
	public void setSelection(IBuildpathEntry containerEntry) {
		// not used
	}

	/**
	 * @see org.eclipse.dltk.ui.wizards.IBuildpathContainerPageExtension#initialize(org.eclipse.dltk.core.IScriptProject,
	 *      org.eclipse.dltk.core.IBuildpathEntry[])
	 */
	@Override
	public void initialize(IScriptProject project, IBuildpathEntry[] currentEntries) {
		// Get all EE for this project in a list.
		currentEEs = new ArrayList<LuaExecutionEnvironment>();
		for (IBuildpathEntry entry : currentEntries) {

			LuaExecutionEnvironment executionEnvironment = null;
			try {
				executionEnvironment = LuaExecutionEnvironmentBuildpathUtil.getExecutionEnvironment(entry.getPath());
			} catch (CoreException e) {
				Activator.logWarning(NLS.bind("Unable to get execution environement for the path {0}.", entry.getPath(), e)); //$NON-NLS-1$
			}
			if (executionEnvironment != null) {
				currentEEs.add(executionEnvironment);
			}
		}

		// If there are EE already installed, show a warning.
		if (!currentEEs.isEmpty()) {
			setMessage(Messages.LuaExecutionEnvironmentWizardPage_warning_several_ee, WARNING);
		}

	}
}
