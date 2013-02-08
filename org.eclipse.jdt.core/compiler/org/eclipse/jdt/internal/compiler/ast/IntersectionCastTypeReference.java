/*******************************************************************************
 * Copyright (c) 2011, 2013 IBM Corporation and others.
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
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class IntersectionCastTypeReference extends TypeReference {
	public TypeReference[] typeReferences;

	public IntersectionCastTypeReference(TypeReference[] typeReferences) {
		this.typeReferences = typeReferences;
		this.sourceStart = typeReferences[0].sourceStart;
		int length = typeReferences.length;
		this.sourceEnd = typeReferences[length - 1].sourceEnd;
	}

	public TypeReference copyDims(int dim) {
		throw new UnsupportedOperationException(); // no syntax for this.
	}
	
	public TypeReference copyDims(int dim, Annotation[][] annotationsOnDimensions) {
		throw new UnsupportedOperationException(); // no syntax for this.
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.TypeReference#getLastToken()
	 */
	public char[] getLastToken() {
		return null;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.ast.ArrayQualifiedTypeReference#getTypeBinding(org.eclipse.jdt.internal.compiler.lookup.Scope)
	 */
	protected TypeBinding getTypeBinding(Scope scope) {
		return null; // not supported here - combined with resolveType(...)
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.TypeReference#getTypeBinding(org.eclipse.jdt.internal.compiler.lookup.Scope)
	 */
	public TypeBinding resolveType(BlockScope scope, boolean checkBounds) {
		return (this.resolvedType = null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.TypeReference#getTypeName()
	 */
	public char[][] getTypeName() {
		// we need to keep a return value that is a char[][]
		return this.typeReferences[0].getTypeName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.TypeReference#traverse(org.eclipse.jdt.internal.compiler.ASTVisitor, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			int length = this.typeReferences == null ? 0 : this.typeReferences.length;
			for (int i = 0; i < length; i++) {
				this.typeReferences[i].traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, scope);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.TypeReference#traverse(org.eclipse.jdt.internal.compiler.ASTVisitor, org.eclipse.jdt.internal.compiler.lookup.ClassScope)
	 */
	public void traverse(ASTVisitor visitor, ClassScope scope) {
		if (visitor.visit(this, scope)) {
			int length = this.typeReferences == null ? 0 : this.typeReferences.length;
			for (int i = 0; i < length; i++) {
				this.typeReferences[i].traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, scope);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.Expression#printExpression(int, java.lang.StringBuffer)
	 */
	public StringBuffer printExpression(int indent, StringBuffer output) {
		int length = this.typeReferences == null ? 0 : this.typeReferences.length;
		printIndent(indent, output);
		for (int i = 0; i < length; i++) {
			this.typeReferences[i].printExpression(0, output);
			if (i != length - 1) {
				output.append(" & "); //$NON-NLS-1$
			}
		}
		return output;
	}
}