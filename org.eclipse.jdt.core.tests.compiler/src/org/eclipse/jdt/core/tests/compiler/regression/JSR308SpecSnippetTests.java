/*******************************************************************************
 * Copyright (c) 2011, 2013 IBM Corporation and others.
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
 * 
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Map;

import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class JSR308SpecSnippetTests extends AbstractRegressionTest {

	static {
//		TESTS_NUMBERS = new int [] { 19 };
//		TESTS_NAMES = new String [] { "testAbbreviatedInnerClass" };
	}
	public static Class testClass() {
		return JSR308SpecSnippetTests.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_8);
	}
	public JSR308SpecSnippetTests(String testName){
		super(testName);
	}

	// Enables the tests to run individually
	protected Map getCompilerOptions() {
		Map defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_8);
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_8);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_8);
		return defaultOptions;
	}
	
	public void test001() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.Map;\n" +
				"import java.util.List;\n" +
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface NonNull {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface NonEmpty {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface Readonly {}\n" +
				"class Document {}\n" +
				"public class X {\n" +
				"	Map<@NonNull String, @NonEmpty List<@Readonly Document>> files;\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"    RuntimeInvisibleTypeAnnotations: \n" + 
				"      #10 @NonNull(\n" + 
				"        target type = 0x13 FIELD\n" + 
				"        location = [TYPE_ARGUMENT(0)]\n" + 
				"      )\n" + 
				"      #11 @NonEmpty(\n" + 
				"        target type = 0x13 FIELD\n" + 
				"        location = [TYPE_ARGUMENT(1)]\n" + 
				"      )\n" + 
				"      #12 @Readonly(\n" + 
				"        target type = 0x13 FIELD\n" + 
				"        location = [TYPE_ARGUMENT(1), TYPE_ARGUMENT(0)]\n" + 
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// note, javac 8b100 emits offset incorrectly.
	public void test002() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface NonNull {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface NonEmpty {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface Readonly {}\n" +
				"class Document {}\n" +
				"public class X {\n" +
				"	static void foo(X o) {\n" +
				"		o.<@NonNull String>m(\"...\");\n" +
				"	}\n" +
				"	<T> void m(String s) {}\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"    RuntimeInvisibleTypeAnnotations: \n" + 
				"      #24 @NonNull(\n" + 
				"        target type = 0x49 METHOD_INVOCATION_TYPE_ARGUMENT\n" + 
				"        offset = 3\n" + 
				"        type argument index = 0\n" + 
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	
	public void test003() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import java.util.Collection;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface Existing {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface NonEmpty {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface Readonly {}\n" +
				"class File {}\n" +
				"class X<F extends @Existing File> { \n" +
				"	Collection<? super @Existing File> c;\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"    RuntimeInvisibleTypeAnnotations: \n" + 
				"      #10 @Existing(\n" + 
				"        target type = 0x13 FIELD\n" + 
				"        location = [TYPE_ARGUMENT(0), WILDCARD]\n" + 
				"      )\n" + 
				"  \n" + 
				"  // Method descriptor #12 ()V\n" + 
				"  // Stack: 1, Locals: 1\n" + 
				"  X();\n" + 
				"    0  aload_0 [this]\n" + 
				"    1  invokespecial java.lang.Object() [14]\n" + 
				"    4  return\n" + 
				"      Line numbers:\n" + 
				"        [pc: 0, line: 11]\n" + 
				"      Local variable table:\n" + 
				"        [pc: 0, pc: 5] local: this index: 0 type: X\n" + 
				"      Local variable type table:\n" + 
				"        [pc: 0, pc: 5] local: this index: 0 type: X<F>\n" + 
				"\n" + 
				"  RuntimeInvisibleTypeAnnotations: \n" + 
				"    #10 @Existing(\n" + 
				"      target type = 0x11 CLASS_TYPE_PARAMETER_BOUND\n" + 
				"      type parameter index = 0 type parameter bound index = 0\n" + 
				"    )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test004() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import java.util.List;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface Existing {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface NonEmpty {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface Readonly {}\n" +
				"class File {}\n" +
				"abstract class X<T> implements @Readonly List<@Readonly T> { }\n",
		},
		"");
		String expectedOutput =
				"  RuntimeInvisibleTypeAnnotations: \n" + 
				"    #23 @Readonly(\n" + 
				"      target type = 0x10 CLASS_EXTENDS\n" + 
				"      type index = 0\n" + 
				"    )\n" + 
				"    #23 @Readonly(\n" + 
				"      target type = 0x10 CLASS_EXTENDS\n" + 
				"      type index = 0\n" + 
				"      location = [TYPE_ARGUMENT(0)]\n" + 
				"    )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test005() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import java.util.List;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface Critical {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface NonEmpty {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface Readonly {}\n" +
				"class TemperatureException extends RuntimeException{}\n" +
				"class X {\n" +
				"	void monitorTemperature() throws @Critical TemperatureException {}\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"    RuntimeInvisibleTypeAnnotations: \n" + 
				"      #19 @Critical(\n" + 
				"        target type = 0x17 THROWS\n" + 
				"        throws index = 0\n" + 
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test006() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface Interned {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface NonEmpty {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface Readonly {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface Tainted {}\n" +
				"class MyObject {\n" +
				"	class NestedClass {}\n" +
				"}\n" +
				"class List<T> {}\n" +
				"class X {\n" +
				"	static void monitorTemperature(MyObject myVar) {\n" +
				"		new <String> @Interned MyObject();\n" +
				"		new @NonEmpty @Readonly List<String>();\n" +
				"		myVar.new @Tainted NestedClass();\n" +
				"	}\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"    RuntimeInvisibleTypeAnnotations: \n" + 
				"      #33 @Interned(\n" + 
				"        target type = 0x44 NEW\n" + 
				"        offset = 0\n" + 
				"      )\n" + 
				"      #34 @NonEmpty(\n" + 
				"        target type = 0x44 NEW\n" + 
				"        offset = 6\n" + 
				"      )\n" + 
				"      #35 @Readonly(\n" + 
				"        target type = 0x44 NEW\n" + 
				"        offset = 6\n" + 
				"      )\n" + 
				"      #36 @Tainted(\n" + 
				"        target type = 0x44 NEW\n" + 
				"        offset = 12\n" + 
				"        location = [INNER_TYPE]\n" + 
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test007() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import java.util.Map;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface NonNull {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface NonEmpty {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface Readonly {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface Tainted {}\n" +
				"class MyObject {\n" +
				"	class NestedClass {}\n" +
				"}\n" +
				"class List<T> {}\n" +
				"class X {\n" +
				"		Map.@NonNull Entry e;\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"    RuntimeInvisibleTypeAnnotations: \n" + 
				"      #8 @NonNull(\n" + 
				"        target type = 0x13 FIELD\n" + 
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test008() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface NonNull {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface NonEmpty {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface A {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface B {}\n" +
				"class MyObject {\n" +
				"	class NestedClass {}\n" +
				"}\n" +
				"class List<T> {}\n" +
				"class Type1 {}\n" +
				"interface Type2 {}\n" +
				"class X {\n" +
				"	static void monitorTemperature(Object myObject) {\n" +
				"		String myString = (@NonNull String) myObject;\n" +
				"		Type1 x = (@A Type1 & @B Type2) null;\n" +
				"	}\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"    RuntimeInvisibleTypeAnnotations: \n" + 
				"      #29 @NonNull(\n" + 
				"        target type = 0x47 CAST\n" + 
				"        offset = 1\n" + 
				"        type argument index = 0\n" + 
				"      )\n" + 
				"      #30 @A(\n" + 
				"        target type = 0x47 CAST\n" + 
				"        offset = 6\n" + 
				"        type argument index = 0\n" + 
				"      )\n" + 
				"      #31 @B(\n" + 
				"        target type = 0x47 CAST\n" + 
				"        offset = 6\n" + 
				"        type argument index = 1\n" + 
				"      )\n" + 
				"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test009() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface NonNull {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface NonEmpty {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface A {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface B {}\n" +
				"class MyObject {\n" +
				"	class NestedClass {}\n" +
				"}\n" +
				"class List<T> {}\n" +
				"class Type1 {}\n" +
				"interface Type2 {}\n" +
				"class X {\n" +
				"	static void monitorTemperature(Object myObject) {\n" +
				"		boolean isNonNull = myObject instanceof @NonNull String;\n" +
				"	}\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"    RuntimeInvisibleTypeAnnotations: \n" + 
				"      #23 @NonNull(\n" + 
				"        target type = 0x43 INSTANCEOF\n" + 
				"        offset = 1\n" + 
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test010() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import java.util.Arrays;\n" +
				"import java.util.Date;\n" +
				"import java.util.List;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface NonNull {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface English {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface Vernal {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface NonNegative {}\n" +
				"class MyObject {\n" +
				"	class NestedClass {}\n" +
				"}\n" +
				"class Type1 {}\n" +
				"interface I {\n" +
				"	int f(Date d);\n" +
				"}\n" +
				"interface J {\n" +
				"	int f(List l);\n" +
				"}\n" +
				"interface K {\n" +
				"	void s(int [] ia);\n" +
				"}\n" +
				"class X {\n" +
				"	static void monitorTemperature(Object myObject) {\n" +
				"		I i = @Vernal Date::getDay;\n" +
				"		J j  = List<@English String>::size;\n" +
				"		K k = Arrays::<@NonNegative Integer>sort;\n" +
				"	}\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"    RuntimeInvisibleTypeAnnotations: \n" + 
				"      #35 @Vernal(\n" + 
				"        target type = 0x46 METHOD_REFERENCE\n" + 
				"        offset = 0\n" + 
				"      )\n" + 
				"      #36 @English(\n" + 
				"        target type = 0x46 METHOD_REFERENCE\n" + 
				"        offset = 6\n" + 
				"        location = [TYPE_ARGUMENT(0)]\n" + 
				"      )\n" + 
				"      #37 @NonNegative(\n" + 
				"        target type = 0x4b METHOD_REFERENCE_TYPE_ARGUMENT\n" + 
				"        offset = 12\n" + 
				"        type argument index = 0\n" + 
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test011() throws Exception { // WILL FAIL WHEN https://bugs.eclipse.org/bugs/show_bug.cgi?id=415397 IS FIXED.
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import java.util.List;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface Immutable {}\n" +
				"class X {\n" +
				"	List<@Immutable ? extends Comparable<X>> x;\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"class X {\n" + 
				"  Constant pool:\n" + 
				"    constant #1 class: #2 X\n" + 
				"    constant #2 utf8: \"X\"\n" + 
				"    constant #3 class: #4 java/lang/Object\n" + 
				"    constant #4 utf8: \"java/lang/Object\"\n" + 
				"    constant #5 utf8: \"x\"\n" + 
				"    constant #6 utf8: \"Ljava/util/List;\"\n" + 
				"    constant #7 utf8: \"Signature\"\n" + 
				"    constant #8 utf8: \"Ljava/util/List<+Ljava/lang/Comparable<LX;>;>;\"\n" + 
				"    constant #9 utf8: \"<init>\"\n" + 
				"    constant #10 utf8: \"()V\"\n" + 
				"    constant #11 utf8: \"Code\"\n" + 
				"    constant #12 method_ref: #3.#13 java/lang/Object.<init> ()V\n" + 
				"    constant #13 name_and_type: #9.#10 <init> ()V\n" + 
				"    constant #14 utf8: \"LineNumberTable\"\n" + 
				"    constant #15 utf8: \"LocalVariableTable\"\n" + 
				"    constant #16 utf8: \"this\"\n" + 
				"    constant #17 utf8: \"LX;\"\n" + 
				"    constant #18 utf8: \"SourceFile\"\n" + 
				"    constant #19 utf8: \"X.java\"\n" + 
				"  \n" + 
				"  // Field descriptor #6 Ljava/util/List;\n" + 
				"  // Signature: Ljava/util/List<+Ljava/lang/Comparable<LX;>;>;\n" + 
				"  java.util.List x;\n" + 
				"  \n" + 
				"  // Method descriptor #10 ()V\n" + 
				"  // Stack: 1, Locals: 1\n" + 
				"  X();\n" + 
				"    0  aload_0 [this]\n" + 
				"    1  invokespecial java.lang.Object() [12]\n" + 
				"    4  return\n" + 
				"      Line numbers:\n" + 
				"        [pc: 0, line: 6]\n" + 
				"      Local variable table:\n" + 
				"        [pc: 0, pc: 5] local: this index: 0 type: X\n" + 
				"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test012() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface Readonly {}\n" +
				"class Document {}\n" +
				"class X {\n" +
				"	@Readonly Document [][] docs1 = new @Readonly Document [2][12]; // array of arrays of read-only documents\n" +
				"	Document @Readonly [][] docs2 = new Document @Readonly [2][12]; // read-only array of arrays of documents\n" +
				"	Document[] @Readonly [] docs3 = new Document[2] @Readonly [12]; // array of read-only arrays of documents\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"  Document[][] docs1;\n" + 
				"    RuntimeInvisibleTypeAnnotations: \n" + 
				"      #8 @Readonly(\n" + 
				"        target type = 0x13 FIELD\n" + 
				"        location = [ARRAY, ARRAY]\n" + 
				"      )\n" + 
				"  \n" + 
				"  // Field descriptor #6 [[LDocument;\n" + 
				"  Document[][] docs2;\n" + 
				"    RuntimeInvisibleTypeAnnotations: \n" + 
				"      #8 @Readonly(\n" + 
				"        target type = 0x13 FIELD\n" + 
				"      )\n" + 
				"  \n" + 
				"  // Field descriptor #6 [[LDocument;\n" + 
				"  Document[][] docs3;\n" + 
				"    RuntimeInvisibleTypeAnnotations: \n" + 
				"      #8 @Readonly(\n" + 
				"        target type = 0x13 FIELD\n" + 
				"        location = [ARRAY]\n" + 
				"      )\n" + 
				"  \n" + 
				"  // Method descriptor #12 ()V\n" + 
				"  // Stack: 3, Locals: 1\n" + 
				"  X();\n" + 
				"     0  aload_0 [this]\n" + 
				"     1  invokespecial java.lang.Object() [14]\n" + 
				"     4  aload_0 [this]\n" + 
				"     5  iconst_2\n" + 
				"     6  bipush 12\n" + 
				"     8  multianewarray Document[][] [16]\n" + 
				"    12  putfield X.docs1 : Document[][] [17]\n" + 
				"    15  aload_0 [this]\n" + 
				"    16  iconst_2\n" + 
				"    17  bipush 12\n" + 
				"    19  multianewarray Document[][] [16]\n" + 
				"    23  putfield X.docs2 : Document[][] [19]\n" + 
				"    26  aload_0 [this]\n" + 
				"    27  iconst_2\n" + 
				"    28  bipush 12\n" + 
				"    30  multianewarray Document[][] [16]\n" + 
				"    34  putfield X.docs3 : Document[][] [21]\n" + 
				"    37  return\n" + 
				"      Line numbers:\n" + 
				"        [pc: 0, line: 6]\n" + 
				"        [pc: 4, line: 7]\n" + 
				"        [pc: 15, line: 8]\n" + 
				"        [pc: 26, line: 9]\n" + 
				"        [pc: 37, line: 6]\n" + 
				"      Local variable table:\n" + 
				"        [pc: 0, pc: 38] local: this index: 0 type: X\n" + 
				"    RuntimeInvisibleTypeAnnotations: \n" + 
				"      #8 @Readonly(\n" + 
				"        target type = 0x44 NEW\n" + 
				"        offset = 8\n" + 
				"        location = [ARRAY, ARRAY]\n" + 
				"      )\n" + 
				"      #8 @Readonly(\n" + 
				"        target type = 0x44 NEW\n" + 
				"        offset = 19\n" + 
				"      )\n" + 
				"      #8 @Readonly(\n" + 
				"        target type = 0x44 NEW\n" + 
				"        offset = 30\n" + 
				"        location = [ARRAY]\n" + 
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test013() throws Exception { // WILL FAIL WHEN https://bugs.eclipse.org/bugs/show_bug.cgi?id=415399 IS FIXED.
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface Immutable {}\n" +
				"class Document {}\n" +
				"class X {\n" +
				"	@Immutable X() {\n" +
				"	}\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"class X {\n" + 
				"  Constant pool:\n" + 
				"    constant #1 class: #2 X\n" + 
				"    constant #2 utf8: \"X\"\n" + 
				"    constant #3 class: #4 java/lang/Object\n" + 
				"    constant #4 utf8: \"java/lang/Object\"\n" + 
				"    constant #5 utf8: \"<init>\"\n" + 
				"    constant #6 utf8: \"()V\"\n" + 
				"    constant #7 utf8: \"Code\"\n" + 
				"    constant #8 method_ref: #3.#9 java/lang/Object.<init> ()V\n" + 
				"    constant #9 name_and_type: #5.#6 <init> ()V\n" + 
				"    constant #10 utf8: \"LineNumberTable\"\n" + 
				"    constant #11 utf8: \"LocalVariableTable\"\n" + 
				"    constant #12 utf8: \"this\"\n" + 
				"    constant #13 utf8: \"LX;\"\n" + 
				"    constant #14 utf8: \"SourceFile\"\n" + 
				"    constant #15 utf8: \"X.java\"\n" + 
				"  \n" + 
				"  // Method descriptor #6 ()V\n" + 
				"  // Stack: 1, Locals: 1\n" + 
				"  X();\n" + 
				"    0  aload_0 [this]\n" + 
				"    1  invokespecial java.lang.Object() [8]\n" + 
				"    4  return\n" + 
				"      Line numbers:\n" + 
				"        [pc: 0, line: 7]\n" + 
				"        [pc: 4, line: 8]\n" + 
				"      Local variable table:\n" + 
				"        [pc: 0, pc: 5] local: this index: 0 type: X\n" + 
				"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test014() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface Immutable {}\n" +
				"class Document {}\n" +
				"interface I {\n" +
				"	void foo();\n" +
				"}\n" +
				"class X {\n" +
				"	void foo (X this, X this) {\n" +
				"	}\n" +
				"	static void foo (X this) {\n" +
				"	}\n" +
				"	I i = (X this) -> {};\n" +
				"}\n" +
				"class Y<T> {\n" +
				"	void foo(X this) {}\n" +
				"	void foo(Y this, int x) {}\n" +
				"	class Z {\n" +
				"		void foo(Y<T>.Z this) {}\n" +
				"	}\n" +
				"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 10)\n" + 
		"	void foo (X this, X this) {\n" + 
		"	                    ^^^^\n" + 
		"Only the first formal parameter may be declared explicitly as \'this\'\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 12)\n" + 
		"	static void foo (X this) {\n" + 
		"	                   ^^^^\n" + 
		"Explicit \'this\' parameter is allowed only in instance methods of non-anonymous classes and inner class constructors\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 14)\n" + 
		"	I i = (X this) -> {};\n" + 
		"	      ^^^^^^^^^^^^^^\n" + 
		"Lambda expression\'s signature does not match the signature of the functional interface method\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 14)\n" + 
		"	I i = (X this) -> {};\n" + 
		"	         ^^^^\n" + 
		"Lambda expressions cannot declare a this parameter\n" + 
		"----------\n" + 
		"5. ERROR in X.java (at line 17)\n" + 
		"	void foo(X this) {}\n" + 
		"	         ^\n" + 
		"The declared type of the explicit \'this\' parameter is expected to be Y<T>\n" + 
		"----------\n" + 
		"6. WARNING in X.java (at line 18)\n" + 
		"	void foo(Y this, int x) {}\n" + 
		"	         ^\n" + 
		"Y is a raw type. References to generic type Y<T> should be parameterized\n" + 
		"----------\n" + 
		"7. ERROR in X.java (at line 18)\n" + 
		"	void foo(Y this, int x) {}\n" + 
		"	         ^\n" + 
		"The declared type of the explicit \'this\' parameter is expected to be Y<T>\n" + 
		"----------\n");
	}
	public void test015() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface Immutable {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface Readonly {}\n" +
				"class Document {}\n" +
				"interface I {\n" +
				"	void foo();\n" +
				"}\n" +
				"class X {\n" +
				"	class Y {\n" +
				"		void foo(@Immutable X.@Readonly Y this) {\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"    RuntimeInvisibleTypeAnnotations: \n" + 
				"      #21 @Immutable(\n" + 
				"        target type = 0x15 METHOD_RECEIVER\n" + 
				"      )\n" + 
				"      #22 @Readonly(\n" + 
				"        target type = 0x15 METHOD_RECEIVER\n" + 
				"        location = [INNER_TYPE]\n" + 
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X$Y.class", "Y", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test016() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface A {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface B {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface C {}\n" +
				"public class X {}\n" +
				"class Outer {\n" +
				"    class Middle {\n" +
				"        class Inner {\n" +
				"            void innerMethod(@A Outer.@B Middle.@C Inner this) { }\n" +
				"        }\n" +
				"    }\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"    RuntimeInvisibleTypeAnnotations: \n" + 
				"      #21 @A(\n" + 
				"        target type = 0x15 METHOD_RECEIVER\n" + 
				"      )\n" + 
				"      #22 @B(\n" + 
				"        target type = 0x15 METHOD_RECEIVER\n" + 
				"        location = [INNER_TYPE]\n" + 
				"      )\n" + 
				"      #23 @C(\n" + 
				"        target type = 0x15 METHOD_RECEIVER\n" + 
				"        location = [INNER_TYPE, INNER_TYPE]\n" + 
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "Outer$Middle$Inner.class", "Inner", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test017() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface Result {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface Receiver {}\n" +
				"class Document {}\n" +
				"interface I {\n" +
				"	void foo();\n" +
				"}\n" +
				"class X {\n" +
				"	class Y {\n" +
				"		 Y(@Receiver X X.this, boolean b) { }\n" +
				"	}\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"    RuntimeInvisibleTypeAnnotations: \n" + 
				"      #22 @Receiver(\n" + 
				"        target type = 0x15 METHOD_RECEIVER\n" + 
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X$Y.class", "Y", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test018() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*;  \n" +
				"@interface Receiver {}\n" +
				"class Document {}\n" +
				"interface I {\n" +
				"	void foo();\n" +
				"}\n" +
				"class X {\n" +
				"	void foo(@Receiver X this) {}\n" +
				"	class Y {\n" +
				"		 Y(@Receiver X X.this, boolean b) { }\n" +
				"	}\n" +
				"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 9)\n" + 
		"	void foo(@Receiver X this) {}\n" + 
		"	         ^^^^^^^^^\n" + 
		"Annotation types that do not specify explicit target element types cannot be applied here\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 11)\n" + 
		"	Y(@Receiver X X.this, boolean b) { }\n" + 
		"	  ^^^^^^^^^\n" + 
		"Annotation types that do not specify explicit target element types cannot be applied here\n" + 
		"----------\n");
	}
	public void test019() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface Readonly {}\n" +
				"class X<@Readonly T> {\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"  RuntimeInvisibleTypeAnnotations: \n" + 
				"    #21 @Readonly(\n" + 
				"      target type = 0x0 CLASS_TYPE_PARAMETER\n" + 
				"      type parameter index = 0\n" + 
				"    )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
}