/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
     IBM Corporation - initial API and implementation
**********************************************************************/
package org.eclipse.jdt.core.util;

/**
 * Description of a constant value attribute as described in the the JVM 
 * specifications.
 *  
 * @since 2.0
 */
public interface IExceptionAttribute extends IClassFileAttribute {

	/**
	 * Answer back the number of exceptions of the exception attribute.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getExceptionsNumber();

	/**
	 * Answer back the exception names of the exception attribute.
	 * 
	 * @return char[][]
	 */
	char[][] getExceptionNames();

	/**
	 * Answer back the exception indexes of the exception attribute.
	 * 
	 * @return int[]
	 */
	int[] getExceptionIndexes();
}
