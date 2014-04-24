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
package org.eclipse.koneki.ldt.ui.wizards.pages;

import org.eclipse.dltk.ui.wizards.NewSourceModulePage;
import org.eclipse.koneki.ldt.core.LuaConstants;
import org.eclipse.koneki.ldt.core.LuaNature;

public class DocLuaFilePage extends NewSourceModulePage {

	@Override
	protected String getPageTitle() {
		return Messages.DocLuaFilePage_title;
	}

	@Override
	protected String getPageDescription() {
		return Messages.DocLuaFilePage_description;
	}

	@Override
	protected String getRequiredNature() {
		return LuaNature.ID;
	}

	protected String getFileName() {
		final String fileText = getFileText();

		String extension = "doclua"; //$NON-NLS-1$
		if (extension.length() > 0 && fileText.endsWith("." + extension)) { //$NON-NLS-1$
			return fileText;
		}

		return fileText + "." + extension; //$NON-NLS-1$
	}

	protected String getFileContent() {
		return String.format(LuaConstants.DOCLUA_FILE_DEFAULT_CONTENT, getFileText());
	}

}
