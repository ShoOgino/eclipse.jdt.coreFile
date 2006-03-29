/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.tests.util.Util;
public class ExistenceTests extends ModifyingResourceTests {
public ExistenceTests(String name) {
	super(name);
}

public static Test suite() {
	return buildModelTestSuite(ExistenceTests.class);
}
protected void assertCorrespondingResourceFails(IJavaElement element) {
	boolean gotException = false;
	try {
		element.getCorrespondingResource();
	} catch (JavaModelException e) {
		if (e.isDoesNotExist()) {
			gotException = true;
		}
	}
	assertTrue("Should not be able to get corresponding resource", gotException);
}
protected void assertOpenFails(String expectedMessage, IOpenable openable) {
	String message = "";
	try {
		openable.open(null);
	} catch (JavaModelException e) {
		message = e.getMessage();
	}
	if (!expectedMessage.equals(message)) {
		System.out.print(Util.displayString(message, 3));
		System.out.println(",");
	}
	assertEquals(expectedMessage, message);
}
protected void assertUnderlyingResourceFails(IJavaElement element) {
	boolean gotException = false;
	try {
		element.getUnderlyingResource();
	} catch (JavaModelException e) {
		if (e.isDoesNotExist()) {
			gotException = true;
		}
	}
	assertTrue("Should not be able to get underlying resource", gotException);
}
public void testClassFileInBinary() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {"src"}, "bin");
		this.createFile("P/bin/X.class", "");
		IClassFile classFile = this.getClassFile("P/bin/X.class");
		assertTrue(!classFile.exists());
	} finally {
		this.deleteProject("P");
	}
}
public void testClassFileInLibrary() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {}, new String[] {"lib"}, "bin");
		this.createFile("P/lib/X.class", "");
		IClassFile classFile = this.getClassFile("P/lib/X.class");
		assertTrue(classFile.exists());
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensure that an IClassFile handle created on a .class file in a source folder
 * doesn't not exist.
 * (regression test for bug 36499 exists() returns true for a source file inside a classfolder)
 */
public void testClassFileInSource1() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {"src"}, "bin");
		this.createFile("P/src/X.class", "");
		IClassFile classFile = this.getClassFile("P/src/X.class");
		assertTrue("Class file should not exist", !classFile.exists()); 
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensure that an IClassFile handle created on a .class file in a source folder
 * cannot be opened.
 * (regression test for bug 36499 exists() returns true for a source file inside a classfolder)
 */
 public void testClassFileInSource2() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {"src"}, "bin");
		this.createFile("P/src/X.class", "");
		IClassFile classFile = this.getClassFile("P/src/X.class");
		assertOpenFails(
			"Operation not supported for specified element type(s):src [in P]", 
			classFile);
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensure that an ICompilationUnit handle created on a .java file in a library folder
 * doesn't not exist.
 * (regression test for bug 36499 exists() returns true for a source file inside a classfolder)
 */
public void testCompilationUnitInLibrary1() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {}, new String[] {"lib"},  "bin");
		this.createFile(
			"P/lib/X.java", 
			"public class X {}"
		);
		ICompilationUnit cu = this.getCompilationUnit("P/lib/X.java");
		assertTrue("Ccompilation unit should not exist", !cu.exists()); 
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensure that an ICompilationUnit handle created on a .java file in a library folder
 *cannot be opened.
 * (regression test for bug 36499 exists() returns true for a source file inside a classfolder)
 */
public void testCompilationUnitInLibrary2() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {}, new String[] {"lib"},  "bin");
		this.createFile(
			"P/lib/X.java", 
			"public class X {}"
		);
		ICompilationUnit cu = this.getCompilationUnit("P/lib/X.java");
		assertOpenFails(
			"Operation not supported for specified element type(s):lib [in P]",
			cu);
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensure that a method with invalid parameters returns false to exists().
 * (regression test for bug 80338 getReturnType() throws a NullArgumentException)
 */
public void testMethodWithInvalidParameter() throws CoreException {
	try {
		createJavaProject("P");
		createFile(
			"P/X.java", 
			"public class X {}"
		);
		IMethod method = getCompilationUnit("P/X.java").getType("X").getMethod("foo", new String[] {"~12345@"});
		assertTrue("Methodr should not exist", !method.exists()); 
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensure that a non-existing class file cannot be opened.
 */
public void testNonExistingClassFile1() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {"src"}, new String[] {"lib"}, "bin");
		IClassFile classFile = getClassFile("/P/lib/X.class");
		assertOpenFails(
			"X.class [in <default> [in lib [in P]]] does not exist", 
			classFile);
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensure that a non-existing class file cannot be opened.
 * (regression test for 52379 JavaElement.getElementInfo no longer works)
 */
public void testNonExistingClassFile2() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {"src"}, new String[] {}, "bin");
		IClassFile classFile = getClassFile("/P/lib/X.class");
		assertOpenFails(
			"lib [in P] is not on its project\'s build path",
			classFile);
	} finally {
		this.deleteProject("P");
	}
}

/*
 * Ensure that a non-existing compilation unit cannot be opened.
 */
public void testNonExistingCompilationUnit() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {"src"}, "bin");
		ICompilationUnit cu = getCompilationUnit("/P/src/X.java");
		assertOpenFails(
			"X.java [in <default> [in src [in P]]] does not exist", 
			cu);
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensure that a non-existing package fragment cannot be opened.
 */
public void testNonExistingPackageFragment1() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {"src"}, "bin");
		IPackageFragment pkg = this.getPackage("/P/src/x");
		assertOpenFails(
			"x [in src [in P]] does not exist", 
			pkg);
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensure that a non-existing package fragment cannot be opened.
 */
public void testNonExistingPackageFragment2() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {}, "bin");
		IFolder folder = createFolder("/P/src/x");
		IPackageFragment pkg = project.getPackageFragmentRoot(folder).getPackageFragment("x");
		assertOpenFails(
			"src/x [in P] is not on its project\'s build path",
			pkg);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensure that a non-Java project doesn't exist.
 * (regression test for bug 28545 JavaProject.exists() returns true if project doesn't have Java nature)
 */
public void testNonJavaProject() throws CoreException {
	try {
		createProject("P");
		IProject project = getProject("P");
		IJavaProject javaProject = JavaCore.create(project);
		
		assertTrue("Simple project should not exist", !javaProject.exists());
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a package fragment root that is not on the classpath cannot be opened.
 */
public void testPkgFragmentRootNotInClasspath() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src"}, "bin");
		IFolder folder = createFolder("/P/otherRoot");
		IPackageFragmentRoot root = project.getPackageFragmentRoot(folder);
		assertTrue("Root should not exist", !root.exists());
		assertOpenFails(
			"otherRoot [in P] is not on its project\'s build path",
			root);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensure that an ITypeParameter exists if it exists in source.
 */
public void testTypeParameter1() throws CoreException {
	try {
		createJavaProject("P");
		createFile(
			"P/X.java", 
			"public class X<T> {}"
		);
		ITypeParameter typeParameter = getCompilationUnit("P/X.java").getType("X").getTypeParameter("T");
		assertTrue("Type parameter should exist", typeParameter.exists()); 
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensure that an ITypeParameter exists if it exists in source.
 */
public void testTypeParameter2() throws CoreException {
	try {
		createJavaProject("P");
		createFile(
			"P/X.java", 
			"public class X {\n" +
			"  <T extends String> void foo() {}\n" +
			"}"
		);
		ITypeParameter typeParameter = getCompilationUnit("P/X.java").getType("X").getMethod("foo", new String[0]).getTypeParameter("T");
		assertTrue("Type parameter should exist", typeParameter.exists()); 
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensure that an ITypeParameter doesn't exist if it doesn't exist in source.
 */
public void testTypeParameter3() throws CoreException {
	try {
		createJavaProject("P");
		createFile(
			"P/X.java", 
			"public class X<T> {}"
		);
		ITypeParameter typeParameter = getCompilationUnit("P/X.java").getType("X").getTypeParameter("U");
		assertTrue("Type parameter should not exist", !typeParameter.exists()); 
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensure that an ITypeParameter doesn't exist if it doesn't exist in source.
 */
public void testTypeParameter4() throws CoreException {
	try {
		createJavaProject("P");
		createFile(
			"P/X.java", 
			"public class X {\n" +
			"  <T extends String> void foo() {}\n" +
			"}"
		);
		ITypeParameter typeParameter = getCompilationUnit("P/X.java").getType("X").getMethod("foo", new String[0]).getTypeParameter("String");
		assertTrue("Type parameter should not exist", !typeParameter.exists()); 
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensure that an ITypeParameter doesn't exist even if a member class with the same name exists in source.
 * (regression test for bug 73255 [1.5][reconciling] ClassCastException in SourceTypeElementInfo#getTypeParameterBounds)
 */
public void testTypeParameter5() throws CoreException {
	try {
		createJavaProject("P");
		createFile(
			"P/X.java", 
			"public class X {\n" +
			"  class T {}\n" +
			"}"
		);
		ITypeParameter typeParameter = getCompilationUnit("P/X.java").getType("X").getTypeParameter("T");
		assertTrue("Type parameter should not exist", !typeParameter.exists()); 
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that one cannot get the corresponding resource of a non-existing class file.
 */
public void testCorrespondingResourceNonExistingClassFile() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[] {"lib"}, "bin");
		IClassFile classFile = getClassFile("/P/lib/X.class");
		assertCorrespondingResourceFails(classFile);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that one cannot get the corresponding resource of a non-existing compilation unit.
 */
public void testCorrespondingResourceNonExistingCompilationUnit() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, "bin");
		ICompilationUnit compilationUnit = getCompilationUnit("/P/src/X.java");
		assertCorrespondingResourceFails(compilationUnit);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that one cannot get the corresponding resource of a non-existing jar package fragment root.
 */
public void testCorrespondingResourceNonExistingJarPkgFragmentRoot() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src"}, "bin");
		IPackageFragmentRoot root = project.getPackageFragmentRoot("/nonExisting.jar");
		assertCorrespondingResourceFails(root);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that one cannot get the corresponding resource of a non-existing package fragment.
 */
public void testCorrespondingResourceNonExistingPkgFragment() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, "bin");
		IPackageFragment pkg = getPackage("/P/src/nonExisting");
		assertCorrespondingResourceFails(pkg);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that one cannot get the corresponding resource of a non-existing package fragment root.
 */
public void testCorrespondingResourceNonExistingPkgFragmentRoot() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src"}, "bin");
		IFolder folder = createFolder("/P/nonExistingRoot");
		IPackageFragmentRoot root = project.getPackageFragmentRoot(folder);
		assertCorrespondingResourceFails(root);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that one cannot get the corresponding resource of a non-existing java project.
 */
public void testCorrespondingResourceNonExistingProject() {
	IProject nonExistingProject = ResourcesPlugin.getWorkspace().getRoot().getProject("NonExisting");
	IJavaProject javaProject = JavaCore.create(nonExistingProject);
	assertCorrespondingResourceFails(javaProject);
}
/*
 * Ensures that one cannot get the corresponding resource of a non-existing type.
 */
public void testCorrespondingResourceNonExistingType() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, "bin");
		createFile(
			"/P/src/X.java",
			"public class X{\n" +
			"}"
		);
		IType type = getCompilationUnit("/P/src/X.java").getType("NonExisting");
		assertCorrespondingResourceFails(type);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that one cannot get the underlying resource of a non-existing class file.
 */
public void testUnderlyingResourceNonExistingClassFile() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[] {"lib"}, "bin");
		IClassFile classFile = getClassFile("/P/lib/X.class");
		assertUnderlyingResourceFails(classFile);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that one cannot get the underlying resource of a non-existing compilation unit.
 */
public void testUnderlyingResourceNonExistingCompilationUnit() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, "bin");
		ICompilationUnit compilationUnit = getCompilationUnit("/P/src/X.java");
		assertUnderlyingResourceFails(compilationUnit);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that one cannot get the underlying resource of a non-existing jar package fragment root.
 */
public void testUnderlyingResourceNonExistingJarPkgFragmentRoot() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src"}, "bin");
		IPackageFragmentRoot root = project.getPackageFragmentRoot("/nonExisting.jar");
		assertUnderlyingResourceFails(root);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that one cannot get the underlying resource of a non-existing package fragment.
 */
public void testUnderlyingResourceNonExistingPkgFragment() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, "bin");
		IPackageFragment pkg = getPackage("/P/src/nonExisting");
		assertUnderlyingResourceFails(pkg);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that one cannot get the underlying resource of a non-existing package fragment root.
 */
public void testUnderlyingResourceNonExistingPkgFragmentRoot() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src"}, "bin");
		IFolder folder = createFolder("/P/nonExistingRoot");
		IPackageFragmentRoot root = project.getPackageFragmentRoot(folder);
		assertUnderlyingResourceFails(root);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that one cannot get the underlying resource of a non-existing java project.
 */
public void testUnderlyingResourceNonExistingProject() {
	IProject nonExistingProject = ResourcesPlugin.getWorkspace().getRoot().getProject("NonExisting");
	IJavaProject javaProject = JavaCore.create(nonExistingProject);
	assertUnderlyingResourceFails(javaProject);
}
/*
 * Ensures that one cannot get the underlying resource of a non-existing type.
 */
public void testUnderlyingResourceNonExistingType() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, "bin");
		createFile(
			"/P/src/X.java",
			"public class X{\n" +
			"}"
		);
		IType type = getCompilationUnit("/P/src/X.java").getType("NonExisting");
		assertUnderlyingResourceFails(type);
	} finally {
		deleteProject("P");
	}
}
}
