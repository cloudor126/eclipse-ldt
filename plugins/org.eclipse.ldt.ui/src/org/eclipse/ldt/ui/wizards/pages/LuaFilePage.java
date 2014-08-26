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
package org.eclipse.ldt.ui.wizards.pages;

import org.eclipse.dltk.ui.wizards.NewSourceModulePage;
import org.eclipse.ldt.core.LuaNature;

public class LuaFilePage extends NewSourceModulePage {

	@Override
	protected String getPageTitle() {
		return Messages.LuaFilePageTitle;
	}

	@Override
	protected String getPageDescription() {
		return Messages.LuaFilePageDescription;
	}

	@Override
	protected String getRequiredNature() {
		return LuaNature.ID;
	}

	@Override
	protected String getFileText() {
		String fileText = super.getFileText();
		if (fileText != null)
			return fileText.trim();

		return null;
	}
}
