package org.eclipse.jdt.internal.core.search.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.core.index.*;
import org.eclipse.jdt.internal.core.search.processing.*;

import org.eclipse.core.resources.*;

import java.io.*;

class RemoveFromIndex implements IJob {
	String resourceName;
	IResource indexedContainer;
	IndexManager manager;
	public RemoveFromIndex(
		String resourceName,
		IResource indexedContainer,
		IndexManager manager) {
		this.resourceName = resourceName;
		this.indexedContainer = indexedContainer;
		this.manager = manager;
	}
	public boolean belongsTo(String jobFamily) {
		return jobFamily.equals(indexedContainer.getProject().getName());
	}
	public boolean execute(IProgressMonitor progressMonitor) {
		
		if (progressMonitor != null && progressMonitor.isCanceled()) return COMPLETE;
		
		try {
			if (this.indexedContainer.isAccessible()) {
				IIndex index = manager.getIndex(this.indexedContainer.getFullPath());
				if (index == null)
					return COMPLETE;

				/* ensure no concurrent write access to index */
				ReadWriteMonitor monitor = manager.getMonitorFor(index);
				if (monitor == null)
					return COMPLETE; // index got deleted since acquired
				try {
					monitor.enterWrite(); // ask permission to write
					index.remove(resourceName);
				} finally {
					monitor.exitWrite(); // free write lock
				}
			}
		} catch (IOException e) {
			return FAILED;
		}
		return COMPLETE;
	}
	public String toString() {
		return "removing from index " + resourceName; //$NON-NLS-1$
	}
}