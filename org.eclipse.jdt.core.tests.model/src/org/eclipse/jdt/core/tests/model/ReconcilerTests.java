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


import java.io.IOException;

import junit.framework.Test;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CompilationParticipant;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.compiler.ReconcileContext;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;

public class ReconcilerTests extends ModifyingResourceTests {
	
	protected ICompilationUnit workingCopy;
	protected ProblemRequestor problemRequestor;
	
	/* A problem requestor that auto-cancels on first problem */
	class CancelingProblemRequestor extends ProblemRequestor {
		IProgressMonitor progressMonitor = new IProgressMonitor() {
			boolean isCanceled = false;
			public void beginTask(String name, int totalWork) {}
			public void done() {}
			public void internalWorked(double work) {}
			public boolean isCanceled() {
				return this.isCanceled;
			}
			public void setCanceled(boolean value) {
				this.isCanceled = value;
			}
			public void setTaskName(String name) {}
			public void subTask(String name) {}
			public void worked(int work) {}
		};
	
		boolean isCanceling = false;
		public void acceptProblem(IProblem problem) {
			if (isCanceling) this.progressMonitor.setCanceled(true); // auto-cancel on first problem
			super.acceptProblem(problem);
		}		
	}
	
	class ReconcileParticipant extends CompilationParticipant {
		int astLevel;
		boolean resolveBinding;
		IJavaElementDelta delta;
		org.eclipse.jdt.core.dom.CompilationUnit ast;
		ReconcileParticipant() {
			this(ICompilationUnit.NO_AST, false);
		}
		ReconcileParticipant(int astLevel, boolean resolveBinding) {
			TestCompilationParticipant.PARTICIPANT = this;
			this.astLevel = astLevel;
			this.resolveBinding = resolveBinding;
		}
		public boolean isActive(IJavaProject project) {
			return true;
		}
		public void reconcile(ReconcileContext context) {
			this.delta = context.getDelta();
			try {
				this.ast = context.getAST(this.astLevel, this.resolveBinding);
			} catch (JavaModelException e) {
				assertNull("Unexpected exception", e);
			}
		}
	}
/**
 */
public ReconcilerTests(String name) {
	super(name);
}
// Use this static initializer to specify subset for tests
// All specified tests which do not belong to the class are skipped...
static {
//	JavaModelManager.VERBOSE = true;
//	org.eclipse.jdt.internal.core.search.BasicSearchEngine.VERBOSE = true;
//	TESTS_PREFIX = "testIgnoreIfBetterNonAccessibleRule";
//	TESTS_NAMES = new String[] { "testIgnoreIfBetterNonAccessibleRule1" };
//	TESTS_NUMBERS = new int[] { 118823 };
//	TESTS_RANGE = new int[] { 16, -1 };
}
public static Test suite() {
	return buildTestSuite(ReconcilerTests.class);
}
protected void assertProblems(String message, String expected) {
	assertProblems(message, expected, this.problemRequestor);
}
// Expect no error as soon as indexing is finished
protected void assertNoProblem(char[] source, ICompilationUnit unit) throws InterruptedException, JavaModelException {
	IndexManager indexManager = JavaModelManager.getJavaModelManager().getIndexManager();
	if (this.problemRequestor.problemCount > 0) {
		// If errors then wait for indexes to finish
		while (indexManager.awaitingJobsCount() > 0) {
			Thread.sleep(100);
		}
		// Reconcile again to see if error goes away
		this.problemRequestor.initialize(source);
		unit.getBuffer().setContents(source); // need to set contents again to be sure that following reconcile will be really done
		unit.reconcile(AST.JLS3,
			true, // force problem detection to see errors if any
			null,	// do not use working copy owner to not use working copies in name lookup
			null);
		if (this.problemRequestor.problemCount > 0) {
			assertEquals("Working copy should NOT have any problem!", "", this.problemRequestor.problems.toString());
		}
	}
}
protected void addClasspathEntries(IClasspathEntry[] entries, boolean enableForbiddenReferences) throws JavaModelException {
	IJavaProject project = getJavaProject("Reconciler");
	IClasspathEntry[] oldClasspath = project.getRawClasspath();
	int oldLength = oldClasspath.length;
	int length = entries.length;
	IClasspathEntry[] newClasspath = new IClasspathEntry[oldLength+length];
	System.arraycopy(oldClasspath, 0, newClasspath, 0, oldLength);
	System.arraycopy(entries, 0, newClasspath, oldLength, length);
	project.setRawClasspath(newClasspath, null);
	
	if (enableForbiddenReferences) {
		project.setOption(JavaCore.COMPILER_PB_FORBIDDEN_REFERENCE, JavaCore.ERROR);
	}
}
protected void removeClasspathEntries(IClasspathEntry[] entries) throws JavaModelException {
	IJavaProject project = getJavaProject("Reconciler");
	IClasspathEntry[] oldClasspath = project.getRawClasspath();
	int oldLength = oldClasspath.length;
	int length = entries.length;
	IClasspathEntry[] newClasspath = new IClasspathEntry[oldLength-length];
	System.arraycopy(oldClasspath, 0, newClasspath, 0, oldLength-length);
	project.setRawClasspath(newClasspath, null);
}
/**
 * Setup for the next test.
 */
public void setUp() throws Exception {
	super.setUp();
	this.problemRequestor =  new ProblemRequestor();
	this.workingCopy = getCompilationUnit("Reconciler/src/p1/X.java").getWorkingCopy(new WorkingCopyOwner() {}, this.problemRequestor, null);
	this.problemRequestor.initialize(this.workingCopy.getSource().toCharArray());
	startDeltas();
}
public void setUpSuite() throws Exception {
	super.setUpSuite();

	// Create project with 1.4 compliance
	IJavaProject project14 = createJavaProject("Reconciler", new String[] {"src"}, new String[] {"JCL_LIB"}, "bin");
	createFolder("/Reconciler/src/p1");
	createFolder("/Reconciler/src/p2");
	createFile(
		"/Reconciler/src/p1/X.java", 
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}"
	);
	project14.setOption(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.IGNORE);
	project14.setOption(JavaCore.COMPILER_PB_INVALID_JAVADOC, JavaCore.WARNING);

	// Create project with 1.5 compliance
	IJavaProject project15 = createJavaProject("Reconciler15", new String[] {"src"}, new String[] {"JCL15_LIB"}, "bin", "1.5");
	addLibrary(
		project15, 
		"lib15.jar", 
		"lib15src.zip", 
		new String[] {
			"java/util/List.java",
			"package java.util;\n" +
			"public class List<T> {\n" +
			"}",
			"java/util/Stack.java",
			"package java.util;\n" +
			"public class Stack<T> {\n" +
			"}",
			"java/util/Map.java",
			"package java.util;\n" +
			"public interface Map<K,V> {\n" +
			"}",
			"java/lang/annotation/Annotation.java",
			"package java.lang.annotation;\n" +
			"public interface Annotation {\n" +
			"}",
			"java/lang/Deprecated.java",
			"package java.lang;\n" +
			"public @interface Deprecated {\n" +
			"}",
			"java/lang/SuppressWarnings.java",
			"package java.lang;\n" +
			"public @interface SuppressWarnings {\n" +
			"   String[] value();\n" +
			"}"
		}, 
		JavaCore.VERSION_1_5
	);
	project15.setOption(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.IGNORE);
}
private void setUp15WorkingCopy() throws JavaModelException {
	setUp15WorkingCopy("Reconciler15/src/p1/X.java", new WorkingCopyOwner() {});
}
private void setUp15WorkingCopy(String path, WorkingCopyOwner owner) throws JavaModelException {
	String contents = this.workingCopy.getSource();
	setUpWorkingCopy(path, contents, owner);
}
private void setUpWorkingCopy(String path, String contents) throws JavaModelException {
	setUpWorkingCopy(path, contents, new WorkingCopyOwner() {});
}
private void setUpWorkingCopy(String path, String contents, WorkingCopyOwner owner) throws JavaModelException {
	this.workingCopy.discardWorkingCopy();
	this.workingCopy = getCompilationUnit(path).getWorkingCopy(owner, this.problemRequestor, null);
	setWorkingCopyContents(contents);
	this.workingCopy.makeConsistent(null);
}
void setWorkingCopyContents(String contents) throws JavaModelException {
	this.workingCopy.getBuffer().setContents(contents);
	this.problemRequestor.initialize(contents.toCharArray());
}
/**
 * Cleanup after the previous test.
 */
public void tearDown() throws Exception {
	TestCompilationParticipant.PARTICIPANT = null;
	if (this.workingCopy != null) {
		this.workingCopy.discardWorkingCopy();
	}
	stopDeltas();
	super.tearDown();
}
public void tearDownSuite() throws Exception {
	deleteProject("Reconciler");
	deleteProject("Reconciler15");
	super.tearDownSuite();
}
/*
 * Ensures that no problem is created for a reference to a type that is included in a prereq project.
 */
public void testAccessRestriction() throws CoreException {
	try {
		createJavaProject("P1", new String[] {"src"}, new String[] {"JCL_LIB"}, null, null, new String[0], null, null, new boolean[0], "bin", null, new String[][] {{"**/X.java"}}, null, "1.4");
		createFolder("/P1/src/p");
		createFile("/P1/src/p/X.java", "package p; public class X {}");
		
		createJavaProject("P2", new String[] {"src"}, new String[] {"JCL_LIB"}, new String[] {"/P1"}, "bin");
		setUpWorkingCopy("/P2/src/Y.java", "public class Y extends p.X {}");
		assertProblems(
			"Unexpected problems", 
			"----------\n" + 
			"----------\n"
		);
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}
/*
 * Ensures that no problem is created for a reference to a binary type that is included in a prereq project.
 * (regression test for bug 82542 Internal error during AST creation)
 */
public void testAccessRestriction2() throws CoreException, IOException {
	try {
		IJavaProject project = createJavaProject("P1");
		addLibrary(
			project,
			"lib.jar",
			"libsrc.zip",
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"}",
			},
			new String[] {
				"**/*"
			},
			null,
			"1.4"
		);
		createJavaProject("P2", new String[] {"src"}, new String[] {"JCL_LIB"}, new String[] {"/P1"}, "bin");
		setUpWorkingCopy("/P2/src/Y.java", "public class Y extends p.X {}");
		assertProblems(
			"Unexpected problems", 
			"----------\n" + 
			"----------\n"
		);
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}
/*
 * Ensures that no problem is created for a reference to a type that is included and not exported in a prereq project
 * but with combineAccessRestriction flag set to false.
 */
public void testAccessRestriction3() throws CoreException {
	try {
		createJavaProject("P1");
		createFolder("/P1/p");
		createFile("/P1/p/X.java", "package p; public class X {}");
		
		createJavaProject("P2", new String[] {}, new String[] {}, null, null, new String[] {"/P1"}, null, null, new boolean[] {true}, "", null, null, null, "1.4");
		
		createJavaProject("P3", new String[] {"src"}, new String[] {"JCL_LIB"}, null, null, new String[] {"/P2"}, null, new String[][] {new String[] {"**/X"}}, false/*don't combine access restrictions*/, new boolean[] {true}, "bin", null, null, null, "1.4");
		setUpWorkingCopy("/P3/src/Y.java", "public class Y extends p.X {}");
		assertProblems(
			"Unexpected problems", 
			"----------\n" + 
			"----------\n"
		);
	} finally {
		deleteProjects(new String[] {"P1", "P2", "P3" });
	}
}
/*
 * Ensures that a problem is created for a reference to a type that is included and not exported in a prereq project
 * but with combineAccessRestriction flag set to true.
 */
public void testAccessRestriction4() throws CoreException {
	try {
		createJavaProject("P1");
		createFolder("/P1/p");
		createFile("/P1/p/X.java", "package p; public class X {}");
		
		createJavaProject("P2", new String[] {}, new String[] {}, null, null, new String[] {"/P1"}, null, null, new boolean[] {true}, "", null, null, null, "1.4");
		
		createJavaProject("P3", new String[] {"src"}, new String[] {"JCL_LIB"}, null, null, new String[] {"/P2"}, null, new String[][] {new String[] {"**/X"}}, true/*combine access restrictions*/, new boolean[] {true}, "bin", null, null, null, "1.4");
		setUpWorkingCopy("/P3/src/Y.java", "public class Y extends p.X {}");
		assertProblems(
			"Unexpected problems", 
			"----------\n" + 
			"1. ERROR in /P3/src/Y.java (at line 1)\n" + 
			"	public class Y extends p.X {}\n" + 
			"	                       ^^^\n" + 
			"Access restriction: The type X is not accessible due to restriction on required project P1\n" + 
			"----------\n"
		);
	} finally {
		deleteProjects(new String[] {"P1", "P2", "P3" });
	}
}
/*
 * Ensures that a problem is created for a reference to a type that is no longer accessible in a prereq project.
 * (regression test for bug 91498 Reconcile still sees old access rules)
 */
public void testAccessRestriction5() throws CoreException {
	try {
		createJavaProject("P1");
		createFolder("/P1/p");
		createFile("/P1/p/X.java", "package p; public class X {}");	
		IJavaProject p2 = createJavaProject("P2", new String[] {"src"}, new String[] {"JCL_LIB"}, "bin");
		IClasspathEntry[] classpath = p2.getRawClasspath();
		int length = classpath.length;
		System.arraycopy(classpath, 0, classpath = new IClasspathEntry[length+1], 0, length);
		classpath[length] = createSourceEntry("P2", "/P1", "+**/p/|-**/*");
		p2.setRawClasspath(classpath, null);
		setUpWorkingCopy("/P2/src/Y.java", "public class Y extends p.X {}");
		assertProblems(
			"Unexpected problems", 
			"----------\n" + 
			"----------\n"
		);
		
		// remove accessible rule
		System.arraycopy(classpath, 0, classpath = new IClasspathEntry[length+1], 0, length);
		classpath[length] = createSourceEntry("P2", "/P1", "-**/*");
		p2.setRawClasspath(classpath, null);
		this.problemRequestor.initialize(this.workingCopy.getSource().toCharArray());
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, true/*force problem detection*/, null, null);
		assertProblems(
			"Unexpected problems", 
			"----------\n" + 
			"1. ERROR in /P2/src/Y.java (at line 1)\n" + 
			"	public class Y extends p.X {}\n" + 
			"	                       ^^^\n" + 
			"Access restriction: The type X is not accessible due to restriction on required project P1\n" + 
			"----------\n"
		);

	} finally {
		deleteProjects(new String[] {"P1", "P2"});
	}
}
/**
 * Ensures that the reconciler handles duplicate members correctly.
 */
public void testAddDuplicateMember() throws JavaModelException {
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta", 
		"X[*]: {CHILDREN | FINE GRAINED}\n" + 
		"	foo()#2[+]: {}"
	);
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents, updating the structure of this reconciler's compilation
 * unit, and fires the Java element delta for the structural changes
 * of the addition of a field and a constructor.
 */
public void testAddFieldAndConstructor() throws JavaModelException {
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  int i;\n" +
		"  X(int i) {\n" +
		"    this.i = i;\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta", 
		"X[*]: {CHILDREN | FINE GRAINED}\n" + 
		"	i[+]: {}\n" + 
		"	X(int)[+]: {}"
	);
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents, updating the structure of this reconciler's compilation
 * unit, and fires the Java element delta for the structural changes
 * of the addition of a field and a constructor.
 */
public void testAddImports() throws JavaModelException {
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"import java.lang.reflect.*;\n" +
		"import java.util.Vector;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta", 
		"<import container>[*]: {CHILDREN | FINE GRAINED}\n" +
		"	import java.lang.reflect.*[+]: {}\n" +
		"	import java.util.Vector[+]: {}"
	);
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents, updating the structure of this reconciler's compilation
 * unit, and fires the Java element delta for the structural changes
 * of the addition of a method.
 */
public void testAddMethod1() throws JavaModelException {
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"  public void bar() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta", 
		"X[*]: {CHILDREN | FINE GRAINED}\n" +
		"	bar()[+]: {}"
	);
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents,updating the structure of this reconciler's compilation
 * unit, and fires the Java element delta for the structural changes
 * of the addition of a portion of a new method.
 */
public void testAddPartialMethod1() throws JavaModelException {
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void some()\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta", 
		"X[*]: {CHILDREN | FINE GRAINED}\n" + 
		"	some()[+]: {}"
	);
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents,updating the structure of this reconciler's compilation
 * unit, and fires the Java element delta for the structural changes
 * of the addition of a portion of a new method.  Ensures that when a
 * second part is added to the new method no structural changes are recognized.
 */
public void testAddPartialMethod1and2() throws JavaModelException {
	// Add partial method before foo
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void some()\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	
	// Add { on partial method
	clearDeltas();
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void some() {\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta", 
		"[Working copy] X.java[*]: {CONTENT | FINE GRAINED}"
	);
}
/*
 * Ensures that the AST broadcasted during a reconcile operation is correct.
 * (case of a working copy being reconciled with changes, creating AST and no problem detection)
 */
public void testBroadcastAST1() throws JavaModelException {
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"}");
	this.workingCopy.reconcile(AST.JLS3, false/*don't force problem detection*/, null/*primary owner*/, null/*no progress*/);
	assertASTNodeEquals(
		"Unexpected ast", 
		"package p1;\n" + 
		"import p2.*;\n" + 
		"public class X {\n" + 
		"}\n",
		this.deltaListener.getCompilationUnitAST(this.workingCopy));
}
/*
 * Ensures that the AST broadcasted during a reconcile operation is correct.
 * (case of a working copy being reconciled with NO changes, creating AST and forcing problem detection)
 */
public void testBroadcastAST2() throws JavaModelException {
	this.workingCopy.reconcile(AST.JLS3, true/*force problem detection*/, null/*primary owner*/, null/*no progress*/);
	assertASTNodeEquals(
		"Unexpected ast", 
		"package p1;\n" + 
		"import p2.*;\n" + 
		"public class X {\n" + 
		"  public void foo(){\n" + 
		"  }\n" + 
		"}\n",
		this.deltaListener.getCompilationUnitAST(this.workingCopy));
}
/*
 * Ensures that the AST broadcasted during a reconcile operation is correct.
 * (case of a working copy being reconciled with NO changes, creating AST and no problem detection)
 */
public void testBroadcastAST3() throws JavaModelException {
	this.workingCopy.reconcile(AST.JLS3, false/*don't force problem detection*/, null/*primary owner*/, null/*no progress*/);
	assertASTNodeEquals(
		"Unexpected ast", 
		"null",
		this.deltaListener.getCompilationUnitAST(this.workingCopy));
}
/*
 * Ensures that the AST broadcasted during a reconcile operation is correct.
 * (case of a working copy being reconciled twice in a batch operation)
 */
public void testBroadcastAST4() throws CoreException {
	JavaCore.run(
		new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				ReconcilerTests.this.workingCopy.reconcile(AST.JLS3, true/*force problem detection*/, null/*primary owner*/, monitor);
				setWorkingCopyContents(
					"package p1;\n" +
					"import p2.*;\n" +
					"public class X {\n" +
					"}");
				ReconcilerTests.this.workingCopy.reconcile(AST.JLS3, false/*don't force problem detection*/, null/*primary owner*/, monitor);				
			}
		},
		null/*no progress*/);
	assertASTNodeEquals(
		"Unexpected ast", 
		"package p1;\n" + 
		"import p2.*;\n" + 
		"public class X {\n" + 
		"}\n",
		this.deltaListener.getCompilationUnitAST(this.workingCopy));
}
/*
 * Ensures that reconciling a subclass doesn't close the buffer while resolving its superclass.
 * (regression test for bug 62854 refactoring does not trigger reconcile)
 */
public void testBufferOpenAfterReconcile() throws CoreException {
 	try {
		createFile(
			"/Reconciler/src/p1/Super.java",
			"package p1;\n" +
			"public class Super {\n" +
			"}"
		);
		setWorkingCopyContents(
			"package p1;\n" +
			"import p2.*;\n" +
			"public class X extends Super {\n" +
			"  public void foo() {\n" +
			"  }\n" +
			"}");
		IBuffer buffer = this.workingCopy.getBuffer();
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, true, null, null);
		assertTrue("Buffer should still be open", !buffer.isClosed());
	} finally {
		deleteFile("/Reconciler/src/p1/Super.java");
	}
}
/**
 * Ensure an OperationCanceledException is correcly thrown when progress monitor is canceled
 * @deprecated using deprecated code
 */
public void testCancel() throws JavaModelException {
	setWorkingCopyContents(
		"package p1;\n" +
		"public class X {\n" +
		"  void foo(String s) {\n" +
		"  }\n" +
		"}"
	);
	this.workingCopy.makeConsistent(null);
	
	// count the number of time isCanceled() is called when converting this source unit
	CancelCounter counter = new CancelCounter();
	this.workingCopy.reconcile(AST.JLS2, true, null, counter);
	
	// throw an OperatonCanceledException at each point isCanceled() is called
	for (int i = 0; i < counter.count; i++) {
		boolean gotException = false;
		try {
			this.workingCopy.reconcile(AST.JLS2, true, null, new Canceler(i));
		} catch (OperationCanceledException e) {
			gotException = true;
		}
		assertTrue("Should get an OperationCanceledException (" + i + ")", gotException);
	}
	
	// last should not throw an OperationCanceledException
	this.workingCopy.reconcile(AST.JLS2, true, null, new Canceler(counter.count));
}
/**
 * Ensures that the delta is correct when adding a category
 */
public void testCategories1() throws JavaModelException {
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  /**\n" +
		"   * @category cat1\n" +
		"   */\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}"
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta", 
		"X[*]: {CHILDREN | FINE GRAINED}\n" + 
		"	foo()[*]: {CATEGORIES}"
	);
}
/**
 * Ensures that the delta is correct when removing a category
 */
public void testCategories2() throws JavaModelException {
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  /**\n" +
		"   * @category cat1\n" +
		"   */\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}"
	);
	this.workingCopy.makeConsistent(null);
	
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}"
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta", 
		"X[*]: {CHILDREN | FINE GRAINED}\n" + 
		"	foo()[*]: {CATEGORIES}"
	);
}
/**
 * Ensures that the delta is correct when changing a category
 */
public void testCategories3() throws JavaModelException {
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  /**\n" +
		"   * @category cat1\n" +
		"   */\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}"
	);
	this.workingCopy.makeConsistent(null);
	
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  /**\n" +
		"   * @category cat2\n" +
		"   */\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}"
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta", 
		"X[*]: {CHILDREN | FINE GRAINED}\n" + 
		"	foo()[*]: {CATEGORIES}"
	);
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents,updating the structure of this reconciler's compilation
 * unit, and fires the Java element deltas for the structural changes
 * of a method's type parameter change.
 */
public void testChangeMethodTypeParameters() throws JavaModelException {
	setUp15WorkingCopy();
	clearDeltas();
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public <T> void foo() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta", 
		"X[*]: {CHILDREN | FINE GRAINED}\n" +
		"	foo()[*]: {CONTENT}"
	);
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents,updating the structure of this reconciler's compilation
 * unit, and fires the Java element deltas for the structural changes
 * of a type's type parameter change.
 */
public void testChangeTypeTypeParameters() throws JavaModelException {
	setUp15WorkingCopy();
	clearDeltas();
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X <T> {\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta", 
		"X[*]: {CONTENT}"
	);
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents,updating the structure of this reconciler's compilation
 * unit, and fires the Java element deltas for the structural changes
 * of a method visibility change.
 */
public void testChangeMethodVisibility() throws JavaModelException {
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  private void foo() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta", 
		"X[*]: {CHILDREN | FINE GRAINED}\n" +
		"	foo()[*]: {MODIFIERS CHANGED}"
	);
}
/**
 * Ensures that the correct delta is reported when closing the working copy and modifying its buffer.
 */
public void testCloseWorkingCopy() throws JavaModelException {
	IBuffer buffer = this.workingCopy.getBuffer();
	this.workingCopy.close();
	buffer.setContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"  public void bar() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta", 
		"X[*]: {CHILDREN | FINE GRAINED}\n" +
		"	bar()[+]: {}"
	);
}

/**
 * Ensures that a reference to a constant with type mismatch doesn't show an error.
 * (regression test for bug 17104 Compiler does not complain but "Quick Fix" ??? complains)
 */
public void testConstantReference() throws CoreException {
	try {
		createFile(
			"/Reconciler/src/p1/OS.java",
			"package p1;\n" +
			"public class OS {\n" +
			"	public static final int CONST = 23 * 1024;\n" +
			"}");
		setWorkingCopyContents(
			"package p1;\n" +
			"public class X {\n" +
			"	public short c;\n" +
			"	public static void main(String[] arguments) {\n" +
			"		short c = 1;\n" +
			"		switch (c) {\n" +
			"			case OS.CONST: return;\n" +
			"		}\n" +
			"	}\n" +
			"}");
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"----------\n"
		);
	} finally {
		deleteFile("/Reconciler/src/p1/OS.java");
	}
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents, updating the structure of this reconciler's compilation
 * unit, and fires the Java element deltas for the structural changes
 * of a method being deleted.
 */
public void testDeleteMethod1() throws JavaModelException {
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta", 
		"X[*]: {CHILDREN | FINE GRAINED}\n" +
		"	foo()[-]: {}"
	);
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents, updating the structure of this reconciler's compilation
 * unit, and fires the Java element deltas for the structural changes
 * of two methods being deleted.
 */
public void testDeleteTwoMethods() throws JavaModelException {
	// create 2 methods
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"  public void bar() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	
	// delete the 2 methods
	clearDeltas();
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta", 
		"X[*]: {CHILDREN | FINE GRAINED}\n" +
		"	bar()[-]: {}\n" +
		"	foo()[-]: {}"
	);
}
/*
 * Ensures that excluded part of prereq project are not visible
 */
public void testExcludePartOfAnotherProject1() throws CoreException {
	IClasspathEntry[] newEntries = createClasspath("Reconciler", new String[] {"/P", "-**/internal/"});
	try {
		addClasspathEntries(newEntries, true);
		createJavaProject("P");
		createFolder("/P/p/internal");
		createFile(
			"/P/p/internal/Y.java",
			"package p.internal;\n" +
			"public class Y {\n" +
			"}"
		);
		setWorkingCopyContents(
			"package p1;\n" +
			"public class X extends p.internal.Y {\n" +
			"}"
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"1. ERROR in /Reconciler/src/p1/X.java (at line 2)\n" + 
			"	public class X extends p.internal.Y {\n" + 
			"	                       ^^^^^^^^^^^^\n" + 
			"Access restriction: The type Y is not accessible due to restriction on required project P\n" + 
			"----------\n"
		);
	} finally {
		removeClasspathEntries(newEntries);
		deleteProject("P");
	}
}
/*
 * Ensures that packages that are not in excluded part of prereq project are visible
 */
public void testExcludePartOfAnotherProject2() throws CoreException {
	IClasspathEntry[] newEntries = createClasspath("Reconciler", new String[] {"/P", "-**/internal/"});
	try {
		addClasspathEntries(newEntries, true);
		createJavaProject("P");
		createFolder("/P/p/api");
		createFile(
			"/P/p/api/Y.java",
			"package p.api;\n" +
			"public class Y {\n" +
			"}"
		);
		setWorkingCopyContents(
			"package p1;\n" +
			"public class X extends p.api.Y {\n" +
			"}"
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"----------\n"
		);
	} finally {
		removeClasspathEntries(newEntries);
		deleteProject("P");
	}
}
/*
 * Ensures that included part of prereq project are visible
 */
public void testIncludePartOfAnotherProject1() throws CoreException {
	IClasspathEntry[] newEntries = createClasspath("Reconciler", new String[] {"/P", "+**/api/"});
	try {
		addClasspathEntries(newEntries, true);
		createJavaProject("P");
		createFolder("/P/p/api");
		createFile(
			"/P/p/api/Y.java",
			"package p.api;\n" +
			"public class Y {\n" +
			"}"
		);
		setWorkingCopyContents(
			"package p1;\n" +
			"public class X extends p.api.Y {\n" +
			"}"
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"----------\n"
		);
	} finally {
		removeClasspathEntries(newEntries);
		deleteProject("P");
	}
}
/*
 * Ensures that packages that are not in included part of prereq project are not visible
 */
public void testIncludePartOfAnotherProject2() throws CoreException {
	IClasspathEntry[] newEntries = createClasspath("Reconciler", new String[] {"/P", "+**/api/|-**"});
	try {
		addClasspathEntries(newEntries, true);
		createJavaProject("P");
		createFolder("/P/p/internal");
		createFile(
			"/P/p/internal/Y.java",
			"package p.internal;\n" +
			"public class Y {\n" +
			"}"
		);
		setWorkingCopyContents(
			"package p1;\n" +
			"public class X extends p.internal.Y {\n" +
			"}"
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"1. ERROR in /Reconciler/src/p1/X.java (at line 2)\n" + 
			"	public class X extends p.internal.Y {\n" + 
			"	                       ^^^^^^^^^^^^\n" + 
			"Access restriction: The type Y is not accessible due to restriction on required project P\n" + 
			"----------\n"
		);
	} finally {
		removeClasspathEntries(newEntries);
		deleteProject("P");
	}
}
/**
 * Start with no imports, add an import, and then append to the import name.
 */
public void testGrowImports() throws JavaModelException {
	// no imports
	setWorkingCopyContents(
		"package p1;\n" +
		"public class X {\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	
	// add an import
	clearDeltas();
	setWorkingCopyContents(
		"package p1;\n" +
		"import p\n" +
		"public class X {\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta", 
		"<import container>[+]: {}"
	);
		
	// append to import name
	clearDeltas();
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2\n" +
		"public class X {\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta", 
		"<import container>[*]: {CHILDREN | FINE GRAINED}\n" +
		"	import p2[+]: {}\n" +
		"	import p[-]: {}"
	);
}
/*
 * Ensures that a type matching a ignore-if-better non-accessible rule is further found when accessible
 * on another classpath entry.
 * (regression test for bug 98127 Access restrictions started showing up after switching to bundle)
 */
public void testIgnoreIfBetterNonAccessibleRule1() throws CoreException {
	IClasspathEntry[] newEntries = createClasspath("Reconciler", new String[] {"/P1", "?**/internal/", "/P2", "+**/internal/Y"});
	try {
		addClasspathEntries(newEntries, true);
		createJavaProject("P1");
		createFolder("/P1/p/internal");
		createFile(
			"/P1/p/internal/Y.java",
			"package p.internal;\n" +
			"public class Y {\n" +
			"}"
		);
		createJavaProject("P2");
		createFolder("/P2/p/internal");
		createFile(
			"/P2/p/internal/Y.java",
			"package p.internal;\n" +
			"public class Y {\n" +
			"}"
		);
		setWorkingCopyContents(
			"package p1;\n" +
			"public class X extends p.internal.Y {\n" +
			"}"
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"----------\n"
		);
	} finally {
		removeClasspathEntries(newEntries);
		deleteProjects(new String[] {"P1", "P2"});
	}
}
/*
 * Ensures that a type matching a ignore-if-better non-accessible rule is further found when accessible
 * on another classpath entry.
 * (regression test for bug 98127 Access restrictions started showing up after switching to bundle)
 */
public void testIgnoreIfBetterNonAccessibleRule2() throws CoreException {
	IClasspathEntry[] newEntries = createClasspath("Reconciler", new String[] {"/P1", "?**/internal/", "/P2", "~**/internal/Y"});
	try {
		addClasspathEntries(newEntries, true);
		createJavaProject("P1");
		createFolder("/P1/p/internal");
		createFile(
			"/P1/p/internal/Y.java",
			"package p.internal;\n" +
			"public class Y {\n" +
			"}"
		);
		createJavaProject("P2");
		createFolder("/P2/p/internal");
		createFile(
			"/P2/p/internal/Y.java",
			"package p.internal;\n" +
			"public class Y {\n" +
			"}"
		);
		setWorkingCopyContents(
			"package p1;\n" +
			"public class X extends p.internal.Y {\n" +
			"}"
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"1. WARNING in /Reconciler/src/p1/X.java (at line 2)\n" + 
			"	public class X extends p.internal.Y {\n" + 
			"	                       ^^^^^^^^^^^^\n" + 
			"Discouraged access: The type Y is not accessible due to restriction on required project P2\n" + 
			"----------\n"
		);
	} finally {
		removeClasspathEntries(newEntries);
		deleteProjects(new String[] {"P1", "P2"});
	}
}
/*
 * Ensures that a type matching a ignore-if-better non-accessible rule is further found non-accessible
 * on another classpath entry.
 * (regression test for bug 98127 Access restrictions started showing up after switching to bundle)
 */
public void testIgnoreIfBetterNonAccessibleRule3() throws CoreException {
	IClasspathEntry[] newEntries = createClasspath("Reconciler", new String[] {"/P1", "?**/internal/", "/P2", "-**/internal/Y"});
	try {
		addClasspathEntries(newEntries, true);
		createJavaProject("P1");
		createFolder("/P1/p/internal");
		createFile(
			"/P1/p/internal/Y.java",
			"package p.internal;\n" +
			"public class Y {\n" +
			"}"
		);
		createJavaProject("P2");
		createFolder("/P2/p/internal");
		createFile(
			"/P2/p/internal/Y.java",
			"package p.internal;\n" +
			"public class Y {\n" +
			"}"
		);
		setWorkingCopyContents(
			"package p1;\n" +
			"public class X extends p.internal.Y {\n" +
			"}"
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"1. ERROR in /Reconciler/src/p1/X.java (at line 2)\n" + 
			"	public class X extends p.internal.Y {\n" + 
			"	                       ^^^^^^^^^^^^\n" + 
			"Access restriction: The type Y is not accessible due to restriction on required project P1\n" + 
			"----------\n"
		);
	} finally {
		removeClasspathEntries(newEntries);
		deleteProjects(new String[] {"P1", "P2"});
	}
}
/*
 * Ensures that a type matching a ignore-if-better non-accessible rule is found non-accessible
 * if no other classpath entry matches it.
 * (regression test for bug 98127 Access restrictions started showing up after switching to bundle)
 */
public void testIgnoreIfBetterNonAccessibleRule4() throws CoreException {
	IClasspathEntry[] newEntries = createClasspath("Reconciler", new String[] {"/P1", "?**/internal/"});
	try {
		addClasspathEntries(newEntries, true);
		createJavaProject("P1");
		createFolder("/P1/p/internal");
		createFile(
			"/P1/p/internal/Y.java",
			"package p.internal;\n" +
			"public class Y {\n" +
			"}"
		);
		setWorkingCopyContents(
			"package p1;\n" +
			"public class X extends p.internal.Y {\n" +
			"}"
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"1. ERROR in /Reconciler/src/p1/X.java (at line 2)\n" + 
			"	public class X extends p.internal.Y {\n" + 
			"	                       ^^^^^^^^^^^^\n" + 
			"Access restriction: The type Y is not accessible due to restriction on required project P1\n" + 
			"----------\n"
		);
	} finally {
		removeClasspathEntries(newEntries);
		deleteProjects(new String[] {"P1"});
	}
}
/**
 * Introduces a syntax error in the modifiers of a method.
 */
public void testMethodWithError01() throws CoreException {
	// Introduce syntax error
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public.void foo() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta after syntax error", 
		"X[*]: {CHILDREN | FINE GRAINED}\n" +
		"	foo()[*]: {MODIFIERS CHANGED}"
	);
	assertProblems(
		"Unexpected problems",
		"----------\n" + 
		"1. ERROR in /Reconciler/src/p1/X.java (at line 4)\n" + 
		"	public.void foo() {\n" + 
		"	      ^\n" + 
		"Syntax error on token \".\", delete this token\n" + 
		"----------\n"
	);

	// Fix the syntax error
	clearDeltas();
	String contents =
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}";
	setWorkingCopyContents(contents);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta after fixing syntax error", 
		"X[*]: {CHILDREN | FINE GRAINED}\n" +
		"	foo()[*]: {MODIFIERS CHANGED}"
	);
	assertProblems(
		"Unexpected problems",
		"----------\n" + 
		"1. WARNING in /Reconciler/src/p1/X.java (at line 2)\n" + 
		"	import p2.*;\n" + 
		"	       ^^\n" + 
		"The import p2 is never used\n" + 
		"----------\n"
	);
}
/**
 * Test reconcile force flag
 */
public void testMethodWithError02() throws CoreException {
	String contents =
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public.void foo() {\n" +
		"  }\n" +
		"}";		
	setWorkingCopyContents(contents);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);

	// use force flag to refresh problems			
	this.problemRequestor.initialize(contents.toCharArray());
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, true, null, null);
	assertProblems(
		"Unexpected problems",
		"----------\n" + 
		"1. ERROR in /Reconciler/src/p1/X.java (at line 4)\n" + 
		"	public.void foo() {\n" + 
		"	      ^\n" + 
		"Syntax error on token \".\", delete this token\n" + 
		"----------\n"
	);
}

/**
 * Test reconcile force flag off
 */
public void testMethodWithError03() throws CoreException {
	String contents =
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public.void foo() {\n" +
		"  }\n" +
		"}";
	setWorkingCopyContents(contents);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);

	// reconcile with force flag turned off
	this.problemRequestor.initialize(contents.toCharArray());
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertProblems(
		"Unexpected problems",
		""
	);
}
/**
 * Test reconcile force flag + cancel
 */
public void testMethodWithError04() throws CoreException {

	CancelingProblemRequestor myPbRequestor = new CancelingProblemRequestor();
	
	this.workingCopy.discardWorkingCopy();
	ICompilationUnit x = getCompilationUnit("Reconciler", "src", "p1", "X.java");
	this.problemRequestor = myPbRequestor;
	this.workingCopy = x.getWorkingCopy(new WorkingCopyOwner() {}, this.problemRequestor, null);

	String contents =
		"package p1;\n" +
		"public class X {\n" +
		"	Zork f;	\n"+
		"	void foo(Zork z){\n"+
		"	}\n"+
		"}	\n";
	setWorkingCopyContents(contents);

	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);

	// use force flag to refresh problems			
	myPbRequestor.isCanceling = true;
	myPbRequestor.initialize(contents.toCharArray());
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, true, null, myPbRequestor.progressMonitor);
	assertProblems(
		"Unexpected problems",
		"----------\n" + 
		"1. ERROR in /Reconciler/src/p1/X.java (at line 3)\n" + 
		"	Zork f;	\n" + 
		"	^^^^\n" + 
		"Zork cannot be resolved to a type\n" + 
		"----------\n"
	);
}

/**
 * Test reconcile force flag off
 */
public void testMethodWithError05() throws CoreException {
	try {
		createFolder("/Reconciler/src/tests");
		String contents =
			"package tests;	\n"+
			"abstract class AbstractSearchableSource extends AbstractSource implements SearchableSource {	\n"+
			"	abstract int indexOfImpl(long value);	\n"+
			"	public final int indexOf(long value) {	\n"+
			"		return indexOfImpl(value);	\n"+
			"	}	\n"+
			"}	\n";
		createFile(
			"/Reconciler/src/tests/AbstractSearchableSource.java", 
			contents);
	
		createFile(
			"/Reconciler/src/tests/Source.java", 
			"package tests;	\n"+
			"interface Source {	\n"+
			"	long getValue(int index);	\n"+
			"	int size();	\n"+
			"}	\n");
	
		createFile(
			"/Reconciler/src/tests/AbstractSource.java", 
			"package tests;	\n"+
			"abstract class AbstractSource implements Source {	\n"+
			"	AbstractSource() {	\n"+
			"	}	\n"+
			"	void invalidate() {	\n"+
			"	}	\n"+
			"	abstract long getValueImpl(int index);	\n"+
			"	abstract int sizeImpl();	\n"+
			"	public final long getValue(int index) {	\n"+
			"		return 0;	\n"+
			"	}	\n"+
			"	public final int size() {	\n"+
			"		return 0;	\n"+
			"	}	\n"+
			"}	\n");
	
		createFile(
			"/Reconciler/src/tests/SearchableSource.java", 
			"package tests;	\n"+
			"interface SearchableSource extends Source {	\n"+
			"	int indexOf(long value);	\n"+
			"}	\n");
	
		ICompilationUnit compilationUnit = getCompilationUnit("Reconciler", "src", "tests", "AbstractSearchableSource.java");
		ProblemRequestor pbReq =  new ProblemRequestor();
		ICompilationUnit wc = compilationUnit.getWorkingCopy(new WorkingCopyOwner() {}, pbReq, null);
		pbReq.initialize(contents.toCharArray());
		startDeltas();
		wc.reconcile(ICompilationUnit.NO_AST, true, null, null);
		String actual = pbReq.problems.toString();
		String expected = 
			"----------\n" + 
			"----------\n";
		if (!expected.equals(actual)){
		 	System.out.println(Util.displayString(actual, 2));
		}
		assertEquals(
			"unexpected errors",
			expected,
			actual);
	} finally {
		deleteFile("/Reconciler/src/tests/AbstractSearchableSource.java");
		deleteFile("/Reconciler/src/tests/SearchableSource.java");
		deleteFile("/Reconciler/src/tests/Source.java");
		deleteFile("/Reconciler/src/tests/AbstractSource.java");
		deleteFolder("/Reconciler/src/tests");
	}
}
/*
 * Test that the creation of a working copy detects errors
 * (regression test for bug 33757 Problem not detected when opening a working copy)
 */
public void testMethodWithError06() throws CoreException {
	this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
	this.workingCopy = null;
	try {
		String contents =
			"package p1;\n" +
			"public class Y {\n" +
			"  public.void foo() {\n" +
			"  }\n" +
			"}";
		createFile(
			"/Reconciler/src/p1/Y.java", 
			contents
		);
		this.problemRequestor =  new ProblemRequestor();
		this.problemRequestor.initialize(contents.toCharArray());
		this.workingCopy = getCompilationUnit("Reconciler/src/p1/Y.java").getWorkingCopy(new WorkingCopyOwner() {}, this.problemRequestor, null);
		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"1. ERROR in /Reconciler/src/p1/Y.java (at line 3)\n" + 
			"	public.void foo() {\n" + 
			"	      ^\n" + 
			"Syntax error on token \".\", delete this token\n" + 
			"----------\n"
		);
	} finally {
		deleteFile("/Reconciler/src/p1/Y.java");
	}
}
/*
 * Test that the opening of a working copy detects errors
 * (regression test for bug 33757 Problem not detected when opening a working copy)
 */
public void testMethodWithError07() throws CoreException {
	this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
	this.workingCopy = null;
	try {
		String contents =
			"package p1;\n" +
			"public class Y {\n" +
			"  public.void foo() {\n" +
			"  }\n" +
			"}";
		createFile(
			"/Reconciler/src/p1/Y.java", 
			contents
		);
		this.problemRequestor =  new ProblemRequestor();
		this.problemRequestor.initialize(contents.toCharArray());
		this.workingCopy = getCompilationUnit("Reconciler/src/p1/Y.java").getWorkingCopy(new WorkingCopyOwner() {}, this.problemRequestor, null);

		// Close working copy
		JavaModelManager.getJavaModelManager().removeInfoAndChildren((CompilationUnit)workingCopy); // use a back door as working copies cannot be closed
		
		// Reopen should detect syntax error
		this.problemRequestor.initialize(contents.toCharArray());
		this.workingCopy.open(null);
		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"1. ERROR in /Reconciler/src/p1/Y.java (at line 3)\n" + 
			"	public.void foo() {\n" + 
			"	      ^\n" + 
			"Syntax error on token \".\", delete this token\n" + 
			"----------\n"
		);
	} finally {
		deleteFile("/Reconciler/src/p1/Y.java");
	}
}
/*
 * Test that the units with similar names aren't presenting each other errors
 * (regression test for bug 39475)
 */
public void testMethodWithError08() throws CoreException {
	this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
	this.workingCopy = null;
	try {
		createFile(
			"/Reconciler/src/p1/X01.java", 
			"package p1;\n" +
			"public abstract class X01 {\n" +
			"	public abstract void bar();	\n"+
			"  public abstract void foo(Zork z); \n"+
			"}"
		);
		String contents = 
			"package p2;\n" +
			"public class X01 extends p1.X01 {\n" +
			"	public void bar(){}	\n"+
			"}";
		createFile(
			"/Reconciler/src/p2/X01.java", 
			contents
		);
		this.problemRequestor =  new ProblemRequestor();
		this.problemRequestor.initialize(contents.toCharArray());
		this.workingCopy = getCompilationUnit("Reconciler/src/p2/X01.java").getWorkingCopy(new WorkingCopyOwner() {}, this.problemRequestor, null);

		// Close working copy
		JavaModelManager.getJavaModelManager().removeInfoAndChildren((CompilationUnit)workingCopy); // use a back door as working copies cannot be closed
		
		// Reopen should detect syntax error
		this.problemRequestor.initialize(contents.toCharArray());
		this.workingCopy.open(null);
		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"----------\n" // shouldn't report problem against p2.X01
		);
	} finally {
		deleteFile("/Reconciler/src/p1/X01.java");
		deleteFile("/Reconciler/src/p2/X01.java");
	}
}
/*
 * Scenario of reconciling using a working copy owner
 */
public void testMethodWithError09() throws CoreException {
	this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
	this.workingCopy = null;
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	ICompilationUnit workingCopy1 = null;
	try {
		workingCopy1 = getCompilationUnit("/Reconciler/src/p1/X1.java").getWorkingCopy(owner, null, null);
		workingCopy1.getBuffer().setContents(
			"package p1;\n" +
			"public abstract class X1 {\n" +
			"	public abstract void bar();	\n"+
			"}"
		);
		workingCopy1.makeConsistent(null);
		
		this.problemRequestor =  new ProblemRequestor();
		this.workingCopy = getCompilationUnit("Reconciler/src/p/X.java").getWorkingCopy(owner, this.problemRequestor, null);
		setWorkingCopyContents(
			"package p;\n" +
			"public class X extends p1.X1 {\n" +
			"	public void bar(){}	\n"+
			"}"
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, owner, null);

		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"----------\n" // shouldn't report problem against p.X
		);
	} finally {
		if (workingCopy1 != null) {
			workingCopy1.discardWorkingCopy();
		}
	}
}
/*
 * Scenario of reconciling using a working copy owner  (68557)
 */
public void testMethodWithError10() throws CoreException {
	this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
	this.workingCopy = null;
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	ICompilationUnit workingCopy1 = null;
	try {
		createFolder("/Reconciler15/src/test/cheetah");
		workingCopy1 = getCompilationUnit("/Reconciler15/src/test/cheetah/NestedGenerics.java").getWorkingCopy(owner, null, null);
		workingCopy1.getBuffer().setContents(
			"package test.cheetah;\n"+
			"import java.util.List;\n"+
			"import java.util.Stack;\n"+
			"public class NestedGenerics {\n"+
			"    Stack< List<Object>> stack = new Stack< List<Object> >();\n"+
			"}\n"
		);
		workingCopy1.makeConsistent(null);
		
		this.problemRequestor =  new ProblemRequestor();
		this.workingCopy = getCompilationUnit("Reconciler15/src/test/cheetah/NestedGenericsTest.java").getWorkingCopy(owner, this.problemRequestor, null);
		setWorkingCopyContents(
			"package test.cheetah;\n"+
			"import java.util.Stack;\n"+
			"public class NestedGenericsTest {\n"+
			"    void test() {  \n"+
			"        Stack s = new NestedGenerics().stack;  \n"+
			"    }\n"+
			"}\n"
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, owner, null);

		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"----------\n"
		);
	} finally {
		if (workingCopy1 != null) {
			workingCopy1.discardWorkingCopy();
		}
		deleteFolder("/Reconciler15/src/test");
	}
}
/*
 * Scenario of reconciling using a working copy owner (68557)
 */
public void testMethodWithError11() throws CoreException {
	this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
	this.workingCopy = null;
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	ICompilationUnit workingCopy1 = null;
	try {
		createFolder("/Reconciler15/src/test/cheetah");
		workingCopy1 = getCompilationUnit("/Reconciler15/src/test/cheetah/NestedGenerics.java").getWorkingCopy(owner, null, null);
		workingCopy1.getBuffer().setContents(
			"package test.cheetah;\n"+
			"import java.util.*;\n"+
			"public class NestedGenerics {\n"+
			"    Map<List<Object>,String> map = null;\n"+
			"    Stack<List<Object>> stack2 = null;\n"+
			"    Map<List<Object>,List<Object>> map3 = null;\n"+
			"}\n"
		);
		workingCopy1.makeConsistent(null);
		
		this.problemRequestor =  new ProblemRequestor();
		this.workingCopy = getCompilationUnit("Reconciler15/src/test/cheetah/NestedGenericsTest.java").getWorkingCopy(owner, this.problemRequestor, null);
		setWorkingCopyContents(
			"package test.cheetah;\n"+
			"import java.util.*;\n"+
			"public class NestedGenericsTest {\n"+
			"    void test() {  \n"+
			"        Map m = new NestedGenerics().map;  \n"+
			"		 Stack s2 = new NestedGenerics().stack2;    \n"+
			"        Map m3 = new NestedGenerics().map3;    \n"+
			"    }\n"+
			"}\n"
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, owner, null);

		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"----------\n"
		);
	} finally {
		if (workingCopy1 != null) {
			workingCopy1.discardWorkingCopy();
		}
		deleteFolder("/Reconciler15/src/test");
	}
}
/*
 * Scenario of reconciling using a working copy owner (68557 variation with wildcards)
 */
public void testMethodWithError12() throws CoreException {
	this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
	this.workingCopy = null;
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	ICompilationUnit workingCopy1 = null;
	try {
		createFolder("/Reconciler15/src/test/cheetah");
		workingCopy1 = getCompilationUnit("/Reconciler15/src/test/cheetah/NestedGenerics.java").getWorkingCopy(owner, null, null);
		workingCopy1.getBuffer().setContents(
			"package test.cheetah;\n"+
			"import java.util.*;\n"+
			"public class NestedGenerics {\n"+
			"    Map<List<?>,? super String> map = null;\n"+
			"    Stack<List<? extends Object>> stack2 = null;\n"+
			"    Map<List<Object[]>,List<Object>[]> map3 = null;\n"+
			"}\n"
		);
		workingCopy1.makeConsistent(null);
		
		this.problemRequestor =  new ProblemRequestor();
		this.workingCopy = getCompilationUnit("Reconciler15/src/test/cheetah/NestedGenericsTest.java").getWorkingCopy(owner, this.problemRequestor, null);
		setWorkingCopyContents(
			"package test.cheetah;\n"+
			"import java.util.*;\n"+
			"public class NestedGenericsTest {\n"+
			"    void test() {  \n"+
			"        Map m = new NestedGenerics().map;  \n"+
			"		 Stack s2 = new NestedGenerics().stack2;    \n"+
			"        Map m3 = new NestedGenerics().map3;    \n"+
			"    }\n"+
			"}\n"
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, owner, null);

		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"----------\n"
		);
	} finally {
		if (workingCopy1 != null) {
			workingCopy1.discardWorkingCopy();
		}
		deleteFolder("/Reconciler15/src/test");
	}
}
/*
 * Scenario of reconciling using a working copy owner (68730)
 */
public void testMethodWithError13() throws CoreException {
	this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
	this.workingCopy = null;
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	ICompilationUnit workingCopy1 = null;
	try {
		workingCopy1 = getCompilationUnit("/Reconciler15/src/test/X.java").getWorkingCopy(owner, null, null);
		createFolder("/Reconciler15/src/test");
		workingCopy1.getBuffer().setContents(
			"package test;\n"+
			"public class X <T extends String, U> {\n"+
			"	<Y1> void bar(Y1[] y) {}\n"+
			"	void bar2(Y<E3[]>[] ye[]) {}\n"+
			"    void foo(java.util.Map<Object[],String>.MapEntry<p.K<T>[],? super q.r.V8> m){}\n"+
			"    Class<? extends Object> getClass0() {}\n"+
			"    <E extends String> void pair (X<? extends E, U> e, T t){}\n"+
			"}\n"
		);
		workingCopy1.makeConsistent(null);
		
		this.problemRequestor =  new ProblemRequestor();
		this.workingCopy = getCompilationUnit("Reconciler15/src/test/Y.java").getWorkingCopy(owner, this.problemRequestor, null);
		setWorkingCopyContents(
			"package test;\n"+
			"public class Y {\n"+
			"	void foo(){\n"+
			"		X someX = new X();\n"+
			"		someX.bar(null);\n"+
			"	}\n"+
			"}\n"
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, owner, null);

		assertProblems(
			"Unexpected problems",
		"----------\n" + 
		"1. WARNING in /Reconciler15/src/test/Y.java (at line 5)\n" + 
		"	someX.bar(null);\n" + 
		"	^^^^^^^^^^^^^^^\n" + 
		"Type safety: The method bar(Object[]) belongs to the raw type X. References to generic type X<T,U> should be parameterized\n" + 
		"----------\n"
		);
	} finally {
		if (workingCopy1 != null) {
			workingCopy1.discardWorkingCopy();
		}
		deleteFolder("/Reconciler15/src/test");
	}
}
/*
 * Scenario of reconciling using a working copy owner (66424)
 */
public void testMethodWithError14() throws CoreException {
	this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
	this.workingCopy = null;
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	ICompilationUnit workingCopy1 = null;
	try {
		workingCopy1 = getCompilationUnit("/Reconciler15/src/test/X.java").getWorkingCopy(owner, null, null);
		createFolder("/Reconciler15/src/test");
		workingCopy1.getBuffer().setContents(
			"package test;\n"+
			"public class X <T> {\n"+
			"	<U> void bar(U u) {}\n"+
			"}\n"
		);
		workingCopy1.makeConsistent(null);
		
		this.problemRequestor =  new ProblemRequestor();
		this.workingCopy = getCompilationUnit("Reconciler15/src/test/Y.java").getWorkingCopy(owner, this.problemRequestor, null);
		setWorkingCopyContents(
			"package test;\n"+
			"public class Y {\n"+
			"	void foo(){\n"+
			"		X someX = new X();\n"+
			"		someX.bar();\n"+
			"	}\n"+
			"}\n"
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, owner, null);

		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"1. ERROR in /Reconciler15/src/test/Y.java (at line 5)\n" + 
			"	someX.bar();\n" + 
			"	      ^^^\n" + 
			"The method bar(Object) in the type X is not applicable for the arguments ()\n" + 
			"----------\n"
		);
	} finally {
		if (workingCopy1 != null) {
			workingCopy1.discardWorkingCopy();
		}
		deleteFolder("/Reconciler15/src/test");
	}
}
/**
 * Ensures that the reconciler handles member move correctly.
 */
public void testMoveMember() throws JavaModelException {
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"  public void bar() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	clearDeltas();
	
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void bar() {\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta", 
		"X[*]: {CHILDREN | FINE GRAINED}\n" + 
		"	bar()[*]: {REORDERED}\n" + 
		"	foo()[*]: {REORDERED}"
	);
}
/**
 * Ensures that the reconciler does nothing when the source
 * to reconcile with is the same as the current contents.
 */
public void testNoChanges1() throws JavaModelException {
	setWorkingCopyContents(this.workingCopy.getSource());
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta",
		"[Working copy] X.java[*]: {CONTENT | FINE GRAINED}"
	);
}
/**
 * Ensures that the reconciler does nothing when the source
 * to reconcile with has the same structure as the current contents.
 */
public void testNoChanges2() throws JavaModelException {
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"    System.out.println()\n" +
		"  }\n" +
		"}"
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta",
		"[Working copy] X.java[*]: {CONTENT | FINE GRAINED}"
	);
}
/*
 * Ensures that using a non-generic method with no parametera and with a raw receiver type doesn't create a type safety warning
 * (regression test for bug 105756 [1.5][model] Incorrect warning on using raw types)
 */
public void testRawUsage() throws CoreException {
	ICompilationUnit otherCopy = null;
	try {
		WorkingCopyOwner owner = new WorkingCopyOwner() {};
		otherCopy = getWorkingCopy(
			"Reconciler15/src/Generic105756.java", 
			"public class Generic105756<T> {\n" +
			"  void foo() {}\n" +
			"}",
			owner,
			false/*don't compute problems*/);
		setUp15WorkingCopy("/Reconciler15/src/X.java", owner);
		setWorkingCopyContents(
			"public class X {\n" +
			"  void bar(Generic105756 g) {\n" +
			"    g.foo();\n" +
			"  }\n" +
			"}"
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"----------\n"
		);
	} finally {
		if (otherCopy != null)
			otherCopy.discardWorkingCopy();
	}
}
/*
 * Ensures that a reconcile participant is notified when a working copy is reconciled.
 */
public void testReconcileParticipant01() throws CoreException {
	ReconcileParticipant participant = new ReconcileParticipant();
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void bar() {\n" +
		"    System.out.println()\n" +
		"  }\n" +
		"}"
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected participant delta",
		"[Working copy] X.java[*]: {CHILDREN | FINE GRAINED}\n" + 
		"	X[*]: {CHILDREN | FINE GRAINED}\n" + 
		"		bar()[+]: {}\n" + 
		"		foo()[-]: {}",
		participant.delta
	);
}
/*
 * Ensures that a reconcile participant is not notified if not participating.
 */
public void testReconcileParticipant02() throws CoreException {
	ReconcileParticipant participant = new ReconcileParticipant(){
		public boolean isActive(IJavaProject project) {
			return false;
		}
	};
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void bar() {\n" +
		"    System.out.println()\n" +
		"  }\n" +
		"}"
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected participant delta",
		"<null>",
		participant.delta
	);
}
/*
 * Ensures that a reconcile participant is notified with the correct AST.
 */
public void testReconcileParticipant03() throws CoreException {
	ReconcileParticipant participant = new ReconcileParticipant(AST.JLS3, false/*don't resolve binding*/);
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void bar() {\n" +
		"    System.out.println()\n" +
		"  }\n" +
		"}"
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertASTNodeEquals(
		"Unexpected participant delta",
		"package p1;\n" + 
		"import p2.*;\n" + 
		"public class X {\n" + 
		"  public void bar(){\n" + 
		"  }\n" + 
		"}\n",
		participant.ast
	);
}
/*
 * Ensures that the same AST as the one a reconcile participant requested is reported.
 */
public void testReconcileParticipant04() throws CoreException {
	ReconcileParticipant participant = new ReconcileParticipant(AST.JLS3, false/*don't resolve binding*/);
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void bar() {\n" +
		"    System.out.println()\n" +
		"  }\n" +
		"}"
	);
	org.eclipse.jdt.core.dom.CompilationUnit ast = this.workingCopy.reconcile(AST.JLS3, false, null, null);
	assertSame(
		"Unexpected participant delta",
		participant.ast,
		ast
	);
}
/*
 * Ensures that a participant can fix an error during reconcile.
 */
public void testReconcileParticipant05() throws CoreException {
	new ReconcileParticipant(AST.JLS3, true/*resolve binding*/) {
		public void reconcile(ReconcileContext context) {
			try {
				setWorkingCopyContents(
					"package p1;\n" +
					"public class X {\n" +
					"  public void bar() {\n" +
					"  }\n" +
					"}"
				);
				context.resetAST();
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}
	};
	setWorkingCopyContents(
		"package p1;\n" +
		"public class X {\n" +
		"  public void bar() {\n" +
		"    toString()\n" +
		"  }\n" +
		"}"
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertProblems(
		"Unexpected problems",
		"----------\n" + 
		"----------\n"
	);
}
/*
 * Ensures that a participant can introduce an error during reconcile.
 */
public void testReconcileParticipant06() throws CoreException {
	new ReconcileParticipant(AST.JLS3, true/*resolve binding*/) {
		public void reconcile(ReconcileContext context) {
			try {
				setWorkingCopyContents(
					"package p1;\n" +
					"public class X {\n" +
					"  public void bar() {\n" +
					"    toString()\n" +
					"  }\n" +
					"}"
				);
				context.resetAST();
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}
	};
	setWorkingCopyContents(
		"package p1;\n" +
		"public class X {\n" +
		"  public void bar() {\n" +
		"  }\n" +
		"}"
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertProblems(
		"Unexpected problems",
		"----------\n" + 
		"1. ERROR in /Reconciler/src/p1/X.java (at line 4)\n" + 
		"	toString()\n" + 
		"	         ^\n" + 
		"Syntax error, insert \";\" to complete BlockStatements\n" + 
		"----------\n"
	);
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents, updating the structure of this reconciler's compilation
 * unit, and fires the Java element deltas for the structural changes
 * of a renaming a method; the original method deleted and the new method added structurally.
 */
public void testRenameMethod1() throws JavaModelException {
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void bar() {\n" +
		"  }\n" +
		"}"
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta",
		"X[*]: {CHILDREN | FINE GRAINED}\n" + 
		"	bar()[+]: {}\n" + 
		"	foo()[-]: {}"
	);
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents,updating the structure of this reconciler's compilation
 * unit, and fires the Java element delta for the structural changes
 * of the addition of a portion of a new method.
 */
public void testRenameWithSyntaxError() throws JavaModelException {
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void bar( {\n" +
		"  }\n" +
		"}"
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta",
		"X[*]: {CHILDREN | FINE GRAINED}\n" + 
		"	bar()[+]: {}\n" + 
		"	foo()[-]: {}"
	);
	assertProblems(
		"Unexpected problems",
		"----------\n" + 
		"1. ERROR in /Reconciler/src/p1/X.java (at line 4)\n" + 
		"	public void bar( {\n" + 
		"	               ^\n" + 
		"Syntax error, insert \")\" to complete MethodDeclaration\n" + 
		"----------\n"
	);
}
/*
 * Ensure that warning are suppressed by an @SuppressWarnings annotation.
 */
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=95056
//TODO (kent) reenable once addressed
public void _testSuppressWarnings1() throws JavaModelException {
	ICompilationUnit otherCopy = null;
	try {
		WorkingCopyOwner owner = new WorkingCopyOwner() {};
		otherCopy = getWorkingCopy(
			"/Reconciler15/src/X.java",
	        "@Deprecated\n" + 
	        "public class X {\n" + 
	        "   void foo(){}\n" +
	        "}\n",
			owner,
			false/*don't compute problems*/);
		setUp15WorkingCopy("/Reconciler15/src/Y.java", owner);
		setWorkingCopyContents(
	        "public class Y extends X {\n" + 
	        "  @SuppressWarnings(\"all\")\n" +
	        "   void foo(){ super.foo(); }\n" +
	        "   Zork z;\n" +
	        "}\n"
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"1. WARNING in /Reconciler15/src/Y.java (at line 1)\n" + 
			"	public class Y extends X {\n" + 
			"	                       ^\n" + 
			"The type X is deprecated\n" + 
			"----------\n" + 
			"2. ERROR in /Reconciler15/src/Y.java (at line 4)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
	} finally {
		if (otherCopy != null)
			otherCopy.discardWorkingCopy();
	}
}
/*
 * Ensure that warning are suppressed by an @SuppressWarning annotation.
 */
public void testSuppressWarnings2() throws JavaModelException {
	ICompilationUnit otherCopy = null;
	try {
		WorkingCopyOwner owner = new WorkingCopyOwner() {};
		otherCopy = getWorkingCopy(
			"/Reconciler15/src/java/util/List.java",
			"package java.util;\n" +
	        "public interface List<E> {\n" + 
	        "}\n",
			owner,
			false/*don't compute problems*/);
		setUp15WorkingCopy("/Reconciler15/src/X.java", owner);
		setWorkingCopyContents(
            "import java.util.List;\n" + 
            "\n" + 
            "public class X {\n" + 
            "    void foo(List list) {\n" + 
            "        List<String> ls1 = list;\n" + 
            "    }\n" + 
            "    @SuppressWarnings(\"unchecked\")\n" + 
            "    void bar(List list) {\n" + 
            "        List<String> ls2 = list;\n" + 
            "    }\n" + 
            "   Zork z;\n" +
            "}\n"
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"1. WARNING in /Reconciler15/src/X.java (at line 5)\n" + 
			"	List<String> ls1 = list;\n" + 
			"	                   ^^^^\n" + 
			"Type safety: The expression of type List needs unchecked conversion to conform to List<String>\n" + 
			"----------\n" + 
			"2. ERROR in /Reconciler15/src/X.java (at line 11)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n"
		);
	} finally {
		if (otherCopy != null)
			otherCopy.discardWorkingCopy();
	}
}
/*
 * Ensure that warning are suppressed by an @SuppressWarning annotation.
 */
public void testSuppressWarnings3() throws JavaModelException {
	ICompilationUnit otherCopy = null;
	try {
		WorkingCopyOwner owner = new WorkingCopyOwner() {};
		otherCopy = getWorkingCopy(
			"/Reconciler15/src/java/util/HashMap.java",
			"package java.util;\n" +
	        "public class HashMap implements Map {\n" + 
	        "}\n",
			owner,
			false/*don't compute problems*/);
		setUp15WorkingCopy("/Reconciler15/src/X.java", owner);
		setWorkingCopyContents(
			"import java.util.*;\n" + 
			"@SuppressWarnings(\"unchecked\")\n" + 
			"public class X {\n" + 
			"	void foo() {\n" + 
			"		Map<String, String>[] map = new HashMap[10];\n" + 
			"	}\n" + 
            "   Zork z;\n" +				
			"}\n"
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"1. ERROR in /Reconciler15/src/X.java (at line 7)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n"
		);
	} finally {
		if (otherCopy != null)
			otherCopy.discardWorkingCopy();
	}
}
/*
 * Ensure that warning are suppressed by an @SuppressWarnings annotation.
 */
public void testSuppressWarnings4() throws JavaModelException {
	ICompilationUnit otherCopy = null;
	try {
		WorkingCopyOwner owner = new WorkingCopyOwner() {};
		otherCopy = getWorkingCopy(
			"/Reconciler15/src/X.java",
	        "/** @deprecated */\n" + 
	        "public class X {\n" + 
	        "   void foo(){}\n" +
	        "}\n",
			owner,
			false/*don't compute problems*/);
		setUp15WorkingCopy("/Reconciler15/src/Y.java", owner);
		setWorkingCopyContents(
	        "public class Y extends X {\n" + 
	        "  @SuppressWarnings(\"all\")\n" +
	        "   void foo(){ super.foo(); }\n" +
	        "   Zork z;\n" +
	        "}\n"
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"1. WARNING in /Reconciler15/src/Y.java (at line 1)\n" + 
			"	public class Y extends X {\n" + 
			"	                       ^\n" + 
			"The type X is deprecated\n" + 
			"----------\n" + 
			"2. ERROR in /Reconciler15/src/Y.java (at line 4)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
	} finally {
		if (otherCopy != null)
			otherCopy.discardWorkingCopy();
	}
}
/**
 * Ensure that an unhandled exception is detected.
 */
public void testUnhandledException() throws JavaModelException {
	setWorkingCopyContents(
		"package p1;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"    throw new Exception();\n" +
		"  }\n" +
		"}"
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertProblems(
		"Unexpected problems",
		"----------\n" + 
		"1. ERROR in /Reconciler/src/p1/X.java (at line 4)\n" + 
		"	throw new Exception();\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Unhandled exception type Exception\n" + 
		"----------\n"
	);
}
/**
 * Check that forcing a make consistent action is leading the next reconcile to not notice changes.
 */
public void testMakeConsistentFoolingReconciler() throws JavaModelException {
	setWorkingCopyContents("");
	this.workingCopy.makeConsistent(null);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Should have got NO delta", 
		""
	);
}
/**
 * Test bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=60689
 * AST on reconcile: AST without Javadoc comments created
 * @deprecated using deprecated code
 */
public void testBug60689() throws JavaModelException {
	setWorkingCopyContents("public class X {\n" +
		"	/**\n" +
		"	 * Returns the length of the string representing the number of \n" +
		"	 * indents in the given string <code>line</code>. Returns \n" +
		"	 * <code>-1<code> if the line isn't prefixed with an indent of\n" +
		"	 * the given number of indents. \n" +
		"	 */\n" +
		"	public static int computeIndentLength(String line, int numberOfIndents, int tabWidth) {\n" +
		"		return 0;\n" +
		"}"
	);
	org.eclipse.jdt.core.dom.CompilationUnit testCU = this.workingCopy.reconcile(AST.JLS2, true, null, null);
	assertNotNull("We should have a comment!", testCU.getCommentList());
	assertEquals("We should have 1 comment!", 1, testCU.getCommentList().size());
	testCU = this.workingCopy.reconcile(AST.JLS2, true, null, null);
	assertNotNull("We should have a comment!", testCU.getCommentList());
	assertEquals("We should have one comment!", 1, testCU.getCommentList().size());
}
/*
 * Ensures that a working copy in a 1.4 project that references a 1.5 project can be reconciled without error.
 * (regression test for bug 98434 A non-1.5 project with 1.5 projects in the classpath does not show methods with generics)
 */
public void testTwoProjectsWithDifferentCompliances() throws CoreException {
	this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
	try {
		createJavaProject("P1", new String[] {""}, new String[] {"JCL15_LIB"}, "", "1.5");
		createFolder("/P1/p");
		createFile(
			"/P1/p/X.java",
			"package p;\n" +
			"public class X {\n" +
			"  void foo(Class<String> c) {\n" +
			"  }\n" +
			"}"
		);
		
		createJavaProject("P2", new String[] {""}, new String[] {"JCL_LIB"}, new String[] {"/P1"}, "", "1.4");
		createFolder("/P2/p");
		WorkingCopyOwner owner = new WorkingCopyOwner() {};
		this.workingCopy = getWorkingCopy("/P2/p/Y.java", "", owner, this.problemRequestor);
		setWorkingCopyContents(
			"package p;\n" +
			"public class Y {\n" +
			"  void bar(Class c) {\n" +
			"    new X().foo(c);\n" +
			"  }\n" +
			"}"
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, true/*force pb detection*/, owner, null);
		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"----------\n"
		);
	} finally {
		deleteProjects(new String[] {"P1", "P2"});
	}
}
/*
 * Ensures that a method that has a type parameter with bound can be overriden in another working copy.
 * (regression test for bug 76780 [model] return type not recognized correctly on some generic methods)
 */
public void testTypeParameterWithBound() throws CoreException {
	this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
	this.workingCopy = null;
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	ICompilationUnit workingCopy1 = null;
	try {
		workingCopy1 = getWorkingCopy(
			"/Reconciler15/src/test/I.java",
			"package test;\n"+
			"public interface I {\n"+
			"	<T extends I> void foo(T t);\n"+
			"}\n",
			owner,
			null /*no problem requestor*/
		);
		
		this.problemRequestor =  new ProblemRequestor();
		this.workingCopy = getWorkingCopy("Reconciler15/src/test/X.java", "", owner, this.problemRequestor);
		setWorkingCopyContents(
			"package test;\n"+
			"public class X implements I {\n"+
			"	public <T extends I> void foo(T t) {\n"+
			"	}\n"+
			"}\n"
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, owner, null);

		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"----------\n"
		);
	} finally {
		if (workingCopy1 != null) {
			workingCopy1.discardWorkingCopy();
		}
	}
}
/*
 * Ensures that a working copy with a type with a dollar name can be reconciled without errors.
 * (regression test for bug 117121 Can't create class called A$B in eclipse)
 */
public void testTypeWithDollarName() throws CoreException {
	this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
	this.workingCopy = null;
	try {
		String contents =
			"package p1;\n" +
			"public class Y$Z {\n" +
			"}";
		createFile(
			"/Reconciler/src/p1/Y$Z.java", 
			contents
		);
		this.problemRequestor =  new ProblemRequestor();
		this.workingCopy = getCompilationUnit("Reconciler/src/p1/Y$Z.java").getWorkingCopy(new WorkingCopyOwner() {}, this.problemRequestor, null);
		
		this.problemRequestor.initialize(contents.toCharArray());
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, true, null, null);
		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"----------\n"
		);
	} finally {
		deleteFile("/Reconciler/src/p1/Y$Z.java");
	}
}
/*
 * Ensures that a varargs method can be referenced from another working copy.
 */
public void testVarargs() throws CoreException {
	this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
	this.workingCopy = null;
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	ICompilationUnit workingCopy1 = null;
	try {
		workingCopy1 = getWorkingCopy(
			"/Reconciler15/src/test/X.java",
			"package test;\n"+
			"public class X {\n"+
			"	void bar(String ... args) {}\n"+
			"}\n",
			owner,
			null /*no problem requestor*/
		);
		
		this.problemRequestor =  new ProblemRequestor();
		this.workingCopy = getWorkingCopy("Reconciler15/src/test/Y.java", "", owner, this.problemRequestor);
		setWorkingCopyContents(
			"package test;\n"+
			"public class Y {\n"+
			"	void foo(){\n"+
			"		X someX = new X();\n"+
			"		someX.bar(\"a\", \"b\");\n"+
			"	}\n"+
			"}\n"
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, owner, null);

		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"----------\n"
		);
	} finally {
		if (workingCopy1 != null) {
			workingCopy1.discardWorkingCopy();
		}
	}
}

/**
 * Bug 114338:[javadoc] Reconciler reports wrong javadoc warning (missing return type)
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=114338"
 *
 */
public void testBug114338() throws CoreException {
	// Set initial CU content
	setWorkingCopyContents(
		"package p1;\n" +
		"public class X {\n" + 
		"	/**\n" + 
		"	 * @return a\n" + 
		"	 */\n" + 
		"	boolean get() {\n" + 
		"		return false;\n" + 
		"	}\n" + 
		"}");
	this.workingCopy.reconcile(AST.JLS3, true, this.wcOwner, null);
	assertProblems(
		"Unexpected problems",
		"----------\n" + 
		"----------\n"
	);

	// Modify content
	String contents =
		"package p1;\n" +
		"public class X {\n" + 
		"	/**\n" + 
		"	 * @return boolean\n" + 
		"	 */\n" + 
		"	boolean get() {\n" + 
		"		return false;\n" + 
		"	}\n" + 
		"}";
	setWorkingCopyContents(contents);
	this.workingCopy.reconcile(AST.JLS3, true, this.wcOwner, null);
	assertProblems(
		"Unexpected problems",
		"----------\n" + 
		"----------\n"
	);
}

/**
 * Bug 36032:[plan] JavaProject.findType() fails to find second type in source file
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=36032"
 */
public void testBug36032a() throws CoreException, InterruptedException {
	try {
		// Resources creation
		createJavaProject("P", new String[] {""}, new String[] {"JCL_LIB"}, "bin");
		String source = 
			"public class Test {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		new SFoo().foo();\n" + 
			"	}\n" + 
			"}\n";
		this.createFile(
			"/P/Foo.java", 
			"class SFoo { void foo() {} }\n"
		);
		this.createFile(
			"/P/Test.java", 
			source
		);
		
		// Get compilation unit and reconcile it
		char[] sourceChars = source.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopy = getCompilationUnit("/P/Test.java").getWorkingCopy(new WorkingCopyOwner() {}, problemRequestor, null);
		this.workingCopy.getBuffer().setContents(source);
		this.workingCopy.reconcile(AST.JLS3, true, null, null);
		assertNoProblem(sourceChars, this.workingCopy);

		// Add new secondary type
		this.createFile(
			"/P/Bar.java", 
			"class SBar{ void bar() {} }\n"
		);
		source = 
			"public class Test {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		new SFoo().foo();\n" + 
			"		new SBar().bar();\n" + 
			"	}\n" + 
			"}\n";
		
		// Reconcile with modified source
		sourceChars = source.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopy.getBuffer().setContents(source);
		this.workingCopy.reconcile(AST.JLS3, true, null, null);
		assertNoProblem(sourceChars, this.workingCopy);
	} finally {
		deleteProject("P");
	}
}
public void testBug36032b() throws CoreException, InterruptedException {
	try {
		// Resources creation
		createJavaProject("P", new String[] {""}, new String[] {"JCL_LIB"}, "bin");
		String source = 
			"public class Test {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		new SFoo().foo();\n" + 
			"		new SBar().bar();\n" + 
			"	}\n" + 
			"}\n";
		createFile(
			"/P/Foo.java", 
			"class SFoo { void foo() {} }\n"
		);
		createFile(
			"/P/Test.java", 
			source
		);
		createFile(
			"/P/Bar.java", 
			"class SBar{ void bar() {} }\n"
		);
		
		// Get compilation unit and reconcile it
		char[] sourceChars = source.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopy = getCompilationUnit("/P/Test.java").getWorkingCopy(new WorkingCopyOwner() {}, this.problemRequestor, null);
		this.workingCopy.getBuffer().setContents(source);
		this.workingCopy.reconcile(AST.JLS3, true, null, null);
		assertNoProblem(sourceChars, this.workingCopy);

		// Delete secondary type => should get a problem
		waitUntilIndexesReady();
		deleteFile("/P/Bar.java");
		this.problemRequestor.initialize(source.toCharArray());
		this.workingCopy.reconcile(AST.JLS3, true, null, null);
		assertEquals("Working copy should not find secondary type 'Bar'!", 1, this.problemRequestor.problemCount);
		assertProblems("Working copy should have problem!",
			"----------\n" +
			"1. ERROR in /P/Test.java (at line 4)\n" +
			"	new SBar().bar();\n" +
			"	    ^^^^\n" +
			"SBar cannot be resolved to a type\n" +
			"----------\n"
		);

		// Fix the problem
		source = 
			"public class Test {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		new SFoo().foo();\n" + 
			"	}\n" + 
			"}\n";
		sourceChars = source.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopy.getBuffer().setContents(source);
		this.workingCopy.reconcile(AST.JLS3, true, null, null);
		assertNoProblem(sourceChars, this.workingCopy);
	} finally {
		deleteProject("P");
	}
}
// Secondary types used through multiple projects
public void testBug36032c() throws CoreException, InterruptedException {
	try {
		// Create first project
		createJavaProject("P1", new String[] {""}, new String[] {"JCL_LIB"}, "bin");
		createFolder("/P1/test");
		createFile(
			"/P1/test/Foo.java", 
			"package test;\n" +
			"class Secondary{ void foo() {} }\n"
		);
		createFile(
			"/P1/test/Test1.java", 
			"package test;\n" +
			"public class Test1 {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		new Secondary().foo();\n" + 
			"	}\n" + 
			"}\n"
		);

		// Create second project
		createJavaProject("P2", new String[] {""}, new String[] {"JCL_LIB"}, new String[] { "/P1" }, "bin");
		String source = 
			"package test;\n" +
			"public class Test2 {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		new Secondary().foo();\n" + 
			"	}\n" + 
			"}\n";
		createFolder("/P2/test");
		createFile(
			"/P2/test/Test2.java", 
			source
		);
		
		// Get compilation unit and reconcile it => expect no error
		char[] sourceChars = source.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopy = getCompilationUnit("/P2/test/Test2.java").getWorkingCopy(new WorkingCopyOwner() {}, this.problemRequestor, null);
		this.workingCopy.getBuffer().setContents(source);
		this.workingCopy.reconcile(AST.JLS3, true, null, null);
		assertNoProblem(sourceChars, this.workingCopy);
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}
/**
 * Bug 118823: [model] Secondary types cache not reset while removing _all_ secondary types from CU
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=118823"
 */
public void testBug118823() throws CoreException, InterruptedException, IOException {
	try {
		// Resources creation
		createJavaProject("P1", new String[] {""}, new String[] {"JCL_LIB"}, "bin");
		String source = "class Test {}\n";
		createFile(
			"/P1/Test.java", 
			source
		);
		createJavaProject("P2", new String[] {""}, new String[] {"JCL_LIB"}, new String[] { "/P1" }, "bin");
		String source2 = 
			"class A {\n" +
			"	Secondary s;\n" +
			"}\n";
		createFile(
			"/P2/A.java",
			source2
		);
		waitUntilIndexesReady();
		this.workingCopies = new ICompilationUnit[2];
		this.wcOwner = new WorkingCopyOwner() {};

		// Get first working copy and verify that there's no error
		char[] sourceChars = source.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopies[0] = getCompilationUnit("/P1/Test.java").getWorkingCopy(new WorkingCopyOwner() {}, this.problemRequestor, null);
		assertNoProblem(sourceChars, this.workingCopies[0]);

		// Get second working copy and verify that there's one error (missing secondary type)
		this.problemRequestor.initialize(source2.toCharArray());
		this.workingCopies[1] = getCompilationUnit("/P2/A.java").getWorkingCopy(new WorkingCopyOwner() {}, this.problemRequestor, null);
		assertEquals("Working copy should not find secondary type 'Secondary'!", 1, this.problemRequestor.problemCount);
		assertProblems("Working copy should have problem!",
			"----------\n" +
			"1. ERROR in /P2/A.java (at line 2)\n" +
			"	Secondary s;\n" +
			"	^^^^^^^^^\n" +
			"Secondary cannot be resolved to a type\n" +
			"----------\n"
		);

		// Delete file and recreate it with secondary
		final String source1 = 
			"public class Test {}\n" + 
			"class Secondary{}\n";
		getWorkspace().run(
			new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					deleteFile("/P1/Test.java");
					createFile(
						"/P1/Test.java", 
						source1
					);
				}
			},
			null
		);

		// Get first working copy and verify that there's still no error
		sourceChars = source1.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopies[0].getBuffer().setContents(source1);
		this.workingCopies[0].reconcile(AST.JLS3,
			true, // force problem detection to see errors if any
			null,	// do not use working copy owner to not use working copies in name lookup
			null);
		assertNoProblem(sourceChars, this.workingCopies[0]);

		// Get second working copy and verify that there's any longer error
		sourceChars = source2.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopies[1].getBuffer().setContents(source2);
		this.workingCopies[1].reconcile(AST.JLS3,
			true, // force problem detection to see errors if any
			null,	// do not use working copy owner to not use working copies in name lookup
			null);
		assertNoProblem(sourceChars, this.workingCopies[1]);
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}
public void testBug118823b() throws CoreException, InterruptedException {
	try {
		// Resources creation
		createJavaProject("P1", new String[] {""}, new String[] {"JCL_LIB"}, "bin");
		String source1 = "class Test {}\n";
		createFile(
			"/P1/Test.java", 
			source1
		);
		createJavaProject("P2", new String[] {""}, new String[] {"JCL_LIB"}, new String[] { "/P1" }, "bin");
		String source2 = 
			"class A {\n" +
			"	Secondary s;\n" +
			"}\n";
		createFile(
			"/P2/A.java",
			source2
		);
		waitUntilIndexesReady();
		this.workingCopies = new ICompilationUnit[2];

		// Get first working copy and verify that there's no error
		char[] sourceChars = source1.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopies[0] = getCompilationUnit("/P1/Test.java").getWorkingCopy(new WorkingCopyOwner() {}, this.problemRequestor, null);
		assertNoProblem(sourceChars, this.workingCopies[0]);

		// Get second working copy and verify that there's one error (missing secondary type)
		this.problemRequestor.initialize(source2.toCharArray());
		this.workingCopies[1] = getCompilationUnit("/P2/A.java").getWorkingCopy(new WorkingCopyOwner() {}, this.problemRequestor, null);
		assertEquals("Working copy should not find secondary type 'Secondary'!", 1, this.problemRequestor.problemCount);
		assertProblems("Working copy should have problem!",
			"----------\n" +
			"1. ERROR in /P2/A.java (at line 2)\n" +
			"	Secondary s;\n" +
			"	^^^^^^^^^\n" +
			"Secondary cannot be resolved to a type\n" +
			"----------\n"
		);

		// Modify first working copy and verify that there's still no error
		source1 = 
			"public class Test {}\n" + 
			"class Secondary{}\n";
		sourceChars = source1.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopies[0].getBuffer().setContents(source1);
		this.workingCopies[0].reconcile(AST.JLS3,
			true, // force problem detection to see errors if any
			null,	// do not use working copy owner to not use working copies in name lookup
			null);
		this.workingCopies[0].commitWorkingCopy(true, null);
		assertNoProblem(sourceChars, this.workingCopies[0]);

		// Get second working copy and verify that there's any longer error
		sourceChars = source2.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopies[1].getBuffer().setContents(source2);
		this.workingCopies[1].reconcile(AST.JLS3,
			true, // force problem detection to see errors if any
			null,	// do not use working copy owner to not use working copies in name lookup
			null);
		assertNoProblem(sourceChars, this.workingCopies[1]);
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}
public void testBug118823c() throws CoreException, InterruptedException {
	try {
		// Resources creation
		createJavaProject("P1", new String[] {""}, new String[] {"JCL_LIB"}, "bin");
		String source1 = "class Test {}\n";
		createFile(
			"/P1/Test.java", 
			source1
		);
		createJavaProject("P2", new String[] {""}, new String[] {"JCL_LIB"}, new String[] { "/P1" }, "bin");
		String source2 = 
			"class A {\n" +
			"	Secondary s;\n" +
			"}\n";
		createFile(
			"/P2/A.java",
			source2
		);
		waitUntilIndexesReady();
		this.workingCopies = new ICompilationUnit[2];
		this.wcOwner = new WorkingCopyOwner() {};

		// Get first working copy and verify that there's no error
		char[] sourceChars = source1.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopies[0] = getCompilationUnit("/P1/Test.java").getWorkingCopy(new WorkingCopyOwner() {}, this.problemRequestor, null);
		assertNoProblem(sourceChars, this.workingCopies[0]);

		// Get second working copy and verify that there's one error (missing secondary type)
		this.problemRequestor.initialize(source2.toCharArray());
		this.workingCopies[1] = getCompilationUnit("/P2/A.java").getWorkingCopy(new WorkingCopyOwner() {}, this.problemRequestor, null);
		assertEquals("Working copy should not find secondary type 'Secondary'!", 1, this.problemRequestor.problemCount);
		assertProblems("Working copy should have problem!",
			"----------\n" +
			"1. ERROR in /P2/A.java (at line 2)\n" +
			"	Secondary s;\n" +
			"	^^^^^^^^^\n" +
			"Secondary cannot be resolved to a type\n" +
			"----------\n"
		);

		// Delete file and recreate it with secondary
		deleteFile("/P1/Test.java");
		source1 = 
			"public class Test {}\n" + 
			"class Secondary{}\n";
		createFile(
			"/P1/Test.java", 
			source1
		);

		// Get first working copy and verify that there's still no error
		sourceChars = source1.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopies[0].getBuffer().setContents(source1);
		this.workingCopies[0].reconcile(AST.JLS3,
			true, // force problem detection to see errors if any
			null,	// do not use working copy owner to not use working copies in name lookup
			null);
		this.workingCopies[0].commitWorkingCopy(true, null);
		assertNoProblem(sourceChars, this.workingCopies[0]);

		// Get second working copy and verify that there's any longer error
		sourceChars = source2.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopies[1].getBuffer().setContents(source2);
		this.workingCopies[1].reconcile(AST.JLS3,
			true, // force problem detection to see errors if any
			null,	// do not use working copy owner to not use working copies in name lookup
			null);
		assertNoProblem(sourceChars, this.workingCopies[1]);
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}
}
