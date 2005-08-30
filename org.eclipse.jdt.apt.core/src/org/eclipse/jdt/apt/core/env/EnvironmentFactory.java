/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    mkaufman@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.apt.core.env;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;

public class EnvironmentFactory {

	/**
	 * Return a processor environment for use outside of building or reconciling.
	 * Note that this environment does <b>NOT</b> support the Filer or Messager API,
	 * as it is to be used to perform type system navigation, not building.<p>
	 * 
	 * If either getFiler() or getMessager() are called, this environment
	 * will throw an UnsupportedOperationException.
	 * 
	 * @param compilationUnit the working copy for which the Environment object is to be created
	 * @param javaProject the java project that the working copy is in. 
	 * @return the created environment.
	 */
	public static AnnotationProcessorEnvironment getEnvironment(ICompilationUnit compilationUnit, IJavaProject javaProject )
	{
		//return ProcessorEnvImpl.newProcessorEnvironmentForReconcile( compilationUnit, javaProject );
		String unitName =  compilationUnit.getResource().getProjectRelativePath().toString();
		ASTNode node = BaseProcessorEnv.createDietAST( unitName, javaProject, compilationUnit, null );
       	BaseProcessorEnv env = new BaseProcessorEnv(
       			(CompilationUnit)node,
       			(IFile)compilationUnit.getResource(),
       			javaProject,
       			Phase.OTHER
       			);
       	return env;
	}
}
