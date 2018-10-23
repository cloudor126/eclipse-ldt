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
package org.eclipse.ldt.core.internal.ast.models.api;

public class MetaTypeRef extends LazyTypeRef {

	private int index;

	public MetaTypeRef(int index) {
		super();
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	/**
	 * @see org.eclipse.ldt.core.internal.ast.models.api.LazyTypeRef#toReadableString()
	 */
	@Override
	public String toReadableString() {
		return "$" + index; //$NON-NLS-1$
	}
}