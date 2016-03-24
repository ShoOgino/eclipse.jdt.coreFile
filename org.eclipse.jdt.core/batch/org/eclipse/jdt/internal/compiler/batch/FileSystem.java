/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipFile;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.IModuleLocation;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.lookup.ModuleEnvironment;
import org.eclipse.jdt.internal.compiler.util.JRTUtil;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.compiler.util.Util;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class FileSystem extends ModuleEnvironment implements SuffixConstants {
	public interface Classpath extends IModuleLocation {
		char[][][] findTypeNames(String qualifiedPackageName, IModule module);
		/**
		 * Return a list of the jar file names defined in the Class-Path section
		 * of the jar file manifest if any, null else. Only ClasspathJar (and
		 * extending classes) instances may return a non-null result.
		 * @param  problemReporter problem reporter with which potential
		 *         misconfiguration issues are raised
		 * @return a list of the jar file names defined in the Class-Path
		 *         section of the jar file manifest if any
		 */
		List fetchLinkedJars(ClasspathSectionProblemReporter problemReporter);
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
	Set knownFileNames;
	protected boolean annotationsFromClasspath; // should annotation files be read from the classpath (vs. explicit separate path)?
	private static HashMap<File, Classpath> JRT_CLASSPATH_CACHE = null;

/*
	classPathNames is a collection is Strings representing the full path of each class path
	initialFileNames is a collection is Strings, the trailing '.java' will be removed if its not already.
*/
public FileSystem(String[] classpathNames, String[] initialFileNames, String encoding) {
//	this.ms = new ModuleSystem(this);
	final int classpathSize = classpathNames.length;
	this.classpaths = new Classpath[classpathSize];
	int counter = 0;
	for (int i = 0; i < classpathSize; i++) {
		Classpath classpath = getClasspath(classpathNames[i], encoding, null, null);
		try {
			classpath.initialize();
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
//	this.ms = new ModuleSystem(this);
	final int length = paths.length;
	int counter = 0;
	this.classpaths = new FileSystem.Classpath[length];
	for (int i = 0; i < length; i++) {
		final Classpath classpath = paths[i];
		try {
			classpath.initialize();
			this.classpaths[counter++] = classpath;
		} catch(IOException | IllegalArgumentException exception) {
			// JRE 9 could through an IAE if the linked JAR paths have invalid chars, such as ":"
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
		if (format > -1) {
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
						result = new ClasspathJar(file, true, accessRuleSet, null, true);
						JRT_CLASSPATH_CACHE.put(file, result);
					}
				} else {
					result = new ClasspathJar(file, true, accessRuleSet, null, false);
				}
			}
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
private NameEnvironmentAnswer findClass(String qualifiedTypeName, char[] typeName, boolean asBinaryOnly, IModule[] modules){
	NameEnvironmentAnswer answer = internalFindClass(qualifiedTypeName, typeName, asBinaryOnly, modules);
	if (this.annotationsFromClasspath && answer != null && answer.getBinaryType() instanceof ClassFileReader) {
		for (int i = 0, length = this.classpaths.length; i < length; i++) {
			Classpath classpathEntry = this.classpaths[i];
			if (classpathEntry.hasAnnotationFileFor(qualifiedTypeName)) {
				ZipFile zip = classpathEntry instanceof ClasspathJar ? ((ClasspathJar) classpathEntry).zipFile : null;
				try {
					((ClassFileReader) answer.getBinaryType()).setExternalAnnotationProvider(classpathEntry.getPath(), qualifiedTypeName, zip, null);
					break;
				} catch (IOException e) {
					// ignore broken entry, keep searching
				}
			}
		}
	}
	return answer;
}
private NameEnvironmentAnswer internalFindClass(String qualifiedTypeName, char[] typeName, boolean asBinaryOnly, IModule[] modules){
	if (this.knownFileNames.contains(qualifiedTypeName)) return null; // looking for a file which we know was provided at the beginning of the compilation

	String qualifiedBinaryFileName = qualifiedTypeName + SUFFIX_STRING_class;
	String qualifiedPackageName =
		qualifiedTypeName.length() == typeName.length
			? Util.EMPTY_STRING
			: qualifiedBinaryFileName.substring(0, qualifiedTypeName.length() - typeName.length - 1);
	String qp2 = File.separatorChar == '/' ? qualifiedPackageName : qualifiedPackageName.replace('/', File.separatorChar);
	NameEnvironmentAnswer suggestedAnswer = null;
	if (qualifiedPackageName == qp2) {
		for (int i = 0, length = this.classpaths.length; i < length; i++) {
			NameEnvironmentAnswer answer = null;
			for (IModule iModule : modules) {
				if (!this.classpaths[i].servesModule(iModule)) continue;
				answer = this.classpaths[i].findClass(new String(typeName), qualifiedPackageName, qualifiedBinaryFileName, asBinaryOnly, iModule);
				if (answer != null) break;
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
	} else {
		String qb2 = qualifiedBinaryFileName.replace('/', File.separatorChar);
		for (int i = 0, length = this.classpaths.length; i < length; i++) {
			Classpath p = this.classpaths[i];
			NameEnvironmentAnswer answer = null;
			for (IModule iModule : modules) {
				if (!p.servesModule(iModule)) continue;
				answer = (p instanceof ClasspathJar)
						? p.findClass(new String(typeName), qualifiedPackageName, qualifiedBinaryFileName, asBinaryOnly, iModule)
								: p.findClass(new String(typeName), qp2, qb2, asBinaryOnly, iModule);
						if (answer != null) break;
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
	}
	if (suggestedAnswer != null)
		// no better answer was found
		return suggestedAnswer;
	return null;
}
public NameEnvironmentAnswer findType(char[][] compoundName, IModule[] modules) {
	if (compoundName != null)
		return findClass(
			new String(CharOperation.concatWith(compoundName, '/')),
			compoundName[compoundName.length - 1],
			false, modules);
	return null;
}
public char[][][] findTypeNames(char[][] packageName, IModule[] modules) {
	char[][][] result = null;
	if (packageName != null) {
		String qualifiedPackageName = new String(CharOperation.concatWith(packageName, '/'));
		String qualifiedPackageName2 = File.separatorChar == '/' ? qualifiedPackageName : qualifiedPackageName.replace('/', File.separatorChar);
		if (qualifiedPackageName == qualifiedPackageName2) {
			for (int i = 0, length = this.classpaths.length; i < length; i++) {
				for (IModule mod : modules) {
					if (!this.classpaths[i].servesModule(mod)) continue;
					char[][][] answers = this.classpaths[i].findTypeNames(qualifiedPackageName, mod);
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
				for (IModule mod : modules) {
					if (!p.servesModule(mod)) continue;
					char[][][] answers = (p instanceof ClasspathJar)
							? p.findTypeNames(qualifiedPackageName, mod)
							: p.findTypeNames(qualifiedPackageName2, mod);
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
public NameEnvironmentAnswer findType(char[][] compoundName, boolean asBinaryOnly, IModule[] modules) {
	if (compoundName != null)
		return findClass(
			new String(CharOperation.concatWith(compoundName, '/')),
			compoundName[compoundName.length - 1],
			asBinaryOnly, modules);
	return null;
}
public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName, IModule[] modules) {
	if (typeName != null)
		return findClass(
			new String(CharOperation.concatWith(packageName, typeName, '/')),
			typeName,
			false,
			modules);
	return null;
}
public boolean isPackage(char[][] compoundName, char[] packageName, IModule[] modules) {
	String qualifiedPackageName = new String(CharOperation.concatWith(compoundName, packageName, '/'));
	String qp2 = File.separatorChar == '/' ? qualifiedPackageName : qualifiedPackageName.replace('/', File.separatorChar);
	if (qualifiedPackageName == qp2) {
		for (int i = 0, length = this.classpaths.length; i < length; i++)
			for (IModule iModule : modules) {
				if (!this.classpaths[i].servesModule(iModule)) continue;
				if (this.classpaths[i].isPackage(qualifiedPackageName))
					return true;
			}
	} else {
		for (int i = 0, length = this.classpaths.length; i < length; i++) {
			Classpath p = this.classpaths[i];
			for (IModule iModule : modules) {
				if (!p.servesModule(iModule)) continue;
				if ((p instanceof ClasspathJar) ? p.isPackage(qualifiedPackageName) : p.isPackage(qp2))
					return true;
			}
		}
	}
	return false;
}
@Override
public IModule getModule(char[] name) {
	// TODO Auto-generated method stub
	return null;
}
}
