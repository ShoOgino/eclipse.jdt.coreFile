/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class Deprecated18Test extends AbstractRegressionTest {
public Deprecated18Test(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_8);
}
public void test412555() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		Runnable r = () -> {\n" + 
			"			Y.callMe();\n" +
			"		};\n" +
			"	}\n" +
			"}\n",
			"Y.java",
			"public class Y {\n" +
			"	@Deprecated\n" +
			 "	public static void callMe() {}\n" +
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	Y.callMe();\n" + 
		"	  ^^^^^^^^\n" + 
		"The method callMe() from the type Y is deprecated\n" + 
		"----------\n",
		null,
		true,
		options);
}
public static Class testClass() {
	return Deprecated18Test.class;
}
}
