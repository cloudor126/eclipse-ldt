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
package org.eclipse.ldt.ui.internal.compare;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.dltk.ui.compare.ScriptMergeViewer;
import org.eclipse.ldt.core.LuaNature;
import org.eclipse.swt.widgets.Composite;

public class LuaMergeViewer extends ScriptMergeViewer {

	public LuaMergeViewer(Composite parent, CompareConfiguration configuration) {
		super(parent, configuration, LuaNature.ID);
	}

}
