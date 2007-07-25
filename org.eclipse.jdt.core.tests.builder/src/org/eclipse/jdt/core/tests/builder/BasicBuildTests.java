/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.core.JavaModelManager;

/**
 * Basic tests of the image builder.
 */
public class BasicBuildTests extends BuilderTests {
	public BasicBuildTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		return buildTestSuite(BasicBuildTests.class);
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
		expectingOnlySpecificProblemFor(pathToA, new Problem("A", "todo nothing", pathToA, 14, 26, -1, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		
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
	
	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=98667
	 */
	public void test98667() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		
		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		
		env.addClass(root, "p1", "Aaa$Bbb$Ccc", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n" + //$NON-NLS-1$ 
			"\n" +  //$NON-NLS-1$
			"public class Aaa$Bbb$Ccc {\n" + //$NON-NLS-1$ 
			"}" //$NON-NLS-1$
		);
			
		fullBuild(projectPath);
		expectingNoProblems();
	}

	/**
	 * @bug 164707: ArrayIndexOutOfBoundsException in JavaModelManager if source level == 6.0
	 * @test Ensure that AIIOB does not longer happen with invalid source level string
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=164707"
	 */
	public void testBug164707() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		IJavaProject javaProject = env.getJavaProject(projectPath); 
		javaProject.setOption(JavaCore.COMPILER_SOURCE, "invalid");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);
		expectingNoProblems();
	}

	/**
	 * @bug 75471: [prefs] no re-compile when loading settings
	 * @test Ensure that changing project preferences is well taking into account while rebuilding project
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=75471"
	 */
	public void _testUpdateProjectPreferences() throws JavaModelException {
		
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

		IPath cuPath = env.addClass(root, "p", "Test", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;\n" + 
			"import util.MyException;\n" + 
			"public class Test {\n" + 
			"}"
		);

		fullBuild(projectPath);
		expectingSpecificProblemFor(
			projectPath,
			new Problem("", "The import util.MyException is never used", cuPath, 18, 34, CategorizedProblem.CAT_UNNECESSARY_CODE, IMarker.SEVERITY_WARNING)); //$NON-NLS-1$ //$NON-NLS-2$

		IJavaProject project = env.getJavaProject(projectPath);
		project.setOption(JavaCore.COMPILER_PB_UNUSED_IMPORT, JavaCore.IGNORE);
		incrementalBuild(projectPath);
		expectingNoProblems();
	}
	public void _testUpdateWkspPreferences() throws JavaModelException {
		
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

		IPath cuPath = env.addClass(root, "p", "Test", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;\n" + 
			"import util.MyException;\n" + 
			"public class Test {\n" + 
			"}"
		);

		fullBuild();
		expectingSpecificProblemFor(
			projectPath,
			new Problem("", "The import util.MyException is never used", cuPath, 18, 34, CategorizedProblem.CAT_UNNECESSARY_CODE, IMarker.SEVERITY_WARNING)); //$NON-NLS-1$ //$NON-NLS-2$

		// Save preference
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		IEclipsePreferences preferences = manager.getInstancePreferences();
		String unusedImport = preferences.get(JavaCore.COMPILER_PB_UNUSED_IMPORT, null);
		try {
			// Modify preference
			preferences.put(JavaCore.COMPILER_PB_UNUSED_IMPORT, JavaCore.IGNORE);
			incrementalBuild();
			expectingNoProblems();
		}
		finally {
			if (unusedImport == null) {
				preferences.remove(JavaCore.COMPILER_PB_UNUSED_IMPORT);
			} else {
				preferences.put(JavaCore.COMPILER_PB_UNUSED_IMPORT, unusedImport);
			}
		}
	}

	// TODO (Olivier) DISABLED: The test pas while running the test suite alone but fails while running all builder tests
	// It seems that the order of markers returned by ENV.getTaskMarkersFor(IPath) is not always the same...
	public void _testTags4() throws JavaModelException {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.COMPILER_TASK_TAGS, "TODO!,TODO,TODO?"); //$NON-NLS-1$
		newOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "HIGH,NORMAL,LOW"); //$NON-NLS-1$
		
		JavaCore.setOptions(newOptions);
		
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
	
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
	
		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
	
		IPath pathToA = env.addClass(root, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"// TODO! TODO? need to review the loop\n" + //$NON-NLS-1$
			"public class A {\n" + //$NON-NLS-1$
			"}");
	
		fullBuild(projectPath);
		IMarker[] markers = env.getTaskMarkersFor(pathToA);
		assertEquals("Wrong size", 2, markers.length);
	
		try {
			IMarker marker = markers[1];
			Object priority = marker.getAttribute(IMarker.PRIORITY);
			String message = (String) marker.getAttribute(IMarker.MESSAGE);
			assertEquals("Wrong message", "TODO? need to review the loop", message);
			assertNotNull("No task priority", priority);
			assertEquals("Wrong priority", new Integer(IMarker.PRIORITY_LOW), priority);
	
			marker = markers[0];
			priority = marker.getAttribute(IMarker.PRIORITY);
			message = (String) marker.getAttribute(IMarker.MESSAGE);
			assertEquals("Wrong message", "TODO! need to review the loop", message);
			assertNotNull("No task priority", priority);
			assertEquals("Wrong priority", new Integer(IMarker.PRIORITY_HIGH), priority);
		} catch (CoreException e) {
			assertTrue(false);
		}
		JavaCore.setOptions(options);
	}
	
}
