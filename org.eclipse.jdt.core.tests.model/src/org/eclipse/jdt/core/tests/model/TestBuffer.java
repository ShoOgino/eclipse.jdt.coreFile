/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.ArrayList;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.*;

/*
 * A simple implementation of IBuffer.
 */
public class TestBuffer implements IBuffer {
		IOpenable owner;
		ArrayList changeListeners;
		char[] contents = null;
		public TestBuffer(IOpenable owner) {
			this.owner = owner;
		}
		/*
		 * @see IBuffer#addBufferChangedListener(IBufferChangedListener)
		 */
		public void addBufferChangedListener(IBufferChangedListener listener) {
			if (this.changeListeners == null) {
				this.changeListeners = new ArrayList(5);
			}
			if (!this.changeListeners.contains(listener)) {
				this.changeListeners.add(listener);
			}
		}

		/*
		 * @see IBuffer#append(char[])
		 */
		public void append(char[] text) {
		}

		/*
		 * @see IBuffer#append(String)
		 */
		public void append(String text) {
		}

		/*
		 * @see IBuffer#close()
		 */
		public void close() {
			this.contents = null; // mark as closed
			if (this.changeListeners != null) {
				BufferChangedEvent event = null;
				event = new BufferChangedEvent(this, 0, 0, null);
				for (int i = 0, size = this.changeListeners.size(); i < size; ++i) {
					IBufferChangedListener listener = (IBufferChangedListener) this.changeListeners.get(i);
					listener.bufferChanged(event);
				}
				this.changeListeners = null;
			}
		}

		/*
		 * @see IBuffer#getChar(int)
		 */
		public char getChar(int position) {
			return 0;
		}

		/*
		 * @see IBuffer#getCharacters()
		 */
		public char[] getCharacters() {
			return contents;
		}

		/*
		 * @see IBuffer#getContents()
		 */
		public String getContents() {
			return new String(contents);
		}

		/*
		 * @see IBuffer#getLength()
		 */
		public int getLength() {
			return contents.length;
		}

		/*
		 * @see IBuffer#getOwner()
		 */
		public IOpenable getOwner() {
			return this.owner;
		}

		/*
		 * @see IBuffer#getText(int, int)
		 */
		public String getText(int offset, int length) {
			return null;
		}

		/*
		 * @see IBuffer#getUnderlyingResource()
		 */
		public IResource getUnderlyingResource() {
			return null;
		}

		/*
		 * @see IBuffer#hasUnsavedChanges()
		 */
		public boolean hasUnsavedChanges() {
			return false;
		}

		/*
		 * @see IBuffer#isClosed()
		 */
		public boolean isClosed() {
			return this.contents == null;
		}

		/*
		 * @see IBuffer#isReadOnly()
		 */
		public boolean isReadOnly() {
			return false;
		}

		/*
		 * @see IBuffer#removeBufferChangedListener(IBufferChangedListener)
		 */
		public void removeBufferChangedListener(IBufferChangedListener listener) {
			if (this.changeListeners != null) {
				this.changeListeners.remove(listener);
				if (this.changeListeners.size() == 0) {
					this.changeListeners = null;
				}
			}
		}

		/*
		 * @see IBuffer#replace(int, int, char[])
		 */
		public void replace(int position, int length, char[] text) {
		}

		/*
		 * @see IBuffer#replace(int, int, String)
		 */
		public void replace(int position, int length, String text) {
		}

		/*
		 * @see IBuffer#save(IProgressMonitor, boolean)
		 */
		public void save(IProgressMonitor progress, boolean force)
			throws JavaModelException {
		}

		/*
		 * @see IBuffer#setContents(char[])
		 */
		public void setContents(char[] characters) {
			contents = characters;
		}

		/*
		 * @see IBuffer#setContents(String)
		 */
		public void setContents(String characters) {
			contents = characters.toCharArray();
		}

}