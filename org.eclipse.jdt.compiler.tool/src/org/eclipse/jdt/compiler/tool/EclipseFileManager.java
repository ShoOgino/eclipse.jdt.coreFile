/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.compiler.tool;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.zip.ZipException;

import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.JavaFileObject.Kind;

import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.batch.Main;

/**
 * Implementation of the Standard Java File Manager
 */
public class EclipseFileManager implements StandardJavaFileManager {
	private static final String NO_EXTENSION = "";
	static final int HAS_EXT_DIRS = 1;
	static final int HAS_BOOTCLASSPATH = 2;
	static final int HAS_ENDORSED_DIRS = 4;

	Map<File, Archive> archivesCache;
	Charset charset;
	Locale locale;
	Map<String, Iterable<? extends File>> locations;
	Main compiler;
	int flags;
	
	public EclipseFileManager(Main eclipseCompiler, Locale locale, Charset charset) {
		this.compiler = eclipseCompiler;
		this.locale = locale == null ? Locale.getDefault() : locale;
		this.charset = charset == null ? Charset.defaultCharset() : charset;
		this.locations = new HashMap<String, Iterable<? extends File>>();
		this.archivesCache = new HashMap<File, Archive>();
		if (locale != null) {
			this.compiler.setLocale(locale);
		}
		try {
			this.setLocation(StandardLocation.PLATFORM_CLASS_PATH, getDefaultBootclasspath());
			this.setLocation(StandardLocation.CLASS_PATH, getDefaultClasspath());
		} catch (IOException e) {
			// ignore
		}
	}

	private void addFiles(File[][] jars, ArrayList<File> files) {
		if (jars != null) {
			for (File[] currentJars : jars) {
				if (currentJars != null) {
					for (File currentJar : currentJars) {
						if (currentJar.exists()) {
							files.add(currentJar);
						}
					}
				}
			}
		}
	}
	
	
	private void addFilesFrom(File javaHome, String propertyName, String defaultPath, ArrayList<File> files) {
		String extdirsStr = System.getProperty(propertyName); //$NON-NLS-1$
		File[] directoriesToCheck = null;
		if (extdirsStr == null) {
			if (javaHome != null) {
				directoriesToCheck = new File[] { new File(javaHome, defaultPath) }; //$NON-NLS-1$
			}
		} else {
			StringTokenizer tokenizer = new StringTokenizer(extdirsStr, File.pathSeparator);
			ArrayList<String> paths = new ArrayList<String>();
			while (tokenizer.hasMoreTokens()) {
				paths.add(tokenizer.nextToken());
			}
			if (paths.size() != 0) {
				directoriesToCheck = new File[paths.size()];
				for (int i = 0; i < directoriesToCheck.length; i++)  {
					directoriesToCheck[i] = new File(paths.get(i));
				}
			}
		}
		if (directoriesToCheck != null) {
			addFiles(Main.getLibrariesFiles(directoriesToCheck), files);
		}
		
	}
	
	/* (non-Javadoc)
	 * @see javax.tools.JavaFileManager#close()
	 */
	public void close() throws IOException {
		this.locations = null;
		for (Archive archive : archivesCache.values()) {
			if (archive.file == null) {
				System.out.println(archive == Archive.UNKNOWN_ARCHIVE);
			}
			archive.close();
		}
	}
	
	private void collectAllMatchingFiles(File file, String normalizedPackageName, Set<Kind> kinds, boolean recurse, ArrayList<JavaFileObject> collector) {
		if (!isArchive(file)) {
			// we must have a directory
			File currentFile = new File(file, normalizedPackageName);
			if (!currentFile.exists()) return;
			String path;
			try {
				path = currentFile.getCanonicalPath();
			} catch (IOException e) {
				return;
			}
			if (File.separatorChar == '/') {
				if (!path.endsWith(normalizedPackageName)) return;
			} else if (!path.endsWith(normalizedPackageName.replace('/', File.separatorChar))) return;
   			File[] files = currentFile.listFiles();
   			if (files != null) {
   				// this was a directory
   				for (File f : files) {
   					if (f.isDirectory() && recurse) {
   						collectAllMatchingFiles(file, normalizedPackageName + '/' + f.getName(), kinds, recurse, collector);
   					} else {
   						final Kind kind = getKind(f);
   						if (kinds.contains(kind)) {
   							collector.add(new EclipseFileObject(normalizedPackageName + currentFile.getName(), currentFile.toURI(), kind, this.charset));
   						}
   					}
   				}
   			}
			// currentFile is not a directory
			// check if it matches the criteria
			final Kind kind = getKind(file);
			if (kinds.contains(kind)) {
				collector.add(new EclipseFileObject(normalizedPackageName + currentFile.getName(), currentFile.toURI(), kind, this.charset));
			}
		} else {
			Archive archive = this.getArchive(file);
			String key = normalizedPackageName;
			if (!normalizedPackageName.endsWith("/")) {
				key += '/';
			}
			// we have an archive file
			if (recurse) {
				for (String packageName : archive.allPackages()) {
					if (packageName.startsWith(key)) {
						ArrayList<String> types = archive.getTypes(packageName);
						if (types != null) {
							for (String typeName : types) {
		   						final Kind kind = getKind(getExtension(typeName));
		   						if (kinds.contains(kind)) {
		   							collector.add(archive.getArchiveFileObject(packageName + typeName, this.charset));
		   						}
							}
						}
					}
				}
			} else {
				ArrayList<String> types = archive.getTypes(key);
				if (types != null) {
					for (String typeName : types) {
   						final Kind kind = getKind(typeName);
   						if (kinds.contains(kind)) {
   							collector.add(archive.getArchiveFileObject(normalizedPackageName + typeName, this.charset));
   						}
					}
				}
			}
		}
	}

	private Iterable<? extends File> concatFiles(Iterable<? extends File> iterable, Iterable<? extends File> iterable2) {
		ArrayList<File> list = new ArrayList<File>();
		for (Iterator<? extends File> iterator = iterable.iterator(); iterator.hasNext(); ) {
			list.add(iterator.next());
		}
		for (Iterator<? extends File> iterator = iterable2.iterator(); iterator.hasNext(); ) {
			list.add(iterator.next());
		}
		return list;
	}

	/* (non-Javadoc)
	 * @see javax.tools.JavaFileManager#flush()
	 */
	public void flush() throws IOException {
		this.locations = null;
		for (Archive archive : archivesCache.values()) {
			archive.flush();
		}
	}

	private Archive getArchive(File f) {
		// check the archive (jar/zip) cache
		Archive archive = this.archivesCache.get(f);
		if (archive == null) {
			// create a new archive
			if (f.exists()) {
    			try {
    				archive = new Archive(f);
    			} catch (ZipException e) {
    				// ignore
    			} catch (IOException e) {
    				// ignore
    			}
    			if (archive != null) {
    				this.archivesCache.put(f, archive);
    			} else {
    				this.archivesCache.put(f, Archive.UNKNOWN_ARCHIVE);
    			}
			} else {
				this.archivesCache.put(f, Archive.UNKNOWN_ARCHIVE);
			}
		}
		return archive;
	}

	private Iterable<? extends File> getBootclasspathFrom(String path) {
		ArrayList<FileSystem.Classpath> paths = new ArrayList<FileSystem.Classpath>();
		ArrayList<File> files = new ArrayList<File>();
		try {
			this.compiler.processPathEntries(Main.DEFAULT_SIZE_CLASSPATH, paths, path, this.charset.toString(), false, false);
		} catch (InvalidInputException e) {
			return null;
		}
		for (FileSystem.Classpath classpath : paths) {
			files.add(new File(classpath.getPath()));
		}
		return files;
	}

	/* (non-Javadoc)
	 * @see javax.tools.JavaFileManager#getClassLoader(javax.tools.JavaFileManager.Location)
	 */
	public ClassLoader getClassLoader(Location location) {
		Iterable<? extends File> iterable = getLocation(location);
		if (iterable == null) {
			// location is unknown
			return null;
		}
		ArrayList<URL> allURLs = new ArrayList<URL>();
		for (File f : iterable) {
			try {
				allURLs.add(f.toURI().toURL());
			} catch (MalformedURLException e) {
				// the url is malformed - this should not happen
				throw new RuntimeException(e);
			}
		}
		URL[] result = new URL[allURLs.size()];
		return new URLClassLoader(allURLs.toArray(result), getClass().getClassLoader());
	}

	private Iterable<? extends File> getClasspathFrom(String path) {
		ArrayList<FileSystem.Classpath> paths = new ArrayList<FileSystem.Classpath>();
		ArrayList<File> files = new ArrayList<File>();
		try {
			this.compiler.processPathEntries(Main.DEFAULT_SIZE_CLASSPATH, paths, path, this.charset.toString(), false, false);
		} catch (InvalidInputException e) {
			return null;
		}
		for (FileSystem.Classpath classpath : paths) {
			files.add(new File(classpath.getPath()));
		}
		return files;
	}

	Iterable<? extends File> getDefaultBootclasspath() {
		ArrayList<File> files = new ArrayList<File>();
		String javaversion = System.getProperty("java.version");//$NON-NLS-1$
		if (javaversion != null && !javaversion.startsWith("1.6")) { //$NON-NLS-1$	
			// wrong jdk - 1.6 is required
			return null;
		}

		/*
		 * Handle >= JDK 1.6
		 */
		File javaHome = this.compiler.getJavaHome();
		addFilesFrom(javaHome, "java.endorsed.dirs", "/lib/endorsed", files);
		if (javaHome != null) {
			File[] directoriesToCheck = null;
			if (System.getProperty("os.name").startsWith("Mac")) {//$NON-NLS-1$//$NON-NLS-2$
				directoriesToCheck = new File[] { new File(javaHome, "../Classes"), //$NON-NLS-1$
				};
			} else {
				directoriesToCheck = new File[] { new File(javaHome, "lib") //$NON-NLS-1$
				};
			}
			File[][] jars = Main.getLibrariesFiles(directoriesToCheck);
			addFiles(jars, files);
		}
		addFilesFrom(javaHome, "java.ext.dirs", "/lib/ext", files);
		return files;
	}

	Iterable<? extends File> getDefaultClasspath() {
		// default classpath
		ArrayList<File> files = new ArrayList<File>();
		String classProp = System.getProperty("java.class.path"); //$NON-NLS-1$
		if ((classProp == null) || (classProp.length() == 0)) {
			return null;
		} else {
			StringTokenizer tokenizer = new StringTokenizer(classProp, File.pathSeparator);
			String token;
			while (tokenizer.hasMoreTokens()) {
				token = tokenizer.nextToken();
				File file = new File(token);
				if (file.exists()) {
					files.add(file);
				}
			}
		}
		return files;
	}

	private Iterable<? extends File> getEndorsedDirsFrom(String path) {
		ArrayList<FileSystem.Classpath> paths = new ArrayList<FileSystem.Classpath>();
		ArrayList<File> files = new ArrayList<File>();
		try {
			this.compiler.processPathEntries(Main.DEFAULT_SIZE_CLASSPATH, paths, path, this.charset.toString(), false, false);
		} catch (InvalidInputException e) {
			return null;
		}
		for (FileSystem.Classpath classpath : paths) {
			files.add(new File(classpath.getPath()));
		}
		return files;
	}

	private Iterable<? extends File> getExtdirsFrom(String path) {
		ArrayList<FileSystem.Classpath> paths = new ArrayList<FileSystem.Classpath>();
		ArrayList<File> files = new ArrayList<File>();
		try {
			this.compiler.processPathEntries(Main.DEFAULT_SIZE_CLASSPATH, paths, path, this.charset.toString(), false, false);
		} catch (InvalidInputException e) {
			return null;
		}
		for (FileSystem.Classpath classpath : paths) {
			files.add(new File(classpath.getPath()));
		}
		return files;
	}

	private String getExtension(File file) {
		String name = file.getName();
		return getExtension(name);
	}
	private String getExtension(String name) {
		int index = name.lastIndexOf('.');
		if (index == -1) {
			return NO_EXTENSION;
		}
		return name.substring(index);
	}

	/* (non-Javadoc)
	 * @see javax.tools.JavaFileManager#getFileForInput(javax.tools.JavaFileManager.Location, java.lang.String, java.lang.String)
	 */
	public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
		Iterable<? extends File> files = getLocation(location);
		if (files == null) {
			throw new IllegalArgumentException("Unknown location : " + location);
		}
		String normalizedFileName = normalized(packageName) + '/' + relativeName.replace('\\', '/');
		for (File file : files) {
			if (file.isDirectory()) {
				// handle directory
				File f = new File(file, normalizedFileName);
				if (f.exists()) {
					return new EclipseFileObject(packageName + File.separator + relativeName, f.toURI(), getKind(f), this.charset);
				} else {
					continue; // go to next entry in the location
				}
			} else if (isArchive(file)) {
				// handle archive file
				Archive archive = getArchive(file);
				if (archive != Archive.UNKNOWN_ARCHIVE) {
					if (archive.contains(normalizedFileName)) {
						return archive.getArchiveFileObject(normalizedFileName, this.charset);
					}
				}
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see javax.tools.JavaFileManager#getFileForOutput(javax.tools.JavaFileManager.Location, java.lang.String, java.lang.String, javax.tools.FileObject)
	 */
	public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling)
			throws IOException {
		Iterable<? extends File> files = getLocation(location);
		if (files == null) {
			throw new IllegalArgumentException("Unknown location : " + location);
		}
		final Iterator<? extends File> iterator = files.iterator();
		if (iterator.hasNext()) {
			File file = iterator.next();
			String normalizedFileName = normalized(packageName) + '/' + relativeName.replace('\\', '/');
			File f = new File(file, normalizedFileName);
			return new EclipseFileObject(packageName + File.separator + relativeName, f.toURI(), getKind(f), this.charset);
		} else {
			throw new IllegalArgumentException("location is empty : " + location);
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.tools.JavaFileManager#getJavaFileForInput(javax.tools.JavaFileManager.Location, java.lang.String, javax.tools.JavaFileObject.Kind)
	 */
	public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind) throws IOException {
		if (kind != Kind.CLASS && kind != Kind.SOURCE) {
			throw new IllegalArgumentException("Invalid kind : " + kind);
		}
		Iterable<? extends File> files = getLocation(location);
		if (files == null) {
			throw new IllegalArgumentException("Unknown location : " + location);
		}
		String normalizedFileName = normalized(className);
		normalizedFileName += kind.extension;
		for (File file : files) {
			if (file.isDirectory()) {
				// handle directory
				File f = new File(file, normalizedFileName);
				if (f.exists()) {
					return new EclipseFileObject(className, f.toURI(), kind, this.charset);
				} else {
					continue; // go to next entry in the location
				}
			} else if (isArchive(file)) {
				// handle archive file
				Archive archive = getArchive(file);
				if (archive != Archive.UNKNOWN_ARCHIVE) {
					if (archive.contains(normalizedFileName)) {
						return archive.getArchiveFileObject(normalizedFileName, this.charset);
					}
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.tools.JavaFileManager#getJavaFileForOutput(javax.tools.JavaFileManager.Location, java.lang.String, javax.tools.JavaFileObject.Kind, javax.tools.FileObject)
	 */
	public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling)
			throws IOException {
		if (kind != Kind.CLASS && kind != Kind.SOURCE) {
			throw new IllegalArgumentException("Invalid kind : " + kind);
		}
		Iterable<? extends File> files = getLocation(location);
		if (files == null) {
			throw new IllegalArgumentException("Unknown location : " + location);
		}
		final Iterator<? extends File> iterator = files.iterator();
		if (iterator.hasNext()) {
			File file = iterator.next();
			String normalizedFileName = normalized(className);
			normalizedFileName += kind.extension;
			File f = new File(file, normalizedFileName);
			return new EclipseFileObject(className, f.toURI(), kind, this.charset);
		} else {
			throw new IllegalArgumentException("location is empty : " + location);
		}
	}

	/* (non-Javadoc)
	 * @see javax.tools.StandardJavaFileManager#getJavaFileObjects(java.io.File[])
	 */
	public Iterable<? extends JavaFileObject> getJavaFileObjects(File... files) {
		return getJavaFileObjectsFromFiles(Arrays.asList(files));
	}

	/* (non-Javadoc)
	 * @see javax.tools.StandardJavaFileManager#getJavaFileObjects(java.lang.String[])
	 */
	public Iterable<? extends JavaFileObject> getJavaFileObjects(String... names) {
		return getJavaFileObjectsFromStrings(Arrays.asList(names));
	}

	/* (non-Javadoc)
	 * @see javax.tools.StandardJavaFileManager#getJavaFileObjectsFromFiles(java.lang.Iterable)
	 */
	public Iterable<? extends JavaFileObject> getJavaFileObjectsFromFiles(Iterable<? extends File> files) {
		ArrayList<JavaFileObject> javaFileArrayList = new ArrayList<JavaFileObject>();
		for (File f : files) {
			javaFileArrayList.add(new EclipseFileObject(f.getAbsolutePath(), f.toURI(), getKind(f), this.charset));
		}
		return javaFileArrayList;
	}

	/* (non-Javadoc)
	 * @see javax.tools.StandardJavaFileManager#getJavaFileObjectsFromStrings(java.lang.Iterable)
	 */
	public Iterable<? extends JavaFileObject> getJavaFileObjectsFromStrings(Iterable<String> names) {
		ArrayList<File> files = new ArrayList<File>();
		for (String name : names) {
			files.add(new File(name));
		}
		return getJavaFileObjectsFromFiles(files);
	}

	public Kind getKind(File f) {
		return getKind(getExtension(f));
	}

	private Kind getKind(String extension) {
		if (Kind.CLASS.extension.equals(extension)) {
			return Kind.CLASS;
		} else if (Kind.SOURCE.extension.equals(extension)) {
			return Kind.SOURCE;
		} else if (Kind.HTML.extension.equals(extension)) {
			return Kind.HTML;
		}
		return Kind.OTHER;
	}

	/* (non-Javadoc)
	 * @see javax.tools.StandardJavaFileManager#getLocation(javax.tools.JavaFileManager.Location)
	 */
	public Iterable<? extends File> getLocation(Location location) {
		return this.locations.get(location.getName());
	}

	private Iterable<? extends File> getOutputDir(String string) {
		if ("none".equals(string)) {
			return null;
		}
		File file = new File(string);
		if (file.exists() && !file.isDirectory()) {
			throw new IllegalArgumentException("file : " + file.getAbsolutePath() + " is not a directory");
		}
		ArrayList<File> list = new ArrayList<File>(1);
		list.add(file);
		return list;
	}

	private Iterable<? extends File> getSourcepathFrom(String path) {
		ArrayList<FileSystem.Classpath> paths = new ArrayList<FileSystem.Classpath>();
		ArrayList<File> files = new ArrayList<File>();
		try {
			this.compiler.processPathEntries(Main.DEFAULT_SIZE_CLASSPATH, paths, path, this.charset.toString(), false, false);
		} catch (InvalidInputException e) {
			return null;
		}
		for (FileSystem.Classpath classpath : paths) {
			files.add(new File(classpath.getPath()));
		}
		return files;
	}

	/* (non-Javadoc)
	 * @see javax.tools.JavaFileManager#handleOption(java.lang.String, java.util.Iterator)
	 */
	public boolean handleOption(String current, Iterator<String> remaining) {
		try {
			if ("-bootclasspath".equals(current)) {
				remaining.remove(); // remove the current option
				if (remaining.hasNext()) {
					final Iterable<? extends File> bootclasspaths = getBootclasspathFrom(remaining.next());
					if (bootclasspaths != null) {
						Iterable<? extends File> iterable = getLocation(StandardLocation.PLATFORM_CLASS_PATH);
						if ((this.flags & HAS_ENDORSED_DIRS) == 0
								&& (this.flags & HAS_EXT_DIRS) == 0) {
							// override default bootclasspath
							setLocation(StandardLocation.PLATFORM_CLASS_PATH, bootclasspaths);
						} else if ((this.flags & HAS_ENDORSED_DIRS) != 0) {
							// endorseddirs have been processed first
							setLocation(StandardLocation.PLATFORM_CLASS_PATH, 
									concatFiles(iterable, bootclasspaths));
						} else {
							// extdirs have been processed first
							setLocation(StandardLocation.PLATFORM_CLASS_PATH, 
									prependFiles(iterable, bootclasspaths));
						}
					}
					remaining.remove();
					this.flags |= HAS_BOOTCLASSPATH;
					return true;
				} else {
					throw new IllegalArgumentException();
				}
			}
			if ("-classpath".equals(current) || "-cp".equals(current)) {
				remaining.remove(); // remove the current option
				if (remaining.hasNext()) {
					final Iterable<? extends File> classpaths = getClasspathFrom(remaining.next());
					if (classpaths != null) setLocation(StandardLocation.CLASS_PATH, classpaths);
					remaining.remove();
					return true;
				} else {
					throw new IllegalArgumentException();
				}
			}
			if ("-encoding".equals(current)) {
				remaining.remove(); // remove the current option
				if (remaining.hasNext()) {
					this.charset = Charset.forName(remaining.next());
					remaining.remove();
					return true;
				} else {
					throw new IllegalArgumentException();
				}
			}
			if ("-sourcepath".equals(current)) {
				remaining.remove(); // remove the current option
				if (remaining.hasNext()) {
					final Iterable<? extends File> sourcepaths = getSourcepathFrom(remaining.next());
					if (sourcepaths != null) setLocation(StandardLocation.SOURCE_PATH, sourcepaths);
					remaining.remove();
					return true;
				} else {
					throw new IllegalArgumentException();
				}
			}
			if ("-extdirs".equals(current)) {
				remaining.remove(); // remove the current option
				if (remaining.hasNext()) {
					Iterable<? extends File> iterable = getLocation(StandardLocation.PLATFORM_CLASS_PATH);
					setLocation(StandardLocation.PLATFORM_CLASS_PATH, 
							concatFiles(iterable, getExtdirsFrom(remaining.next())));
					remaining.remove();
					this.flags |= HAS_EXT_DIRS;
					return true;
				} else {
					throw new IllegalArgumentException();
				}
			}
			if ("-endorseddirs".equals(current)) {
				remaining.remove(); // remove the current option
				if (remaining.hasNext()) {
					Iterable<? extends File> iterable = getLocation(StandardLocation.PLATFORM_CLASS_PATH);
					setLocation(StandardLocation.PLATFORM_CLASS_PATH, 
							prependFiles(iterable, getEndorsedDirsFrom(remaining.next())));
					remaining.remove();
					this.flags |= HAS_ENDORSED_DIRS;
					return true;
				} else {
					throw new IllegalArgumentException();
				}
			}
			if ("-d".equals(current)) {
				remaining.remove(); // remove the current option
				if (remaining.hasNext()) {
					final Iterable<? extends File> outputDir = getOutputDir(remaining.next());
					if (outputDir != null) {
						setLocation(StandardLocation.CLASS_OUTPUT, outputDir);
					}
					remaining.remove();
					return true;
				} else {
					throw new IllegalArgumentException();
				}
			}
		} catch (IOException e) {
			// ignore
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.tools.JavaFileManager#hasLocation(javax.tools.JavaFileManager.Location)
	 */
	public boolean hasLocation(Location location) {
		return this.locations.containsKey(location.getName());
	}

	/* (non-Javadoc)
	 * @see javax.tools.JavaFileManager#inferBinaryName(javax.tools.JavaFileManager.Location, javax.tools.JavaFileObject)
	 */
	public String inferBinaryName(Location location, JavaFileObject file) {
		String name = file.getName();
		JavaFileObject javaFileObject = null;
		int index = name.lastIndexOf('.');
		if (index != -1) {
			name = name.substring(0, index);
		}
		try {
			javaFileObject = getJavaFileForInput(location, name, file.getKind());
		} catch (IOException e) {
			// ignore
		}
		if (javaFileObject == null) {
			return null;
		}
		return normalized(name);
	}

	private boolean isArchive(File f) {
		String extension = getExtension(f);
		return extension.equalsIgnoreCase(".jar") || extension.equalsIgnoreCase(".zip");
	}

	/* (non-Javadoc)
	 * @see javax.tools.StandardJavaFileManager#isSameFile(javax.tools.FileObject, javax.tools.FileObject)
	 */
	public boolean isSameFile(FileObject fileObject1, FileObject fileObject2) {
		// EclipseFileManager creates only EcliseFileObject
		if (!(fileObject1 instanceof EclipseFileObject)) throw new IllegalArgumentException("Unsupported file object class : " + fileObject1.getClass());
		if (!(fileObject2 instanceof EclipseFileObject)) throw new IllegalArgumentException("Unsupported file object class : " + fileObject2.getClass());
		return fileObject1.equals(fileObject2);
	}
	/* (non-Javadoc)
	 * @see javax.tools.OptionChecker#isSupportedOption(java.lang.String)
	 */
	public int isSupportedOption(String option) {
		return Options.processOptionsFileManager(option);
	}

	/* (non-Javadoc)
	 * @see javax.tools.JavaFileManager#list(javax.tools.JavaFileManager.Location, java.lang.String, java.util.Set, boolean)
	 */
	public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse)
			throws IOException {
		
		Iterable<? extends File> allFilesInLocations = getLocation(location);
		if (allFilesInLocations == null) {
			throw new IllegalArgumentException("Unknown location : " + location);
		}
		
		ArrayList<JavaFileObject> collector = new ArrayList<JavaFileObject>();
		String normalizedPackageName = normalized(packageName);
		for (File file : allFilesInLocations) {
			collectAllMatchingFiles(file, normalizedPackageName, kinds, recurse, collector);
		}
		return collector;
	}

	private String normalized(String className) {
		char[] classNameChars = className.toCharArray();
		for (int i = 0, max = classNameChars.length; i < max; i++) {
			switch(classNameChars[i]) {
				case '\\' :
					classNameChars[i] = '/';
					break;
				case '.' :
					classNameChars[i] = '/';
			}
		}
		return new String(classNameChars);
	}

	private Iterable<? extends File> prependFiles(Iterable<? extends File> iterable,
			Iterable<? extends File> iterable2) {
		ArrayList<File> list = new ArrayList<File>();
		for (Iterator<? extends File> iterator = iterable2.iterator(); iterator.hasNext(); ) {
			list.add(iterator.next());
		}
		for (Iterator<? extends File> iterator = iterable.iterator(); iterator.hasNext(); ) {
			list.add(iterator.next());
		}
		return list;
	}

	/* (non-Javadoc)
	 * @see javax.tools.StandardJavaFileManager#setLocation(javax.tools.JavaFileManager.Location, java.lang.Iterable)
	 */
	public void setLocation(Location location, Iterable<? extends File> path) throws IOException {
		if (path != null) {
			if (location.isOutputLocation()) {
				// output location
				int count = 0;
				for (Iterator<? extends File> iterator = path.iterator(); iterator.hasNext(); ) {
					iterator.next();
					count++;
				}
				if (count != 1) {
					throw new IllegalArgumentException("output location can only have one path");
				}
			}
			this.locations.put(location.getName(), path);
		}
	}
	
	public void setLocale(Locale locale) {
		this.locale = locale;
	}
}
