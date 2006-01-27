/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    jgarms@bea.com - initial API and implementation
 *    
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.eclipse.jdt.apt.core.AptPlugin;

/**
 * This classloader allows us to close out underlying jars,
 * so that projects can be deleted even if they contain
 * factory jars that are in use.<P>
 * 
 * This classloader caches open jars while it is in use,
 * and once closed it will close those jars. It can still be used
 * after that point, but it will open and close on each classloader
 * operation.
 */
public class JarClassLoader extends ClassLoader {
	
	// This is nulled out when the classloader is closed
	private List<JarFile> _jars;
	private final List<File> _files;
	private List<JarCLInputStream> _openStreams = new LinkedList<JarCLInputStream>();
	private boolean _open = true;
	
	public JarClassLoader(List<File> jarFiles, final ClassLoader parent) {
		super(parent);
		_files = jarFiles;
		open();
	}
	
	private void open() {
		// Create all jar files
		_jars = new ArrayList<JarFile>(_files.size());
		for (File f : _files) {
			try {
				JarFile jar = new JarFile(f);
				_jars.add(jar);
			}
			catch (IOException ioe) {
				AptPlugin.log(ioe, "Unable to create JarFile for file: " + f); //$NON-NLS-1$
			}
		}
	}
	
	public synchronized void close() {
		if (! _open) return;
		_open = false;
		
		for (JarCLInputStream st : _openStreams) {
			try {
				st.close();
			}
			catch (IOException ioe) {
				AptPlugin.log(ioe, "Failed to close stream"); //$NON-NLS-1$
			}
		}
		_openStreams = null;

		for (JarFile jar : _jars) {
			try {
				jar.close();
			}
			catch (IOException ioe) {
				AptPlugin.log(ioe, "Failed to close jar: " + jar); //$NON-NLS-1$
			}
		}
		_jars = null;
	}	
	
	private InputStream openInputStream(InputStream in) {
		JarCLInputStream result = new JarCLInputStream(in);
		_openStreams.add(result);
		return in;
	}
	
	private synchronized void closeInputStream(JarCLInputStream in) {
		if (_open)
			_openStreams.remove(in);
	}
	
	@Override
	protected synchronized Class<?> findClass(String name) throws ClassNotFoundException {
		if (!_open)
			throw new ClassNotFoundException("Classloader closed: " + name); //$NON-NLS-1$

		byte[] b = loadClassData(name);
		if (b == null) {
			throw new ClassNotFoundException("Could not find class " + name); //$NON-NLS-1$
		}
		Class<?> clazz = defineClass(name, b, 0, b.length);
		// Define the package if necessary
		String pkgName = getPackageName(name);
		if (pkgName != null) {
			Package pkg = getPackage(pkgName);
			if (pkg == null) {
				definePackage(pkgName, null, null, null, null, null, null, null);
			}
		}
		return clazz;
	}
	
	private String getPackageName(String fullyQualifiedName) {
		int index = fullyQualifiedName.lastIndexOf('.');
		if (index != -1) {
			return fullyQualifiedName.substring(0, index);
		}
		return null;
	}
	
	// returns null if no class found
	private byte[] loadClassData(String name) {
		name = name.replace('.','/');
		InputStream input = getResourceAsStream(name + ".class"); //$NON-NLS-1$
		if (input == null)
			return null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
	        int len;
	        while ((len = input.read(buf)) > 0) {
	            baos.write(buf, 0, len);
	        }
	        baos.close();
	        return baos.toByteArray();
		}
		catch (IOException ioe) {
			return null;
		}
		finally {
			try {input.close();} catch (IOException ioe) {}
		}		
	}
	
	@Override
	public synchronized InputStream getResourceAsStream(String name) {
		InputStream input = getParent().getResourceAsStream(name);
		if (input != null)
			return input;
		
		if (!_open) 
			return null;
	
		for (JarFile j : _jars) {
			try {
				ZipEntry entry = j.getEntry(name);
				if (entry != null) {
					InputStream zipInput = j.getInputStream(entry);
					return openInputStream(zipInput);
				}
			}
			catch (IOException ioe) {
				AptPlugin.log(ioe, "Unable to get entry from jar: " + j); //$NON-NLS-1$
			}
		}
		return null;
	}
	
	/**
	 * This is difficult to implement and close out resources underneath.
	 * Delaying until someone actually requests this.
	 * 
	 * If we actually contain the entry throw UnsupportedOperationException,
	 * else return null in case another classloader can handle this.
	 */
	@Override
	public URL getResource(String name) {
		for (JarFile j : _jars) {
			ZipEntry entry = j.getEntry(name);
			if (entry != null) {
				throw new UnsupportedOperationException("getResource() not implemented: " + name); //$NON-NLS-1$
			}
		}
		return null;
	}

	/**
	 * This is difficult to implement and close out resources underneath.
	 * Delaying until someone actually requests this.
	 */
	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		throw new UnsupportedOperationException("getResources() not implemented"); //$NON-NLS-1$
	}
	
	private class JarCLInputStream extends InputStream {
		
		private boolean _closed = false;
		
		private final InputStream _input;
		
		public JarCLInputStream(InputStream origInput) {
			_input = origInput;
		}

		@Override
		public void close() throws IOException {
 			if (_closed) {
				// NOOP
				return;
			}
			try {
				super.close();
				_input.close();
				_closed = true;
			}
			finally {
				closeInputStream(this);
			}
		}

		@Override
		public int read() throws IOException {
			return _input.read();
		}

		@Override
		public int available() throws IOException {
			return _input.available();
		}

		@Override
		public synchronized void mark(int readlimit) {
			_input.mark(readlimit);
		}

		@Override
		public boolean markSupported() {
			return _input.markSupported();
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			return _input.read(b, off, len);
		}

		@Override
		public int read(byte[] b) throws IOException {
			return _input.read(b);
		}

		@Override
		public synchronized void reset() throws IOException {
			_input.reset();
		}

		@Override
		public long skip(long n) throws IOException {
			return _input.skip(n);
		}
	}
}
