/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
     IBM Corporation - initial API and implementation
**********************************************************************/
package org.eclipse.jdt.internal.core.util;

import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.core.util.IAttributeNamesConstants;
import org.eclipse.jdt.core.util.IConstantPool;
import org.eclipse.jdt.core.util.IConstantPoolConstant;
import org.eclipse.jdt.core.util.IConstantPoolEntry;
import org.eclipse.jdt.core.util.IConstantValueAttribute;
import org.eclipse.jdt.core.util.IFieldInfo;
import org.eclipse.jdt.internal.compiler.codegen.AttributeNamesConstants;
import org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.eclipse.jdt.internal.compiler.impl.BooleanConstant;
import org.eclipse.jdt.internal.compiler.impl.ByteConstant;
import org.eclipse.jdt.internal.compiler.impl.CharConstant;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.DoubleConstant;
import org.eclipse.jdt.internal.compiler.impl.FloatConstant;
import org.eclipse.jdt.internal.compiler.impl.IntConstant;
import org.eclipse.jdt.internal.compiler.impl.LongConstant;
import org.eclipse.jdt.internal.compiler.impl.ShortConstant;
import org.eclipse.jdt.internal.compiler.impl.StringConstant;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

/**
 * Default implementation of IFieldInfo.
 */
public class FieldInfo extends ClassFileStruct implements IFieldInfo {
	private boolean isDeprecated;
	private boolean isSynthetic;
	private int accessFlags;
	private char[] name;
	private char[] descriptor;
	private int nameIndex;
	private int descriptorIndex;
	private int attributesCount;
	private int attributeBytes;
	private IConstantValueAttribute constantValueAttribute;
	
	/**
	 * @param classFileBytes byte[]
	 * @param offsets int[]
	 * @param offset int
	 */
	public FieldInfo(byte classFileBytes[], IConstantPool constantPool, int offset)
		throws ClassFormatException {
		this.accessFlags = u2At(classFileBytes, 0, offset);

		this.nameIndex = u2At(classFileBytes, 2, offset);
		IConstantPoolEntry constantPoolEntry = constantPool.decodeEntry(this.nameIndex);
		if (constantPoolEntry.getKind() != IConstantPoolConstant.CONSTANT_Utf8) {
			throw new ClassFormatException(ClassFormatException.INVALID_CONSTANT_POOL_ENTRY);
		}
		this.name = constantPoolEntry.getUtf8Value();

		this.descriptorIndex = u2At(classFileBytes, 4, offset);
		constantPoolEntry = constantPool.decodeEntry(this.descriptorIndex);
		if (constantPoolEntry.getKind() != IConstantPoolConstant.CONSTANT_Utf8) {
			throw new ClassFormatException(ClassFormatException.INVALID_CONSTANT_POOL_ENTRY);
		}
		this.descriptor = constantPoolEntry.getUtf8Value();

		this.attributesCount = u2At(classFileBytes, 6, offset);
		int readOffset = 8;

		for (int i = 0; i < attributesCount; i++) {
			constantPoolEntry = constantPool.decodeEntry(u2At(classFileBytes, readOffset, offset));
			if (constantPoolEntry.getKind() != IConstantPoolConstant.CONSTANT_Utf8) {
				throw new ClassFormatException(ClassFormatException.INVALID_CONSTANT_POOL_ENTRY);
			}
			char[] attributeName = constantPoolEntry.getUtf8Value();
			if (equals(attributeName, IAttributeNamesConstants.DEPRECATED)) {
				this.isDeprecated = true;
			} else if (equals(attributeName, IAttributeNamesConstants.SYNTHETIC)) {
				this.isSynthetic = true;
			} else if (equals(attributeName, IAttributeNamesConstants.CONSTANT_VALUE)) {
				this.constantValueAttribute = new ConstantValueAttribute(classFileBytes, constantPool, offset + readOffset);
			}
			readOffset += (6 + u4At(classFileBytes, readOffset + 2, offset));
		}

		attributeBytes = readOffset;
	}
	/**
	 * @see IFieldInfo#getAccessFlags()
	 */
	public int getAccessFlags() {
		return this.accessFlags;
	}

	/**
	 * @see IFieldInfo#getConstantValueAttribute()
	 */
	public IConstantValueAttribute getConstantValueAttribute() {
		return this.constantValueAttribute;
	}

	/**
	 * @see IFieldInfo#getDescriptor()
	 */
	public char[] getDescriptor() {
		return this.descriptor;
	}

	/**
	 * @see IFieldInfo#getName()
	 */
	public char[] getName() {
		return this.name;
	}

	/**
	 * @see IFieldInfo#hasConstantValueAttribute()
	 */
	public boolean hasConstantValueAttribute() {
		return this.constantValueAttribute != null;
	}

	/**
	 * @see IFieldInfo#isDeprecated()
	 */
	public boolean isDeprecated() {
		return this.isDeprecated;
	}

	/**
	 * @see IFieldInfo#isSynthetic()
	 */
	public boolean isSynthetic() {
		return this.isSynthetic;
	}

	int sizeInBytes() {
		return attributeBytes;
	}
	/**
	 * @see IFieldInfo#getAttributeCount()
	 */
	public int getAttributeCount() {
		return this.attributesCount;
	}

	/**
	 * @see IFieldInfo#getDescriptorIndex()
	 */
	public int getDescriptorIndex() {
		return this.descriptorIndex;
	}

	/**
	 * @see IFieldInfo#getNameIndex()
	 */
	public int getNameIndex() {
		return this.nameIndex;
	}

}