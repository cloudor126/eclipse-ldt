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
package org.eclipse.ldt.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IBuildpathEntry;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.ui.wizards.CapabilityConfigurationPage;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ldt.core.LuaNature;
import org.eclipse.ldt.core.internal.buildpath.LuaExecutionEnvironment;
import org.eclipse.ldt.core.internal.buildpath.LuaExecutionEnvironmentBuildpathUtil;
import org.eclipse.ldt.core.internal.buildpath.LuaExecutionEnvironmentConstants;
import org.eclipse.ldt.ui.internal.Activator;
import org.eclipse.ldt.ui.internal.ImageConstants;
import org.eclipse.ldt.ui.wizards.pages.ConvertToLuaProjectMainPage;
import org.eclipse.osgi.util.NLS;

/**
 * @since 1.3
 */
public class ConvertToLuaProjectWizard extends Wizard {

	private static final Object KONEKI_CONTAINER_PATH_START = "org.eclipse.koneki.ldt.ExecutionEnvironmentContainer"; //$NON-NLS-1$

	private IProject project;
	private ConvertToLuaProjectMainPage mainpage;
	private CapabilityConfigurationPage buildPathpage;
	private IScriptProject scriptProject;

	public ConvertToLuaProjectWizard(IProject project) {
		Assert.isNotNull(project);
		this.project = project;
		scriptProject = DLTKCore.create(project);

		final ImageRegistry imageRegistry = Activator.getDefault().getImageRegistry();
		setDefaultPageImageDescriptor(imageRegistry.getDescriptor(ImageConstants.LUA_WIZARD_BAN));
		setWindowTitle(Messages.ConvertToLuaProjectWizard_wizardTitle);
	}

	/**
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		mainpage = new ConvertToLuaProjectMainPage("mainPage", project); //$NON-NLS-1$
		addPage(mainpage);
		buildPathpage = new CapabilityConfigurationPage("secondPage") { //$NON-NLS-1$
			@Override
			protected String getScriptNature() {
				return LuaNature.ID;
			}

			@Override
			public void setVisible(boolean visible) {
				if (visible) {
					// update default buildpath
					this.init(scriptProject, getDefaultBuildpath(), true);
				}
				super.setVisible(visible);
			}
		};
		addPage(buildPathpage);
	}

	private IBuildpathEntry[] getDefaultBuildpath() {
		IBuildpathEntry[] rawBuildPath = new IBuildpathEntry[0];

		// if this is a koneki migration, use koneki buildpath
		if (mainpage.isKonekiMigration()) {
			try {
				IBuildpathEntry[] konekiRawBuildPath = scriptProject.getRawBuildpath();
				rawBuildPath = new IBuildpathEntry[konekiRawBuildPath.length];
				for (int i = 0; i < konekiRawBuildPath.length; i++) {
					// convert koneki lua execution environment path to ldt environment path
					IPath konekiPath = konekiRawBuildPath[i].getPath();
					if (isValidKonekiExecutionEnvironmentBuildPath(konekiPath)) {
						String ldtpath = konekiPath.toString().replaceAll("org\\.eclipse\\.koneki\\.ldt\\.ExecutionEnvironmentContainer", //$NON-NLS-1$
								LuaExecutionEnvironmentConstants.CONTAINER_PATH_START);
						rawBuildPath[i] = DLTKCore.newContainerEntry(new Path(ldtpath));
					} else {
						rawBuildPath[i] = konekiRawBuildPath[i];
					}
				}
			} catch (ModelException e) {
				Activator.logWarning("unable to get koneki buildpath for project", e); //$NON-NLS-1$
			}
		}

		// if an execution environment is selected add it to the build path (if necessary)
		LuaExecutionEnvironment executionEnvironement = mainpage.getLuaExecutionEnvironement();
		if (executionEnvironement != null) {
			IPath path = LuaExecutionEnvironmentBuildpathUtil.getLuaExecutionEnvironmentContainerPath(executionEnvironement);
			IBuildpathEntry newContainerEntry = DLTKCore.newContainerEntry(path);
			if (!ArrayUtils.contains(rawBuildPath, newContainerEntry)) {
				IBuildpathEntry[] newBuildPath = new IBuildpathEntry[rawBuildPath.length + 1];
				System.arraycopy(rawBuildPath, 0, newBuildPath, 1, rawBuildPath.length);
				newBuildPath[0] = newContainerEntry;

				rawBuildPath = newBuildPath;
			}
		}
		return rawBuildPath;
	}

	private static boolean isValidKonekiExecutionEnvironmentBuildPath(final IPath eePath) {
		if (eePath == null)
			return false;

		final String[] segments = eePath.segments();
		return (segments.length == 3) && KONEKI_CONTAINER_PATH_START.equals(segments[0]);
	}

	/**
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		try {
			getContainer().run(false, true, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					SubMonitor smonitor = SubMonitor.convert(monitor, 100);

					try {
						IProjectDescription description = project.getDescription();

						// Add Lua Nature
						String[] natures = description.getNatureIds();
						String[] newNatures = new String[natures.length + 1];
						System.arraycopy(natures, 0, newNatures, 0, natures.length);
						newNatures[natures.length] = LuaNature.ID;
						description.setNatureIds(newNatures);

						// Add dltk builder
						ICommand[] commands = description.getBuildSpec();
						ICommand[] newcommands = new ICommand[commands.length + 1];
						System.arraycopy(commands, 0, newcommands, 0, commands.length);
						ICommand command = description.newCommand();
						command.setBuilderName("org.eclipse.dltk.core.scriptbuilder"); //$NON-NLS-1$
						newcommands[commands.length] = command;
						description.setBuildSpec(newcommands);
						project.setDescription(description, smonitor.newChild(30));

						// Update Build Path
						if (getContainer().getCurrentPage() == buildPathpage)
							buildPathpage.configureScriptProject(smonitor.newChild(70));
						else
							scriptProject.setRawBuildpath(getDefaultBuildpath(), smonitor.newChild(70));

					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					}
				}
			});
		} catch (InvocationTargetException e) {
			String message = NLS.bind(Messages.ConvertToLuaProjectWizard_convertFailedMessage, project.getName());
			Activator.logError(message, e);
			ErrorDialog.openError(getShell(), Messages.ConvertToLuaProjectWizard_ConvertFailedDialogTitle, null, new Status(IStatus.ERROR,
					Activator.PLUGIN_ID, message, e.getCause()));
			return false;
		} catch (InterruptedException e) {
			String message = NLS.bind(Messages.ConvertToLuaProjectWizard_convertFailedMessage, project.getName());
			Activator.logError(message, e);
			return false;
		}
		return true;
	}
}
