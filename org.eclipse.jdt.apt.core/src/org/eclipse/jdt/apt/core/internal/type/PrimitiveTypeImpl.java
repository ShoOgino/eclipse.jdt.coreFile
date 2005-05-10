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

package org.eclipse.jdt.apt.core.internal.type; 

import com.sun.mirror.type.PrimitiveType;
import com.sun.mirror.util.TypeVisitor;
import org.eclipse.jdt.apt.core.internal.EclipseMirrorImpl;
import org.eclipse.jdt.apt.core.internal.env.ProcessorEnvImpl;
import org.eclipse.jdt.core.dom.ITypeBinding;

public class PrimitiveTypeImpl implements PrimitiveType, EclipseMirrorImpl
{	
    private final ITypeBinding _binding;    
    
    public PrimitiveTypeImpl(ITypeBinding binding)
    {
		assert binding != null;
        _binding = binding;        
    }
    public void accept(TypeVisitor visitor)
    {
        visitor.visitTypeMirror(this);
        visitor.visitPrimitiveType(this);
    }

    public PrimitiveType.Kind getKind()
    {
		final String name = getTypeBinding().getName();
		if( "int".equals(name) )
			return PrimitiveType.Kind.INT; 
		else if( "byte".equals(name) )
			return PrimitiveType.Kind.BYTE;
		else if( "short".equals(name) )
			return PrimitiveType.Kind.SHORT;
		else if( "char".equals(name) )
			return PrimitiveType.Kind.CHAR;
		else if( "long".equals(name) )
			return PrimitiveType.Kind.LONG;
		else if( "float".equals(name) )
			return PrimitiveType.Kind.FLOAT;
		else if( "double".equals(name) )
			return PrimitiveType.Kind.DOUBLE;
		else if( "boolean".equals(name))
			return PrimitiveType.Kind.BOOLEAN;
		else
			throw new IllegalStateException("unrecognized primitive type " + _binding);
    }
    
    public String toString(){ return _binding.getName(); }

    public ITypeBinding getTypeBinding(){ return _binding; }

    public MirrorKind kind(){ return MirrorKind.TYPE_PRIMITIVE; }
	
	public boolean equals(final Object obj)
	{
		try{
			return this._binding.isEqualTo( ((PrimitiveTypeImpl)obj)._binding );
		}
		catch(ClassCastException e){
			return false;
		}
	}
	
	public ProcessorEnvImpl getEnvironment(){ return null; }
}
