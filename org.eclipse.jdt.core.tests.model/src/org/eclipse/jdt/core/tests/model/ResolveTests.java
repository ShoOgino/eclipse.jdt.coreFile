/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.ICompilationUnit;

import junit.framework.*;

public class ResolveTests extends AbstractJavaModelTests {


public static Test suite() {
	return new Suite(ResolveTests.class);
}

public ResolveTests(String name) {
	super(name);
}
private IJavaElement[] codeSelect(ISourceReference sourceReference, String selectAt, String selection) throws JavaModelException {
	String str = sourceReference.getSource();
	int start = str.indexOf(selectAt);
	int length = selection.length();
	return ((ICodeAssist)sourceReference).codeSelect(start, length);
}
public void setUpSuite() throws Exception {
	super.setUpSuite();
	
	setUpJavaProject("Resolve");
}
public void tearDownSuite() throws Exception {
	deleteProject("Resolve");
	
	super.tearDownSuite();
}
/**
 * Resolve default abstract method
 * bugs http://dev.eclipse.org/bugs/show_bug.cgi?id=23594
 */
public void testAbstractMethod() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveAbstractMethod.java");
	IJavaElement[] elements = codeSelect(cu, "foo", "foo");
	assertElementsEqual(
		"Unexpected elements",
		"foo() [in SuperInterface [in SuperInterface.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolve an argument name
 */
public void testArgumentName1() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveArgumentName.java");
	IJavaElement[] elements = codeSelect(cu, "var1", "var1");
	assertElementsEqual(
		"Unexpected elements",
		"Object [in Object.class [in java.lang [in " + getExternalJCLPath(). toString() + " [in Resolve]]]]",
		elements
	);
}
/**
 * Resolve an argument name with base type
 */
public void testArgumentName2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveArgumentName.java");
	IJavaElement[] elements = codeSelect(cu, "var2", "var2");
	assertElementsEqual(
		"Unexpected elements",
		"",
		elements
	);
}
/**
 * Resolve the field "length" of an array
 */
public void testArrayLength() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveArrayLength.java");
	IJavaElement[] elements = codeSelect(cu, "length", "length");
	assertElementsEqual(
		"Unexpected elements",
		"",
		elements
	);
}

/**
 * Resolve an argument name inside catch statement
 */
public void testCatchArgumentName1() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveCatchArgumentName.java");
	IJavaElement[] elements = codeSelect(cu, "var1", "var1");
	assertElementsEqual(
		"Unexpected elements",
		"Object [in Object.class [in java.lang [in " + getExternalJCLPath(). toString() + " [in Resolve]]]]",
		elements
	);
}
/**
 * Resolve an argument name inside catch statement with base type
 */
public void testCatchArgumentName2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveCatchArgumentName.java");
	IJavaElement[] elements = codeSelect(cu, "var2", "var2");
	assertElementsEqual(
		"Unexpected elements",
		"",
		elements
	);
}
/**
 * bugs http://dev.eclipse.org/bugs/show_bug.cgi?id=24626
 */
public void testCatchArgumentType1() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveCatchArgumentType1.java");
	IJavaElement[] elements = codeSelect(cu, "Y1", "Y1");
	assertElementsEqual(
		"Unexpected elements",
		"Y1 [in X1 [in X1.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * bugs http://dev.eclipse.org/bugs/show_bug.cgi?id=24626
 */
public void testCatchArgumentType2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveCatchArgumentType2.java");
	IJavaElement[] elements = codeSelect(cu, "Y1", "Y1");
	assertElementsEqual(
		"Unexpected elements",
		"Y1 [in X1 [in X1.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolve the class 'X' (field type).
 */
public void testClass1() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveClass1.java");
	IJavaElement[] elements = codeSelect(cu, "X", "X");
	assertElementsEqual(
		"Unexpected elements",
		"X [in X.java [in p1 [in src [in Resolve]]]]",
		elements
	);
}
/**
 * Resolve the class 'X' (local variable type).
 */
public void testClass2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveClass2.java");
	IJavaElement[] elements = codeSelect(cu, "X", "X");
	assertElementsEqual(
		"Unexpected elements",
		"X [in X.java [in p1 [in src [in Resolve]]]]",
		elements
	);
}
/**
 * Resolve the class 'X'(array initializer type).
 */
public void testClass3() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveClass3.java");
	IJavaElement[] elements = codeSelect(cu, "X[]{", "X");
	assertElementsEqual(
		"Unexpected elements",
		"X [in X.java [in p1 [in src [in Resolve]]]]",
		elements
	);
}
/**
 * Resolve the class 'X' (return type).
 */
public void testClass4() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveClass4.java");
	IJavaElement[] elements = codeSelect(cu, "X", "X");
	assertElementsEqual(
		"Unexpected elements",
		"X [in X.java [in p1 [in src [in Resolve]]]]",
		elements
	);
}
/**
 * Resolve the class 'X' (method argument).
 */
public void testClass5() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveClass5.java");
	IJavaElement[] elements = codeSelect(cu, "X", "X");
	assertElementsEqual(
		"Unexpected elements",
		"X [in X.java [in p1 [in src [in Resolve]]]]",
		elements
	);
}
/**
 * Resolve the class 'SuperClass' (super class).
 */
public void testClass6() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveClass6.java");
	IJavaElement[] elements = codeSelect(cu, "X", "X");
	assertElementsEqual(
		"Unexpected elements",
		"X [in X.java [in p1 [in src [in Resolve]]]]",
		elements
	);
}
/**
 * Resolve a constructor
 */
public void testConstructor() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveConstructor.java");
	IJavaElement[] elements = codeSelect(cu, "ResolveConstructor(\"", "ResolveConstructor");
	assertElementsEqual(
		"Unexpected elements",
		"ResolveConstructor(String) [in ResolveConstructor [in ResolveConstructor.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolve constructor call
 */
public void testConstructorCallOfMemberType() throws JavaModelException {
	IClassFile cf = getClassFile("Resolve", "class-folder", "", "ResolveConstructorCallOfMemberType.class");
	IJavaElement[] elements = codeSelect(cf, "Inner()", "Inner");
	assertElementsEqual(
		"Unexpected elements",
		"Inner(ResolveConstructorCallOfMemberType) [in Inner [in ResolveConstructorCallOfMemberType$Inner.class [in <default> [in class-folder [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolve constructor declaration
 */
public void testConstructorDeclaration() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveConstructorDeclaration.java");
	IJavaElement[] elements = codeSelect(cu, "ResolveConstructorDeclaration(i", "ResolveConstructorDeclaration");
	assertElementsEqual(
		"Unexpected elements",
		"ResolveConstructorDeclaration(int) [in ResolveConstructorDeclaration [in ResolveConstructorDeclaration.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}

/**
 * Resolve empty selection
 */
public void testEmptySelection() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveTypeEmptySelection.java");
	IJavaElement[] elements = codeSelect(cu, "ject", "");
	assertElementsEqual(
		"Unexpected elements",
		"Object [in Object.class [in java.lang [in " + getExternalJCLPath(). toString() + " [in Resolve]]]]",
		elements
	);
}
/**
 * Resolve empty selection
 */
public void testEmptySelection2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveTypeEmptySelection2.java");
	IJavaElement[] elements = codeSelect(cu, "Obj", "");
	assertElementsEqual(
		"Unexpected elements",
		"Object [in Object.class [in java.lang [in " + getExternalJCLPath(). toString() + " [in Resolve]]]]",
		elements
	);
}
/**
 * Resolve empty selection
 */
public void testEmptySelectionOnMethod() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveEmptySelectionOnMethod.java");
	IJavaElement[] elements = codeSelect(cu, "oo();", "");
	assertElementsEqual(
		"Unexpected elements",
		"foo() [in ResolveEmptySelectionOnMethod [in ResolveEmptySelectionOnMethod.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolse explicit super constructor call
 */
public void testExplicitSuperConstructorCall() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveExplicitSuperConstructorCall.java");
	IJavaElement[] elements = codeSelect(cu, "super(", "super");
	assertElementsEqual(
		"Unexpected elements",
		"SuperClass(int) [in SuperClass [in SuperClass.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolse explicit this constructor call
 */
public void testExplicitThisConstructorCall() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveExplicitThisConstructorCall.java");
	IJavaElement[] elements = codeSelect(cu, "this(", "this");
	assertElementsEqual(
		"Unexpected elements",
		"ResolveExplicitThisConstructorCall() [in ResolveExplicitThisConstructorCall [in ResolveExplicitThisConstructorCall.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolve the field "foo"
 */
public void testField() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveField.java");
	IJavaElement[] elements = codeSelect(cu, "foo =", "foo");
	assertElementsEqual(
		"Unexpected elements",
		"foo [in ResolveField [in ResolveField.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolve field declaration
 */
public void testFieldDeclaration() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveFieldDeclaration.java");
	IJavaElement[] elements = codeSelect(cu, "foo", "foo");
	assertElementsEqual(
		"Unexpected elements",
		"foo [in ResolveFieldDeclaration [in ResolveFieldDeclaration.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolve in import
 */
public void testImport() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveImport.java");
	IJavaElement[] elements = codeSelect(cu, "ImportedClass", "ImportedClass");
	assertElementsEqual(
		"Unexpected elements",
		"ImportedClass [in ImportedClass.java [in a.b [in src [in Resolve]]]]",
		elements
	);
}
/**
 * Tests code resolve on a class file without attached source.
 */
public void testInClassFileWithoutSource() throws JavaModelException {
	IClassFile cu = getClassFile("Resolve", "p4.jar", "p4", "X.class");
	String selection = "Object";
	int start = 34;
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"",
		elements
	);
}
/**
 * Tests code resolve on a class file with attached source.
 */
public void testInClassFileWithSource() throws JavaModelException {
	IClassFile cf = getClassFile("Resolve", "p3.jar", "p3", "X.class");
	IJavaElement[] elements = codeSelect(cf, "Object", "Object");
	assertElementsEqual(
		"Unexpected elements",
		"Object [in Object.class [in java.lang [in " + getExternalJCLPath(). toString() + " [in Resolve]]]]",
		elements
	);
}
/**
 * bugs http://dev.eclipse.org/bugs/show_bug.cgi?id=25687
 */
public void testInnerClassAsParamater() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveInnerClassAsParamater.java");
	IJavaElement[] elements = codeSelect(cu, "foo(i)", "foo");
	assertElementsEqual(
		"Unexpected elements",
		"foo(Inner) [in ResolveInnerClassAsParamater [in ResolveInnerClassAsParamater.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolve the interface "Y"
 */
public void testInterface() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveInterface.java");
	IJavaElement[] elements = codeSelect(cu, "Y", "Y");
	assertElementsEqual(
		"Unexpected elements",
		"Y [in Y.java [in p1 [in src [in Resolve]]]]",
		elements
	);
}
/**
 * Attempt to resolve outside of the range of the compilation unit.
 */
public void testInvalidResolve() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "p1", "X.java");
	try {
		cu.codeSelect(-1, 10); 
	} catch (IllegalArgumentException e) {
		return;
	}
	assertTrue("Exception should have been thrown for out of bounds resolution", false);
}
/**
 * Resolve the local class 'Y' (field type).
 */
public void testLocalClass1() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalClass1.java");
	IJavaElement[] elements = codeSelect(cu, "Y[]", "Y");
	assertElementsEqual(
		"Unexpected elements",
		"Y [in foo() [in ResolveLocalClass1 [in ResolveLocalClass1.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/**
 * Resolve the local class 'Y' (local variable type).
 */
public void testLocalClass2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalClass2.java");
	IJavaElement[] elements = codeSelect(cu, "Y y", "Y");
	assertElementsEqual(
		"Unexpected elements",
		"Y [in foo() [in ResolveLocalClass2 [in ResolveLocalClass2.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/**
 * Resolve the local class 'Y'(array initializer type).
 */
public void testLocalClass3() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalClass3.java");
	IJavaElement[] elements = codeSelect(cu, "Y[]{", "Y");
	assertElementsEqual(
		"Unexpected elements",
		"Y [in foo() [in ResolveLocalClass3 [in ResolveLocalClass3.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/**
 * Resolve the local class 'Y' (return type).
 */
public void testLocalClass4() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalClass4.java");
	IJavaElement[] elements = codeSelect(cu, "Y bar()", "Y");
	assertElementsEqual(
		"Unexpected elements",
		"Y [in foo() [in ResolveLocalClass4 [in ResolveLocalClass4.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/**
 * Resolve the local class 'Y' (method argument).
 */
public void testLocalClass5() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalClass5.java");
	IJavaElement[] elements = codeSelect(cu, "Y y", "Y");
	assertElementsEqual(
		"Unexpected elements",
		"Y [in foo() [in ResolveLocalClass5 [in ResolveLocalClass5.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/**
 * Resolve the local class 'SuperClass' (super class).
 */
public void testLocalClass6() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalClass6.java");
	IJavaElement[] elements = codeSelect(cu, "Y { // superclass", "Y");
	assertElementsEqual(
		"Unexpected elements",
		"Y [in foo() [in ResolveLocalClass6 [in ResolveLocalClass6.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/**
 * Resolve a local declaration name
 */
public void testLocalName1() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalName.java");
	IJavaElement[] elements = codeSelect(cu, "var1", "var1");
	assertElementsEqual(
		"Unexpected elements",
		"Object [in Object.class [in java.lang [in " + getExternalJCLPath(). toString() + " [in Resolve]]]]",
		elements
	);
}
/**
 * Resolve a local declaration name with base type
 */
public void testLocalName2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalName.java");
	IJavaElement[] elements = codeSelect(cu, "var2", "var2");
	assertElementsEqual(
		"Unexpected elements",
		"",
		elements
	);
}
/**
 * Resolve member type declaration
 */
public void testMemberTypeDeclaration() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveMemberTypeDeclaration1.java");
	IJavaElement[] elements = codeSelect(cu, "MemberInterface", "MemberInterface");
	assertElementsEqual(
		"Unexpected elements",
		"MemberInterface [in ResolveMemberTypeDeclaration1 [in ResolveMemberTypeDeclaration1.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolve member type declaration located in default package
 */
public void testMemberTypeDeclaration2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveMemberTypeDeclaration2.java");
	IJavaElement[] elements = codeSelect(cu, "MemberOfMember", "MemberOfMember");
	assertElementsEqual(
		"Unexpected elements",
		"MemberOfMember [in Member [in ResolveMemberTypeDeclaration2 [in ResolveMemberTypeDeclaration2.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/**
 * Try to resolve message send on base type.
 */
public void testMessageSendOnBaseType() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveMessageSendOnBaseType.java");
	IJavaElement[] elements = codeSelect(cu, "hello", "hello");
	assertElementsEqual(
		"Unexpected elements",
		"",
		elements
	);
}
/**
 * Resolve the method "foo"
 */
public void testMethod() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveMethod.java");
	IJavaElement[] elements = codeSelect(cu, "foo(\"", "foo");
	assertElementsEqual(
		"Unexpected elements",
		"foo(String) [in ResolveMethod [in ResolveMethod.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolve method declaration
 */
public void testMethodDeclaration() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveMethodDeclaration.java");
	IJavaElement[] elements = codeSelect(cu, "foo(i", "foo");
	assertElementsEqual(
		"Unexpected elements",
		"foo(int) [in ResolveMethodDeclaration [in ResolveMethodDeclaration.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolve the method
 */
public void testMethodWithIncorrectParameter() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveMethodWithIncorrectParameter.java");
	IJavaElement[] elements = codeSelect(cu, "foo(\"String", "foo");
	assertElementsEqual(
		"Unexpected elements",
		"foo(int) [in ResolveMethodWithIncorrectParameter [in ResolveMethodWithIncorrectParameter.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolve method in inner type.
 */
public void testMethodWithInnerTypeInClassFile() throws JavaModelException {
	IClassFile cf = getClassFile("Resolve", "zzz.jar", "", "MyClass$Inner.class");
	IJavaElement[] elements = codeSelect(cf, "test", "test");
	assertElementsEqual(
		"Unexpected elements",
		"test() [in MyClass [in MyClass.class [in <default> [in zzz.jar [in Resolve]]]]]",
		elements
	);
}
/**
 * bug 33785
 */
public void testMethodWithInnerTypeInClassFile2() throws JavaModelException {
	IClassFile cf = getClassFile("Resolve", "zzz.jar", "", "MyClass2$Inner.class");
	IJavaElement[] elements = codeSelect(cf, "method", "method");
	assertElementsEqual(
		"Unexpected elements",
		"method(MyClass2.Inner[]) [in MyClass2 [in MyClass2.class [in <default> [in zzz.jar [in Resolve]]]]]",
		elements
	);
		
	IMethod method = (IMethod) elements[0];
	ISourceRange sourceRange = method.getSourceRange();
	String methodString = "void method(MyClass2.Inner[] arg){}";
	int o = cf.getSource().indexOf(methodString);
	int l = methodString.length();
	assertEquals("Unexpected offset", o, sourceRange.getOffset());
	assertEquals("Unexpected length", l, sourceRange.getLength());
}
/**
 * Tries to resolve the type "lang. \u0053tring" which doesn't exist.
 */
public void testNegativeResolveUnicode() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveUnicode.java");
	IJavaElement[] elements = codeSelect(cu, "lang.\\u0053tring", "lang.\\u0053tring");
	assertElementsEqual(
		"Unexpected elements",
		"",
		elements
	);
}
/**
 * Resolve the package "java.lang"
 */
public void testPackage() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolvePackage.java");
	IJavaElement[] elements = codeSelect(cu, "lang", "lang");
	assertElementsEqual(
		"Unexpected elements",
		"java.lang [in " + getExternalJCLPath().toString() + " [in Resolve]]",
		elements
	);
}
/**
 * Try to resolve the qualified type "lang.Object"
 */
public void testPartiallyQualifiedType() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolvePartiallyQualifiedType.java");
	IJavaElement[] elements = codeSelect(cu, "lang.Object", "lang.Object");
	assertElementsEqual(
		"Unexpected elements",
		"",
		elements
	);
}
/**
 * Resolve the qualified type "java.lang.Object"
 */
public void testQualifiedType() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveQualifiedType.java");
	IJavaElement[] elements = codeSelect(cu, "java.lang.Object", "java.lang.Object");
	assertElementsEqual(
		"Unexpected elements",
		"Object [in Object.class [in java.lang [in " + getExternalJCLPath(). toString() + " [in Resolve]]]]",
		elements
	);
}
/**
 * bugs http://dev.eclipse.org/bugs/show_bug.cgi?id=25888
 */
public void testStaticClassConstructor() throws JavaModelException {
	IClassFile cu = getClassFile("Resolve", "test25888.jar", "", "ResolveStaticClassConstructor.class");
	IJavaElement[] elements = codeSelect(cu, "StaticInnerClass();", "StaticInnerClass");
	assertElementsEqual(
		"Unexpected elements",
		"StaticInnerClass() [in StaticInnerClass [in ResolveStaticClassConstructor$StaticInnerClass.class [in <default> [in test25888.jar [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolve type declaration
 */
public void testTypeDeclaration() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveTypeDeclaration.java");
	IJavaElement[] elements = codeSelect(cu, "OtherType", "OtherType");
	assertElementsEqual(
		"Unexpected elements",
		"OtherType [in ResolveTypeDeclaration.java [in <default> [in src [in Resolve]]]]",
		elements
	);
}
/**
 * Resolve type in comment.
 */
public void testTypeInComment() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveTypeInComment.java");
	IJavaElement[] elements = codeSelect(cu, "X */", "X");
	assertElementsEqual(
		"Unexpected elements",
		"X [in X.java [in p2 [in src [in Resolve]]]]",
		elements
	);
}
/**
 * Resolve the type "java.lang. \u0053ring"
 */
public void testUnicode() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveUnicode.java");
	IJavaElement[] elements = codeSelect(cu, "java.lang.\\u0053tring", "java.lang.\\u0053tring");
	assertElementsEqual(
		"Unexpected elements",
		"String [in String.class [in java.lang [in " + getExternalJCLPath().toString() + " [in Resolve]]]]",
		elements
	);
}
}
