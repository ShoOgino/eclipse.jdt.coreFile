/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.util;

/**
 * Description of record components attribute as described in the JVM
 * specifications.
 *
 * @since 3.20 BETA_JAVA14 
 */
public interface IRecordAttribute extends IClassFileAttribute {

	/**
	 * Answer back the number of components as specified in
	 * the JVM specifications.
	 *
	 * @return the number of components as specified in
	 * the JVM specifications
	 */
	int getNumberOfComponents();

	/**
	 * Answer back the array of record component infos as specified in
	 * the JVM specifications, or an empty array if none.
	 *
	 * @return the array of record component infos as specified in
	 * the JVM specifications, or an empty array if none
	 */
	IComponentInfo[] getComponentInfos();
}
