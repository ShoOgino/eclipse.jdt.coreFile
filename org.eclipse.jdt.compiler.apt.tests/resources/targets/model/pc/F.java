/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package targets.model.pc;

/**
 * Javadoc on element F
 * @param <T1> a type parameter
 */
@AnnoY("on F")
public class F<T1> {
	/**
	 * Javadoc on nested element FChild
	 */  
	public class FChild {
	}
	
	/**
	 * Javadoc on nested enum FEnum
	 * Two lines long
	 */
	enum FEnum { FEnum1, FEnum2 }

	/**
	 * Javadoc on nested interface FChildI
	 */
	public interface FChildI {}
	
	/** Javadoc on field _fieldT1_protected, inline format */
	protected T1 _fieldT1_protected;
	
	private T1 _fieldT1_private;
	
	int fieldInt;
	
	/**
	 * Javadoc on F.method_T1
	 */
	@AnnoY("on F.method_T1")
	T1 method_T1(T1 param1) 
	{
		return null;
	}
	
	String method_String(T1 param1)
	{
		_fieldT1_private = param1;
		return _fieldT1_private.toString();
	}
	
	// not hidden by G.staticMethod() - private
	private static void staticMethod()
	{
	}

	// not hidden by G.staticMethod - different name
	public static void staticMethod2()
	{
	}

	@SuppressWarnings("deprecation")
	@Deprecated
	void deprecatedMethod()
	{
	}
	
	// unrelated to the same-signature methods in Overriding.java
	void f() {}
}