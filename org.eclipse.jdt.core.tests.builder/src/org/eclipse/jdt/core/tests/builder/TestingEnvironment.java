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
package org.eclipse.jdt.core.tests.builder;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.core.*;

import org.eclipse.jdt.internal.core.JavaProject;
import java.io.*;
import java.util.*;

public class TestingEnvironment {
	private static final String testJarName = "buildertests.jar"; //$NON-NLS-1$
	private static final String testProjectName = "org.eclipse.jdt.core.tests.builder"; //$NON-NLS-1$
	
	private boolean fIsOpen = false;
	private boolean fWasBuilt = false;

	private IWorkspace fWorkspace = null;
	private Hashtable fProjects = null;

	private void addBuilderSpecs(String projectName) {
		try {
			IProject project = getProject(projectName);
			IProjectDescription description = project.getDescription();
			description.setNatureIds(new String[] { JavaCore.NATURE_ID });
			project.setDescription(description, null);
		} catch (CoreException e) {
			handleCoreException(e);
		}
	}

	/** Adds a binary class with the given contents to the
	 * given package in the workspace.  The package is created
	 * if necessary.  If a class with the same name already
	 * exists, it is replaced.  A workspace must be open,
	 * and the given class name must not end with ".class".
	 * Returns the path of the added class.
	 */
	public IPath addBinaryClass(IPath packagePath, String className, byte[] contents) {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		IPath classPath = packagePath.append(className + ".class"); //$NON-NLS-1$
		createFile(classPath, contents);
		return classPath;
	}
	
	/** Adds a binary class with the given contents to the
	 * given package in the workspace.  The package is created
	 * if necessary.  If a class with the same name already
	 * exists, it is replaced.  A workspace must be open,
	 * and the given class name must not end with ".class".
	 * Returns the path of the added class.
	 */
	public IPath addBinaryClass(IPath packageFragmentRootPath, String packageName, String className, byte[] contents) {
		/* make sure the package exists */
		if(packageName != null && packageName.length() >0){
			IPath packagePath = addPackage(packageFragmentRootPath, packageName);

			return addBinaryClass(packagePath, className, contents);
		} else {
			return addBinaryClass(packageFragmentRootPath, className, contents);
		}
			
	}
	
	/** Adds a class with the given contents to the given
	 * package in the workspace.  The package is created
	 * if necessary.  If a class with the same name already
	 * exists, it is replaced.  A workspace must be open,
	 * and the given class name must not end with ".java".
	 * Returns the path of the added class.
	 */
	public IPath addClass(IPath packagePath, String className, String contents) {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		IPath classPath = packagePath.append(className + ".java"); //$NON-NLS-1$
		try {
			createFile(classPath, contents.getBytes("UTF8")); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			checkAssertion("e1", false); //$NON-NLS-1$
		}
		return classPath;
	}
	
	/** Adds a class with the given contents to the given
	 * package in the workspace.  The package is created
	 * if necessary.  If a class with the same name already
	 * exists, it is replaced.  A workspace must be open,
	 * and the given class name must not end with ".java".
	 * Returns the path of the added class.
	 */
	public IPath addClass(IPath packageFragmentRootPath, String packageName, String className, String contents) {
		/* make sure the package exists */
		if(packageName != null && packageName.length() >0){
			IPath packagePath = addPackage(packageFragmentRootPath, packageName);

			return addClass(packagePath, className, contents);
		} else {
			return addClass(packageFragmentRootPath, className, contents);
		}
	}

	/** Adds a package to the given package fragment root
	 * in the workspace.  The package fragment root is created
	 * if necessary.  If a package with the same name already
	 * exists, it is not replaced.  A workspace must be open.
	 * Returns the path of the added package.
	 */
	public IPath addPackage(IPath packageFragmentRootPath, String packageName) {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		IPath path =
			packageFragmentRootPath.append(packageName.replace('.', IPath.SEPARATOR));
		createFolder(path);
		return path;
	}

	public IPath addPackageFragmentRoot(IPath projectPath, String sourceFolderName) throws JavaModelException {
		return addPackageFragmentRoot(projectPath, sourceFolderName, null, null); //$NON-NLS-1$
	}

	/** Adds a package fragment root to the workspace.  If
	 * a package fragment root with the same name already
	 * exists, it is not replaced.  A workspace must be open.
	 * Returns the path of the added package fragment root.
	 */
	public IPath addPackageFragmentRoot(IPath projectPath, String sourceFolderName, IPath[] exclusionPatterns, String specificOutputLocation) throws JavaModelException {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		IPath path = getPackageFragmentRootPath(projectPath, sourceFolderName);
		createFolder(path);
		IPath outputPath = null;
		if (specificOutputLocation != null) {
			outputPath = getPackageFragmentRootPath(projectPath, specificOutputLocation);
			createFolder(outputPath);
		}
		IClasspathEntry entry = JavaCore.newSourceEntry(path, exclusionPatterns == null ? new Path[0] : exclusionPatterns, outputPath);
		addEntry(projectPath, entry);
		return path;
	}
	
	public IPath addProject(String projectName){
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		IProject project = createProject(projectName);
		return project.getFullPath();
	}

	public void addRequiredProject(IPath projectPath, IPath requiredProjectPath) throws JavaModelException {
		addRequiredProject(projectPath, requiredProjectPath, false);
	}
	
	/** Adds a project to the classpath of a project.
	 */
	public void addRequiredProject(IPath projectPath, IPath requiredProjectPath, boolean isExported) throws JavaModelException {
		checkAssertion("required project must not be in project", !projectPath.isPrefixOf(requiredProjectPath)); //$NON-NLS-1$
		addEntry(projectPath, JavaCore.newProjectEntry(requiredProjectPath, isExported));
	}

	public void addExternalJar(IPath projectPath, String jar) throws JavaModelException {
		addExternalJar(projectPath, jar, false);
	}
	
	/** Adds an external jar to the classpath of a project.
	 */
	public void addExternalJar(IPath projectPath, String jar, boolean isExported) throws JavaModelException {
		checkAssertion("file name must end with .zip or .jar", jar.endsWith(".zip") || jar.endsWith(".jar")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		addEntry(projectPath, JavaCore.newLibraryEntry(new Path(jar), null, null, isExported));
	}
	
	private void addEntry(IPath projectPath, IClasspathEntry entryPath) throws JavaModelException {
		IClasspathEntry[] classpath = getClasspath(projectPath);
		IClasspathEntry[] newClaspath = new IClasspathEntry[classpath.length + 1];
		System.arraycopy(classpath, 0, newClaspath, 0, classpath.length);
		newClaspath[classpath.length] = entryPath;
		setClasspath(projectPath, newClaspath);
	}
	
	/** Adds a file.
	 */
	public IPath addFile(IPath root, String fileName, String contents){
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		IPath filePath = root.append(fileName);
		try {
			createFile(filePath, contents.getBytes("UTF8")); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			checkAssertion("e1", false); //$NON-NLS-1$
		}
		return filePath;
	}
	
	/** Adds a folder.
	 */
	public IPath addFolder(IPath root, String folderName){
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		IPath folderPath = root.append(folderName);
		createFolder(folderPath);
		return folderPath;
	}

	public IPath addInternalJar(IPath projectPath, String zipName, byte[] contents) throws JavaModelException {
		return addInternalJar(projectPath, zipName, contents, false);
	}

	/** Adds a jar with the given contents to the the workspace.
	 * If a jar with the same name already exists, it is
	 * replaced.  A workspace must be open, and the given
	 * zip name must end with ".zip" or ".jar".  Returns the path of
	 * the added jar.
	 */
	public IPath addInternalJar(IPath projectPath, String zipName, byte[] contents, boolean isExported) throws JavaModelException {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		checkAssertion("zipName must end with .zip or .jar", zipName.endsWith(".zip") || zipName.endsWith(".jar")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		IPath path = projectPath.append(zipName);
		
		/* remove any existing zip from the java model */
		removeInternalJar(projectPath, zipName);

		createFile(path, contents);
		addEntry(projectPath, JavaCore.newLibraryEntry(path, null, null, isExported));
		return path;
	}

	private void checkAssertion(String message, boolean b) {
		Assert.isTrue(b, message);
	}

	/** Closes the testing environment and frees up any
	 * resources.  Once the testing environment is closed,
	 * it shouldn't be used any more.
	 */
	public void close() {
		try {
			if (fProjects != null) {
				Enumeration projectNames = fProjects.keys();
				while (projectNames.hasMoreElements()) {
					String projectName = (String) projectNames.nextElement();
					getJavaProject(projectName).getJavaModel().close();
				}
			}
			closeWorkspace();
		} catch (JavaModelException e) {
			e.printStackTrace();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}
	
	/** Close a project from the workspace.
	 */
	public void closeProject(IPath projectPath){
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		try {
			getJavaProject(projectPath).getProject().close(null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	private void closeWorkspace() {
		fIsOpen = false;
		fWasBuilt = false;
	}

	private IFile createFile(IPath path, byte[] contents) {
		try {
			IFile file = fWorkspace.getRoot().getFile(path);

			ByteArrayInputStream is = new ByteArrayInputStream(contents);
			if (file.exists()) {
				file.delete(true, null);
			}
			file.create(is, true, null);
			return file;
		} catch (CoreException e) {
			handle(e);
		}
		return null;
	}

	private IFolder createFolder(IPath path) {
		checkAssertion("root", !path.isRoot()); //$NON-NLS-1$

		/* don't create folders for projects */
		if (path.segmentCount() <= 1) {
			return null;
		}

		IFolder folder = fWorkspace.getRoot().getFolder(path);
		if (!folder.exists()) {
			/* create the parent folder if necessary */
			createFolder(path.removeLastSegments(1));

			try {
				folder.create(true, true, null);
			} catch (CoreException e) {
				handle(e);
			}
		}
		return folder;
	}

	private IProject createProject(String projectName) {
		IProject project = null;
		try {
			project = fWorkspace.getRoot().getProject(projectName);
			project.create(null, null);
			project.open(null);
			fProjects.put(projectName, project);
			addBuilderSpecs(projectName);
		} catch (CoreException e) {
			handle(e);
		}
		
		return project;
	}

	private void deleteFile(File file) {
		file = file.getAbsoluteFile();
		if (!file.exists())
			return;
		if (file.isDirectory()) {
			String[] files = file.list();
			//file.list() can return null
			if (files != null) {
				for (int i = 0; i < files.length; ++i) {
					deleteFile(new File(file, files[i]));
				}
			}
		}
		if (!file.delete()) {
			System.out.println(
				"WARNING: deleteFile(File) could not delete: " + file.getPath()); //$NON-NLS-1$
		}
	}
	
	/** Batch builds the workspace.  A workspace must be
	 * open.
	 */
	public void fullBuild() {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		try {
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
		} catch (CoreException e) {
			handle(e);
		}
		fWasBuilt = true;
	}

	/** Batch builds a project.  A workspace must be
	 * open.
	 */
	public void fullBuild(IPath projectPath) {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		try {
			getProject(projectPath).build(IncrementalProjectBuilder.FULL_BUILD, null);
		} catch (CoreException e) {
			handle(e);
		}
		fWasBuilt = true;
	}
	
	/**
	* Returns the class path.
	*/
	public IClasspathEntry[] getClasspath(IPath projectPath) {
		try {
			checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
			JavaProject javaProject = (JavaProject) JavaCore.create(getProject(projectPath));
			return javaProject.getExpandedClasspath(true);
//			IPath[] packageFragmentRootsPath = new IPath[entries.length];
//			for (int i = 0; i < entries.length; ++i)
//				packageFragmentRootsPath[i] = entries[i].getPath();
//			return packageFragmentRootsPath;
		} catch (JavaModelException e) {
			e.printStackTrace();
			checkAssertion("JavaModelException", false); //$NON-NLS-1$
			return null; // not reachable
		}
	}
	
	/**
	* Returns the Java Model element for the project.
	*/
	public IJavaProject getJavaProject(IPath projectPath) {
		IJavaProject javaProject = JavaCore.create(getProject(projectPath));
		Assert.isNotNull(javaProject);
		return javaProject;
	}
	
	/**
	* Returns the Java Model element for the project.
	*/
	public IJavaProject getJavaProject(String projectName) {
		IJavaProject javaProject = JavaCore.create(getProject(projectName));
		Assert.isNotNull(javaProject);
		return javaProject;
	}
	
	/**
	 * Return output location for a project.
	 */
	public IPath getOutputLocation(IPath projectPath){
		try {
			IJavaProject javaProject = JavaCore.create(getProject(projectPath));
			return javaProject.getOutputLocation();
		} catch(CoreException e){
		
		}
		return null;
	}
	
	/**
	 * Return all problems with workspace.
	 */
	public Problem[] getProblems(){
		return getProblemsFor(getWorkspaceRootPath());
	}
	
	/**
	 * Return all problems with the specified element.
	 */
	public Problem[] getProblemsFor(IPath path){
		IResource resource;
		if(path.equals(getWorkspaceRootPath())){
			resource = getWorkspace().getRoot();
		} else {
			IProject p = getProject(path);
			if(p != null && path.equals(p.getFullPath())) {
				resource = getProject(path.lastSegment());
			} else if(path.getFileExtension() == null) {
				resource = getWorkspace().getRoot().getFolder(path);
			} else {
				resource = getWorkspace().getRoot().getFile(path);
			}
		}
		try {
			ArrayList problems = new ArrayList();
			IMarker[] markers = resource.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
			for (int i = 0; i < markers.length; i++)
				problems.add(new Problem(markers[i]));

			markers = resource.findMarkers(IJavaModelMarker.BUILDPATH_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
			for (int i = 0; i < markers.length; i++)
				problems.add(new Problem(markers[i]));
			
			markers = resource.findMarkers(IJavaModelMarker.TASK_MARKER, true, IResource.DEPTH_INFINITE);
			for (int i = 0; i < markers.length; i++)
				problems.add(new Problem(markers[i]));

			Problem[] result = new Problem[problems.size()];
			problems.toArray(result);
			return result;
		} catch(CoreException e){
		}
		return new Problem[0];
	}
	
	/** Return the path of the package
	 * with the given name.  A workspace must be open, and
	 * the package must exist.
	 */
	public IPath getPackagePath(IPath root, String packageName) {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		if (packageName.length() == 0)
			return root;
		else
			return root.append(packageName.replace('.', IPath.SEPARATOR));
	}

	/** Return the path of the package fragment root
	 * with the given name.  A workspace must be open, and
	 * the package fragment root must exist.
	 */
	public IPath getPackageFragmentRootPath(IPath projectPath, String name) {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		if (name.length() == 0)
			return projectPath;
		else
			return projectPath.append(name);
	}
	
	/**
	* Returns the core project.
	*/
	public IProject getProject(String projectName) {
		return (IProject)fProjects.get(projectName);
	}
	
	/**
	* Returns the core project.
	*/
	public IProject getProject(IPath projectPath) {
		return (IProject)fProjects.get(projectPath.lastSegment());
	}

	/**
	* Returns the workspace.
	*/
	public IWorkspace getWorkspace() {
		return fWorkspace;
	}
	
	/**
	* Returns the path of workspace root.
	*/
	public IPath getWorkspaceRootPath(){
		return getWorkspace().getRoot().getLocation();
	}

	private IPath getJarRootPath(IPath projectPath) {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		return getProject(projectPath).getFullPath();
	}

	void handle(Exception e) {
		if (e instanceof CoreException) {
			handleCoreException((CoreException) e);
		} else {
			e.printStackTrace();
			Assert.isTrue(false);
		}
	}

	/**
	* Handles a core exception thrown during a testing environment operation
	*/
	private void handleCoreException(CoreException e) {
		e.printStackTrace();
		Assert.isTrue(
			false,
			"Core exception in testing environment: " + e.getMessage()); //$NON-NLS-1$
	}

	/** Incrementally builds the workspace.  A workspace must be
	 * open.
	 */
	public void incrementalBuild() {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		checkAssertion("the workspace must have been built", fWasBuilt); //$NON-NLS-1$
		try {
			getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
		} catch (CoreException e) {
			handle(e);
		}
	}
	
	/** Incrementally builds a project.  A workspace must be
	 * open.
	 */
	public void incrementalBuild(IPath projectPath) {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		checkAssertion("the workspace must have been built", fWasBuilt); //$NON-NLS-1$
		try {
			getProject(projectPath).build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
		} catch (CoreException e) {
			handle(e);
		}
	}

	/** Open an empty workspace.
 	*/
	public void openEmptyWorkspace() {
		close();
		openWorkspace();
		fProjects = new Hashtable(10);
		setup();
	}
	
	/** Close a project from the workspace.
	 */
	public void openProject(IPath projectPath){
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		try {
			getJavaProject(projectPath).getProject().open(null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private void openWorkspace() {
		try {
			closeWorkspace();

			fWorkspace = ResourcesPlugin.getWorkspace();

			// turn off auto-build -- the tests determine when builds occur
			IWorkspaceDescription description = fWorkspace.getDescription();
			description.setAutoBuilding(false);
			fWorkspace.setDescription(description);
		} catch (Exception e) {
			handle(e);
		}
	}

	/** Renames a compilation unit int the given package in the workspace.
	 * A workspace must be open.
	 */
	public void renameCU(IPath packagePath, String cuName, String newName) {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		IFolder packageFolder = fWorkspace.getRoot().getFolder(packagePath);
		try {
			packageFolder.getFile(cuName).move(packageFolder.getFile(newName).getFullPath(), true, null);
		} catch (CoreException e) {
			handle(e);
		}
	}

	/** Removes a binary class from the given package in
	 * the workspace.  A workspace must be open, and the
	 * given class name must not end with ".class".
	 */
	public void removeBinaryClass(IPath packagePath, String className) {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		className += ".class"; //$NON-NLS-1$
		IFolder packageFolder = fWorkspace.getRoot().getFolder(packagePath);
		try {
			packageFolder.getFile(className).delete(true, null);
		} catch (CoreException e) {
			handle(e);
		}
	}

	/** Removes a class from the given package in the workspace.
	 * A workspace must be open, and the given class name must
	 * not end with ".java".
	 */
	public void removeClass(IPath packagePath, String className) {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		className += ".java"; //$NON-NLS-1$
		IFolder packageFolder = fWorkspace.getRoot().getFolder(packagePath);
		try {
			packageFolder.getFile(className).delete(true, null);
		} catch (CoreException e) {
			handle(e);
		}
	}

	/** Removes a package from the given package fragment root
	 * in the workspace.  A workspace must be open.
	 */
	public void removePackage(IPath packageFragmentRootPath, String packageName) {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		IPath path =
			packageFragmentRootPath.append(packageName.replace('.', IPath.SEPARATOR));
		IFolder folder = fWorkspace.getRoot().getFolder(path);
		try {
			folder.delete(false, null);
		} catch (CoreException e) {
			handle(e);
		}
	}

	/** Removes the given package fragment root from the
	 * the workspace.  A workspace must be open.
	 */
	public void removePackageFragmentRoot(IPath projectPath, String packageFragmentRootName) throws JavaModelException {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		if (packageFragmentRootName.length() > 0) {
			IFolder folder = getProject(projectPath).getFolder(packageFragmentRootName);
			if (folder.exists()) {
				try {
					folder.delete(false, null);
				} catch (CoreException e) {
					handle(e);
				}
			}
		}
		IPath rootPath = getPackageFragmentRootPath(projectPath, packageFragmentRootName);
		removeEntry(projectPath, rootPath);
	}
	
	/** Remove a project from the workspace.
	 */
	public void removeProject(IPath projectPath){
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		try {
			getJavaProject(projectPath).close();
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		IProject project = getProject(projectPath);
		try {
			project.delete(true, null);
		} catch (CoreException e) {
			handle(e);
		}
		
	}
	
	/** Remove a required project from the classpath
	 */
	public void removeRequiredProject(IPath projectPath, IPath requiredProject) throws JavaModelException {
		removeEntry(projectPath, requiredProject);
	}
	
	/** Remove all elements in the workspace.
	 */
	public void resetWorkspace(){
		if (fProjects != null) {
			Enumeration projectNames = fProjects.keys();
			while (projectNames.hasMoreElements()) {
				String projectName = (String) projectNames.nextElement();
				removeProject(getProject(projectName).getFullPath());
			}
		}
	}

	/** Removes the given internal jar from the workspace.
	 * A workspace must be open.
	 */
	public void removeInternalJar(IPath projectPath, String zipName) throws JavaModelException {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		checkAssertion("zipName must end with .zip or .jar", zipName.endsWith(".zip") || zipName.endsWith(".jar")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		/* remove zip from the java model (it caches open zip files) */
		IPath zipPath = getJarRootPath(projectPath).append(zipName);
		try {
			getJavaProject(projectPath)
				.getPackageFragmentRoot(getWorkspace().getRoot().getFile(zipPath))
				.close();
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		removePackageFragmentRoot(projectPath, zipName);

		IFile file = getProject(projectPath).getFile(zipName);
		try {
			file.delete(false, null);
		} catch (CoreException e) {
			handle(e);
		}
	}
	
	/**
	 * Remove an external jar from the classpath.
	 */
	public void removeExternalJar(IPath projectPath, IPath jarPath) throws JavaModelException {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		removeEntry(projectPath, jarPath);
	}
	
	private void removeEntry(IPath projectPath, IPath entryPath) throws JavaModelException {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		IClasspathEntry[] oldEntries = getClasspath(projectPath);
		for (int i = 0; i < oldEntries.length; ++i) {
			if (oldEntries[i].getPath().equals(entryPath)) {
				IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length - 1];
				System.arraycopy(oldEntries, 0, newEntries, 0, i);
				System.arraycopy(oldEntries, i + 1, newEntries, i, oldEntries.length - i - 1);
				setClasspath(projectPath, newEntries);
			}
		}
	}

	/** Remove a file
	 */
	public void removeFile(IPath filePath) {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		try {
			fWorkspace.getRoot().getFile(filePath).delete(true, null);
		} catch (CoreException e) {
			handle(e);
		}
	}
	
	/** Remove a folder
	 */
	public void removeFolder(IPath folderPath) {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		IFolder folder = fWorkspace.getRoot().getFolder(folderPath);
		try {
			folder.delete(true, null);
		} catch (CoreException e) {
			handle(e);
		}
	}
	
	/** Sets the classpath to the given package fragment
	 * roots.  The builder searches the classpath to
	 * find the java files it needs during a build.
	 */
//	public void setClasspath(IPath projectPath, IPath[] packageFragmentRootsPath) {
//		try {
//			checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
//			IJavaProject javaProject = JavaCore.create(getProject(projectPath));
//			IClasspathEntry[] entries =
//				new IClasspathEntry[packageFragmentRootsPath.length];
//			for (int i = 0; i < packageFragmentRootsPath.length; ++i) {
//				IPath path = packageFragmentRootsPath[i];
//				if ("jar".equals(path.getFileExtension()) //$NON-NLS-1$
//					|| "zip".equals(path.getFileExtension())) { //$NON-NLS-1$
//					entries[i] = JavaCore.newLibraryEntry(path, null, null, isExported);
//				} else if (projectPath.isPrefixOf(packageFragmentRootsPath[i])) {
//					entries[i] = JavaCore.newSourceEntry(path, IPath[] exclusionPatterns, IPath specificOutputLocation)
//				} else {
//					entries[i] = JavaCore.newProjectEntry(path, isExported);
//				}
//			}
//			javaProject.setRawClasspath(entries, null);
//		} catch (JavaModelException e) {
//			e.printStackTrace();
//			checkAssertion("JavaModelException", false); //$NON-NLS-1$
//		}
//	}

	public void setClasspath(IPath projectPath, IClasspathEntry[] entries) throws JavaModelException {
		checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
		IJavaProject javaProject = JavaCore.create(getProject(projectPath));
		javaProject.setRawClasspath(entries, null);
	}
	
	public IPath setExternalOutputFolder(IPath projectPath, String name, IPath externalOutputLocation){
		IPath result = null;
		try {
			checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
			IProject p = getProject(projectPath);
			IFolder f = p.getFolder(name);
			f.createLink(externalOutputLocation, IFolder.ALLOW_MISSING_LOCAL, null);

			result = f.getFullPath();
			IJavaProject javaProject = JavaCore.create(p);
			javaProject.setOutputLocation(result, null);
		} catch (CoreException e) {
			e.printStackTrace();
			checkAssertion("CoreException", false); //$NON-NLS-1$
		}
		return result;
	}
	
	public IPath setOutputFolder(IPath projectPath, String outputFolder){
		IPath outputPath = null;
		try {
			checkAssertion("a workspace must be open", fIsOpen); //$NON-NLS-1$
			IJavaProject javaProject = JavaCore.create(getProject(projectPath));
			outputPath = projectPath.append(outputFolder);
			javaProject.setOutputLocation(outputPath, null);
		} catch (JavaModelException e) {
			e.printStackTrace();
			checkAssertion("JavaModelException", false); //$NON-NLS-1$
		}
		return outputPath;
	}

	private void setup() {
		fIsOpen = true;
	}
}