/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal.env;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.apt.core.env.EclipseAnnotationProcessorEnvironment;
import org.eclipse.jdt.apt.core.env.Phase;
import org.eclipse.jdt.apt.core.internal.AptPlugin;
import org.eclipse.jdt.apt.core.internal.declaration.EclipseMirrorObject;
import org.eclipse.jdt.apt.core.internal.declaration.TypeDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.env.MessagerImpl.Severity;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.apt.core.internal.util.Visitors.AnnotationVisitor;
import org.eclipse.jdt.apt.core.util.EclipseMessager;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.BuildContext;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.ReconcileContext;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;

import com.sun.mirror.apt.AnnotationProcessorListener;
import com.sun.mirror.apt.Filer;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

/** Base environment to be used during reconcile or build */ 
public abstract class AbstractCompilationEnv 
	extends BaseProcessorEnv 
	implements EclipseAnnotationProcessorEnvironment{
	
	private Set<AnnotationProcessorListener> _listeners = null;
	
	protected List<APTProblem> _problems = new ArrayList<APTProblem>();
	private boolean _isClosed = false;
	
	EnvCallback _callback;

    private Set<IFile> _allGeneratedSourceFiles = new HashSet<IFile>();
    private Set<IFile> _modifiedGeneratedSourceFiles = new HashSet<IFile>();	

    /**
	 * Currently open dom pipeline, used to request type bindings.
	 */
	protected ASTRequestor _requestor;
	
	public static interface EnvCallback {
		public void run(AbstractCompilationEnv env);
	}
	
	public static void newReconcileEnv(ReconcileContext reconcileContext,  EnvCallback callback)
	{
		assert reconcileContext != null : "reconcile context is null"; //$NON-NLS-1$
		ReconcileEnv env = ReconcileEnv.newEnv(reconcileContext);
		env._callback = callback;
		env.openPipeline();
	}
    
    public static void newBuildEnv(
    		BuildContext[] filesWithAnnotations,
    		final BuildContext[] additionalFiles,
    		IJavaProject javaProj,
    		EnvCallback callback)
    {
    	assert filesWithAnnotations != null : "missing files"; //$NON-NLS-1$    	
    
		// note, we are not reading any files.
		BuildEnv env = new BuildEnv(filesWithAnnotations, additionalFiles, javaProj);
		env._callback = callback;
		env.createASTs(filesWithAnnotations);
    }
	
	AbstractCompilationEnv(
			CompilationUnit compilationUnit,
			IFile file,
			IJavaProject javaProj,
			Phase phase)
	{
		super(compilationUnit, file, javaProj, phase);
	}
	
	@Override
	protected ITypeBinding getTypeBindingFromKey(String key, ICompilationUnit unit) {
		return (ITypeBinding)_requestor.createBindings(new String[] {key})[0];
	}

	public void addListener(AnnotationProcessorListener listener)
    {
		checkValid();
        if(_listeners == null )
			_listeners = new HashSet<AnnotationProcessorListener>();
		_listeners.add(listener);
    }

    public void removeListener(AnnotationProcessorListener listener)
    {
		checkValid();
        if( _listeners == null ) return;
		_listeners.remove(listener);
    }

	public Set<AnnotationProcessorListener> getProcessorListeners()
	{
		if( _listeners == null )
			return Collections.emptySet();
		// Return a copy, to avoid ConcurrentModificationException if a listener
		// removes itself in response to the callback.
		return new HashSet<AnnotationProcessorListener>(_listeners);
	}
	
	public Map<String, String> getOptions()
    {
        final HashMap<String, String> options = new HashMap<String, String>(_options);
		options.put("phase", getPhase().toString()); //$NON-NLS-1$
		return options;
    }
	
	abstract public CompilationUnit getASTFrom(final IFile file);
	
	public CompilationUnit getAST(){
		return _astRoot;
	}
	
	public EclipseMessager getMessager()
    {
		checkValid();
		return new MessagerImpl(this);
	}
	
	abstract void addMessage(
			IFile resource, 
		    int start, 
			int end,
            Severity severity, 
            String msg, 
            int line,
            String[] arguments);
	
	public List<? extends CategorizedProblem> getProblems(){
		checkValid();
		if( !_problems.isEmpty() )
			EnvUtil.updateProblemLength(_problems, getAstCompilationUnit());
		return _problems;
	}
	
	APTProblem createProblem(
	    		IFile resource, 
			    int start, 
				int end,
	            Severity severity, 
	            String msg, 
	            int line,
	            String[] arguments)
    {	 
    	// end-1 since IProblem ending offsets are inclusive but DOM layer
    	// ending offsets are exclusive.
    	final APTProblem newProblem = 
        	new APTProblem(msg, severity, resource, start, end-1, line, arguments);
    	return newProblem;
    }
	
	public abstract Filer getFiler();
	
	public void addGeneratedSourceFile( IFile f, boolean contentsChanged ) {
		if (!f.toString().endsWith(".java")) { //$NON-NLS-1$
			throw new IllegalArgumentException("Source files must be java source files, and end with .java"); //$NON-NLS-1$
		}
		
		boolean addedToAll = _allGeneratedSourceFiles.add(f);
		boolean addedToMod = false;
		if (contentsChanged)
			addedToMod = _modifiedGeneratedSourceFiles.add(f);
		if (AptPlugin.DEBUG_COMPILATION_ENV) {
			AptPlugin.trace("add generated file " + f + " to env " + this + //$NON-NLS-1$ //$NON-NLS-2$
					"; addToAll = " + addedToAll + "; addToMod = " + addedToMod + //$NON-NLS-1$ //$NON-NLS-2$
					"; contentsChanged = " + contentsChanged); //$NON-NLS-1$
		}
	}
	
	public void addGeneratedNonSourceFile(final IFile file) {
		_allGeneratedSourceFiles.add(file);
	}
	
    public Set<IFile> getAllGeneratedFiles() { 
    	return _allGeneratedSourceFiles; 
    }
    
    public Set<IFile> getModifiedGeneratedFiles() { 
    	return _modifiedGeneratedSourceFiles; 
    }

	/**
	 * @return true iff source files has been generated.
	 *         Always return false when this environment is closed.
	 */
	public boolean hasGeneratedSourceFiles(){ return !_allGeneratedSourceFiles.isEmpty();  }


	/**
	 * @return all annotation types in the current compilation unit.
	 */
	public Map<String, AnnotationTypeDeclaration> getAnnotationTypes()
    {
    	checkValid();
    	final List<Annotation> instances = new ArrayList<Annotation>();
		final Map<String, AnnotationTypeDeclaration> decls = 
			new HashMap<String, AnnotationTypeDeclaration>();
		final AnnotationVisitor visitor = new AnnotationVisitor(instances);
		_astRoot.accept(visitor);
			
		for (int instanceIndex=0, size = instances.size(); instanceIndex < size; instanceIndex++) {
			final Annotation instance = instances.get(instanceIndex);
			final ITypeBinding annoType = instance.resolveTypeBinding();
			if (annoType == null)
				continue;
			final TypeDeclarationImpl decl = 
				Factory.createReferenceType(annoType, this);
			if (decl.kind() == EclipseMirrorObject.MirrorKind.TYPE_ANNOTATION){
				final AnnotationTypeDeclaration annoDecl = (AnnotationTypeDeclaration)decl;
				decls.put(annoDecl.getQualifiedName(), annoDecl);
			}
		}
		
		return decls;
    }
	
	/* package */ void checkValid()
	{
		if( _isClosed )
			throw new IllegalStateException("Environment has expired"); //$NON-NLS-1$
	}
	
	// Call this after each file; cf. BuildEnv#beginFileProcessing()
	protected void completedProcessing() {
		_modifiedGeneratedSourceFiles.clear();
	}
	
	public void close(){
		if (isClosed()) 
			return; 
		if(_listeners != null)
			_listeners.clear();
		_problems = null;
		_typeCache.clear();
		_packageRootsCache = null;
		_isClosed = true;
		_callback = null;
		_requestor = null;
		_allGeneratedSourceFiles = null;
		_modifiedGeneratedSourceFiles = null;
		if (AptPlugin.DEBUG_COMPILATION_ENV) AptPlugin.trace(
				"closed env " + this); //$NON-NLS-1$
	}
	
	boolean isClosed(){ return _isClosed; }

	/**
	 * Check typeName to ensure it doesn't contain any bogus characters.
	 * @param typeName
	 * @throws CoreException 
	 */
	@SuppressWarnings("unchecked")
	public void validateTypeName(String typeName) throws CoreException
	{
        Map<String, String> options = getJavaProject().getOptions(true);
        String sourceLevel = options.get(JavaCore.COMPILER_SOURCE);
        String complianceLevel = options.get(JavaCore.COMPILER_COMPLIANCE);
        IStatus status = JavaConventions.validateJavaTypeName(typeName, sourceLevel, complianceLevel);
        if (!status.isOK()) {
        	throw new CoreException(status);
        }
	}
}
