/*******************************************************************************
 * Copyright (c) 2011-2012 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.ldt.debug.ui.internal.launchconfiguration.attach;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.dltk.debug.core.DLTKDebugPlugin;
import org.eclipse.dltk.debug.ui.launchConfigurations.ScriptLaunchConfigurationTab;
import org.eclipse.dltk.debug.ui.messages.DLTKLaunchConfigurationsMessages;
import org.eclipse.dltk.internal.launching.LaunchConfigurationUtils;
import org.eclipse.dltk.launching.ScriptLaunchConfigurationConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.ldt.core.internal.LuaLanguageToolkit;
import org.eclipse.ldt.debug.core.internal.LuaDebugConstants;
import org.eclipse.ldt.debug.ui.internal.Activator;
import org.eclipse.ldt.debug.ui.internal.DocumentationLinksConstants;
import org.eclipse.ldt.debug.ui.internal.Messages;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

public class LuaAttachMainTab extends ScriptLaunchConfigurationTab {

	private static final String DEFAULT_IDEKEY = "luaidekey"; //$NON-NLS-1$
	private static final String DEFAULT_REPLACE_PATH = ""; //$NON-NLS-1$
	private static final String DEFAULT_MAPPING_TYPE = LuaDebugConstants.LOCAL_MAPPING_TYPE;

	private Text txtIdeKey;
	private Text txtTimeout;
	private Button btnLocalResolution;
	private Button btnModuleResolution;
	private Button btnReplacePathResolution;
	private Label lblReplacePath;
	private Text txtReplacePath;

	private SelectionListener sourceMappingSelectionListener = new SelectionListener() {

		@Override
		public void widgetSelected(SelectionEvent e) {
			widgetDefaultSelected(e);
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			updateSouceMappingUI();
			scheduleUpdateJob();
		}
	};

	private ModifyListener textModifyListener = new ModifyListener() {
		public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
			scheduleUpdateJob();
		};
	};

	public LuaAttachMainTab(String mode) {
		super(mode);
	}

	/*
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return DLTKLaunchConfigurationsMessages.remoteTab_title;
	}

	/*
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getImage()
	 */
	@Override
	public Image getImage() {
		return Activator.getDefault().getImageRegistry().get(org.eclipse.ldt.debug.ui.internal.ImageConstants.LUA_ATTACH_MAINTAB);
	}

	private int getDefaultRemoteTimeout() {
		return DLTKDebugPlugin.getConnectionTimeout() * 3;
	}

	/*
	 * @see org.eclipse.dltk.debug.ui.launchConfigurations.ScriptLaunchConfigurationTab #doInitializeForm(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	protected void doInitializeForm(ILaunchConfiguration config) {
		txtIdeKey.setText(LaunchConfigurationUtils.getString(config, ScriptLaunchConfigurationConstants.ATTR_DLTK_DBGP_SESSION_ID, DEFAULT_IDEKEY));

		txtTimeout.setText(Integer.toString(LaunchConfigurationUtils.getConnectionTimeout(config, getDefaultRemoteTimeout()) / 1000));

		String mappingType = LaunchConfigurationUtils.getString(config, LuaDebugConstants.ATTR_LUA_SOURCE_MAPPING_TYPE, DEFAULT_MAPPING_TYPE);
		selectSourceMapping(mappingType);

		txtReplacePath.setText(LaunchConfigurationUtils.getString(config, ScriptLaunchConfigurationConstants.ATTR_DLTK_DBGP_REMOTE_WORKING_DIR,
				DEFAULT_REPLACE_PATH));
	}

	/**
	 * select the source mapping graphicaly
	 */
	private void selectSourceMapping(String mappingType) {
		if (mappingType.equals(LuaDebugConstants.MODULE_MAPPING_TYPE)) {
			btnModuleResolution.setSelection(true);
		} else if (mappingType.equals(LuaDebugConstants.REPLACE_PATH_MAPPING_TYPE)) {
			btnReplacePathResolution.setSelection(true);

		} else {
			// LOCAL MAPPING TYPE AS DEFAULT
			btnLocalResolution.setSelection(true);
		}
		updateSouceMappingUI();
	}

	private void updateSouceMappingUI() {
		boolean selection = btnReplacePathResolution.getSelection();
		txtReplacePath.setEnabled(selection);
		lblReplacePath.setEnabled(selection);
	}

	/*
	 * @see org.eclipse.dltk.debug.ui.launchConfigurations.ScriptLaunchConfigurationTab
	 * #doPerformApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	protected void doPerformApply(ILaunchConfigurationWorkingCopy config) {
		// set idekey
		config.setAttribute(ScriptLaunchConfigurationConstants.ATTR_DLTK_DBGP_SESSION_ID, txtIdeKey.getText().trim());

		// set time out
		int timeout;
		try {
			timeout = Integer.parseInt(txtTimeout.getText().trim());
		} catch (NumberFormatException e) {
			timeout = getDefaultRemoteTimeout() / 1000;
		}
		config.setAttribute(ScriptLaunchConfigurationConstants.ATTR_DLTK_DBGP_WAITING_TIMEOUT, timeout * 1000);

		// set source mapping type
		String sourceMapping = getSelectedSourceMapping();
		config.setAttribute(LuaDebugConstants.ATTR_LUA_SOURCE_MAPPING_TYPE, sourceMapping);

		// set replace path
		config.setAttribute(ScriptLaunchConfigurationConstants.ATTR_DLTK_DBGP_REMOTE_WORKING_DIR, txtReplacePath.getText().trim());
	}

	/**
	 * get the selecte source mapping
	 */
	private String getSelectedSourceMapping() {
		if (btnModuleResolution.getSelection()) {
			return LuaDebugConstants.MODULE_MAPPING_TYPE;
		} else if (btnReplacePathResolution.getSelection()) {
			return LuaDebugConstants.REPLACE_PATH_MAPPING_TYPE;
		} else {
			return LuaDebugConstants.LOCAL_MAPPING_TYPE;
		}
	}

	protected boolean validate() {
		return super.validate() && validateIdeKey();
	}

	/**
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#canSave()
	 */
	@Override
	public boolean canSave() {
		return validate();
	}

	protected boolean validateIdeKey() {
		String key = txtIdeKey.getText().trim();
		if (key.length() == 0) {
			setErrorMessage(DLTKLaunchConfigurationsMessages.remoteError_ideKeyEmpty);
			return false;
		}

		return true;
	}

	/*
	 * @see ILaunchConfigurationTab#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);

		GridLayout topLayout = new GridLayout();
		topLayout.verticalSpacing = 0;
		comp.setLayout(topLayout);

		createClientInfo(comp);
		createProjectEditor(comp);
		createVerticalSpacer(comp, 1);

		doCreateControl(comp);
		createVerticalSpacer(comp, 1);

		createDebugOptionsGroup(comp);

		createCustomSections(comp);
		Dialog.applyDialogFont(comp);
	}

	protected void createClientInfo(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().applyTo(comp);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(comp);

		Link lnkDocumentation = new Link(comp, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(lnkDocumentation);
		lnkDocumentation.setText(NLS.bind(
				org.eclipse.ldt.debug.ui.internal.launchconfiguration.attach.Messages.LuaAttachMainTab_client_info_description,
				LuaDebugConstants.DEBUGGER_FILE_NAME));

		lnkDocumentation.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (LuaDebugConstants.DEBUGGER_FILE_NAME.equals(event.text)) {
					// get debugger file

					// prompt a directory selection dialog
					DirectoryDialog directoryDialog = new DirectoryDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell());
					String selectedDirPath = directoryDialog.open();
					if (selectedDirPath != null) {
						// if directory is selected :
						File destDir = new File(selectedDirPath);

						// get the debugger file
						try {
							URL debuggerEntry = org.eclipse.ldt.debug.core.internal.Activator.getDefault().getBundle()
									.getEntry(LuaDebugConstants.DEBUGGER_PATH);
							File debuggerFolder = new File(FileLocator.toFileURL(debuggerEntry).getFile());
							File debuggerFile = new File(debuggerFolder, LuaDebugConstants.DEBUGGER_FILE_NAME);

							// copy debugger file in selected directory
							FileUtils.copyFileToDirectory(debuggerFile, destDir, true);

							if (MessageDialog.openQuestion(PlatformUI.getWorkbench().getDisplay().getActiveShell(), org.eclipse.ldt.debug.ui.internal.launchconfiguration.attach.Messages.LuaAttachMainTab_copy_done_title, NLS.bind(
									org.eclipse.ldt.debug.ui.internal.launchconfiguration.attach.Messages.LuaAttachMainTab_copy_done_question,
									LuaDebugConstants.DEBUGGER_FILE_NAME))) {
								Program.launch(destDir.toString());
							}

						} catch (IOException e) {
							ErrorDialog.openError(
									PlatformUI.getWorkbench().getDisplay().getActiveShell(),
									org.eclipse.ldt.debug.ui.internal.launchconfiguration.attach.Messages.LuaAttachMainTab_copy_failed_title,
									null,
									new Status(IStatus.WARNING, Activator.PLUGIN_ID, NLS.bind(org.eclipse.ldt.debug.ui.internal.launchconfiguration.attach.Messages.LuaAttachMainTab_copy_failed_description,
											LuaDebugConstants.DEBUGGER_FILE_NAME), e));
						}
					}
				} else
					// open documentation
					PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(DocumentationLinksConstants.ATTACH_DEBUG);
			}
		});
	}

	/*
	 * @see org.eclipse.dltk.debug.ui.launchConfigurations.ScriptLaunchConfigurationTab #doCreateControl(org.eclipse.swt.widgets.Composite)
	 */
	protected void doCreateControl(Composite composite) {

		// ======= Connection GROUP ==========
		Group grpConnectionProperties = new Group(composite, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(grpConnectionProperties);
		grpConnectionProperties.setText(Messages.LuaAttachMainTab_connection_properties_group);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(grpConnectionProperties);

		Label lblIdekey = new Label(grpConnectionProperties, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(lblIdekey);
		lblIdekey.setText(Messages.LuaAttachMainTab_idekey_label);

		txtIdeKey = new Text(grpConnectionProperties, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtIdeKey);
		txtIdeKey.addModifyListener(textModifyListener);

		Label lblTimeout = new Label(grpConnectionProperties, SWT.NONE);
		lblTimeout.setText(Messages.LuaAttachMainTab_timeout_label);
		GridDataFactory.swtDefaults().applyTo(lblTimeout);

		txtTimeout = new Text(grpConnectionProperties, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtTimeout);
		txtTimeout.addModifyListener(textModifyListener);

		// ======= SOURCE MAPPING GROUP ==========
		final Group grpSourceMapping = new Group(composite, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(grpSourceMapping);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(grpSourceMapping);
		grpSourceMapping.setText(Messages.LuaAttachMainTab_sourcemapping_group);

		int hident = 40;
		GridDataFactory generalInfoGridDataFactory = GridDataFactory.swtDefaults().grab(true, false).span(2, 1);
		GridDataFactory radiobuttonGridDataFactory = generalInfoGridDataFactory.copy().indent(10, 0);
		GridDataFactory infoGridDataFactory = GridDataFactory.fillDefaults().grab(true, false).span(2, 1).indent(hident, 0);

		// get information font
		Font italicfont = getInformationFont();

		Label lblsourcemappingintro = new Label(grpSourceMapping, SWT.NONE);
		generalInfoGridDataFactory.applyTo(lblsourcemappingintro);
		lblsourcemappingintro.setText(Messages.LuaAttachMainTab_documentation_intro);

		// Local Resolution
		btnLocalResolution = new Button(grpSourceMapping, SWT.RADIO);
		radiobuttonGridDataFactory.applyTo(btnLocalResolution);
		btnLocalResolution.setText(Messages.LuaAttachMainTab_localresolution_radiobutton);
		btnLocalResolution.addSelectionListener(sourceMappingSelectionListener);

		Text txtLocalResolution = new Text(grpSourceMapping, SWT.WRAP);
		infoGridDataFactory.applyTo(txtLocalResolution);
		txtLocalResolution.setText(Messages.LuaAttachMainTab_localresolution_textinfo);
		txtLocalResolution.setFont(italicfont);
		txtLocalResolution.setBackground(lblsourcemappingintro.getBackground());
		txtLocalResolution.setEnabled(false);

		btnModuleResolution = new Button(grpSourceMapping, SWT.RADIO);
		radiobuttonGridDataFactory.applyTo(btnModuleResolution);
		btnModuleResolution.setText(Messages.LuaAttachMainTab_moduleresolution_radiobutton);
		btnModuleResolution.addSelectionListener(sourceMappingSelectionListener);

		Text txtModuleResolution = new Text(grpSourceMapping, SWT.WRAP);
		infoGridDataFactory.applyTo(txtModuleResolution);
		txtModuleResolution.setText(Messages.LuaAttachMainTab_moduleresolution_textinfo);
		txtModuleResolution.setFont(italicfont);
		txtModuleResolution.setBackground(lblsourcemappingintro.getBackground());
		txtModuleResolution.setEnabled(false);

		// Replace path Resolution
		btnReplacePathResolution = new Button(grpSourceMapping, SWT.RADIO);
		btnReplacePathResolution.setText(Messages.LuaAttachMainTab_replacepathresolution_radiobutton);
		radiobuttonGridDataFactory.applyTo(btnReplacePathResolution);
		btnReplacePathResolution.addSelectionListener(sourceMappingSelectionListener);

		Text txtReplacePathResolution = new Text(grpSourceMapping, SWT.WRAP);
		infoGridDataFactory.applyTo(txtReplacePathResolution);
		txtReplacePathResolution.setText(Messages.LuaAttachMainTab_replacepathresolution_textinfo);
		txtReplacePathResolution.setFont(italicfont);
		txtReplacePathResolution.setBackground(lblsourcemappingintro.getBackground());
		txtReplacePathResolution.setEnabled(false);

		lblReplacePath = new Label(grpSourceMapping, SWT.NONE);
		GridDataFactory.swtDefaults().indent(hident, 0).applyTo(lblReplacePath);
		lblReplacePath.setText(Messages.LuaAttachMainTab_path_label);

		txtReplacePath = new Text(grpSourceMapping, SWT.BORDER);
		GridDataFactory.fillDefaults().applyTo(txtReplacePath);
		txtReplacePath.addModifyListener(textModifyListener);

		// link documentation
		Link lnkDocumentation = new Link(grpSourceMapping, SWT.NONE);
		generalInfoGridDataFactory.applyTo(lnkDocumentation);
		lnkDocumentation.setText(Messages.LuaAttachMainTab_documentation_link);
		lnkDocumentation.addListener(SWT.Selection, new Listener() {
			public void handleEvent(final Event event) {
				PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(DocumentationLinksConstants.SOURCE_MAPPING);
			}
		});
	}

	private Font getInformationFont() {
		Font textFont = JFaceResources.getTextFont();
		if (textFont == null)
			return JFaceResources.getDefaultFont();

		if (textFont.getFontData().length > 0) {
			Font italic = JFaceResources.getFontRegistry().getItalic(textFont.getFontData()[0].getName());
			if (italic != null)
				return italic;
		}
		return textFont;
	}

	@Override
	protected String getNatureID() {
		return LuaLanguageToolkit.getDefault().getNatureId();
	}
}
