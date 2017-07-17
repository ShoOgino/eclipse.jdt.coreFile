/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Terry Parker <tparker@google.com> 
 *           - Contribution for https://bugs.eclipse.org/bugs/show_bug.cgi?id=372418
 *           -  Another problem with inner classes referenced from jars or class folders: "The type ... cannot be resolved"
 *     Stephan Herrmann - Contribution for
 *								Bug 392727 - Cannot compile project when a java file contains $ in its file name
 *								Bug 440477 - [null] Infrastructure for feeding external annotations into compilation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.builder;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.ModuleBinding;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.util.SimpleLookupTable;
import org.eclipse.jdt.internal.compiler.util.SimpleSet;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.jdt.internal.core.*;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings({"rawtypes", "unchecked"})
public class NameEnvironment implements IModuleAwareNameEnvironment, SuffixConstants {

boolean isIncrementalBuild;
ClasspathMultiDirectory[] sourceLocations;
ClasspathLocation[] binaryLocations;
Map<String,IModulePathEntry> modulePathEntries; // is null when performing a non-modular compilation
BuildNotifier notifier;

SimpleSet initialTypeNames; // assumed that each name is of the form "a/b/ClassName", or, if a module is given: "my.mod:a/b/ClassName"
SimpleLookupTable additionalUnits;

NameEnvironment(IWorkspaceRoot root, JavaProject javaProject, SimpleLookupTable binaryLocationsPerProject, BuildNotifier notifier) throws CoreException {
	this.isIncrementalBuild = false;
	this.notifier = notifier;
	computeClasspathLocations(root, javaProject, binaryLocationsPerProject);
	setNames(null, null);
}

public NameEnvironment(IJavaProject javaProject) {
	this.isIncrementalBuild = false;
	try {
		computeClasspathLocations(javaProject.getProject().getWorkspace().getRoot(), (JavaProject) javaProject, null);
	} catch(CoreException e) {
		this.sourceLocations = new ClasspathMultiDirectory[0];
		this.binaryLocations = new ClasspathLocation[0];
	}
	setNames(null, null);
}

/* Some examples of resolved class path entries.
* Remember to search class path in the order that it was defined.
*
* 1a. typical project with no source folders:
*   /Test[CPE_SOURCE][K_SOURCE] -> D:/eclipse.test/Test
* 1b. project with source folders:
*   /Test/src1[CPE_SOURCE][K_SOURCE] -> D:/eclipse.test/Test/src1
*   /Test/src2[CPE_SOURCE][K_SOURCE] -> D:/eclipse.test/Test/src2
*  NOTE: These can be in any order & separated by prereq projects or libraries
* 1c. project external to workspace (only detectable using getLocation()):
*   /Test/src[CPE_SOURCE][K_SOURCE] -> d:/eclipse.zzz/src
*  Need to search source folder & output folder
*
* 2. zip files:
*   D:/j9/lib/jclMax/classes.zip[CPE_LIBRARY][K_BINARY][sourcePath:d:/j9/lib/jclMax/source/source.zip]
*      -> D:/j9/lib/jclMax/classes.zip
*  ALWAYS want to take the library path as is
*
* 3a. prereq project (regardless of whether it has a source or output folder):
*   /Test[CPE_PROJECT][K_SOURCE] -> D:/eclipse.test/Test
*  ALWAYS want to append the output folder & ONLY search for .class files
*/
private void computeClasspathLocations(
	IWorkspaceRoot root,
	JavaProject javaProject,
	SimpleLookupTable binaryLocationsPerProject) throws CoreException {

	/* Update cycle marker */
	IMarker cycleMarker = javaProject.getCycleMarker();
	if (cycleMarker != null) {
		int severity = JavaCore.ERROR.equals(javaProject.getOption(JavaCore.CORE_CIRCULAR_CLASSPATH, true))
			? IMarker.SEVERITY_ERROR
			: IMarker.SEVERITY_WARNING;
		if (severity != cycleMarker.getAttribute(IMarker.SEVERITY, severity))
			cycleMarker.setAttribute(IMarker.SEVERITY, severity);
	}

	IClasspathEntry[] classpathEntries = javaProject.getExpandedClasspath();
	ArrayList sLocations = new ArrayList(classpathEntries.length);
	ArrayList bLocations = new ArrayList(classpathEntries.length);
	Map<String, IModulePathEntry> moduleEntries = null;
	if (CompilerOptions.versionToJdkLevel(javaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true)) >= ClassFileConstants.JDK9) {
		moduleEntries = new HashMap<>(classpathEntries.length);
	}
	IModuleDescription mod = null;
	
	nextEntry : for (int i = 0, l = classpathEntries.length; i < l; i++) {
		ClasspathEntry entry = (ClasspathEntry) classpathEntries[i];
		IPath path = entry.getPath();
		Object target = JavaModel.getTarget(path, true);
		IPath externalAnnotationPath = ClasspathEntry.getExternalAnnotationPath(entry, javaProject.getProject(), true);
		if (target == null) continue nextEntry;

		switch(entry.getEntryKind()) {
			case IClasspathEntry.CPE_SOURCE :
				if (!(target instanceof IContainer)) continue nextEntry;
				IPath outputPath = entry.getOutputLocation() != null
					? entry.getOutputLocation()
					: javaProject.getOutputLocation();
				IContainer outputFolder;
				if (outputPath.segmentCount() == 1) {
					outputFolder = javaProject.getProject();
				} else {
					outputFolder = root.getFolder(outputPath);
					if (!outputFolder.exists())
						createOutputFolder(outputFolder);
				}
				sLocations.add(ClasspathLocation.forSourceFolder(
							(IContainer) target, 
							outputFolder,
							entry.fullInclusionPatternChars(), 
							entry.fullExclusionPatternChars(),
							entry.ignoreOptionalProblems(),
							this));
				continue nextEntry;

			case IClasspathEntry.CPE_PROJECT :
				if (!(target instanceof IProject)) continue nextEntry;
				IProject prereqProject = (IProject) target;
				if (!JavaProject.hasJavaNature(prereqProject)) continue nextEntry; // if project doesn't have java nature or is not accessible

				JavaProject prereqJavaProject = (JavaProject) JavaCore.create(prereqProject);
				IClasspathEntry[] prereqClasspathEntries = prereqJavaProject.getRawClasspath();
				ArrayList seen = new ArrayList();
				List<ClasspathLocation> projectLocations = new ArrayList<ClasspathLocation>();
				nextPrereqEntry: for (int j = 0, m = prereqClasspathEntries.length; j < m; j++) {
					IClasspathEntry prereqEntry = prereqClasspathEntries[j];
					if (prereqEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
						Object prereqTarget = JavaModel.getTarget(prereqEntry.getPath(), true);
						if (!(prereqTarget instanceof IContainer)) continue nextPrereqEntry;
						IPath prereqOutputPath = prereqEntry.getOutputLocation() != null
							? prereqEntry.getOutputLocation()
							: prereqJavaProject.getOutputLocation();
						IContainer binaryFolder = prereqOutputPath.segmentCount() == 1
							? (IContainer) prereqProject
							: (IContainer) root.getFolder(prereqOutputPath);
						if (binaryFolder.exists() && !seen.contains(binaryFolder)) {
							seen.add(binaryFolder);
							ClasspathLocation bLocation = ClasspathLocation.forBinaryFolder(binaryFolder, true, entry.getAccessRuleSet(), externalAnnotationPath, this, entry.isAutomaticModule());
							bLocations.add(bLocation);
							projectLocations.add(bLocation);
							if (binaryLocationsPerProject != null) { // normal builder mode
								ClasspathLocation[] existingLocations = (ClasspathLocation[]) binaryLocationsPerProject.get(prereqProject);
								if (existingLocations == null) {
									existingLocations = new ClasspathLocation[] {bLocation};
								} else {
									int size = existingLocations.length;
									System.arraycopy(existingLocations, 0, existingLocations = new ClasspathLocation[size + 1], 0, size);
									existingLocations[size] = bLocation;
								}
								binaryLocationsPerProject.put(prereqProject, existingLocations);
							}
						}
					}
				}
				if (moduleEntries != null && (mod = prereqJavaProject.getModuleDescription()) != null && projectLocations.size() > 0) {
					try {
						SourceModule sourceModule = (SourceModule)mod;
						ModuleDescriptionInfo info = (ModuleDescriptionInfo) sourceModule.getElementInfo();
						ModulePathEntry projectEntry = new ModulePathEntry(prereqJavaProject.getPath(), info, projectLocations.toArray(new ClasspathLocation[projectLocations.size()]));
						moduleEntries.put(sourceModule.getElementName(), projectEntry);
					} catch (JavaModelException jme) {
						// do nothing, probably a non module project
					}
				}
				continue nextEntry;

			case IClasspathEntry.CPE_LIBRARY :
				if (target instanceof IResource) {
					IResource resource = (IResource) target;
					ClasspathLocation bLocation = null;
					if (resource instanceof IFile) {
						AccessRuleSet accessRuleSet =
							(JavaCore.IGNORE.equals(javaProject.getOption(JavaCore.COMPILER_PB_FORBIDDEN_REFERENCE, true))
							&& JavaCore.IGNORE.equals(javaProject.getOption(JavaCore.COMPILER_PB_DISCOURAGED_REFERENCE, true)))
								? null
								: entry.getAccessRuleSet();
						bLocation = ClasspathLocation.forLibrary((IFile) resource, accessRuleSet, externalAnnotationPath, this, entry.isAutomaticModule());
					} else if (resource instanceof IContainer) {
						AccessRuleSet accessRuleSet =
							(JavaCore.IGNORE.equals(javaProject.getOption(JavaCore.COMPILER_PB_FORBIDDEN_REFERENCE, true))
							&& JavaCore.IGNORE.equals(javaProject.getOption(JavaCore.COMPILER_PB_DISCOURAGED_REFERENCE, true)))
								? null
								: entry.getAccessRuleSet();
						bLocation = ClasspathLocation.forBinaryFolder((IContainer) target, false, accessRuleSet, externalAnnotationPath, this, entry.isAutomaticModule());	 // is library folder not output folder
					}
					bLocations.add(bLocation);
					// TODO: Ideally we need to do something like mapToModulePathEntry using the path and if it is indeed
					// a module path entry, then add the corresponding entry here, but that would need the target platform
					if (moduleEntries != null) {
						if (bLocation instanceof IMultiModuleEntry) {
							IMultiModuleEntry binaryModulePathEntry = (IMultiModuleEntry) bLocation;
							for (String moduleName : binaryModulePathEntry.getModuleNames()) {
								moduleEntries.put(moduleName, binaryModulePathEntry);							
							}
						} else if (bLocation instanceof IModulePathEntry) {
							IModulePathEntry binaryModulePathEntry = (IModulePathEntry) bLocation;
							IModule module = binaryModulePathEntry.getModule();
							if (module != null)
								moduleEntries.put(String.valueOf(module.name()), binaryModulePathEntry);
						}
					}
					if (binaryLocationsPerProject != null) { // normal builder mode
						IProject p = resource.getProject(); // can be the project being built
						ClasspathLocation[] existingLocations = (ClasspathLocation[]) binaryLocationsPerProject.get(p);
						if (existingLocations == null) {
							existingLocations = new ClasspathLocation[] {bLocation};
						} else {
							int size = existingLocations.length;
							System.arraycopy(existingLocations, 0, existingLocations = new ClasspathLocation[size + 1], 0, size);
							existingLocations[size] = bLocation;
						}
						binaryLocationsPerProject.put(p, existingLocations);
					}
				} else if (target instanceof File) {
					AccessRuleSet accessRuleSet =
						(JavaCore.IGNORE.equals(javaProject.getOption(JavaCore.COMPILER_PB_FORBIDDEN_REFERENCE, true))
							&& JavaCore.IGNORE.equals(javaProject.getOption(JavaCore.COMPILER_PB_DISCOURAGED_REFERENCE, true)))
								? null
								: entry.getAccessRuleSet();
					ClasspathLocation bLocation = ClasspathLocation.forLibrary(path.toOSString(), accessRuleSet, externalAnnotationPath, this, entry.isAutomaticModule());
					bLocations.add(bLocation);
					// TODO: Ideally we need to do something like mapToModulePathEntry using the path and if it is indeed
					// a module path entry, then add the corresponding entry here, but that would need the target platform
					if (moduleEntries != null) {
						if (bLocation instanceof IMultiModuleEntry) {
							IMultiModuleEntry binaryModulePathEntry = (IMultiModuleEntry) bLocation;
							for (String moduleName : binaryModulePathEntry.getModuleNames()) {
								moduleEntries.put(moduleName, binaryModulePathEntry);							
							}
						} else if (bLocation instanceof IModulePathEntry) {
							IModulePathEntry binaryModulePathEntry = (IModulePathEntry) bLocation;
							IModule module = binaryModulePathEntry.getModule();
							if (module != null)
								moduleEntries.put(String.valueOf(module.name()), binaryModulePathEntry);
						}
					}
				}
				continue nextEntry;
		}
	}

	// now split the classpath locations... place the output folders ahead of the other .class file folders & jars
	ArrayList outputFolders = new ArrayList(1);
	this.sourceLocations = new ClasspathMultiDirectory[sLocations.size()];
	if (!sLocations.isEmpty()) {
		sLocations.toArray(this.sourceLocations);
		if (moduleEntries != null && (mod = javaProject.getModuleDescription()) != null) {
			try {
				SourceModule sourceModule = (SourceModule)mod;
				ModuleDescriptionInfo info = (ModuleDescriptionInfo) sourceModule.getElementInfo();
				ModulePathEntry projectEntry = new ModulePathEntry(javaProject.getPath(), info, this.sourceLocations);
				moduleEntries.put(sourceModule.getElementName(), projectEntry);
			} catch (JavaModelException jme) {
				// do nothing, probably a non module project
			}
		}
		// collect the output folders, skipping duplicates
		next : for (int i = 0, l = this.sourceLocations.length; i < l; i++) {
			ClasspathMultiDirectory md = this.sourceLocations[i];
			IPath outputPath = md.binaryFolder.getFullPath();
			for (int j = 0; j < i; j++) { // compare against previously walked source folders
				if (outputPath.equals(this.sourceLocations[j].binaryFolder.getFullPath())) {
					md.hasIndependentOutputFolder = this.sourceLocations[j].hasIndependentOutputFolder;
					continue next;
				}
			}
			outputFolders.add(md);

			// also tag each source folder whose output folder is an independent folder & is not also a source folder
			for (int j = 0, m = this.sourceLocations.length; j < m; j++)
				if (outputPath.equals(this.sourceLocations[j].sourceFolder.getFullPath()))
					continue next;
			md.hasIndependentOutputFolder = true;
		}
	}

	// combine the output folders with the binary folders & jars... place the output folders before other .class file folders & jars
	this.binaryLocations = new ClasspathLocation[outputFolders.size() + bLocations.size()];
	int index = 0;
	for (int i = 0, l = outputFolders.size(); i < l; i++)
		this.binaryLocations[index++] = (ClasspathLocation) outputFolders.get(i);
	for (int i = 0, l = bLocations.size(); i < l; i++)
		this.binaryLocations[index++] = (ClasspathLocation) bLocations.get(i);
	
	if (moduleEntries != null && !moduleEntries.isEmpty())
		this.modulePathEntries = moduleEntries;
}

public void cleanup() {
	this.initialTypeNames = null;
	this.additionalUnits = null;
	for (int i = 0, l = this.sourceLocations.length; i < l; i++)
		this.sourceLocations[i].cleanup();
	for (int i = 0, l = this.binaryLocations.length; i < l; i++)
		this.binaryLocations[i].cleanup();
	// assume modulePathEntries are cleaned-up via the corresponding source/binaryLocations
}

private void createOutputFolder(IContainer outputFolder) throws CoreException {
	createParentFolder(outputFolder.getParent());
	((IFolder) outputFolder).create(IResource.FORCE | IResource.DERIVED, true, null);
}

private void createParentFolder(IContainer parent) throws CoreException {
	if (!parent.exists()) {
		createParentFolder(parent.getParent());
		((IFolder) parent).create(true, true, null);
	}
}

private NameEnvironmentAnswer findClass(String qualifiedTypeName, char[] typeName, String moduleName) {
	if (this.notifier != null)
		this.notifier.checkCancelWithinCompiler();

	String moduleQualifiedName = moduleName != null ? moduleName+':'+qualifiedTypeName : qualifiedTypeName;
	if (this.initialTypeNames != null && this.initialTypeNames.includes(moduleQualifiedName)) {
		if (this.isIncrementalBuild)
			// catch the case that a type inside a source file has been renamed but other class files are looking for it
			throw new AbortCompilation(true, new AbortIncrementalBuildException(qualifiedTypeName));
		return null; // looking for a file which we know was provided at the beginning of the compilation
	}

	if (this.additionalUnits != null && this.sourceLocations.length > 0) {
		// if an additional source file is waiting to be compiled, answer it BUT not if this is a secondary type search
		// if we answer X.java & it no longer defines Y then the binary type looking for Y will think the class path is wrong
		// let the recompile loop fix up dependents when the secondary type Y has been deleted from X.java
		// Only enclosing type names are present in the additional units table, so strip off inner class specifications
		// when doing the lookup (https://bugs.eclipse.org/372418). 
		// Also take care of $ in the name of the class (https://bugs.eclipse.org/377401)
		// and prefer name with '$' if unit exists rather than failing to search for nested class (https://bugs.eclipse.org/392727)
		SourceFile unit = (SourceFile) this.additionalUnits.get(qualifiedTypeName); // doesn't have file extension
		if (unit != null)
			return new NameEnvironmentAnswer(unit, null /*no access restriction*/);
		int index = qualifiedTypeName.indexOf('$');
		if (index > 0) {
			String enclosingTypeName = qualifiedTypeName.substring(0, index);
			unit = (SourceFile) this.additionalUnits.get(enclosingTypeName); // doesn't have file extension
			if (unit != null)
				return new NameEnvironmentAnswer(unit, null /*no access restriction*/);
		}
	}

	String qBinaryFileName = qualifiedTypeName + SUFFIX_STRING_class;
	String qPackageName =  (qualifiedTypeName.length() == typeName.length) ? Util.EMPTY_STRING :
		qBinaryFileName.substring(0, qBinaryFileName.length() - typeName.length - 7);
	char[] binaryFileName = CharOperation.concat(typeName, SUFFIX_class);

	ClasspathLocation[] relevantLocations;
	if (moduleName != null && this.modulePathEntries != null) {
		IModulePathEntry modulePathEntry = this.modulePathEntries.get(moduleName);
		if (modulePathEntry instanceof ModulePathEntry) {
			relevantLocations = ((ModulePathEntry) modulePathEntry).getClasspathLocations();
		} else if (modulePathEntry instanceof ClasspathLocation) {
			return ((ClasspathLocation) modulePathEntry).findClass(typeName, qPackageName, moduleName, qBinaryFileName, false);
		} else {
			return null;
		}
	} else {
		relevantLocations = this.binaryLocations;
	}
	NameEnvironmentAnswer suggestedAnswer = null;
	for (ClasspathLocation classpathLocation : relevantLocations) {
		NameEnvironmentAnswer answer = classpathLocation.findClass(binaryFileName, qPackageName, moduleName, qBinaryFileName, false);
		if (answer != null) {
			if (!answer.ignoreIfBetter()) {
				if (answer.isBetter(suggestedAnswer))
					return answer;
			} else if (answer.isBetter(suggestedAnswer))
				// remember suggestion and keep looking
				suggestedAnswer = answer;
		}
	}
	return suggestedAnswer;
}

@Override
public NameEnvironmentAnswer findType(char[][] compoundName, char[] moduleName) {
	String stringModuleName = moduleName == ModuleBinding.ANY ? null : String.valueOf(moduleName);
	if (compoundName != null)
		return findClass(
			String.valueOf(CharOperation.concatWith(compoundName, '/')),
			compoundName[compoundName.length - 1], 
			stringModuleName);
	return null;
}

@Override
public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName, char[] moduleName) {
	String stringModuleName = moduleName == ModuleBinding.ANY ? null : String.valueOf(moduleName);
		return findClass(
			String.valueOf(CharOperation.concatWith(packageName, typeName, '/')),
			typeName,
			stringModuleName);
}

public boolean isPackage(String qualifiedPackageName) {
	return isPackage(qualifiedPackageName, null);
}
@Override
public char[][] getModulesDeclaringPackage(char[][] parentPackageName, char[] name, char[] moduleName) {
	String pkgName = new String(CharOperation.concatWith(parentPackageName, name, '/'));
	if (moduleName == ModuleBinding.UNNAMED || this.modulePathEntries == null) {
		char[][] names = CharOperation.NO_CHAR_CHAR;
		for (ClasspathLocation location : this.binaryLocations) {
			if (location.module == null && !(location instanceof ClasspathJrt)) {
				char[][] declaringModules = location.getModulesDeclaringPackage(pkgName, null);
				if (declaringModules != null)
					names = CharOperation.arrayConcat(names, declaringModules);
			}
		}
		for (ClasspathLocation location : this.sourceLocations) {
			if (location.module == null) {
				char[][] declaringModules = location.getModulesDeclaringPackage(pkgName, null);
				if (declaringModules != null)
					names = CharOperation.arrayConcat(names, declaringModules);
			}
		}
		return names == CharOperation.NO_CHAR_CHAR ? null : names;
	} else if (moduleName == ModuleBinding.ANY) {
		char[][] names = CharOperation.NO_CHAR_CHAR;
		for (IModulePathEntry modulePathEntry : this.modulePathEntries.values()) {
			char[][] declaringModules = modulePathEntry.getModulesDeclaringPackage(pkgName, null);
			if (declaringModules != null)
				names = CharOperation.arrayConcat(names, declaringModules);
		}
		return names == CharOperation.NO_CHAR_CHAR ? null : names;
	} else {
		String modName = new String(moduleName);
		IModulePathEntry modulePathEntry = this.modulePathEntries.get(modName);
		if (modulePathEntry != null) {
			return modulePathEntry.getModulesDeclaringPackage(pkgName, modName);
		}
	}
	return null;
}
private boolean isPackage(String qualifiedPackageName, char[] moduleName) {
	if (moduleName == ModuleBinding.ANY || this.modulePathEntries == null) {
		// NOTE: the output folders are added at the beginning of the binaryLocations
		for (int i = 0, l = this.binaryLocations.length; i < l; i++)
			if (this.binaryLocations[i].isPackage(qualifiedPackageName, null))
				return true;
		// TODO(SHMOD): also search sourceLocations?
	} else {
		String stringModuleName = String.valueOf(moduleName);
		IModulePathEntry modulePathEntry = this.modulePathEntries.get(stringModuleName);
		if (modulePathEntry instanceof ModulePathEntry) {
			for (ClasspathLocation classpathLocation : ((ModulePathEntry) modulePathEntry).getClasspathLocations()) {
				if (classpathLocation.isPackage(qualifiedPackageName, stringModuleName))
					return true;
			}
		} else if (modulePathEntry instanceof ClasspathLocation) {
			return ((ClasspathLocation) modulePathEntry).isPackage(qualifiedPackageName, stringModuleName);
		}
	}
	return false;
}

void setNames(String[] typeNames, SourceFile[] additionalFiles) {
	// convert the initial typeNames to a set
	if (typeNames == null) {
		this.initialTypeNames = null;
	} else {
		this.initialTypeNames = new SimpleSet(typeNames.length);
		for (int i = 0, l = typeNames.length; i < l; i++)
			this.initialTypeNames.add(typeNames[i]);
	}
	// map the additional source files by qualified type name
	if (additionalFiles == null) {
		this.additionalUnits = null;
	} else {
		this.additionalUnits = new SimpleLookupTable(additionalFiles.length);
		for (int i = 0, l = additionalFiles.length; i < l; i++) {
			SourceFile additionalUnit = additionalFiles[i];
			if (additionalUnit != null)
				this.additionalUnits.put(additionalUnit.initialTypeName, additionalFiles[i]);
		}
	}

	for (int i = 0, l = this.sourceLocations.length; i < l; i++)
		this.sourceLocations[i].reset();
	for (int i = 0, l = this.binaryLocations.length; i < l; i++)
		this.binaryLocations[i].reset();
}

@Override
public IModule getModule(char[] name) {
	if (this.modulePathEntries != null) {
		IModulePathEntry modulePathEntry = this.modulePathEntries.get(String.valueOf(name));
		if (modulePathEntry instanceof IMultiModuleEntry)
			return modulePathEntry.getModule(name);
		else if (modulePathEntry != null)
			return modulePathEntry.getModule();
	}
	return null;
}

@Override
public IModule[] getAllAutomaticModules() {
	if (this.modulePathEntries == null)
		return IModule.NO_MODULES;
	Set<IModule> set = this.modulePathEntries.values().stream().map(e -> e.getModule()).filter(m -> m.isAutomatic())
			.collect(Collectors.toSet());
	return set.toArray(new IModule[set.size()]);
}
}
