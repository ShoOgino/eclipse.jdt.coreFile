/*******************************************************************************
 * Copyright (c) 2011, 2012 IBM Corporation and others.
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

import junit.framework.Test;

public class GrammarCoverageTests308 extends AbstractRegressionTest {

	static { 
//		TESTS_NUMBERS = new int [] { 35 };
//		TESTS_NAMES = new String [] { "testnew" };
	}
	public static Class testClass() {
		return GrammarCoverageTests308.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_8);
	}
	public GrammarCoverageTests308(String testName){
		super(testName);
	}
	// Lone test to verify that multiple annotations of all three kinds are accepted. All other tests will use only marker annotations
	public void test000() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X extends @Marker @SingleMember(0) @Normal(Value = 0) Object {\n" +
					"}\n"
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 1)\n" + 
				"	public class X extends @Marker @SingleMember(0) @Normal(Value = 0) Object {\n" + 
				"	                        ^^^^^^\n" + 
				"Marker cannot be resolved to a type\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 1)\n" + 
				"	public class X extends @Marker @SingleMember(0) @Normal(Value = 0) Object {\n" + 
				"	                                ^^^^^^^^^^^^\n" + 
				"SingleMember cannot be resolved to a type\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 1)\n" + 
				"	public class X extends @Marker @SingleMember(0) @Normal(Value = 0) Object {\n" + 
				"	                                                 ^^^^^^\n" + 
				"Normal cannot be resolved to a type\n" + 
				"----------\n");
	}
	// FieldDeclaration ::= Modifiersopt Type VariableDeclarators ';'
	public void test001() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    @Marker int x;\n" +
					"    Zork z;\n" +
					"}\n" +
					"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_PARAMETER)\n" +
					"@interface Marker {}\n",
					
					"java/lang/annotation/ElementType.java",
					"package java.lang.annotation;\n"+
					"public enum ElementType {\n" +
					"    TYPE,\n" +
					"    FIELD,\n" +
					"    METHOD,\n" +
					"    PARAMETER,\n" +
					"    CONSTRUCTOR,\n" +
					"    LOCAL_VARIABLE,\n" +
					"    ANNOTATION_TYPE,\n" +
					"    PACKAGE,\n" +
					"    TYPE_PARAMETER,\n" +
					"    TYPE_USE\n" +
					"}\n"
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	@Marker int x;\n" + 
				"	^^^^^^^\n" + 
				"The annotation @Marker is disallowed for this location\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 3)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n");
	}
	// TYPE:   MethodHeaderName ::= Modifiersopt TypeParameters Type 'Identifier' '('
	public void test002() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    @Marker <T> @Marker int x() { return 10; };\n" +
					"    Zork z;\n" +
					"}\n" +
					"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_PARAMETER)\n" +
					"@interface Marker {}\n",
					
					"java/lang/annotation/ElementType.java",
					"package java.lang.annotation;\n"+
					"public enum ElementType {\n" +
					"    TYPE,\n" +
					"    FIELD,\n" +
					"    METHOD,\n" +
					"    PARAMETER,\n" +
					"    CONSTRUCTOR,\n" +
					"    LOCAL_VARIABLE,\n" +
					"    ANNOTATION_TYPE,\n" +
					"    PACKAGE,\n" +
					"    TYPE_PARAMETER,\n" +
					"    TYPE_USE\n" +
					"}\n"
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	@Marker <T> @Marker int x() { return 10; };\n" + 
				"	^^^^^^^\n" + 
				"The annotation @Marker is disallowed for this location\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 2)\n" + 
				"	@Marker <T> @Marker int x() { return 10; };\n" + 
				"	            ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 3)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n");
	}
	// TYPE:   MethodHeaderName ::= Modifiersopt Type 'Identifier' '('
	public void test003() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    @Marker int x() { return 10; };\n" +
					"    Zork z;\n" +
					"}\n" +
					"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_PARAMETER)\n" +
					"@interface Marker {}\n",
					
					"java/lang/annotation/ElementType.java",
					"package java.lang.annotation;\n"+
					"public enum ElementType {\n" +
					"    TYPE,\n" +
					"    FIELD,\n" +
					"    METHOD,\n" +
					"    PARAMETER,\n" +
					"    CONSTRUCTOR,\n" +
					"    LOCAL_VARIABLE,\n" +
					"    ANNOTATION_TYPE,\n" +
					"    PACKAGE,\n" +
					"    TYPE_PARAMETER,\n" +
					"    TYPE_USE\n" +
					"}\n"
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	@Marker int x() { return 10; };\n" + 
				"	^^^^^^^\n" + 
				"The annotation @Marker is disallowed for this location\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 3)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n");
	}
	// FormalParameter ::= Modifiersopt Type VariableDeclaratorIdOrThis
	public void test004() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    int x(@Marker int p) { return 10; };\n" +
					"    Zork z;\n" +
					"}\n" +
					"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_PARAMETER)\n" +
					"@interface Marker {}\n",
					
					"java/lang/annotation/ElementType.java",
					"package java.lang.annotation;\n"+
					"public enum ElementType {\n" +
					"    TYPE,\n" +
					"    FIELD,\n" +
					"    METHOD,\n" +
					"    PARAMETER,\n" +
					"    CONSTRUCTOR,\n" +
					"    LOCAL_VARIABLE,\n" +
					"    ANNOTATION_TYPE,\n" +
					"    PACKAGE,\n" +
					"    TYPE_PARAMETER,\n" +
					"    TYPE_USE\n" +
					"}\n"
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	int x(@Marker int p) { return 10; };\n" + 
				"	      ^^^^^^^\n" + 
				"The annotation @Marker is disallowed for this location\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 3)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n");
	}
	// FormalParameter ::= Modifiersopt Type PushZeroTypeAnnotations '...' VariableDeclaratorIdOrThis
	public void test005() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    int x(@Marker int ... p) { return 10; };\n" +
					"    Zork z;\n" +
					"}\n" +
					"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_PARAMETER)\n" +
					"@interface Marker {}\n",
					
					"java/lang/annotation/ElementType.java",
					"package java.lang.annotation;\n"+
					"public enum ElementType {\n" +
					"    TYPE,\n" +
					"    FIELD,\n" +
					"    METHOD,\n" +
					"    PARAMETER,\n" +
					"    CONSTRUCTOR,\n" +
					"    LOCAL_VARIABLE,\n" +
					"    ANNOTATION_TYPE,\n" +
					"    PACKAGE,\n" +
					"    TYPE_PARAMETER,\n" +
					"    TYPE_USE\n" +
					"}\n"
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	int x(@Marker int ... p) { return 10; };\n" + 
				"	      ^^^^^^^\n" + 
				"The annotation @Marker is disallowed for this location\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 3)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n");
	}
	// FormalParameter ::= Modifiersopt Type @308... TypeAnnotations '...' VariableDeclaratorIdOrThis
	public void test006() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    int x(@Marker int [] @Marker ... p) { return 10; };\n" +
					"    Zork z;\n" +
					"}\n"
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	int x(@Marker int [] @Marker ... p) { return 10; };\n" + 
				"	       ^^^^^^\n" + 
				"Marker cannot be resolved to a type\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 2)\n" + 
				"	int x(@Marker int [] @Marker ... p) { return 10; };\n" + 
				"	                      ^^^^^^\n" + 
				"Marker cannot be resolved to a type\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 3)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n");
	}
	// UnionType ::= Type
	// UnionType ::= UnionType '|' Type
	public void test007() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    int x() {\n" +
					"        try {\n" +
					"        } catch (@Marker NullPointerException | @Marker ArrayIndexOutOfBoundsException e) {\n" +
					"        }\n" +
					"        return 10;\n" +
					"    }\n" +
					"    Zork z;\n" +
					"}\n"
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	} catch (@Marker NullPointerException | @Marker ArrayIndexOutOfBoundsException e) {\n" + 
				"	          ^^^^^^\n" + 
				"Marker cannot be resolved to a type\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	} catch (@Marker NullPointerException | @Marker ArrayIndexOutOfBoundsException e) {\n" + 
				"	                                         ^^^^^^\n" + 
				"Marker cannot be resolved to a type\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 8)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n");
	}	
	// LocalVariableDeclaration ::= Type PushModifiers VariableDeclarators
    // LocalVariableDeclaration ::= Modifiers Type PushRealModifiers VariableDeclarators
	public void test008() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    int x() {\n" +
					"        @Marker int p;\n" +
					"        final @Marker int q;\n" +
					"        @Marker final int r;\n" +
					"        return 10;\n" +
					"    }\n" +
					"    Zork z;\n" +
					"}\n" +
					"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_PARAMETER)\n" +
					"@interface Marker {}\n",
					
					"java/lang/annotation/ElementType.java",
					"package java.lang.annotation;\n"+
					"public enum ElementType {\n" +
					"    TYPE,\n" +
					"    FIELD,\n" +
					"    METHOD,\n" +
					"    PARAMETER,\n" +
					"    CONSTRUCTOR,\n" +
					"    LOCAL_VARIABLE,\n" +
					"    ANNOTATION_TYPE,\n" +
					"    PACKAGE,\n" +
					"    TYPE_PARAMETER,\n" +
					"    TYPE_USE\n" +
					"}\n"
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	@Marker int p;\n" + 
				"	^^^^^^^\n" + 
				"The annotation @Marker is disallowed for this location\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	final @Marker int q;\n" + 
				"	      ^^^^^^^\n" + 
				"The annotation @Marker is disallowed for this location\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 5)\n" + 
				"	@Marker final int r;\n" + 
				"	^^^^^^^\n" + 
				"The annotation @Marker is disallowed for this location\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 8)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n");
	}
	// Resource ::= Type PushModifiers VariableDeclaratorId EnterVariable '=' ForceNoDiet VariableInitializer RestoreDiet ExitVariableWithInitialization
	// Resource ::= Modifiers Type PushRealModifiers VariableDeclaratorId EnterVariable '=' ForceNoDiet VariableInitializer RestoreDiet ExitVariableWithInitialization
	public void test009() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    int x() {\n" +
					"        try (@Marker Integer p = null; final @Marker Integer q = null; @Marker final Integer r = null) {\n" +
					"        }\n" +
					"        return 10;\n" +
					"    }\n" +
					"    Zork z;\n" +
					"}\n" +
					"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_PARAMETER)\n" +
					"@interface Marker {}\n",
					
					"java/lang/annotation/ElementType.java",
					"package java.lang.annotation;\n"+
					"public enum ElementType {\n" +
					"    TYPE,\n" +
					"    FIELD,\n" +
					"    METHOD,\n" +
					"    PARAMETER,\n" +
					"    CONSTRUCTOR,\n" +
					"    LOCAL_VARIABLE,\n" +
					"    ANNOTATION_TYPE,\n" +
					"    PACKAGE,\n" +
					"    TYPE_PARAMETER,\n" +
					"    TYPE_USE\n" +
					"}\n"
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	try (@Marker Integer p = null; final @Marker Integer q = null; @Marker final Integer r = null) {\n" + 
				"	     ^^^^^^^\n" + 
				"The annotation @Marker is disallowed for this location\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 3)\n" + 
				"	try (@Marker Integer p = null; final @Marker Integer q = null; @Marker final Integer r = null) {\n" + 
				"	             ^^^^^^^\n" + 
				"The resource type Integer does not implement java.lang.AutoCloseable\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 3)\n" + 
				"	try (@Marker Integer p = null; final @Marker Integer q = null; @Marker final Integer r = null) {\n" + 
				"	                                     ^^^^^^^\n" + 
				"The annotation @Marker is disallowed for this location\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 3)\n" + 
				"	try (@Marker Integer p = null; final @Marker Integer q = null; @Marker final Integer r = null) {\n" + 
				"	                                             ^^^^^^^\n" + 
				"The resource type Integer does not implement java.lang.AutoCloseable\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 3)\n" + 
				"	try (@Marker Integer p = null; final @Marker Integer q = null; @Marker final Integer r = null) {\n" + 
				"	                                                               ^^^^^^^\n" + 
				"The annotation @Marker is disallowed for this location\n" + 
				"----------\n" + 
				"6. ERROR in X.java (at line 3)\n" + 
				"	try (@Marker Integer p = null; final @Marker Integer q = null; @Marker final Integer r = null) {\n" + 
				"	                                                                             ^^^^^^^\n" + 
				"The resource type Integer does not implement java.lang.AutoCloseable\n" + 
				"----------\n" + 
				"7. ERROR in X.java (at line 7)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n");
	}
	// EnhancedForStatementHeaderInit ::= 'for' '(' Type PushModifiers Identifier Dimsopt
	// EnhancedForStatementHeaderInit ::= 'for' '(' Modifiers Type PushRealModifiers Identifier Dimsopt
	public void test010() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    int x() {\n" +
					"        for (@Marker int i: new int[3]) {}\n" +
					"        for (final @Marker int i: new int[3]) {}\n" +
					"        for (@Marker final int i: new int[3]) {}\n" +
					"        return 10;\n" +
					"    }\n" +
					"    Zork z;\n" +
					"}\n" +
					"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_PARAMETER)\n" +
					"@interface Marker {}\n",
					
					"java/lang/annotation/ElementType.java",
					"package java.lang.annotation;\n"+
					"public enum ElementType {\n" +
					"    TYPE,\n" +
					"    FIELD,\n" +
					"    METHOD,\n" +
					"    PARAMETER,\n" +
					"    CONSTRUCTOR,\n" +
					"    LOCAL_VARIABLE,\n" +
					"    ANNOTATION_TYPE,\n" +
					"    PACKAGE,\n" +
					"    TYPE_PARAMETER,\n" +
					"    TYPE_USE\n" +
					"}\n"
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	for (@Marker int i: new int[3]) {}\n" + 
				"	     ^^^^^^^\n" + 
				"The annotation @Marker is disallowed for this location\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	for (final @Marker int i: new int[3]) {}\n" + 
				"	           ^^^^^^^\n" + 
				"The annotation @Marker is disallowed for this location\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 5)\n" + 
				"	for (@Marker final int i: new int[3]) {}\n" + 
				"	     ^^^^^^^\n" + 
				"The annotation @Marker is disallowed for this location\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 8)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n");
	}	
	// AnnotationMethodHeaderName ::= Modifiersopt TypeParameters Type 'Identifier' '('
	// AnnotationMethodHeaderName ::= Modifiersopt Type 'Identifier' '('
	public void test011() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public @interface X { \n" +
					"	public @Marker String value(); \n" +
					"	@Marker String value2(); \n" +
					"	@Marker public String value3(); \n" +
					"	public @Marker <T> @Marker String value4(); \n" +
					"	@Marker <T> @Marker String value5(); \n" +
					"	@Marker public <T> @Marker String value6(); \n" +
					"}\n" +
					
					"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_PARAMETER)\n" +
					"@interface Marker {}\n",
					
					"java/lang/annotation/ElementType.java",
					"package java.lang.annotation;\n"+
					"public enum ElementType {\n" +
					"    TYPE,\n" +
					"    FIELD,\n" +
					"    METHOD,\n" +
					"    PARAMETER,\n" +
					"    CONSTRUCTOR,\n" +
					"    LOCAL_VARIABLE,\n" +
					"    ANNOTATION_TYPE,\n" +
					"    PACKAGE,\n" +
					"    TYPE_PARAMETER,\n" +
					"    TYPE_USE\n" +
					"}\n"
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	public @Marker String value(); \n" + 
				"	       ^^^^^^^\n" + 
				"The annotation @Marker is disallowed for this location\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 3)\n" + 
				"	@Marker String value2(); \n" + 
				"	^^^^^^^\n" + 
				"The annotation @Marker is disallowed for this location\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 4)\n" + 
				"	@Marker public String value3(); \n" + 
				"	^^^^^^^\n" + 
				"The annotation @Marker is disallowed for this location\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 5)\n" + 
				"	public @Marker <T> @Marker String value4(); \n" + 
				"	       ^^^^^^^\n" + 
				"The annotation @Marker is disallowed for this location\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 5)\n" + 
				"	public @Marker <T> @Marker String value4(); \n" + 
				"	                   ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"6. ERROR in X.java (at line 5)\n" + 
				"	public @Marker <T> @Marker String value4(); \n" + 
				"	                                  ^^^^^^^^\n" + 
				"Annotation attributes cannot be generic\n" + 
				"----------\n" + 
				"7. ERROR in X.java (at line 6)\n" + 
				"	@Marker <T> @Marker String value5(); \n" + 
				"	^^^^^^^\n" + 
				"The annotation @Marker is disallowed for this location\n" + 
				"----------\n" + 
				"8. ERROR in X.java (at line 6)\n" + 
				"	@Marker <T> @Marker String value5(); \n" + 
				"	            ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"9. ERROR in X.java (at line 6)\n" + 
				"	@Marker <T> @Marker String value5(); \n" + 
				"	                           ^^^^^^^^\n" + 
				"Annotation attributes cannot be generic\n" + 
				"----------\n" + 
				"10. ERROR in X.java (at line 7)\n" + 
				"	@Marker public <T> @Marker String value6(); \n" + 
				"	^^^^^^^\n" + 
				"The annotation @Marker is disallowed for this location\n" + 
				"----------\n" + 
				"11. ERROR in X.java (at line 7)\n" + 
				"	@Marker public <T> @Marker String value6(); \n" + 
				"	                   ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"12. ERROR in X.java (at line 7)\n" + 
				"	@Marker public <T> @Marker String value6(); \n" + 
				"	                                  ^^^^^^^^\n" + 
				"Annotation attributes cannot be generic\n" + 
				"----------\n");
	}
	// PrimaryNoNewArray ::= PrimitiveType Dims '.' 'class'
	// PrimaryNoNewArray ::= PrimitiveType '.' 'class'
	public void test012() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X { \n" +
					"	public void value() {\n" +
					"		Object o = @Marker int.class;\n" +
					"		Object o2 = @Marker int @Marker[] [] @Marker[].class;\n" +
					"   }\n" +
					"}\n"
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	Object o = @Marker int.class;\n" + 
				"	           ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	Object o2 = @Marker int @Marker[] [] @Marker[].class;\n" + 
				"	            ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 4)\n" + 
				"	Object o2 = @Marker int @Marker[] [] @Marker[].class;\n" + 
				"	                        ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 4)\n" + 
				"	Object o2 = @Marker int @Marker[] [] @Marker[].class;\n" + 
				"	                                     ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n");
	}
	// ReferenceExpression ::= PrimitiveType Dims '::' NonWildTypeArgumentsopt IdentifierOrNew
	public void test013() throws Exception {  // WILL FAIL WHEN REFERENCE EXPRESSIONS ARE ANALYZED.
		this.runNegativeTest(
				new String[] {
					"X.java",
					"interface I {\n" +
					"    Object copy(int [] ia);\n" +
					"}\n" +
					"public class X  {\n" +
					"    public static void main(String [] args) {\n" +
					"        I i = @Marker int @Marker []::<String>clone;\n" +
					"        Zork z;\n" +
					"    }\n" +
					"}\n"
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 7)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n");
	}
	// ArrayCreationWithoutArrayInitializer ::= 'new' PrimitiveType DimWithOrWithOutExprs
	// ArrayCreationWithArrayInitializer ::= 'new' PrimitiveType DimWithOrWithOutExprs ArrayInitializer
	public void test014() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X  {\n" +
					"    public static void main(String [] args) {\n" +
					"        int i [] = new @Marker int @Marker [4];\n" +
					"        int j [] = new @Marker int @Marker [] { 10 };\n" +
					"        Zork z;\n" +
					"    }\n" +
					"}\n"
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	int i [] = new @Marker int @Marker [4];\n" + 
				"	                ^^^^^^\n" + 
				"Marker cannot be resolved to a type\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 3)\n" + 
				"	int i [] = new @Marker int @Marker [4];\n" + 
				"	                            ^^^^^^\n" + 
				"Marker cannot be resolved to a type\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 4)\n" + 
				"	int j [] = new @Marker int @Marker [] { 10 };\n" + 
				"	                ^^^^^^\n" + 
				"Marker cannot be resolved to a type\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 4)\n" + 
				"	int j [] = new @Marker int @Marker [] { 10 };\n" + 
				"	                            ^^^^^^\n" + 
				"Marker cannot be resolved to a type\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 5)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n");
	}
	// CastExpression ::= PushLPAREN PrimitiveType Dimsopt PushRPAREN InsideCastExpression UnaryExpression
	public void test015() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X  {\n" +
					"    public static void main(String [] args) {\n" +
					"        int i = (@Marker int) 0;\n" +
					"        int j [] = (@Marker int @Marker []) null;\n" +
					"        Zork z;\n" +
					"    }\n" +
					"}\n"
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	int i = (@Marker int) 0;\n" + 
				"	          ^^^^^^\n" + 
				"Marker cannot be resolved to a type\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	int j [] = (@Marker int @Marker []) null;\n" + 
				"	             ^^^^^^\n" + 
				"Marker cannot be resolved to a type\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 4)\n" + 
				"	int j [] = (@Marker int @Marker []) null;\n" + 
				"	                         ^^^^^^\n" + 
				"Marker cannot be resolved to a type\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 5)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n");
	}
	// InstanceofExpression ::= InstanceofExpression 'instanceof' ReferenceType
	public void test016() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X  {\n" +
					"    public static void main(String [] args) {\n" +
					"        if (args instanceof @Readonly String) {\n" +
					"        }\n" +
					"    }\n" +
					"}\n"
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	if (args instanceof @Readonly String) {\n" + 
				"	    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Incompatible conditional operand types String[] and String\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 3)\n" + 
				"	if (args instanceof @Readonly String) {\n" + 
				"	                     ^^^^^^^^\n" + 
				"Readonly cannot be resolved to a type\n" + 
				"----------\n");
	}
	// TypeArgument ::= ReferenceType
	public void test017() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X extends Y<@Marker Integer, String> {}\n" +
					"class Y<T, V> {\n" +
				    "    Zork z;\n" +
					"}\n"
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 1)\n" + 
				"	public class X extends Y<@Marker Integer, String> {}\n" + 
				"	                          ^^^^^^\n" + 
				"Marker cannot be resolved to a type\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 3)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n");
	}
	// ReferenceType1 ::= ReferenceType '>'
	public void test018() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X extends Y<@Marker Integer> {}\n" +
					"class Y<T> {\n" +
				    "    Zork z;\n" +
					"}\n"
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 1)\n" + 
				"	public class X extends Y<@Marker Integer> {}\n" + 
				"	                          ^^^^^^\n" + 
				"Marker cannot be resolved to a type\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 3)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n");
	}
	
	// ReferenceType2 ::= ReferenceType '>>'
	public void test019() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X<T extends Object & Comparable<? super @Marker String>> {}\n" +
					"class Y<T> {\n" +
				    "    Zork z;\n" +
					"}\n"
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n");
	}
	// ReferenceType3 ::= ReferenceType '>>>'
	public void test020() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X<A extends X<X<X<@Marker String>>>> {}\n" +
					"class Y<T> {\n" +
				    "    Zork z;\n" +
					"}\n"
 				},
 				"----------\n" + 
				"1. ERROR in X.java (at line 1)\n" + 
				"	public class X<A extends X<X<X<@Marker String>>>> {}\n" + 
				"	                           ^\n" + 
				"Bound mismatch: The type X<X<String>> is not a valid substitute for the bounded parameter <A extends X<X<X<String>>>> of the type X<A>\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 1)\n" + 
				"	public class X<A extends X<X<X<@Marker String>>>> {}\n" + 
				"	                             ^\n" + 
				"Bound mismatch: The type X<String> is not a valid substitute for the bounded parameter <A extends X<X<X<String>>>> of the type X<A>\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 1)\n" + 
				"	public class X<A extends X<X<X<@Marker String>>>> {}\n" + 
				"	                               ^^^^^^^^^^^^^^\n" + 
				"Bound mismatch: The type String is not a valid substitute for the bounded parameter <A extends X<X<X<String>>>> of the type X<A>\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 1)\n" + 
				"	public class X<A extends X<X<X<@Marker String>>>> {}\n" + 
				"	                                ^^^^^^\n" + 
				"Marker cannot be resolved to a type\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 3)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n");
	}
	// WildcardBounds ::= 'extends' ReferenceType
	// WildcardBounds ::= 'super' ReferenceType
	public void test021() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	void foo(Map<@Marker ? super @Marker Object, @Marker ? extends @Marker String> m){}\n" +
					"   void goo(Map<@Marker ? extends @Marker Object, @Marker ? super @Marker String> m){}\n" +
					"}\n"
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	void foo(Map<@Marker ? super @Marker Object, @Marker ? extends @Marker String> m){}\n" + 
				"	         ^^^\n" + 
				"Map cannot be resolved to a type\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 2)\n" + 
				"	void foo(Map<@Marker ? super @Marker Object, @Marker ? extends @Marker String> m){}\n" + 
				"	              ^^^^^^\n" + 
				"Marker cannot be resolved to a type\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 2)\n" + 
				"	void foo(Map<@Marker ? super @Marker Object, @Marker ? extends @Marker String> m){}\n" + 
				"	                              ^^^^^^\n" + 
				"Marker cannot be resolved to a type\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 2)\n" + 
				"	void foo(Map<@Marker ? super @Marker Object, @Marker ? extends @Marker String> m){}\n" + 
				"	                                              ^^^^^^\n" + 
				"Marker cannot be resolved to a type\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 2)\n" + 
				"	void foo(Map<@Marker ? super @Marker Object, @Marker ? extends @Marker String> m){}\n" + 
				"	                                                                ^^^^^^\n" + 
				"Marker cannot be resolved to a type\n" + 
				"----------\n" + 
				"6. ERROR in X.java (at line 3)\n" + 
				"	void goo(Map<@Marker ? extends @Marker Object, @Marker ? super @Marker String> m){}\n" + 
				"	         ^^^\n" + 
				"Map cannot be resolved to a type\n" + 
				"----------\n" + 
				"7. ERROR in X.java (at line 3)\n" + 
				"	void goo(Map<@Marker ? extends @Marker Object, @Marker ? super @Marker String> m){}\n" + 
				"	              ^^^^^^\n" + 
				"Marker cannot be resolved to a type\n" + 
				"----------\n" + 
				"8. ERROR in X.java (at line 3)\n" + 
				"	void goo(Map<@Marker ? extends @Marker Object, @Marker ? super @Marker String> m){}\n" + 
				"	                                ^^^^^^\n" + 
				"Marker cannot be resolved to a type\n" + 
				"----------\n" + 
				"9. ERROR in X.java (at line 3)\n" + 
				"	void goo(Map<@Marker ? extends @Marker Object, @Marker ? super @Marker String> m){}\n" + 
				"	                                                ^^^^^^\n" + 
				"Marker cannot be resolved to a type\n" + 
				"----------\n" + 
				"10. ERROR in X.java (at line 3)\n" + 
				"	void goo(Map<@Marker ? extends @Marker Object, @Marker ? super @Marker String> m){}\n" + 
				"	                                                                ^^^^^^\n" + 
				"Marker cannot be resolved to a type\n" + 
				"----------\n");
	}
	// TypeParameter ::= TypeParameterHeader 'extends' ReferenceType
	public void test022() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X <@Marker T extends @Marker Y<@Marker ?>, @Marker Q extends @Marker Integer> {\n" +
					"}\n" +
					"class Y<T> {}\n"
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 1)\n" + 
				"	public class X <@Marker T extends @Marker Y<@Marker ?>, @Marker Q extends @Marker Integer> {\n" + 
				"	                 ^^^^^^\n" + 
				"Marker cannot be resolved to a type\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 1)\n" + 
				"	public class X <@Marker T extends @Marker Y<@Marker ?>, @Marker Q extends @Marker Integer> {\n" + 
				"	                                   ^^^^^^\n" + 
				"Marker cannot be resolved to a type\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 1)\n" + 
				"	public class X <@Marker T extends @Marker Y<@Marker ?>, @Marker Q extends @Marker Integer> {\n" + 
				"	                                             ^^^^^^\n" + 
				"Marker cannot be resolved to a type\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 1)\n" + 
				"	public class X <@Marker T extends @Marker Y<@Marker ?>, @Marker Q extends @Marker Integer> {\n" + 
				"	                                                         ^^^^^^\n" + 
				"Marker cannot be resolved to a type\n" + 
				"----------\n" + 
				"5. WARNING in X.java (at line 1)\n" + 
				"	public class X <@Marker T extends @Marker Y<@Marker ?>, @Marker Q extends @Marker Integer> {\n" + 
				"	                                                                          ^^^^^^^^^^^^^^^\n" + 
				"The type parameter Q should not be bounded by the final type Integer. Final types cannot be further extended\n" + 
				"----------\n" + 
				"6. ERROR in X.java (at line 1)\n" + 
				"	public class X <@Marker T extends @Marker Y<@Marker ?>, @Marker Q extends @Marker Integer> {\n" + 
				"	                                                                           ^^^^^^\n" + 
				"Marker cannot be resolved to a type\n" + 
				"----------\n");
	}
	// TypeParameter ::= TypeParameterHeader 'extends' ReferenceType AdditionalBoundList
	// AdditionalBound ::= '&' ReferenceType
	// TypeParameter1 ::= TypeParameterHeader 'extends' ReferenceType AdditionalBoundList1
	public void test023() throws Exception {
		this.runNegativeTest(
				new String[] {
					"I.java",
					"public interface I<U extends J<? extends I<U>>> {\n" +
					"}\n" +
					"interface J<T extends I<? extends J<T>>> {\n" +
					"}\n" +
					"class CI<U extends CJ<T, U> & @Marker J<@Marker T>,\n" +
					"			T extends CI<U, T> & @Marker I<U>>\n" +
					"	implements I<U> {\n" +
					"}\n" +
					"class CJ<T extends CI<U, T> & @Marker I<@Marker U>,\n" +
					"			U extends CJ<T, U> & J<T>>\n" +
					"	implements J<T> {\n" +
					"}\n"
				},
				"----------\n" + 
				"1. ERROR in I.java (at line 5)\n" + 
				"	class CI<U extends CJ<T, U> & @Marker J<@Marker T>,\n" + 
				"	                               ^^^^^^\n" + 
				"Marker cannot be resolved to a type\n" + 
				"----------\n" + 
				"2. ERROR in I.java (at line 5)\n" + 
				"	class CI<U extends CJ<T, U> & @Marker J<@Marker T>,\n" + 
				"	                                         ^^^^^^\n" + 
				"Marker cannot be resolved to a type\n" + 
				"----------\n" + 
				"3. ERROR in I.java (at line 6)\n" + 
				"	T extends CI<U, T> & @Marker I<U>>\n" + 
				"	                      ^^^^^^\n" + 
				"Marker cannot be resolved to a type\n" + 
				"----------\n" + 
				"4. ERROR in I.java (at line 9)\n" + 
				"	class CJ<T extends CI<U, T> & @Marker I<@Marker U>,\n" + 
				"	                               ^^^^^^\n" + 
				"Marker cannot be resolved to a type\n" + 
				"----------\n" + 
				"5. ERROR in I.java (at line 9)\n" + 
				"	class CJ<T extends CI<U, T> & @Marker I<@Marker U>,\n" + 
				"	                                         ^^^^^^\n" + 
				"Marker cannot be resolved to a type\n" + 
				"----------\n");
	}
	// InstanceofExpression_NotName ::= Name 'instanceof' ReferenceType
	public void test024() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X<E> {\n" +
					"  class Y {\n" +
					"    E e;\n" +
					"    E getOtherElement(Object other) {\n" +
					"      if (!(other instanceof @Marker X<?>.Y)) {};\n" +
					"      return null;\n" +
					"    }\n" +
					"  }\n" +
					"}\n"
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 5)\n" + 
				"	if (!(other instanceof @Marker X<?>.Y)) {};\n" + 
				"	                        ^^^^^^\n" + 
				"Marker cannot be resolved to a type\n" + 
				"----------\n");
	}
	// InstanceofExpression_NotName ::= InstanceofExpression_NotName 'instanceof' ReferenceType
	public void test025() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X<P, C> {\n" +
					"  public X() {\n" +
					"    if (!(this instanceof @Marker X)) {}\n" +
					"  }\n" +
					"}\n"
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	if (!(this instanceof @Marker X)) {}\n" + 
				"	                       ^^^^^^\n" + 
				"Marker cannot be resolved to a type\n" + 
				"----------\n");
	}	
}