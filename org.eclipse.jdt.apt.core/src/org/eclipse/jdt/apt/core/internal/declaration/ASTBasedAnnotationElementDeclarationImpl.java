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
package org.eclipse.jdt.apt.core.internal.declaration;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.AnnotationTypeElementDeclaration;
import com.sun.mirror.declaration.AnnotationValue;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.util.DeclarationVisitor;

public class ASTBasedAnnotationElementDeclarationImpl 
	extends ASTBasedMethodDeclarationImpl implements AnnotationTypeElementDeclaration{
	
	public ASTBasedAnnotationElementDeclarationImpl(
			final AnnotationTypeMemberDeclaration astNode,
			final IFile file,
            final BaseProcessorEnv env)
	{
		super(astNode, file, env);
	}

	public void accept(DeclarationVisitor visitor) {
		visitor.visitAnnotationTypeElementDeclaration(this);
	}

	public AnnotationTypeDeclaration getDeclaringType() {
		return (AnnotationTypeDeclaration) super.getDeclaringType();
	}

	/**
	 * @return the default value of this annotation element if one exists.
	 *         Return null if the annotation element is defined in binary
	 *         (feature not available right now). Return null if the annotation
	 *         element is part of a seconary type that is defined outside the
	 *         file associated with the environment.
	 */
	public AnnotationValue getDefaultValue() {
		
		final AnnotationTypeMemberDeclaration decl = getMemberAstNode();
		if (decl != null){
			final Expression defaultExpr = decl.getDefault();
			if( defaultExpr != null )
				return Factory.createDefaultValue(defaultExpr.resolveConstantExpressionValue(), this, _env);
		}

		return null;
	}

	public ASTNode getAstNodeForDefault() {
		final AnnotationTypeMemberDeclaration decl = (AnnotationTypeMemberDeclaration) getAstNode();
		if (decl != null)
			return decl.getDefault();

		return null;
	}
	
	public boolean isVarArgs(){ return false; }

    public String getSimpleName()
    {
    	final AnnotationTypeMemberDeclaration memberAstNode = getMemberAstNode(); 
    	final SimpleName nameNode = memberAstNode.getName();
    	return nameNode == null ? EMPTY_STRING : nameNode.getIdentifier();
    }  
    
    public TypeMirror getReturnType()
    {
    	final AnnotationTypeMemberDeclaration memberAstNode = getMemberAstNode();
    	final Type retType = memberAstNode.getType();
    	// some funny error case where the return type is missing but it's not a constructor.
    	if( retType == null )
    		return Factory.createErrorClassType(EMPTY_STRING);
        final ITypeBinding typeBinding = retType.resolveBinding();
        // This is most likely the reason that we end up with an ast based implementation.
        if( typeBinding == null ){
        	return Factory.createErrorClassType(retType.toString());
        }
        else{
        	final TypeMirror type = Factory.createTypeMirror(typeBinding, _env);
            if(type == null )
                return Factory.createErrorClassType(retType.toString());
            return type;
        }
    }
	
	public String toString()
    {
        final StringBuilder buffer = new StringBuilder();
        final AnnotationTypeMemberDeclaration memberAstNode = (AnnotationTypeMemberDeclaration) getAstNode();

        if( memberAstNode.getType() != null )
            buffer.append(memberAstNode.getType());
        buffer.append(' ');
        buffer.append(memberAstNode.getName());
        buffer.append("()"); //$NON-NLS-1$

        return buffer.toString();
    }

	public Collection<ParameterDeclaration> getParameters() {
		return Collections.emptyList();
	}

	public MirrorKind kind() {
		return MirrorKind.ANNOTATION_ELEMENT;
	}
	
	private AnnotationTypeMemberDeclaration getMemberAstNode(){ 
		return (AnnotationTypeMemberDeclaration)_astNode; 
	}
}
