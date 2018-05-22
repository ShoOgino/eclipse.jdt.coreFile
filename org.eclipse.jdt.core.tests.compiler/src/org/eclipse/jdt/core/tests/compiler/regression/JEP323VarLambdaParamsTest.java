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
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.IOException;
import java.util.Map;

import org.eclipse.jdt.core.tests.util.CompilerTestSetup;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import junit.framework.Test;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class JEP323VarLambdaParamsTest extends AbstractRegressionTest {

public static Class testClass() {
	return JEP323VarLambdaParamsTest.class;
}
public void initialize(CompilerTestSetup setUp) {
	super.initialize(setUp);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_11);
}

public JEP323VarLambdaParamsTest(String testName){
	super(testName);
}
static {
//	TESTS_NUMBERS = new int[] { 1 };
//	TESTS_RANGE = new int[] { 1, -1 };
//	TESTS_NAMES = new String[] { "testBug534787_positive_001" };
}
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_11);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_11);
	return options;
}

public void testBug534787_positive_001() throws IOException {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        I lam = (var  x) -> {System.out.println(\"SUCCESS \" + x);};\n" +
				"        lam.apply(20);\n" +
				"    }\n" +
				"}\n" +
				"interface I {\n" +
				"    public void apply(Integer k);\n" +
				"}\n"
			},
			"SUCCESS 20");
}
public void testBug534787_positive_002() throws IOException {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        I lam = (var  x, var y) -> {System.out.println(\"SUCCESS \" + (x+y));};\n" +
				"        lam.apply(20, 200);\n" +
				"    }\n" +
				"}\n" +
				"interface I {\n" +
				"    public void apply(Integer k, Integer l);\n" +
				"}\n"
			},
			"SUCCESS 220");
}
public void testBug534787_positive_003() throws IOException {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        I lam = var -> {System.out.println(\"SUCCESS \" + var);};\n" +
				"        lam.apply(10);\n" +
				"    }\n" +
				"}\n" +
				"interface I {\n" +
				"    public void apply(Integer k);\n" +
				"}\n"
			},
			"SUCCESS 10");
}
public void testBug534787_positive_004() throws IOException {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        I lam = (var var) -> {System.out.println(\"SUCCESS \" + var);};\n" +
				"        lam.apply(10);\n" +
				"    }\n" +
				"}\n" +
				"interface I {\n" +
				"    public void apply(Integer k);\n" +
				"}\n"
			},
			"SUCCESS 10");
}
public void testBug534787_negative_001() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        I lam = (var  x, y) -> {System.out.println(\"SUCCESS \" + x);};\n" +
				"    }\n" +
				"}\n" +
				"interface I {\n" +
				"    public void apply(Integer k, Integer z);\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	I lam = (var  x, y) -> {System.out.println(\"SUCCESS \" + x);};\n" + 
			"	         ^^^\n" + 
			"Syntax error on token \"var\", ( expected after this token\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	I lam = (var  x, y) -> {System.out.println(\"SUCCESS \" + x);};\n" + 
			"	                  ^\n" + 
			"Syntax error on token \")\", delete this token\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 3)\n" + 
			"	I lam = (var  x, y) -> {System.out.println(\"SUCCESS \" + x);};\n" + 
			"	                                                           ^\n" + 
			"Syntax error, insert \")\" to complete Expression\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 3)\n" + 
			"	I lam = (var  x, y) -> {System.out.println(\"SUCCESS \" + x);};\n" + 
			"	                                                           ^\n" + 
			"Syntax error, insert \")\" to complete Expression\n" + 
			"----------\n");
}
public void testBug534787_negative_002() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        I lam = (var  x, Integer y) -> {System.out.println(\"SUCCESS \" + x);};\n" +
				"        lam.apply(20, 200);\n" +
				"    }\n" +
				"}\n" +
				"interface I {\n" +
				"    public void apply(Integer k, Integer z);\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	I lam = (var  x, Integer y) -> {System.out.println(\"SUCCESS \" + x);};\n" + 
			"	              ^\n" + 
			"\'var\' cannot be mixed with non-var parameters\n" + 
			"----------\n");
}
public void testBug534787_negative_003() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        I lam = (Integer  x, var y) -> {System.out.println(\"SUCCESS \" + x);};\n" +
				"        lam.apply(20, 200);\n" +
				"    }\n" +
				"}\n" +
				"interface I {\n" +
				"    public void apply(Integer k, Integer z);\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	I lam = (Integer  x, var y) -> {System.out.println(\"SUCCESS \" + x);};\n" + 
			"	                         ^\n" + 
			"'var' cannot be mixed with non-var parameters\n" + 
			"----------\n");
}
public void testBug534787_negative_004() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        I lam = (var  x, var y, var...s) -> {System.out.println(\"SUCCESS \" + x);};\n" +
				"        lam.apply(20, 200, \"hello\");\n" +
				"    }\n" +
				"}\n" +
				"interface I {\n" +
				"    public void apply(Integer k, Integer z, String s);\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	I lam = (var  x, var y, var...s) -> {System.out.println(\"SUCCESS \" + x);};\n" + 
			"	                              ^\n" + 
			"'var' is not allowed as an element type of an array\n" +
			"----------\n");
}
public void testBug534787_negative_005() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        I lam = (var  x, Integer y, var...s) -> {System.out.println(\"SUCCESS \" + x);};\n" +
				"        lam.apply(20, 200, \"hello\");\n" +
				"    }\n" +
				"}\n" +
				"interface I {\n" +
				"    public void apply(Integer k, Integer z, String s);\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	I lam = (var  x, Integer y, var...s) -> {System.out.println(\"SUCCESS \" + x);};\n" + 
			"	              ^\n" + 
			"'var' cannot be mixed with non-var parameters\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	I lam = (var  x, Integer y, var...s) -> {System.out.println(\"SUCCESS \" + x);};\n" + 
			"	                                  ^\n" + 
			"'var' is not allowed as an element type of an array\n" +
			"----------\n");
}

public void testBug534787_negative_006() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        I lam = var  x -> {System.out.println(\"SUCCESS \" + x);};\n" +
				"    }\n" +
				"}\n" +
				"interface I {\n" +
				"    public void apply(Integer k);\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	I lam = var  x -> {System.out.println(\"SUCCESS \" + x);};\n" + 
			"	             ^\n" + 
			"Syntax error on token \"x\", delete this token\n" + 
			"----------\n");
}
}