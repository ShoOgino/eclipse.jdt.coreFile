/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist.complete;

import org.eclipse.jdt.internal.compiler.ast.JavadocSingleNameReference;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class CompletionOnJavadocParamNameReference extends JavadocSingleNameReference implements CompletionOnJavadoc {
	public int completionFlags = JAVADOC;
	public char[][] missingParams;
	public char[][] missingTypeParams;

	public CompletionOnJavadocParamNameReference(long pos, int start, int end) {
		super(null, pos, start, end);
	}

	/**
	 * @param flags The completionFlags to set.
	 */
	public void addCompletionFlags(int flags) {
		this.completionFlags |= flags;
	}

	public CompletionOnJavadocParamNameReference(JavadocSingleNameReference nameRef) {
		super(nameRef.token, (((long)nameRef.sourceStart)<<32)+nameRef.sourceEnd, nameRef.tagSourceStart, nameRef.tagSourceStart);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.AllocationExpression#printExpression(int, java.lang.StringBuffer)
	 */
	public StringBuffer printExpression(int indent, StringBuffer output) {
		output.append("<CompleteOnJavadocSingleNameReference:"); //$NON-NLS-1$
		if (this.token != null) super.printExpression(indent, output);
		return output.append('>');
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.SingleNameReference#reportError(org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public TypeBinding reportError(BlockScope scope) {
		return null;
	}
}
