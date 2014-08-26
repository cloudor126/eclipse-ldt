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
package org.eclipse.ldt.core;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.dltk.compiler.env.IModuleSource;
import org.eclipse.dltk.core.IBuildpathEntry;
import org.eclipse.dltk.core.IExternalSourceModule;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IParent;
import org.eclipse.dltk.core.IProjectFragment;
import org.eclipse.dltk.core.IScriptFolder;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.core.environment.EnvironmentPathUtils;
import org.eclipse.ldt.core.internal.Activator;
import org.eclipse.ldt.core.internal.buildpath.LuaExecutionEnvironmentBuildpathUtil;

/**
 * Utility class for Lua
 */
public final class LuaUtils {

	private LuaUtils() {
	}

	/**
	 * @return name of the module (without package qualifier) (no support of init.lua)
	 */
	public static String getModuleName(ISourceModule module) {
		String moduleName = module.getElementName();
		if (moduleName.endsWith(".lua")) { //$NON-NLS-1$
			moduleName = moduleName.replaceFirst("\\.lua$", ""); //$NON-NLS-1$//$NON-NLS-2$
		} else if (moduleName.endsWith(".doclua")) { //$NON-NLS-1$
			moduleName = moduleName.replaceFirst("\\.doclua$", ""); //$NON-NLS-1$//$NON-NLS-2$
		}
		return moduleName;
	}

	/**
	 * @return name of the module (without package qualifier) (no support of init.lua)
	 */
	public static String getModuleName(final IModuleSource module) {
		final IModelElement modelElement = module.getModelElement();
		if (modelElement instanceof ISourceModule) {
			return getModuleName((ISourceModule) modelElement);
		} else {
			return module.getFileName();
		}
	}

	/**
	 * @return full name of a module with dot syntax (support init.lua case)<br>
	 * 
	 *         e.g. : socket.core
	 */
	public static String getModuleFullName(final IModuleSource module) {
		final IModelElement modelElement = module.getModelElement();
		if (modelElement instanceof ISourceModule) {
			return getModuleFullName((ISourceModule) modelElement);
		} else {
			return module.getFileName();
		}
	}

	/**
	 * @return full name of a module with dot syntax (support init.lua case) <br>
	 * 
	 *         e.g. : socket.core
	 */
	public static String getModuleFullName(final ISourceModule module) {
		IPath path = getSourcePathRelativePath(module);

		// replace file name by the module name
		String moduleName = getModuleName(module);
		path = path.removeLastSegments(1);
		// handle the case of a package with a init module
		if (path.segmentCount() == 0 || !"init".equals(moduleName)) { //$NON-NLS-1$
			path = path.append(moduleName);
		}

		// build package prefix from the path
		StringBuilder fullNameBuilder = new StringBuilder();
		for (String segment : path.segments()) {
			if (fullNameBuilder.length() > 0) {
				fullNameBuilder.append("."); //$NON-NLS-1$
			}
			fullNameBuilder.append(segment);
		}

		return fullNameBuilder.toString();
	}

	/**
	 * @return the source path relative path of the given module
	 */
	public static IPath getSourcePathRelativePath(IModuleSource module) {
		final IModelElement modelElement = module.getModelElement();
		if (modelElement instanceof ISourceModule) {
			return getSourcePathRelativePath((ISourceModule) modelElement);
		} else {
			return new Path(module.getFileName());
		}
	}

	/**
	 * @return the source path relative path of the given module
	 */
	public static IPath getSourcePathRelativePath(ISourceModule module) {
		String moduleName = module.getElementName();

		// get prefix
		IPath prefix = null;
		if (module.getParent() instanceof IScriptFolder) {
			prefix = getFolderSourcePathRelativePath((IScriptFolder) module.getParent());
		}

		if (prefix != null)
			return prefix.append(moduleName);
		else
			return new Path(moduleName);
	}

	/*
	 * @return the source folder full name with module dot syntax
	 */
	private static IPath getFolderSourcePathRelativePath(final IScriptFolder folder) {
		if (!folder.isRootFolder()) {
			// get folder name
			final String folderName = folder.getElementName();

			// get prefix
			final IModelElement parent = folder.getParent();
			IPath prefix = null;
			if (parent instanceof IScriptFolder) {
				prefix = getFolderSourcePathRelativePath((IScriptFolder) parent);
			}

			if (prefix != null)
				return prefix.append(folderName);
			else
				return new Path(folderName);
		}
		return null;
	}

	/**
	 * @return the {@link IModuleSource} from full name with module dot syntax
	 */
	public static IModuleSource getModuleSource(final String name, final IScriptProject project) {
		if (project == null && name == null || name.isEmpty())
			return null;

		// search in all source path.
		IProjectFragment[] allProjectFragments;
		try {
			allProjectFragments = project.getAllProjectFragments();
			for (final IProjectFragment projectFragment : allProjectFragments) {
				final IModuleSource moduleSource = getModuleSource(name, projectFragment);
				if (moduleSource != null)
					return moduleSource;
			}
		} catch (final ModelException e) {
			Activator.logError(MessageFormat.format("Unable to find module: {0}.", name), e); //$NON-NLS-1$
			return null;
		}
		return null;
	}

	/*
	 * @return the {@link IModuleSource} from full name with module dot syntax
	 */
	private static IModuleSource getModuleSource(final String name, final IParent parent) throws ModelException {
		final IModelElement[] children = parent.getChildren();
		for (final IModelElement child : children) {
			if (child instanceof IModuleSource) {
				if (name.equals(getModuleFullName((IModuleSource) child))) {
					return (IModuleSource) child;
				}
			} else if (child instanceof IParent) {
				final IModuleSource moduleSource = getModuleSource(name, (IParent) child);
				if (moduleSource != null)
					return moduleSource;
			}

		}
		return null;
	}

	/**
	 * @return the {@link ISourceModule} from full name with module dot syntax
	 */
	public static ISourceModule getSourceModule(final String name, final IScriptProject project) {
		final IModuleSource moduleSource = getModuleSource(name, project);
		if (moduleSource instanceof ISourceModule) {
			return (ISourceModule) moduleSource;
		}
		return null;
	}

	/**
	 * @return the {@link IModuleSource} from Absolute local file URI
	 */
	public static IModuleSource getModuleSourceFromAbsoluteURI(final URI absolutepath, final IScriptProject project) {
		if (project == null || absolutepath == null)
			return null;

		final ISourceModule sourceModule = getSourceModuleFromAbsoluteURI(absolutepath, project);
		if (sourceModule instanceof IModuleSource) {
			return (IModuleSource) sourceModule;
		}
		return null;
	}

	/**
	 * @return the {@link ISourceModule} from Absolute local file URI
	 */
	public static ISourceModule getSourceModuleFromAbsoluteURI(final URI absolutepath, final IScriptProject project) {
		if (project == null || absolutepath == null)
			return null;

		// search in all source path.
		IProjectFragment[] allProjectFragments;
		try {
			allProjectFragments = project.getAllProjectFragments();
			for (final IProjectFragment projectFragment : allProjectFragments) {
				final ISourceModule moduleSource = getSourceModuleFromAbsolutePath(absolutepath, projectFragment);
				if (moduleSource != null)
					return moduleSource;
			}
		} catch (final ModelException e) {
			Activator.logError(MessageFormat.format("Unable to find module: {0}.", absolutepath), e); //$NON-NLS-1$
			return null;
		}
		return null;
	}

	/*
	 * @return the {@link ISourceModule} from Absolute local file URI and a parent
	 */
	private static ISourceModule getSourceModuleFromAbsolutePath(final URI absolutepath, final IParent parent) throws ModelException {
		final IModelElement[] children = parent.getChildren();
		for (final IModelElement child : children) {
			if (child instanceof ISourceModule) {
				if (URIUtil.sameURI(absolutepath, getModuleAbsolutePath((ISourceModule) child))) {
					return (ISourceModule) child;
				}
			} else if (child instanceof IParent) {
				final ISourceModule moduleSource = getSourceModuleFromAbsolutePath(absolutepath, (IParent) child);
				if (moduleSource != null)
					return moduleSource;
			}

		}
		return null;
	}

	/**
	 * @return Absolute local file URI of a module source
	 */
	public static URI getModuleAbsolutePath(final ISourceModule module) {
		if (module instanceof IExternalSourceModule) {
			String path = EnvironmentPathUtils.getLocalPath(module.getPath()).toString();
			if (path.length() != 0 && path.charAt(0) != '/') {
				path = '/' + path;
			}
			try {
				return new URI("file", "", path, null); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (final URISyntaxException e) {
				final String message = MessageFormat.format("Unable to get file uri for external module : {0}.", module.getPath()); //$NON-NLS-1$
				Activator.logWarning(message, e);
			}
		} else {
			if (module.getResource() != null)
				return module.getResource().getLocationURI();
		}
		return null;
	}

	/**
	 * @return the list of direct project dependencies
	 * @throws ModelException
	 */
	public static List<IScriptProject> getDependencies(final IScriptProject project) throws ModelException {
		final ArrayList<IScriptProject> result = new ArrayList<IScriptProject>();
		// check in all project fragments
		final IProjectFragment[] projectFragments = project.getAllProjectFragments();
		for (int i = 0; i < projectFragments.length; i++) {
			final IProjectFragment projectFragment = projectFragments[i];
			if (isProjectDependencyFragment(project, projectFragment)) {
				final IScriptProject currentScriptProject = projectFragment.getScriptProject();
				result.add(currentScriptProject);
			}
		}
		return result;
	}

	public static boolean isProjectDependencyFragment(final IScriptProject project, final IProjectFragment projectFragment) throws ModelException {
		final IScriptProject fragmentProject = projectFragment.getScriptProject();
		if (fragmentProject != null && fragmentProject != project) {
			return (!projectFragment.isArchive() && !projectFragment.isBinary() && !projectFragment.isExternal());
		} else {
			return false;
		}
	}

	public static boolean isExecutionEnvironmentFragment(final IProjectFragment projectFragment) throws ModelException {
		final IBuildpathEntry rawBuildpathEntry = projectFragment.getRawBuildpathEntry();
		return (rawBuildpathEntry != null && LuaExecutionEnvironmentBuildpathUtil.isValidExecutionEnvironmentBuildPath(rawBuildpathEntry.getPath()));
	}

	public enum ProjectFragmentFilter {
		EXECUTION_ENVIRONMENT, DEPENDENT_PROJECT, ARCHIVE
	}

	private static List<IProjectFragment> getProjectFragments(final IScriptProject project, EnumSet<ProjectFragmentFilter> filter,
			final IProgressMonitor monitor) throws ModelException {

		ArrayList<IProjectFragment> filteredProjecFragment = new ArrayList<IProjectFragment>();

		// filter project fragment
		final IProjectFragment[] projectFragments = project.getAllProjectFragments();
		final SubMonitor filteredLoopMonitor = SubMonitor.convert(monitor, projectFragments.length);

		for (int i = 0; i < projectFragments.length && !monitor.isCanceled(); i++) {
			final IProjectFragment projectFragment = projectFragments[i];

			if (isProjectDependencyFragment(project, projectFragment)) {
				if (filter.contains(ProjectFragmentFilter.DEPENDENT_PROJECT))
					filteredProjecFragment.add(projectFragment);
			} else if (isExecutionEnvironmentFragment(projectFragment)) {
				if (filter.contains(ProjectFragmentFilter.EXECUTION_ENVIRONMENT))
					filteredProjecFragment.add(projectFragment);
			} else if (projectFragment.isArchive()) {
				if (filter.contains(ProjectFragmentFilter.ARCHIVE))
					filteredProjecFragment.add(projectFragment);
			} else {
				filteredProjecFragment.add(projectFragment);
			}
			filteredLoopMonitor.worked(1);
		}
		return filteredProjecFragment;
	}

	/**
	 * @since 1.1
	 */
	public static void visitRootSourceFolder(final IScriptProject project, EnumSet<ProjectFragmentFilter> filter,
			final IProjectSourceRootFolderVisitor2 visitor, final IProgressMonitor monitor) throws CoreException {

		visitRootSourceFolder(project, filter, (Object) visitor, monitor);
	}

	/**
	 * Enable to perform operation in Root source folders
	 * 
	 * @deprecated Use visitRootSourceFolder(IScriptProject, EnumSet<ProjectFragmentFilter>, IProjectSourceRootFolderVisitor2, IProgressMonitor)
	 *             instead.
	 * */
	public static void visitRootSourceFolder(final IScriptProject project, EnumSet<ProjectFragmentFilter> filter,
			final IProjectSourceRootFolderVisitor visitor, final IProgressMonitor monitor) throws CoreException {

		visitRootSourceFolder(project, filter, (Object) visitor, monitor);
	}

	@SuppressWarnings("deprecation")
	private static void visitRootSourceFolder(final IScriptProject project, EnumSet<ProjectFragmentFilter> filter, final Object visitor,
			final IProgressMonitor monitor) throws CoreException {

		SubMonitor subMonitor = SubMonitor.convert(monitor, 10);

		// get filtered project fragments
		List<IProjectFragment> filteredProjecFragments = getProjectFragments(project, filter, subMonitor.newChild(1));

		// visit fragment
		final SubMonitor visitLoopMonitor = subMonitor.newChild(9).setWorkRemaining(filteredProjecFragments.size());
		for (IProjectFragment projectFragment : filteredProjecFragments) {
			if (monitor.isCanceled())
				return;

			IPath absolutePathFromModelElement = getAbsolutePathFromModelElement(projectFragment);

			SubMonitor subMonitor2 = visitLoopMonitor.newChild(1);
			if (visitor instanceof IProjectSourceRootFolderVisitor2) {
				((IProjectSourceRootFolderVisitor2) visitor).processSourceRootFolder(projectFragment, absolutePathFromModelElement, subMonitor2);
			} else if (visitor instanceof IProjectSourceRootFolderVisitor) {
				((IProjectSourceRootFolderVisitor) visitor).processSourceRootFolder(absolutePathFromModelElement, subMonitor2);
			}
		}
	}

	/**
	 * @since 1.1
	 */
	public static void visitSourceFiles(final IScriptProject project, EnumSet<ProjectFragmentFilter> filter, final IProjectSourceVisitor2 visitor,
			final IProgressMonitor monitor) throws CoreException {

		visitSourceFiles(project, filter, (Object) visitor, monitor);
	}

	/**
	 * Enable to perform operation in all files and directories in project fragments source directories
	 * 
	 * @deprecated Use visitSourceFiles(IScriptProject, EnumSet<ProjectFragmentFilter>, IProjectSourceVisitor2, IProgressMonitor) instead
	 * */
	public static void visitSourceFiles(final IScriptProject project, EnumSet<ProjectFragmentFilter> filter, final IProjectSourceVisitor visitor,
			final IProgressMonitor monitor) throws CoreException {

		visitSourceFiles(project, filter, (Object) visitor, monitor);
	}

	private static void visitSourceFiles(final IScriptProject project, EnumSet<ProjectFragmentFilter> filter, final Object visitor,
			final IProgressMonitor monitor) throws CoreException {

		SubMonitor subMonitor = SubMonitor.convert(monitor, 10);

		// get filtered project fragments
		List<IProjectFragment> filteredProjecFragments = getProjectFragments(project, filter, subMonitor.newChild(1));

		// visit fragment
		final SubMonitor visitLoopMonitor = subMonitor.newChild(9).setWorkRemaining(filteredProjecFragments.size());
		for (IProjectFragment projectFragment : filteredProjecFragments) {
			if (monitor.isCanceled())
				return;
			visitSourceFiles(projectFragment, visitor, visitLoopMonitor.newChild(1), Path.EMPTY);
		}
	}

	@SuppressWarnings("deprecation")
	private static void visitSourceFiles(final IParent parent, final Object visitor, final IProgressMonitor monitor, final IPath currentPath)
			throws CoreException {

		final IModelElement[] children = parent.getChildren();

		SubMonitor subMonitor = SubMonitor.convert(monitor, children.length);

		for (int i = 0; i < children.length && !monitor.isCanceled(); i++) {
			final IModelElement modelElement = children[i];
			if (modelElement instanceof ISourceModule) {

				/*
				 * Support local module
				 */
				IPath absolutePath = getAbsolutePathFromModelElement(modelElement);
				String charset = getCharsetOfModelElement(modelElement);

				final IPath relativeFilePath = currentPath.append(absolutePath.lastSegment());

				if (visitor instanceof IProjectSourceVisitor2) {
					((IProjectSourceVisitor2) visitor).processFile((ISourceModule) modelElement, absolutePath, relativeFilePath, charset,
							subMonitor.newChild(1));
				} else if (visitor instanceof IProjectSourceVisitor) {
					((IProjectSourceVisitor) visitor).processFile(absolutePath, relativeFilePath, charset, subMonitor.newChild(1));
				}

			} else if (modelElement instanceof IScriptFolder) {

				/*
				 * Support source folder
				 */
				final IScriptFolder innerSourceFolder = (IScriptFolder) modelElement;
				// Do not notify interface for Source folders
				if (!innerSourceFolder.isRootFolder()) {

					IPath absolutePath = getAbsolutePathFromModelElement(modelElement);
					final IPath newPath = currentPath.append(innerSourceFolder.getElementName());

					if (visitor instanceof IProjectSourceVisitor2) {
						((IProjectSourceVisitor2) visitor).processDirectory(innerSourceFolder, absolutePath, newPath, subMonitor.newChild(1));
					} else if (visitor instanceof IProjectSourceVisitor) {
						((IProjectSourceVisitor) visitor).processDirectory(absolutePath, newPath, subMonitor.newChild(1));
					}
					visitSourceFiles(innerSourceFolder, visitor, subMonitor.newChild(1), newPath);
				} else {
					// Deal with sub elements
					visitSourceFiles(innerSourceFolder, visitor, subMonitor.newChild(1), Path.EMPTY);
				}
			}
		}
	}

	private static IPath getAbsolutePathFromModelElement(final IModelElement modelElement) throws CoreException {
		final IResource resource = modelElement.getResource();
		if (resource != null) {
			return resource.getLocation();
		}

		final IPath folderPath = modelElement.getPath();
		if (EnvironmentPathUtils.isFull(folderPath)) {
			return EnvironmentPathUtils.getLocalPath(folderPath);
		}

		final String message = MessageFormat.format("Unable to get absolute location for {0}.", modelElement.getElementName()); //$NON-NLS-1$
		final Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, message);
		throw new CoreException(status);
	}

	private static String getCharsetOfModelElement(final IModelElement modelElement) throws CoreException {
		final IResource resource = modelElement.getResource();
		if (resource instanceof IFile) {
			final IFile file = (IFile) resource;
			return file.getCharset();
		}

		return Charset.defaultCharset().toString();
	}

	/**
	 * @return all Open Lua project in the workspace
	 */
	public static final IProject[] getLuaProjects() {
		List<IProject> luaProjects = new LinkedList<IProject>();
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (IProject iProject : projects) {
			try {
				if (iProject.isAccessible() && iProject.hasNature(LuaNature.ID)) {
					luaProjects.add(iProject);
				}
			} catch (CoreException e) {
				// must not append
				Activator.logWarning("Unexcepted error when collecting Lua project", e); //$NON-NLS-1$
			}
		}
		return luaProjects.toArray(new IProject[luaProjects.size()]);
	}

	public static boolean isLuaProject(IProject project) {
		try {
			return project.hasNature(LuaNature.ID);
		} catch (CoreException e) {
			// must not append
			Activator.logWarning("Unexcepted error when checking if project is a Lua project", e); //$NON-NLS-1$
		}
		return false;
	}

	/**
	 * @since 1.2
	 */
	public static IPath getLuaExecutionEnvironmentPath(IScriptProject project) {
		try {
			IBuildpathEntry[] rawBuildpath = project.getRawBuildpath();
			for (IBuildpathEntry bpe : rawBuildpath) {
				IPath path = bpe.getPath();
				if (LuaExecutionEnvironmentBuildpathUtil.isLuaExecutionEnvironmentContainer(path)) {
					return path;
				}
			}
		} catch (ModelException e) {
			Activator.logWarning(
					String.format("Unexcepted error when getting Lua Execution environement for project %s", project.getElementName()), e); //$NON-NLS-1$
		}
		return null;
	}
}
