/*******************************************************************************
 * Copyright (c) 2011, 2013 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.ldt.core.internal.ast.models.api;

public class InlineTypeRef extends TypeRef {

	private TypeDef definition;

	public InlineTypeRef(TypeDef definition) {
		super();
		this.definition = definition;
	}

	public TypeDef getDefinition() {
		return definition;
	}

	public void setDefinition(TypeDef definition) {
		this.definition = definition;
	}

	/**
	 * @see org.eclipse.ldt.core.internal.ast.models.api.TypeRef#toReadableString()
	 */
	@Override
	public String toReadableString() {
		if (definition instanceof RecordTypeDef) {
			RecordTypeDef recordTypeDef = (RecordTypeDef) definition;
			if (recordTypeDef.getName().equals("list")) //$NON-NLS-1$
				return String.format("#list<%s>", recordTypeDef.getDefaultvaluetyperef().toReadableString()); //$NON-NLS-1$
			else if (recordTypeDef.getName().equals("map")) //$NON-NLS-1$
				return String.format("#map<%s,%s>", recordTypeDef.getDefaultkeytyperef().toReadableString(), //$NON-NLS-1$
						recordTypeDef.getDefaultvaluetyperef().toReadableString());
			else
				return "#table"; //$NON-NLS-1$
		} else if (definition instanceof FunctionTypeDef) {
			FunctionTypeDef functionTypeDef = (FunctionTypeDef) definition;
			StringBuilder sb = new StringBuilder();
			for (Parameter param : functionTypeDef.getParameters()) {
				if (sb.length() > 0) {
					sb.append(',');
				}
				sb.append(param.getType().toReadableString());
			}
			String params = sb.toString();
			sb.setLength(0);
			for (TypeRef retRef : functionTypeDef.getReturns().get(0).getTypes()) {
				if (sb.length() > 0) {
					sb.append(',');
				}
				sb.append(retRef.toReadableString());
			}
			String returns = sb.toString();
			return String.format("#(%s)->(%s)", params, returns); //$NON-NLS-1$
		} else {
			return null;
		}
	}
}
