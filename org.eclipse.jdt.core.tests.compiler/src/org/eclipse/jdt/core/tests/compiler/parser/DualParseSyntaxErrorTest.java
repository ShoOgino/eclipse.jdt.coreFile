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
package org.eclipse.jdt.core.tests.compiler.parser;

import java.util.Locale;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

public class DualParseSyntaxErrorTest extends org.eclipse.jdt.core.tests.junit.extension.TestCase {
	public static boolean optimizeStringLiterals = false;
	public static long sourceLevel = ClassFileConstants.JDK1_3; //$NON-NLS-1$
	
	public DualParseSyntaxErrorTest(String testName){
	super(testName);
}
public void checkParse(
	char[] source, 
	String expectedSyntaxErrorDiagnosis,
	String testName) {

	Parser parser = 
		new Parser(
			new ProblemReporter(
				DefaultErrorHandlingPolicies.proceedWithAllProblems(), 
				new CompilerOptions(), 
				new DefaultProblemFactory(Locale.getDefault())),
			optimizeStringLiterals);

	ICompilationUnit sourceUnit = new CompilationUnit(source, testName, null);
	CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0, 0);	
	
	CompilationUnitDeclaration computedUnit = parser.dietParse(sourceUnit, compilationResult);
	if (computedUnit.types != null) {
		for (int i = computedUnit.types.length; --i >= 0;){
			computedUnit.types[i].binding = new SourceTypeBinding(){};
			computedUnit.types[i].parseMethod(parser, computedUnit);
		}
	}

	StringBuffer buffer = new StringBuffer(100);
	if (compilationResult.hasProblems() || compilationResult.hasTasks()) {
		IProblem[] problems = compilationResult.getAllProblems();
		int count = problems.length;
		int problemCount = 0;
		for (int i = 0; i < count; i++) { 
			if (problems[i] != null) {
				if (problemCount == 0)
					buffer.append("----------\n");
				problemCount++;
				buffer.append(problemCount + (problems[i].isError() ? ". ERROR" : ". WARNING"));
				buffer.append(" in " + new String(problems[i].getOriginatingFileName()));
				try {
					buffer.append(((DefaultProblem)problems[i]).errorReportSource(compilationResult.compilationUnit));
					buffer.append("\n");
					buffer.append(problems[i].getMessage());
					buffer.append("\n");
				} catch (Exception e) {
				}
				buffer.append("----------\n");
			}
		}
	}
	String computedSyntaxErrorDiagnosis = buffer.toString();
	if(!expectedSyntaxErrorDiagnosis.equals(computedSyntaxErrorDiagnosis)) {
 		System.out.println(Util.displayString(computedSyntaxErrorDiagnosis));
	}
	assertEquals(
		"Invalid syntax error diagnosis" + testName,
		expectedSyntaxErrorDiagnosis,
		computedSyntaxErrorDiagnosis);
}

public void test01() {

	String s = 
		"public class X {							\n"+
		"	int fX;         						\n"+
		"	void foo() {   							\n"+
		"		fX = 0;  							\n"+
		"	}			  							\n"+
		"	public void bar() {						\n"+
		"	}										\n"+
		"}											\n"; 	

	String expectedSyntaxErrorDiagnosis =
		"";

	String testName = "<test1>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
public void test02() {

	String s = 
		"public class X {							\n"+
		"	int fX;         						\n"+
		"	void foo() {   							\n"+
		"		fX = 0;  							\n"+
		"	public void bar() {						\n"+
		"	}										\n"+
		"}											\n"; 	

	String expectedSyntaxErrorDiagnosis =
		"----------\n" + 
		"1. ERROR in <test2> (at line 3)\n" + 
		"	void foo() {   							\n" + 
		"	           ^\n" + 
		"Syntax error, insert \"}\" to complete MethodBody\n" + 
		"----------\n";

	String testName = "<test2>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
public void test03() {

	String s = 
		"public class X {							\n"+
		"	int fX;         						\n"+
		"	void foo()   							\n"+
		"		fX = 0;  							\n"+
		"	public void bar() {						\n"+
		"	}										\n"+
		"}											\n"; 	

	String expectedSyntaxErrorDiagnosis =
		"----------\n" + 
		"1. ERROR in <test3> (at line 3)\n" + 
		"	void foo()   							\n" + 
		"	         ^\n" + 
		"Syntax error, insert \"}\" to complete MethodBody\n" + 
		"----------\n" + 
		"2. ERROR in <test3> (at line 3)\n" + 
		"	void foo()   							\n" + 
		"	         ^\n" + 
		"Syntax error on token \")\", { expected after this token\n" + 
		"----------\n";

	String testName = "<test3>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
public void test04() {

	String s = 
		"public class X {							\n"+
		"	int fX;         						\n"+
		"	void foo()   							\n"+
		"		fX = 0;  							\n"+
		"	} 			  							\n"+
		"	public void bar() {						\n"+
		"	}										\n"+
		"}											\n"; 	

	String expectedSyntaxErrorDiagnosis =
		"----------\n" + 
		"1. ERROR in <test4> (at line 3)\n" + 
		"	void foo()   							\n" + 
		"	         ^\n" + 
		"Syntax error on token \")\", { expected after this token\n" + 
		"----------\n";

	String testName = "<test4>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
public void test05() {

	String s = 
		"public class X {							\n"+
		"	int fX;         						\n"+
		"	void foo() {  							\n"+
		"		if(true){  							\n"+
		"	} 			  							\n"+
		"	public void bar() {						\n"+
		"	}										\n"+
		"}											\n"; 	

	String expectedSyntaxErrorDiagnosis =
		"----------\n" + 
		"1. ERROR in <test5> (at line 4)\n" + 
		"	if(true){  							\n" + 
		"	        ^\n" + 
		"Syntax error, insert \"}\" to complete Statement\n" + 
		"----------\n";

	String testName = "<test5>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
public void test06() {

	String s = 
		"public class X {							\n"+
		"	int fX;         						\n"+
		"	void foo() {  							\n"+
		"		if(true){  							\n"+
		"	} 			  							\n"+
		"	//comment								\n"+
		"	public void bar() {						\n"+
		"	}										\n"+
		"}											\n"; 	

	String expectedSyntaxErrorDiagnosis =
		"----------\n" + 
		"1. ERROR in <test6> (at line 4)\n" + 
		"	if(true){  							\n" + 
		"	        ^\n" + 
		"Syntax error, insert \"}\" to complete Statement\n" + 
		"----------\n";

	String testName = "<test6>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
public void test07() {

	String s = 
		"public class X {							\n"+
		"	int fX;         						\n"+
		"	void foo() {  							\n"+
		"		if(true){  							\n"+
		"	} 			  							\n"+
		"	System.out.println();					\n"+
		"	public void bar() {						\n"+
		"	}										\n"+
		"}											\n"; 	

	String expectedSyntaxErrorDiagnosis =
		"----------\n" + 
		"1. ERROR in <test7> (at line 3)\n" + 
		"	void foo() {  							\n" + 
		"	           ^\n" + 
		"Syntax error, insert \"}\" to complete MethodBody\n" + 
		"----------\n";

	String testName = "<test7>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
public void test08() {

	String s = 
		"public class X {							\n"+
		"	int fX;         						\n"+
		"	void foo() {  							\n"+
		"		if(true){  							\n"+
		"	} 			  							\n"+
		"	public int bar;							\n"+
		"}											\n";  	

	String expectedSyntaxErrorDiagnosis =
		"----------\n" + 
		"1. ERROR in <test8> (at line 4)\n" + 
		"	if(true){  							\n" + 
		"	        ^\n" + 
		"Syntax error, insert \"}\" to complete Statement\n" + 
		"----------\n";

	String testName = "<test8>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
public void test09() {

	String s = 
		"public class X {							\n"+
		"	int fX;         						\n"+
		"	void foo() {  							\n"+
		"		if(true){  							\n"+
		"	} 			  							\n"+
		"	//comment	  							\n"+
		"	public int bar;							\n"+
		"}											\n";  	

	String expectedSyntaxErrorDiagnosis =
		"----------\n" + 
		"1. ERROR in <test9> (at line 4)\n" + 
		"	if(true){  							\n" + 
		"	        ^\n" + 
		"Syntax error, insert \"}\" to complete Statement\n" + 
		"----------\n";

	String testName = "<test9>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
public void test10() {

	String s = 
		"public class X {							\n"+
		"	int fX;         						\n"+
		"	void foo() {  							\n"+
		"		if(true){  							\n"+
		"	} 			  							\n"+
		"	System.out.println();					\n"+
		"	public int bar;							\n"+
		"}											\n";  	

	String expectedSyntaxErrorDiagnosis =
		"----------\n" + 
		"1. ERROR in <test10> (at line 3)\n" + 
		"	void foo() {  							\n" + 
		"	           ^\n" + 
		"Syntax error, insert \"}\" to complete MethodBody\n" + 
		"----------\n";

	String testName = "<test10>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
}
