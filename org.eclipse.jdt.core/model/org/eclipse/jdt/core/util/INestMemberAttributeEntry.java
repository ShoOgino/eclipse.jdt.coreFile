/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * Description of a nest host attribute as described in the JVM
 * specifications.
 * @since 3.15 BETA_JAVA11
 */
public interface INestMemberAttributeEntry {

	/**
	 * Answer back the nest member name as specified
	 * in the JVM specifications.
	 *
	 * @return the nest member name as specified
	 * in the JVM specifications
	 */
	char[] getNestMemberName();

	/**
	 * Answer back the nest member name index.
	 *
	 * @return the nest memeber name index
	 */
	int getNestMemberIndex();
}
