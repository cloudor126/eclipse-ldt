/*******************************************************************************
 * Copyright (c) 2012 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.ldt.ui.wizards;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IBuildpathEntry;
import org.eclipse.dltk.ui.wizards.ILocationGroup;
import org.eclipse.dltk.ui.wizards.IProjectWizard;
import org.eclipse.dltk.ui.wizards.ProjectCreator;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ldt.core.LuaConstants;
import org.eclipse.ldt.core.internal.LuaLanguageToolkit;
import org.eclipse.ldt.core.internal.PreferenceInitializer;
import org.eclipse.ldt.core.internal.buildpath.LuaExecutionEnvironment;
import org.eclipse.ldt.core.internal.buildpath.LuaExecutionEnvironmentBuildpathUtil;
import org.eclipse.ldt.ui.internal.Activator;
import org.eclipse.ldt.ui.wizards.pages.LuaProjectSettingsPage;
import org.osgi.service.prefs.BackingStoreException;

public class LuaProjectCreator extends ProjectCreator {

	private LuaProjectSettingsPage luaProjectSettingPage; // purpose of this field is simply to "gain" visibility on fLocationGroup private field
															// (sigh...)

	/**
	 * Adds a step for creating default file in default source folder.
	 * 
	 * @param owner
	 *            IProjectWizard asking for this project creator
	 * @param locationGroup
	 *            must be a IWizardPage from IProjectWizard described above
	 */
	public LuaProjectCreator(IProjectWizard owner, LuaProjectSettingsPage locationGroup) {
		super(owner, locationGroup);
		this.luaProjectSettingPage = locationGroup;
		ProjectCreateStep createSourceFolderStep = createSourceFolderStep();
		if (createSourceFolderStep != null)
			addStep(IProjectCreateStep.KIND_FINISH, 0, createSourceFolderStep, (IWizardPage) locationGroup);

		ProjectCreateStep setGrammarStep = createGrammaStep();
		if (setGrammarStep != null)
			addStep(IProjectCreateStep.KIND_FINISH, 0, setGrammarStep, (IWizardPage) locationGroup);
	}

	/**
	 * Sets a specific source folder instead of project's root folder.
	 */
	@Override
	protected List<IBuildpathEntry> getDefaultBuildpathEntries() {
		List<IBuildpathEntry> buildPath = new ArrayList<IBuildpathEntry>();

		if (!luaProjectSettingPage.isExistingLocation()) {

			boolean useTemplateBuildpath = false;
			LuaExecutionEnvironment luaExecutionEnvironment = luaProjectSettingPage.getExecutionEnvironment();
			if (luaExecutionEnvironment != null) {

				// add execution environment to buildpath
				IPath path = LuaExecutionEnvironmentBuildpathUtil.getLuaExecutionEnvironmentContainerPath(luaExecutionEnvironment);
				IBuildpathEntry newContainerEntry = DLTKCore.newContainerEntry(path);
				buildPath.add(newContainerEntry);

				// add template buildpath if needed
				if (luaProjectSettingPage.hasToCreateTemplate()) {
					Map<?, ?> defaultTemplateInfo = luaExecutionEnvironment.getDefaultTemplateInfo();
					if (defaultTemplateInfo != null && defaultTemplateInfo.get(LuaExecutionEnvironment.TEMPLATE_BUILDPATH) instanceof Map
							&& luaExecutionEnvironment.getTemplatesPath() != null) {
						useTemplateBuildpath = true;
						Map<?, ?> templateBuildPathEntries = (Map<?, ?>) defaultTemplateInfo.get(LuaExecutionEnvironment.TEMPLATE_BUILDPATH);
						// add template buildpaths define in the EE
						for (Object entry : templateBuildPathEntries.values()) {
							// handle the case of the project itself is the source path
							if (entry instanceof String) {
								IBuildpathEntry newSourceEntry;
								if ("/".equals(entry) || ".".equals(entry) || "./".equals(entry) || "/.".equals(entry)) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
									newSourceEntry = DLTKCore.newSourceEntry(getProject().getFullPath());
								} else {
									final IFolder sourcefolder = getProject().getFolder((String) entry);
									newSourceEntry = DLTKCore.newSourceEntry(sourcefolder.getFullPath());
								}
								buildPath.add(newSourceEntry);
							}
						}
					}
				}
			}

			// if no buildpath set use the default one
			if (!useTemplateBuildpath) {
				final IFolder sourcefolder = getProject().getFolder(LuaConstants.SOURCE_FOLDER);
				final IBuildpathEntry newSourceEntry = DLTKCore.newSourceEntry(sourcefolder.getFullPath());
				buildPath.add(newSourceEntry);
			}
		}
		return buildPath;
	}

	/**
	 * @see #getDefaultBuildpathEntries()
	 * @see ProjectCreator#initBuildpath(IProgressMonitor)
	 */
	protected IBuildpathEntry[] initBuildpath(IProgressMonitor monitor) throws CoreException {
		final List<IBuildpathEntry> entries = new ArrayList<IBuildpathEntry>(getDefaultBuildpathEntries());
		monitor.done();
		return entries.toArray(new IBuildpathEntry[entries.size()]);
	}

	private class SetGrammarStep extends ProjectCreateStep {

		@Override
		public void execute(IProject project, IProgressMonitor monitor) throws CoreException, InterruptedException {
			String grammar = luaProjectSettingPage.getGrammar();
			try {
				IEclipsePreferences node = new ProjectScope(project).getNode(LuaLanguageToolkit.getDefault().getPreferenceQualifier());
				node.put(PreferenceInitializer.GRAMMAR_DEFAULT_ID, grammar);
				node.flush();
			} catch (BackingStoreException e) {
				Activator.logError(MessageFormat.format("Unable store grammar version {0} for project {1}", grammar, project.getName()), e); //$NON-NLS-1$
			}
			monitor.done();
		}

	}

	/**
	 * Creates a default file named LuaWizardContants.DEFAULT_MAIN_FILE in default source folder.
	 */
	private class CreateDefaultSourceFolderProjectCreateStep extends ProjectCreateStep {

		/**
		 * @see ProjectCreateStep#execute(IProject,IProgressMonitor)
		 */
		@Override
		public void execute(final IProject project, final IProgressMonitor monitor) throws CoreException, InterruptedException {
			monitor.beginTask(Messages.LuaProjectCreatorInitializingSourceFolder, 1);

			// Create main file for application project
			if (!luaProjectSettingPage.isExistingLocation() && luaProjectSettingPage.hasToCreateTemplate()) {
				boolean foundTemplate = false;

				// Find template in Execution Environment
				final LuaExecutionEnvironment ee = luaProjectSettingPage.getExecutionEnvironment();
				if (ee != null) {

					// current way to store template
					final IPath templatesPath = ee.getTemplatesPath();
					if (templatesPath != null) {
						File templateFile = templatesPath.append(LuaExecutionEnvironment.DEFAULT_TEMPLATE).toFile();
						if (templateFile != null && templateFile.exists() && templateFile.listFiles().length > 0) {
							foundTemplate = true;
							try {
								FileUtils.copyDirectory(templateFile, project.getLocation().toFile());
							} catch (IOException e) {
								Activator.logError(MessageFormat.format("Unable to copy {0} from EE {1}.", templateFile.toString(), ee), e); //$NON-NLS-1$
							}
						}

					} else if (ee.getOldTemplatePath() != null) {
						// try to find the legacy main.lua in the EE (used by legacy EE only)

						IFolder srcFolder = project.getFolder(LuaConstants.SOURCE_FOLDER);
						if (srcFolder != null) {
							IPath mainPath = ee.getOldTemplatePath().append(LuaConstants.DEFAULT_MAIN_FILE);
							foundTemplate = true;
							try {
								FileUtils.copyFileToDirectory(mainPath.toFile(), srcFolder.getLocation().toFile());
							} catch (IOException e) {
								Activator.logError(MessageFormat.format("Unable to copy 'main.lua' from EE {0}.", ee), e); //$NON-NLS-1$
							}
						}
					}
				}

				// When no template is available, use default one
				if (!foundTemplate) {
					IFolder srcFolder = project.getFolder(LuaConstants.SOURCE_FOLDER);
					if (srcFolder != null) {
						final IFile mainFile = srcFolder.getFile(LuaConstants.DEFAULT_MAIN_FILE);
						InputStream mainContent = null;

						mainContent = new ByteArrayInputStream(LuaConstants.MAIN_FILE_CONTENT.getBytes());
						mainFile.create(mainContent, false, new SubProgressMonitor(monitor, 1));
					}
				}
			}
			// refreshing the folder
			project.refreshLocal(IProject.DEPTH_INFINITE, new SubProgressMonitor(monitor, 1));
			monitor.done();
		}
	}

	/**
	 * @return the locationGroup
	 */
	public ILocationGroup getLocationGroup() {
		return luaProjectSettingPage;
	}

	protected ProjectCreateStep createSourceFolderStep() {
		return new CreateDefaultSourceFolderProjectCreateStep();
	}

	protected ProjectCreateStep createGrammaStep() {
		return new SetGrammarStep();
	}
}
