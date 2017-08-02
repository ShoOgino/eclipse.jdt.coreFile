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
 *								Bug 440687 - [compiler][batch][null] improve command line option for external annotations
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.batch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.zip.ZipFile;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ExternalAnnotationDecorator;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.IModulePathEntry;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.IModuleAwareNameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.lookup.ModuleBinding;
import org.eclipse.jdt.internal.compiler.env.IUpdatableModule;
import org.eclipse.jdt.internal.compiler.env.IUpdatableModule.UpdateKind;
import org.eclipse.jdt.internal.compiler.env.IUpdatableModule.UpdatesByKind;
import org.eclipse.jdt.internal.compiler.util.JRTUtil;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.compiler.util.Util;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class FileSystem implements IModuleAwareNameEnvironment, SuffixConstants {

	// Keep the type as ArrayList and not List as there are clients that are already written to expect ArrayList.
	public static ArrayList<FileSystem.Classpath> EMPTY_CLASSPATH = new ArrayList<>();

	/**
	 * A <code>Classpath</code>, even though an IModuleLocation, can represent a plain
	 * classpath location too. The FileSystem tells the Classpath whether to behave as a module or regular class
	 * path via {@link Classpath#acceptModule(IModule)}.
	 *
	 * Sub types of classpath are responsible for appropriate behavior based on this.
	 */
	public interface Classpath extends IModulePathEntry {
		char[][][] findTypeNames(String qualifiedPackageName, String moduleName);
		NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName, String moduleName, String qualifiedBinaryFileName);
		NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName, String moduleName, String qualifiedBinaryFileName, boolean asBinaryOnly);
		boolean isPackage(String qualifiedPackageName, /*@Nullable*/String moduleName);
		default boolean hasModule() { return getModule() != null; }
		/**
		 * Return a list of the jar file names defined in the Class-Path section
		 * of the jar file manifest if any, null else. Only ClasspathJar (and
		 * extending classes) instances may return a non-null result.
		 * @param  problemReporter problem reporter with which potential
		 *         misconfiguration issues are raised
		 * @return a list of the jar file names defined in the Class-Path
		 *         section of the jar file manifest if any
		 */
		List<Classpath> fetchLinkedJars(ClasspathSectionProblemReporter problemReporter);
		/**
		 * This method resets the environment. The resulting state is equivalent to
		 * a new name environment without creating a new object.
		 */
		void reset();
		/**
		 * Return a normalized path for file based classpath entries. This is an
		 * absolute path in which file separators are transformed to the
		 * platform-agnostic '/', ending with a '/' for directories. This is an
		 * absolute path in which file separators are transformed to the
		 * platform-agnostic '/', deprived from the '.jar' (resp. '.zip')
		 * extension for jar (resp. zip) files.
		 * @return a normalized path for file based classpath entries
		 */
		char[] normalizedPath();
		/**
		 * Return the path for file based classpath entries. This is an absolute path
		 * ending with a file separator for directories, an absolute path including the '.jar'
		 * (resp. '.zip') extension for jar (resp. zip) files.
		 * @return the path for file based classpath entries
		 */
		String getPath();
		/**
		 * Initialize the entry
		 */
		void initialize() throws IOException;
		/**
		 * Can the current location provide an external annotation file for the given type?
		 * @param qualifiedTypeName type name in qualified /-separated notation.
		 */
		boolean hasAnnotationFileFor(String qualifiedTypeName);
		/**
		 * Accepts to represent a module location with the given module description.
		 *
		 * @param module
		 */
		public void acceptModule(IModule module);
		public String getDestinationPath();
		Collection<String> getModuleNames();
	}
	public interface ClasspathSectionProblemReporter {
		void invalidClasspathSection(String jarFilePath);
		void multipleClasspathSections(String jarFilePath);
	}

	/**
	 * This class is defined how to normalize the classpath entries.
	 * It removes duplicate entries.
	 */
	public static class ClasspathNormalizer {
		/**
		 * Returns the normalized classpath entries (no duplicate).
		 * <p>The given classpath entries are FileSystem.Classpath. We check the getPath() in order to find
		 * duplicate entries.</p>
		 *
		 * @param classpaths the given classpath entries
		 * @return the normalized classpath entries
		 */
		public static ArrayList normalize(ArrayList classpaths) {
			ArrayList normalizedClasspath = new ArrayList();
			HashSet cache = new HashSet();
			for (Iterator iterator = classpaths.iterator(); iterator.hasNext(); ) {
				FileSystem.Classpath classpath = (FileSystem.Classpath) iterator.next();
				if (!cache.contains(classpath)) {
					normalizedClasspath.add(classpath);
					cache.add(classpath);
				}
			}
			return normalizedClasspath;
		}
	}
	
	protected Classpath[] classpaths;
	// Used only in single-module mode when the module descriptor is
	// provided via command line.
	protected IModule module;
	Set knownFileNames;
	protected boolean annotationsFromClasspath; // should annotation files be read from the classpath (vs. explicit separate path)?
	private static HashMap<File, Classpath> JRT_CLASSPATH_CACHE = null;
	
	Map<String,Classpath> moduleLocations = new HashMap<>();

	/** Tasks resulting from --add-reads or --add-exports command line options. */
	Map<String,UpdatesByKind> moduleUpdates = new HashMap<>();

/*
	classPathNames is a collection is Strings representing the full path of each class path
	initialFileNames is a collection is Strings, the trailing '.java' will be removed if its not already.
*/
public FileSystem(String[] classpathNames, String[] initialFileNames, String encoding) {
	final int classpathSize = classpathNames.length;
	this.classpaths = new Classpath[classpathSize];
	int counter = 0;
	for (int i = 0; i < classpathSize; i++) {
		Classpath classpath = getClasspath(classpathNames[i], encoding, null, null);
		try {
			classpath.initialize();
			for (String moduleName : classpath.getModuleNames())
				this.moduleLocations.put(moduleName, classpath);
			this.classpaths[counter++] = classpath;
		} catch (IOException e) {
			// ignore
		}
	}
	if (counter != classpathSize) {
		System.arraycopy(this.classpaths, 0, (this.classpaths = new Classpath[counter]), 0, counter);
	}
	initializeKnownFileNames(initialFileNames);
}
protected FileSystem(Classpath[] paths, String[] initialFileNames, boolean annotationsFromClasspath) {
	final int length = paths.length;
	int counter = 0;
	this.classpaths = new FileSystem.Classpath[length];
	for (int i = 0; i < length; i++) {
		final Classpath classpath = paths[i];
		try {
			classpath.initialize();
			for (String moduleName : classpath.getModuleNames())
				this.moduleLocations.put(moduleName, classpath);
			this.classpaths[counter++] = classpath;
		} catch(IOException | IllegalArgumentException exception) {
			// JRE 9 could throw an IAE if the linked JAR paths have invalid chars, such as ":"
			// ignore
		}
	}
	if (counter != length) {
		// should not happen
		System.arraycopy(this.classpaths, 0, (this.classpaths = new FileSystem.Classpath[counter]), 0, counter);
	}
	initializeKnownFileNames(initialFileNames);
	this.annotationsFromClasspath = annotationsFromClasspath;
}
public static Classpath getClasspath(String classpathName, String encoding, AccessRuleSet accessRuleSet) {
	return getClasspath(classpathName, encoding, false, accessRuleSet, null, null);
}
public static Classpath getClasspath(String classpathName, String encoding, AccessRuleSet accessRuleSet, Map options) {
	return getClasspath(classpathName, encoding, false, accessRuleSet, null, options);
}
public static Classpath getJrtClasspath(String jdkHome, String encoding, AccessRuleSet accessRuleSet, Map options) {
	return new ClasspathJrt(new File(convertPathSeparators(jdkHome)), true, accessRuleSet, null);
}
public static Classpath getClasspath(String classpathName, String encoding,
		boolean isSourceOnly, AccessRuleSet accessRuleSet,
		String destinationPath, Map options) {
	Classpath result = null;
	File file = new File(convertPathSeparators(classpathName));
	if (file.isDirectory()) {
		if (file.exists()) {
			result = new ClasspathDirectory(file, encoding,
					isSourceOnly ? ClasspathLocation.SOURCE :
						ClasspathLocation.SOURCE | ClasspathLocation.BINARY,
					accessRuleSet,
					destinationPath == null || destinationPath == Main.NONE ?
						destinationPath : // keep == comparison valid
						convertPathSeparators(destinationPath), options);
		}
	} else {
		int format = Util.archiveFormat(classpathName);
		if (format == Util.ZIP_FILE) {
			if (isSourceOnly) {
				// source only mode
				result = new ClasspathSourceJar(file, true, accessRuleSet,
					encoding,
					destinationPath == null || destinationPath == Main.NONE ?
						destinationPath : // keep == comparison valid
						convertPathSeparators(destinationPath));
			} else if (destinationPath == null) {
				// class file only mode
				if (classpathName.endsWith(JRTUtil.JRT_FS_JAR)) {
					if (JRT_CLASSPATH_CACHE == null) {
						JRT_CLASSPATH_CACHE = new HashMap<>();
					} else {
						result = JRT_CLASSPATH_CACHE.get(file);
					}
					if (result == null) {
						result = new ClasspathJrt(file, true, accessRuleSet, null);
						try {
							result.initialize();
						} catch (IOException e) {
							// Broken entry, but let clients have it anyway.
						}
						JRT_CLASSPATH_CACHE.put(file, result);
					}
				} else {
					result = new ClasspathJar(file, true, accessRuleSet, null);
				}
			}
		} else if (format == Util.JMOD_FILE) {
			// TODO BETA_JAVA9: we need new type of classpath to handle Jmod files in batch compiler.
		}

	}
	return result;
}
private void initializeKnownFileNames(String[] initialFileNames) {
	if (initialFileNames == null) {
		this.knownFileNames = new HashSet(0);
		return;
	}
	this.knownFileNames = new HashSet(initialFileNames.length * 2);
	for (int i = initialFileNames.length; --i >= 0;) {
		File compilationUnitFile = new File(initialFileNames[i]);
		char[] fileName = null;
		try {
			fileName = compilationUnitFile.getCanonicalPath().toCharArray();
		} catch (IOException e) {
			// this should not happen as the file exists
			continue;
		}
		char[] matchingPathName = null;
		final int lastIndexOf = CharOperation.lastIndexOf('.', fileName);
		if (lastIndexOf != -1) {
			fileName = CharOperation.subarray(fileName, 0, lastIndexOf);
		}
		CharOperation.replace(fileName, '\\', '/');
		boolean globalPathMatches = false;
		// the most nested path should be the selected one
		for (int j = 0, max = this.classpaths.length; j < max; j++) {
			char[] matchCandidate = this.classpaths[j].normalizedPath();
			boolean currentPathMatch = false;
			if (this.classpaths[j] instanceof ClasspathDirectory
					&& CharOperation.prefixEquals(matchCandidate, fileName)) {
				currentPathMatch = true;
				if (matchingPathName == null) {
					matchingPathName = matchCandidate;
				} else {
					if (currentPathMatch) {
						// we have a second source folder that matches the path of the source file
						if (matchCandidate.length > matchingPathName.length) {
							// we want to preserve the shortest possible path
							matchingPathName = matchCandidate;
						}
					} else {
						// we want to preserve the shortest possible path
						if (!globalPathMatches && matchCandidate.length < matchingPathName.length) {
							matchingPathName = matchCandidate;
						}
					}
				}
				if (currentPathMatch) {
					globalPathMatches = true;
				}
			}
		}
		if (matchingPathName == null) {
			this.knownFileNames.add(new String(fileName)); // leave as is...
		} else {
			this.knownFileNames.add(new String(CharOperation.subarray(fileName, matchingPathName.length, fileName.length)));
		}
		matchingPathName = null;
	}
}
public void cleanup() {
	for (int i = 0, max = this.classpaths.length; i < max; i++)
		this.classpaths[i].reset();
}
private static String convertPathSeparators(String path) {
	return File.separatorChar == '/'
		? path.replace('\\', '/')
		 : path.replace('/', '\\');
}
private NameEnvironmentAnswer findClass(String qualifiedTypeName, char[] typeName, boolean asBinaryOnly, /*NonNull*/char[] moduleName) {
	NameEnvironmentAnswer answer = internalFindClass(qualifiedTypeName, typeName, asBinaryOnly, moduleName);
	if (this.annotationsFromClasspath && answer != null && answer.getBinaryType() instanceof ClassFileReader) {
		for (int i = 0, length = this.classpaths.length; i < length; i++) {
			Classpath classpathEntry = this.classpaths[i];
			if (classpathEntry.hasAnnotationFileFor(qualifiedTypeName)) {
				// in case of 'this.annotationsFromClasspath' we indeed search for .eea entries inside the main zipFile of the entry:
				ZipFile zip = classpathEntry instanceof ClasspathJar ? ((ClasspathJar) classpathEntry).zipFile : null;
				boolean shouldClose = false; // don't close classpathEntry.zipFile, which we don't own
				try {
					if (zip == null) {
						zip = ExternalAnnotationDecorator.getAnnotationZipFile(classpathEntry.getPath(), null);
						shouldClose = true;
					}
					answer.setBinaryType(ExternalAnnotationDecorator.create(answer.getBinaryType(), classpathEntry.getPath(), 
							qualifiedTypeName, zip));
					return answer;
				} catch (IOException e) {
					// ignore broken entry, keep searching
				} finally {
					if (shouldClose && zip != null)
						try {
							zip.close();
						} catch (IOException e) { /* nothing */ }
				}
			}
		}
		// globally configured (annotationsFromClasspath), but no .eea found, decorate in order to answer NO_EEA_FILE:
		answer.setBinaryType(new ExternalAnnotationDecorator(answer.getBinaryType(), null));
	}
	return answer;
}
private NameEnvironmentAnswer internalFindClass(String qualifiedTypeName, char[] typeName, boolean asBinaryOnly, /*NonNull*/char[] moduleName) {
	if (this.knownFileNames.contains(qualifiedTypeName)) return null; // looking for a file which we know was provided at the beginning of the compilation

	String qualifiedBinaryFileName = qualifiedTypeName + SUFFIX_STRING_class;
	String qualifiedPackageName =
		qualifiedTypeName.length() == typeName.length
			? Util.EMPTY_STRING
			: qualifiedBinaryFileName.substring(0, qualifiedTypeName.length() - typeName.length - 1);

	LookupStrategy strategy = LookupStrategy.get(moduleName);
	if (strategy == LookupStrategy.Named) {
		if (this.moduleLocations != null) {
			// searching for a specific named module:
			String moduleNameString = String.valueOf(moduleName);
			Classpath classpath = this.moduleLocations.get(moduleNameString);
			if (classpath != null) {
				return classpath.findClass(typeName, qualifiedPackageName, moduleNameString, qualifiedBinaryFileName);
			}
		}
		return null;
	}
	String qp2 = File.separatorChar == '/' ? qualifiedPackageName : qualifiedPackageName.replace('/', File.separatorChar);
	NameEnvironmentAnswer suggestedAnswer = null;
	if (qualifiedPackageName == qp2) {
		for (int i = 0, length = this.classpaths.length; i < length; i++) {
			if (!strategy.matches(this.classpaths[i], Classpath::hasModule))
				continue;
			NameEnvironmentAnswer answer = this.classpaths[i].findClass(typeName, qualifiedPackageName, null, qualifiedBinaryFileName, asBinaryOnly);
			if (answer != null) {
				if (!answer.ignoreIfBetter()) {
					if (answer.isBetter(suggestedAnswer))
						return answer;
				} else if (answer.isBetter(suggestedAnswer))
					// remember suggestion and keep looking
					suggestedAnswer = answer;
			}
		}
	} else {
		String qb2 = qualifiedBinaryFileName.replace('/', File.separatorChar);
		for (int i = 0, length = this.classpaths.length; i < length; i++) {
			Classpath p = this.classpaths[i];
			if (!strategy.matches(p, Classpath::hasModule))
				continue;
			NameEnvironmentAnswer answer = (p instanceof ClasspathJar || p instanceof ClasspathJrt)
				? p.findClass(typeName, qualifiedPackageName, null, qualifiedBinaryFileName, asBinaryOnly)
				: p.findClass(typeName, qp2, null, qb2, asBinaryOnly);
			if (answer != null) {
				if (!answer.ignoreIfBetter()) {
					if (answer.isBetter(suggestedAnswer))
						return answer;
				} else if (answer.isBetter(suggestedAnswer))
					// remember suggestion and keep looking
					suggestedAnswer = answer;
			}
		}
	}
	return suggestedAnswer;
}

public NameEnvironmentAnswer findType(char[][] compoundName, char[] moduleName) {
	if (compoundName != null)
		return findClass(
			new String(CharOperation.concatWith(compoundName, '/')),
			compoundName[compoundName.length - 1],
			false,
			moduleName);
	return null;
}
public char[][][] findTypeNames(char[][] packageName, String[] moduleNames) {
	char[][][] result = null;
	if (packageName != null) {
		String qualifiedPackageName = new String(CharOperation.concatWith(packageName, '/'));
		String qualifiedPackageName2 = File.separatorChar == '/' ? qualifiedPackageName : qualifiedPackageName.replace('/', File.separatorChar);
		if (qualifiedPackageName == qualifiedPackageName2) {
			for (int i = 0, length = this.classpaths.length; i < length; i++) {
				for (String moduleName : moduleNames) {
					if (moduleName !=  null && !this.classpaths[i].servesModule(moduleName.toCharArray())) continue;
					char[][][] answers = this.classpaths[i].findTypeNames(qualifiedPackageName, moduleName);
					if (answers != null) {
						// concat with previous answers
						if (result == null) {
							result = answers;
						} else {
							int resultLength = result.length;
							int answersLength = answers.length;
							System.arraycopy(result, 0, (result = new char[answersLength + resultLength][][]), 0, resultLength);
							System.arraycopy(answers, 0, result, resultLength, answersLength);
						}
					}
				}
			}
		} else {
			for (int i = 0, length = this.classpaths.length; i < length; i++) {
				Classpath p = this.classpaths[i];
				for (String moduleName : moduleNames) {
					if (moduleName != null && !p.servesModule(moduleName.toCharArray())) continue;
					char[][][] answers = (p instanceof ClasspathJar)
							? p.findTypeNames(qualifiedPackageName, moduleName)
							: p.findTypeNames(qualifiedPackageName2, moduleName);
						if (answers != null) {
							// concat with previous answers
							if (result == null) {
								result = answers;
							} else {
								int resultLength = result.length;
								int answersLength = answers.length;
								System.arraycopy(result, 0, (result = new char[answersLength + resultLength][][]), 0, resultLength);
								System.arraycopy(answers, 0, result, resultLength, answersLength);
							}
						}
				}
			}
		}
	}
	return result;
}

public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName, char[] moduleName) {
	if (typeName != null)
		return findClass(
			new String(CharOperation.concatWith(packageName, typeName, '/')),
			typeName,
			false,
			moduleName);
	return null;
}

public char[][] getModulesDeclaringPackage(char[][] parentPackageName, char[] packageName, char[] moduleName) {
	String qualifiedPackageName = new String(CharOperation.concatWith(parentPackageName, packageName, '/'));
	String moduleNameString = String.valueOf(moduleName);

	LookupStrategy strategy = LookupStrategy.get(moduleName);
	if (strategy == LookupStrategy.Named) {
		if (this.moduleLocations != null) {
			// specific search in a given module:
			Classpath classpath = this.moduleLocations.get(moduleNameString);
			if (classpath != null) {
				if (classpath.isPackage(qualifiedPackageName, moduleNameString))
					return new char[][] {moduleName};
			}
		}
		return null;
	}
	// search the entire environment and answer which modules declare that package:
	char[][] allNames = null;
	for (Classpath cp : this.classpaths) {
		if (strategy.matches(cp, Classpath::hasModule)) {
			if (strategy == LookupStrategy.Unnamed) {
				// short-cut
				if (cp.isPackage(qualifiedPackageName, moduleNameString))
					return new char[][] { ModuleBinding.UNNAMED };
			} else {
				char[][] declaringModules = cp.getModulesDeclaringPackage(qualifiedPackageName, null);
				if (declaringModules != null) {
					if (allNames == null)
						allNames = declaringModules;
					else
						allNames = CharOperation.arrayConcat(allNames, declaringModules);
				}
			}
		}
	}
	return allNames;
}

@Override
public boolean hasCompilationUnit(char[][] qualifiedPackageName, char[] moduleName) {
	String qPackageName = String.valueOf(CharOperation.concatWith(qualifiedPackageName, '/'));
	String moduleNameString = String.valueOf(moduleName);
	LookupStrategy strategy = LookupStrategy.get(moduleName);
	switch (strategy) {
		case Named:
			if (this.moduleLocations != null) {
				Classpath location = this.moduleLocations.get(moduleNameString);
				if (location != null)
					return location.hasCompilationUnit(qPackageName, moduleNameString);
			}
			return false;
		default:
			for (int i = 0; i < this.classpaths.length; i++) {
				Classpath location = this.classpaths[i];
				if (strategy.matches(location, Classpath::hasModule))
					if (location.hasCompilationUnit(qPackageName, moduleNameString))
						return true;
			}
			return false;
	}
}

@Override
public IModule getModule(char[] name) {
	if (this.module != null && CharOperation.equals(name, this.module.name())) {
		return this.module;
	}
	for (Classpath classpath : this.classpaths) {
		IModule mod = classpath.getModule(name);
		if (mod != null) {
			return mod;
		}
	}
	return null;
}

@Override
public IModule[] getAllAutomaticModules() {
	Set<IModule> set = new HashSet<>();
	for (int i = 0, l = this.classpaths.length; i < l; i++) {
		if (this.classpaths[i].isAutomaticModule()) {
			set.add(this.classpaths[i].getModule());
		}
	}
	return set.toArray(new IModule[set.size()]);
}

void addModuleUpdate(String moduleName, Consumer<IUpdatableModule> update, UpdateKind kind) {
	UpdatesByKind updates = this.moduleUpdates.get(moduleName);
	if (updates == null) {
		this.moduleUpdates.put(moduleName, updates = new UpdatesByKind());
	}
	updates.getList(kind, true).add(update);
}
@Override
public void applyModuleUpdates(IUpdatableModule compilerModule, IUpdatableModule.UpdateKind kind) {
	char[] name = compilerModule.name();
	if (name != ModuleBinding.UNNAMED) { // can't update the unnamed module
		UpdatesByKind updates = this.moduleUpdates.get(String.valueOf(name));
		if (updates != null) {
			for (Consumer<IUpdatableModule> update : updates.getList(kind, false))
				update.accept(compilerModule);
		}
	}
}
}
