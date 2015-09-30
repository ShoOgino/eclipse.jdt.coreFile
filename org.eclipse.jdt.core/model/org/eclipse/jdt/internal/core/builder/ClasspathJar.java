/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *     Tal Lev-Ami - added package cache for zip files
 *     Stephan Herrmann - Contribution for
 *								Bug 440477 - [null] Infrastructure for feeding external annotations into compilation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.builder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.util.JimageUtil;
import org.eclipse.jdt.internal.compiler.util.SimpleLookupTable;
import org.eclipse.jdt.internal.compiler.util.SimpleSet;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.util.Util;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.zip.*;

@SuppressWarnings("rawtypes")
public class ClasspathJar extends ClasspathLocation {

static class PackageCacheEntry {
	long lastModified;
	long fileSize;
	SimpleSet packageSet;

	PackageCacheEntry(long lastModified, long fileSize, SimpleSet packageSet) {
		this.lastModified = lastModified;
		this.fileSize = fileSize;
		this.packageSet = packageSet;
	}
}

static SimpleLookupTable PackageCache = new SimpleLookupTable();


protected static void addToPackageSet(SimpleSet packageSet, String fileName, boolean endsWithSep) {
	int last = endsWithSep ? fileName.length() : fileName.lastIndexOf('/');
	while (last > 0) {
		// extract the package name
		String packageName = fileName.substring(0, last);
		if (packageSet.addIfNotIncluded(packageName) == null)
			return; // already existed
		last = packageName.lastIndexOf('/');
	}
}

/**
 * Calculate and cache the package list available in the zipFile.
 * @param jar The ClasspathJar to use
 * @return A SimpleSet with the all the package names in the zipFile.
 */
static SimpleSet findPackageSet(ClasspathJar jar) {
	String zipFileName = jar.zipFilename;
	PackageCacheEntry cacheEntry = (PackageCacheEntry) PackageCache.get(zipFileName);
	long lastModified = jar.lastModified();
	long fileSize = new File(zipFileName).length();
	if (cacheEntry != null && cacheEntry.lastModified == lastModified && cacheEntry.fileSize == fileSize)
		return cacheEntry.packageSet;
	final SimpleSet packageSet = new SimpleSet(41);
	packageSet.add(""); //$NON-NLS-1$
	if (jar.isJimage) {
		try {
			org.eclipse.jdt.internal.compiler.util.JimageUtil.walkModuleImage(new File(zipFileName), 
					new org.eclipse.jdt.internal.compiler.util.JimageUtil.JimageVisitor<Path>() {

				@Override
				public FileVisitResult visitPackage(Path dir, Path mod, BasicFileAttributes attrs) throws IOException {
					ClasspathJar.addToPackageSet(packageSet, dir.toString(), true);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file, Path mod, BasicFileAttributes attrs) throws IOException {
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitModule(Path mod) throws IOException {
					return FileVisitResult.CONTINUE;
				}
			}, JimageUtil.NOTIFY_PACKAGES);
		} catch (IOException e) {
			// TODO: Should report better
		}
		PackageCache.put(zipFileName, new PackageCacheEntry(0, 0, packageSet));
	} else {
		packageSet.add(""); //$NON-NLS-1$
		for (Enumeration e = jar.zipFile.entries(); e.hasMoreElements(); ) {
			String fileName = ((ZipEntry) e.nextElement()).getName();
			addToPackageSet(packageSet, fileName, false);
		}
		PackageCache.put(zipFileName, new PackageCacheEntry(lastModified, fileSize, packageSet));
	}
	return packageSet;
}


String zipFilename; // keep for equals
IFile resource;
ZipFile zipFile;
ZipFile annotationZipFile;
long lastModified;
boolean closeZipFileAtEnd;
SimpleSet knownPackageNames;
AccessRuleSet accessRuleSet;
String externalAnnotationPath;
boolean isJimage;

ClasspathJar(IFile resource, AccessRuleSet accessRuleSet, IPath externalAnnotationPath) {
	this.resource = resource;
	try {
		java.net.URI location = resource.getLocationURI();
		if (location == null) {
			this.zipFilename = ""; //$NON-NLS-1$
		} else {
			File localFile = Util.toLocalFile(location, null);
			this.zipFilename = localFile.getPath();
		}
	} catch (CoreException e) {
		// ignore
	}
	this.zipFile = null;
	this.knownPackageNames = null;
	this.accessRuleSet = accessRuleSet;
	if (externalAnnotationPath != null)
		this.externalAnnotationPath = externalAnnotationPath.toString();
}

ClasspathJar(String zipFilename, long lastModified, AccessRuleSet accessRuleSet, IPath externalAnnotationPath) {
	this.zipFilename = zipFilename;
	this.lastModified = lastModified;
	this.zipFile = null;
	this.knownPackageNames = null;
	this.accessRuleSet = accessRuleSet;
	this.isJimage = JavaModelManager.isJimage(zipFilename);
	if (externalAnnotationPath != null)
		this.externalAnnotationPath = externalAnnotationPath.toString();
}

public ClasspathJar(ZipFile zipFile, AccessRuleSet accessRuleSet, IPath externalAnnotationPath) {
	this(zipFile.getName(), accessRuleSet, externalAnnotationPath);
	this.zipFile = zipFile;
}

public ClasspathJar(String fileName, AccessRuleSet accessRuleSet, IPath externalAnnotationPath) {
	this(fileName, 0, accessRuleSet, externalAnnotationPath);
	if (externalAnnotationPath != null)
		this.externalAnnotationPath = externalAnnotationPath.toString();
	this.isJimage = JavaModelManager.isJimage(fileName);

}

public void cleanup() {
	if (this.closeZipFileAtEnd) {
		if (this.zipFile != null) {
			try {
				this.zipFile.close();
			} catch(IOException e) { // ignore it
			}
			this.zipFile = null;
		}
		if (this.annotationZipFile != null) {
			try {
				this.annotationZipFile.close();
			} catch(IOException e) { // ignore it
			}
			this.annotationZipFile = null;
		}
	}
	if (!this.isJimage)
		this.knownPackageNames = null;
}

public boolean equals(Object o) {
	if (this == o) return true;
	if (!(o instanceof ClasspathJar)) return false;
	ClasspathJar jar = (ClasspathJar) o;
	if (this.isJimage && jar.isJimage) return this.zipFilename.endsWith(jar.zipFilename);
	if (this.accessRuleSet != jar.accessRuleSet)
		if (this.accessRuleSet == null || !this.accessRuleSet.equals(jar.accessRuleSet))
			return false;
	return this.zipFilename.equals(jar.zipFilename) && lastModified() == jar.lastModified();
}

public NameEnvironmentAnswer findClass(String binaryFileName, String qualifiedPackageName, String qualifiedBinaryFileName) {
	if (!isPackage(qualifiedPackageName)) return null; // most common case

	try {
		ClassFileReader reader = null;
		if (this.isJimage) {
			reader = ClassFileReader.readFromJimage(new File(this.zipFilename), qualifiedBinaryFileName);
		} else {
			reader = ClassFileReader.read(this.zipFile, qualifiedBinaryFileName);
		}
		if (reader != null) {
			String fileNameWithoutExtension = qualifiedBinaryFileName.substring(0, qualifiedBinaryFileName.length() - SuffixConstants.SUFFIX_CLASS.length);
			if (this.externalAnnotationPath != null) {
				try {
					this.annotationZipFile = reader.setExternalAnnotationProvider(this.externalAnnotationPath, fileNameWithoutExtension, this.annotationZipFile, null);
				} catch (IOException e) {
					// don't let error on annotations fail class reading
				}
			}
			if (this.accessRuleSet == null)
				return new NameEnvironmentAnswer(reader, null);
			return new NameEnvironmentAnswer(reader, this.accessRuleSet.getViolatedRestriction(fileNameWithoutExtension.toCharArray()));
		}
	} catch (IOException e) { // treat as if class file is missing
	} catch (ClassFormatException e) { // treat as if class file is missing
	}
	return null;
}

public IPath getProjectRelativePath() {
	if (this.resource == null) return null;
	return	this.resource.getProjectRelativePath();
}

public int hashCode() {
	return this.zipFilename == null ? super.hashCode() : this.zipFilename.hashCode();
}

public boolean isPackage(String qualifiedPackageName) {
	if (this.knownPackageNames != null)
		return this.knownPackageNames.includes(qualifiedPackageName);

	try {
		if (this.isJimage) {
			synchronized (this) {
				if (this.knownPackageNames == null) {
					this.knownPackageNames = findPackageSet(this);
				}
			}
		} else if (this.zipFile == null) {
			if (org.eclipse.jdt.internal.core.JavaModelManager.ZIP_ACCESS_VERBOSE) {
				System.out.println("(" + Thread.currentThread() + ") [ClasspathJar.isPackage(String)] Creating ZipFile on " + this.zipFilename); //$NON-NLS-1$	//$NON-NLS-2$
			}
			this.zipFile = new ZipFile(this.zipFilename);
			this.closeZipFileAtEnd = true;
			this.knownPackageNames = findPackageSet(this);
		} else {
			this.knownPackageNames = findPackageSet(this);
		}
	} catch(Exception e) {
		this.knownPackageNames = new SimpleSet(); // assume for this build the zipFile is empty
	}
	return this.knownPackageNames.includes(qualifiedPackageName);
}

public long lastModified() {
	if (this.lastModified == 0)
		this.lastModified = new File(this.zipFilename).lastModified();
	return this.lastModified;
}

public String toString() {
	String start = "Classpath jar file " + this.zipFilename; //$NON-NLS-1$
	if (this.accessRuleSet == null)
		return start;
	return start + " with " + this.accessRuleSet; //$NON-NLS-1$
}

public String debugPathString() {
	long time = lastModified();
	if (time == 0)
		return this.zipFilename;
	return this.zipFilename + '(' + (new Date(time)) + " : " + time + ')'; //$NON-NLS-1$
}

}
