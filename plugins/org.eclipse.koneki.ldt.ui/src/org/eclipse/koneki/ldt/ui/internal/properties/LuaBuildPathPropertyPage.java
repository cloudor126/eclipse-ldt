/*******************************************************************************
 * Copyright (c) 2009, 2012 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 ******************************************************************************/
package org.eclipse.koneki.ldt.ui.internal.properties;

import org.eclipse.dltk.core.IDLTKLanguageToolkit;
import org.eclipse.dltk.ui.preferences.BuildPathsPropertyPage;
import org.eclipse.koneki.ldt.core.internal.LuaLanguageToolkit;
import org.eclipse.ui.IWorkbenchPropertyPage;

public class LuaBuildPathPropertyPage extends BuildPathsPropertyPage implements IWorkbenchPropertyPage {
	public LuaBuildPathPropertyPage() {
	}

	public IDLTKLanguageToolkit getLanguageToolkit() {
		return LuaLanguageToolkit.getDefault();
	}
}
