/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IBufferChangedListener;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.core.BufferCache;
import org.eclipse.jdt.internal.core.Openable;
import org.eclipse.jdt.internal.core.OverflowingLRUCache;
import org.eclipse.jdt.internal.core.util.MementoTokenizer;

import junit.framework.Test;

/**
 * Tests internal Java element cache and buffer cache.
 */
public class OverflowingCacheTests extends ModifyingResourceTests {
	
	/**
	 * Simple implementation of IBuffer to test the BufferCache.
	 * A OverflowingTestBuffer is created with/without saved changes (determined
	 * by client), and is opened on creation.
	 */
	public class OverflowingTestBuffer implements IBuffer {
		/**
		 * True if this buffer is open, otherwise false.
		 */
		public boolean isOpen;
	
		/**
		 * True if this buffer has unsaved changes, otherwise false.
		 */
		public boolean hasUnsavedChanges;
	
		/**
		 * The cache this buffer is stored in
		 */
		public OverflowingLRUCache cache;
	
		/**
		 * This buffers owner (this buffer's key in this buffer's cache)
		 */
		public IOpenable owner;
		
		public OverflowingTestBuffer(boolean hasUnsavedChanges, OverflowingLRUCache cache) {
			super();
			this.hasUnsavedChanges= hasUnsavedChanges;
			this.cache= cache;
		}

		public void addBufferChangedListener(IBufferChangedListener listener) {
			// Not required for testing, does nothing.
		}

		public void append(char[] text) {
			// Sets this buffer to have unsaved changes, but the actual contents are ignored.
			this.hasUnsavedChanges= true;
		}

		public void append(String text) {
			// Sets this buffer to have unsaved changes, but the actual contents are ignored.
			this.hasUnsavedChanges= true;
		}

		public void close() {
			// Closes the buffer and removes it from its cache
			this.isOpen= false;
			this.cache.remove(this.owner);
		}

		public char getChar(int position) {
			// Returns a dummy char - test buffers have no contents
			return '.';
		}

		public char[] getCharacters() {
			// Returns null - test buffers have no contents
			return null;
		}

		public String getContents() {
			// Returns null - test buffers have no contents
			return null;
		}

		public int getLength() {
			// Returns 0 - test buffers have no contents
			return 0;
		}

		public IOpenable getOwner() {
			return this.owner;
		}

		public String getText(int beginIndex, int endIndex) {
			// Returns null - test buffers have no contents
			return null;
		}

		public IResource getUnderlyingResource() {
			// Returns null - test buffers have no underlying resource
			return null;
		}

		public boolean hasUnsavedChanges() {
			return this.hasUnsavedChanges;
		}

		public boolean isClosed() {
			return !this.isOpen;
		}

		public boolean isReadOnly() {
			return false;
		}

		public void removeBufferChangedListener(IBufferChangedListener listener) {
			// Not required for testing, does nothing.
		}

		public void replace(int position, int length, char[] text) {
			// Sets this buffer to have unsaved changes, but the actual contents are ignored.
			this.hasUnsavedChanges = true;
		}

		public void replace(int position, int length, String text) {
			// Sets this buffer to have unsaved changes, but the actual contents are ignored.
			this.hasUnsavedChanges = true;
		}

		public void save(IProgressMonitor progress) throws IllegalArgumentException {
			save(progress, false);
		}

		public void save(IProgressMonitor progress, boolean force) throws IllegalArgumentException {
			this.hasUnsavedChanges= false;
		}

		public void setContents(char[] contents) {
			// Sets this buffer to have unsaved changes, but the actual contents are ignored.
			this.hasUnsavedChanges= true;
		}

		public void setContents(String contents) {
			// Sets this buffer to have unsaved changes, but the actual contents are ignored.
			this.hasUnsavedChanges= true;
		}
	}

	/**
	 * Simple implementation of IOpenable to test the ElementCache.
	 * A OverflowingTestBuffer is created with/without saved changes (determined
	 * by client), and is opened on creation.
	 */
	
	public class OverflowingTestOpenable extends Openable {
		/**
		 * True if this element is open, otherwise false.
		 */
		public boolean isOpen;
	
		public OverflowingTestBuffer buffer;
	
		/**
		 * The cache this element is stored in
		 */
		public OverflowingLRUCache cache;

		/**
		 * Constructs a new openable, with unsaved changes as specified,
		 * that lives in the given cache, and opens it.
		 */
		public OverflowingTestOpenable(OverflowingTestBuffer buffer, OverflowingLRUCache cache) {
			super(null);
			this.buffer = buffer;
			buffer.owner = this;
			this.cache= cache;
			open(null);
		}
		
		protected boolean buildStructure(OpenableElementInfo info, IProgressMonitor pm, Map newElements, IResource underlyingResource) {
			return false;
		}
		
		public void close() {
			// Closes this element and removes if from the cache.
			this.isOpen = false;
			this.cache.remove(this);
		}

		public boolean equals(Object o) {
			if (!(o instanceof OverflowingTestOpenable)) return false;
			return super.equals(o);
		}

		public IBuffer getBuffer() {
			// This functionality not required for testing element cache - returns <code>null</code>.
			return null;
		}

		public int getElementType() {
			return 0;
		}

		public IJavaElement getHandleFromMemento(String token, MementoTokenizer memento, WorkingCopyOwner owner) {
			return null;
		}
		
		protected char getHandleMementoDelimiter() {
			return 0;
		}

		public IPath getPath() {
			return null;
		}

		public IResource getResource() {
			return null;
		}

		public boolean hasUnsavedChanges() {
			return this.buffer.hasUnsavedChanges();
		}

		public boolean isConsistent() {
			return true;
		}

		public boolean isOpen() {
			return this.isOpen;
		}

		public void makeConsistent(IProgressMonitor pm) {
			// This functionality not required for testing element cache - does nothing.
		}

		public void open(IProgressMonitor pm) {
			this.isOpen = true;
		}

		public void save(IProgressMonitor pm) {
			save(pm, false);
		}

		public void save(IProgressMonitor pm, boolean force) {
			this.buffer.hasUnsavedChanges = false;
		}
	}

	public static Test suite() {
		return new Suite(OverflowingCacheTests.class);
	}

	public OverflowingCacheTests(String name) {
		super(name);
	}

	/**
	 * Returns true if i is not divisible by 10.
	 */
	private boolean hasUnsavedChanges(int i) {
		return i % 10 != 0;
	}

	/**
	 * Creates an empty BufferCache and ensures that it answers correctly
	 * for #getSpaceLimit, #getCurrentSpace, and #getOverlflow.
	 */
	public void testBufferCacheCreation() {
		int spaceLimit = 10, actualSpaceLimit;
		int overflow = 0, actualOverflow;
		int current = 0, actualCurrent;
		OverflowingLRUCache cache = new BufferCache(spaceLimit);
		
		actualSpaceLimit = cache.getSpaceLimit();
		assertEquals("space limit incorrect ", spaceLimit, actualSpaceLimit);
		
		actualCurrent = cache.getCurrentSpace();
		assertEquals("current space incorrect ", current, actualCurrent);
		
		actualOverflow = cache.getOverflow();
		assertEquals("overflow space incorrect ", overflow, actualOverflow);
	}
	
	/**
	 * Creates an empty BufferCache of size 20, inserts 20 buffers
	 * and ensures that it is full, with zero overflow.
	 */
	public void testBufferCacheFill() {
		int spaceLimit = 20, actualSpaceLimit;
		int overflow = 0, actualOverflow;
		int current = 0, actualCurrent;
	
		OverflowingLRUCache cache = new BufferCache(spaceLimit);
		OverflowingTestBuffer[] buffers= new OverflowingTestBuffer[spaceLimit];
		OverflowingTestOpenable[] openables= new OverflowingTestOpenable[spaceLimit];
		for(int i = 0; i < spaceLimit; i++) {
			buffers[i] = new OverflowingTestBuffer(false, cache);
			openables[i]= new OverflowingTestOpenable(buffers[i], null);
			cache.put(openables[i], buffers[i]);
			current++;
		}

		actualSpaceLimit = cache.getSpaceLimit();
		assertEquals("space limit incorrect ", spaceLimit, actualSpaceLimit);
		
		actualCurrent = cache.getCurrentSpace();
		assertEquals("current space incorrect ", current, actualCurrent);
		
		actualOverflow = cache.getOverflow();
		assertEquals("overflow space incorrect ", overflow, actualOverflow);
	
		for(int i = spaceLimit - 1; i >= 0; i--) {
			Object value = cache.get(openables[i]);
			assertEquals("wrong value ("+i+")", buffers[i], value);
		}	
	}
	
	/**
	 * Creates an empty BufferCache of size 500, inserts 1000 buffers
	 * and ensures that the cache has 334 buffers left in it. When the
	 * 501st buffer is placed in the cache, the cache will remove 333
	 * buffers from the cache leaving 167 buffers in the cache. When the
	 * 833rd buffer is added, it will reach its space limit again, and
	 * shrink to 167 entries. The remaining 167 buffers will be added
	 * the cache, leaving it with 334 entries.
	 */
	public void testBufferCacheUseNoOverflow() {
		int spaceLimit = 500, actualSpaceLimit;
		int overflow = 0, actualOverflow;
		int actualCurrent, predictedCurrent = 334;
		int entryCount = 1000;
	
		OverflowingLRUCache cache = new BufferCache(spaceLimit);
		OverflowingTestOpenable[] openables = new OverflowingTestOpenable[entryCount];
		OverflowingTestBuffer[] buffers= new OverflowingTestBuffer[entryCount];
		for(int i = 0; i < entryCount; i++) {
			buffers[i]= new OverflowingTestBuffer(false, cache);
			openables[i] = new OverflowingTestOpenable(buffers[i], null);
			cache.put(openables[i], buffers[i]);
		}

		actualSpaceLimit = cache.getSpaceLimit();
		assertEquals("space limit incorrect ", spaceLimit, actualSpaceLimit);
		
		actualCurrent = cache.getCurrentSpace();
		assertEquals("current space incorrect ", predictedCurrent, actualCurrent);
		
		actualOverflow = cache.getOverflow();
		assertEquals("overflow space incorrect ", overflow, actualOverflow);
	
	
		for(int i = entryCount - 1; i >= entryCount - predictedCurrent; i--) {
			Object value = cache.get(openables[i]);
			assertEquals("wrong value ("+i+")", buffers[i], value);
		}
	
		// ensure first 500 entries swaped out
		for(int i = 0; i < spaceLimit; i++) {
			Object value = cache.get(openables[i]);
			assertEquals("entry should not be present", null, value);
		}	
	}

	/**
	 * Creates an empty BufferCache of size 500, inserts 1000 buffers.
	 * Nine of every ten entries cannot be removed - there are 1000 entries,
	 * leaving 900 entries which can't be closed. The table size should equal
	 * 900 when done with an overflow of 400.
	 *
	 * @see #hasUnsavedChanges(int)
	 */
	public void testBufferCacheUseOverflow() {
		int spaceLimit = 500;
		int entryCount = 1000;
		
		OverflowingLRUCache cache = new BufferCache(spaceLimit);
		OverflowingTestOpenable[] openables = new OverflowingTestOpenable[entryCount];
		OverflowingTestBuffer[] buffers= new OverflowingTestBuffer[entryCount];
		for (int i = 0; i < entryCount; i++) {
			boolean hasUnsavedChanges = hasUnsavedChanges(i);
			buffers[i]= new OverflowingTestBuffer(hasUnsavedChanges, cache);
			openables[i] = new OverflowingTestOpenable(buffers[i], null);
			cache.put(openables[i], buffers[i]);
		}
	
		Hashtable table = cache.getEntryTable();
		assertEquals("Hashtable wrong size", 900, table.size());	 	
		
		int actualCurrent = cache.getCurrentSpace();
		assertEquals("current space incorrect", 900, actualCurrent);
		
		int actualOverflow = cache.getOverflow();
		assertEquals("overflow space incorrect", 400, actualOverflow);
		
		for (int i = entryCount - 1; i >= 0; i--) {
			if (hasUnsavedChanges(i)) {
				Object value = cache.get(openables[i]);
				assertEquals("wrong value (" + i + ")", buffers[i], value);
			}
		}
	
		// the cache should shrink back to the spaceLimit as we save entries with unsaved changes
		for (int i= 0; i < entryCount; i++) {
			buffers[i].save(null, false);
		}
		// now add another entry to remove saved openables.
		OverflowingTestBuffer buffer1= new OverflowingTestBuffer(false, cache);
		IOpenable openable1= new OverflowingTestOpenable(buffer1, null);
		cache.put(openable1, buffer1);
		
		// now the size should be back to 168, with 0 overflow
		actualCurrent = cache.getCurrentSpace();
		assertEquals("current space incorrect (after flush)", 168, actualCurrent);
		actualOverflow = cache.getOverflow();
		assertEquals("overflow space incorrect (after flush)", 0, actualOverflow);	
	}

	/**
	 * Tests the actual Java Model buffer cache to ensure that it fills and empties
	 * as expected. This test first closes all open buffers to ensure the cache
	 * is empty.
	 *
	 * <p>60 compilation units are opened to fill the cache.
	 */
	public void testBufferRecycling() throws CoreException {
		Enumeration openBuffers= BufferManager.getDefaultBufferManager().getOpenBuffers();
		while (openBuffers.hasMoreElements()) {
			IBuffer buf= (IBuffer)openBuffers.nextElement();
			buf.close();
		}
		openBuffers= BufferManager.getDefaultBufferManager().getOpenBuffers();
		assertTrue("buffer cache not empty", !openBuffers.hasMoreElements());
		
		try {
			createJavaProject("P");
			for (int i = 0; i < 61; i++) {
				createFile(
					"P/X" + i + ".java",
					"public class X" + i + " {\n" +
					"}"
				);
			}
			IPackageFragment pkg= getPackage("P");
			ICompilationUnit[] cus = pkg.getCompilationUnits();
		
			// fill the cache - open 60 buffers
			int max = 60;
			for (int i= 0; i < max; i++) {
				cus[i].getBuffer();
			}
		
			// should be 60 buffers now
			openBuffers= BufferManager.getDefaultBufferManager().getOpenBuffers();
			int count= 0;
			while (openBuffers.hasMoreElements()) {
				openBuffers.nextElement();
				count++;
			}
			assertEquals("incorrect numbers of open buffers (1)", max, count);
		
			// open one more buffer should cause the cache to free up 2/3rds of its space
			// which means 40 buffers will be closed, and 1 will be added (this one),
			// totalling 21 = 60 - 40 + 1.
			cus[max].getBuffer();
			openBuffers= BufferManager.getDefaultBufferManager().getOpenBuffers();
			count= 0;
			while (openBuffers.hasMoreElements()) {
				openBuffers.nextElement();
				count++;
			}
			assertEquals("incorrect numbers of open buffers (2)", 21, count);	
		
			//re-opening a previous open buffer should work fine
			cus[0].getBuffer();
			openBuffers= BufferManager.getDefaultBufferManager().getOpenBuffers();
			count= 0;
			while (openBuffers.hasMoreElements()) {
				openBuffers.nextElement();
				count++;
			}
			assertEquals("incorrect numbers of open buffers (3)", 22, count);	
		} finally {
			deleteProject("P");
		}
	}

	/**
	 * Creates an empty ElementCache and ensures that it answers correctly
	 * for #getSpaceLimit, #getCurrentSpace, and #getOverlflow.
	 */
	public void testElementCacheCreation() {
		int spaceLimit = 10, actualSpaceLimit;
		int overflow = 0, actualOverflow;
		int current = 0, actualCurrent;
		OverflowingLRUCache cache = new ElementCache(spaceLimit);
		
		actualSpaceLimit = cache.getSpaceLimit();
		assertEquals("space limit incorrect ", spaceLimit, actualSpaceLimit);
		
		actualCurrent = cache.getCurrentSpace();
		assertEquals("current space incorrect ", current, actualCurrent);
		
		actualOverflow = cache.getOverflow();
		assertEquals("overflow space incorrect ", overflow, actualOverflow);
	}

	/**
	 * Creates an empty ElementCache of size 500, inserts 500 elements
	 * and ensures that it is full, with zero overflow.
	 */
	public void testElementCacheFill() {
		int spaceLimit = 500, actualSpaceLimit;
		int overflow = 0, actualOverflow;
		int current = 0, actualCurrent;
	
		OverflowingLRUCache cache = new ElementCache(spaceLimit);
		OverflowingTestOpenable[] openables = new OverflowingTestOpenable[spaceLimit];
		for(int i = 0; i < spaceLimit; i++) {
			openables[i] = new OverflowingTestOpenable(new OverflowingTestBuffer(false, null), cache);
			cache.put(openables[i], Integer.toString(i));
			current++;
		}

		actualSpaceLimit = cache.getSpaceLimit();
		assertEquals("space limit incorrect ", spaceLimit, actualSpaceLimit);
		
		actualCurrent = cache.getCurrentSpace();
		assertEquals("current space incorrect ", current, actualCurrent);
		
		actualOverflow = cache.getOverflow();
		assertEquals("overflow space incorrect ", overflow, actualOverflow);
	
		for(int i = spaceLimit - 1; i >= 0; i--) {
			Object value = cache.get(openables[i]);
			assertEquals("wrong value (" + i + ")", Integer.toString(i), value);
		}	
	}

	/**
	 * Creates an empty ElementCache of size 500, inserts 1000 elements
	 * and ensures that the cache has 334 elements left in it. When the
	 * 501st element is placed in the cache, the cache will remove 333
	 * elements from the cache leaving 167 elements in the cache. When the
	 * 833rd element is added, it will reach its space limit again, and
	 * shrink to 167 entries. The remaining 167 elements will be added
	 * the cache, leaving it with 334 entries.
	 */
	public void testElementCacheUseNoOverflow() {
		int spaceLimit = 500, actualSpaceLimit;
		int overflow = 0, actualOverflow;
		int actualCurrent, predictedCurrent = 334;
		int entryCount = 1000;
	
		OverflowingLRUCache cache = new ElementCache(spaceLimit);
		OverflowingTestOpenable[] openables = new OverflowingTestOpenable[entryCount];
		for(int i = 0; i < entryCount; i++) {
			openables[i] = new OverflowingTestOpenable(new OverflowingTestBuffer(false, null), cache);
			cache.put(openables[i], Integer.toString(i));
		}

		actualSpaceLimit = cache.getSpaceLimit();
		assertEquals("space limit incorrect ", spaceLimit, actualSpaceLimit);
		
		actualCurrent = cache.getCurrentSpace();
		assertEquals("current space incorrect ", predictedCurrent, actualCurrent);
		
		actualOverflow = cache.getOverflow();
		assertEquals("overflow space incorrect ", overflow, actualOverflow);
	
		for(int i = entryCount - 1; i >= entryCount - predictedCurrent; i--) {
			Object value = cache.get(openables[i]);
			assertEquals("wrong value (" + i + ")", Integer.toString(i), value);
		}
	
		// ensure first 500 entries swaped out
		for(int i = 0; i < spaceLimit; i++) {
			Object value = cache.get(openables[i]);
			assertEquals("entry should not be present", null, value);
		}	
	}

	/**
	 * Creates an empty ElementCache of size 500, inserts 1000 elements.
	 * Nine of every ten entries cannot be removed - there are 1000 entries,
	 * leaving 900 entries which can't be closed. Thetable size should equal
	 * 900 when done with an overflow of 400.
	 *
	 * @see #hasUnsavedChanges(int)
	 */
	public void testElementCacheUseOverflow() {
		int spaceLimit = 500;
		int entryCount = 1000;
		
		OverflowingLRUCache cache = new ElementCache(spaceLimit);
		OverflowingTestOpenable[] openables = new OverflowingTestOpenable[entryCount];
		for (int i = 0; i < entryCount; i++) {
			openables[i] = new OverflowingTestOpenable(new OverflowingTestBuffer(hasUnsavedChanges(i), null), cache);
			cache.put(openables[i], Integer.toString(i));
		}
	
		Hashtable table = cache.getEntryTable();
		assertEquals("Hashtable wrong size", 900, table.size());	 	
		int actualCurrent = cache.getCurrentSpace();
		assertEquals("current space incorrect", 900, actualCurrent);
		int actualOverflow = cache.getOverflow();
		assertEquals("overflow space incorrect", 400, actualOverflow);
		
		for (int i = entryCount - 1; i >= 0; i--) {
			if (hasUnsavedChanges(i)) {
				Object value = cache.get(openables[i]);
				assertEquals("wrong value (" + i + ")", Integer.toString(i), value);
			}
		}
	
		// the cache should shrink back to the spaceLimit as we save entries with unsaved changes
		for (int i= 0; i < entryCount; i++) {
			openables[i].save(null, false);
		}
		// now add another entry to remove saved openables.
		cache.put(Integer.toString(1001), new OverflowingTestOpenable(new OverflowingTestBuffer(false, null), cache));
		// now the size should be back to 500, with 0 overflow
		actualCurrent = cache.getCurrentSpace();
		assertEquals("current space incorrect (after flush)", 168, actualCurrent);
		actualOverflow = cache.getOverflow();
		assertEquals("overflow space incorrect (after flush)", 0, actualOverflow);	
	}

}
