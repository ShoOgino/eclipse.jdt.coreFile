/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * 
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
 */

package org.eclipse.jdt.internal.codeassist.select;

import org.eclipse.jdt.internal.compiler.ast.ReferenceExpression;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class SelectionOnReferenceExpression extends ReferenceExpression {
	
	public SelectionOnReferenceExpression(ReferenceExpression referenceExpression) {
		initialize(referenceExpression.compilationResult, referenceExpression.lhs, referenceExpression.typeArguments, referenceExpression.selector, referenceExpression.sourceEnd);
	}
	
	@Override
	public TypeBinding resolveType(BlockScope scope) {
		TypeBinding resolveType = super.resolveType(scope);
		if (this.expectedType != null && !this.trialResolution)
			throw new SelectionNodeFound(this.descriptor);
		return resolveType;
	}
}