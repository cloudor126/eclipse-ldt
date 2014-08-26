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

import org.eclipse.dltk.ui.wizards.NewSourceModulePage;
import org.eclipse.dltk.ui.wizards.NewSourceModuleWizard;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ldt.ui.internal.Activator;
import org.eclipse.ldt.ui.internal.ImageConstants;
import org.eclipse.ldt.ui.wizards.pages.LuaFilePage;

public class LuaNewFileWizard extends NewSourceModuleWizard {

	public LuaNewFileWizard() {
		final ImageRegistry imageRegistry = Activator.getDefault().getImageRegistry();
		setDefaultPageImageDescriptor(imageRegistry.getDescriptor(ImageConstants.LUA_FILE_WIZARD_BAN));
		setDialogSettings(Activator.getDefault().getDialogSettings());
		setWindowTitle(Messages.LuaNewFileWizardTitle);
	}

	@Override
	protected NewSourceModulePage createNewSourceModulePage() {
		return new LuaFilePage();
	}

}
