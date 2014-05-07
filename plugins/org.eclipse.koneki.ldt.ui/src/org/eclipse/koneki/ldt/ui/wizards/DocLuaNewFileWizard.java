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
package org.eclipse.koneki.ldt.ui.wizards;

import org.eclipse.dltk.ui.wizards.NewSourceModulePage;
import org.eclipse.dltk.ui.wizards.NewSourceModuleWizard;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.koneki.ldt.ui.internal.Activator;
import org.eclipse.koneki.ldt.ui.internal.ImageConstants;
import org.eclipse.koneki.ldt.ui.wizards.pages.DocLuaFilePage;

/** @since 1.2 */
public class DocLuaNewFileWizard extends NewSourceModuleWizard {

	public DocLuaNewFileWizard() {
		final ImageRegistry imageRegistry = Activator.getDefault().getImageRegistry();
		setDefaultPageImageDescriptor(imageRegistry.getDescriptor(ImageConstants.DOCLUA_FILE_WIZARD_BAN));
		setDialogSettings(Activator.getDefault().getDialogSettings());
		setWindowTitle(Messages.DocLuaNewFileWizard_title);
	}

	@Override
	protected NewSourceModulePage createNewSourceModulePage() {
		return new DocLuaFilePage();
	}

}
