/*******************************************************************************
 * Copyright (c) 2011, 2012 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.ldt.core.internal.buildpath;

public interface LuaExecutionEnvironmentConstants {
	public String PREF_EXECUTION_ENVIRONMENTS_LIST = "executionEnvironments"; //$NON-NLS-1$
	public String EXECUTION_ENVIRONMENTS_LIST_SEPARATOR = ";"; //$NON-NLS-1$
	public String PREFERENCE_PAGE_ID = "org.eclipse.ldt.ui.executionenvironmentpreferencepage"; //$NON-NLS-1$
	public String FILE_EXTENSION = "*.zip"; //$NON-NLS-1$
	public String MANIFEST_EXTENSION = ".rockspec"; //$NON-NLS-1$
	public String CONTAINER_PATH_START = "org.eclipse.ldt.ExecutionEnvironmentContainer"; //$NON-NLS-1$
	public String EE_FILE_API_ARCHIVE = "api.zip"; //$NON-NLS-1$
	public String EE_FILE_DOCS_FOLDER = "docs/"; //$NON-NLS-1$
	public String EE_FILE_DOCS_INDEX = "index.html"; //$NON-NLS-1$
	public String EE_TEMPLATE_FOLDER = "templates/"; //$NON-NLS-1$
	public String EE_OLDTEMPLATE_FOLDER = "template/"; //$NON-NLS-1$
}
