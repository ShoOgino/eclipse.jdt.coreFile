/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 *  Simple utility class to encapsulate an mkdirs() that avoids a timing issue
 *  in the jdk.  
 */
public final class FileSystemUtil
{
	private FileSystemUtil() {}
	
    public static void mkdirs( File parent )
    {
        if ( parent == null )
            return;
        
        // It is necessary to synchronize to prevent timing issues while creating the parent directories
        // We can be codegening multiple files that go into the same directory at the same time.        
        synchronized (FileSystemUtil.class) {
            if (!parent.exists()) {
                boolean succeed = false;
                for (int i = 0 ; !succeed && i < 5 ; i++)
                    succeed = parent.mkdirs();
            }
        }
    }
    
    /**
     * Returns the contents of a file as a string in UTF8 format
     */
    public static String getContentsOfIFile(IFile file) throws IOException, CoreException {
    	return getContents(file.getContents(true));
    }
    
    public static String getContentsOfFile(File file) throws IOException {
    	return getContents(new FileInputStream(file));
    }
    
    private static String getContents(InputStream in) throws IOException {
    	try {
    		ByteArrayOutputStream out = new ByteArrayOutputStream();
    		byte[] buffer = new byte[512];
    		int len;
    		while ((len = in.read(buffer)) > 0) {
    			out.write(buffer, 0, len);
    		}
    		out.close();
    		String s = new String(out.toByteArray(), "UTF8");
    		return s;
    	}
    	finally {
    		try {in.close();} catch (IOException ioe) {}
    	}
    }
    
    /**
     * Stores a string into an Eclipse file in UTF8 format.  The file
     * will be created if it does not already exist.
     * @throws IOException, CoreException
     */
    public static void writeStringToIFile(IFile file, String contents) throws IOException, CoreException {
    	byte[] data = contents.getBytes("UTF8");
    	ByteArrayInputStream input = new ByteArrayInputStream(data);
    	// create with FORCE will overwrite if the file already exists
    	file.create(input, IResource.FORCE, null);
    }
    
    /**
     * Stores a string into an ordinary workspace file in UTF8 format.
     * The file will be created if it does not already exist.
     * @throws IOException
     */
    public static void writeStringToFile(File file, String contents) throws IOException {
    	byte[] data = contents.getBytes("UTF8");
    	OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
    	try {
    		for (byte b : data) {
    			out.write(b);
    		}
    	}
    	finally {
    		try {out.close();} catch (IOException ioe) {}
    	}
    }
}
