/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
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
package org.eclipse.jdt.internal.core.util;

import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.core.util.IConstantPool;
import org.eclipse.jdt.core.util.IConstantPoolConstant;
import org.eclipse.jdt.core.util.IConstantPoolEntry;
import org.eclipse.jdt.core.util.IModuleMainClassAttribute;

/**
 * Default implementation of IModuleMainClassAttribute
 */
public class ModuleMainClassAttribute extends ClassFileAttribute implements IModuleMainClassAttribute {

	private final int mainClassIndex;
	private final char[] mainClassName;

	/**
	 * Constructor for ModuleMainClassAttribute.
	 * @param classFileBytes
	 * @param constantPool
	 * @param offset
	 * @throws ClassFormatException
	 */
	public ModuleMainClassAttribute(	byte[] classFileBytes,	IConstantPool constantPool,	int offset)	throws ClassFormatException {
		super(classFileBytes, constantPool, offset);
		int readOffset = 6;
		this.mainClassIndex = u2At(classFileBytes, readOffset, offset);
		readOffset += 2;
		int index = u2At(classFileBytes, readOffset, offset);
		IConstantPoolEntry constantPoolEntry = constantPool.decodeEntry(index);
		if (constantPoolEntry.getKind() != IConstantPoolConstant.CONSTANT_Class) {
			throw new ClassFormatException(ClassFormatException.INVALID_CONSTANT_POOL_ENTRY);
		}
		this.mainClassName = constantPoolEntry.getClassInfoName();
	}

	@Override
	public int getMainClassIndex() {
		return this.mainClassIndex;
	}

	@Override
	public char[] getMainClassName() {
		return this.mainClassName;
	}
}