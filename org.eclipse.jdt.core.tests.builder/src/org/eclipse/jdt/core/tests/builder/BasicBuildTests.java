/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;

import junit.framework.*;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.Util;

/**
 * Basic tests of the image builder.
 */
public class BasicBuildTests extends Tests {
	public BasicBuildTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		if (false) {
			TestSuite suite = new TestSuite(BasicBuildTests.class.getName());
			suite.addTest(new BasicBuildTests("testTags3"));
			return suite;
		}
		return new TestSuite(BasicBuildTests.class);
	}
	
	public void testBuild() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);
		
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		
		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		
		env.addClass(root, "p1", "Hello", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class Hello {\n"+ //$NON-NLS-1$
			"   public static void main(String args[]) {\n"+ //$NON-NLS-1$
			"      System.out.println(\"Hello world\");\n"+ //$NON-NLS-1$
			"   }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
		incrementalBuild(projectPath);
	}

	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=23894
	 */
	public void testToDoMarker() throws JavaModelException {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.COMPILER_TASK_TAGS, "todo"); //$NON-NLS-1$
		
		JavaCore.setOptions(newOptions);
		
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		IPath pathToA = env.addClass(root, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"//todo nothing\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"}"); //$NON-NLS-1$

		fullBuild(projectPath);
		expectingOnlySpecificProblemFor(pathToA, new Problem("A", "todo nothing", pathToA, 14, 26, -1)); //$NON-NLS-1$ //$NON-NLS-2$
		
		JavaCore.setOptions(options);
	}

	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=91426
	 */
	public void testToDoMarker2() throws JavaModelException {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.COMPILER_TASK_TAGS, "TODO,FIXME,XXX"); //$NON-NLS-1$
		newOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "NORMAL,HIGH,LOW"); //$NON-NLS-1$
		
		JavaCore.setOptions(newOptions);
		
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		IPath pathToA = env.addClass(root, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"//TODO normal\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	public void foo() {\n"+ //$NON-NLS-1$
			"		//FIXME high\n"+ //$NON-NLS-1$
			"	}\n"+ //$NON-NLS-1$
			"	public void foo2() {\n"+ //$NON-NLS-1$
			"		//XXX low\n"+ //$NON-NLS-1$
			"	}\n"+ //$NON-NLS-1$
			"}"); //$NON-NLS-1$

		fullBuild(projectPath);
		IMarker[] markers = env.getTaskMarkersFor(pathToA);
		assertEquals("Wrong size", 3, markers.length);
		Arrays.sort(markers, new Comparator() {
			public int compare(Object o1, Object o2) {
				IMarker marker1 = (IMarker) o1;
				IMarker marker2 = (IMarker) o2;
				try {
					final int start1 = ((Integer) marker1.getAttribute(IMarker.CHAR_START)).intValue();
					final int start2 = ((Integer) marker2.getAttribute(IMarker.CHAR_START)).intValue();
					return start1 - start2;
				} catch (CoreException e) {
					return 0;
				}
			}
		});
		
		try {
			IMarker marker = markers[0];
			Object priority = marker.getAttribute(IMarker.PRIORITY);
			String message = (String) marker.getAttribute(IMarker.MESSAGE);
			assertTrue("Wrong message", message.startsWith("TODO "));
			assertNotNull("No task priority", priority);
			assertEquals("Wrong priority", new Integer(IMarker.PRIORITY_NORMAL), priority);

			marker = markers[1];
			priority = marker.getAttribute(IMarker.PRIORITY);
			message = (String) marker.getAttribute(IMarker.MESSAGE);
			assertTrue("Wrong message", message.startsWith("FIXME "));
			assertNotNull("No task priority", priority);
			assertEquals("Wrong priority", new Integer(IMarker.PRIORITY_HIGH), priority);

			marker = markers[2];
			priority = marker.getAttribute(IMarker.PRIORITY);
			message = (String) marker.getAttribute(IMarker.MESSAGE);
			assertTrue("Wrong message", message.startsWith("XXX "));
			assertNotNull("No task priority", priority);
			assertEquals("Wrong priority", new Integer(IMarker.PRIORITY_LOW), priority);
		} catch (CoreException e) {
			assertTrue(false);
		}
		JavaCore.setOptions(options);
	}
	
	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=110797
	 */
	public void testTags() throws JavaModelException {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.COMPILER_TASK_TAGS, "TODO,FIXME,XXX"); //$NON-NLS-1$
		newOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "NORMAL,HIGH,LOW"); //$NON-NLS-1$
		
		JavaCore.setOptions(newOptions);
		
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		IPath pathToA = env.addClass(root, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"// TODO FIXME need to review the loop TODO should be done\n" + //$NON-NLS-1$
			"public class A {\n" + //$NON-NLS-1$
			"}");

		fullBuild(projectPath);
		IMarker[] markers = env.getTaskMarkersFor(pathToA);
		assertEquals("Wrong size", 3, markers.length);
		Arrays.sort(markers, new Comparator() {
			public int compare(Object o1, Object o2) {
				IMarker marker1 = (IMarker) o1;
				IMarker marker2 = (IMarker) o2;
				try {
					final int start1 = ((Integer) marker1.getAttribute(IMarker.CHAR_START)).intValue();
					final int start2 = ((Integer) marker2.getAttribute(IMarker.CHAR_START)).intValue();
					return start1 - start2;
				} catch (CoreException e) {
					return 0;
				}
			}
		});

		try {
			IMarker marker = markers[2];
			Object priority = marker.getAttribute(IMarker.PRIORITY);
			String message = (String) marker.getAttribute(IMarker.MESSAGE);
			assertEquals("Wrong message", "TODO should be done", message);
			assertNotNull("No task priority", priority);
			assertEquals("Wrong priority", new Integer(IMarker.PRIORITY_NORMAL), priority);

			marker = markers[1];
			priority = marker.getAttribute(IMarker.PRIORITY);
			message = (String) marker.getAttribute(IMarker.MESSAGE);
			assertEquals("Wrong message", "FIXME need to review the loop", message);
			assertNotNull("No task priority", priority);
			assertEquals("Wrong priority", new Integer(IMarker.PRIORITY_HIGH), priority);

			marker = markers[0];
			priority = marker.getAttribute(IMarker.PRIORITY);
			message = (String) marker.getAttribute(IMarker.MESSAGE);
			assertEquals("Wrong message", "TODO need to review the loop", message);
			assertNotNull("No task priority", priority);
			assertEquals("Wrong priority", new Integer(IMarker.PRIORITY_NORMAL), priority);
		} catch (CoreException e) {
			assertTrue(false);
		}
		JavaCore.setOptions(options);
	}

	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=110797
	 */
	public void testTags2() throws JavaModelException {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.COMPILER_TASK_TAGS, "TODO,FIXME,XXX"); //$NON-NLS-1$
		newOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "NORMAL,HIGH,LOW"); //$NON-NLS-1$
		
		JavaCore.setOptions(newOptions);
		
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		IPath pathToA = env.addClass(root, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"// TODO TODO need to review the loop\n" + //$NON-NLS-1$
			"public class A {\n" + //$NON-NLS-1$
			"}");

		fullBuild(projectPath);
		IMarker[] markers = env.getTaskMarkersFor(pathToA);
		assertEquals("Wrong size", 2, markers.length);
		Arrays.sort(markers, new Comparator() {
			public int compare(Object o1, Object o2) {
				IMarker marker1 = (IMarker) o1;
				IMarker marker2 = (IMarker) o2;
				try {
					final int start1 = ((Integer) marker1.getAttribute(IMarker.CHAR_START)).intValue();
					final int start2 = ((Integer) marker2.getAttribute(IMarker.CHAR_START)).intValue();
					return start1 - start2;
				} catch (CoreException e) {
					return 0;
				}
			}
		});

		try {
			IMarker marker = markers[1];
			Object priority = marker.getAttribute(IMarker.PRIORITY);
			String message = (String) marker.getAttribute(IMarker.MESSAGE);
			assertEquals("Wrong message", "TODO need to review the loop", message);
			assertNotNull("No task priority", priority);
			assertEquals("Wrong priority", new Integer(IMarker.PRIORITY_NORMAL), priority);

			marker = markers[0];
			priority = marker.getAttribute(IMarker.PRIORITY);
			message = (String) marker.getAttribute(IMarker.MESSAGE);
			assertEquals("Wrong message", "TODO need to review the loop", message);
			assertNotNull("No task priority", priority);
			assertEquals("Wrong priority", new Integer(IMarker.PRIORITY_NORMAL), priority);
		} catch (CoreException e) {
			assertTrue(false);
		}
		JavaCore.setOptions(options);
	}
	
	/*
	 * Ensures that a task tag is not user editable
	 * (regression test for bug 123721 two types of 'remove' for TODO task tags)
	 */
	public void testTags3() throws CoreException {
		Hashtable options = JavaCore.getOptions();
		
		try {
			Hashtable newOptions = JavaCore.getOptions();
			newOptions.put(JavaCore.COMPILER_TASK_TAGS, "TODO,FIXME,XXX"); //$NON-NLS-1$
			newOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "NORMAL,HIGH,LOW"); //$NON-NLS-1$
			
			JavaCore.setOptions(newOptions);
			
			IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
			env.addExternalJars(projectPath, Util.getJavaClassLibs());
	
			// remove old package fragment root so that names don't collide
			env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
	
			IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
			env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
	
			IPath pathToA = env.addClass(root, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
				"package p; \n"+ //$NON-NLS-1$
				"// TODO need to review\n" + //$NON-NLS-1$
				"public class A {\n" + //$NON-NLS-1$
				"}");
	
			fullBuild(projectPath);
			IMarker[] markers = env.getTaskMarkersFor(pathToA);
			assertEquals("Marker should not be editable", Boolean.FALSE, markers[0].getAttribute(IMarker.USER_EDITABLE));
		} finally {
			JavaCore.setOptions(options);
		}
	}
	
	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=92821
	 */
	public void testUnusedImport() throws JavaModelException {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.COMPILER_PB_UNUSED_IMPORT, JavaCore.WARNING);
		
		JavaCore.setOptions(newOptions);
		
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "util", "MyException", //$NON-NLS-1$ //$NON-NLS-2$
			"package util;\n" + 
			"public class MyException extends Exception {\n" + 
			"	private static final long serialVersionUID = 1L;\n" +
			"}"
		); //$NON-NLS-1$

		env.addClass(root, "p", "Test", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;\n" + 
			"import util.MyException;\n" + 
			"public class Test {\n" + 
			"	/**\n" + 
			"	 * @throws MyException\n" + 
			"	 */\n" + 
			"	public void bar() {\n" + 
			"	}\n" + 
			"}"
		);

		fullBuild(projectPath);
		expectingNoProblems();
		
		JavaCore.setOptions(options);
	}
}
