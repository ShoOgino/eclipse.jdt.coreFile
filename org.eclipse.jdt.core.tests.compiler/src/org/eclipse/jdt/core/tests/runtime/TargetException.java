/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.runtime;

/**
 * A TargetException is thrown when an operation on a target has failed
 * for any reason.
 */
public class TargetException extends Exception {
/**
 * Constructs a <code>TargetException</code> with no detail  message.
 */
public TargetException() {
	super();
}
/**
 * Constructs a <code>TargetException</code> with the specified 
 * detail message. 
 *
 * @param   s   the detail message.
 */
public TargetException(String s) {
	super(s);
}
}
