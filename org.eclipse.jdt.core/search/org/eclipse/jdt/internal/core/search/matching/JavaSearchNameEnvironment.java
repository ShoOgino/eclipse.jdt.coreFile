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
 *     Stephan Herrmann - Contribution for
 *								Bug 440477 - [null] Infrastructure for feeding external annotations into compilation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.IModuleAwareNameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.ModuleBinding;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.JavaElementRequestor;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.ModuleDescriptionInfo;
import org.eclipse.jdt.internal.core.NameLookup;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jdt.internal.core.builder.ClasspathJar;
import org.eclipse.jdt.internal.core.builder.ClasspathJrt;
import org.eclipse.jdt.internal.core.builder.ClasspathLocation;
import org.eclipse.jdt.internal.core.util.Util;

/*
 * A name environment based on the classpath of a Java project.
 */
public class JavaSearchNameEnvironment implements IModuleAwareNameEnvironment, SuffixConstants {

	LinkedHashSet<ClasspathLocation> locationSet;
	Map<String, IModuleDescription> modules;
	private boolean modulesComputed = false;
	Map<String,ClasspathLocation> moduleLocations;

	/*
	 * A map from the fully qualified slash-separated name of the main type (String) to the working copy
	 */
	Map<String, org.eclipse.jdt.core.ICompilationUnit> workingCopies;

public JavaSearchNameEnvironment(IJavaProject javaProject, org.eclipse.jdt.core.ICompilationUnit[] copies) {
	if (CompilerOptions.versionToJdkLevel(javaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true)) >= ClassFileConstants.JDK9) {
		this.moduleLocations = new HashMap<>();
	}
	this.modules = new HashMap<>();
	this.locationSet = computeClasspathLocations((JavaProject) javaProject);
	this.workingCopies = getWorkingCopyMap(copies);
}

public static Map<String, org.eclipse.jdt.core.ICompilationUnit> getWorkingCopyMap(
		org.eclipse.jdt.core.ICompilationUnit[] copies) {
	int length = copies == null ? 0 : copies.length;
	HashMap<String, org.eclipse.jdt.core.ICompilationUnit> result = new HashMap<>(length);
	try {
		if (copies != null) {
			for (int i = 0; i < length; i++) {
				org.eclipse.jdt.core.ICompilationUnit workingCopy = copies[i];
				IPackageDeclaration[] pkgs = workingCopy.getPackageDeclarations();
				String pkg = pkgs.length > 0 ? pkgs[0].getElementName() : ""; //$NON-NLS-1$
				String cuName = workingCopy.getElementName();
				String mainTypeName = Util.getNameWithoutJavaLikeExtension(cuName);
				String qualifiedMainTypeName = pkg.length() == 0 ? mainTypeName : pkg.replace('.', '/') + '/' + mainTypeName;
				result.put(qualifiedMainTypeName, workingCopy);
				// TODO : JAVA 9 - module-info.java has the same name across modules - Any issues here?
			}
		}
	} catch (JavaModelException e) {
		// working copy doesn't exist: cannot happen
	}
	return result;
}

public void cleanup() {
	this.locationSet.clear();
}

void addProjectClassPath(JavaProject javaProject) {
	LinkedHashSet<ClasspathLocation> locations = computeClasspathLocations(javaProject);
	if (locations != null) this.locationSet.addAll(locations);
}

private LinkedHashSet<ClasspathLocation> computeClasspathLocations(JavaProject javaProject) {

	IPackageFragmentRoot[] roots = null;
	try {
		roots = javaProject.getAllPackageFragmentRoots();
	} catch (JavaModelException e) {
		return null;// project doesn't exist
	}
	LinkedHashSet<ClasspathLocation> locations = new LinkedHashSet<ClasspathLocation>();
	int length = roots.length;
	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	for (int i = 0; i < length; i++) {
		ClasspathLocation cp = mapToClassPathLocation(manager, (PackageFragmentRoot) roots[i]);
		if (cp != null) locations.add(cp);
	}
	return locations;
}

private void computeModules() {
	if (!this.modulesComputed) {
		this.modulesComputed = true;
		JavaElementRequestor requestor = new JavaElementRequestor();
		try {
			JavaModelManager.getModulePathManager().seekModule(CharOperation.ALL_PREFIX, true, requestor);
			IModuleDescription[] mods = requestor.getModules();
			for (IModuleDescription mod : mods) {
				this.modules.putIfAbsent(mod.getElementName(), mod);
			}
		} catch (JavaModelException e) {
			// do nothing
		}
	}
}

private ClasspathLocation mapToClassPathLocation(JavaModelManager manager, PackageFragmentRoot root) {
	ClasspathLocation cp = null;
	IPath path = root.getPath();
	try {
		if (root.isArchive()) {
			ClasspathEntry rawClasspathEntry = (ClasspathEntry) root.getRawClasspathEntry();
			cp = JavaModelManager.isJrt(path) ? 
					new ClasspathJrt(path.toOSString(), 
							ClasspathEntry.getExternalAnnotationPath(rawClasspathEntry, ((IJavaProject)root.getParent()).getProject(), true), this) :
						new ClasspathJar(manager.getZipFile(path), rawClasspathEntry.getAccessRuleSet(),
								ClasspathEntry.getExternalAnnotationPath(rawClasspathEntry,
										((IJavaProject) root.getParent()).getProject(), true),
								this, rawClasspathEntry.isAutomaticModule());
		} else {
			Object target = JavaModel.getTarget(path, true);
			if (target != null) {
				if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
					cp = new ClasspathSourceDirectory((IContainer)target, root.fullExclusionPatternChars(), root.fullInclusionPatternChars());
				} else {
					ClasspathEntry rawClasspathEntry = (ClasspathEntry) root.getRawClasspathEntry();
					cp = ClasspathLocation.forBinaryFolder((IContainer) target, false, rawClasspathEntry.getAccessRuleSet(),
														ClasspathEntry.getExternalAnnotationPath(rawClasspathEntry, ((IJavaProject)root.getParent()).getProject(), true),
														this, rawClasspathEntry.isAutomaticModule());
				}
			}
		}
	} catch (CoreException e1) {
		// problem opening zip file or getting root kind
		// consider root corrupt and ignore
	}
	IModuleDescription imd = root.getModuleDescription();
	if (imd != null) {
		IModule mod = NameLookup.getModuleDescriptionInfo(imd);
		String moduleName = new String(mod.name());
		this.modules.put(moduleName, imd);
		cp.setModule(mod);
		this.moduleLocations.put(moduleName, cp);
	}
	return cp;
}

private NameEnvironmentAnswer findClass(String qualifiedTypeName, char[] typeName, /*@Nullable*/String moduleName) {
	String
		binaryFileName = null, qBinaryFileName = null,
		sourceFileName = null, qSourceFileName = null,
		qPackageName = null;
	NameEnvironmentAnswer suggestedAnswer = null;
	Iterator<ClasspathLocation> iter = getLocationsFor(moduleName);
	while (iter.hasNext()) {
		ClasspathLocation location = iter.next();
		NameEnvironmentAnswer answer;
		if (location instanceof ClasspathSourceDirectory) {
			if (sourceFileName == null) {
				qSourceFileName = qualifiedTypeName; // doesn't include the file extension
				sourceFileName = qSourceFileName;
				qPackageName =  ""; //$NON-NLS-1$
				if (qualifiedTypeName.length() > typeName.length) {
					int typeNameStart = qSourceFileName.length() - typeName.length;
					qPackageName =  qSourceFileName.substring(0, typeNameStart - 1);
					sourceFileName = qSourceFileName.substring(typeNameStart);
				}
			}
			ICompilationUnit workingCopy = (ICompilationUnit) this.workingCopies.get(qualifiedTypeName);
			if (workingCopy != null) {
				answer = new NameEnvironmentAnswer(workingCopy, null /*no access restriction*/);
			} else {
				answer = location.findClass(
					sourceFileName, // doesn't include the file extension
					qPackageName,
					moduleName,
					qSourceFileName);  // doesn't include the file extension
			}
		} else {
			if (binaryFileName == null) {
				qBinaryFileName = qualifiedTypeName + SUFFIX_STRING_class;
				binaryFileName = qBinaryFileName;
				qPackageName =  ""; //$NON-NLS-1$
				if (qualifiedTypeName.length() > typeName.length) {
					int typeNameStart = qBinaryFileName.length() - typeName.length - 6; // size of ".class"
					qPackageName =  qBinaryFileName.substring(0, typeNameStart - 1);
					binaryFileName = qBinaryFileName.substring(typeNameStart);
				}
			}
			answer =
				location.findClass(
					binaryFileName,
					qPackageName,
					moduleName,
					qBinaryFileName);
		}
		if (answer != null) {
			if (!answer.ignoreIfBetter()) {
				if (answer.isBetter(suggestedAnswer))
					return answer;
			} else if (answer.isBetter(suggestedAnswer))
				// remember suggestion and keep looking
				suggestedAnswer = answer;
		}
	}
	if (suggestedAnswer != null)
		// no better answer was found
		return suggestedAnswer;
	return null;
}

private Iterator<ClasspathLocation> getLocationsFor(/*@Nullable*/String moduleName) {
	if (moduleName != null) {
		ClasspathLocation moduleLocation = this.moduleLocations.get(moduleName);
		if (moduleLocation != null)
			return Collections.singletonList(moduleLocation).iterator();
	}
	return this.locationSet.iterator();
}

@Override
public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName, char[] moduleName) {
	if (typeName != null)
		return findClass(
			new String(CharOperation.concatWith(packageName, typeName, '/')),
			typeName,
			moduleName == ModuleBinding.ANY ? null : String.valueOf(moduleName));
	return null;
}

@Override
public NameEnvironmentAnswer findType(char[][] compoundName, char[] moduleName) {
	if (compoundName != null)
		return findClass(
			new String(CharOperation.concatWith(compoundName, '/')),
			compoundName[compoundName.length - 1],
			moduleName == ModuleBinding.ANY ? null : String.valueOf(moduleName));
	return null;
}

@Override
public char[][] getModulesDeclaringPackage(char[][] parentPackageName, char[] packageName, char[] moduleName) {
	String qualifiedPackageName = String.valueOf(CharOperation.concatWith(parentPackageName, packageName, '/'));
	if (this.moduleLocations == null || moduleName == ModuleBinding.UNNAMED) {
		for (ClasspathLocation location : this.locationSet) {
			IModule module = location.getModule();
			if (module == null && location.isPackage(qualifiedPackageName, null))
				return new char[][] { moduleName };
		}
		return null;
		
	} else if (moduleName == ModuleBinding.ANY) {
		char[][] moduleNames = CharOperation.NO_CHAR_CHAR;
		for (ClasspathLocation location : this.locationSet) {
			IModule module = location.getModule();
			if (module != null && location.isPackage(qualifiedPackageName, null))
				moduleNames = CharOperation.arrayConcat(moduleNames, module.name()); // FIXME(SHMOD): handle multiple modules per location
		}
		return moduleNames == CharOperation.NO_CHAR_CHAR ? null : moduleNames;
	} else {
		String moduleNameString = String.valueOf(moduleName);
		ClasspathLocation location = this.moduleLocations.get(moduleNameString);
		if (location != null) {
			if (location.isPackage(qualifiedPackageName, moduleNameString))
				return new char[][] { moduleName };
			return null;
		}
	}
	return null;
}

@Override
public IModule getModule(char[] moduleName) {
	computeModules();
	IModuleDescription moduleDesc = this.modules.get(new String(moduleName));
	IModule module = null;
	try {
		if (moduleDesc != null)
			module =  ((ModuleDescriptionInfo)((JavaElement) moduleDesc).getElementInfo());
	} catch (JavaModelException e) {
		// do nothing
	}
	return module;
}

@Override
public IModule[] getAllAutomaticModules() {
	if (this.moduleLocations == null || this.moduleLocations.size() == 0)
		return IModule.NO_MODULES;
	Set<IModule> set = this.moduleLocations.values().stream().map(e -> e.getModule()).filter(m -> m.isAutomatic())
			.collect(Collectors.toSet());
	return set.toArray(new IModule[set.size()]);
}
}
