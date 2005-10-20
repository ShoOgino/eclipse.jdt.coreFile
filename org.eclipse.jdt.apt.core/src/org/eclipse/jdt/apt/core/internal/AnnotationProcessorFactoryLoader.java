/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.apt.core.AptPlugin;
import org.eclipse.jdt.apt.core.internal.util.FactoryContainer;
import org.eclipse.jdt.apt.core.internal.util.FactoryPath;
import org.eclipse.jdt.apt.core.internal.util.FactoryPathUtil;
import org.eclipse.jdt.apt.core.internal.util.FactoryContainer.FactoryType;
import org.eclipse.jdt.apt.core.internal.util.FactoryPath.Attributes;
import org.eclipse.jdt.core.IJavaProject;

import com.sun.mirror.apt.AnnotationProcessorFactory;

/**
 * Stores annotation processor factories, and handles mapping from projects
 * to them.
 */
public class AnnotationProcessorFactoryLoader {
	
	/** Loader instance -- holds all workspace and project data */
	private static AnnotationProcessorFactoryLoader LOADER;
	
	private static boolean VERBOSE_LOAD = false;
	
	// Members -- workspace and project data	
	
	private final Map<IJavaProject, Map<AnnotationProcessorFactory, FactoryPath.Attributes>> _project2Factories = 
		new HashMap<IJavaProject, Map<AnnotationProcessorFactory, FactoryPath.Attributes>>();
    
	// Caches the iterative classloaders so that iterative processors
	// are not reloaded on every clean build, unlike batch processors 
	// which are.
	private final Map<IJavaProject, ClassLoader> _project2IterativeClassloaders = 
		new HashMap<IJavaProject, ClassLoader>();
    
	/** 
	 * Singleton
	 */
    public static synchronized AnnotationProcessorFactoryLoader getLoader() {
    	if ( LOADER == null )
    		LOADER = new AnnotationProcessorFactoryLoader();
    	return LOADER;
    }
    
    private AnnotationProcessorFactoryLoader() {
    	FactoryPathUtil.loadPluginFactories();
    }
    
    /**
     * Called when underlying preferences change
     */
    public synchronized void resetAll() {
    	_project2Factories.clear();
    	_project2IterativeClassloaders.clear();
    }
    
    /**
     * Called when doing a clean build -- resets
     * the classloaders for the batch processors
     */
    public synchronized void resetBatchProcessors(IJavaProject javaProj) {
    	// Only need to do a reset if we have batch processors
    	Map<AnnotationProcessorFactory, Attributes> factories = _project2Factories.get(javaProj);
    	if (factories == null) {
    		// Already empty
    		return;
    	}
    	boolean batchProcsFound = false;
    	for (Attributes attr : factories.values()) {
    		if (attr.runInBatchMode()) {
    			batchProcsFound = true;
    			break;
    		}
    	}
    	if (batchProcsFound) {
    		_project2Factories.remove(javaProj);
    	}
    }
    
    /**
     * @param jproj
     * @return order preserving map of annotation processor factories to their attributes.
     * The order the annotation processor factories respect the order of factory containers in 
     * <code>jproj</code>
     */
    public synchronized Map<AnnotationProcessorFactory, FactoryPath.Attributes> 
    	getFactoriesAndAttributesForProject(IJavaProject jproj){
    	
    	Map<AnnotationProcessorFactory, FactoryPath.Attributes> factories = _project2Factories.get(jproj);
    	if( factories != null )
    		return Collections.unmodifiableMap(factories);
    	
    	// Load the project
		FactoryPath fp = FactoryPathUtil.getFactoryPath(jproj);
		Map<FactoryContainer, FactoryPath.Attributes> containers = fp.getEnabledContainers(jproj);
		factories = loadFactories(containers, jproj);
		_project2Factories.put(jproj, factories);
		return Collections.unmodifiableMap(factories);
    	
    }
    
    /**
     * @param javaProj
     * @return <code>true</code> iff there are Annotation Processor Factories associated with 
     * the given project
     */
    public synchronized boolean hasFactoriesForProject(IJavaProject javaProj){
    	
    	Map<AnnotationProcessorFactory, FactoryPath.Attributes> factories = _project2Factories.get(javaProj);
    	if( factories != null && !factories.isEmpty() )
    		return true;
    	
    	// Load the project
		FactoryPath fp = FactoryPathUtil.getFactoryPath(javaProj);
		Map<FactoryContainer, FactoryPath.Attributes> containers = fp.getEnabledContainers(javaProj);
		factories = loadFactories(containers, javaProj);
		_project2Factories.put(javaProj, factories);
		return factories != null && !factories.isEmpty();
    }
    
    public synchronized List<AnnotationProcessorFactory> getFactoriesForProject( IJavaProject jproj ) {
    	
    	Map<AnnotationProcessorFactory, FactoryPath.Attributes> factoriesAndAttrs = 
    		getFactoriesAndAttributesForProject(jproj);
    	final List<AnnotationProcessorFactory> factories = 
    		new ArrayList<AnnotationProcessorFactory>(factoriesAndAttrs.keySet());
    	return Collections.unmodifiableList(factories);
    }
    
	/**
	 * @param containers an ordered map.
	 * @return order preserving map of annotation processor factories to their attributes. 
	 * The order of the factories respect the order of the containers.
	 */
	private Map<AnnotationProcessorFactory, FactoryPath.Attributes> loadFactories( Map<FactoryContainer, FactoryPath.Attributes> containers, IJavaProject project )
	{
		Map<AnnotationProcessorFactory, FactoryPath.Attributes> factoriesAndAttrs = 
			new LinkedHashMap<AnnotationProcessorFactory, FactoryPath.Attributes>(containers.size() * 4 / 3 + 1);
		
		// Need to use the cached classloader if we have one
		ClassLoader iterativeClassLoader = _project2IterativeClassloaders.get(project);
		if (iterativeClassLoader == null) {
			iterativeClassLoader = _createIterativeClassLoader(containers);
			_project2IterativeClassloaders.put(project, iterativeClassLoader);
		}
		
		ClassLoader batchClassLoader = _createBatchClassLoader(containers, iterativeClassLoader);
		
		for ( Map.Entry<FactoryContainer, FactoryPath.Attributes> entry : containers.entrySet() )
		{
			try {
				final FactoryContainer fc = entry.getKey();
				final FactoryPath.Attributes attr = entry.getValue();
				List<AnnotationProcessorFactory> factories;
				if (attr.runInBatchMode()) {
					factories = loadFactoryClasses(fc, batchClassLoader);
				}
				else {
					factories = loadFactoryClasses(fc, iterativeClassLoader);
				}
				for ( AnnotationProcessorFactory apf : factories )
					factoriesAndAttrs.put( apf, entry.getValue() );
			}
			catch (FileNotFoundException fnfe) {
				AptPlugin.log(fnfe, Messages.AnnotationProcessorFactoryLoader_jarNotFound + fnfe.getLocalizedMessage());
			}
			catch (IOException ioe) {
				AptPlugin.log(ioe, Messages.AnnotationProcessorFactoryLoader_ioError + ioe.getLocalizedMessage());
			}
		}
		return factoriesAndAttrs;
	}
	
	private List<AnnotationProcessorFactory> loadFactoryClasses( 
			FactoryContainer fc, ClassLoader classLoader )
			throws IOException
	{
		List<String> factoryNames = fc.getFactoryNames();
		List<AnnotationProcessorFactory> factories = new ArrayList<AnnotationProcessorFactory>( factoryNames.size() ); 
		for ( String factoryName : factoryNames )
		{
			AnnotationProcessorFactory factory;
			if ( fc.getType() == FactoryType.PLUGIN )
				factory = FactoryPathUtil.getFactoryFromPlugin( factoryName );
			else
				factory = loadFactoryFromClassLoader( factoryName, classLoader );
			
			if ( factory != null )
				factories.add( factory );
		}
		return factories;
	}
	
	private AnnotationProcessorFactory loadFactoryFromClassLoader( String factoryName, ClassLoader cl )
	{
		AnnotationProcessorFactory f = null;
		try
		{
			Class c = cl.loadClass( factoryName );
			f = (AnnotationProcessorFactory)c.newInstance();
		}
		catch( Exception e )
		{
			AptPlugin.log(e, "Could not load annotation processor factory " + factoryName); //$NON-NLS-1$
		}
		catch ( NoClassDefFoundError ncdfe )
		{
			// **DO NOT REMOVE THIS CATCH BLOCK***
			// This error indicates a problem with the factory path specified 
			// by the project, and it needs to be caught and reported!
			AptPlugin.log(ncdfe, "Could not load annotation processor factory " + factoryName); //$NON-NLS-1$
		}
		return f;
	}
	
	/**
	 * @param containers an ordered map.
	 */
	private ClassLoader _createIterativeClassLoader( Map<FactoryContainer, FactoryPath.Attributes> containers )
	{
		ArrayList<URL> urlList = new ArrayList<URL>( containers.size() );
		for (Map.Entry<FactoryContainer, FactoryPath.Attributes> entry : containers.entrySet()) {
			FactoryPath.Attributes attr = entry.getValue();
			FactoryContainer fc = entry.getKey();
			if (!attr.runInBatchMode() && fc instanceof JarFactoryContainer) {
				try
				{
					JarFactoryContainer jfc = (JarFactoryContainer)fc;
					URL u = jfc.getJarFileURL();
					urlList.add( u );
				}
				catch ( MalformedURLException mue )
				{
					AptPlugin.log(mue, "Could not create ClassLoader for " + fc); //$NON-NLS-1$
				}
			}
		}
		
		ClassLoader cl;
		if ( urlList.size() > 0 ) {
			URL[] urls = urlList.toArray(new URL[urlList.size()]);
			cl = new URLClassLoader( urls, AnnotationProcessorFactoryLoader.class.getClassLoader() );
		}
		else {
			cl = AnnotationProcessorFactoryLoader.class.getClassLoader();
		}
		return cl;
	}
	
	private ClassLoader _createBatchClassLoader( 
			Map<FactoryContainer, FactoryPath.Attributes> containers, 
			ClassLoader iterativeClassLoader) {
		
		ArrayList<URL> urlList = new ArrayList<URL>( containers.size() );
		for (Map.Entry<FactoryContainer, FactoryPath.Attributes> entry : containers.entrySet()) {
			FactoryPath.Attributes attr = entry.getValue();
			FactoryContainer fc = entry.getKey();
			if (attr.runInBatchMode() && fc instanceof JarFactoryContainer) {
				try
				{
					JarFactoryContainer jfc = (JarFactoryContainer)fc;
					URL u = jfc.getJarFileURL();
					urlList.add( u );
				}
				catch ( MalformedURLException mue )
				{
					AptPlugin.log(mue, "Could not create ClassLoader for " + fc); //$NON-NLS-1$
				}
			}
		}
		
		ClassLoader cl;
		if ( urlList.size() > 0 ) {
			URL[] urls = urlList.toArray(new URL[urlList.size()]);
			// This needs to be a child of the iterative class loader
			cl = new URLClassLoader( urls, iterativeClassLoader );
		}
		else {
			cl = iterativeClassLoader;
		}
		return cl;
		
	}
}
