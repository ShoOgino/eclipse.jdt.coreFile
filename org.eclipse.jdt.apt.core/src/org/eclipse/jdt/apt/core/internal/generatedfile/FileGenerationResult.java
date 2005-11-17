package org.eclipse.jdt.apt.core.internal.generatedfile;

import org.eclipse.core.resources.IFile;

/**
 * Simple container class for holding the result of file generation.<P>
 * 
 * It contains the generated file, as well as a boolean indicating if it
 * has changed since it was last seen. This is used to force compilation 
 * of the file later.
 */
public class FileGenerationResult {

	private final IFile file;
	private final boolean modified;
	private final boolean sourcepathChanged;
	
	public FileGenerationResult(final IFile file, final boolean modified, final boolean sourcepathChanged) {
		this.file = file;
		this.modified = modified;
		this.sourcepathChanged = sourcepathChanged;
	}
	
	public IFile getFile() {
		return file;
	}
	
	public boolean isModified() {
		return modified;
	}	
	
	public boolean hasSourcepathChanged() {
		return sourcepathChanged;
	}
}
