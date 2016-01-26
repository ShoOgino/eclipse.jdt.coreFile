/*******************************************************************************
 * Copyright (c) 2015 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.pdom.java;

import org.eclipse.jdt.internal.core.pdom.PDOM;
import org.eclipse.jdt.internal.core.pdom.PDOMNode;
import org.eclipse.jdt.internal.core.pdom.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.pdom.field.FieldSearchKey;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

/**
 * Represents the fully-qualified signature a method. Holds back-pointers to all the entities that refer to the name,
 * along with pointers to all methods that have this fully-qualified name. Note that this isn't the class declaration
 * itself. If there are multiple jar files containing a class of the same fully-qualified name, there may also be
 * multiple methods with the same method ID.
 * @since 3.12
 */
public class PDOMMethodId extends PDOMNode {
	public static final FieldSearchKey<JavaIndex> METHOD_NAME;
	public static final FieldOneToMany<PDOMMethod> METHODS;
//	public static final FieldOneToMany<PDOMType> USED_AS_FUNCTIONAL_INTERFACE_METHOD;
	public static final FieldOneToMany<PDOMType> DECLARED_TYPES;

	@SuppressWarnings("hiding")
	public static final StructDef<PDOMMethodId> type;

	static {
		type = StructDef.create(PDOMMethodId.class, PDOMNode.type);
		METHOD_NAME = FieldSearchKey.create(type, JavaIndex.METHODS);
		METHODS = FieldOneToMany.create(type, PDOMMethod.METHOD_ID, 2);
//		USED_AS_FUNCTIONAL_INTERFACE_METHOD = FieldOneToMany.create(type, PDOMType.class,
//				PDOMType.FUNCTIONAL_INTERFACE_METHOD, 0);
		DECLARED_TYPES = FieldOneToMany.create(type, PDOMType.DECLARING_METHOD);

		type.useStandardRefCounting().done();
	}

	public PDOMMethodId(PDOM pdom, long record) {
		super(pdom, record);
	}

	/**
	 *
	 * @param pdom
	 * @param methodIdentifier a field descriptor for the method type followed by a "#" followed by a method selector
	 *  followed by method descriptor. For example, "Lorg/eclipse/MyClass#foo()Ljava/lang/Object;V"
	 */
	public PDOMMethodId(PDOM pdom, char[] methodIdentifier) {
		super(pdom);

		METHOD_NAME.put(pdom, this.address, methodIdentifier);
	}
}
