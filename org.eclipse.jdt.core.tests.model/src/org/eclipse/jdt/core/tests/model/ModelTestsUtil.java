/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;

public class ModelTestsUtil {

static private void collectAllFiles(File root, ArrayList collector, FileFilter fileFilter) {
	File[] files = root.listFiles(fileFilter);
	for (int i = 0; i < files.length; i++) {
		final File currentFile = files[i];
		if (currentFile.isDirectory()) {
			collectAllFiles(currentFile, collector, fileFilter);
		} else {
			collector.add(currentFile);
		}
	}
}

static public boolean convertToIndependantLineDelimiter(File file) {
	return file.getName().endsWith(".java");
}

/**
 * Copy file from src (path to the original file) to dest (path to the destination file).
 */
static public void copy(File src, File dest) throws IOException {
	// read source bytes
	byte[] srcBytes = read(src);
	
	if (convertToIndependantLineDelimiter(src)) {
		String contents = new String(srcBytes);
		contents = org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(contents);
		srcBytes = contents.getBytes();
	}

	// write bytes to dest
	FileOutputStream out = new FileOutputStream(dest);
	out.write(srcBytes);
	out.close();
}

/**
 * Copy the given source directory (and all its contents) to the given target directory.
 */
static public void copyDirectory(File source, File target) throws IOException {
	if (!target.exists()) {
		target.mkdirs();
	}
	File[] files = source.listFiles();
	if (files == null) return;
	for (int i = 0; i < files.length; i++) {
		File sourceChild = files[i];
		String name =  sourceChild.getName();
		if (name.equals("CVS") || name.equals(".svn")) continue;
		File targetChild = new File(target, name);
		if (sourceChild.isDirectory()) {
			copyDirectory(sourceChild, targetChild);
		} else {
			copy(sourceChild, targetChild);
		}
	}
}

public static File[] getAllFiles(File root, FileFilter fileFilter) {
	ArrayList files = new ArrayList();
	if (root.isDirectory()) {
		collectAllFiles(root, files, fileFilter);
		File[] result = new File[files.size()];
		files.toArray(result);
		return result;
	} else {
		return null;
	}
}

/**
 * Returns the specified compilation unit in the given project, root, and
 * package fragment or <code>null</code> if it does not exist.
 */
static public IClassFile getClassFile(IJavaProject project, String rootPath, String packageName, String className) throws JavaModelException {
	IPackageFragment pkg= getPackageFragment(project, rootPath, packageName);
	if (pkg == null) {
		return null;
	}
	return pkg.getClassFile(className);
}

/**
 * Returns compilation unit with given name in given project and package.
 * @param javaProject
 * @param packageName
 * @param unitName
 * @return org.eclipse.jdt.core.ICompilationUnit
 */
static public ICompilationUnit getCompilationUnit(IJavaProject javaProject, String packageName, String unitName) throws JavaModelException {
	if (javaProject == null) return null;
	IType type = javaProject.findType(packageName, unitName);
	if (type != null) {
		return type.getCompilationUnit();
	}
	return null;
}

/**
 * Returns all compilation units of a given project.
 * @param javaProject Project to collect units
 * @return List of org.eclipse.jdt.core.ICompilationUnit
 */
static public List getCompilationUnits(IJavaProject javaProject) throws JavaModelException {
	IPackageFragmentRoot[] fragmentRoots = javaProject.getPackageFragmentRoots();
	int length = fragmentRoots.length;
	List allUnits = new ArrayList();
	for (int i=0; i<length; i++) {
		if (fragmentRoots[i] instanceof JarPackageFragmentRoot) continue;
		IJavaElement[] packages= fragmentRoots[i].getChildren();
		for (int k= 0; k < packages.length; k++) {
			IPackageFragment pack = (IPackageFragment) packages[k];
			ICompilationUnit[] units = pack.getCompilationUnits();
			for (int u=0; u<units.length; u++) {
				allUnits.add(units[u]);
			}
		}
	}
	return allUnits;
}

/**
 * Returns all compilation units of a given project.
 * @param root The package fragment root to collect units
 * @return List of org.eclipse.jdt.core.ICompilationUnit
 */
static public List getCompilationUnits(IPackageFragmentRoot root) throws JavaModelException {
	List allUnits = new ArrayList();
	if (!(root instanceof JarPackageFragmentRoot)) {
		IJavaElement[] packages= root.getChildren();
		for (int k= 0; k < packages.length; k++) {
			IPackageFragment pack = (IPackageFragment) packages[k];
			ICompilationUnit[] units = pack.getCompilationUnits();
			for (int u=0; u<units.length; u++) {
				allUnits.add(units[u]);
			}
		}
	}
	return allUnits;
}

static public IPackageFragmentRoot getExternalJarFile(IJavaProject project, String jarSimpleName) throws JavaModelException {
	IPackageFragmentRoot[] roots = project.getPackageFragmentRoots();
	if (roots == null || roots.length == 0) {
		return null;
	}
	for (int i = 0; i < roots.length; i++) {
		IPackageFragmentRoot root = roots[i];
		if (root.isExternal() && root.getElementName().equals(jarSimpleName)) {
			return root;
		}
	}		
	return null;
}

/**
 * Returns the IPath to the external java class library (e.g. jclMin.jar)
 */
static public IPath getExternalJCLPath() {
	return new Path(getExternalJCLPathString(""));
}	
/**
 * Returns the IPath to the external java class library (e.g. jclMin.jar)
 */
static public IPath getExternalJCLPath(String compliance) {
	return new Path(getExternalJCLPathString(compliance));
}
/**
 * Returns the java.io path to the external java class library (e.g. jclMin.jar)
 */
static public String getExternalJCLPathString() {
	return getExternalJCLPathString("");
}
/**
 * Returns the java.io path to the external java class library (e.g. jclMin.jar)
 */
static public String getExternalJCLPathString(String compliance) {
	return getExternalPath() + "jclMin" + compliance + ".jar";
}
/**
 * Returns the IPath to the root source of the external java class library (e.g. "src")
 */
static public IPath getExternalJCLRootSourcePath() {
	return new Path("src");
}
/**
 * Returns the IPath to the source of the external java class library (e.g. jclMinsrc.zip)
 */
static public IPath getExternalJCLSourcePath() {
	return new Path(getExternalJCLSourcePathString(""));
}
/**
 * Returns the IPath to the source of the external java class library (e.g. jclMinsrc.zip)
 */
static public IPath getExternalJCLSourcePath(String compliance) {
	return new Path(getExternalJCLSourcePathString(compliance));
}
/**
 * Returns the java.io path to the source of the external java class library (e.g. jclMinsrc.zip)
 */
static public String getExternalJCLSourcePathString() {
	return getExternalJCLSourcePathString("");
}
/**
 * Returns the java.io path to the source of the external java class library (e.g. jclMinsrc.zip)
 */
static public String getExternalJCLSourcePathString(String compliance) {
	return getExternalPath() + "jclMin" + compliance + "src.zip";
}
/*
 * Returns the OS path to the external directory that contains external jar files.
 * This path ends with a File.separatorChar.
 */
static public String getExternalPath() {
	try {
		String path = getWorkspaceRoot().getLocation().toFile().getParentFile().getCanonicalPath();
		if (path.charAt(path.length()-1) != File.separatorChar)
			path += File.separatorChar;
		return path;
	} catch (IOException e) {
		e.printStackTrace();
	}
	return  null;
}

/**
 * Returns the specified package fragment in the given project and root, or
 * <code>null</code> if it does not exist.
 * The rootPath must be specified as a project relative path. The empty
 * path refers to the default package fragment.
 */
static public IPackageFragment getPackageFragment(IJavaProject project, String rootPath, String packageName) throws JavaModelException {
	IPackageFragmentRoot root= getPackageFragmentRoot(project, rootPath);
	if (root == null) {
		return null;
	}
	return root.getPackageFragment(packageName);
}

/**
 * Returns the specified package fragment root in the given project, or
 * <code>null</code> if it does not exist.
 * If relative, the rootPath must be specified as a project relative path. 
 * The empty path refers to the package fragment root that is the project
 * folder itself.
 * If absolute, the rootPath refers to either an external jar, or a resource 
 * internal to the workspace
 */
static public IPackageFragmentRoot getPackageFragmentRoot(
	IJavaProject project, 
	String rootPath)
	throws JavaModelException {

	if (project == null) {
		return null;
	}
	IPath path = new Path(rootPath);
	if (path.isAbsolute()) {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = workspaceRoot.findMember(path);
		IPackageFragmentRoot root;
		if (resource == null) {
			// external jar
			root = project.getPackageFragmentRoot(rootPath);
		} else {
			// resource in the workspace
			root = project.getPackageFragmentRoot(resource);
		}
		return root;
	} else {
		IPackageFragmentRoot[] roots = project.getPackageFragmentRoots();
		if (roots == null || roots.length == 0) {
			return null;
		}
		for (int i = 0; i < roots.length; i++) {
			IPackageFragmentRoot root = roots[i];
			if (!root.isExternal()
				&& root.getUnderlyingResource().getProjectRelativePath().equals(path)) {
				return root;
			}
		}
	}
	return getExternalJarFile(project, rootPath);
}
static public String getPluginDirectoryPath() {
	return getPluginDirectoryPath("model");
}
/**
 * Returns the OS path to the directory that contains this plugin.
 * 
 * @param type May be one of the following value
 */
static public String getPluginDirectoryPath(String type) {
	try {
		StringBuffer bundleName = new StringBuffer("org.eclipse.jdt.core.tests");
		if (type != null) {
			bundleName.append('.');
			bundleName.append(type);
		}
		URL platformURL = Platform.getBundle(bundleName.toString()).getEntry("/");
		return new File(FileLocator.toFileURL(platformURL).getFile()).getAbsolutePath();
	} catch (IOException e) {
		e.printStackTrace();
	}
	return null;
}

static public String getSourceWorkspacePath(String workspace) {
	return getPluginDirectoryPath() +  java.io.File.separator + workspace;
}

/**
 * Returns the IWorkspace this test suite is running on.
 */
static public IWorkspace getWorkspace() {
	return ResourcesPlugin.getWorkspace();
}

static public IWorkspaceRoot getWorkspaceRoot() {
	return getWorkspace().getRoot();
}

static public byte[] read(java.io.File file) throws java.io.IOException {
	int fileLength;
	byte[] fileBytes = new byte[fileLength = (int) file.length()];
	java.io.FileInputStream stream = new java.io.FileInputStream(file);
	int bytesRead = 0;
	int lastReadSize = 0;
	while ((lastReadSize != -1) && (bytesRead != fileLength)) {
		lastReadSize = stream.read(fileBytes, bytesRead, fileLength - bytesRead);
		bytesRead += lastReadSize;
	}
	stream.close();
	return fileBytes;
}

/**
 * Remove all white spaces from a string.
 * 
 * @param input The input string
 * @return A new string without any whitespaces
 */
public static String removeWhiteSpace(String input) {
	StringTokenizer tokenizer = new StringTokenizer(input);
	StringBuffer buffer = new StringBuffer();
	while (tokenizer.hasMoreTokens()) {
		buffer.append(tokenizer.nextToken());
	}
    return buffer.toString();
}

/**
 * Check locally for the required JCL files, <jclName>.jar and <jclName>src.zip.
 * If not available, copy from the project resources.
 */
static public void setupExternalJCL(String jclName) throws IOException {
	String externalPath = getExternalPath();
	String separator = java.io.File.separator;
	String resourceJCLDir = getPluginDirectoryPath() + separator + "JCL";
	java.io.File jclDir = new java.io.File(externalPath);
	java.io.File jclMin =
		new java.io.File(externalPath + jclName + ".jar");
	java.io.File jclMinsrc = new java.io.File(externalPath + jclName + "src.zip");
	if (!jclDir.exists()) {
		if (!jclDir.mkdir()) {
			//mkdir failed
			throw new IOException("Could not create the directory " + jclDir);
		}
		//copy the two files to the JCL directory
		java.io.File resourceJCLMin =
			new java.io.File(resourceJCLDir + separator + jclName + ".jar");
		copy(resourceJCLMin, jclMin);
		java.io.File resourceJCLMinsrc =
			new java.io.File(resourceJCLDir + separator + jclName + "src.zip");
		copy(resourceJCLMinsrc, jclMinsrc);
	} else {
		//check that the two files, jclMin.jar and jclMinsrc.zip are present
		//copy either file that is missing or less recent than the one in workspace
		java.io.File resourceJCLMin =
			new java.io.File(resourceJCLDir + separator + jclName + ".jar");
		if ((jclMin.lastModified() < resourceJCLMin.lastModified())
                || (jclMin.length() != resourceJCLMin.length())) {
			copy(resourceJCLMin, jclMin);
		}
		java.io.File resourceJCLMinsrc =
			new java.io.File(resourceJCLDir + separator + jclName + "src.zip");
		if ((jclMinsrc.lastModified() < resourceJCLMinsrc.lastModified())
                || (jclMinsrc.length() != resourceJCLMinsrc.length())) {
			copy(resourceJCLMinsrc, jclMinsrc);
		}
	}
}

static public IJavaProject setUpJavaProject(final String projectName) throws CoreException, IOException {
	return setUpJavaProject(projectName, "1.4");
}

static public IJavaProject setUpJavaProject(final String projectName, String compliance) throws CoreException, IOException {
	// copy files in project from source workspace to target workspace
	String sourceWorkspacePath = getSourceWorkspacePath("workspace");
	String targetWorkspacePath = getWorkspaceRoot().getLocation().toFile().getCanonicalPath();
	copyDirectory(new File(sourceWorkspacePath, projectName), new File(targetWorkspacePath, projectName));

	// ensure variables are set
	setUpJCLClasspathVariables(compliance);

	// create project
	final IProject project = getWorkspaceRoot().getProject(projectName);
	IWorkspaceRunnable populate = new IWorkspaceRunnable() {
		public void run(IProgressMonitor monitor) throws CoreException {
			project.create(null);
			project.open(null);
		}
	};
	getWorkspace().run(populate, null);
	IJavaProject javaProject = JavaCore.create(project);
	setUpProjectCompliance(javaProject, compliance);
	javaProject.setOption(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.IGNORE);
	javaProject.setOption(JavaCore.COMPILER_PB_UNUSED_PRIVATE_MEMBER, JavaCore.IGNORE);
	javaProject.setOption(JavaCore.COMPILER_PB_FIELD_HIDING, JavaCore.IGNORE);
	javaProject.setOption(JavaCore.COMPILER_PB_LOCAL_VARIABLE_HIDING, JavaCore.IGNORE);
	javaProject.setOption(JavaCore.COMPILER_PB_TYPE_PARAMETER_HIDING, JavaCore.IGNORE);
	return javaProject;
}

static public void setUpJCLClasspathVariables(String compliance) throws JavaModelException, IOException {
	if ("1.5".equals(compliance)) {
		if (JavaCore.getClasspathVariable("JCL15_LIB") == null) {
			setupExternalJCL("jclMin1.5");
			JavaCore.setClasspathVariables(
				new String[] {"JCL15_LIB", "JCL15_SRC", "JCL_SRCROOT"},
				new IPath[] {getExternalJCLPath(compliance), getExternalJCLSourcePath(compliance), getExternalJCLRootSourcePath()},
				null);
		} 
	} else {
		if (JavaCore.getClasspathVariable("JCL_LIB") == null) {
			setupExternalJCL("jclMin");
			JavaCore.setClasspathVariables(
				new String[] {"JCL_LIB", "JCL_SRC", "JCL_SRCROOT"},
				new IPath[] {getExternalJCLPath(), getExternalJCLSourcePath(), getExternalJCLRootSourcePath()},
				null);
		} 
	}	
}

static public void setUpProjectCompliance(IJavaProject javaProject, String compliance) throws JavaModelException, IOException {
	// Look for version to set and return if that's already done
	String version = CompilerOptions.VERSION_1_4;
	String jclLibString = null;
	String newJclLibString = null;
	String newJclSrcString = null;
	switch (compliance.charAt(2)) {
		case '6':
			version = CompilerOptions.VERSION_1_6;
			if (version.equals(javaProject.getOption(CompilerOptions.OPTION_Compliance, false))) {
				return;
			}
			jclLibString = "JCL_LIB";
			newJclLibString = "JCL15_LIB";
			newJclSrcString = "JCL15_SRC";
			break;
		case '5':
			version = CompilerOptions.VERSION_1_5;
			if (version.equals(javaProject.getOption(CompilerOptions.OPTION_Compliance, false))) {
				return;
			}
			jclLibString = "JCL_LIB";
			newJclLibString = "JCL15_LIB";
			newJclSrcString = "JCL15_SRC";
			break;
		case '3':
			version = CompilerOptions.VERSION_1_3;
		default:
			if (version.equals(javaProject.getOption(CompilerOptions.OPTION_Compliance, false))) {
				return;
			}
			jclLibString = "JCL15_LIB";
			newJclLibString = "JCL_LIB";
			newJclSrcString = "JCL_SRC";
			break;
	}
	
	// ensure variables are set
	setUpJCLClasspathVariables(compliance);
	
	// set options
	Map options = new HashMap();
	options.put(CompilerOptions.OPTION_Compliance, version);
	options.put(CompilerOptions.OPTION_Source, version);	
	options.put(CompilerOptions.OPTION_TargetPlatform, version);
	javaProject.setOptions(options);
	
	// replace JCL_LIB with JCL15_LIB, and JCL_SRC with JCL15_SRC
	IClasspathEntry[] classpath = javaProject.getRawClasspath();
	IPath jclLib = new Path(jclLibString);
	for (int i = 0, length = classpath.length; i < length; i++) {
		IClasspathEntry entry = classpath[i];
		if (entry.getPath().equals(jclLib)) {
			classpath[i] = JavaCore.newVariableEntry(
					new Path(newJclLibString), 
					new Path(newJclSrcString), 
					entry.getSourceAttachmentRootPath(), 
					entry.getAccessRules(), 
					new IClasspathAttribute[0], 
					entry.isExported());
			break;
		}
	}
	javaProject.setRawClasspath(classpath, null);
}

/**
 * Remove all white spaces at the deginning of each lines from a string.
 * 
 * @param input The input string
 * @return A new string without any whitespaces
 */
public static String trimLinesLeadingWhitespaces(String input) {
	StringTokenizer tokenizer = new StringTokenizer(input, "\r\n\f");
	StringBuffer buffer = new StringBuffer();
	while (tokenizer.hasMoreTokens()) {
		String line = tokenizer.nextToken();
		int length = line.length();
		int size = 0;
		int idx = -1;
		if (length > 0) {
			loop: while ((idx+1) < length) {
				char ch = line.charAt(++idx);
				switch (ch) {
					case '\t':
						size += 4;
						break;
					case '*':
					case ' ':
						break;
					default:
						break loop;
				}
			}
		}
		if (length > 0 && idx > 0 && idx < length) {
			int splitLineIndex = line.indexOf("||", idx);
			if (splitLineIndex > 0) {
				int commentStart = line.indexOf("/*", splitLineIndex);
				if (commentStart >= 80-((size*3)/4)) {
					StringBuffer newLine = new StringBuffer(line.substring(idx-1, splitLineIndex).trim());
					newLine.append('\n');
					newLine.append(line.substring(splitLineIndex).trim());
					newLine.append('\n');
					buffer.append(newLine);
					continue;
				}
			}
			buffer.append(line.substring(idx).trim());
		} else {
			buffer.append(line);
		}
		buffer.append('\n');
	}
    return buffer.toString();
}

}
