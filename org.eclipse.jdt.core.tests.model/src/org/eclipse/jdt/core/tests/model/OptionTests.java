/*******************************************************************************
 * Copyright (c) 2002 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.Hashtable;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class OptionTests extends ModifyingResourceTests {

public OptionTests(String name) {
	super(name);
}
public static Test suite() {

	if (false){
		TestSuite suite = new Suite(OptionTests.class.getName());
		suite.addTest(new ClasspathTests("testDenseCycleDetection"));
		return suite;
	}
	return new Suite(OptionTests.class);	
}
/**
 * Test persistence of project custom options
 */
public void test01() throws CoreException {
	try {
		IJavaProject projectA = 
			this.createJavaProject(
				"A", 
				new String[] {}, // source folders
				new String[] {}, // lib folders
				new String[] {}, // projects
				"");
		IJavaProject projectB = 
			this.createJavaProject(
				"B", 
				new String[] {}, // source folders
				new String[] {}, // lib folders
				new String[] {}, // projects
				"");
				
		Hashtable options = new Hashtable();
		options.put(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE, JavaCore.DISABLED);
		options.put(JavaCore.COMPILER_COMPLIANCE, "8.0");
		options.put(JavaCore.COMPILER_PB_HIDDEN_CATCH_BLOCK, JavaCore.ERROR);
		JavaCore.setOptions(options);

		options.clear();
		options.put(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_COMPLIANCE, "10.0");
		projectA.setOptions(options);

		// check project A custom options		
		assertEquals("projA:unexpected custom value for deprecation option", JavaCore.ENABLED, projectA.getOption(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE, true));
		assertEquals("projA:unexpected custom value for compliance option", "10.0", projectA.getOption(JavaCore.COMPILER_COMPLIANCE, true));
		assertEquals("projA:unexpected inherited value1 for hidden-catch option", JavaCore.ERROR, projectA.getOption(JavaCore.COMPILER_PB_HIDDEN_CATCH_BLOCK, true));
		
		// check project B custom options	(should be none, indicating it sees global ones only)
		assertEquals("projB:unexpected custom value for deprecation option", JavaCore.DISABLED, projectB.getOption(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE, true));
		assertEquals("projB:unexpected custom value for compliance option", "8.0", projectB.getOption(JavaCore.COMPILER_COMPLIANCE, true));
		assertEquals("projB:unexpected inherited value for hidden-catch option", JavaCore.ERROR, projectB.getOption(JavaCore.COMPILER_PB_HIDDEN_CATCH_BLOCK, true));

		// flush custom options - project A should revert to global ones
		projectA.setOptions(null); 
		assertEquals("projA:unexpected reverted value for deprecation option", JavaCore.DISABLED, projectA.getOption(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE, true));
		assertEquals("projA:unexpected reverted value for compliance option", "8.0", projectA.getOption(JavaCore.COMPILER_COMPLIANCE, true));
		assertEquals("projA:unexpected inherited value2 for hidden-catch option", JavaCore.ERROR, projectA.getOption(JavaCore.COMPILER_PB_HIDDEN_CATCH_BLOCK, true));

	} finally {
		this.deleteProject("A");
		this.deleteProject("B");
	}
}

/**
 * Test custom encoding
 */
public void test02() throws CoreException {
	try {
		IJavaProject projectA = 
			this.createJavaProject(
				"A", 
				new String[] {}, // source folders
				new String[] {}, // lib folders
				new String[] {}, // projects
				"");
		IJavaProject projectB = 
			this.createJavaProject(
				"B", 
				new String[] {}, // source folders
				new String[] {}, // lib folders
				new String[] {}, // projects
				"");
				
		String globalEncoding = JavaCore.getOption(JavaCore.CORE_ENCODING);

		Hashtable options = new Hashtable();
		options.put(JavaCore.CORE_ENCODING, "custom");
		projectA.setOptions(options);

		// check project A custom options		
		assertEquals("projA:unexpected custom encoding", "custom", projectA.getOption(JavaCore.CORE_ENCODING, true));
		
		// check project B custom options	(should be none, indicating it sees global ones only)
		assertEquals("projB:unexpected custom encoding", globalEncoding, projectB.getOption(JavaCore.CORE_ENCODING, true));

		// flush custom options - project A should revert to global ones
		projectA.setOptions(null); 
		assertEquals("projA:unexpected reverted encoding", globalEncoding, projectA.getOption(JavaCore.CORE_ENCODING, true));

	} finally {
		this.deleteProject("A");
		this.deleteProject("B");
	}
}

/**
 * Test custom project option (if not considering JavaCore options)
 */
public void test03() throws CoreException {
	try {
		IJavaProject projectA = 
			this.createJavaProject(
				"A", 
				new String[] {}, // source folders
				new String[] {}, // lib folders
				new String[] {}, // projects
				"");
		IJavaProject projectB = 
			this.createJavaProject(
				"B", 
				new String[] {}, // source folders
				new String[] {}, // lib folders
				new String[] {}, // projects
				"");
				
		Hashtable options = new Hashtable();
		options.put(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE, JavaCore.DISABLED);
		options.put(JavaCore.COMPILER_COMPLIANCE, "8.0");
		options.put(JavaCore.COMPILER_PB_HIDDEN_CATCH_BLOCK, JavaCore.ERROR);
		JavaCore.setOptions(options);

		options.clear();
		options.put(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_COMPLIANCE, "10.0");
		projectA.setOptions(options);

		// check project A custom options		
		assertEquals("projA:unexpected custom value for deprecation option", JavaCore.ENABLED, projectA.getOption(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE, false));
		assertEquals("projA:unexpected custom value for compliance option", "10.0", projectA.getOption(JavaCore.COMPILER_COMPLIANCE, false));
		assertEquals("projA:unexpected inherited value1 for hidden-catch option", null, projectA.getOption(JavaCore.COMPILER_PB_HIDDEN_CATCH_BLOCK, false));
		
		// check project B custom options	(should be none, indicating it sees global ones only)
		assertEquals("projB:unexpected custom value for deprecation option", null, projectB.getOption(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE, false));
		assertEquals("projB:unexpected custom value for compliance option", null, projectB.getOption(JavaCore.COMPILER_COMPLIANCE, false));
		assertEquals("projB:unexpected inherited value for hidden-catch option", null, projectB.getOption(JavaCore.COMPILER_PB_HIDDEN_CATCH_BLOCK, false));

		// flush custom options - project A should revert to global ones
		projectA.setOptions(null); 
		assertEquals("projA:unexpected reverted value for deprecation option", null, projectA.getOption(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE, false));
		assertEquals("projA:unexpected reverted value for compliance option", null, projectA.getOption(JavaCore.COMPILER_COMPLIANCE, false));
		assertEquals("projA:unexpected inherited value2 for hidden-catch option", null, projectA.getOption(JavaCore.COMPILER_PB_HIDDEN_CATCH_BLOCK, false));

	} finally {
		this.deleteProject("A");
		this.deleteProject("B");
	}
}
/**
 * Test persistence of project custom options - using getOptions()
 */
public void test04() throws CoreException {
	try {
		IJavaProject projectA = 
			this.createJavaProject(
				"A", 
				new String[] {}, // source folders
				new String[] {}, // lib folders
				new String[] {}, // projects
				"");
		IJavaProject projectB = 
			this.createJavaProject(
				"B", 
				new String[] {}, // source folders
				new String[] {}, // lib folders
				new String[] {}, // projects
				"");
				
		Hashtable options = new Hashtable();
		options.put(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE, JavaCore.DISABLED);
		options.put(JavaCore.COMPILER_COMPLIANCE, "8.0");
		options.put(JavaCore.COMPILER_PB_HIDDEN_CATCH_BLOCK, JavaCore.ERROR);
		JavaCore.setOptions(options);

		options.clear();
		options.put(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_COMPLIANCE, "10.0");
		projectA.setOptions(options);

		// check project A custom options		
		assertEquals("projA:unexpected custom value for deprecation option", JavaCore.ENABLED, projectA.getOptions(true).get(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE));
		assertEquals("projA:unexpected custom value for compliance option", "10.0", projectA.getOptions(true).get(JavaCore.COMPILER_COMPLIANCE));
		assertEquals("projA:unexpected inherited value1 for hidden-catch option", JavaCore.ERROR, projectA.getOptions(true).get(JavaCore.COMPILER_PB_HIDDEN_CATCH_BLOCK));
		
		// check project B custom options	(should be none, indicating it sees global ones only)
		assertEquals("projB:unexpected custom value for deprecation option", JavaCore.DISABLED, projectB.getOptions(true).get(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE));
		assertEquals("projB:unexpected custom value for compliance option", "8.0", projectB.getOptions(true).get(JavaCore.COMPILER_COMPLIANCE));
		assertEquals("projB:unexpected inherited value for hidden-catch option", JavaCore.ERROR, projectB.getOptions(true).get(JavaCore.COMPILER_PB_HIDDEN_CATCH_BLOCK));

		// flush custom options - project A should revert to global ones
		projectA.setOptions(null); 
		assertEquals("projA:unexpected reverted value for deprecation option", JavaCore.DISABLED, projectA.getOptions(true).get(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE));
		assertEquals("projA:unexpected reverted value for compliance option", "8.0", projectA.getOptions(true).get(JavaCore.COMPILER_COMPLIANCE));
		assertEquals("projA:unexpected inherited value2 for hidden-catch option", JavaCore.ERROR, projectA.getOptions(true).get(JavaCore.COMPILER_PB_HIDDEN_CATCH_BLOCK));

	} finally {
		this.deleteProject("A");
		this.deleteProject("B");
	}
}

/**
 * Test custom encoding - using getOptions()
 */
public void test05() throws CoreException {
	try {
		IJavaProject projectA = 
			this.createJavaProject(
				"A", 
				new String[] {}, // source folders
				new String[] {}, // lib folders
				new String[] {}, // projects
				"");
		IJavaProject projectB = 
			this.createJavaProject(
				"B", 
				new String[] {}, // source folders
				new String[] {}, // lib folders
				new String[] {}, // projects
				"");
				
		String globalEncoding = JavaCore.getOption(JavaCore.CORE_ENCODING);

		Hashtable options = new Hashtable();
		options.put(JavaCore.CORE_ENCODING, "custom");
		projectA.setOptions(options);

		// check project A custom options		
		assertEquals("projA:unexpected custom encoding", "custom", projectA.getOptions(true).get(JavaCore.CORE_ENCODING));
		
		// check project B custom options	(should be none, indicating it sees global ones only)
		assertEquals("projB:unexpected custom encoding", globalEncoding, projectB.getOptions(true).get(JavaCore.CORE_ENCODING));

		// flush custom options - project A should revert to global ones
		projectA.setOptions(null); 
		assertEquals("projA:unexpected reverted encoding", globalEncoding, projectA.getOptions(true).get(JavaCore.CORE_ENCODING));

	} finally {
		this.deleteProject("A");
		this.deleteProject("B");
	}
}

/**
 * Test custom project option (if not considering JavaCore options) - using getOptions()
 */
public void test06() throws CoreException {
	try {
		IJavaProject projectA = 
			this.createJavaProject(
				"A", 
				new String[] {}, // source folders
				new String[] {}, // lib folders
				new String[] {}, // projects
				"");
		IJavaProject projectB = 
			this.createJavaProject(
				"B", 
				new String[] {}, // source folders
				new String[] {}, // lib folders
				new String[] {}, // projects
				"");
				
		Hashtable options = new Hashtable();
		options.put(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE, JavaCore.DISABLED);
		options.put(JavaCore.COMPILER_COMPLIANCE, "8.0");
		options.put(JavaCore.COMPILER_PB_HIDDEN_CATCH_BLOCK, JavaCore.ERROR);
		JavaCore.setOptions(options);

		options.clear();
		options.put(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_COMPLIANCE, "10.0");
		projectA.setOptions(options);

		// check project A custom options		
		assertEquals("projA:unexpected custom value for deprecation option", JavaCore.ENABLED, projectA.getOptions(false).get(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE));
		assertEquals("projA:unexpected custom value for compliance option", "10.0", projectA.getOptions(false).get(JavaCore.COMPILER_COMPLIANCE));
		assertEquals("projA:unexpected inherited value1 for hidden-catch option", null, projectA.getOptions(false).get(JavaCore.COMPILER_PB_HIDDEN_CATCH_BLOCK));
		
		// check project B custom options	(should be none, indicating it sees global ones only)
		assertEquals("projB:unexpected custom value for deprecation option", null, projectB.getOptions(false).get(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE));
		assertEquals("projB:unexpected custom value for compliance option", null, projectB.getOptions(false).get(JavaCore.COMPILER_COMPLIANCE));
		assertEquals("projB:unexpected inherited value for hidden-catch option", null, projectB.getOptions(false).get(JavaCore.COMPILER_PB_HIDDEN_CATCH_BLOCK));

		// flush custom options - project A should revert to global ones
		projectA.setOptions(null); 
		assertEquals("projA:unexpected reverted value for deprecation option", null, projectA.getOptions(false).get(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE));
		assertEquals("projA:unexpected reverted value for compliance option", null, projectA.getOptions(false).get(JavaCore.COMPILER_COMPLIANCE));
		assertEquals("projA:unexpected inherited value2 for hidden-catch option", null, projectA.getOptions(false).get(JavaCore.COMPILER_PB_HIDDEN_CATCH_BLOCK));

	} finally {
		this.deleteProject("A");
		this.deleteProject("B");
	}
}
/**
 * Custom options must replace existing ones completely
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26255
 */
public void test07() throws CoreException {
	try {
		IJavaProject projectA = 
			this.createJavaProject(
				"A", 
				new String[] {}, // source folders
				new String[] {}, // lib folders
				new String[] {}, // projects
				"");
				
		Hashtable options = new Hashtable();
		options.put(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_COMPLIANCE, "10.0");
		projectA.setOptions(options);

		// check project A custom options		
		assertEquals("projA:unexpected custom value for deprecation option", JavaCore.ENABLED, projectA.getOptions(false).get(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE));
		assertEquals("projA:unexpected custom value for compliance option", "10.0", projectA.getOptions(false).get(JavaCore.COMPILER_COMPLIANCE));
		assertEquals("projA:unexpected inherited value1 for hidden-catch option", null, projectA.getOptions(false).get(JavaCore.COMPILER_PB_HIDDEN_CATCH_BLOCK));
		
		// change custom options to have one less
		options.clear();
		options.put(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE, JavaCore.ENABLED);
		projectA.setOptions(options);
		assertEquals("projA:unexpected custom value for deprecation option", JavaCore.ENABLED, projectA.getOptions(false).get(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE));
		assertEquals("projA:unexpected custom value for compliance option", null, projectA.getOptions(false).get(JavaCore.COMPILER_COMPLIANCE));
		assertEquals("projA:unexpected inherited value1 for hidden-catch option", null, projectA.getOptions(false).get(JavaCore.COMPILER_PB_HIDDEN_CATCH_BLOCK));

	} finally {
		this.deleteProject("A");
	}
}}
