/*******************************************************************************
 * Copyright (c) 2016, 2017 IBM Corporation.
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
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.batch;

import java.io.File;
import java.io.IOException;

import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.classfmt.ExternalAnnotationDecorator;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.IMultiModuleEntry;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding.ExternalAnnotationStatus;
import org.eclipse.jdt.internal.compiler.util.JRTUtil;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ClasspathJrt extends ClasspathLocation implements IMultiModuleEntry {
	protected File file;
	protected ZipFile annotationZipFile;
	protected boolean closeZipFileAtEnd;
	private static HashMap<String, Map<String,IModule>> ModulesCache = new HashMap<>();
	//private Set<String> packageCache;
	protected List<String> annotationPaths;

	public ClasspathJrt(File file, boolean closeZipFileAtEnd,
			AccessRuleSet accessRuleSet, String destinationPath) {
		super(accessRuleSet, destinationPath);
		this.file = file;
		this.closeZipFileAtEnd = closeZipFileAtEnd;
	}

	public List fetchLinkedJars(FileSystem.ClasspathSectionProblemReporter problemReporter) {
		return null;
	}
	public char[][] getModulesDeclaringPackage(String qualifiedPackageName, String moduleName) {
		List<String> modules = JRTUtil.getModulesDeclaringPackage(this.file, qualifiedPackageName, moduleName);
		return CharOperation.toCharArrays(modules);
	}
	@Override
	public boolean hasCompilationUnit(String qualifiedPackageName, String moduleName) {
		return JRTUtil.hasCompilationUnit(this.file, qualifiedPackageName, moduleName);
	}
	public NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName, String moduleName, String qualifiedBinaryFileName) {
		return findClass(typeName, qualifiedPackageName, moduleName, qualifiedBinaryFileName, false);
	}
	public NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName, String moduleName, String qualifiedBinaryFileName, boolean asBinaryOnly) {
		if (!isPackage(qualifiedPackageName, moduleName))
			return null; // most common case

		try {
			IBinaryType reader = ClassFileReader.readFromModule(this.file, moduleName, qualifiedBinaryFileName);

			if (reader != null) {
				searchPaths:
				if (this.annotationPaths != null) {
					String qualifiedClassName = qualifiedBinaryFileName.substring(0, qualifiedBinaryFileName.length()-SuffixConstants.EXTENSION_CLASS.length()-1);
					for (String annotationPath : this.annotationPaths) {
						try {
							if (this.annotationZipFile == null) {
								this.annotationZipFile = ExternalAnnotationDecorator.getAnnotationZipFile(annotationPath, null);
							}
							reader = ExternalAnnotationDecorator.create(reader, annotationPath, qualifiedClassName, this.annotationZipFile);

							if (reader.getExternalAnnotationStatus() == ExternalAnnotationStatus.TYPE_IS_ANNOTATED) {
								break searchPaths;
							}
						} catch (IOException e) {
							// don't let error on annotations fail class reading
						}
					}
					// location is configured for external annotations, but no .eea found, decorate in order to answer NO_EEA_FILE:
					reader = new ExternalAnnotationDecorator(reader, null);
				}
				char[] answerModuleName = reader.getModule();
				if (answerModuleName == null && moduleName != null)
					answerModuleName = moduleName.toCharArray();
				return new NameEnvironmentAnswer(reader, fetchAccessRestriction(qualifiedBinaryFileName), answerModuleName);
			}
		} catch(ClassFormatException e) {
			// treat as if class file is missing
		} catch (IOException e) {
			// treat as if class file is missing
		}
		return null;
	}
	@Override
	public boolean hasAnnotationFileFor(String qualifiedTypeName) {
		return false; // TODO(SHMOD): implement
	}
	public char[][][] findTypeNames(final String qualifiedPackageName, final String moduleName) {
		if (!isPackage(qualifiedPackageName, moduleName))
			return null; // most common case
		final char[] packageArray = qualifiedPackageName.toCharArray();
		final ArrayList answers = new ArrayList();
	
		try {
			JRTUtil.walkModuleImage(this.file, new JRTUtil.JrtFileVisitor<java.nio.file.Path>() {

				@Override
				public FileVisitResult visitPackage(java.nio.file.Path dir, java.nio.file.Path modPath, BasicFileAttributes attrs) throws IOException {
					if (qualifiedPackageName.startsWith(dir.toString())) {
						return FileVisitResult.CONTINUE;	
					}
					return FileVisitResult.SKIP_SUBTREE;
				}

				@Override
				public FileVisitResult visitFile(java.nio.file.Path dir, java.nio.file.Path modPath, BasicFileAttributes attrs) throws IOException {
					if (!dir.getParent().toString().equals(qualifiedPackageName)) {
						return FileVisitResult.CONTINUE;
					}
					String fileName = dir.getName(dir.getNameCount() - 1).toString();
					// The path already excludes the folders and all the '/', hence the -1 for last index of '/'
					addTypeName(answers, fileName, -1, packageArray);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitModule(java.nio.file.Path modPath) throws IOException {
					if (moduleName == null)
						return FileVisitResult.CONTINUE;
					if (!moduleName.equals(modPath.toString())) {
						return FileVisitResult.SKIP_SUBTREE;
					}
					return FileVisitResult.CONTINUE;
				}

			}, JRTUtil.NOTIFY_ALL);
		} catch (IOException e) {
			// Ignore and move on
		}
		
		int size = answers.size();
		if (size != 0) {
			char[][][] result = new char[size][][];
			answers.toArray(result);
			return result;
		}
		return null;
	}

	protected void addTypeName(final ArrayList answers, String fileName, int last, char[] packageName) {
		int indexOfDot = fileName.lastIndexOf('.');
		if (indexOfDot != -1) {
			String typeName = fileName.substring(last + 1, indexOfDot);
			answers.add(
				CharOperation.arrayConcat(
					CharOperation.splitOn('/', packageName),
					typeName.toCharArray()));
		}
	}
	public void initialize() throws IOException {
		loadModules();
	}
//	public void acceptModule(IModuleDeclaration mod) {
//		if (this.isJrt) 
//			return;
//		this.module = mod;
//	}
	public void loadModules() {
		Map<String,IModule> cache = ModulesCache.get(this.file.getPath());

		if (cache == null) {
			try {
				org.eclipse.jdt.internal.compiler.util.JRTUtil.walkModuleImage(this.file,
						new org.eclipse.jdt.internal.compiler.util.JRTUtil.JrtFileVisitor<Path>() {

					@Override
					public FileVisitResult visitPackage(Path dir, Path mod, BasicFileAttributes attrs)
							throws IOException {
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFile(Path f, Path mod, BasicFileAttributes attrs)
							throws IOException {
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitModule(Path mod) throws IOException {
						try {
							ClasspathJrt.this.acceptModule(JRTUtil.getClassfileContent(ClasspathJrt.this.file, IModule.MODULE_INFO_CLASS, mod.toString()));
						} catch (ClassFormatException e) {
							e.printStackTrace();
						}
						return FileVisitResult.SKIP_SUBTREE;
					}
				}, JRTUtil.NOTIFY_MODULES);
			} catch (IOException e) {
				// TODO: BETA_JAVA9 Should report better
			}
		}
	}
	void acceptModule(ClassFileReader reader) {
		if (reader != null) {
			IModule moduleDecl = reader.getModuleDeclaration();
			if (moduleDecl != null) {
				Map<String, IModule> cache = ModulesCache.get(this.file.getPath());
				if (cache == null) {
					ModulesCache.put(this.file.getPath(), cache = new HashMap<String,IModule>());
				}
				cache.put(String.valueOf(moduleDecl.name()), moduleDecl);
			}
		}
		
	}
	void acceptModule(byte[] content) {
		if (content == null) 
			return;
		ClassFileReader reader = null;
		try {
			reader = new ClassFileReader(content, IModule.MODULE_INFO_CLASS.toCharArray());
		} catch (ClassFormatException e) {
			e.printStackTrace();
		}
		if (reader != null) {
			acceptModule(reader);
		}
	}
	
	@Override
	public Collection<String> getModuleNames() {
		return ModulesCache.values().stream()
				.flatMap(entryMap -> entryMap.keySet().stream())
				.collect(Collectors.toList());
	}
//	protected void addToPackageCache(String fileName, boolean endsWithSep) {
//		int last = endsWithSep ? fileName.length() : fileName.lastIndexOf('/');
//		while (last > 0) {
//			// extract the package name
//			String packageName = fileName.substring(0, last);
//			if (this.packageCache.contains(packageName))
//				return;
//			this.packageCache.add(packageName);
//			last = packageName.lastIndexOf('/');
//		}
//	}
//	public synchronized boolean isPackage(String qualifiedPackageName) {
//		if (this.packageCache != null)
//			return this.packageCache.contains(qualifiedPackageName);
//
//		this.packageCache = new HashSet<>(41);
//		this.packageCache.add(Util.EMPTY_STRING);
//		
//			try {
//				JRTUtil.walkModuleImage(this.file, new JRTUtil.JrtFileVisitor<java.nio.file.Path>() {
//
//					@Override
//					public FileVisitResult visitPackage(java.nio.file.Path dir, java.nio.file.Path mod, BasicFileAttributes attrs) throws IOException {
//						addToPackageCache(dir.toString(), true);
//						return FileVisitResult.CONTINUE;
//					}
//
//					@Override
//					public FileVisitResult visitFile(java.nio.file.Path dir, java.nio.file.Path mod, BasicFileAttributes attrs) throws IOException {
//						return FileVisitResult.CONTINUE;
//					}
//
//					@Override
//					public FileVisitResult visitModule(java.nio.file.Path mod) throws IOException {
//						return FileVisitResult.CONTINUE;
//					}
//
//				}, JRTUtil.NOTIFY_PACKAGES);
//			} catch (IOException e) {
//				// Ignore and move on
//			}
//		return this.packageCache.contains(qualifiedPackageName);
//	}
	public void reset() {
		if (this.closeZipFileAtEnd) {
			if (this.annotationZipFile != null) {
				try {
					this.annotationZipFile.close();
				} catch(IOException e) {
					// ignore
				}
				this.annotationZipFile = null;
			}
		}
		if (this.annotationPaths != null) {
			//this.packageCache = null;
			this.annotationPaths = null;
		}
	}
	public String toString() {
		return "Classpath for JRT System " + this.file.getPath(); //$NON-NLS-1$
	}
	public char[] normalizedPath() {
		if (this.normalizedPath == null) {
			String path2 = this.getPath();
			char[] rawName = path2.toCharArray();
			if (File.separatorChar == '\\') {
				CharOperation.replace(rawName, '\\', '/');
			}
			this.normalizedPath = CharOperation.subarray(rawName, 0, CharOperation.lastIndexOf('.', rawName));
		}
		return this.normalizedPath;
	}
	public String getPath() {
		if (this.path == null) {
			try {
				this.path = this.file.getCanonicalPath();
			} catch (IOException e) {
				// in case of error, simply return the absolute path
				this.path = this.file.getAbsolutePath();
			}
		}
		return this.path;
	}
	public int getMode() {
		return BINARY;
	}
	@Override
	public boolean hasModule() {
		return true;
	}
	public IModule getModule(char[] moduleName) {
		Map<String, IModule> modules = ModulesCache.get(this.file.getPath());
		if (modules != null) {
			return modules.get(String.valueOf(moduleName));
		}
		return null;
	}
	@Override
	public boolean servesModule(char[] moduleName) {
		return getModule(moduleName) != null;
	}
}
