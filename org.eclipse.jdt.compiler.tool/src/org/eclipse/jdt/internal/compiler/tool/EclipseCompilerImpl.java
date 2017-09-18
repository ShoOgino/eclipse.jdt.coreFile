/*******************************************************************************
 * Copyright (c) 2007, 2017 IBM Corporation and others.
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
 *    IBM Corporation - initial API and implementation
 *    IBM Corporation - fix for 342936
 *    Kenneth Olson - Contribution for bug 188796 - [jsr199] Using JSR199 to extend ECJ
 *    Dennis Hendriks - Contribution for bug 188796 - [jsr199] Using JSR199 to extend ECJ
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.tool;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.processing.Processor;
import javax.lang.model.SourceVersion;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.CompilationProgress;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.batch.ClasspathJrt;
import org.eclipse.jdt.internal.compiler.batch.ClasspathJsr199;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;
import org.eclipse.jdt.internal.compiler.batch.Main;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilationUnit;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;
import org.eclipse.jdt.internal.compiler.util.Messages;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.compiler.util.Util;

public class EclipseCompilerImpl extends Main {
	private static final CompilationUnit[] NO_UNITS = new CompilationUnit[0];
	private HashMap<CompilationUnit, JavaFileObject> javaFileObjectMap;
	Iterable<? extends JavaFileObject> compilationUnits;
	public JavaFileManager fileManager;
	protected Processor[] processors;
	// TODO: This is not yet used anywhere
	protected String[] modules;
	public DiagnosticListener<? super JavaFileObject> diagnosticListener;

	public EclipseCompilerImpl(PrintWriter out, PrintWriter err, boolean systemExitWhenFinished) {
		super(out, err, systemExitWhenFinished, null/*options*/, null/*progress*/);
	}

	public boolean call() {
		try {
			handleLocations();
			if (this.proceed) {
				this.globalProblemsCount = 0;
				this.globalErrorsCount = 0;
				this.globalWarningsCount = 0;
				this.globalTasksCount = 0;
				this.exportedClassFilesCounter = 0;
				// request compilation
				performCompilation();
			}
		} catch(IllegalArgumentException e) {
			this.logger.logException(e);
			if (this.systemExitWhenFinished) {
				cleanup();
				System.exit(-1);
			}
			return false;
		} catch (RuntimeException e) { // internal compiler failure
			e.printStackTrace();
			this.logger.logException(e);
			return false;
		} finally {
			cleanup();
		}
		if (this.globalErrorsCount == 0)
			return true;
		return false;
	}

	private void cleanup() {
		this.logger.flush();
		this.logger.close();
		this.processors = null;
		try {
			if (this.fileManager != null) {
				this.fileManager.flush();
			}
		} catch (IOException e) {
			// ignore
		}
	}

	@Override
	public CompilationUnit[] getCompilationUnits() {
		// This method is largely a copy of Main#getCompilationUnits()
		if (this.compilationUnits == null) return EclipseCompilerImpl.NO_UNITS;
		Map<String,CompilationUnit> pathToModCU = new HashMap<>();
		HashtableOfObject knownFileNames = new HashtableOfObject();
		ArrayList<CompilationUnit> units = new ArrayList<>();
		for (int round = 0; round < 2; round++) {
			int i = 0;
			for (final JavaFileObject javaFileObject : this.compilationUnits) {
				String name = javaFileObject.getName();
				char[] charName = name.toCharArray();
				boolean isModuleInfo = CharOperation.endsWith(charName, TypeConstants.MODULE_INFO_FILE_NAME);
				if (isModuleInfo == (round==0)) { // 1st round: modules, 2nd round others (to ensure populating pathToModCU well in time)
					if (knownFileNames.get(charName) != null)
						throw new IllegalArgumentException(this.bind("unit.more", name)); //$NON-NLS-1$
					knownFileNames.put(charName, charName);
					File file = new File(name);
					if (!file.exists())
						throw new IllegalArgumentException(this.bind("unit.missing", name)); //$NON-NLS-1$
					CompilationUnit cu = new CompilationUnit(null,
							name,
							null, 
							this.destinationPaths[i],
							shouldIgnoreOptionalProblems(this.ignoreOptionalProblemsFromFolders, name.toCharArray()), this.modNames[i]) {

							@Override
							public char[] getContents() {
								try {
									return javaFileObject.getCharContent(true).toString().toCharArray();
								} catch(IOException e) {
									e.printStackTrace();
									throw new AbortCompilationUnit(null, e, null);
								}
							}
						};
						units.add(cu);
						this.javaFileObjectMap.put(cu, javaFileObject);
						if (isModuleInfo) {
							int lastSlash = CharOperation.lastIndexOf(File.separatorChar, cu.fileName);
							if (lastSlash != -1) {
								pathToModCU.put(String.valueOf(CharOperation.subarray(cu.fileName, 0, lastSlash)), cu);
							}
						} else {
							for (Entry<String, CompilationUnit> entry : pathToModCU.entrySet()) {
								Path modPath = Paths.get(entry.getKey());
								Path cuPath = Paths.get(name);
								while (cuPath != null && cuPath.startsWith(modPath)) {
									cu.setModule(entry.getValue());
									break;
								}
							}
						}
				}
				i++;
			}
		}
		CompilationUnit[] result = new CompilationUnit[units.size()];
		units.toArray(result);
		return result;
	}
	/*
	 *  Low-level API performing the actual compilation
	 */
	@Override
	public IErrorHandlingPolicy getHandlingPolicy() {
		// passes the initial set of files to the batch oracle (to avoid finding more than once the same units when case insensitive match)
		return new IErrorHandlingPolicy() {
			@Override
			public boolean proceedOnErrors() {
				return false; // stop if there are some errors
			}
			@Override
			public boolean stopOnFirstError() {
				return false;
			}
			@Override
			public boolean ignoreAllErrors() {
				return false;
			}
		};
	}

	@Override
	public IProblemFactory getProblemFactory() {
		return new DefaultProblemFactory() {
			@Override
			public CategorizedProblem createProblem(
					final char[] originatingFileName,
					final int problemId,
					final String[] problemArguments,
					final String[] messageArguments,
					final int severity,
					final int startPosition,
					final int endPosition,
					final int lineNumber,
					final int columnNumber) {

				DiagnosticListener<? super JavaFileObject> diagListener = EclipseCompilerImpl.this.diagnosticListener;
				if (diagListener != null) {
					diagListener.report(new Diagnostic<JavaFileObject>() {
						@Override
						public String getCode() {
							return Integer.toString(problemId);
						}
						@Override
						public long getColumnNumber() {
							return columnNumber;
						}
						@Override
						public long getEndPosition() {
							return endPosition;
						}
						@Override
						public Kind getKind() {
							if ((severity & ProblemSeverities.Error) != 0) {
								return Diagnostic.Kind.ERROR;
							}
							if ((severity & ProblemSeverities.Optional) != 0) {
								return Diagnostic.Kind.WARNING;
							}
							if ((severity & ProblemSeverities.Warning) != 0) {
								return Diagnostic.Kind.MANDATORY_WARNING;
							}
							return Diagnostic.Kind.OTHER;
						}
						@Override
						public long getLineNumber() {
							return lineNumber;
						}
						@Override
						public String getMessage(Locale locale) {
							if (locale != null) {
								setLocale(locale);
							}
							return getLocalizedMessage(problemId, problemArguments);
						}
						@Override
						public long getPosition() {
							return startPosition;
						}
						@Override
						public JavaFileObject getSource() {
							File f = new File(new String(originatingFileName));
							if (f.exists()) {
								return new EclipseFileObject(null, f.toURI(), JavaFileObject.Kind.SOURCE, null);
							}
							return null;
						}
						@Override
						public long getStartPosition() {
							return startPosition;
						}
					});
				}
				return super.createProblem(originatingFileName, problemId, problemArguments, messageArguments, severity, startPosition, endPosition, lineNumber, columnNumber);
			}
			@Override
			public CategorizedProblem createProblem(
					final char[] originatingFileName,
					final int problemId,
					final String[] problemArguments,
					final int elaborationID,
					final String[] messageArguments,
					final int severity,
					final int startPosition,
					final int endPosition,
					final int lineNumber,
					final int columnNumber) {

				DiagnosticListener<? super JavaFileObject> diagListener = EclipseCompilerImpl.this.diagnosticListener;
				if (diagListener != null) {
					diagListener.report(new Diagnostic<JavaFileObject>() {
						@Override
						public String getCode() {
							return Integer.toString(problemId);
						}
						@Override
						public long getColumnNumber() {
							return columnNumber;
						}
						@Override
						public long getEndPosition() {
							return endPosition;
						}
						@Override
						public Kind getKind() {
							if ((severity & ProblemSeverities.Error) != 0) {
								return Diagnostic.Kind.ERROR;
							}
							if ((severity & ProblemSeverities.Info) != 0) {
								return Diagnostic.Kind.NOTE;
							}
							if ((severity & ProblemSeverities.Optional) != 0) {
								return Diagnostic.Kind.WARNING;
							}
							if ((severity & ProblemSeverities.Warning) != 0) {
								return Diagnostic.Kind.MANDATORY_WARNING;
							}
							return Diagnostic.Kind.OTHER;
						}
						@Override
						public long getLineNumber() {
							return lineNumber;
						}
						@Override
						public String getMessage(Locale locale) {
							if (locale != null) {
								setLocale(locale);
							}
							return getLocalizedMessage(problemId, problemArguments);
						}
						@Override
						public long getPosition() {
							return startPosition;
						}
						@Override
						public JavaFileObject getSource() {
							File f = new File(new String(originatingFileName));
							if (f.exists()) {
								return new EclipseFileObject(null, f.toURI(), JavaFileObject.Kind.SOURCE, null);
							}
							return null;
						}
						@Override
						public long getStartPosition() {
							return startPosition;
						}
					});
				}
				return super.createProblem(originatingFileName, problemId, problemArguments, elaborationID, messageArguments, severity, startPosition, endPosition, lineNumber, columnNumber);
			}
		};
	}

	@Override
	protected void initialize(PrintWriter outWriter, PrintWriter errWriter, boolean systemExit, Map<String, String> customDefaultOptions, CompilationProgress compilationProgress) {
		super.initialize(outWriter, errWriter, systemExit, customDefaultOptions, compilationProgress);
		this.javaFileObjectMap = new HashMap<>();
	}

	@Override
	protected void initializeAnnotationProcessorManager() {
		super.initializeAnnotationProcessorManager();
		if (this.batchCompiler.annotationProcessorManager != null && this.processors != null) {
			this.batchCompiler.annotationProcessorManager.setProcessors(this.processors);
		} else if (this.processors != null) {
			throw new UnsupportedOperationException("Cannot handle annotation processing"); //$NON-NLS-1$
		}
	}

	// Dump classfiles onto disk for all compilation units that where successful
	// and do not carry a -d none spec, either directly or inherited from Main.
	@Override
	public void outputClassFiles(CompilationResult unitResult) {
		if (!((unitResult == null) || (unitResult.hasErrors() && !this.proceedOnError))) {
			ClassFile[] classFiles = unitResult.getClassFiles();
			boolean generateClasspathStructure = this.fileManager.hasLocation(StandardLocation.CLASS_OUTPUT);
			File outputLocation = null;
			String currentDestinationPath = unitResult.getCompilationUnit().getDestinationPath();
			if (currentDestinationPath == null)
				currentDestinationPath = this.destinationPath;
			if (currentDestinationPath != null) {
				outputLocation = new File(currentDestinationPath);
				outputLocation.mkdirs();
			}
			for (int i = 0, fileCount = classFiles.length; i < fileCount; i++) {
				// retrieve the key and the corresponding classfile
				ClassFile classFile = classFiles[i];
				char[] filename = classFile.fileName();
				int length = filename.length;
				char[] relativeName = new char[length + 6];
				System.arraycopy(filename, 0, relativeName, 0, length);
				System.arraycopy(SuffixConstants.SUFFIX_class, 0, relativeName, length, 6);
				CharOperation.replace(relativeName, '/', File.separatorChar);
				String relativeStringName = new String(relativeName);
				if (this.compilerOptions.verbose) {
					EclipseCompilerImpl.this.out.println(
						Messages.bind(
							Messages.compilation_write,
							new String[] {
								String.valueOf(this.exportedClassFilesCounter+1),
								relativeStringName
							}));
				}
				try {
					char[] modName = unitResult.compilationUnit.getModuleName();
					Location location = null;
					if (modName == null) {
						location = StandardLocation.CLASS_OUTPUT;
					} else {
						// TODO: Still possible to end up with a non-null module name without JDK 9 in build path
						System.out.println("module name:" + new String(modName)); //$NON-NLS-1$
						System.out.println("CU:" + new String(unitResult.compilationUnit.getFileName())); //$NON-NLS-1$
						location = this.fileManager.getLocationForModule(StandardLocation.CLASS_OUTPUT, new String(modName));
						System.out.println("Location from getLocationForModule(): " + location); //$NON-NLS-1$
					}
					JavaFileObject javaFileForOutput =
						this.fileManager.getJavaFileForOutput(
								location,
								new String(filename),
								JavaFileObject.Kind.CLASS,
								this.javaFileObjectMap.get(unitResult.compilationUnit));

					if (generateClasspathStructure) {
						if (currentDestinationPath != null) {
							int index = CharOperation.lastIndexOf(File.separatorChar, relativeName);
							if (index != -1) {
								File currentFolder = new File(currentDestinationPath, relativeStringName.substring(0, index));
								currentFolder.mkdirs();
							}
						} else {
							// create the subfolfers is necessary
							// need a way to retrieve the folders to create
							String path = javaFileForOutput.toUri().getPath();
							int index = path.lastIndexOf('/');
							if (index != -1) {
								File file = new File(path.substring(0, index));
								file.mkdirs();
							}
						}
					}

					try (OutputStream openOutputStream = javaFileForOutput.openOutputStream(); BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(openOutputStream)) {
						bufferedOutputStream.write(classFile.header, 0, classFile.headerOffset);
						bufferedOutputStream.write(classFile.contents, 0, classFile.contentsOffset);
						bufferedOutputStream.flush();
					}
				} catch (IOException e) {
					this.logger.logNoClassFileCreated(currentDestinationPath, relativeStringName, e);
				}
				this.logger.logClassFile(
					generateClasspathStructure,
					currentDestinationPath,
					relativeStringName);
				this.exportedClassFilesCounter++;
			}
			this.batchCompiler.lookupEnvironment.releaseClassFiles(classFiles);
		}
	}

	@Override
	protected void setPaths(ArrayList<String> bootclasspaths,
			String sourcepathClasspathArg,
			ArrayList<String> sourcepathClasspaths,
			ArrayList<String> classpaths,
			String modulePath,
			String moduleSourcepath,
			ArrayList<String> extdirsClasspaths,
			ArrayList<String> endorsedDirClasspaths,
			String customEncoding) {
		// Sometimes this gets called too early there by losing locations set after that point.
		// The code is now moved to handleLocations() which is invoked just before compilation
	}

	protected void handleLocations() {
		ArrayList<FileSystem.Classpath> fileSystemClasspaths = new ArrayList<>();
		EclipseFileManager eclipseJavaFileManager = null;
		StandardJavaFileManager standardJavaFileManager = null;
		JavaFileManager javaFileManager = null;
		boolean havePlatformPaths = false;
		boolean haveClassPaths = false;
		if (this.fileManager instanceof EclipseFileManager) {
			eclipseJavaFileManager = (EclipseFileManager) this.fileManager;
		}
		if (this.fileManager instanceof StandardJavaFileManager) {
			standardJavaFileManager = (StandardJavaFileManager) this.fileManager;
		}
		javaFileManager = this.fileManager;

		if (eclipseJavaFileManager != null) {
			if ((eclipseJavaFileManager.flags & EclipseFileManager.HAS_ENDORSED_DIRS) == 0
					&& (eclipseJavaFileManager.flags & EclipseFileManager.HAS_BOOTCLASSPATH) != 0) {
				fileSystemClasspaths.addAll(this.handleEndorseddirs(null));
			}
		}
		Iterable<? extends File> locationFiles = null;
		if (standardJavaFileManager != null) {
			locationFiles = standardJavaFileManager.getLocation(StandardLocation.PLATFORM_CLASS_PATH);
			if (locationFiles != null) {
				for (File file : locationFiles) {
					if (file.isDirectory()) {
						List<Classpath> platformLocations = getPlatformLocations(fileSystemClasspaths, file);
						if (standardJavaFileManager instanceof EclipseFileManager) {
							if (platformLocations.size() == 1) {
								Classpath jrt = platformLocations.get(0);
								if (jrt instanceof ClasspathJrt) {
									// TODO: double check, should it be platform or system module?
									try {
										((EclipseFileManager) standardJavaFileManager).locationHandler.newSystemLocation(StandardLocation.SYSTEM_MODULES, (ClasspathJrt) jrt);
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							}
						}
						fileSystemClasspaths.addAll(platformLocations);
						break; // Only possible scenario is, we have one and only entry representing the Java home.
					} else {
						Classpath classpath = FileSystem.getClasspath(
								file.getAbsolutePath(),
								null,
								null, this.options);
							if (classpath != null) {
								fileSystemClasspaths.add(classpath);
								havePlatformPaths = true;
							}
					}
				}
			}
		} else if (javaFileManager != null) {
			File javaHome = Util.getJavaHome();
			if (Util.getJDKLevel(javaHome) >= ClassFileConstants.JDK9) { 
				ClasspathJrt jrt = (ClasspathJrt) FileSystem.getJrtClasspath(javaHome.toString(), null, null, null);
				Classpath classpath = new ClasspathJsr199(jrt, this.fileManager, StandardLocation.PLATFORM_CLASS_PATH);
				fileSystemClasspaths.add(classpath);
			} else {
				Classpath classpath = new ClasspathJsr199(this.fileManager, StandardLocation.PLATFORM_CLASS_PATH);
				fileSystemClasspaths.add(classpath);
			}
			havePlatformPaths = true;
		}
		if (eclipseJavaFileManager != null) {
			if ((eclipseJavaFileManager.flags & EclipseFileManager.HAS_EXT_DIRS) == 0
					&& (eclipseJavaFileManager.flags & EclipseFileManager.HAS_BOOTCLASSPATH) != 0) {
				fileSystemClasspaths.addAll(this.handleExtdirs(null));
			}
		}
		if (standardJavaFileManager != null) {
			locationFiles = standardJavaFileManager.getLocation(StandardLocation.SOURCE_PATH);
			if (locationFiles != null) {
				for (File file : locationFiles) {
					Classpath classpath = FileSystem.getClasspath(
							file.getAbsolutePath(),
							null,
							null, this.options);
					if (classpath != null) {
						fileSystemClasspaths.add(classpath);
					}
				}
			}
			locationFiles = standardJavaFileManager.getLocation(StandardLocation.CLASS_PATH);
			if (locationFiles != null) {
				for (File file : locationFiles) {
					Classpath classpath = FileSystem.getClasspath(
						file.getAbsolutePath(),
						null,
						null, this.options);
					if (classpath != null) {
						fileSystemClasspaths.add(classpath);
						haveClassPaths = true;
					}
				}
			}
			if (SourceVersion.latest().compareTo(SourceVersion.RELEASE_8) > 0) {
				try {
					Iterable<? extends Path> locationAsPaths = standardJavaFileManager.getLocationAsPaths(StandardLocation.MODULE_SOURCE_PATH);
					if (locationAsPaths != null) {
						for (Path path : locationAsPaths) {
							ArrayList<Classpath> modulepaths = handleModuleSourcepath(path.toFile().getCanonicalPath());
							for (Classpath classpath : modulepaths) {
								Collection<String> moduleNames = classpath.getModuleNames(null);
								for (String modName : moduleNames) {
									Path p = Paths.get(classpath.getPath());
									standardJavaFileManager.setLocationForModule(StandardLocation.MODULE_SOURCE_PATH, modName, 
											Collections.singletonList(p));
									p = Paths.get(classpath.getDestinationPath());
									standardJavaFileManager.setLocationForModule(StandardLocation.CLASS_OUTPUT, modName, 
											Collections.singletonList(p));
								}
							}
							fileSystemClasspaths.addAll(modulepaths);
						}
					}
				} catch (Exception e) {
					// TODO: Revisit when JRE 9 no longer throws IllegalStateException for getLocation.
				}
				try {
					locationFiles = standardJavaFileManager.getLocation(StandardLocation.MODULE_PATH);
					if (locationFiles != null) {
						for (File file : locationFiles) {
							try {
								ArrayList<Classpath> modulepaths = handleModulepath(file.getCanonicalPath());
								for (Classpath classpath : modulepaths) {
									Collection<String> moduleNames = classpath.getModuleNames(null);
									for (String string : moduleNames) {
										Path path = Paths.get(classpath.getPath());
										standardJavaFileManager.setLocationForModule(StandardLocation.MODULE_PATH, string, 
												Collections.singletonList(path));
									}
								}
								fileSystemClasspaths.addAll(modulepaths);
							} catch (IOException e) {
								throw new AbortCompilationUnit(null, e, null);
							}
						}
					}
				} catch (Exception e) {
					// TODO: Revisit when JRE 9 no longer throws IllegalStateException for getLocation.
				}
			}
		} else if (javaFileManager != null) {
			Classpath classpath = null;
			if (this.fileManager.hasLocation(StandardLocation.SOURCE_PATH)) {
				classpath = new ClasspathJsr199(this.fileManager, StandardLocation.SOURCE_PATH);
				fileSystemClasspaths.add(classpath);
			}
			if (SourceVersion.latest().compareTo(SourceVersion.RELEASE_8) > 0) {
				// Add the locations to search for in specific order
				if (this.fileManager.hasLocation(StandardLocation.UPGRADE_MODULE_PATH)) {
					classpath = new ClasspathJsr199(this.fileManager, StandardLocation.UPGRADE_MODULE_PATH);
				}
				if (this.fileManager.hasLocation(StandardLocation.SYSTEM_MODULES)) {
					classpath = new ClasspathJsr199(this.fileManager, StandardLocation.SYSTEM_MODULES);
					fileSystemClasspaths.add(classpath);
				}				
				if (this.fileManager.hasLocation(StandardLocation.PATCH_MODULE_PATH)) {
					classpath = new ClasspathJsr199(this.fileManager, StandardLocation.PATCH_MODULE_PATH);
					fileSystemClasspaths.add(classpath);
				}
				if (this.fileManager.hasLocation(StandardLocation.MODULE_SOURCE_PATH)) {
					classpath = new ClasspathJsr199(this.fileManager, StandardLocation.MODULE_SOURCE_PATH);
					fileSystemClasspaths.add(classpath);
				}
				if (this.fileManager.hasLocation(StandardLocation.MODULE_PATH)) {
					classpath = new ClasspathJsr199(this.fileManager, StandardLocation.MODULE_PATH);
					fileSystemClasspaths.add(classpath);
				}
			}
			classpath = new ClasspathJsr199(this.fileManager, StandardLocation.CLASS_PATH);
			fileSystemClasspaths.add(classpath);
			haveClassPaths = true;
		}
		if (this.checkedClasspaths == null) {
			// It appears to be necessary to handleBootclasspath() for IBM JVMs
			// in order to have visibility to java.lang.String (not present in rt.jar).
			// The jars returned by StandardFileManager.getLocation(PLATFORM_CLASS_PATH) are
			// not sufficient to resolve all standard classes.
			if (!havePlatformPaths) fileSystemClasspaths.addAll(this.handleBootclasspath(null, null));
			if (!haveClassPaths) fileSystemClasspaths.addAll(this.handleClasspath(null, null));
		}
		fileSystemClasspaths = FileSystem.ClasspathNormalizer.normalize(fileSystemClasspaths);
		final int size = fileSystemClasspaths.size();
		if (size != 0) {
			this.checkedClasspaths = new FileSystem.Classpath[size];
			int i = 0;
			for (FileSystem.Classpath classpath : fileSystemClasspaths) {
				this.checkedClasspaths[i++] = classpath;
			}
		}
	}

	protected List<Classpath> getPlatformLocations(ArrayList<FileSystem.Classpath> fileSystemClasspaths, File file) {
		List<Classpath> platformLibraries = Util.collectPlatformLibraries(file);
		return platformLibraries;
	}
	@Override
	protected void loggingExtraProblems() {
		super.loggingExtraProblems();
		for (@SuppressWarnings("rawtypes")
			Iterator iterator = this.extraProblems.iterator(); iterator.hasNext(); ) {
			final CategorizedProblem problem = (CategorizedProblem) iterator.next();
			if (this.diagnosticListener != null) {
				this.diagnosticListener.report(new Diagnostic<JavaFileObject>() {
					@Override
					public String getCode() {
						return null;
					}
					@Override
					public long getColumnNumber() {
						if (problem instanceof DefaultProblem) {
							return ((DefaultProblem) problem).column;
						}
						return Diagnostic.NOPOS;
					}
					@Override
					public long getEndPosition() {
						if (problem instanceof DefaultProblem) {
							return ((DefaultProblem) problem).getSourceEnd();
						}
						return Diagnostic.NOPOS;
					}
					@Override
					public Kind getKind() {
						if (problem.isError()) {
							return Diagnostic.Kind.ERROR;
						}
						if (problem.isWarning()) {
							return Diagnostic.Kind.WARNING;
						} else if (problem instanceof DefaultProblem && ((DefaultProblem) problem).isInfo()) {
							return Diagnostic.Kind.NOTE;
						}
						return Diagnostic.Kind.OTHER;
					}
					@Override
					public long getLineNumber() {
						if (problem instanceof DefaultProblem) {
							return ((DefaultProblem) problem).getSourceLineNumber();
						}
						return Diagnostic.NOPOS;
					}
					@Override
					public String getMessage(Locale locale) {
						return problem.getMessage();
					}
					@Override
					public long getPosition() {
						if (problem instanceof DefaultProblem) {
							return ((DefaultProblem) problem).getSourceStart();
						}
						return Diagnostic.NOPOS;
					}
					@Override
					public JavaFileObject getSource() {
						if (problem instanceof DefaultProblem) {
							File f = new File(new String(((DefaultProblem) problem).getOriginatingFileName()));
							if (f.exists()) {
								Charset charset = (EclipseCompilerImpl.this.fileManager instanceof EclipseFileManager) ?
														((EclipseFileManager) EclipseCompilerImpl.this.fileManager).charset : Charset.defaultCharset();
								return new EclipseFileObject(null, f.toURI(), JavaFileObject.Kind.SOURCE, charset);
							}
							return null;
						}
						return null;
					}
					@Override
					public long getStartPosition() {
						return getPosition();
					}
				});
			}
		}
	}
}
