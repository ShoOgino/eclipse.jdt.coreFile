/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jdt.core.tests.junit.extension.TestCase;

import junit.framework.Test;
import junit.framework.TestSuite;

@SuppressWarnings({"rawtypes", "unchecked"})
public class RunConverterTests extends junit.framework.TestCase {
public RunConverterTests(String name) {
	super(name);
}
public static Class[] getAllTestClasses() {
	return new Class[] {
		ASTConverterTest.class,
		ASTConverterTest2.class,
		ASTConverterBugsTest.class,
		ASTConverterBugsTestJLS3.class,
		ASTConverterJavadocTest.class,
		ASTConverter15Test.class,
		ASTConverter16Test.class,
		ASTConverter17Test.class,
		ASTConverterAST3Test.class,
		ASTConverterTestAST3_2.class,
		ASTConverterBindingsTest.class,
		ASTConverterRecoveryTest.class,
		ASTConverterAST4Test.class,
		ASTConverterAST8Test.class,
		ASTConverterTestAST4_2.class,
		ASTConverterTestAST8_2.class,
		ASTConverterBugsTestJLS4.class,
		ASTConverterBugsTestJLS8.class,
		ASTConverter15JLS4Test.class,
		ASTConverter15JLS8Test.class,
		TypeAnnotationsConverterTest.class,
		ASTConverter18Test.class,
		ASTConverter9Test.class,
		ASTConverter14Test.class,
		ASTConverter_15Test.class,
	};
}
public static Test suite() {
	TestSuite ts = new TestSuite(RunConverterTests.class.getName());

	ConverterTestSetup.TEST_SUITES = new ArrayList(Arrays.asList(getAllTestClasses()));
	// Reset forgotten subsets of tests
	TestCase.TESTS_PREFIX = null;
	TestCase.TESTS_NAMES = null;
	TestCase.TESTS_NUMBERS = null;
	TestCase.TESTS_RANGE = null;
	TestCase.RUN_ONLY_ID = null;

	for (int i = 0, l=ConverterTestSetup.TEST_SUITES.size(); i < l; i++) {
		Class testClass = (Class) ConverterTestSetup.TEST_SUITES.get(i);

		// call the suite() method and add the resulting suite to the suite
		try {
			Method suiteMethod = testClass.getDeclaredMethod("suite", new Class[0]); //$NON-NLS-1$
			Test suite = (Test)suiteMethod.invoke(null, new Object[0]);
			ts.addTest(suite);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}
	return ts;
}
protected void tearDown() throws Exception {
	ConverterTestSetup.PROJECT_SETUP = false;
	super.tearDown();
}
}
