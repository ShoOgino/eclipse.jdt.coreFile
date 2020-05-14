/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class PatternMatching15Test extends AbstractRegressionTest {

	private static final JavacTestOptions JAVAC_OPTIONS = new JavacTestOptions("-source 15 --enable-preview -Xlint:-preview");
	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "testBug562392" };
	}

	public static Class<?> testClass() {
		return PatternMatching15Test.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_15);
	}
	public PatternMatching15Test(String testName){
		super(testName);
	}
	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions(boolean preview) {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
		defaultOptions.put(CompilerOptions.OPTION_EnablePreviews,
				preview ? CompilerOptions.ENABLED : CompilerOptions.DISABLED);
		defaultOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.WARNING);
		return defaultOptions;
	}

	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions) {
		runConformTest(testFiles, expectedOutput, customOptions, new String[] {"--enable-preview"}, JAVAC_OPTIONS);
	}
	protected void runNegativeTest(
			String[] testFiles,
			String expectedCompilerLog,
			String javacLog,
			String[] classLibraries,
			boolean shouldFlushOutputDirectory,
			Map<String, String> customOptions) {
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedCompilerLog = expectedCompilerLog;
		runner.javacTestOptions = JAVAC_OPTIONS;
		runner.customOptions = customOptions;
		runner.expectedJavacOutputString = javacLog;
		runner.runNegativeTest();
	}
	public void test001() {
		Map<String, String> options = getCompilerOptions(false);
		runNegativeTest(
				new String[] {
						"X1.java",
						"public class X1 {\n" +
						"  public void foo(Object obj) {\n" +
						"		if (obj instanceof String s) {\n" +
						"		}\n " +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X1.java (at line 3)\n" +
				"	if (obj instanceof String s) {\n" +
				"	                   ^^^^^^^^\n" +
				"Instanceof Pattern is a preview feature and disabled by default. Use --enable-preview to enable\n" +
				"----------\n",
				/* omit one arg to directly call super method without JAVA_OPTIONS */
				null,
				true,
				options);
	}
	public void test002() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X2.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X2 {\n" +
						"  public void foo(Integer obj) {\n" +
						"		if (obj instanceof String s) {\n" +
						"		}\n " +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X2.java (at line 4)\n" +
				"	if (obj instanceof String s) {\n" +
				"	    ^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Incompatible conditional operand types Integer and String\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test003() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X3.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X3 {\n" +
						"  public void foo(Number num) {\n" +
						"		if (num instanceof Integer s) {\n" +
						"		} else if (num instanceof String) {\n" +
						"		}\n " +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X3.java (at line 5)\n" +
				"	} else if (num instanceof String) {\n" +
				"	           ^^^^^^^^^^^^^^^^^^^^^\n" +
				"Incompatible conditional operand types Number and String\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test003a() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X3.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X3 {\n" +
						"  public void foo(Number num) {\n" +
						"		if (num instanceof int) {\n" +
						"		}\n " +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X3.java (at line 4)\n" +
				"	if (num instanceof int) {\n" +
				"	    ^^^^^^^^^^^^^^^^^^\n" +
				"Incompatible conditional operand types Number and int\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test004() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X4.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X4 {\n" +
						"  public void foo(Object obj) {\n" +
						"		String s = null;\n" +
						"		if (obj instanceof Integer s) {\n" +
						"		} else if (obj instanceof String) {\n" +
						"		}\n " +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X4.java (at line 5)\n" +
				"	if (obj instanceof Integer s) {\n" +
				"	                           ^\n" +
				"Duplicate local variable s\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test005() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X5.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X5 {\n" +
						"@SuppressWarnings(\"preview\")\n" +
						"  public static void foo(Object obj) {\n" +
						"		if (obj instanceof Integer i) {\n" +
						"			System.out.print(i);\n" +
						"		} else if (obj instanceof String s) {\n" +
						"			System.out.print(s);\n" +
						"		}\n " +
						"	}\n" +
						"  public static void main(String[] obj) {\n" +
						"		foo(100);\n" +
						"	}\n" +
						"}\n",
				},
				"100",
				options);
	}
	public void test006() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X6.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X6 {\n" +
						"  public static void foo(Object obj) {\n" +
						"		if (obj instanceof Integer i) {\n" +
						"			System.out.print(i);\n" +
						"		} else if (obj instanceof String s) {\n" +
						"			System.out.print(s);\n" +
						"		}\n " +
						"	}\n" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"abcd",
				options);
	}
	public void test006a() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X6a.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X6a {\n" +
						"	public static void foo(Object obj) {\n" +
						"		if (obj != null) {\n" +
						"			if (obj instanceof Integer i) {\n" +
						"				System.out.print(i);\n" +
						"			} else if (obj instanceof String s) {\n" +
						"				System.out.print(i);\n" +
						"			}\n " +
						"		}\n " +
						"		System.out.print(i);\n" +
						"	}\n" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X6a.java (at line 8)\n" +
				"	System.out.print(i);\n" +
				"	                 ^\n" +
				"The pattern variable i is not in scope in this location\n" +
				"----------\n" +
				"2. ERROR in X6a.java (at line 11)\n" +
				"	System.out.print(i);\n" +
				"	                 ^\n" +
				"The pattern variable i is not in scope in this location\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test006b() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X6b.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X6b {\n" +
						"	public static void foo(Object obj) {\n" +
						"		if (obj != null) {\n" +
						"			if (obj instanceof Integer i) {\n" +
						"				System.out.print(i);\n" +
						"			} else if (obj instanceof String s) {\n" +
						"				System.out.print(i);\n" +
						"			}\n " +
						"		}\n " +
						"		System.out.print(s);\n" +
						"	}\n" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X6b.java (at line 8)\n" +
				"	System.out.print(i);\n" +
				"	                 ^\n" +
				"The pattern variable i is not in scope in this location\n" +
				"----------\n" +
				"2. ERROR in X6b.java (at line 11)\n" +
				"	System.out.print(s);\n" +
				"	                 ^\n" +
				"The pattern variable s is not in scope in this location\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test006c() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X6c.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X6c {\n" +
						"  public static void foo(Object obj) {\n" +
						"		if (obj instanceof Integer i) {\n" +
						"			System.out.print(i);\n" +
						"		} else if (obj instanceof String s) {\n" +
						"			System.out.print(i);\n" +
						"		}\n " +
						"	}\n" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X6c.java (at line 7)\n" +
				"	System.out.print(i);\n" +
				"	                 ^\n" +
				"The pattern variable i is not in scope in this location\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test006d() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X6d.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X6d {\n" +
						"  public static void foo(Object obj) {\n" +
						"		if (obj instanceof Integer i) {\n" +
						"			System.out.print(i);\n" +
						"		} else {\n" +
						"			System.out.print(i);\n" +
						"		}\n " +
						"	}\n" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X6d.java (at line 7)\n" +
				"	System.out.print(i);\n" +
				"	                 ^\n" +
				"The pattern variable i is not in scope in this location\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test007() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X7.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X7 {\n" +
						"  public static void foo(Object obj) {\n" +
						"		if (obj instanceof Integer i) {\n" +
						"			System.out.print(i);\n" +
						"		} else if (obj instanceof String s) {\n" +
						"			System.out.print(i);\n" +
						"		}\n " +
						"	}\n" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X7.java (at line 7)\n" +
				"	System.out.print(i);\n" +
				"	                 ^\n" +
				"The pattern variable i is not in scope in this location\n" +
				"----------\n",
				"X7.java:4: warning: [preview] pattern matching in instanceof is a preview feature and may be removed in a future release.\n" +
				"		if (obj instanceof Integer i) {\n" +
				"		                           ^\n" +
				"X7.java:6: warning: [preview] pattern matching in instanceof is a preview feature and may be removed in a future release.\n" +
				"		} else if (obj instanceof String s) {\n" +
				"		                                 ^\n" +
				"X7.java:7: error: cannot find symbol\n" +
				"			System.out.print(i);\n" +
				"			                 ^\n" +
				"  symbol:   variable i\n" +
				"  location: class X7\n" +
				"1 error\n" +
				"2 warnings",
				null,
				true,
				options);
	}
	public void test008() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X8.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X8 {\n" +
						"  	public static void foo(Object b) {\n" +
						"		Object c = null;\n" +
						"		if (b != c) {\n" +
						"			if ((b instanceof String s) && (s.length() != 0))\n" +
						"				System.out.println(\"s:\" + s);\n" +
						"			else \n" +
						"				System.out.println(\"b:\" + b);\n" +
						"		}\n" +
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(100);\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"b:100\n" +
				"s:abcd",
				options);
	}
	public void test009() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X9.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X9 {\n" +
						"  	public static void foo(Object b) {\n" +
						"		Object c = null;\n" +
						"		if (b != c) {\n" +
						"			if ((b instanceof String s) && (s.length() != 0))\n" +
						"				System.out.println(\"s:\" + s);\n" +
						"			else if ((b instanceof Integer i2))\n" +
						"				System.out.println(\"i2:\" + i2);\n" +
						"		}\n" +
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(100);\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"i2:100\n" +
				"s:abcd",
				options);
	}
	public void test010() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X10.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X10 {\n" +
						"  	public static void foo(Object b) {\n" +
						"		Object c = null;\n" +
						"		if (b != c) {\n" +
						"			if (b != null && (b instanceof String s))\n" +
						"				System.out.println(\"s:\" + s);\n" +
						"			else " +
						"				System.out.println(\"b:\" + b);\n" +
						"		}\n" +
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(100);\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"b:100\n" +
				"s:abcd",
				options);
	}
	public void test011() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X11.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X11 {\n" +
						"  	public static void foo(Object b) {\n" +
						"		Object c = null;\n" +
						"		if (b == null && (b instanceof String s)) {\n" +
						"		} else {" +
						"		}\n" +
						"		System.out.println(s);\n" +
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(100);\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X11.java (at line 7)\n" +
				"	System.out.println(s);\n" +
				"	                   ^\n" +
				"The pattern variable s is not in scope in this location\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test012() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X12.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X12 {\n" +
						"  	public static void foo(Object b) {\n" +
						"		Object c = new Object();\n" +
						"		if (b != c) {\n" +
						"			if (b == null && (b instanceof String s)) {\n" +
						"				System.out.println(\"s:\" + s);\n" +
						"			} else {\n" +
						"				System.out.println(\"b:\" + b);\n" +
						"			}\n" +
						"			s = null;\n" +
						"		}\n" +
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(100);\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X12.java (at line 11)\n" +
				"	s = null;\n" +
				"	^\n" +
				"The pattern variable s is not in scope in this location\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test013() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X13.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X13 {\n" +
						"  	public static void foo(Object b) {\n" +
						"		Object c = null;\n" +
						"		if (b != c) {\n" +
						"			if (b == null && (b instanceof String s))\n" +
						"				System.out.println(\"s:\" + s);\n" +
						"			else " +
						"				System.out.println(\"b:\" + b);\n" +
						"			System.out.println(s);\n" +
						"		}\n" +
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(100);\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. WARNING in X13.java (at line 7)\n" +
				"	System.out.println(\"s:\" + s);\n" +
				"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Dead code\n" +
				"----------\n" +
				"2. ERROR in X13.java (at line 9)\n" +
				"	System.out.println(s);\n" +
				"	                   ^\n" +
				"The pattern variable s is not in scope in this location\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test014() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X14.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X14 {\n" +
						"  	public static void foo(Object o) {\n" +
						"		if (!(o instanceof String s)) {\n" +
						"			System.out.print(\"then:\" + s);\n" +
						"		} else {\n" +
						"			System.out.print(\"else:\" + s);\n" +
						"		}\n" +
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X14.java (at line 5)\n" +
				"	System.out.print(\"then:\" + s);\n" +
				"	                           ^\n" +
				"The pattern variable s is not in scope in this location\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test014a() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X14a.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X14a {\n" +
						"  	public static void foo(Object o) {\n" +
						"		if (!(o instanceof String s)) {\n" +
						"			System.out.print(\"then:\" + s);\n" +
						"		} else {\n" +
						"			System.out.print(\"else:\" + s);\n" +
						"		}\n" +
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X14a.java (at line 5)\n" +
				"	System.out.print(\"then:\" + s);\n" +
				"	                           ^\n" +
				"The pattern variable s is not in scope in this location\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test014b() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X14b.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X14b {\n" +
						"  	public static void foo(Object o) {\n" +
						"		if (!!(o instanceof String s)) {\n" +
						"			System.out.print(\"then:\" + s);\n" +
						"		} else {\n" +
						"			System.out.print(\"else:\" + s);\n" +
						"		}\n" +
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X14b.java (at line 7)\n" +
				"	System.out.print(\"else:\" + s);\n" +
				"	                           ^\n" +
				"The pattern variable s is not in scope in this location\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test014c() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X14c.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X14c {\n" +
						"  	public static void foo(Object o) {\n" +
						"		if (o == null) {\n" +
						"			System.out.print(\"null\");\n" +
						"		} else if(!(o instanceof String s)) {\n" +
						"			System.out.print(\"else:\" + s);\n" +
						"		}\n" +
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X14c.java (at line 7)\n" +
				"	System.out.print(\"else:\" + s);\n" +
				"	                           ^\n" +
				"The pattern variable s is not in scope in this location\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test014d() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X14d.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X14d {\n" +
						"  	public static void foo(Object o) {\n" +
						"		if (o == null) {\n" +
						"			System.out.print(\"null\");\n" +
						"		} else if(!!(o instanceof String s)) {\n" +
						"			System.out.print(\"else:\" + s);\n" +
						"		}\n" +
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"else:abcd",
				options);
	}
	public void test014e() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X14a.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X14a {\n" +
						"  	public static void foo(Object o) {\n" +
						"		 if (!(!(o instanceof String s))) {\n" +
						"			System.out.print(\"s:\" + s);\n" +
						"		} else {\n" +
						"		}\n" +
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"s:abcd",
				options);
	}
	/*
	 * Test that when pattern tests for false and if doesn't complete
	 * normally, then the variable is available beyond the if statement
	 */
	public void test015() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X15.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X15 {\n" +
						"  	public static void foo(Object o) {\n" +
						"		if (!(o instanceof String s)) {\n" +
						"			throw new IllegalArgumentException();\n" +
						"		} else {\n" +
						"			System.out.print(\"s:\" + s);\n" +
						"		}\n" +
						"		System.out.print(s);\n" +
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"s:abcdabcd",
				options);
	}
	/*
	 * Test that when pattern tests for false and if doesn't complete
	 * normally, then the variable is available beyond the if statement
	 */
	public void test015a() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X15a.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X15a {\n" +
						"  	public static void foo(Object o) {\n" +
						"		if (!(o instanceof String s)) {\n" +
						"			throw new IllegalArgumentException();\n" +
						"		}\n" +
						"		System.out.print(s);\n" +
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"abcd",
				options);
	}
	/*
	 * Test that when pattern tests for false and if completes
	 * normally, then the variable is not available beyond the if statement
	 */
	public void test015b() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X15b.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X15b {\n" +
						"  	public static void foo(Object o) {\n" +
						"		if (!(o instanceof String s)) {\n" +
						"			//throw new IllegalArgumentException();\n" +
						"		} else {\n" +
						"			System.out.print(\"s:\" + s);\n" +
						"		}\n" +
						"		System.out.print(s);\n" +
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X15b.java (at line 9)\n" +
				"	System.out.print(s);\n" +
				"	                 ^\n" +
				"The pattern variable s is not in scope in this location\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test016() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X16.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X16 {\n" +
						"  	public static void foo(Object o) {\n" +
						"		boolean b = (o instanceof String[] s && s.length == 1);\n" +
						"		System.out.print(b);\n" +
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(new String[]{\"one\"});\n" +
						"	}\n" +
						"}\n",
				},
				"true",
				options);
	}
	public void test017() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X17.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X17 {\n" +
						"  	public static void foo(Object o) {\n" +
						"		boolean b = (o instanceof String[] s && s.length == 1);\n" +
						"		System.out.print(s[0]);\n" +
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(new String[]{\"one\"});\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X17.java (at line 5)\n" +
				"	System.out.print(s[0]);\n" +
				"	                 ^\n" +
				"The pattern variable s is not in scope in this location\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	/* Test that the scopes of pattern variable in a block doesn't affect
	 * another outside but declared after the block
	 */
	public void test018() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X18.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X18 {\n" +
						"  public static void main(String[] obj) {\n" +
						"		boolean a = true;\n" +
						"		{\n" +
						"			boolean b = (obj instanceof String[] s && s.length == 0);\n" +
						"			System.out.print(b + \",\");\n" +
						"		}\n" +
						"		boolean b = a ? false : (obj instanceof String[] s && s.length == 0);\n" +
						"		System.out.print(b);\n" +
						"	}\n" +
						"}\n",
				},
				"true,false",
				options);
	}
	/* Test that the scopes of pattern variable in a block doesn't affect
	 * another outside but declared before the block
	 */
	public void test019() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X19.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X19 {\n" +
						"  public static void main(String[] obj) {\n" +
						"		boolean a = true;\n" +
						"		boolean b = a ? false : (obj instanceof String[] s && s.length == 0);\n" +
						"		System.out.print(b + \",\");\n" +
						"		{\n" +
						"			b = (obj instanceof String[] s && s.length == 0);\n" +
						"			System.out.print(b);\n" +
						"		}\n" +
						"	}\n" +
						"}\n",
				},
				"false,true",
				options);
	}
	/* Test that we still detect duplicate pattern variable declarations
	 */
	public void test019b() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X19b.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X19b {\n" +
						"  public static void main(String[] obj) {\n" +
						"		boolean a = true;\n" +
						"		if (obj instanceof String[] s && s.length == 0) {\n" +
						"			boolean b = (obj instanceof String[] s && s.length == 0);\n" +
						"			System.out.print(b);\n" +
						"		}\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X19b.java (at line 6)\n" +
				"	boolean b = (obj instanceof String[] s && s.length == 0);\n" +
				"	                                     ^\n" +
				"Duplicate local variable s\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	/* Test that we allow consequent pattern expressions in the same statement
	 */
	public void test020() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X20.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X20 {\n" +
						"  public static void main(String[] o) {\n" +
						"		boolean b = (o instanceof String[] s) && s instanceof String[] s2;\n" +
						"		System.out.print(b);\n" +
						"	}\n" +
						"}\n",
				},
				"true",
				options);
	}
	/* Test that we allow consequent pattern expressions in the same statement
	 */
	public void test021() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X21.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X21 {\n" +
						"  public static void main(String[] o) {\n" +
						"		boolean b = (o instanceof String[] s) && s instanceof String[] s2;\n" +
						"		System.out.print(s);\n" +
						"		System.out.print(s2);\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X21.java (at line 5)\n" +
				"	System.out.print(s);\n" +
				"	                 ^\n" +
				"The pattern variable s is not in scope in this location\n" +
				"----------\n" +
				"2. ERROR in X21.java (at line 6)\n" +
				"	System.out.print(s2);\n" +
				"	                 ^^\n" +
				"The pattern variable s2 is not in scope in this location\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	/* Test that we allow pattern expressions in a while statement
	 */
	public void test022() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X22.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X22 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" +
						"	}\n" +
						"  public static void foo(Object o) {\n" +
						"		while ((o instanceof String s) && s.length() > 0) {\n" +
						"			o = s.substring(0, s.length() - 1);\n" +
						"			System.out.println(s);\n" +
						"		}\n" +
						"	}\n" +
						"}\n",
				},
				"one\non\no",
				options);
	}
	public void test022a() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X22a.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X22a {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" +
						"	}\n" +
						"  public static void foo(Object o) {\n" +
						"		do {\n" +
						"			o = s.substring(0, s.length() - 1);\n" +
						"			System.out.println(s);\n" +
						"		} while ((o instanceof String s) && s.length() > 0);\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X22a.java (at line 8)\n" +
				"	o = s.substring(0, s.length() - 1);\n" +
				"	    ^\n" +
				"The pattern variable s is not in scope in this location\n" +
				"----------\n" +
				"2. ERROR in X22a.java (at line 8)\n" +
				"	o = s.substring(0, s.length() - 1);\n" +
				"	                   ^\n" +
				"The pattern variable s is not in scope in this location\n" +
				"----------\n" +
				"3. ERROR in X22a.java (at line 9)\n" +
				"	System.out.println(s);\n" +
				"	                   ^\n" +
				"The pattern variable s is not in scope in this location\n" +
				"----------\n",
				null,
				true,
				options);
	}
	public void test022b() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X22b.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X22b {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" +
						"	}\n" +
						"  public static void foo(Object o) {\n" +
						"		do {\n" +
						"			// nothing\n" +
						"		} while ((o instanceof String s));\n" +
						"		System.out.println(s);\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X22b.java (at line 10)\n" +
				"	System.out.println(s);\n" +
				"	                   ^\n" +
				"The pattern variable s is not in scope in this location\n" +
				"----------\n",
				null,
				true,
				options);
	}
	public void test022c() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X22c.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X22c {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" +
						"	}\n" +
						"  public static void foo(Object o) {\n" +
						"		do {\n" +
						"			// nothing\n" +
						"		} while (!(o instanceof String s));\n" +
						"		System.out.println(s);\n" +
						"	}\n" +
						"}\n",
				},
				"one",
				options);
	}
	/* Test pattern expressions in a while statement with break
	 */
	public void test023() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X23.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X23 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" +
						"	}\n" +
						"  public static void foo(Object o) {\n" +
						"		while (!(o instanceof String s) && s.length() > 0) {\n" +
						"			System.out.println(s);\n" +
						"			break;\n" +
						"		}\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X23.java (at line 7)\n" +
				"	while (!(o instanceof String s) && s.length() > 0) {\n" +
				"	                                   ^\n" +
				"The pattern variable s is not in scope in this location\n" +
				"----------\n" +
				"2. ERROR in X23.java (at line 8)\n" +
				"	System.out.println(s);\n" +
				"	                   ^\n" +
				"The pattern variable s is not in scope in this location\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test023a() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X23a.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X23a {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" +
						"	}\n" +
						"  public static void foo(Object o) {\n" +
						"		do {\n" +
						"			System.out.println(s);\n" +
						"			break;\n" +
						"		} while (!(o instanceof String s) && s.length() > 0);\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X23a.java (at line 8)\n" +
				"	System.out.println(s);\n" +
				"	                   ^\n" +
				"The pattern variable s is not in scope in this location\n" +
				"----------\n" +
				"2. ERROR in X23a.java (at line 10)\n" +
				"	} while (!(o instanceof String s) && s.length() > 0);\n" +
				"	                                     ^\n" +
				"The pattern variable s is not in scope in this location\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	/* Test pattern expressions in a while statement with no break
	 */
	public void test023b() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X23b.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X23b {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" +
						"	}\n" +
						"  public static void foo(Object o) {\n" +
						"		while (!(o instanceof String s) && s.length() > 0) {\n" +
						"			System.out.println(s);\n" +
						"			//break;\n" +
						"		}\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X23b.java (at line 7)\n" +
				"	while (!(o instanceof String s) && s.length() > 0) {\n" +
				"	                                   ^\n" +
				"The pattern variable s is not in scope in this location\n" +
				"----------\n" +
				"2. ERROR in X23b.java (at line 8)\n" +
				"	System.out.println(s);\n" +
				"	                   ^\n" +
				"The pattern variable s is not in scope in this location\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	// Same as above but with do while
	public void test023c() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X23c.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X23c {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" +
						"	}\n" +
						"  public static void foo(Object o) {\n" +
						"		do {\n" +
						"			System.out.println(s);\n" +
						"			//break;\n" +
						"		}while (!(o instanceof String s) && s.length() > 0);\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X23c.java (at line 8)\n" +
				"	System.out.println(s);\n" +
				"	                   ^\n" +
				"The pattern variable s is not in scope in this location\n" +
				"----------\n" +
				"2. ERROR in X23c.java (at line 10)\n" +
				"	}while (!(o instanceof String s) && s.length() > 0);\n" +
				"	                                    ^\n" +
				"The pattern variable s is not in scope in this location\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test024a() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X24a.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X24a {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" +
						"	}\n" +
						"  public static void foo(Object o) {\n" +
						"		while (!(o instanceof String s)) {\n" +
						"			throw new IllegalArgumentException();\n" +
						"		}\n" +
						"		System.out.println(s);\n" +
						"	}\n" +
						"}\n",
				},
				"one",
				options);
	}
	/*
	 * It's not a problem to define the same var in two operands of a binary expression,
	 * but then it is not in scope below.
	 */
	public void test025() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X25.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X25 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\", \"two\");\n" +
						"	}\n" +
						"  public static void foo(Object o, Object p) {\n" +
						"		if ((o instanceof String s) != p instanceof String s) {\n" +
						"			System.out.print(\"s:\" + s);\n" +
						"		}\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X25.java (at line 8)\n" +
				"	System.out.print(\"s:\" + s);\n" +
				"	                        ^\n" +
				"The pattern variable s is not in scope in this location\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	/*
	 * It's not a problem to define the same var in two operands of a binary expression,
	 * but then it is not in scope below.
	 */
	public void test026() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X26.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X26 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\", \"two\");\n" +
						"	}\n" +
						"  public static void foo(Object o, Object p) {\n" +
						"		if ((o instanceof String s) == p instanceof String s) {\n" +
						"			System.out.print(\"s:\" + s);\n" +
						"		}\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X26.java (at line 8)\n" +
				"	System.out.print(\"s:\" + s);\n" +
				"	                        ^\n" +
				"The pattern variable s is not in scope in this location\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test027() {
		runConformTest(
				new String[] {
						"X27.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X27 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" +
						"	}\n" +
						"  public static void foo(Object obj) {\n" +
						"		for(int i = 0; (obj instanceof String[] s && s.length > 0 && i < s.length); i++) {\n" +
						"			System.out.println(s[i]);\n" +
						"		}\n" +
						"	}\n" +
						"}\n",
				},
				"",
				getCompilerOptions(true));
	}
	public void test028() {
		runConformTest(
				new String[] {
						"X28.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X28 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(new String[] {\"one\", \"two\"});\n" +
						"	}\n" +
						"  public static void foo(Object obj) {\n" +
						"		for(int i = 0; (obj instanceof String[] s && s.length > 0 && i < s.length); i++) {\n" +
						"			System.out.println(s[i]);\n" +
						"		}\n" +
						"	}\n" +
						"}\n",
				},
				"one\ntwo",
				getCompilerOptions(true));
	}
	public void test029() {
		runConformTest(
				new String[] {
						"X29.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X29 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(new String[] {\"one\", \"two\"});\n" +
						"	}\n" +
						"  public static void foo(Object obj) {\n" +
						"		for(int i = 0; (obj instanceof String[] s) && s.length > 0 && i < s.length; i = (s != null ? i + 1 : i)) {\n" +
						"			System.out.println(s[i]);\n" +
						"		}\n" +
						"	}\n" +
						"}\n",
				},
				"one\ntwo",
				getCompilerOptions(true));
	}
	/*
	 * Test that pattern variables are accepted in initialization of a for statement,
	 * but unavailable in the body if uncertain which if instanceof check was true
	 */
	public void test030() {
		runNegativeTest(
				new String[] {
						"X30.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X30 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" +
						"	}\n" +
						"  public static void foo(Object obj) {\n" +
						"		for(int i = 0, length = (obj instanceof String s) ? s.length() : 0; i < length; i++) {\n" +
						"			System.out.print(s.charAt(i));\n" +
						"		}\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X30.java (at line 8)\n" +
				"	System.out.print(s.charAt(i));\n" +
				"	                 ^\n" +
				"The pattern variable s is not in scope in this location\n" +
				"----------\n",
				"",
				null,
				true,
				getCompilerOptions(true));
	}
	public void test031() {
		runNegativeTest(
				new String[] {
						"X31.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X31 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" +
						"	}\n" +
						"  public static void foo(Object obj) {\n" +
						"		for(int i = 0; !(obj instanceof String[] s) && s.length > 0 && i < s.length; i++) {\n" +
						"			System.out.println(s[i]);\n" +
						"		}\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X31.java (at line 7)\n" +
				"	for(int i = 0; !(obj instanceof String[] s) && s.length > 0 && i < s.length; i++) {\n" +
				"	                                               ^\n" +
				"The pattern variable s is not in scope in this location\n" +
				"----------\n" +
				"2. ERROR in X31.java (at line 7)\n" +
				"	for(int i = 0; !(obj instanceof String[] s) && s.length > 0 && i < s.length; i++) {\n" +
				"	                                                                   ^\n" +
				"The pattern variable s is not in scope in this location\n" +
				"----------\n" +
				"3. ERROR in X31.java (at line 8)\n" +
				"	System.out.println(s[i]);\n" +
				"	                   ^\n" +
				"The pattern variable s is not in scope in this location\n" +
				"----------\n",
				"",
				null,
				true,
				getCompilerOptions(true));
	}
	public void test032() {
		runConformTest(
				new String[] {
						"X32.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X32 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" +
						"	}\n" +
						"  public static void foo(Object obj) {\n" +
						"		String res = null;\n" +
						"		int i = 0;\n" +
						"		switch(i) {\n" +
						"		case 0:\n" +
						"			res = (obj instanceof String s) ? s : null;\n" +
						"		default:\n" +
						"			break;\n" +
						"		}\n" +
						"		System.out.println(res);\n" +
						"	}\n" +
						"}\n",
				},
				"one",
				getCompilerOptions(true));
	}
	public void test033() {
		runNegativeTest(
				new String[] {
						"X33.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X33 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" +
						"	}\n" +
						"  public static void foo(Object obj) {\n" +
						"		String res = null;\n" +
						"		int i = 0;\n" +
						"		switch(i) {\n" +
						"		case 0:\n" +
						"			res = (obj instanceof String s) ? s : null;\n" +
						"			res = s.substring(1);\n" +
						"		default:\n" +
						"			break;\n" +
						"		}\n" +
						"		System.out.println(res);\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X33.java (at line 12)\n" +
				"	res = s.substring(1);\n" +
				"	      ^\n" +
				"The pattern variable s is not in scope in this location\n" +
				"----------\n",
				"",
				null,
				true,
				getCompilerOptions(true));
	}
	public void test034() {
		runNegativeTest(
				new String[] {
						"X34.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X34 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" +
						"	}\n" +
						"  public static void foo(Object obj) {\n" +
						"		int i = 0;\n" +
						"		String result = switch(i) {\n" +
						"			case 0 -> {\n" +
						"				result = (obj instanceof String s) ? s : null;\n" +
						"				yield result;\n" +
						"			}\n" +
						"			default -> {\n" +
						"				yield result;\n" +
						"			}\n" +
						"		};\n" +
						"		System.out.println(result);\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X34.java (at line 14)\n" +
				"	yield result;\n" +
				"	      ^^^^^^\n" +
				"The local variable result may not have been initialized\n" +
				"----------\n",
				"",
				null,
				true,
				getCompilerOptions(true));
	}
	public void test035() {
		runNegativeTest(
				new String[] {
						"X35.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X35 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" +
						"	}\n" +
						"  public static void foo(Object obj) {\n" +
						"		int i = 0;\n" +
						"		String result = switch(i) {\n" +
						"			case 0 -> {\n" +
						"				result = (obj instanceof String s) ? s : null;\n" +
						"				yield s;\n" +
						"			}\n" +
						"			default -> {\n" +
						"				yield s;\n" +
						"			}\n" +
						"		};\n" +
						"		System.out.println(result);\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X35.java (at line 11)\n" +
				"	yield s;\n" +
				"	      ^\n" +
				"The pattern variable s is not in scope in this location\n" +
				"----------\n" +
				"2. ERROR in X35.java (at line 14)\n" +
				"	yield s;\n" +
				"	      ^\n" +
				"The pattern variable s is not in scope in this location\n" +
				"----------\n",
				"",
				null,
				true,
				getCompilerOptions(true));
	}
	public void test036() {
		runConformTest(
				new String[] {
						"X36.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X36 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" +
						"	}\n" +
						"  public static void foo(Object obj) {\n" +
						"		int i = 0;\n" +
						"		String result = switch(i) {\n" +
						"			default -> {\n" +
						"				result = (obj instanceof String s) ? s : null;\n" +
						"				yield result;\n" +
						"			}\n" +
						"		};\n" +
						"		System.out.println(result);\n" +
						"	}\n" +
						"}\n",
				},
				"one",
				getCompilerOptions(true));
	}
	public void test037() {
		runNegativeTest(
				new String[] {
						"X37.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X37 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(new String[] {\"abcd\"});\n" +
						"	}\n" +
						"  public static void foo(Object[] obj) {\n" +
						"		for(int i = 0; (obj[i] instanceof String s) && s.length() > 0 ; i++) {\n" +
						"			System.out.println(s[i]);\n" +
						"		}\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X37.java (at line 8)\n" +
				"	System.out.println(s[i]);\n" +
				"	                   ^^^^\n" +
				"The type of the expression must be an array type but it resolved to String\n" +
				"----------\n",
				"",
				null,
				true,
				getCompilerOptions(true));
	}
	public void test038() {
		runNegativeTest(
				new String[] {
						"X38.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X38 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(new String[] {\"abcd\"});\n" +
						"	}\n" +
						"  public static void foo(Object[] obj) {\n" +
						"		for(int i = 0; (obj[i] instanceof String s) && s.length() > 0 ;) {\n" +
						"			throw new IllegalArgumentException();\n" +
						"		}\n" +
						"		System.out.println(s);\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X38.java (at line 10)\n" +
				"	System.out.println(s);\n" +
				"	                   ^\n" +
				"The pattern variable s is not in scope in this location\n" +
				"----------\n",
				"",
				null,
				true,
				getCompilerOptions(true));
	}
	/*
	 * Failing with VerifyError
	 */
	public void _test039() {
		runConformTest(
				new String[] {
						"X39.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X39 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(new String[] {\"one\"});;\n" +
						"	}\n" +
						"  public static void foo(Object[] obj) {\n" +
						"		for(int i = 0; i < obj.length && (obj[i] instanceof String s) && i < s.length(); i++) {\n" +
						"			System.out.println(s);\n" +
						"		}\n" +
						"	}\n" +
						"}\n",
				},
				"one",
				getCompilerOptions(true));
	}
	public void test040() {
		runConformTest(
				new String[] {
						"X40.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X40 {\n" +
						"	String a;\n" +
						"    Object o1 = \"x\";\n" +
						"    public static void main(String argv[]) {\n" +
						"        System.out.println(new X40().foo());\n" +
						"    }\n" +
						"    public String foo() {\n" +
						"        String res = \"\";\n" +
						"    	 Object o2 = \"x\";\n" +
						"        if (o1 instanceof String s) { \n" +
						"            res = \"then_\" + s;\n" +
						"        } else {\n" +
						"            res = \"else_\";\n" +
						"        }\n" +
						"        return res;\n" +
						"    }\n" +
						"}\n",
				},
				"then_x",
				getCompilerOptions(true));
	}
	public void test041() {
		runConformTest(
				new String[] {
						"X41.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X41 {\n" +
						"	String a;\n" +
						"    Object o1 = \"x\";\n" +
						"    public static void main(String argv[]) {\n" +
						"        System.out.println(new X41().foo());\n" +
						"    }\n" +
						"    public String foo() {\n" +
						"        String res = \"\";\n" +
						"        Object o2 = \"x\";\n" +
						"        if ( !(o1 instanceof String s) || !o1.equals(s) ) { \n" +
						"            res = \"then_\";\n" +
						"        } else {\n" +
						"            res = \"else_\" + s;\n" +
						"        }\n" +
						"        return res;\n" +
						"    }\n" +
						"}\n",
				},
				"else_x",
				getCompilerOptions(true));
	}
	public void test042() {
		runConformTest(
				new String[] {
						"X42.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X42 {\n" +
						"	 String a;\n" +
						"    Object o1 = \"x\";\n" +
						"    public static void main(String argv[]) {\n" +
						"        System.out.println(new X42().foo());\n" +
						"    }\n" +
						"    public String foo() {\n" +
						"        String res = \"\";\n" +
						"        Object o2 = o1;\n" +
						"        if ( !(o1 instanceof String s) || !o1.equals(s) ) { \n" +
						"            res = \"then_\";\n" +
						"        } else {\n" +
						"            res = \"else_\" + s;\n" +
						"        }\n" +
						"        return res;\n" +
						"    }\n" +
						"}\n",
				},
				"else_x",
				getCompilerOptions(true));
	}
	public void _test043() {
		runConformTest(
				new String[] {
						"X43.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X43 {\n" +
						"	 public static void main(String argv[]) {\n" +
						"		System.out.println(new X43().foo(\"foo\", \"test\"));\n" +
						"	}\n" +
						"	public boolean foo(Object obj, String s) {\n" +
						"		class Inner {\n" +
						"			public boolean foo(Object obj) {\n" +
						"				if (obj instanceof String s) {\n" +
						"					// x is shadowed now\n" +
						"					if (!\"foo\".equals(s))\n" +
						"						return false;\n" +
						"				}\n" +
						"				// x is not shadowed\n" +
						"				return \"test\".equals(s);\n" +
						"			}\n" +
						"		}\n" +
						"		return new Inner().foo(obj);\n" +
						"	}\n" +
						"}\n",
				},
				"else_x",
				getCompilerOptions(true));
	}
	public void test044() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		String old = compilerOptions.get(CompilerOptions.OPTION_PreserveUnusedLocal);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		runConformTest(
				new String[] {
						"X44.java",
						"@SuppressWarnings(\"preview\")\n" +
						"class Inner<T> {\n" +
						"    public boolean foo(Object obj) {\n" +
						"        if (obj instanceof Inner<?> p) {\n" +
						"            return true;\n" +
						"        }\n" +
						"        return false;\n" +
						"    }\n" +
						"} \n" +
						"public class X44  {\n" +
						"    public static void main(String argv[]) {\n" +
						"    	Inner<String> param = new Inner<>();\n" +
						"    	System.out.println(new Inner<String>().foo(param));\n" +
						"    }\n" +
						"}\n",
				},
				"true",
				compilerOptions);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, old);
	}
	public void test045() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		String old = compilerOptions.get(CompilerOptions.OPTION_PreserveUnusedLocal);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		runConformTest(
				new String[] {
						"X45.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X45 {\n" +
						"    String s = \"test\";\n" +
						"    boolean result = s instanceof String s1;\n" +
						"	 public static void main(String argv[]) {\n" +
						"    	System.out.println(\"true\");\n" +
						"    }\n" +
						"}\n",
				},
				"true",
				compilerOptions);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, old);
	}
	public void test046() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		String old = compilerOptions.get(CompilerOptions.OPTION_PreserveUnusedLocal);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		runConformTest(
				new String[] {
						"X46.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X46 {\n" +
						"    String s = \"test\";\n" +
						"    boolean result = (s instanceof String s1 && s1 != null);\n" +
						"	 public static void main(String argv[]) {\n" +
						"    	System.out.println(\"true\");\n" +
						"    }\n" +
						"}\n",
				},
				"true",
				compilerOptions);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, old);
	}
	public void test047() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		String old = compilerOptions.get(CompilerOptions.OPTION_PreserveUnusedLocal);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		runConformTest(
				new String[] {
						"InstanceOfPatternTest.java",
						"public class InstanceOfPatternTest {\n" +
						"	public static void main(String[] args) {\n" +
						"		if (getChars() instanceof String s) {\n" +
						"			System.out.println(s);\n" +
						"		}\n" +
						"	}\n" +
						"	static CharSequence getChars() {\n" +
						"		return \"xyz\";\n" +
						"	}\n" +
						"}\n",
				},
				"xyz",
				compilerOptions);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, old);
	}
	public void test048() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		String old = compilerOptions.get(CompilerOptions.OPTION_PreserveUnusedLocal);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		runConformTest(
				new String[] {
						"InstanceOfPatternTest.java",
						"public class InstanceOfPatternTest {\n" +
						"	public static void main(String[] args) {\n" +
						"		if (getChars() instanceof String s) {\n" +
						"			System.out.println(s);\n" +
						"		}\n" +
						"	}\n" +
						"	static CharSequence getChars() {\n" +
						"		return \"xyz\";\n" +
						"	}\n" +
						"}\n",
				},
				"xyz",
				compilerOptions);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, old);
	}
	public void test049() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		String old = compilerOptions.get(CompilerOptions.OPTION_PreserveUnusedLocal);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		runConformTest(
				new String[] {
						"InstanceOfPatternTest.java",
						"public class InstanceOfPatternTest {\n" +
						"	public static void main(String[] args) {\n" +
						"		if ( ((CharSequence) getChars()) instanceof String s) {\n" +
						"			System.out.println(s);\n" +
						"		}\n" +
						"	}\n" +
						"	static Object getChars() {\n" +
						"		return \"xyz\";\n" +
						"	}\n" +
						"}\n",
				},
				"xyz",
				compilerOptions);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, old);
	}
	public void test050() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		String old = compilerOptions.get(CompilerOptions.OPTION_PreserveUnusedLocal);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		runNegativeTest(
				new String[] {
						"InstanceOfPatternTest.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class InstanceOfPatternTest {\n" +
						"	public static void main(String[] args) {\n" +
						"		if ( ((s) -> {return s;}) instanceof I s) {\n" +
						"			System.out.println(s);\n" +
						"		}\n" +
						"	}\n" +
						"} \n" +
						"interface I {\n" +
						"	public String foo(String s);\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in InstanceOfPatternTest.java (at line 4)\n" +
				"	if ( ((s) -> {return s;}) instanceof I s) {\n" +
				"	     ^^^^^^^\n" +
				"The target type of this expression must be a functional interface\n" +
				"----------\n",
				"",
				null,
				true,
				compilerOptions);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, old);
	}
	public void _test051() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		String old = compilerOptions.get(CompilerOptions.OPTION_PreserveUnusedLocal);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		runConformTest(
				new String[] {
						"InstanceOfPatternTest.java",
						"public class InstanceOfPatternTest {\n" +
						"	static String STR = \"2\";\n" +
						"	public static void main(String[] args) {\n" +
						"		if ( switch(STR) {\n" +
						"				case \"1\" -> \"one\";\n" +
						"				default -> \"Unknown\";\n" +
						"			  } \n" +
						"				instanceof String s) {\n" +
						"			System.out.println(s);\n" +
						"		}\n" +
						"	}\n" +
						"	public CharSequence chars() {\n" +
						"		return \"abc\";\n" +
						"	}\n" +
						"}\n",
				},
				"Unknown",
				compilerOptions);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, old);
	}
	public void test052() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		String old = compilerOptions.get(CompilerOptions.OPTION_PreserveUnusedLocal);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	@SuppressWarnings(\"preview\")\n" +
						"	public static void main(String args[]) {\n" +
						"		String result = null;\n" +
						"		Object obj = \"abc\";\n" +
						"		int i = switch (0) {\n" +
						"			case 1 -> {\n" +
						"				yield 1;\n" +
						"			}\n" +
						"			default -> {\n" +
						"				for (int j = 0; !(obj instanceof String s);) {\n" +
						"					obj = null;\n" +
						"				}\n" +
						"				result = s;\n" +
						"				System.out.println(result);\n" +
						"				yield 2;\n" +
						"			}\n" +
						"		};\n" +
						"		System.out.println(i);\n" +
						"	}\n" +
						"}\n",
				},
				"abc\n" +
				"2",
				compilerOptions);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, old);
	}
	public void testBug562392a() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"public class X<T> {\n" +
						"	public boolean foo(T obj) {\n" +
						"		if (obj instanceof T s) {\n" +
						"			System.out.println(s);\n" +
						"		}\n" +
						"		return true;\n" +
						"	}\n" +
						"	public static void main(String argv[]) {\n" +
						"		String s = \"x\";\n" +
						"		System.out.println(new X<String>().foo(s));\n" +
						"	}\n" +
						"}\n",
				},
				"x\n" +
				"true",
				compilerOptions);
		}
	public void testBug562392b() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X<T> {\n" +
						"	public boolean foo(Object obj) {\n" +
						"        if (obj instanceof T) {\n" +
						"            return false;\n" +
						"        }\n" +
						"        return true;\n" +
						"    }\n" +
						"	public static void main(String argv[]) {\n" +
						"		System.out.println(\"\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	if (obj instanceof T) {\n" +
				"	    ^^^\n" +
				"Type Object cannot be safely cast to T\n" +
				"----------\n",
				"X.java:4: error: Object cannot be safely cast to T\n" +
				"        if (obj instanceof T) {\n" +
				"            ^\n" +
				"  where T is a type-variable:\n" +
				"    T extends Object declared in class X",
				null,
				true,
				compilerOptions);
		}
	public void testBug562392c() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X<T> {\n" +
						"	public boolean foo(Object obj) {\n" +
						"        if (obj instanceof T t) {\n" +
						"            return false;\n" +
						"        }\n" +
						"        return true;\n" +
						"    }\n" +
						"	public static void main(String argv[]) {\n" +
						"		System.out.println(\"\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	if (obj instanceof T t) {\n" +
				"	    ^^^\n" +
				"Type Object cannot be safely cast to T\n" +
				"----------\n",
				"X.java:4: error: Object cannot be safely cast to T\n" +
				"        if (obj instanceof T t) {\n" +
				"            ^\n" +
				"  where T is a type-variable:\n" +
				"    T extends Object declared in class X",
				null,
				true,
				compilerOptions);
		}
	public void testBug562392d() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X<T> {\n" +
						"	public boolean foo(Object obj) {\n" +
						"        if (null instanceof T t) {\n" +
						"            return false;\n" +
						"        }\n" +
						"        return true;\n" +
						"    }\n" +
						"	public static void main(String argv[]) {\n" +
						"		System.out.println(\"\");\n" +
						"	}\n" +
						"}\n",
				},
				"",
				compilerOptions);
	}
	public void testBug562392e() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X<T> {\n" +
						"	public boolean foo(X<?> obj) {\n" +
						"        if (obj instanceof X<String> p) {\n" +
						"            return true;\n" +
						"        }\n" +
						"        return false;\n" +
						"    }\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	if (obj instanceof X<String> p) {\n" +
				"	    ^^^\n" +
				"Type X<capture#1-of ?> cannot be safely cast to X<String>\n" +
				"----------\n",
				"",
				null,
				true,
				compilerOptions);
	}
	public void testBug562392f() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"class Outer<T> {\n" +
						"    static class Inner<T> {\n" +
						"    }\n" +
						"}\n" +
						"@SuppressWarnings({\"preview\", \"rawtypes\"})\n" +
						"class X<T> {\n" +
						"    public boolean foo(Outer.Inner obj) {\n" +
						"        if (obj instanceof Outer<?> p) {\n" +
						"            return true;\n" +
						"        }\n" +
						"        return false;\n" +
						"    }\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 8)\n" +
				"	if (obj instanceof Outer<?> p) {\n" +
				"	    ^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Incompatible conditional operand types Outer.Inner and Outer<?>\n" +
				"----------\n",
				"",
				null,
				true,
				compilerOptions);
	}
	public void testBug562392g() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"class Outer<T> {\n" +
						"    static class Inner<T> {\n" +
						"    }\n" +
						"}\n" +
						"@SuppressWarnings({\"preview\", \"rawtypes\"})\n" +
						"class X<T> {\n" +
						"    public boolean foo(Object obj) {\n" +
						"        if (obj instanceof Outer.Inner<?> p) {\n" +
						"            return true;\n" +
						"        }\n" +
						"        return false;\n" +
						"    }\n" +
						"	public static void main(String argv[]) {\n" +
						"		Outer.Inner inn = new Outer.Inner();\n" +
						"    	System.out.println(new X<String>().foo(inn));\n" +
						"	}\n" +
						"}\n",
				},
				"true",
				compilerOptions);
	}
	public void testBug562392h() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"@SuppressWarnings({\"preview\", \"rawtypes\"})\n" +
						"public class X<T> {\n" +
						"	public boolean foo(X[] obj) {\n" +
						"        if (obj instanceof Object[] p) {\n" +
						"            return true;\n" +
						"        }\n" +
						"        return false;\n" +
						"    }\n" +
						"	public static void main(String argv[]) {\n" +
						"		Object[] param = {new X()};\n" +
						"       System.out.println(new X<String>().foo(param));\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 11)\n" +
				"	System.out.println(new X<String>().foo(param));\n" +
				"	                                   ^^^\n" +
				"The method foo(X[]) in the type X<String> is not applicable for the arguments (Object[])\n" +
				"----------\n",
				"",
				null,
				true,
				compilerOptions);
	}
	public void testBug562392i() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		String backup = compilerOptions.get(CompilerOptions.OPTION_EnablePreviews);
		compilerOptions.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		try {
			runNegativeTest(
					new String[] {
							"Test.java",
							"import java.util.ArrayList;\n" +
									"import java.util.List;\n" +
									"import java.util.function.Function;\n" +
									"import java.util.function.UnaryOperator;\n" +
									"@SuppressWarnings({\"preview\"})\n" +
									"public class Test<T> {\n" +
									"    public boolean foo(Function<ArrayList<T>, ArrayList<T>> obj) {\n" +
									"        if (obj instanceof UnaryOperator<? extends List<T>>) {\n" +
									"            return false;\n" +
									"        }\n" +
									"        return true;\n" +
									"    }\n" +
									"}\n",
					},
					"----------\n" +
					"1. ERROR in Test.java (at line 8)\n" +
					"	if (obj instanceof UnaryOperator<? extends List<T>>) {\n" +
					"	    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
					"Cannot perform instanceof check against parameterized type UnaryOperator<? extends List<T>>. Use the form UnaryOperator<?> instead since further generic type information will be erased at runtime\n" +
					"----------\n",
					"",
					null,
					true,
					compilerOptions);
		} finally {
			compilerOptions.put(CompilerOptions.OPTION_EnablePreviews, backup);
		}
	}
	public void testBug562392j() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"Test.java",
						"import java.util.ArrayList;\n" +
						"import java.util.List;\n" +
						"import java.util.function.Function;\n" +
						"import java.util.function.UnaryOperator;\n" +
						"@SuppressWarnings({\"preview\", \"rawtypes\"})\n" +
						"public class Test<T> {\n" +
						"    public boolean foo(Function<ArrayList<T>, ArrayList<T>> obj) {\n" +
						"        if (obj instanceof UnaryOperator<? extends List<T>>) {\n" +
						"            return false;\n" +
						"        }\n" +
						"        return true;\n" +
						"    }\n" +
						"	public static void main(String argv[]) {\n" +
						"       System.out.println(\"\");\n" +
						"	}\n" +
						"}\n",
				},
				"",
				compilerOptions);
	}
	public void test053() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	@SuppressWarnings(\"preview\")\n" +
						"	public static void main(String argv[]) {\n" +
						"		Object obj = \"x\";\n" +
						"		if (obj instanceof String s) {\n" +
						"			System.out.println(s);\n" +
						"		}\n" +
						"		String s = \"y\";\n" +
						"		System.out.println(s);\n" +
						"	}\n" +
						"}\n",
				},
				"x\n" +
				"y",
				compilerOptions);
	}
	public void test054() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	@SuppressWarnings(\"preview\")\n" +
						"	public static void main(String argv[]) {\n" +
						"		Object obj = \"x\";\n" +
						"		while (!(obj instanceof String s)) {\n" +
						"			String s = \"y\";\n" +
						"			System.out.println(s);\n" +
						"		}\n" +
						"		System.out.println(s);\n" +
						"	}\n" +
						"}\n",
				},
				"x",
				compilerOptions);
	}
	public void test055() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"public static void main(String argv[]) {\n" +
						"		String result = \"\";\n" +
						"		Object obj = \"abc\";\n" +
						"		for (; !(obj instanceof String a);) {\n" +
						"			String a = \"\";\n" +
						"			result = a;\n" +
						"			obj = null;\n" +
						"		}\n" +
						"		if (!result.equals(\"abc\")) {\n" +
						"			System.out.println(\"PASS\");\n" +
						"		} else {\n" +
						"			System.out.println(\"FAIL\");\n" +
						"		}\n" +
						"	}\n" +
						"}\n",
				},
				"PASS",
				compilerOptions);
	}
	// Positive - Test conflicting pattern variable and lambda argument in for loop
	public void test056() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X {\n" +
						"	public static int impl(I a) {\n" +
						"		return a.foo(\"Default\");\n" +
						"	}\n" +
						"	public static void main(String argv[]) {\n" +
						"		String result = \"\";\n" +
						"		Object obj = \"a\";\n" +
						"		for (int i = 0; !(obj instanceof String a); i = impl(a -> a.length())) {\n" +
						"			obj = null;\n" +
						"		}\n" +
						"		if (!result.equals(\"\"))\n" +
						"			System.out.println(\"FAIL\");\n" +
						"		else\n" +
						"			System.out.println(\"PASS\");\n" +
						"	}\n" +
						"}\n" +
						"interface I {\n" +
						"	int foo(String s);\n" +
						"}\n",
				},
				"PASS",
				compilerOptions);
	}
	// Positive - Test conflicting pattern variable and lambda argument in for loop (block)
	public void test056a() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X {\n" +
						"	public static int impl(I a) {\n" +
						"		return a.foo(\"Default\");\n" +
						"	}\n" +
						"	public static void main(String argv[]) {\n" +
						"		String result = \"\";\n" +
						"		Object obj = \"a\";\n" +
						"		for (int i = 0; !(obj instanceof String a); i = impl(x -> {\n" +
						"															String a = \"\";\n" +
						"															return a.length();\n" +
						"														})) {\n" +
						"			obj = null;\n" +
						"		}\n" +
						"		if (!result.equals(\"\"))\n" +
						"			System.out.println(\"FAIL\");\n" +
						"		else\n" +
						"			System.out.println(\"PASS\");\n" +
						"	}\n" +
						"}\n" +
						"interface I {\n" +
						"	int foo(String s);\n" +
						"}\n",
				},
				"PASS",
				compilerOptions);
	}
	// Positive - Test conflicting pattern variable and lambda argument in if
	public void test056b() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X {\n" +
						"	public static int impl(I a) {\n" +
						"		return a.foo(\"Default\");\n" +
						"	}\n" +
						"	public static void main(String argv[]) {\n" +
						"		String result = \"\";\n" +
						"		Object obj = \"a\";\n" +
						"		if (!(obj instanceof String a)) {\n" +
						"			  int i = impl(a -> a.length());\n" +
						"		}\n" +
						"		if (!result.equals(\"\"))\n" +
						"			System.out.println(\"FAIL\");\n" +
						"		else\n" +
						"			System.out.println(\"PASS\");\n" +
						"	}\n" +
						"}\n" +
						"interface I {\n" +
						"	int foo(String s);\n" +
						"}\n",
				},
				"PASS",
				compilerOptions);
	}
	// Positive - Test conflicting pattern variable and lambda argument in if
	public void test056d() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X {\n" +
						"	public static int impl(I a) {\n" +
						"		return a.foo(\"Default\");\n" +
						"	}\n" +
						"	public static void main(String argv[]) {\n" +
						"		String result = \"\";\n" +
						"		Object obj = \"a\";\n" +
						"		for (int i = 0; (obj instanceof String a); i = impl(a -> a.length())) {\n" +
						"			obj = null;\n" +
						"		}\n" +
						"		if (!result.equals(\"\"))\n" +
						"			System.out.println(\"FAIL\");\n" +
						"		else\n" +
						"			System.out.println(\"PASS\");\n" +
						"	}\n" +
						"}\n" +
						"interface I {\n" +
						"	int foo(String s);\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 9)\n" +
				"	for (int i = 0; (obj instanceof String a); i = impl(a -> a.length())) {\n" +
				"	                                                    ^\n" +
				"Lambda expression\'s parameter a cannot redeclare another local variable defined in an enclosing scope. \n" +
				"----------\n",
				"",
				null,
				true,
				compilerOptions);
	}
	/*
	 * Test we report only one duplicate variable, i.e., in THEN stmt
	 * where pattern variable is in scope.
	 */
	public void test057() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	@SuppressWarnings(\"preview\")\n" +
						"	public static void main(String argv[]) {\n" +
						"		Object obj = \"x\";\n" +
						"		if (obj instanceof String s) {\n" +
						"			String s = \"\";\n" +
						"			System.out.println(s);\n" +
						"		}\n" +
						"		String s = \"y\";\n" +
						"		System.out.println(s);\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	String s = \"\";\n" +
				"	       ^\n" +
				"Duplicate local variable s\n" +
				"----------\n",
				"",
				null,
				true,
				compilerOptions);
		}
	public void test058() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	@SuppressWarnings(\"preview\")\n" +
						"	public static void main(String[] s) {\n" +
						"		Object obj = \"x\";\n" +
						"		if (obj instanceof String[] s && s.length > 0) {\n" +
						"			System.out.println(s[0]);\n" +
						"		}\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	if (obj instanceof String[] s && s.length > 0) {\n" +
				"	                            ^\n" +
				"Duplicate local variable s\n" +
				"----------\n",
				"",
				null,
				true,
				compilerOptions);
	}
}
