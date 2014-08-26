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
package org.eclipse.ldt.debug.core.internal.model.interpreter;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Info</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.ldt.debug.core.internal.model.interpreter.Info#isExecuteOptionCapable <em>Execute Option Capable</em>}</li>
 *   <li>{@link org.eclipse.ldt.debug.core.internal.model.interpreter.Info#isFileAsArgumentsCapable <em>File As Arguments Capable</em>}</li>
 *   <li>{@link org.eclipse.ldt.debug.core.internal.model.interpreter.Info#getLinkedExecutionEnvironmentName <em>Linked Execution Environment Name</em>}</li>
 *   <li>{@link org.eclipse.ldt.debug.core.internal.model.interpreter.Info#getLinkedExecutionEnvironmentVersion <em>Linked Execution Environment Version</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.ldt.debug.core.internal.model.interpreter.InterpreterPackage#getInfo()
 * @model
 * @generated
 */
public interface Info extends EObject {
	/**
	 * Returns the value of the '<em><b>Execute Option Capable</b></em>' attribute.
	 * The default value is <code>"true"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Execute Option Capable</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Execute Option Capable</em>' attribute.
	 * @see #setExecuteOptionCapable(boolean)
	 * @see org.eclipse.ldt.debug.core.internal.model.interpreter.InterpreterPackage#getInfo_ExecuteOptionCapable()
	 * @model default="true" required="true"
	 * @generated
	 */
	boolean isExecuteOptionCapable();

	/**
	 * Sets the value of the '{@link org.eclipse.ldt.debug.core.internal.model.interpreter.Info#isExecuteOptionCapable <em>Execute Option Capable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Execute Option Capable</em>' attribute.
	 * @see #isExecuteOptionCapable()
	 * @generated
	 */
	void setExecuteOptionCapable(boolean value);

	/**
	 * Returns the value of the '<em><b>File As Arguments Capable</b></em>' attribute.
	 * The default value is <code>"true"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>File As Arguments Capable</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>File As Arguments Capable</em>' attribute.
	 * @see #setFileAsArgumentsCapable(boolean)
	 * @see org.eclipse.ldt.debug.core.internal.model.interpreter.InterpreterPackage#getInfo_FileAsArgumentsCapable()
	 * @model default="true"
	 * @generated
	 */
	boolean isFileAsArgumentsCapable();

	/**
	 * Sets the value of the '{@link org.eclipse.ldt.debug.core.internal.model.interpreter.Info#isFileAsArgumentsCapable <em>File As Arguments Capable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>File As Arguments Capable</em>' attribute.
	 * @see #isFileAsArgumentsCapable()
	 * @generated
	 */
	void setFileAsArgumentsCapable(boolean value);

	/**
	 * Returns the value of the '<em><b>Linked Execution Environment Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Linked Execution Environment Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Linked Execution Environment Name</em>' attribute.
	 * @see #setLinkedExecutionEnvironmentName(String)
	 * @see org.eclipse.ldt.debug.core.internal.model.interpreter.InterpreterPackage#getInfo_LinkedExecutionEnvironmentName()
	 * @model
	 * @generated
	 */
	String getLinkedExecutionEnvironmentName();

	/**
	 * Sets the value of the '{@link org.eclipse.ldt.debug.core.internal.model.interpreter.Info#getLinkedExecutionEnvironmentName <em>Linked Execution Environment Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Linked Execution Environment Name</em>' attribute.
	 * @see #getLinkedExecutionEnvironmentName()
	 * @generated
	 */
	void setLinkedExecutionEnvironmentName(String value);

	/**
	 * Returns the value of the '<em><b>Linked Execution Environment Version</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Linked Execution Environment Version</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Linked Execution Environment Version</em>' attribute.
	 * @see #setLinkedExecutionEnvironmentVersion(String)
	 * @see org.eclipse.ldt.debug.core.internal.model.interpreter.InterpreterPackage#getInfo_LinkedExecutionEnvironmentVersion()
	 * @model
	 * @generated
	 */
	String getLinkedExecutionEnvironmentVersion();

	/**
	 * Sets the value of the '{@link org.eclipse.ldt.debug.core.internal.model.interpreter.Info#getLinkedExecutionEnvironmentVersion <em>Linked Execution Environment Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Linked Execution Environment Version</em>' attribute.
	 * @see #getLinkedExecutionEnvironmentVersion()
	 * @generated
	 */
	void setLinkedExecutionEnvironmentVersion(String value);

} // Info
