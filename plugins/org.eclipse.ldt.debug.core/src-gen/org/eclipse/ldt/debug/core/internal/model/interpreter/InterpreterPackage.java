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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see org.eclipse.ldt.debug.core.internal.model.interpreter.InterpreterFactory
 * @model kind="package"
 * @generated
 */
public interface InterpreterPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "interpreter";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://eclipse.org/ldt";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	InterpreterPackage eINSTANCE = org.eclipse.ldt.debug.core.internal.model.interpreter.impl.InterpreterPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.ldt.debug.core.internal.model.interpreter.impl.InfoImpl <em>Info</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.ldt.debug.core.internal.model.interpreter.impl.InfoImpl
	 * @see org.eclipse.ldt.debug.core.internal.model.interpreter.impl.InterpreterPackageImpl#getInfo()
	 * @generated
	 */
	int INFO = 0;

	/**
	 * The feature id for the '<em><b>Execute Option Capable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INFO__EXECUTE_OPTION_CAPABLE = 0;

	/**
	 * The feature id for the '<em><b>File As Arguments Capable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INFO__FILE_AS_ARGUMENTS_CAPABLE = 1;

	/**
	 * The feature id for the '<em><b>Linked Execution Environment Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INFO__LINKED_EXECUTION_ENVIRONMENT_NAME = 2;

	/**
	 * The feature id for the '<em><b>Linked Execution Environment Version</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INFO__LINKED_EXECUTION_ENVIRONMENT_VERSION = 3;

	/**
	 * The number of structural features of the '<em>Info</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INFO_FEATURE_COUNT = 4;


	/**
	 * Returns the meta object for class '{@link org.eclipse.ldt.debug.core.internal.model.interpreter.Info <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Info</em>'.
	 * @see org.eclipse.ldt.debug.core.internal.model.interpreter.Info
	 * @generated
	 */
	EClass getInfo();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ldt.debug.core.internal.model.interpreter.Info#isExecuteOptionCapable <em>Execute Option Capable</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Execute Option Capable</em>'.
	 * @see org.eclipse.ldt.debug.core.internal.model.interpreter.Info#isExecuteOptionCapable()
	 * @see #getInfo()
	 * @generated
	 */
	EAttribute getInfo_ExecuteOptionCapable();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ldt.debug.core.internal.model.interpreter.Info#isFileAsArgumentsCapable <em>File As Arguments Capable</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>File As Arguments Capable</em>'.
	 * @see org.eclipse.ldt.debug.core.internal.model.interpreter.Info#isFileAsArgumentsCapable()
	 * @see #getInfo()
	 * @generated
	 */
	EAttribute getInfo_FileAsArgumentsCapable();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ldt.debug.core.internal.model.interpreter.Info#getLinkedExecutionEnvironmentName <em>Linked Execution Environment Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Linked Execution Environment Name</em>'.
	 * @see org.eclipse.ldt.debug.core.internal.model.interpreter.Info#getLinkedExecutionEnvironmentName()
	 * @see #getInfo()
	 * @generated
	 */
	EAttribute getInfo_LinkedExecutionEnvironmentName();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ldt.debug.core.internal.model.interpreter.Info#getLinkedExecutionEnvironmentVersion <em>Linked Execution Environment Version</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Linked Execution Environment Version</em>'.
	 * @see org.eclipse.ldt.debug.core.internal.model.interpreter.Info#getLinkedExecutionEnvironmentVersion()
	 * @see #getInfo()
	 * @generated
	 */
	EAttribute getInfo_LinkedExecutionEnvironmentVersion();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	InterpreterFactory getInterpreterFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link org.eclipse.ldt.debug.core.internal.model.interpreter.impl.InfoImpl <em>Info</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.ldt.debug.core.internal.model.interpreter.impl.InfoImpl
		 * @see org.eclipse.ldt.debug.core.internal.model.interpreter.impl.InterpreterPackageImpl#getInfo()
		 * @generated
		 */
		EClass INFO = eINSTANCE.getInfo();

		/**
		 * The meta object literal for the '<em><b>Execute Option Capable</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INFO__EXECUTE_OPTION_CAPABLE = eINSTANCE.getInfo_ExecuteOptionCapable();

		/**
		 * The meta object literal for the '<em><b>File As Arguments Capable</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INFO__FILE_AS_ARGUMENTS_CAPABLE = eINSTANCE.getInfo_FileAsArgumentsCapable();

		/**
		 * The meta object literal for the '<em><b>Linked Execution Environment Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INFO__LINKED_EXECUTION_ENVIRONMENT_NAME = eINSTANCE.getInfo_LinkedExecutionEnvironmentName();

		/**
		 * The meta object literal for the '<em><b>Linked Execution Environment Version</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INFO__LINKED_EXECUTION_ENVIRONMENT_VERSION = eINSTANCE.getInfo_LinkedExecutionEnvironmentVersion();

	}

} //InterpreterPackage
