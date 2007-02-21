/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.apt.ide.dispatch;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.processing.Processor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.apt.core.internal.AnnotationProcessorFactoryLoader;
import org.eclipse.jdt.apt.core.internal.IServiceFactory;
import org.eclipse.jdt.apt.core.internal.util.FactoryPath;
import org.eclipse.jdt.apt.core.internal.util.FactoryPath.Attributes;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseAnnotationProcessorManager;
import org.eclipse.jdt.internal.compiler.apt.dispatch.ProcessorInfo;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.core.CompilationUnitProblemFinder;
import org.eclipse.jdt.internal.core.builder.ICompilationUnitLocator;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * Java 6 annotation processor manager used when compiling within the IDE. 
 * @see org.eclipse.jdt.internal.compiler.apt.dispatch.BatchAnnotationProcessorManager
 */
public class IdeAnnotationProcessorManager extends BaseAnnotationProcessorManager {
	
	private IJavaProject _javaProject;
	private ICompilationUnitLocator _cuLocator;
	private Map<IServiceFactory, FactoryPath.Attributes> _processors;
	private Iterator<Entry<IServiceFactory, Attributes>> _processorIter;

	/**
	 * Initialize the processor manager for a particular project.  It is an error
	 * to initialize a manager more than once.
	 * 
	 * @param abstractImageBuilder must be an instanceof AbstractImageBuilder.
	 * (But it can't be prototyped that way because the abstract base class must
	 * compile without Eclipse platform code.)
	 * 
	 * @param javaProject must be an instanceof IJavaProject.  (But it can't be
	 * prototyped that way because the abstract base class must compile without
	 * Eclipse platform code.)
	 */
	@Override
	public void configureFromPlatform(Compiler compiler, Object compilationUnitLocator, Object javaProject) {
		_javaProject = (IJavaProject) javaProject;
		_cuLocator = (ICompilationUnitLocator) compilationUnitLocator;
		if (null != _processingEnv) {
			throw new IllegalStateException(
					"Calling configure() more than once on an AnnotationProcessorManager is not supported"); //$NON-NLS-1$
		}
		// If it's a CompilationUnitProblemFinder, we're in reconcile phase.  Else it's build.
		if (compiler instanceof CompilationUnitProblemFinder) {
			_processingEnv = new IdeReconcileProcessingEnvImpl(this, _javaProject, compiler);
		} else {
			_processingEnv = new IdeBuildProcessingEnvImpl(this, _javaProject, compiler);
		}
	}

	/**
	 * If this project has a ProcessorPath defined, use it.  Else, construct
	 * one from the classpath.
	 */
	@Override
	public ProcessorInfo discoverNextProcessor() {
		// _processorIter gets initialized the first time through processAnnotations()
		if (_processorIter.hasNext()) {
			Entry<IServiceFactory, Attributes> entry = _processorIter.next();
			Processor p;
			try {
				p = (Processor)entry.getKey().newInstance();
				p.init(_processingEnv);
				ProcessorInfo pi = new ProcessorInfo(p);
				return pi;
			} catch (CoreException e) {
				Util.log(e, "Unable to create instance of annotation processor " + entry.getKey()); //$NON-NLS-1$
			}
		}
		return null;
	}

	@Override
	public void reportProcessorException(Processor p, Exception e) {
		Util.log(e, "Exception thrown by Java annotation processor " + p); //$NON-NLS-1$
	}

	/**
	 * @return an ICompilationUnit corresponding to the specified file.  In IDE mode this
	 * will be backed by an org.eclipse.jdt.internal.core.builder.SourceFile.
	 */
	public ICompilationUnit findCompilationUnit(IFile file) {
		return _cuLocator.fromIFile(file);
	}

	/**
	 * In IDE mode, we are able to determine whether there are no processors.  If that's the case,
	 * then we can avoid doing the work of walking the ASTs to search for annotations.  We still
	 * need to clean up no-longer-generated files when the factory path is changed, but the best
	 * way to do that is to force a clean build.
	 * @see BaseAnnotationProcessorManager#processAnnotations(CompilationUnitDeclaration[], boolean)
	 */
	@Override
	public void processAnnotations(CompilationUnitDeclaration[] units, boolean isLastRound) {
		if (null == _processors ) {
			_processors = AnnotationProcessorFactoryLoader.getLoader().getJava6FactoriesAndAttributesForProject(_javaProject);
			_processorIter = _processors.entrySet().iterator();
		}
		if (!_processors.isEmpty()) {
			super.processAnnotations(units, isLastRound);
		}
	}

}
