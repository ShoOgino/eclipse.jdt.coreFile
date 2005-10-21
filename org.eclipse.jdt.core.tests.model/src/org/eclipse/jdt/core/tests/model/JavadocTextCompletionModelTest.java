/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

/**
 * Test class for completion in text of a Javadoc comment.
 */
public class JavadocTextCompletionModelTest extends AbstractJavadocCompletionModelTest {

public JavadocTextCompletionModelTest(String name) {
	super(name);
}

static {
//	TESTS_NUMBERS = new int[] { 34 };
//	TESTS_RANGE = new int[] { 51, -1 };
}
public static Test suite() {
	return buildTestSuite(JavadocTextCompletionModelTest.class);
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.model.AbstractJavadocCompletionModelTest#setUp()
 */
protected void setUp() throws Exception {
	super.setUp();
	setUpProjectOptions(CompilerOptions.VERSION_1_4);
}

/**
 * @category Tests for tag names completion
 */
public void test001() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"/**\n" +
		" * Completion on @ inside text\n" +
		" */\n" +
		"public class BasicTestTextIns {}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "@");
	assertResults(
		"link[JAVADOC_INLINE_TAG]{{@link }, null, null, link, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"docRoot[JAVADOC_INLINE_TAG]{{@docRoot }, null, null, docRoot, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"linkplain[JAVADOC_INLINE_TAG]{{@linkplain }, null, null, linkplain, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"value[JAVADOC_INLINE_TAG]{{@value }, null, null, value, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test002() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"/**\n" +
		" * Completion on @s inside text\n" +
		" */\n" +
		"public class BasicTestTextIns {}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "@s");
	assertResults("");
}

public void test003() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_4);
	String source =
		"package javadoc.text;\n" +
		"public class BasicTestTextIns {\n" +
		"	/**\n" +
		"	 * Completion on @ inside text\n" +
		"	 */\n" +
		"	int field;\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "@");
	assertResults(
		"link[JAVADOC_INLINE_TAG]{{@link }, null, null, link, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"docRoot[JAVADOC_INLINE_TAG]{{@docRoot }, null, null, docRoot, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"linkplain[JAVADOC_INLINE_TAG]{{@linkplain }, null, null, linkplain, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"value[JAVADOC_INLINE_TAG]{{@value }, null, null, value, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test004() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_4);
	String source =
		"package javadoc.text;\n" +
		"public class BasicTestTextIns {\n" +
		"	/**\n" +
		"	 * Completion on @d inside text\n" +
		"	 */\n" +
		"	int field;\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "@d");
	assertResults(
		"docRoot[JAVADOC_INLINE_TAG]{{@docRoot }, null, null, docRoot, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test005() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" +
		"	/**\n" +
		"	 * Completion on empty tag name: @\n" +
		"	 */\n" +
		"	public void foo() {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "@");
	assertResults(
		"link[JAVADOC_INLINE_TAG]{{@link }, null, null, link, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"docRoot[JAVADOC_INLINE_TAG]{{@docRoot }, null, null, docRoot, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"inheritDoc[JAVADOC_INLINE_TAG]{{@inheritDoc }, null, null, inheritDoc, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"linkplain[JAVADOC_INLINE_TAG]{{@linkplain }, null, null, linkplain, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"value[JAVADOC_INLINE_TAG]{{@value }, null, null, value, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test006() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" +
		"	/**\n" +
		"	 * Completion on impossible tag name: @ret\n" +
		"	 */\n" +
		"	public void foo() {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "@ret");
	assertResults("");
}

public void test007() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" +
		"	/**\n" +
		"	 * Completion on one letter: @l\n" +
		"	 */\n" +
		"	public void foo() {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "@l");
	assertResults(
		"link[JAVADOC_INLINE_TAG]{{@link }, null, null, link, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"linkplain[JAVADOC_INLINE_TAG]{{@linkplain }, null, null, linkplain, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test008() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" +
		"	/**\n" +
		"	 * Completion on started inline tag: {@li\n" +
		"	 */\n" +
		"	public void foo() {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "{@li");
	assertResults(
		"link[JAVADOC_INLINE_TAG]{{@link }, null, null, link, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"linkplain[JAVADOC_INLINE_TAG]{{@linkplain }, null, null, linkplain, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test009() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" +
		"	/**\n" +
		"	 * Completion with several letters:\n" +
		"	 *		@param str @inh\n" +
		"	 */\n" +
		"	public void foo(String str) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "@inh");
	assertResults(
		"inheritDoc[JAVADOC_INLINE_TAG]{{@inheritDoc }, null, null, inheritDoc, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test010() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" +
		"	/**\n" +
		"	 * Completion with several letters:\n" +
		"	 *		@param str {@inh\n" +
		"	 */\n" +
		"	public void foo(String str) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "{@inh");
	assertResults(
		"inheritDoc[JAVADOC_INLINE_TAG]{{@inheritDoc }, null, null, inheritDoc, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test011() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" +
		"	/**\n" +
		"	 * Completion on full tag name: {@docRoot}\n" +
		"	 */\n" +
		"	public void foo() {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "{@docRoot");
	assertResults(
		"docRoot[JAVADOC_INLINE_TAG]{{@docRoot }, null, null, docRoot, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

/**
 * @category Tests for types completion
 */
public void test020() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"\n" + 
		"	/**\n" + 
		"	 * Completion after: BasicTestTextIns\n" + 
		"	 */\n" + 
		"	public void foo() {\n" + 
		"	}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "BasicTestTextIns", -1);
	assertSortedResults(
		"BasicTestTextIns[JAVADOC_TYPE_REF]{{@link BasicTestTextIns }, javadoc.text, Ljavadoc.text.BasicTestTextIns;, null, null, "+this.positions+(25+R_INLINE_TAG)+"}\n" + 
		"BasicTestTextIns[TYPE_REF]{BasicTestTextIns, javadoc.text, Ljavadoc.text.BasicTestTextIns;, null, null, "+this.positions+"25}"
	);
}

public void test021() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"\n" + 
		"	/**\n" + 
		"	 * Completion after: {@link BasicTestTextIns\n" + 
		"	 */\n" + 
		"	public void foo() {}\n" + 
		"}\n" + 
		"class BasicTestTextInsException extends Exception{\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "BasicTestTextIns", 2);
	assertSortedResults(
		"BasicTestTextIns[TYPE_REF]{BasicTestTextIns, javadoc.text, Ljavadoc.text.BasicTestTextIns;, null, null, "+this.positions+"25}\n" + 
		"BasicTestTextInsException[TYPE_REF]{BasicTestTextInsException, javadoc.text, Ljavadoc.text.BasicTestTextInsException;, null, null, "+this.positions+"21}"
	);
}

public void test022() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"\n" + 
		"	/**\n" + 
		"	 * Completion after: @link BasicTestTextIns\n" + 
		"	 */\n" + 
		"	public void foo() throws BasicTestTextInsException {}\n" + 
		"}\n" + 
		"class BasicTestTextInsException extends Exception{\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "BasicTestTextIns", 2); // 2nd occurence
	assertSortedResults(
		"BasicTestTextInsException[JAVADOC_TYPE_REF]{{@link BasicTestTextInsException }, javadoc.text, Ljavadoc.text.BasicTestTextInsException;, null, null, "+this.positions+(51+R_INLINE_TAG)+"}\n" + 
		"BasicTestTextIns[JAVADOC_TYPE_REF]{{@link BasicTestTextIns }, javadoc.text, Ljavadoc.text.BasicTestTextIns;, null, null, "+this.positions+(25+R_INLINE_TAG)+"}\n" +
		"BasicTestTextInsException[TYPE_REF]{BasicTestTextInsException, javadoc.text, Ljavadoc.text.BasicTestTextInsException;, null, null, "+this.positions+"51}\n" + 
		"BasicTestTextIns[TYPE_REF]{BasicTestTextIns, javadoc.text, Ljavadoc.text.BasicTestTextIns;, null, null, "+this.positions+"25}"
	);
}

public void test023() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"\n" + 
		"	/**\n" + 
		"	 * Completion after: @see BasicTestTextIns\n" + 
		"	 */\n" + 
		"	public void foo() {\n" + 
		"	}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "BasicTestTextIns", 2); // 2nd occurence
	assertSortedResults(
		"BasicTestTextIns[JAVADOC_TYPE_REF]{{@link BasicTestTextIns }, javadoc.text, Ljavadoc.text.BasicTestTextIns;, null, null, "+this.positions+(25+R_INLINE_TAG)+"}\n" + 
		"BasicTestTextIns[TYPE_REF]{BasicTestTextIns, javadoc.text, Ljavadoc.text.BasicTestTextIns;, null, null, "+this.positions+"25}"
	);
}

public void test024() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"\n" + 
		"	/**\n" + 
		"	 * Completion after: java.la\n" + 
		"	 */\n" + 
		"	public void foo() throws InterruptedException {\n" + 
		"	}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "java.la");
	assertSortedResults(
		"java.lang[PACKAGE_REF]{java.lang, java.lang, null, null, null, "+this.positions+"20}"
	);
}

public void test025() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"\n" + 
		"	/**\n" + 
		"	 * Completion after: java.lang.I\n" + 
		"	 */\n" + 
		"	public void foo() throws InterruptedException {\n" + 
		"	}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "java.lang.I");
	assertSortedResults(
		"InterruptedException[JAVADOC_TYPE_REF]{{@link InterruptedException }, java.lang, Ljava.lang.InterruptedException;, null, null, "+this.positions+(48+R_INLINE_TAG)+"}\n" + 
		"IllegalMonitorStateException[JAVADOC_TYPE_REF]{{@link IllegalMonitorStateException }, java.lang, Ljava.lang.IllegalMonitorStateException;, null, null, "+this.positions+(18+R_INLINE_TAG)+"}\n" + 
		"InterruptedException[TYPE_REF]{InterruptedException, java.lang, Ljava.lang.InterruptedException;, null, null, "+this.positions+"48}\n" + 
		"IllegalMonitorStateException[TYPE_REF]{IllegalMonitorStateException, java.lang, Ljava.lang.IllegalMonitorStateException;, null, null, "+this.positions+"18}"
	);
}

/**
 * @category Tests for fields completion
 */
public void test030() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: #fo\n" + 
		"	 */\n" + 
		"	int foo;\n" + 
		"}";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "#fo");
	assertSortedResults(
		"foo[JAVADOC_FIELD_REF]{{@link #foo }, Ljavadoc.text.BasicTestTextIns;, I, foo, null, "+this.positions+(29+R_INLINE_TAG)+"}"
	);
}

public void test031() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: BasicTestTextIns#fo\n" + 
		"	 */\n" + 
		"	static int foo;\n" + 
		"}";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "BasicTestTextIns#fo");
	assertSortedResults(
		"foo[JAVADOC_FIELD_REF]{{@link BasicTestTextIns#foo }, Ljavadoc.text.BasicTestTextIns;, I, foo, null, "+this.positions+(18+R_INLINE_TAG)+"}\n" + 
		"foo[JAVADOC_VALUE_REF]{{@value BasicTestTextIns#foo }, Ljavadoc.text.BasicTestTextIns;, I, foo, null, "+this.positions+(18+R_INLINE_TAG)+"}"
	);
}

public void test032() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: javadoc.text.BasicTestTextIns#fo\n" + 
		"	 */\n" + 
		"	int foo;\n" + 
		"}";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "javadoc.text.BasicTestTextIns#fo");
	assertSortedResults(
		"foo[JAVADOC_FIELD_REF]{{@link javadoc.text.BasicTestTextIns#foo }, Ljavadoc.text.BasicTestTextIns;, I, foo, null, "+this.positions+(29+R_INLINE_TAG)+"}"
	);
}

public void test033() throws JavaModelException {
	String[] sources = {
		"/Completion/src/javadoc/text/BasicTestTextIns.java",
			"package javadoc.text;\n" + 
			"public class BasicTestTextIns {\n" + 
			"	/**\n" + 
			"	 * Completion after: OtherFields#fo\n" + 
			"	 */\n" + 
			"	int foo;\n" +
			"}",
		"/Completion/src/javadoc/text/OtherFields.java",
			"package javadoc.text;\n" + 
			"public class OtherFields {\n" + 
			"	static int foo;\n" + 
			"}"
	};
	completeInJavadoc(sources, true, "OtherFields#fo");
	assertSortedResults(
		"foo[JAVADOC_FIELD_REF]{{@link OtherFields#foo }, Ljavadoc.text.OtherFields;, I, foo, null, "+this.positions+(18+R_INLINE_TAG)+"}\n" + 
		"foo[JAVADOC_VALUE_REF]{{@value OtherFields#foo }, Ljavadoc.text.OtherFields;, I, foo, null, "+this.positions+(18+R_INLINE_TAG)+"}"
	);
}

public void test034() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: {@value #fo\n" + 
		"	 *		Note: this test must be run with 1.4 compliance\n" + 
		"	 */\n" + 
		"	int foo;\n" + 
		"}";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "#fo");
	assertSortedResults(
		"foo[JAVADOC_FIELD_REF]{{@link #foo }, Ljavadoc.text.BasicTestTextIns;, I, foo, null, "+this.positions+(29+R_INLINE_TAG)+"}"
	);
}

public void test035() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: {@value #fo\n" + 
		"	 *		Note: this test must be run with 1.5 compliance\n" + 
		"	 */\n" + 
		"	int foo;\n" + 
		"}";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "fo");
	assertSortedResults(
		"foo[FIELD_REF]{foo, Ljavadoc.text.BasicTestTextIns;, I, foo, null, "+this.positions+"29}"
	);
}

public void test036() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: {@link BasicTestTextIns#fo\n" + 
		"	 */\n" + 
		"	static int foo;\n" + 
		"}";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "fo");
	assertSortedResults(
		"foo[FIELD_REF]{foo, Ljavadoc.text.BasicTestTextIns;, I, foo, null, "+this.positions+"18}"
	);
}

public void test037() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: {@link javadoc.text.BasicTestTextIns#fo }\n" + 
		"	 */\n" + 
		"	int foo;\n" + 
		"}";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "fo");
	assertSortedResults(
		"foo[FIELD_REF]{foo, Ljavadoc.text.BasicTestTextIns;, I, foo, null, "+this.positions+"29}"
	);
}

public void test038() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String[] sources = {
		"/Completion/src/javadoc/text/BasicTestTextIns.java",
			"package javadoc.text;\n" + 
			"public class BasicTestTextIns {\n" + 
			"	/**\n" + 
			"	 * Completion after: {@value OtherFields#fo\n" + 
		"	 *		Note: this test must be run with 1.5 compliance\n" + 
			"	 */\n" + 
			"	int foo;\n" +
			"}",
		"/Completion/src/javadoc/text/OtherFields.java",
			"package javadoc.text;\n" + 
			"public class OtherFields {\n" + 
			"	static int foo;\n" + 
			"}"
	};
	completeInJavadoc(sources, true, "fo");
	assertSortedResults(
		"foo[FIELD_REF]{foo, Ljavadoc.text.OtherFields;, I, foo, null, "+this.positions+"18}"
	);
}

/**
 * @category Tests for methods completion
 */
public void test040() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: meth\n" + 
		"	 */\n" + 
		"	void method() {}\n" + 
		"	void paramMethod(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "meth");
	assertSortedResults("");
}

public void test041() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: #meth\n" + 
		"	 */\n" + 
		"	void method() {}\n" + 
		"	void paramMethod(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "#meth");
	assertSortedResults(
		"method[JAVADOC_METHOD_REF]{{@link #method() }, Ljavadoc.text.BasicTestTextIns;, ()V, method, null, "+this.positions+(29+R_INLINE_TAG)+"}"
	);
}

public void test042() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: #meth with text after\n" + 
		"	 */\n" + 
		"	void method() {}\n" + 
		"	void paramMethod(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "#meth");
	assertSortedResults(
		"method[JAVADOC_METHOD_REF]{{@link #method() }, Ljavadoc.text.BasicTestTextIns;, ()V, method, null, "+this.positions+(29+R_INLINE_TAG)+"}"
	);
}

public void test043() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: #method\n" + 
		"	 */\n" + 
		"	void method() {}\n" + 
		"	void paramMethod(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "#meth");
	assertSortedResults(
		"method[JAVADOC_METHOD_REF]{{@link #method() }, Ljavadoc.text.BasicTestTextIns;, ()V, method, null, "+this.positions+(29+R_INLINE_TAG)+"}"
	);
}

public void test044() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: BasicTestTextIns#param\n" + 
		"	 */\n" + 
		"	void method() {}\n" + 
		"	void paramMethod(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "BasicTestTextIns#param");
	assertSortedResults(
		"paramMethod[JAVADOC_METHOD_REF]{{@link BasicTestTextIns#paramMethod(String, boolean, Object) }, Ljavadoc.text.BasicTestTextIns;, (Ljava.lang.String;ZLjava.lang.Object;)V, paramMethod, (str, flag, obj), "+this.positions+(29+R_INLINE_TAG)+"}"
	);
}

public void test045() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: javadoc.text.BasicTestTextIns#meth\n" + 
		"	 */\n" + 
		"	void method() {}\n" + 
		"	void paramMethod(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "javadoc.text.BasicTestTextIns#meth");
	assertSortedResults(
		"method[JAVADOC_METHOD_REF]{{@link javadoc.text.BasicTestTextIns#method() }, Ljavadoc.text.BasicTestTextIns;, ()V, method, null, "+this.positions+(29+R_INLINE_TAG)+"}"
	);
}

public void test046() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	void method() {}\n" + 
		"	/**\n" + 
		"	 * Completion after: {@link #param\n" + 
		"	 */\n" + 
		"	void paramMethod(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "param");
	assertSortedResults(
		"paramMethod[METHOD_REF]{paramMethod(String, boolean, Object), Ljavadoc.text.BasicTestTextIns;, (Ljava.lang.String;ZLjava.lang.Object;)V, paramMethod, (str, flag, obj), "+this.positions+"29}"
	);
}

public void test047() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: {@link BasicTestTextIns#meth\n" + 
		"	 */\n" + 
		"	void method() {}\n" + 
		"	void paramMethod(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "meth");
	assertSortedResults(
		"method[METHOD_REF]{method(), Ljavadoc.text.BasicTestTextIns;, ()V, method, null, "+this.positions+"29}"
	);
}

public void test048() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	void method() {}\n" + 
		"	/**\n" + 
		"	 * Completion after: {@link javadoc.text.BasicTestTextIns#param }\n" + 
		"	 */\n" + 
		"	void paramMethod(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "param");
	assertSortedResults(
		"paramMethod[METHOD_REF]{paramMethod(String, boolean, Object), Ljavadoc.text.BasicTestTextIns;, (Ljava.lang.String;ZLjava.lang.Object;)V, paramMethod, (str, flag, obj), "+this.positions+"29}"
	);
}

public void test049() throws JavaModelException {
	String[] sources = {
		"/Completion/src/javadoc/text/BasicTestTextIns.java",
			"package javadoc.text;\n" + 
			"public class BasicTestTextIns {\n" + 
			"	/**\n" + 
			"	 * Completion after: OtherTypes#meth\n" + 
			"	 */\n" + 
			"	void foo() {};\n" +
			"}",
		"/Completion/src/javadoc/text/OtherTypes.java",
			"package javadoc.text;\n" + 
			"public class OtherTypes {\n" + 
			"	void method() {};\n" +
			"}"
	};
	completeInJavadoc(sources, true, "OtherTypes#meth");
	assertSortedResults(
		"method[JAVADOC_METHOD_REF]{{@link OtherTypes#method() }, Ljavadoc.text.OtherTypes;, ()V, method, null, "+this.positions+(29+R_INLINE_TAG)+"}"
	);
}

public void test050() throws JavaModelException {
	String[] sources = {
		"/Completion/src/javadoc/text/BasicTestTextIns.java",
			"package javadoc.text;\n" + 
			"public class BasicTestTextIns {\n" + 
			"	/**\n" + 
			"	 * Completion after: {@link OtherTypes#method }\n" + 
			"	 */\n" + 
			"	void foo() {};\n" +
			"}",
		"/Completion/src/javadoc/text/OtherTypes.java",
			"package javadoc.text;\n" + 
			"public class OtherTypes {\n" + 
			"	void method() {};\n" +
			"}"
	};
	completeInJavadoc(sources, true, "meth");
	assertSortedResults(
		"method[METHOD_REF]{method(), Ljavadoc.text.OtherTypes;, ()V, method, null, "+this.positions+"29}"
	);
}

public void test051() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: #\n" + 
		"	 */\n" + 
		"	void method() {}\n" + 
		"	void paramMethod(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "#");
	assertSortedResults(
		"method[JAVADOC_METHOD_REF]{{@link #method() }, Ljavadoc.text.BasicTestTextIns;, ()V, method, null, "+this.positions+(29+R_INLINE_TAG)+"}\n" + 
		"paramMethod[JAVADOC_METHOD_REF]{{@link #paramMethod(String, boolean, Object) }, Ljavadoc.text.BasicTestTextIns;, (Ljava.lang.String;ZLjava.lang.Object;)V, paramMethod, (str, flag, obj), "+this.positions+(29+R_INLINE_TAG)+"}\n" + 
		"BasicTestTextIns[JAVADOC_METHOD_REF]{{@link #BasicTestTextIns() }, Ljavadoc.text.BasicTestTextIns;, ()V, BasicTestTextIns, null, "+this.positions+(JAVADOC_RELEVANCE+R_INLINE_TAG)+"}"
	);
}

public void test052() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: #method\n" + 
		"	 */\n" + 
		"	void method() {}\n" + 
		"	void paramMethod(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "#");
	assertSortedResults(
		"method[JAVADOC_METHOD_REF]{{@link #method() }, Ljavadoc.text.BasicTestTextIns;, ()V, method, null, "+this.positions+(29+R_INLINE_TAG)+"}\n" + 
		"paramMethod[JAVADOC_METHOD_REF]{{@link #paramMethod(String, boolean, Object) }, Ljavadoc.text.BasicTestTextIns;, (Ljava.lang.String;ZLjava.lang.Object;)V, paramMethod, (str, flag, obj), "+this.positions+(29+R_INLINE_TAG)+"}\n" + 
		"BasicTestTextIns[JAVADOC_METHOD_REF]{{@link #BasicTestTextIns() }, Ljavadoc.text.BasicTestTextIns;, ()V, BasicTestTextIns, null, "+this.positions+(JAVADOC_RELEVANCE+R_INLINE_TAG)+"}"
	);
}

public void test053() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: {@link BasicTestTextIns#\n" + 
		"	 */\n" + 
		"	void method() {}\n" + 
		"	void paramMethod(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "#", 0); //empty token
	assertSortedResults(
		"method[METHOD_REF]{method(), Ljavadoc.text.BasicTestTextIns;, ()V, method, null, "+this.positions+"29}\n" + 
		"paramMethod[METHOD_REF]{paramMethod(String, boolean, Object), Ljavadoc.text.BasicTestTextIns;, (Ljava.lang.String;ZLjava.lang.Object;)V, paramMethod, (str, flag, obj), "+this.positions+"29}\n" + 
		"BasicTestTextIns[METHOD_REF<CONSTRUCTOR>]{BasicTestTextIns(), Ljavadoc.text.BasicTestTextIns;, ()V, BasicTestTextIns, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test054() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: {@link javadoc.text.BasicTestTextIns# }\n" + 
		"	 */\n" + 
		"	void method() {}\n" + 
		"	void paramMethod(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "#", 0); //empty token
	assertSortedResults(
		"method[METHOD_REF]{method(), Ljavadoc.text.BasicTestTextIns;, ()V, method, null, "+this.positions+"29}\n" + 
		"paramMethod[METHOD_REF]{paramMethod(String, boolean, Object), Ljavadoc.text.BasicTestTextIns;, (Ljava.lang.String;ZLjava.lang.Object;)V, paramMethod, (str, flag, obj), "+this.positions+"29}\n" + 
		"BasicTestTextIns[METHOD_REF<CONSTRUCTOR>]{BasicTestTextIns(), Ljavadoc.text.BasicTestTextIns;, ()V, BasicTestTextIns, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test055() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	void method() {}\n" + 
		"	/**\n" + 
		"	 * Completion after: #paramMethod(\n" + 
		"	 */\n" + 
		"	void paramMethod(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "#paramMethod(");
	assertSortedResults(
		"paramMethod[JAVADOC_METHOD_REF]{{@link #paramMethod(String, boolean, Object) }, Ljavadoc.text.BasicTestTextIns;, (Ljava.lang.String;ZLjava.lang.Object;)V, paramMethod, (str, flag, obj), "+this.positions+(25+R_INLINE_TAG)+"}"
	);
}

public void test056() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	void method() {}\n" + 
		"	/**\n" + 
		"	 * Completion after: {@link #paramMethod(Str\n" + 
		"	 */\n" + 
		"	void paramMethod(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "Str");
	assertSortedResults(
		"String[TYPE_REF]{String, java.lang, Ljava.lang.String;, null, null, "+this.positions+"21}"
	);
}

public void test057() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	void method() {}\n" + 
		"	/**\n" + 
		"	 * Completion after: {@link #paramMethod(String s\n" + 
		"	 */\n" + 
		"	void paramMethod(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "paramMethod(String s");
	assertSortedResults(
		"paramMethod[METHOD_REF]{paramMethod(String, boolean, Object), Ljavadoc.text.BasicTestTextIns;, (Ljava.lang.String;ZLjava.lang.Object;)V, paramMethod, (str, flag, obj), "+this.positions+"25}"
	);
}

public void test058() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	void method() {}\n" + 
		"	/**\n" + 
		"	 * Completion after: #paramMethod(String str, \n" + 
		"	 */\n" + 
		"	void paramMethod(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "#paramMethod(String str,");
	assertSortedResults(
		"paramMethod[JAVADOC_METHOD_REF]{{@link #paramMethod(String, boolean, Object) }, Ljavadoc.text.BasicTestTextIns;, (Ljava.lang.String;ZLjava.lang.Object;)V, paramMethod, (str, flag, obj), "+this.positions+(25+R_INLINE_TAG)+"}"
	);
}

public void test059() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	void method() {}\n" + 
		"	/**\n" + 
		"	 * Completion after: {@link #paramMethod(String,\n" + 
		"	 */\n" + 
		"	void paramMethod(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "paramMethod(String,");
	assertSortedResults(
		"paramMethod[METHOD_REF]{paramMethod(String, boolean, Object), Ljavadoc.text.BasicTestTextIns;, (Ljava.lang.String;ZLjava.lang.Object;)V, paramMethod, (str, flag, obj), "+this.positions+"25}"
	);
}

/**
 * @category Tests for constructors completion
 */
public void test070() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: #BasicTest\n" + 
		"	 */\n" + 
		"	BasicTestTextIns() {}\n" + 
		"	BasicTestTextIns(int xxx, float real, Class clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "#BasicTest");
	assertSortedResults(
		"BasicTestTextIns[JAVADOC_METHOD_REF]{{@link #BasicTestTextIns(int, float, Class) }, Ljavadoc.text.BasicTestTextIns;, (IFLjava.lang.Class;)V, BasicTestTextIns, (xxx, real, clazz), "+this.positions+(JAVADOC_RELEVANCE+R_INLINE_TAG)+"}\n" + 
		"BasicTestTextIns[JAVADOC_METHOD_REF]{{@link #BasicTestTextIns() }, Ljavadoc.text.BasicTestTextIns;, ()V, BasicTestTextIns, null, "+this.positions+(JAVADOC_RELEVANCE+R_INLINE_TAG)+"}"
	);
}

public void test071() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: {@link BasicTestTextIns#BasicTest\n" + 
		"	 */\n" + 
		"	BasicTestTextIns() {}\n" + 
		"	BasicTestTextIns(int xxx, float real, Class clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "BasicTest", 3); // 3rd occurence
	assertSortedResults(
		"BasicTestTextIns[METHOD_REF<CONSTRUCTOR>]{BasicTestTextIns(int, float, Class), Ljavadoc.text.BasicTestTextIns;, (IFLjava.lang.Class;)V, BasicTestTextIns, (xxx, real, clazz), "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"BasicTestTextIns[METHOD_REF<CONSTRUCTOR>]{BasicTestTextIns(), Ljavadoc.text.BasicTestTextIns;, ()V, BasicTestTextIns, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test072() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: {@linkplain javadoc.text.BasicTestTextIns#BasicTest }\n" + 
		"	 */\n" + 
		"	BasicTestTextIns() {}\n" + 
		"	BasicTestTextIns(int xxx, float real, Class clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "BasicTest", 3); // 3rd occurence
	assertSortedResults(
		"BasicTestTextIns[METHOD_REF<CONSTRUCTOR>]{BasicTestTextIns(int, float, Class), Ljavadoc.text.BasicTestTextIns;, (IFLjava.lang.Class;)V, BasicTestTextIns, (xxx, real, clazz), "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"BasicTestTextIns[METHOD_REF<CONSTRUCTOR>]{BasicTestTextIns(), Ljavadoc.text.BasicTestTextIns;, ()V, BasicTestTextIns, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test073() throws JavaModelException {
	String[] sources = {
		"/Completion/src/javadoc/text/BasicTestTextIns.java",
			"package javadoc.text;\n" + 
			"public class BasicTestTextIns {\n" + 
			"	/**\n" + 
			"	 * Completion after: OtherTypes#Other\n" + 
			"	 */\n" + 
			"	void foo() {};\n" +
			"}",
		"/Completion/src/javadoc/text/OtherTypes.java",
			"package javadoc.text;\n" + 
			"public class OtherTypes {\n" + 
			"	OtherTypes() {};\n" +
			"}"
	};
	completeInJavadoc(sources, true, "OtherTypes#O");
	assertSortedResults(
		"OtherTypes[JAVADOC_METHOD_REF]{{@link OtherTypes#OtherTypes() }, Ljavadoc.text.OtherTypes;, ()V, OtherTypes, null, "+this.positions+(JAVADOC_RELEVANCE+R_INLINE_TAG)+"}"
	);
}

public void test074() throws JavaModelException {
	String[] sources = {
		"/Completion/src/javadoc/text/BasicTestTextIns.java",
			"package javadoc.text;\n" + 
			"public class BasicTestTextIns {\n" + 
			"	/**\n" + 
			"	 * Completion after: {@link OtherTypes#O implicit default constructor\n" + 
			"	 */\n" + 
			"	void foo() {};\n" +
			"}",
		"/Completion/src/javadoc/text/OtherTypes.java",
			"package javadoc.text;\n" + 
			"public class OtherTypes {\n" + 
			"}"
	};
	completeInJavadoc(sources, true, "O", 2); // 2nd occurence
	assertSortedResults(
		"OtherTypes[METHOD_REF<CONSTRUCTOR>]{OtherTypes(), Ljavadoc.text.OtherTypes;, ()V, OtherTypes, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test075() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: #\n" + 
		"	 */\n" + 
		"	BasicTestTextIns() {}\n" + 
		"	BasicTestTextIns(int xxx, float real, Class clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "#");
	assertSortedResults(
		"BasicTestTextIns[JAVADOC_METHOD_REF]{{@link #BasicTestTextIns(int, float, Class) }, Ljavadoc.text.BasicTestTextIns;, (IFLjava.lang.Class;)V, BasicTestTextIns, (xxx, real, clazz), "+this.positions+(JAVADOC_RELEVANCE+R_INLINE_TAG)+"}\n" + 
		"BasicTestTextIns[JAVADOC_METHOD_REF]{{@link #BasicTestTextIns() }, Ljavadoc.text.BasicTestTextIns;, ()V, BasicTestTextIns, null, "+this.positions+(JAVADOC_RELEVANCE+R_INLINE_TAG)+"}"
	);
}

public void test076() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: #blabla\n" + 
		"	 */\n" + 
		"	BasicTestTextIns() {}\n" + 
		"	BasicTestTextIns(int xxx, float real, Class clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "#");
	assertSortedResults(
		"BasicTestTextIns[JAVADOC_METHOD_REF]{{@link #BasicTestTextIns(int, float, Class) }, Ljavadoc.text.BasicTestTextIns;, (IFLjava.lang.Class;)V, BasicTestTextIns, (xxx, real, clazz), "+this.positions+(JAVADOC_RELEVANCE+R_INLINE_TAG)+"}\n" + 
		"BasicTestTextIns[JAVADOC_METHOD_REF]{{@link #BasicTestTextIns() }, Ljavadoc.text.BasicTestTextIns;, ()V, BasicTestTextIns, null, "+this.positions+(JAVADOC_RELEVANCE+R_INLINE_TAG)+"}"
	);
}

public void test077() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: {@link # }\n" + 
		"	 */\n" + 
		"	BasicTestTextIns() {}\n" + 
		"	BasicTestTextIns(int xxx, float real, Class clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "#", 0); // empty token
	assertSortedResults(
		"BasicTestTextIns[METHOD_REF<CONSTRUCTOR>]{BasicTestTextIns(int, float, Class), Ljavadoc.text.BasicTestTextIns;, (IFLjava.lang.Class;)V, BasicTestTextIns, (xxx, real, clazz), "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"BasicTestTextIns[METHOD_REF<CONSTRUCTOR>]{BasicTestTextIns(), Ljavadoc.text.BasicTestTextIns;, ()V, BasicTestTextIns, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test078() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	void method() {}\n" + 
		"	/**\n" + 
		"	 * Completion after: {@link #BasicTestTextIns(\n" + 
		"	 */\n" + 
		"	BasicTestTextIns(int xxx, float real, Class clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "BasicTestTextIns(");
	assertSortedResults(
		"BasicTestTextIns[METHOD_REF<CONSTRUCTOR>]{BasicTestTextIns(int, float, Class), Ljavadoc.text.BasicTestTextIns;, (IFLjava.lang.Class;)V, BasicTestTextIns, (xxx, real, clazz), "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test079() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	void method() {}\n" + 
		"	/**\n" + 
		"	 * Completion after: BasicTestTextIns#BasicTestTextIns(int,\n" + 
		"	 */\n" + 
		"	BasicTestTextIns() {}\n" + 
		"	BasicTestTextIns(int xxx, float real, Class clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "BasicTestTextIns#BasicTestTextIns(int,");
	assertSortedResults(
		"BasicTestTextIns[JAVADOC_METHOD_REF]{{@link BasicTestTextIns#BasicTestTextIns(int, float, Class) }, Ljavadoc.text.BasicTestTextIns;, (IFLjava.lang.Class;)V, BasicTestTextIns, (xxx, real, clazz), "+this.positions+(JAVADOC_RELEVANCE+R_INLINE_TAG)+"}"
	);
}

public void test080() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	void method() {}\n" + 
		"	/**\n" + 
		"	 * Completion after: BasicTestTextIns#BasicTestTextIns(int,\n" + 
		"	 * 	Note: completion takes place just after opening brace\n" + 
		"	 */\n" + 
		"	BasicTestTextIns() {}\n" + 
		"	BasicTestTextIns(int xxx, float real, Class clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "BasicTestTextIns#BasicTestTextIns(");
	assertSortedResults(
			"BasicTestTextIns[JAVADOC_METHOD_REF]{{@link BasicTestTextIns#BasicTestTextIns(int, float, Class) }, Ljavadoc.text.BasicTestTextIns;, (IFLjava.lang.Class;)V, BasicTestTextIns, (xxx, real, clazz), "+this.positions+(JAVADOC_RELEVANCE+R_INLINE_TAG)+"}\n" + 
			"BasicTestTextIns[JAVADOC_METHOD_REF]{{@link BasicTestTextIns#BasicTestTextIns() }, Ljavadoc.text.BasicTestTextIns;, ()V, BasicTestTextIns, null, "+this.positions+(JAVADOC_RELEVANCE+R_INLINE_TAG)+"}"
	);
}

public void test081() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	javadoc.text.BasicTestTextIns#BasicTestTextIns(\n" + 
		"	 */\n" + 
		"	void method() {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "javadoc.text.BasicTestTextIns#BasicTestTextIns(");
	assertSortedResults(
		"BasicTestTextIns[JAVADOC_METHOD_REF]{{@link javadoc.text.BasicTestTextIns#BasicTestTextIns() }, Ljavadoc.text.BasicTestTextIns;, ()V, BasicTestTextIns, null, "+this.positions+(JAVADOC_RELEVANCE+R_INLINE_TAG)+"}"
	);
}

public void test082() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	void method() {}\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	#BasicTestTextIns(int, float, java.lang.\n" + 
		"	 */\n" + 
		"	BasicTestTextIns(int xxx, float real, Class clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "#BasicTestTextIns(int, float, java.lang.");
	assertSortedResults(
		"BasicTestTextIns[JAVADOC_METHOD_REF]{{@link #BasicTestTextIns(int, float, Class) }, Ljavadoc.text.BasicTestTextIns;, (IFLjava.lang.Class;)V, BasicTestTextIns, (xxx, real, clazz), "+this.positions+(JAVADOC_RELEVANCE+R_INLINE_TAG)+"}"
	);
}

public void test083() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	void method() {}\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	#BasicTestTextIns(int, float, java.lang.Cla\n" + 
		"	 */\n" + 
		"	BasicTestTextIns(int xxx, float real, Class clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "#BasicTestTextIns(int, float, java.lang.Cla");
	assertSortedResults(
		"BasicTestTextIns[JAVADOC_METHOD_REF]{{@link #BasicTestTextIns(int, float, Class) }, Ljavadoc.text.BasicTestTextIns;, (IFLjava.lang.Class;)V, BasicTestTextIns, (xxx, real, clazz), "+this.positions+(JAVADOC_RELEVANCE+R_INLINE_TAG)+"}"
	);
}

public void test084() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	void method() {}\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	#BasicTestTextIns(int, float, Class)\n" + 
		"	 * 	Note: completion takes place before closing parenthesis\n" + 
		"	 */\n" + 
		"	BasicTestTextIns(int xxx, float real, Class clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "#BasicTestTextIns(int, float, Class");
	assertSortedResults(
		"BasicTestTextIns[JAVADOC_METHOD_REF]{{@link #BasicTestTextIns(int, float, Class) }, Ljavadoc.text.BasicTestTextIns;, (IFLjava.lang.Class;)V, BasicTestTextIns, (xxx, real, clazz), "+this.positions+(JAVADOC_RELEVANCE+R_INLINE_TAG)+"}"
	);
}

public void test085() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	void method() {}\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	#BasicTestTextIns(int, float, Class)\n" + 
		"	 * 	Note: completion takes place after closing parenthesis\n" + 
		"	 */\n" + 
		"	BasicTestTextIns(int xxx, float real, Class clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "#BasicTestTextIns(int, float, Class)");
	assertSortedResults("");
	}
}
