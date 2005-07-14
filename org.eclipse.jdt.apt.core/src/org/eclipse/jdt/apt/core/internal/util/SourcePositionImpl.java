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

import com.sun.mirror.util.SourcePosition;
import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.apt.core.internal.EclipseMirrorImpl;
import org.eclipse.jdt.apt.core.internal.declaration.AnnotationMirrorImpl;
import org.eclipse.jdt.apt.core.internal.declaration.AnnotationValueImpl;
import org.eclipse.jdt.apt.core.internal.declaration.DeclarationImpl;

public class SourcePositionImpl implements SourcePosition
{
    private final int _startingOffset;
    private final int _length;
    private final int _line;
    private final int _column;
    /** the back pointer to the declaration that created this object */
    private final EclipseMirrorImpl _decl;

    public SourcePositionImpl(final int startingOffset,
                              final int length,
                              final int line,
                              final int column,
                              final DeclarationImpl decl)
    {
        _startingOffset = startingOffset;
        _length = length;
        _line = line;
        _column = column;
        _decl = decl;
        assert decl != null : "missing declaration [decl] == null."; //$NON-NLS-1$
    }
	
	public SourcePositionImpl(final int startingOffset,
							  final int length,
							  final int line,
							  final int column,
							  final AnnotationValueImpl decl )
	{
		_startingOffset = startingOffset;
        _length = length;
        _line = line;
        _column = column;
        _decl = decl;
        assert decl != null : "missing declaration [decl] == null."; //$NON-NLS-1$
	}
	
	public SourcePositionImpl(final int startingOffset,
							  final int length,
							  final int line,
							  final int column,
							  final AnnotationMirrorImpl decl )
	{
		_startingOffset = startingOffset;
        _length = length;
        _line = line;
        _column = column;
        _decl = decl;
        assert decl != null : "missing declaration [decl] == null."; //$NON-NLS-1$
	}
    
    public int line(){ return _line; }
    public int column(){ return _column; }
    public File file(){
        IResource resource = getResource();
        if( resource == null ) return null;
        final IPath absPath = resource.getRawLocation();
        if(absPath == null) return null;
        return new File( absPath.toOSString() );
    }

    // for use in IDE mode for squiggling.
    public int getStartingOffset(){ return _startingOffset; }
    public int getEndingOffset(){ return _startingOffset + _length; }
    public int getLength(){ return _length; }
    public IFile getResource(){
		if( _decl instanceof DeclarationImpl )
			return ((DeclarationImpl)_decl).getResource();
		else if( _decl instanceof AnnotationMirrorImpl )
			return ((AnnotationMirrorImpl)_decl).getResouce();
		else if( _decl instanceof AnnotationValueImpl )
			return ((AnnotationValueImpl)_decl).getResource();
		
		throw new IllegalStateException();
    }
    
    public String toString()
    {
    	StringBuilder buffer = new StringBuilder();
    	buffer.append("offset = "); //$NON-NLS-1$
    	buffer.append(_startingOffset); //$NON-NLS-1$
    	buffer.append(" line = "); //$NON-NLS-1$
    	buffer.append( _line ); //$NON-NLS-1$
    	buffer.append(" column = "); //$NON-NLS-1$
    	buffer.append( _column ); //$NON-NLS-1$
    	buffer.append(" length = "); //$NON-NLS-1$
    	buffer.append( _length ); //$NON-NLS-1$
    	
    	return buffer.toString();
    }
}
