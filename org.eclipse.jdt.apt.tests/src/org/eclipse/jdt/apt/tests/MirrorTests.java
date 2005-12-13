/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    jgarms@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.apt.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.apt.tests.annotations.ProcessorTestStatus;
import org.eclipse.jdt.apt.tests.annotations.mirrortest.CodeExample;
import org.eclipse.jdt.apt.tests.annotations.mirrortest.MirrorTestAnnotationProcessor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.tests.builder.Problem;
import org.eclipse.jdt.core.tests.util.Util;

public class MirrorTests extends APTTestBase {
	
	public MirrorTests(final String name) {
		super( name );
	}

	public static Test suite() {
		return new TestSuite( MirrorTests.class );
	}
	
	/**
	 * Runs the MirrorTestAnnotationProcessor, which contains
	 * the actual tests
	 */
	public void testMirror() throws Exception {
		MirrorTestAnnotationProcessor._processRun = false;
		
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();
		
		String code = CodeExample.CODE;

		env.addClass( 
				srcRoot, 
				CodeExample.CODE_PACKAGE, 
				CodeExample.CODE_CLASS_NAME,
				code );

		fullBuild( project.getFullPath() );

		expectingNoProblems();
		
		assertTrue("Processor was not run", MirrorTestAnnotationProcessor._processRun); //$NON-NLS-1$
		
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}
	
	public void testTypeParmaterAPI() throws Exception{
		final String projName = MirrorTests.class.getName() + "TypeParameter.Project"; //$NON-NLS-1$
		IPath projectPath = env.addProject( projName, "1.5" ); //$NON-NLS-1$
		env.addExternalJars( projectPath, Util.getJavaClassLibs() );

		env.removePackageFragmentRoot( projectPath, "" ); //$NON-NLS-1$
		env.addPackageFragmentRoot( projectPath, "src" ); //$NON-NLS-1$
		env.setOutputFolder( projectPath, "bin" ); //$NON-NLS-1$

		TestUtil.createAndAddAnnotationJar( env
			.getJavaProject( projectPath ) );
		IProject project = env.getProject( projName );
		IFolder srcFolder = project.getFolder( "src" );
		IPath srcRoot = srcFolder.getFullPath();

		String a1Code = "package pkg;\n" +
			"import org.eclipse.jdt.apt.tests.annotations.apitest.Common;\n" +
			"import java.lang.annotation.Annotation;\n" +
			"@Common\n" +
			"public class A1<T> {\n " +
			"   @Common\n" + 
			"   <A extends Annotation> A get(A a){ return a;}\n" +
			"}\n";
	
		final IPath a1Path = env.addClass( srcRoot, "pkg", "A1", a1Code ); //$NON-NLS-1$ //$NON-NLS-2$
		
		// Set some per-project preferences
		IJavaProject jproj = env.getJavaProject( projName );
		AptConfig.setEnabled(jproj, true);
		fullBuild( project.getFullPath() );
		expectingNoMarkers();
		expectingSpecificProblemsFor(a1Path, new Problem[]{
				new Problem("", "Type parameter 'T' belongs to org.eclipse.jdt.apt.core.internal.declaration.ClassDeclarationImpl A1", a1Path),
				new Problem("", "Type parameter 'A' belongs to org.eclipse.jdt.apt.core.internal.declaration.MethodDeclarationImpl get", a1Path)
				}
		);
	}
}
