/*******************************************************************************
 * Copyright (c) 2012, 2013 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.koneki.ldt.debug.ui.internal.interpreters;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.dltk.core.environment.IEnvironment;
import org.eclipse.dltk.core.internal.environment.LazyFileHandle;
import org.eclipse.dltk.internal.debug.ui.interpreters.AbstractInterpreterLibraryBlock;
import org.eclipse.dltk.internal.debug.ui.interpreters.AddScriptInterpreterDialog;
import org.eclipse.dltk.internal.debug.ui.interpreters.IAddInterpreterDialogRequestor;
import org.eclipse.dltk.internal.debug.ui.interpreters.IScriptInterpreterDialog;
import org.eclipse.dltk.internal.debug.ui.interpreters.InterpretersMessages;
import org.eclipse.dltk.launching.EnvironmentVariable;
import org.eclipse.dltk.launching.IInterpreterInstall;
import org.eclipse.dltk.launching.IInterpreterInstallType;
import org.eclipse.dltk.launching.InterpreterStandin;
import org.eclipse.dltk.ui.environment.IEnvironmentUI;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.koneki.ldt.core.internal.LuaLanguageToolkit;
import org.eclipse.koneki.ldt.core.internal.PreferenceInitializer;
import org.eclipse.koneki.ldt.core.internal.buildpath.LuaExecutionEnvironment;
import org.eclipse.koneki.ldt.core.internal.buildpath.LuaExecutionEnvironmentManager;
import org.eclipse.koneki.ldt.debug.core.IEmbeddedInterpreterInstallType;
import org.eclipse.koneki.ldt.debug.core.internal.interpreter.generic.LuaGenericInterpreterInstallType;
import org.eclipse.koneki.ldt.debug.core.internal.model.interpreter.Info;
import org.eclipse.koneki.ldt.debug.core.internal.model.interpreter.InterpreterFactory;
import org.eclipse.koneki.ldt.debug.core.internal.model.interpreter.impl.InterpreterFactoryImpl;
import org.eclipse.koneki.ldt.debug.core.internal.model.interpreter.impl.InterpreterPackageImpl;
import org.eclipse.koneki.ldt.debug.core.interpreter.ILuaInterpreterInstallType;
import org.eclipse.koneki.ldt.debug.core.interpreter.LuaInterpreterUtil;
import org.eclipse.koneki.ldt.debug.ui.internal.Activator;
import org.eclipse.koneki.ldt.ui.LuaExecutionEnvironmentUIManager;
import org.eclipse.koneki.ldt.ui.SWTUtil;
import org.eclipse.koneki.ldt.ui.internal.buildpath.LuaExecutionEnvironmentContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

@SuppressWarnings("restriction")
public class AddLuaInterpreterDialog extends StatusDialog implements IScriptInterpreterDialog {

	private IAddInterpreterDialogRequestor requestor;
	private IEnvironment environment;
	private IInterpreterInstall currentInterperter;
	private IInterpreterInstallType[] interpreterInstallTypes;

	// UI components
	private ComboViewer typesCombo;
	private Text nameText;
	private Text pathText;
	private Button browseButton;
	private Text argsText;
	private LuaInterpreterEnvironmentVariablesBlock environementVariableBlock;
	private Button handlesExecutionOption;
	private Group capabilitiesGroup;
	private Button handlesFilesAsArguments;
	private ComboViewer installedEEsComboViewer;

	public AddLuaInterpreterDialog(final IAddInterpreterDialogRequestor requestor, final Shell shell, final IEnvironment environment,
			final IInterpreterInstallType[] interpreterInstallTypes, final IInterpreterInstall standin) {
		super(shell);
		this.requestor = requestor;
		this.environment = environment;
		this.currentInterperter = standin;
		this.interpreterInstallTypes = interpreterInstallTypes;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected Control createDialogArea(final Composite parent) {

		Composite container = new Composite(parent, SWT.NONE);
		Point margin = new Point(0, 0);
		margin.x = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		margin.y = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		Point spacing = new Point(0, 0);
		spacing.x = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		spacing.y = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		GridLayoutFactory.swtDefaults().spacing(spacing).margins(margin).numColumns(3).applyTo(container);
		GridDataFactory.swtDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(container);

		createLabel(container, InterpretersMessages.addInterpreterDialog_InterpreterEnvironmentType);
		typesCombo = new ComboViewer(container);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).span(2, 1).applyTo(typesCombo.getControl());
		typesCombo.setContentProvider(new ArrayContentProvider());
		typesCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IInterpreterInstallType) {
					return ((IInterpreterInstallType) element).getName();
				}
				return super.getText(element);
			}
		});

		createLabel(container, InterpretersMessages.addInterpreterDialog_InterpreterExecutableName);
		pathText = new Text(container, SWT.SINGLE | SWT.BORDER);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).hint(300, SWT.DEFAULT).applyTo(pathText);
		browseButton = new Button(container, SWT.PUSH);
		browseButton.setText(InterpretersMessages.addInterpreterDialog_browse1);
		GridDataFactory.swtDefaults().hint(SWTUtil.getButtonWidthHint(browseButton), -1).applyTo(browseButton);

		createLabel(container, InterpretersMessages.addInterpreterDialog_InterpreterEnvironmentName);
		nameText = new Text(container, SWT.SINGLE | SWT.BORDER);
		GridDataFactory.swtDefaults().grab(true, false).span(2, 1).align(SWT.FILL, SWT.CENTER).applyTo(nameText);

		createLabel(container, InterpretersMessages.AddInterpreterDialog_iArgs);
		argsText = new Text(container, SWT.SINGLE | SWT.BORDER);
		GridDataFactory.swtDefaults().grab(true, false).span(2, 1).align(SWT.FILL, SWT.CENTER).applyTo(argsText);

		// Execution Environment actual list
		createLabel(container, Messages.AddLuaInterpreterDialog_linkExecutionEnvironment);
		installedEEsComboViewer = new ComboViewer(container, SWT.READ_ONLY | SWT.BORDER);
		installedEEsComboViewer.setContentProvider(new LuaExecutionEnvironmentContentProvider());
		GridDataFactory.swtDefaults().grab(true, false).span(2, 1).align(SWT.FILL, SWT.BEGINNING).applyTo(installedEEsComboViewer.getControl());

		environementVariableBlock = new LuaInterpreterEnvironmentVariablesBlock(new AddInterpreterDialogAdapter(requestor, getShell(),
				interpreterInstallTypes, currentInterperter));
		final Composite environmentComposite = (Composite) environementVariableBlock.createControl(container);
		GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(2).applyTo(environmentComposite);
		GridDataFactory.swtDefaults().grab(true, true).span(3, 1).align(SWT.FILL, SWT.FILL).applyTo(environmentComposite);

		// Interpreter Capabilities
		capabilitiesGroup = new Group(container, SWT.None);
		capabilitiesGroup.setText(Messages.AddLuaInterpreterDialog_CapabilitesGroupLabel);
		GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(1).applyTo(capabilitiesGroup);
		GridDataFactory.swtDefaults().grab(true, false).span(3, 1).align(SWT.FILL, SWT.FILL).applyTo(capabilitiesGroup);

		handlesExecutionOption = new Button(capabilitiesGroup, SWT.CHECK);
		handlesExecutionOption.setText(Messages.AddLuaInterpreterDialog_ExecutionOption);

		handlesFilesAsArguments = new Button(capabilitiesGroup, SWT.CHECK);
		handlesFilesAsArguments.setText(Messages.AddLuaInterpreterDialog_FilesAsArguments);

		applyDialogFont(container);
		init();
		hookListeners();

		return container;
	}

	private Label createLabel(final Composite container, final String text) {
		final Label label = new Label(container, SWT.NONE);
		label.setText(text);
		GridDataFactory.swtDefaults().applyTo(label);
		return label;
	}

	private void hookListeners() {
		typesCombo.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateWidgetDefaultValue();
				updateWidgetState();
			}
		});

		browseButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				widgetDefaultSelected(e);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				browseForInstallation();
			}
		});

		pathText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				updateStatusLine();
			}
		});

		nameText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				updateStatusLine();
			}
		});
	}

	private void init() {
		// init type combo
		typesCombo.setInput(interpreterInstallTypes);
		typesCombo.getControl().setEnabled(currentInterperter == null);

		// init execution environment combo
		final List<LuaExecutionEnvironment> installedExecutionEnvironments = LuaExecutionEnvironmentUIManager.getAvailableExecutionEnvironments();
		installedEEsComboViewer.setInput(installedExecutionEnvironments);

		if (currentInterperter != null) {
			// Current interpreter != null => we are in edit mode

			// select interpreter type
			typesCombo.setSelection(new StructuredSelection(currentInterperter.getInterpreterInstallType()));

			// update widget value.
			if (currentInterperter.getInstallLocation().toOSString().length() > 0)
				pathText.setText(currentInterperter.getInstallLocation().toOSString());

			nameText.setText(currentInterperter.getName());

			String args = currentInterperter.getInterpreterArgs();
			if (args != null)
				argsText.setText(args);

			environementVariableBlock.initializeFrom(currentInterperter, currentInterperter.getInterpreterInstallType());

			handlesExecutionOption.setSelection(LuaInterpreterUtil.interpreterHandlesExecuteOption(currentInterperter));
			handlesFilesAsArguments.setSelection(LuaInterpreterUtil.interpreterHandlesFilesAsArgument(currentInterperter));

			String eeName = LuaInterpreterUtil.linkedExecutionEnvironmentName(currentInterperter);
			String eeVersion = LuaInterpreterUtil.linkedExecutionEnvironmentVersion(currentInterperter);

			try {
				LuaExecutionEnvironment ee = LuaExecutionEnvironmentManager.getAvailableExecutionEnvironment(eeName, eeVersion);
				if (ee != null)
					installedEEsComboViewer.setSelection(new StructuredSelection(ee));
			} catch (CoreException e) {
				Activator.log(e.getStatus());
			}

		} else {
			// Current interpreter == null => we are in create mode
			IInterpreterInstallType lua51 = null;
			IInterpreterInstallType luageneric = null;
			for (IInterpreterInstallType type : interpreterInstallTypes) {
				if ("Lua 5.1".equals(type.getName())) { //$NON-NLS-1$
					lua51 = type;
					break;
				}
				if (type instanceof LuaGenericInterpreterInstallType) {
					luageneric = type;
				}
			}
			if (lua51 != null)
				typesCombo.setSelection(new StructuredSelection(lua51));
			else
				typesCombo.setSelection(new StructuredSelection(luageneric));

			// Select default
			// for user experience, Lua 5.1 by default or lua generic is not found.
			updateWidgetDefaultValue();
		}

		// update widget state
		updateWidgetState();

		// update environment block buttons
		environementVariableBlock.update();
	}

	private void updateWidgetDefaultValue() {

		// Get selected interpreter type
		final IInterpreterInstallType selectedType = getSelectedInterpreterType();

		// Get default value
		String defaultPath = ""; //$NON-NLS-1$
		String defaultName = ""; //$NON-NLS-1$
		String defaultArgs = ""; //$NON-NLS-1$
		boolean defaultHandlesFilesAsArguments = true;
		boolean defaulthandlesExecutionOption = true;
		LuaExecutionEnvironment defaultee = null;

		// Try to cast it has LuaInterpreter to know default value
		ILuaInterpreterInstallType selectedLuaInterpreterType;
		if (selectedType instanceof ILuaInterpreterInstallType) {
			selectedLuaInterpreterType = (ILuaInterpreterInstallType) selectedType;

			if (selectedLuaInterpreterType.isEmbeddedInterpreter())
				defaultPath = "(Embedded)"; //$NON-NLS-1$

			if (selectedLuaInterpreterType.getDefaultInterpreterName() != null)
				defaultName = selectedLuaInterpreterType.getDefaultInterpreterName();

			if (selectedLuaInterpreterType.getDefaultInterpreterArguments() != null)
				defaultArgs = selectedLuaInterpreterType.getDefaultInterpreterArguments();

			defaulthandlesExecutionOption = selectedLuaInterpreterType.handleExecuteOption();
			defaultHandlesFilesAsArguments = selectedLuaInterpreterType.handleFilesAsArgument();

			try {
				defaultee = LuaExecutionEnvironmentManager.getAvailableExecutionEnvironment(selectedLuaInterpreterType.getDefaultEEName(),
						selectedLuaInterpreterType.getDefaultEEVersion());
			} catch (CoreException e) {
				Activator.log(e.getStatus());
			}
		} else {
			if (selectedType instanceof IEmbeddedInterpreterInstallType)
				defaultPath = "(Embedded)"; //$NON-NLS-1$
		}

		// if no default execution environment linked to this interpreter type.
		if (defaultee == null) {
			final List<LuaExecutionEnvironment> installedExecutionEnvironments = LuaExecutionEnvironmentUIManager.getAvailableExecutionEnvironments();
			if (installedExecutionEnvironments.size() > 0) {
				// look for default EE
				ScopedPreferenceStore preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, LuaLanguageToolkit.getDefault()
						.getPreferenceQualifier());
				String defaultEEId = preferenceStore.getString(PreferenceInitializer.EE_DEFAULT_ID);
				for (LuaExecutionEnvironment execEnv : installedExecutionEnvironments) {
					if (execEnv.getEEIdentifier().equals(defaultEEId))
						defaultee = execEnv;
				}

				// if no default EE were found, select the first one
				if (defaultee == null) {
					defaultee = installedExecutionEnvironments.get(0);
				}
			}
		}

		// set value
		pathText.setText(defaultPath);
		nameText.setText(defaultName);
		argsText.setText(defaultArgs);
		handlesExecutionOption.setSelection(defaulthandlesExecutionOption);
		handlesFilesAsArguments.setSelection(defaultHandlesFilesAsArguments);
		installedEEsComboViewer.setSelection(new StructuredSelection(defaultee));
	}

	private void updateWidgetState() {
		// Get selected interpreter type
		final IInterpreterInstallType selectedType = getSelectedInterpreterType();

		// TODO This declaration be move when we will remove IEmbeddedInterpreterInstallType for ldt 2.0.0.
		boolean isEmbedded = false;

		// Try to cast it has LuaInterpreter to know default value
		ILuaInterpreterInstallType selectedLuaInterpreterType;
		if (selectedType instanceof ILuaInterpreterInstallType) {
			selectedLuaInterpreterType = (ILuaInterpreterInstallType) selectedType;
			argsText.setEnabled(selectedLuaInterpreterType.handleInterpreterArguments());
			handlesExecutionOption.setEnabled(false);
			handlesFilesAsArguments.setEnabled(false);
			isEmbedded = selectedLuaInterpreterType.isEmbeddedInterpreter();
		} else {
			argsText.setEnabled(true);
			handlesExecutionOption.setEnabled(true);
			handlesFilesAsArguments.setEnabled(true);
			// Manage deprecated code (since 1.2)
			// Check if it is embedded
			isEmbedded = selectedType instanceof IEmbeddedInterpreterInstallType;
		}

		// Manage interpreter path :
		// Embedded interpreter does not need interpreter location
		browseButton.setEnabled(!isEmbedded);
		pathText.setEnabled(!isEmbedded);

	}

	private IInterpreterInstallType getSelectedInterpreterType() {
		return (IInterpreterInstallType) ((IStructuredSelection) typesCombo.getSelection()).getFirstElement();
	}

	private void browseForInstallation() {
		IEnvironmentUI environmentUI = (IEnvironmentUI) environment.getAdapter(IEnvironmentUI.class);
		if (environmentUI != null) {
			String defaultPath = currentInterperter != null ? currentInterperter.getInstallLocation().toOSString() : null;
			String newPath = environmentUI.selectFile(getShell(), IEnvironmentUI.EXECUTABLE, defaultPath);
			if (newPath != null) {
				pathText.setText(newPath);
			}
		}
	}

	@Override
	protected void okPressed() {
		if (currentInterperter == null) {
			IInterpreterInstallType selectedType = getSelectedInterpreterType();
			currentInterperter = new InterpreterStandin(selectedType, createUniqueId(selectedType));

			// notify the preference page of the new interpreter, the interpreter have to be filled
			setFieldValuesToInterpreter();
			requestor.interpreterAdded(currentInterperter);
		} else {
			setFieldValuesToInterpreter();
		}
		super.okPressed();
	}

	private String createUniqueId(IInterpreterInstallType interpreterType) {
		String id = null;
		do {
			id = String.valueOf(System.currentTimeMillis());
		} while (interpreterType.findInterpreterInstall(id) != null);
		return id;
	}

	protected void setFieldValuesToInterpreter() {
		currentInterperter.setInstallLocation(new LazyFileHandle(environment.getId(), new Path(pathText.getText().trim())));
		currentInterperter.setName(nameText.getText().trim());

		final String argString = argsText.getText().trim();
		if (argString != null && argString.length() > 0) {
			currentInterperter.setInterpreterArgs(argString);
		} else {
			currentInterperter.setInterpreterArgs(null);
		}

		/*
		 * Update interpreter capabilities
		 */

		// Execution option
		final boolean executionOptionChecked = handlesExecutionOption != null && handlesExecutionOption.getSelection();

		// Files as argument option
		final boolean filesAsArgumentOptionChecked = handlesFilesAsArguments != null && handlesFilesAsArguments.getSelection();

		// manage linked Execution environement
		ISelection selection = installedEEsComboViewer.getSelection();
		String eeName = null;
		String eeVersion = null;
		if (selection instanceof IStructuredSelection) {
			Object firstElement = ((IStructuredSelection) selection).getFirstElement();
			if (firstElement instanceof LuaExecutionEnvironment) {
				eeName = ((LuaExecutionEnvironment) firstElement).getID();
				eeVersion = ((LuaExecutionEnvironment) firstElement).getVersion();
			}
		}

		final InterpreterFactory factory = InterpreterFactoryImpl.eINSTANCE;
		final Info info = factory.createInfo();
		info.setExecuteOptionCapable(executionOptionChecked);
		info.setFileAsArgumentsCapable(filesAsArgumentOptionChecked);
		info.setLinkedExecutionEnvironmentName(eeName);
		info.setLinkedExecutionEnvironmentVersion(eeVersion);

		currentInterperter.replaceExtension(InterpreterPackageImpl.eINSTANCE.getInfo(), info);

		environementVariableBlock.performApply(currentInterperter);
	}

	@Override
	public boolean execute() {
		return open() == Window.OK;
	}

	@Override
	public void updateStatusLine() {
		String path = pathText.getText().trim();
		if (path.isEmpty()) {
			updateStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, InterpretersMessages.addInterpreterDialog_enterLocation));
		} else {
			String name = nameText.getText().trim();
			if (name.isEmpty()) {
				updateStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, InterpretersMessages.addInterpreterDialog_enterName));
			} else if (requestor.isDuplicateName(name, currentInterperter)) {
				updateStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, InterpretersMessages.addInterpreterDialog_duplicateName));
			} else {
				updateStatus(Status.OK_STATUS);
			}
		}
	}

	/**
	 * This method is override just to fix a scope problem between IStatus and Dialog super class
	 */
	@Override
	public void setButtonLayoutData(Button button) {
		super.setButtonLayoutData(button);
	}

	/**
	 * Adapter for the Environment Variable Block The block doesn't take a IAddScriptInterpreter dialog but a AddScriptInterpreterDialog
	 */
	private class AddInterpreterDialogAdapter extends AddScriptInterpreterDialog {

		public AddInterpreterDialogAdapter(IAddInterpreterDialogRequestor requestor, Shell shell, IInterpreterInstallType[] interpreterInstallTypes,
				IInterpreterInstall editedInterpreter) {
			super(requestor, shell, interpreterInstallTypes, editedInterpreter);
		}

		@Override
		protected AbstractInterpreterLibraryBlock createLibraryBlock(AddScriptInterpreterDialog dialog) {
			return null;
		}

		@Override
		public IEnvironment getEnvironment() {
			return environment;
		}

		@Override
		public void updateLibraries(EnvironmentVariable[] newVars, EnvironmentVariable[] oldVars) {
		}

		@Override
		protected void updateValidateInterpreterLocation() {
		}

		@Override
		public Shell getShell() {
			return AddLuaInterpreterDialog.this.getShell();
		}

		@Override
		public void updateStatusLine() {
			AddLuaInterpreterDialog.this.updateStatusLine();
		}

		@Override
		public void setSystemLibraryStatus(IStatus status) {
		}

		@Override
		public void setButtonLayoutData(Button button) {
			AddLuaInterpreterDialog.this.setButtonLayoutData(button);
		}

		@Override
		public int open() {
			return OK;
		}
	}

}
