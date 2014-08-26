/*******************************************************************************
 * Copyright (c) 2013 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
/**
 */
package org.eclipse.ldt.debug.core.internal.model.interpreter.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.ldt.debug.core.internal.model.interpreter.Info;
import org.eclipse.ldt.debug.core.internal.model.interpreter.InterpreterPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Info</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.ldt.debug.core.internal.model.interpreter.impl.InfoImpl#isExecuteOptionCapable <em>Execute Option Capable</em>}</li>
 *   <li>{@link org.eclipse.ldt.debug.core.internal.model.interpreter.impl.InfoImpl#isFileAsArgumentsCapable <em>File As Arguments Capable</em>}</li>
 *   <li>{@link org.eclipse.ldt.debug.core.internal.model.interpreter.impl.InfoImpl#getLinkedExecutionEnvironmentName <em>Linked Execution Environment Name</em>}</li>
 *   <li>{@link org.eclipse.ldt.debug.core.internal.model.interpreter.impl.InfoImpl#getLinkedExecutionEnvironmentVersion <em>Linked Execution Environment Version</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class InfoImpl extends EObjectImpl implements Info {
	/**
	 * The default value of the '{@link #isExecuteOptionCapable() <em>Execute Option Capable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isExecuteOptionCapable()
	 * @generated
	 * @ordered
	 */
	protected static final boolean EXECUTE_OPTION_CAPABLE_EDEFAULT = true;

	/**
	 * The cached value of the '{@link #isExecuteOptionCapable() <em>Execute Option Capable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isExecuteOptionCapable()
	 * @generated
	 * @ordered
	 */
	protected boolean executeOptionCapable = EXECUTE_OPTION_CAPABLE_EDEFAULT;

	/**
	 * The default value of the '{@link #isFileAsArgumentsCapable() <em>File As Arguments Capable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isFileAsArgumentsCapable()
	 * @generated
	 * @ordered
	 */
	protected static final boolean FILE_AS_ARGUMENTS_CAPABLE_EDEFAULT = true;

	/**
	 * The cached value of the '{@link #isFileAsArgumentsCapable() <em>File As Arguments Capable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isFileAsArgumentsCapable()
	 * @generated
	 * @ordered
	 */
	protected boolean fileAsArgumentsCapable = FILE_AS_ARGUMENTS_CAPABLE_EDEFAULT;

	/**
	 * The default value of the '{@link #getLinkedExecutionEnvironmentName() <em>Linked Execution Environment Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLinkedExecutionEnvironmentName()
	 * @generated
	 * @ordered
	 */
	protected static final String LINKED_EXECUTION_ENVIRONMENT_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getLinkedExecutionEnvironmentName() <em>Linked Execution Environment Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLinkedExecutionEnvironmentName()
	 * @generated
	 * @ordered
	 */
	protected String linkedExecutionEnvironmentName = LINKED_EXECUTION_ENVIRONMENT_NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getLinkedExecutionEnvironmentVersion() <em>Linked Execution Environment Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLinkedExecutionEnvironmentVersion()
	 * @generated
	 * @ordered
	 */
	protected static final String LINKED_EXECUTION_ENVIRONMENT_VERSION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getLinkedExecutionEnvironmentVersion() <em>Linked Execution Environment Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLinkedExecutionEnvironmentVersion()
	 * @generated
	 * @ordered
	 */
	protected String linkedExecutionEnvironmentVersion = LINKED_EXECUTION_ENVIRONMENT_VERSION_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected InfoImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return InterpreterPackage.Literals.INFO;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isExecuteOptionCapable() {
		return executeOptionCapable;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setExecuteOptionCapable(boolean newExecuteOptionCapable) {
		boolean oldExecuteOptionCapable = executeOptionCapable;
		executeOptionCapable = newExecuteOptionCapable;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, InterpreterPackage.INFO__EXECUTE_OPTION_CAPABLE, oldExecuteOptionCapable, executeOptionCapable));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isFileAsArgumentsCapable() {
		return fileAsArgumentsCapable;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFileAsArgumentsCapable(boolean newFileAsArgumentsCapable) {
		boolean oldFileAsArgumentsCapable = fileAsArgumentsCapable;
		fileAsArgumentsCapable = newFileAsArgumentsCapable;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, InterpreterPackage.INFO__FILE_AS_ARGUMENTS_CAPABLE, oldFileAsArgumentsCapable, fileAsArgumentsCapable));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getLinkedExecutionEnvironmentName() {
		return linkedExecutionEnvironmentName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setLinkedExecutionEnvironmentName(String newLinkedExecutionEnvironmentName) {
		String oldLinkedExecutionEnvironmentName = linkedExecutionEnvironmentName;
		linkedExecutionEnvironmentName = newLinkedExecutionEnvironmentName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, InterpreterPackage.INFO__LINKED_EXECUTION_ENVIRONMENT_NAME, oldLinkedExecutionEnvironmentName, linkedExecutionEnvironmentName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getLinkedExecutionEnvironmentVersion() {
		return linkedExecutionEnvironmentVersion;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setLinkedExecutionEnvironmentVersion(String newLinkedExecutionEnvironmentVersion) {
		String oldLinkedExecutionEnvironmentVersion = linkedExecutionEnvironmentVersion;
		linkedExecutionEnvironmentVersion = newLinkedExecutionEnvironmentVersion;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, InterpreterPackage.INFO__LINKED_EXECUTION_ENVIRONMENT_VERSION, oldLinkedExecutionEnvironmentVersion, linkedExecutionEnvironmentVersion));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case InterpreterPackage.INFO__EXECUTE_OPTION_CAPABLE:
				return isExecuteOptionCapable();
			case InterpreterPackage.INFO__FILE_AS_ARGUMENTS_CAPABLE:
				return isFileAsArgumentsCapable();
			case InterpreterPackage.INFO__LINKED_EXECUTION_ENVIRONMENT_NAME:
				return getLinkedExecutionEnvironmentName();
			case InterpreterPackage.INFO__LINKED_EXECUTION_ENVIRONMENT_VERSION:
				return getLinkedExecutionEnvironmentVersion();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case InterpreterPackage.INFO__EXECUTE_OPTION_CAPABLE:
				setExecuteOptionCapable((Boolean)newValue);
				return;
			case InterpreterPackage.INFO__FILE_AS_ARGUMENTS_CAPABLE:
				setFileAsArgumentsCapable((Boolean)newValue);
				return;
			case InterpreterPackage.INFO__LINKED_EXECUTION_ENVIRONMENT_NAME:
				setLinkedExecutionEnvironmentName((String)newValue);
				return;
			case InterpreterPackage.INFO__LINKED_EXECUTION_ENVIRONMENT_VERSION:
				setLinkedExecutionEnvironmentVersion((String)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case InterpreterPackage.INFO__EXECUTE_OPTION_CAPABLE:
				setExecuteOptionCapable(EXECUTE_OPTION_CAPABLE_EDEFAULT);
				return;
			case InterpreterPackage.INFO__FILE_AS_ARGUMENTS_CAPABLE:
				setFileAsArgumentsCapable(FILE_AS_ARGUMENTS_CAPABLE_EDEFAULT);
				return;
			case InterpreterPackage.INFO__LINKED_EXECUTION_ENVIRONMENT_NAME:
				setLinkedExecutionEnvironmentName(LINKED_EXECUTION_ENVIRONMENT_NAME_EDEFAULT);
				return;
			case InterpreterPackage.INFO__LINKED_EXECUTION_ENVIRONMENT_VERSION:
				setLinkedExecutionEnvironmentVersion(LINKED_EXECUTION_ENVIRONMENT_VERSION_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case InterpreterPackage.INFO__EXECUTE_OPTION_CAPABLE:
				return executeOptionCapable != EXECUTE_OPTION_CAPABLE_EDEFAULT;
			case InterpreterPackage.INFO__FILE_AS_ARGUMENTS_CAPABLE:
				return fileAsArgumentsCapable != FILE_AS_ARGUMENTS_CAPABLE_EDEFAULT;
			case InterpreterPackage.INFO__LINKED_EXECUTION_ENVIRONMENT_NAME:
				return LINKED_EXECUTION_ENVIRONMENT_NAME_EDEFAULT == null ? linkedExecutionEnvironmentName != null : !LINKED_EXECUTION_ENVIRONMENT_NAME_EDEFAULT.equals(linkedExecutionEnvironmentName);
			case InterpreterPackage.INFO__LINKED_EXECUTION_ENVIRONMENT_VERSION:
				return LINKED_EXECUTION_ENVIRONMENT_VERSION_EDEFAULT == null ? linkedExecutionEnvironmentVersion != null : !LINKED_EXECUTION_ENVIRONMENT_VERSION_EDEFAULT.equals(linkedExecutionEnvironmentVersion);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (executeOptionCapable: ");
		result.append(executeOptionCapable);
		result.append(", fileAsArgumentsCapable: ");
		result.append(fileAsArgumentsCapable);
		result.append(", linkedExecutionEnvironmentName: ");
		result.append(linkedExecutionEnvironmentName);
		result.append(", linkedExecutionEnvironmentVersion: ");
		result.append(linkedExecutionEnvironmentVersion);
		result.append(')');
		return result.toString();
	}

} //InfoImpl
