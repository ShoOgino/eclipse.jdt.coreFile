/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core.util;

/**
 * Description of a source attribute as described in the JVM 
 * specifications.
 *  
 * @since 2.0
 */
public interface ISourceAttribute extends IClassFileAttribute {

	/**
	 * Answer back the source file index of this attribute.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getSourceFileIndex();

	/**
	 * Answer back the source file name of this attribute.
	 * 
	 * @return char[]
	 */
	char[] getSourceFileName();

}
