/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.Map;
import java.util.StringTokenizer;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.search.SearchEngine;

/**
 * Tests the Java search engine in Javadoc comment.
 *
 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=45518">bug 45518</a>
 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=46761">bug 46761</a>
 */
public class JavaSearchJavadocTests extends JavaSearchTests {

	Map originalOptions;
	final String docCommentSupport;
	static final String DOC_COMMENT_SUPPORT = System.getProperty("doc.support");

	/**
	 * @param name
	 */
	public JavaSearchJavadocTests(String name, String support) {
		super(name);
		this.docCommentSupport = support;
	}
	public JavaSearchJavadocTests(String name) {
		this(name, JavaCore.ENABLED);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.tests.model.SuiteOfTestCases#setUpSuite()
	 */
	public void setUp() throws Exception {
		super.setUp();
		this.originalOptions = this.javaProject.getOptions(true);
		this.javaProject.setOption(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, this.docCommentSupport);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.tests.model.SuiteOfTestCases#tearDownSuite()
	 */
	public void tearDown() throws Exception {
		super.tearDown();
		this.javaProject.setOptions(originalOptions);
	}
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#getName()
	 */
	public String getName() {
		return "Doc "+this.docCommentSupport+" - "+super.getName();
	}
	private void setJavadocOptions() {
		this.javaProject.setOption(JavaCore.COMPILER_PB_INVALID_JAVADOC, JavaCore.WARNING);
		this.javaProject.setOption(JavaCore.COMPILER_PB_MISSING_JAVADOC_COMMENTS, JavaCore.ERROR);
	}
	public static Test suite() {
		// NOTE: cannot use 'new Suite(JavaSearchJavadocTests.class)' as this would include tests from super class
		TestSuite suite = new Suite(JavaSearchJavadocTests.class.getName());

		if (DOC_COMMENT_SUPPORT == null) {
			// Default is to test both Doc Comment Support ON and OFF
			buildSuite(suite, JavaCore.ENABLED);
			buildSuite(suite, JavaCore.DISABLED);
		} else {
			// Test specified with Doc Comment Support
			String support = DOC_COMMENT_SUPPORT==null ? JavaCore.DISABLED : (DOC_COMMENT_SUPPORT.equals(JavaCore.DISABLED)?JavaCore.DISABLED:JavaCore.ENABLED);
			buildSuite(suite, support);
		}
		
		return suite;
	}

	public static void buildSuite(TestSuite suite, String support) {
//		TestSuite suite = new Suite("Doc "+support);
		// Tests on type declarations
		suite.addTest(new JavaSearchJavadocTests("testJavadocTypeDeclaration", support));
		suite.addTest(new JavaSearchJavadocTests("testJavadocTypeDeclarationWithJavadoc", support));
		suite.addTest(new JavaSearchJavadocTests("testJavadocTypeStringDeclaration", support));
		
		// Tests on field declarations
		suite.addTest(new JavaSearchJavadocTests("testJavadocFieldDeclaration", support));
		suite.addTest(new JavaSearchJavadocTests("testJavadocFieldDeclarationWithJavadoc", support));
		suite.addTest(new JavaSearchJavadocTests("testJavadocFieldStringDeclaration", support));

		// Tests on method declarations
		suite.addTest(new JavaSearchJavadocTests("testJavadocMethodDeclaration", support));
		suite.addTest(new JavaSearchJavadocTests("testJavadocMethodArgDeclaration", support));
		suite.addTest(new JavaSearchJavadocTests("testJavadocMethodDeclarationWithJavadoc", support));
		suite.addTest(new JavaSearchJavadocTests("testJavadocMethodArgDeclarationWithJavadoc", support));
		suite.addTest(new JavaSearchJavadocTests("testJavadocMethodStringDeclaration", support));

		// Tests on type references
		suite.addTest(new JavaSearchJavadocTests("testJavadocTypeReference", support));
		suite.addTest(new JavaSearchJavadocTests("testJavadocTypeReferenceWithJavadoc", support));
		suite.addTest(new JavaSearchJavadocTests("testJavadocTypeStringReference", support));
		suite.addTest(new JavaSearchJavadocTests("testJavadocTypeStringReferenceWithJavadoc", support));

		// Tests on field references
		suite.addTest(new JavaSearchJavadocTests("testJavadocFieldReference", support));
		suite.addTest(new JavaSearchJavadocTests("testJavadocFieldReferenceWithJavadoc", support));
		suite.addTest(new JavaSearchJavadocTests("testJavadocFieldStringReference", support));
		suite.addTest(new JavaSearchJavadocTests("testJavadocFieldStringReferenceWithJavadoc", support));

		// Tests on method references
		suite.addTest(new JavaSearchJavadocTests("testJavadocMethodReference", support));
		suite.addTest(new JavaSearchJavadocTests("testJavadocMethodArgReference", support));
		suite.addTest(new JavaSearchJavadocTests("testJavadocMethodReferenceWithJavadoc", support));
		suite.addTest(new JavaSearchJavadocTests("testJavadocMethodArgReferenceWithJavadoc", support));
		suite.addTest(new JavaSearchJavadocTests("testJavadocMethodStringReference", support));
		suite.addTest(new JavaSearchJavadocTests("testJavadocMethodStringReferenceWithJavadoc", support));

		// Tests on constructor references
		suite.addTest(new JavaSearchJavadocTests("testJavadocConstructorReference", support));
		suite.addTest(new JavaSearchJavadocTests("testJavadocConstructorArgReference", support));
		suite.addTest(new JavaSearchJavadocTests("testJavadocConstructorReferenceWithJavadoc", support));
		suite.addTest(new JavaSearchJavadocTests("testJavadocConstructorArgReferenceWithJavadoc", support));
		suite.addTest(new JavaSearchJavadocTests("testJavadocConstructorStringReference", support));
		suite.addTest(new JavaSearchJavadocTests("testJavadocConstructorStringReferenceWithJavadoc", support));

		// Tests on bugs
		suite.addTest(new JavaSearchJavadocTests("testBug47909", support));
		suite.addTest(new JavaSearchJavadocTests("testBug47968type", support));
		suite.addTest(new JavaSearchJavadocTests("testBug47968field", support));
		suite.addTest(new JavaSearchJavadocTests("testBug47968method", support));
		suite.addTest(new JavaSearchJavadocTests("testBug47968constructor", support));
		suite.addTest(new JavaSearchJavadocTests("testBug47209type", support));
		suite.addTest(new JavaSearchJavadocTests("testBug47209field", support));
		suite.addTest(new JavaSearchJavadocTests("testBug47209method", support));
		suite.addTest(new JavaSearchJavadocTests("testBug47209constructor", support));
		suite.addTest(new JavaSearchJavadocTests("testBug49994", support));
		suite.addTest(new JavaSearchJavadocTests("testBug49994field", support));
		suite.addTest(new JavaSearchJavadocTests("testBug49994method", support));
		suite.addTest(new JavaSearchJavadocTests("testBug49994constructor", support));
		
//		return suite;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.tests.model.AbstractJavaModelTests#assertSearchResults(java.lang.String, java.lang.Object)
	 */
	protected void assertSearchResults(String message, String expected, Object collector) {
		if (JavaCore.ENABLED.equals(this.docCommentSupport)) {
			super.assertSearchResults(message, expected, collector);
		} else {
			StringTokenizer tokenizer = new StringTokenizer(expected, "\n");
			StringBuffer buffer = new StringBuffer(expected.length());
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				if (token.startsWith("src/j1/JavadocSearched.java") || token.startsWith("test47909.jar")) {
					if (buffer.length() > 0) buffer.append('\n');
					buffer.append(token);
				}
			}
//			System.out.println(getName()+" - Expected: "+buffer.toString());
			super.assertSearchResults(message, buffer.toString(), collector);
		}
	}
	/*
	 * Test search of type declaration in javadoc comments
	 * ===================================================
	 */
	public void testJavadocTypeDeclaration() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy = true;
		new SearchEngine().search(
				getWorkspace(), 
				type,
				DECLARATIONS, 
				getJavaSearchScope(), 
				result
				);
		assertSearchResults(
				"src/j1/JavadocSearched.java j1.JavadocSearched [JavadocSearched] EXACT_MATCH",
				result);
	}
	public void testJavadocTypeStringDeclaration() throws CoreException {
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy = true;
		new SearchEngine().search(
				getWorkspace(), 
				"JavadocSearched",
				TYPE,
				DECLARATIONS, 
				getJavaSearchScope(), 
				result
				);
		assertSearchResults(
				"src/j1/JavadocSearched.java j1.JavadocSearched [JavadocSearched] EXACT_MATCH",
				result);
	}
	public void testJavadocTypeDeclarationWithJavadoc() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
//		try {
			setJavadocOptions();
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			new SearchEngine().search(
					getWorkspace(), 
					type,
					DECLARATIONS, 
					getJavaSearchScope(), 
					result
					);
			assertSearchResults(
					"src/j1/JavadocSearched.java j1.JavadocSearched [JavadocSearched] EXACT_MATCH",
					result);
//		} finally {
//			resetProjectOptions();
//		}
	}

	/*
	 * Test search of field declaration in javadoc comments
	 * ====================================================
	 */
	public void testJavadocFieldDeclaration() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		IField field = type.getField("javadocSearchedVar");
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy = true;
		new SearchEngine().search(
				getWorkspace(), 
				field,
				DECLARATIONS, 
				getJavaSearchScope(), 
				result
				);
		assertSearchResults(
				"src/j1/JavadocSearched.java j1.JavadocSearched.javadocSearchedVar [javadocSearchedVar] EXACT_MATCH",
				result);
	}
	public void testJavadocFieldStringDeclaration() throws CoreException {
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy = true;
		new SearchEngine().search(
				getWorkspace(), 
				"javadocSearchedVar",
				FIELD,
				DECLARATIONS, 
				getJavaSearchScope(), 
				result
				);
		assertSearchResults(
				"src/j1/JavadocSearched.java j1.JavadocSearched.javadocSearchedVar [javadocSearchedVar] EXACT_MATCH",
				result);
	}
	public void testJavadocFieldDeclarationWithJavadoc() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		IField field = type.getField("javadocSearchedVar");
//		try {
			setJavadocOptions();
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			new SearchEngine().search(
					getWorkspace(), 
					field,
					DECLARATIONS, 
					getJavaSearchScope(), 
					result
					);
			assertSearchResults(
					"src/j1/JavadocSearched.java j1.JavadocSearched.javadocSearchedVar [javadocSearchedVar] EXACT_MATCH",
					result);
//		} finally {
//			resetProjectOptions();
//		}
	}

	/*
	 * Test search of method declarations in javadoc comments
	 * ======================================================
	 */
	public void testJavadocMethodDeclaration() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		IMethod method = type.getMethod("javadocSearchedMethod", null);
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy = true;
		new SearchEngine().search(
				getWorkspace(), 
				method,
				DECLARATIONS, 
				getJavaSearchScope(), 
				result
				);
		assertSearchResults(
				"src/j1/JavadocSearched.java void j1.JavadocSearched.javadocSearchedMethod() [javadocSearchedMethod] EXACT_MATCH",
				result);
	}
	public void testJavadocMethodArgDeclaration() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		IMethod method = type.getMethod("javadocSearchedMethod", new String[] { "QString;" });
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy = true;
		new SearchEngine().search(
				getWorkspace(), 
				method,
				DECLARATIONS, 
				getJavaSearchScope(), 
				result
				);
		assertSearchResults(
				"src/j1/JavadocSearched.java void j1.JavadocSearched.javadocSearchedMethod(String) [javadocSearchedMethod] EXACT_MATCH",
				result);
	}
	public void testJavadocMethodStringDeclaration() throws CoreException {
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy = true;
		new SearchEngine().search(
				getWorkspace(), 
				"javadocSearchedMethod",
				METHOD,
				DECLARATIONS, 
				getJavaSearchScope(), 
				result
				);
		assertSearchResults(
				"src/j1/JavadocSearched.java void j1.JavadocSearched.javadocSearchedMethod() [javadocSearchedMethod] EXACT_MATCH\n" + 
				"src/j1/JavadocSearched.java void j1.JavadocSearched.javadocSearchedMethod(String) [javadocSearchedMethod] EXACT_MATCH",
				result);
	}
	public void testJavadocMethodDeclarationWithJavadoc() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
//		try {
			setJavadocOptions();
			IMethod method = type.getMethod("javadocSearchedMethod", null);
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			new SearchEngine().search(
					getWorkspace(), 
					method,
					DECLARATIONS, 
					getJavaSearchScope(), 
					result
					);
			assertSearchResults(
					"src/j1/JavadocSearched.java void j1.JavadocSearched.javadocSearchedMethod() [javadocSearchedMethod] EXACT_MATCH",
					result);
//		} finally {
//			resetProjectOptions();
//		}
	}
	public void testJavadocMethodArgDeclarationWithJavadoc() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
//		try {
			setJavadocOptions();
			IMethod method = type.getMethod("javadocSearchedMethod", new String[] { "QString;" });
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			new SearchEngine().search(
					getWorkspace(), 
					method,
					DECLARATIONS, 
					getJavaSearchScope(), 
					result
					);
			assertSearchResults(
					"src/j1/JavadocSearched.java void j1.JavadocSearched.javadocSearchedMethod(String) [javadocSearchedMethod] EXACT_MATCH",
					result);
//		} finally {
//			resetProjectOptions();
//		}
	}

	/*
	 * Test search of type references in javadoc comments
	 * ==================================================
	 */
	public void testJavadocTypeReference() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy = true;
		new SearchEngine().search(
				getWorkspace(), 
				type,
				REFERENCES, 
				getJavaSearchScope(), 
				result
				);
		assertSearchResults(
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [j1.JavadocSearched] EXACT_MATCH\n" + 
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH\n" + 
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH\n" + 
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH\n" + 
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH\n" + 
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH\n"+
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH\n"+
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [j1.JavadocSearched] EXACT_MATCH\n"+
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH\n"+
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH\n"+
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH",
			result);
	}
	public void testJavadocTypeStringReference() throws CoreException {
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy = true;
		new SearchEngine().search(
				getWorkspace(), 
				"JavadocSearched",
				TYPE,
				REFERENCES, 
				getJavaSearchScope(), 
				result
				);
		assertSearchResults(
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH\n" + 
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH\n" + 
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH\n" + 
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH\n" + 
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH\n" + 
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH\n"+
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH\n"+
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH\n"+
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH\n"+
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH\n"+
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH",
			result);
	}
	public void testJavadocTypeReferenceWithJavadoc() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
//		try {
			setJavadocOptions();
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			new SearchEngine().search(
					getWorkspace(), 
					type,
					REFERENCES, 
					getJavaSearchScope(), 
					result
					);
			assertSearchResults(
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [j1.JavadocSearched] EXACT_MATCH\n" + 
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH\n" + 
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH\n" + 
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH\n" + 
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH\n" + 
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH\n"+
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH\n"+
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [j1.JavadocSearched] EXACT_MATCH\n"+
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH\n"+
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH\n"+
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH",
				result);
//		} finally {
//			resetProjectOptions();
//		}
	}
	public void testJavadocTypeStringReferenceWithJavadoc() throws CoreException {
//		try {
			setJavadocOptions();
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			new SearchEngine().search(
					getWorkspace(), 
					"JavadocSearched",
					TYPE,
					REFERENCES, 
					getJavaSearchScope(), 
					result
					);
			assertSearchResults(
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH\n" + 
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH\n" + 
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH\n" + 
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH\n" + 
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH\n" + 
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH\n"+
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH\n"+
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH\n"+
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH\n"+
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH\n"+
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH",
				result);
//		} finally {
//			resetProjectOptions();
//		}
	}

	/*
	 * Test search of field references in javadoc comments
	 * ===================================================
	 */
	public void testJavadocFieldReference() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		IField field = type.getField("javadocSearchedVar");
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy = true;
		new SearchEngine().search(
				getWorkspace(), 
				field,
				REFERENCES, 
				getJavaSearchScope(), 
				result
				);
		assertSearchResults(
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [javadocSearchedVar] POTENTIAL_MATCH\n" + 
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [javadocSearchedVar] EXACT_MATCH",
				result);
	}
	public void testJavadocFieldStringReference() throws CoreException {
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy = true;
		new SearchEngine().search(
				getWorkspace(), 
				"javadocSearchedVar",
				FIELD,
				REFERENCES, 
				getJavaSearchScope(), 
				result
				);
		assertSearchResults(
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [javadocSearchedVar] EXACT_MATCH\n" + 
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [javadocSearchedVar] POTENTIAL_MATCH\n" + 
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [javadocSearchedVar] EXACT_MATCH",
				result);
	}
	public void testJavadocFieldReferenceWithJavadoc() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		IField field = type.getField("javadocSearchedVar");
//		try {
			setJavadocOptions();
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			new SearchEngine().search(
					getWorkspace(), 
					field,
					REFERENCES, 
					getJavaSearchScope(), 
					result
					);
			assertSearchResults(
					"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [javadocSearchedVar] POTENTIAL_MATCH\n" + 
					"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [javadocSearchedVar] EXACT_MATCH",
					result);
//		} finally {
//			resetProjectOptions();
//		}
	}
	public void testJavadocFieldStringReferenceWithJavadoc() throws CoreException {
//		try {
			setJavadocOptions();
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			new SearchEngine().search(
					getWorkspace(), 
					"javadocSearchedVar",
					FIELD,
					REFERENCES, 
					getJavaSearchScope(), 
					result
					);
			assertSearchResults(
					"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [javadocSearchedVar] EXACT_MATCH\n" + 
					"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [javadocSearchedVar] POTENTIAL_MATCH\n" + 
					"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [javadocSearchedVar] EXACT_MATCH",
					result);
//		} finally {
//			resetProjectOptions();
//		}
	}

	/*
	 * Test search of method references in javadoc comments
	 * ====================================================
	 */
	public void testJavadocMethodReference() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		IMethod method = type.getMethod("javadocSearchedMethod", null);
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy = true;
		new SearchEngine().search(
				getWorkspace(), 
				method,
				REFERENCES, 
				getJavaSearchScope(), 
				result
				);
		assertSearchResults(
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [javadocSearchedMethod] POTENTIAL_MATCH\n" + 
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [javadocSearchedMethod] EXACT_MATCH",
				result);
	}
	public void testJavadocMethodArgReference() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		IMethod method = type.getMethod("javadocSearchedMethod", new String[] { "QString;" });
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy = true;
		new SearchEngine().search(
				getWorkspace(), 
				method,
				REFERENCES, 
				getJavaSearchScope(), 
				result
				);
		assertSearchResults(
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [javadocSearchedMethod] EXACT_MATCH",
				result);
	}
	public void testJavadocMethodStringReference() throws CoreException {
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy = true;
		new SearchEngine().search(
				getWorkspace(), 
				"javadocSearchedMethod",
				METHOD,
				REFERENCES, 
				getJavaSearchScope(), 
				result
				);
		assertSearchResults(
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [javadocSearchedMethod] EXACT_MATCH\n" + 
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [javadocSearchedMethod] EXACT_MATCH\n" + 
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [javadocSearchedMethod] EXACT_MATCH\n" + 
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [javadocSearchedMethod] EXACT_MATCH",
				result);
	}
	public void testJavadocMethodReferenceWithJavadoc() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
//		try {
			setJavadocOptions();
			IMethod method = type.getMethod("javadocSearchedMethod", null);
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			new SearchEngine().search(
					getWorkspace(), 
					method,
					REFERENCES, 
					getJavaSearchScope(), 
					result
					);
			assertSearchResults(
					"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [javadocSearchedMethod] POTENTIAL_MATCH\n" + 
					"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [javadocSearchedMethod] EXACT_MATCH",
					result);
//		} finally {
//			resetProjectOptions();
//		}
	}
	public void testJavadocMethodArgReferenceWithJavadoc() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
//		try {
			setJavadocOptions();
			IMethod method = type.getMethod("javadocSearchedMethod", new String[] { "QString;" });
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			new SearchEngine().search(
					getWorkspace(), 
					method,
					REFERENCES, 
					getJavaSearchScope(), 
					result
					);
			assertSearchResults(
					"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [javadocSearchedMethod] EXACT_MATCH",
					result);
//		} finally {
//			resetProjectOptions();
//		}
	}
	public void testJavadocMethodStringReferenceWithJavadoc() throws CoreException {
//		try {
			setJavadocOptions();
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			new SearchEngine().search(
					getWorkspace(), 
					"javadocSearchedMethod",
					METHOD,
					REFERENCES, 
					getJavaSearchScope(), 
					result
					);
			assertSearchResults(
					"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [javadocSearchedMethod] EXACT_MATCH\n" + 
					"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [javadocSearchedMethod] EXACT_MATCH\n" + 
					"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [javadocSearchedMethod] EXACT_MATCH\n" + 
					"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [javadocSearchedMethod] EXACT_MATCH",
					result);
//		} finally {
//			resetProjectOptions();
//		}
	}

	/*
	 * Test search of constrcutor references in javadoc comments
	 * ====================================================
	 */
	public void testJavadocConstructorReference() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		IMethod method = type.getMethod("JavadocSearched", null);
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy = true;
		new SearchEngine().search(
				getWorkspace(), 
				method,
				REFERENCES, 
				getJavaSearchScope(), 
				result
				);
		assertSearchResults(
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH",
				result);
	}
	public void testJavadocConstructorArgReference() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		IMethod method = type.getMethod("JavadocSearched", new String[] { "QString;" });
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy = true;
		new SearchEngine().search(
				getWorkspace(), 
				method,
				REFERENCES, 
				getJavaSearchScope(), 
				result
				);
		assertSearchResults(
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH",
				result);
	}
	public void testJavadocConstructorStringReference() throws CoreException {
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy = true;
		new SearchEngine().search(
				getWorkspace(), 
				"JavadocSearched",
				CONSTRUCTOR,
				REFERENCES, 
				getJavaSearchScope(), 
				result
				);
		assertSearchResults(
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH\n" + 
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH\n" + 
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH",
				result);
	}
	public void testJavadocConstructorReferenceWithJavadoc() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
//		try {
			setJavadocOptions();
			IMethod method = type.getMethod("JavadocSearched", null);
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			new SearchEngine().search(
					getWorkspace(), 
					method,
					REFERENCES, 
					getJavaSearchScope(), 
					result
					);
			assertSearchResults(
					"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH",
					result);
//		} finally {
//			resetProjectOptions();
//		}
	}
	public void testJavadocConstructorArgReferenceWithJavadoc() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
//		try {
			setJavadocOptions();
			IMethod method = type.getMethod("JavadocSearched", new String[] { "QString;" });
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			new SearchEngine().search(
					getWorkspace(), 
					method,
					REFERENCES, 
					getJavaSearchScope(), 
					result
					);
			assertSearchResults(
					"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH",
					result);
//		} finally {
//			resetProjectOptions();
//		}
	}
	public void testJavadocConstructorStringReferenceWithJavadoc() throws CoreException {
//		try {
			setJavadocOptions();
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			new SearchEngine().search(
				getWorkspace(), 
				"JavadocSearched",
				CONSTRUCTOR,
				REFERENCES, 
				getJavaSearchScope(), 
				result
			);
			assertSearchResults(
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH\n" + 
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH\n" + 
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH",
				result);
//		} finally {
//			resetProjectOptions();
//		}
	}

	/**
	 * Test fix for bug 47909.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=47909">47909</a>
	 * @throws CoreException
	 */
	public void testBug47909() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j3", "Y.java").getType("Y");
//		try {
			setJavadocOptions();
			IMethod method = type.getMethod("Y", new String[] { "I" });
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			new SearchEngine().search(
				getWorkspace(), 
				method,
				REFERENCES, 
				getJavaSearchScope(), 
				result
			);
			assertSearchResults(
				"test47909.jar void j3.X.bar() EXACT_MATCH",
				result);
//		} finally {
//			resetProjectOptions();
//		}
	}
	
	/**
	 * Test fix for bug 47968.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=47968">47968</a>
	 * @throws CoreException
	 */
	public void testBug47968type() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j2", "Bug47968.java").getType("Bug47968");
//		try {
			setJavadocOptions();
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			new SearchEngine().search(
				getWorkspace(), 
				type,
				REFERENCES, 
				getJavaSearchScope(), 
				result
			);
			assertSearchResults(
				// These matches were not found before...
				"src/j2/Bug47968s.java j2.Bug47968s [Bug47968] EXACT_MATCH\n" + 
				"src/j2/Bug47968s.java j2.Bug47968s [Bug47968] EXACT_MATCH\n" + 
				"src/j2/Bug47968s.java j2.Bug47968s [Bug47968] EXACT_MATCH\n" + 
				"src/j2/Bug47968s.java j2.Bug47968s [Bug47968] EXACT_MATCH\n" + 
				// ...end
				"src/j2/Bug47968s.java j2.Bug47968s.y [Bug47968] EXACT_MATCH\n" + 
				"src/j2/Bug47968s.java j2.Bug47968s.y [Bug47968] EXACT_MATCH\n" + 
				"src/j2/Bug47968s.java j2.Bug47968s.y [Bug47968] EXACT_MATCH\n" + 
				"src/j2/Bug47968s.java j2.Bug47968s.y [Bug47968] EXACT_MATCH\n" + 
				"src/j2/Bug47968s.java j2.Bug47968s() [Bug47968] EXACT_MATCH\n" + 
				"src/j2/Bug47968s.java j2.Bug47968s() [Bug47968] EXACT_MATCH\n" + 
				"src/j2/Bug47968s.java j2.Bug47968s() [Bug47968] EXACT_MATCH\n" + 
				"src/j2/Bug47968s.java j2.Bug47968s() [Bug47968] EXACT_MATCH\n" + 
				"src/j2/Bug47968s.java void j2.Bug47968s.bar() [Bug47968] EXACT_MATCH\n" + 
				"src/j2/Bug47968s.java void j2.Bug47968s.bar() [Bug47968] EXACT_MATCH\n" + 
				"src/j2/Bug47968s.java void j2.Bug47968s.bar() [Bug47968] EXACT_MATCH\n" + 
				"src/j2/Bug47968s.java void j2.Bug47968s.bar() [Bug47968] EXACT_MATCH",
				result);
//		} finally {
//			resetProjectOptions();
//		}
	}
	public void testBug47968field() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j2", "Bug47968.java").getType("Bug47968");
//		try {
			setJavadocOptions();
			IField field = type.getField("x");
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			new SearchEngine().search(
				getWorkspace(), 
				field,
				REFERENCES, 
				getJavaSearchScope(), 
				result
			);
			assertSearchResults(
				"src/j2/Bug47968s.java j2.Bug47968s [x] EXACT_MATCH\n" + // This match was not found before...
				"src/j2/Bug47968s.java j2.Bug47968s.y [x] EXACT_MATCH\n" + 
				"src/j2/Bug47968s.java j2.Bug47968s() [x] EXACT_MATCH\n" + 
				"src/j2/Bug47968s.java void j2.Bug47968s.bar() [x] EXACT_MATCH",
				result);
//		} finally {
//			resetProjectOptions();
//		}
	}
	public void testBug47968method() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j2", "Bug47968.java").getType("Bug47968");
//		try {
			setJavadocOptions();
			IMethod method = type.getMethod("foo", new String[] { "I" });
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			new SearchEngine().search(
				getWorkspace(), 
				method,
				REFERENCES, 
				getJavaSearchScope(), 
				result
			);
			assertSearchResults(
				"src/j2/Bug47968s.java j2.Bug47968s [foo] EXACT_MATCH\n" + // This match was not found before...
				"src/j2/Bug47968s.java j2.Bug47968s.y [foo] EXACT_MATCH\n" + 
				"src/j2/Bug47968s.java j2.Bug47968s() [foo] EXACT_MATCH\n" + 
				"src/j2/Bug47968s.java void j2.Bug47968s.bar() [foo] EXACT_MATCH",
				result);
//		} finally {
//			resetProjectOptions();
//		}
	}
	public void testBug47968constructor() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j2", "Bug47968.java").getType("Bug47968");
//		try {
			setJavadocOptions();
			IMethod method = type.getMethod("Bug47968", new String[] { "QString;" });
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			new SearchEngine().search(
				getWorkspace(), 
				method,
				REFERENCES, 
				getJavaSearchScope(), 
				result
			);
			assertSearchResults(
				"src/j2/Bug47968s.java j2.Bug47968s [Bug47968] EXACT_MATCH\n" + // This match was not found before...
				"src/j2/Bug47968s.java j2.Bug47968s.y [Bug47968] EXACT_MATCH\n" + 
				"src/j2/Bug47968s.java j2.Bug47968s() [Bug47968] EXACT_MATCH\n" + 
				"src/j2/Bug47968s.java void j2.Bug47968s.bar() [Bug47968] EXACT_MATCH",
				result);
//		} finally {
//			resetProjectOptions();
//		}
	}

	/**
	 * Test fix for bug 47209.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=47209">47209</a>
	 * @throws CoreException
	 */
	public void testBug47209type() throws CoreException {
//		try {
			setJavadocOptions();
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			IType type = getCompilationUnit("JavaSearch", "src", "j4", "TT47209.java").getType("TT47209");
			new SearchEngine().search(getWorkspace(),  type, REFERENCES,  getJavaSearchScope(), result);
			type = getCompilationUnit("JavaSearch", "src", "j4", "TF47209.java").getType("TF47209");
			new SearchEngine().search(getWorkspace(),  type, REFERENCES,  getJavaSearchScope(), result);
			type = getCompilationUnit("JavaSearch", "src", "j4", "TC47209.java").getType("TC47209");
			new SearchEngine().search(getWorkspace(),  type, REFERENCES,  getJavaSearchScope(), result);
			type = getCompilationUnit("JavaSearch", "src", "j4", "TT47209.java").getType("TM47209");
			new SearchEngine().search(getWorkspace(),  type, REFERENCES,  getJavaSearchScope(), result);
			assertSearchResults(
				"src/j4/TT47209.java j4.TT47209 [TT47209] EXACT_MATCH\n" + 
					"src/j4/TF47209.java j4.TF47209.f47209 [TF47209] EXACT_MATCH\n" + 
					"src/j4/TC47209.java j4.TC47209(String) [TC47209] EXACT_MATCH\n" +
					"src/j4/TM47209.java void j4.TM47209.m47209(int) [TM47209] EXACT_MATCH",
				result);
//		} finally {
//			resetProjectOptions();
//		}
	}
	public void testBug47209field() throws CoreException {
//		try {
			setJavadocOptions();
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			IType type = getCompilationUnit("JavaSearch", "src", "j4", "FT47209.java").getType("FT47209");
			new SearchEngine().search(getWorkspace(),  type, REFERENCES,  getJavaSearchScope(), result);
			type = getCompilationUnit("JavaSearch", "src", "j4", "FF47209.java").getType("FF47209");
			new SearchEngine().search(getWorkspace(),  type, REFERENCES,  getJavaSearchScope(), result);
			type = getCompilationUnit("JavaSearch", "src", "j4", "FC47209.java").getType("FC47209");
			new SearchEngine().search(getWorkspace(),  type, REFERENCES,  getJavaSearchScope(), result);
			type = getCompilationUnit("JavaSearch", "src", "j4", "FT47209.java").getType("FM47209");
			new SearchEngine().search(getWorkspace(),  type, REFERENCES,  getJavaSearchScope(), result);
			assertSearchResults(
				"src/j4/FT47209.java j4.FT47209 [FT47209] EXACT_MATCH\n" + 
					"src/j4/FF47209.java j4.FF47209.f47209 [FF47209] EXACT_MATCH\n" + 
					"src/j4/FC47209.java j4.FC47209(String) [FC47209] EXACT_MATCH\n" +
					"src/j4/FM47209.java void j4.FM47209.m47209(int) [FM47209] EXACT_MATCH",
				result);
//		} finally {
//			resetProjectOptions();
//		}
	}
	public void testBug47209method() throws CoreException {
//		try {
			setJavadocOptions();
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			IType type = getCompilationUnit("JavaSearch", "src", "j4", "MT47209.java").getType("MT47209");
			new SearchEngine().search(getWorkspace(),  type, REFERENCES,  getJavaSearchScope(), result);
			type = getCompilationUnit("JavaSearch", "src", "j4", "MF47209.java").getType("MF47209");
			new SearchEngine().search(getWorkspace(),  type, REFERENCES,  getJavaSearchScope(), result);
			type = getCompilationUnit("JavaSearch", "src", "j4", "MC47209.java").getType("MC47209");
			new SearchEngine().search(getWorkspace(),  type, REFERENCES,  getJavaSearchScope(), result);
			type = getCompilationUnit("JavaSearch", "src", "j4", "MT47209.java").getType("MM47209");
			new SearchEngine().search(getWorkspace(),  type, REFERENCES,  getJavaSearchScope(), result);
			assertSearchResults(
				"src/j4/MT47209.java j4.MT47209 [MT47209] EXACT_MATCH\n" + 
					"src/j4/MF47209.java j4.MF47209.f47209 [MF47209] EXACT_MATCH\n" + 
					"src/j4/MC47209.java j4.MC47209(String) [MC47209] EXACT_MATCH\n" +
					"src/j4/MM47209.java void j4.MM47209.m47209(int) [MM47209] EXACT_MATCH",
				result);
//		} finally {
//			resetProjectOptions();
//		}
	}
	public void testBug47209constructor() throws CoreException {
//		try {
			setJavadocOptions();
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			IType type = getCompilationUnit("JavaSearch", "src", "j4", "CT47209.java").getType("CT47209");
			new SearchEngine().search(getWorkspace(),  type, REFERENCES,  getJavaSearchScope(), result);
			type = getCompilationUnit("JavaSearch", "src", "j4", "CF47209.java").getType("CF47209");
			new SearchEngine().search(getWorkspace(),  type, REFERENCES,  getJavaSearchScope(), result);
			type = getCompilationUnit("JavaSearch", "src", "j4", "CC47209.java").getType("CC47209");
			new SearchEngine().search(getWorkspace(),  type, REFERENCES,  getJavaSearchScope(), result);
			type = getCompilationUnit("JavaSearch", "src", "j4", "CT47209.java").getType("CM47209");
			new SearchEngine().search(getWorkspace(),  type, REFERENCES,  getJavaSearchScope(), result);
			assertSearchResults(
				"src/j4/CT47209.java j4.CT47209 [CT47209] EXACT_MATCH\n" + 
					"src/j4/CF47209.java j4.CF47209.f47209 [CF47209] EXACT_MATCH\n" + 
					"src/j4/CC47209.java j4.CC47209(String) [CC47209] EXACT_MATCH\n" +
					"src/j4/CM47209.java void j4.CM47209.m47209(int) [CM47209] EXACT_MATCH",
				result);
//		} finally {
//			resetProjectOptions();
//		}
	}

	/**
	 * Test fix for bug 49994.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=49994">49994</a>
	 * @throws CoreException
	 */
	public void testBug49994() throws CoreException {
//		try {
			setJavadocOptions();
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			IType type = getCompilationUnit("JavaSearch", "src", "j5", "Bug49994.java").getType("Bug49994");
			new SearchEngine().search(getWorkspace(),  type, REFERENCES,  getJavaSearchScope(), result);
			assertSearchResults("", result);
//		} finally {
//			resetProjectOptions();
//		}
	}
	public void testBug49994field() throws CoreException {
//		try {
			setJavadocOptions();
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			IType type = getCompilationUnit("JavaSearch", "src", "j5", "Bug49994.java").getType("Bug49994");
			IField field = type.getField("field");
			new SearchEngine().search(getWorkspace(), field, REFERENCES, getJavaSearchScope(), result);
			assertSearchResults("src/j5/Bug49994.java void j5.Bug49994.foo() [field] EXACT_MATCH", result);
//		} finally {
//			resetProjectOptions();
//		}
	}
	public void testBug49994method() throws CoreException {
//		try {
			setJavadocOptions();
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			IType type = getCompilationUnit("JavaSearch", "src", "j5", "Bug49994.java").getType("Bug49994");
			IMethod method = type.getMethod("bar", new String[0]);
			new SearchEngine().search(getWorkspace(), method, REFERENCES, getJavaSearchScope(), result);
			assertSearchResults("src/j5/Bug49994.java void j5.Bug49994.foo() [bar] EXACT_MATCH", result);
//		} finally {
//			resetProjectOptions();
//		}
	}
	public void testBug49994constructor() throws CoreException {
//		try {
			setJavadocOptions();
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			IType type = getCompilationUnit("JavaSearch", "src", "j5", "Bug49994.java").getType("Bug49994");
			IMethod method = type.getMethod("Bug49994", new String[] { "QString;" });
			new SearchEngine().search(getWorkspace(), method, REFERENCES, getJavaSearchScope(), result);
			assertSearchResults("src/j5/Bug49994.java void j5.Bug49994.foo() [Bug49994] EXACT_MATCH", result);
//		} finally {
//			resetProjectOptions();
//		}
	}
}
