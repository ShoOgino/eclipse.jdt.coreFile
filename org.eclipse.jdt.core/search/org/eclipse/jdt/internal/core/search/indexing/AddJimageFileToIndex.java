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
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.indexing;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.attribute.BasicFileAttributes;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.jdt.internal.compiler.util.JimageUtil;
import org.eclipse.jdt.internal.compiler.util.SimpleLookupTable;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.index.Index;
import org.eclipse.jdt.internal.core.index.IndexLocation;
import org.eclipse.jdt.internal.core.search.JavaSearchDocument;
import org.eclipse.jdt.internal.core.search.processing.JobManager;

public class AddJimageFileToIndex extends IndexRequest {

	IFile resource;
	Scanner scanner;
	private IndexLocation indexFileURL;
	private final boolean forceIndexUpdate;
	static final char JAR_SEPARATOR = IJavaSearchScope.JAR_FILE_ENTRY_SEPARATOR.charAt(0);
	
	enum FILE_INDEX_STATE {
		EXISTS,
		DELETED
	}


	public AddJimageFileToIndex(IFile resource, IndexLocation indexFile, IndexManager manager) {
		this(resource, indexFile, manager, false);
	}
	public AddJimageFileToIndex(IFile resource, IndexLocation indexFile, IndexManager manager, final boolean updateIndex) {
		super(resource.getFullPath(), manager);
		this.resource = resource;
		this.indexFileURL = indexFile;
		this.forceIndexUpdate = updateIndex;
	}
	public AddJimageFileToIndex(IPath jimagePath, IndexLocation indexFile, IndexManager manager) {
		this(jimagePath, indexFile, manager, false);
	}
	public AddJimageFileToIndex(IPath jimagePath, IndexLocation indexFile, IndexManager manager, final boolean updateIndex) {
		// external JAR scenario - no resource
		super(jimagePath, manager);
		this.indexFileURL = indexFile;
		this.forceIndexUpdate = updateIndex;
	}
	public boolean equals(Object o) {
		if (o instanceof AddJimageFileToIndex) {
			if (this.resource != null)
				return this.resource.equals(((AddJimageFileToIndex) o).resource);
			if (this.containerPath != null)
				return this.containerPath.equals(((AddJimageFileToIndex) o).containerPath);
		}
		return false;
	}
	public int hashCode() {
		if (this.resource != null)
			return this.resource.hashCode();
		if (this.containerPath != null)
			return this.containerPath.hashCode();
		return -1;
	}
	
	private class JimageTraverser implements org.eclipse.jdt.internal.compiler.util.JimageUtil.JimageVisitor<java.nio.file.Path> {
		
		SimpleLookupTable indexedFileNames;
		public JimageTraverser() {
		}
		public JimageTraverser(SimpleLookupTable indexedFileNames) {
			this.indexedFileNames = indexedFileNames;
		}

		@Override
		public FileVisitResult visitPackage(java.nio.file.Path dir, java.nio.file.Path mod, BasicFileAttributes attrs)
				throws IOException {
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(java.nio.file.Path path, java.nio.file.Path mod, BasicFileAttributes attrs)
				throws IOException {
			String name = path.getFileName().toString();
			if (Util.isClassFileName(name) && 
					isValidPackageNameForClass(name)) {
				this.indexedFileNames.put(name, FILE_INDEX_STATE.EXISTS);
			}
			return FileVisitResult.CONTINUE;
		}
		@Override
		public FileVisitResult visitModule(java.nio.file.Path mod) throws IOException {
			return FileVisitResult.CONTINUE;
		}
	}
	
	private class JimageIndexer extends JimageTraverser {
		final SearchParticipant participant;
		final IPath indexPath;
		final IndexManager indexManager;
		final IPath container;
		final Index index;
		final File jimage;

		public JimageIndexer(File jimage, SearchParticipant participant, Index index, IPath container, IndexManager indexManager) {
			this.jimage = jimage;
			this.participant = (participant != null) ? participant : SearchEngine.getDefaultSearchParticipant();
			this.index = index;
			IndexLocation indexLocation = index.getIndexLocation();
			this.indexPath = indexLocation != null ? new Path(indexLocation.getCanonicalFilePath()) : null;
			this.container = container;
			this.indexManager = indexManager;
		}

		public FileVisitResult visitFile(java.nio.file.Path path, java.nio.file.Path mod, BasicFileAttributes attrs)
				throws IOException {
			String name = path.getFileName().toString();
			if (Util.isClassFileName(name) && 
					isValidPackageNameForClass(name)) {
				try {
					String fullPath = path.toString();
					final byte[] classFileBytes = JimageUtil.getClassfileContent(this.jimage, fullPath, mod.toString());
					String docFullPath =  this.container.toString() + JAR_SEPARATOR + fullPath;
					JavaSearchDocument entryDocument = new JavaSearchDocument(docFullPath, classFileBytes, this.participant);
					this.indexManager.indexDocument(entryDocument, this.participant, this.index, this.indexPath);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return FileVisitResult.CONTINUE;
		}
	}

	public boolean execute(IProgressMonitor progressMonitor) {

		if (this.isCancelled || progressMonitor != null && progressMonitor.isCanceled()) return true;

		if (hasPreBuiltIndex()) {
			boolean added = this.manager.addIndex(this.containerPath, this.indexFileURL);
			if (added) return true;	
			this.indexFileURL = null;
		}

		try {
			// if index is already cached, then do not perform any check
			// MUST reset the IndexManager if a jar file is changed
			if (this.manager.getIndexForUpdate(this.containerPath, false, /*do not reuse index file*/ false /*do not create if none*/) != null) {
				if (JobManager.VERBOSE)
					org.eclipse.jdt.internal.core.util.Util.verbose("-> no indexing required (index already exists) for " + this.containerPath); //$NON-NLS-1$
				return true;
			}

			final Index index = this.manager.getIndexForUpdate(this.containerPath, true, /*reuse index file*/ true /*create if none*/);
			if (index == null) {
				if (JobManager.VERBOSE)
					org.eclipse.jdt.internal.core.util.Util.verbose("-> index could not be created for " + this.containerPath); //$NON-NLS-1$
				return true;
			}
			index.separator = JAR_SEPARATOR;
			ReadWriteMonitor monitor = index.monitor;
			if (monitor == null) {
				if (JobManager.VERBOSE)
					org.eclipse.jdt.internal.core.util.Util.verbose("-> index for " + this.containerPath + " just got deleted"); //$NON-NLS-1$//$NON-NLS-2$
				return true; // index got deleted since acquired
			}
			try {
				final String fileName;
				final IPath container;
				monitor.enterWrite(); // ask permission to write

				if (this.resource != null) {
					URI location = this.resource.getLocationURI();
					if (location == null) return false;
					if (JavaModelManager.JIMAGE_ACCESS_VERBOSE)
						System.out.println("(" + Thread.currentThread() + ") [AddJimageFileToIndex.execute()] Creating ZipFile on " + location.getPath()); //$NON-NLS-1$	//$NON-NLS-2$
					File file = null;
					try {
						file = org.eclipse.jdt.internal.core.util.Util.toLocalFile(location, progressMonitor);
					} catch (CoreException e) {
						if (JobManager.VERBOSE) {
							org.eclipse.jdt.internal.core.util.Util.verbose("-> failed to index " + location.getPath() + " because of the following exception:"); //$NON-NLS-1$ //$NON-NLS-2$
							e.printStackTrace();
						}
					}
					if (file == null) {
						if (JobManager.VERBOSE)
							org.eclipse.jdt.internal.core.util.Util.verbose("-> failed to index " + location.getPath() + " because the file could not be fetched"); //$NON-NLS-1$ //$NON-NLS-2$
						return false;
					}
					fileName = file.getAbsolutePath();
					container =  this.resource.getFullPath().makeRelative();
					// absolute path relative to the workspace
				} else {
					
					fileName = this.containerPath.toOSString();
					container = this.containerPath;
				}


				if (JobManager.VERBOSE)
					org.eclipse.jdt.internal.core.util.Util.verbose("-> indexing " + fileName); //$NON-NLS-1$
				long initialTime = System.currentTimeMillis();
				String[] paths = index.queryDocumentNames(""); // all file names //$NON-NLS-1$
				if (paths != null) {
					int max = paths.length;
					/* check integrity of the existing index file
					 * if the length is equal to 0, we want to index the whole jimage again
					 * If not, then we want to check that there is no missing entry, if
					 * one entry is missing then we recreate the index
					 */
					
					final SimpleLookupTable indexedFileNames = new SimpleLookupTable(max == 0 ? 33 : max + 11);
					for (int i = 0; i < max; i++)
						indexedFileNames.put(paths[i], FILE_INDEX_STATE.DELETED);
					
					org.eclipse.jdt.internal.compiler.util.JimageUtil.walkModuleImage(new File(fileName), 
							new JimageTraverser(indexedFileNames));

					boolean needToReindex = indexedFileNames.elementSize != max; // a new file was added
					if (!needToReindex) {
						Object[] valueTable = indexedFileNames.valueTable;
						for (int i = 0, l = valueTable.length; i < l; i++) {
							if (valueTable[i] == FILE_INDEX_STATE.DELETED) {
								needToReindex = true; // a file was deleted so re-index
								break;
							}
						}
						if (!needToReindex) {
							if (JobManager.VERBOSE)
								org.eclipse.jdt.internal.core.util.Util.verbose("-> no indexing required (index is consistent with library) for " //$NON-NLS-1$
								+ fileName + " (" //$NON-NLS-1$
								+ (System.currentTimeMillis() - initialTime) + "ms)"); //$NON-NLS-1$
							this.manager.saveIndex(index); // to ensure its placed into the saved state
							return true;
						}
					}
				}

				// Index the jimage for the first time or reindex the jimage in case the previous index file has been corrupted
				// index already existed: recreate it so that we forget about previous entries
				if (!this.manager.resetIndex(this.containerPath)) {
					// failed to recreate index, see 73330
					this.manager.removeIndex(this.containerPath);
					return false;
				}
				
				File jimage = new File(fileName);
				org.eclipse.jdt.internal.compiler.util.JimageUtil.walkModuleImage(jimage, 
						new JimageIndexer(jimage, SearchEngine.getDefaultSearchParticipant(), index, container, this.manager));

				if(this.forceIndexUpdate) {
					this.manager.savePreBuiltIndex(index);
				}
				else {
					this.manager.saveIndex(index);
				}
				if (JobManager.VERBOSE)
					org.eclipse.jdt.internal.core.util.Util.verbose("-> done indexing of " //$NON-NLS-1$
						+ fileName + " (" //$NON-NLS-1$
						+ (System.currentTimeMillis() - initialTime) + "ms)"); //$NON-NLS-1$
			} finally {
				monitor.exitWrite();
			}
		} catch (IOException e ) {
			if (JobManager.VERBOSE) {
				org.eclipse.jdt.internal.core.util.Util.verbose("-> failed to index " + this.containerPath + " because of the following exception:"); //$NON-NLS-1$ //$NON-NLS-2$
				e.printStackTrace();
			}
			this.manager.removeIndex(this.containerPath);
			return false;
		}
		return true;
	}
	public String getJobFamily() {
		if (this.resource != null)
			return super.getJobFamily();
		return this.containerPath.toOSString(); // external jar
	}	
	private boolean isIdentifier() throws InvalidInputException {
		switch(this.scanner.scanIdentifier()) {
			// assert and enum will not be recognized as java identifiers 
			// in 1.7 mode, which are in 1.3.
			case TerminalTokens.TokenNameIdentifier:
			case TerminalTokens.TokenNameassert:
			case TerminalTokens.TokenNameenum:
				return true;
			default:
				return false;
		}
	}
	boolean isValidPackageNameForClass(String className) {
		char[] classNameArray = className.toCharArray();
		// use 1.7 as the source level as there are more valid identifiers in 1.7 mode
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376673
		if (this.scanner == null)
			this.scanner = new Scanner(false /* comment */, true /* whitespace */, false /* nls */,
					ClassFileConstants.JDK1_7/* sourceLevel */, null/* taskTag */, null/* taskPriorities */, true /* taskCaseSensitive */);
		
		this.scanner.setSource(classNameArray); 
		this.scanner.eofPosition = classNameArray.length - SuffixConstants.SUFFIX_CLASS.length;
		try {
			if (isIdentifier()) {
				while (this.scanner.eofPosition > this.scanner.currentPosition) {
					if (this.scanner.getNextChar() != '/' || this.scanner.eofPosition <= this.scanner.currentPosition) {
						return false;
					}
					if (!isIdentifier()) return false;
				}
				return true;
			}
		} catch (InvalidInputException e) {
			// invalid class name
		}
		return false;
	}
	protected Integer updatedIndexState() {

		Integer updateState = null;
		if(hasPreBuiltIndex()) {
			updateState = IndexManager.REUSE_STATE;
		}
		else {
			updateState = IndexManager.REBUILDING_STATE;
		}
		return updateState;
	}
	public String toString() {
		return "indexing " + this.containerPath.toString(); //$NON-NLS-1$
	}

	protected boolean hasPreBuiltIndex() {
		return !this.forceIndexUpdate && (this.indexFileURL != null && this.indexFileURL.exists());
	}
}