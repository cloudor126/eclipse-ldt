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
package org.eclipse.koneki.ldt.core.internal.ast.models;

import org.eclipse.dltk.core.Flags;
import org.eclipse.dltk.core.IField;
import org.eclipse.dltk.core.IMember;
import org.eclipse.dltk.core.IMethod;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.ModelException;

public final class LuaDLTKModelUtils {
	private LuaDLTKModelUtils() {

	}

	public static boolean isAncestor(IModelElement element, IModelElement ancestor) {
		return ancestor != null && element != null && (ancestor.equals(element.getParent()) || isAncestor(element.getParent(), ancestor));
	}

	private static boolean isModule(int flags) {
		return (flags & Flags.AccModule) != 0;
	}

	private static boolean isPublic(int flags) {
		return (flags & Flags.AccPublic) != 0;
	}

	private static boolean isPrivate(int flags) {
		return (flags & Flags.AccPrivate) != 0;
	}

	private static boolean isTable(int flags) {
		return (flags & Flags.AccInterface) != 0;
	}

	public static boolean isModule(IMember member) throws ModelException {
		return member instanceof IType && isModule(member.getFlags());
	}

	public static boolean isType(IMember member) throws ModelException {
		return member instanceof IType && isPublic(member.getFlags());
	}

	public static boolean isModuleFunction(IMember member) throws ModelException {
		return member instanceof IMethod && isModule(member.getFlags());
	}

	public static boolean isModuleField(IMember member) throws ModelException {
		return member instanceof IField && !isTable(member.getFlags()) && isModule(member.getFlags());
	}

	public static boolean isModuleTable(IMember member) throws ModelException {
		return member instanceof IField && isTable(member.getFlags()) && isModule(member.getFlags());
	}

	public static boolean isPrivateFunction(IMember member) throws ModelException {
		return member instanceof IMethod && isPrivate(member.getFlags());
	}

	public static boolean isPrivateField(IMember member) throws ModelException {
		return member instanceof IField && !isTable(member.getFlags()) && isPrivate(member.getFlags());
	}

	public static boolean isPrivateTable(IMember member) throws ModelException {
		return member instanceof IField && isTable(member.getFlags()) && isPrivate(member.getFlags());
	}

	public static boolean isPublicFunction(IMember member) throws ModelException {
		return member instanceof IMethod && isPublic(member.getFlags());
	}

	public static boolean isPublicField(IMember member) throws ModelException {
		return member instanceof IField && !isTable(member.getFlags()) && isPublic(member.getFlags());
	}

	public static boolean isPublicTable(IMember member) throws ModelException {
		return member instanceof IField && isTable(member.getFlags()) && isPublic(member.getFlags());
	}
}
