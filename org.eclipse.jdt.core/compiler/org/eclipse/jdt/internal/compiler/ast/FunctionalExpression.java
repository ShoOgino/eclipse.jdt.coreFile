/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
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
 *     Jesper S Moller - Contributions for
 *							bug 382701 - [1.8][compiler] Implement semantic analysis of Lambda expressions & Reference expression
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public abstract class FunctionalExpression extends Expression {
	
	TypeBinding expectedType;

	public FunctionalExpression() {
		super();
	}

	public void setExpectedType(TypeBinding expectedType) {
		this.expectedType = expectedType;
	}

	public TypeBinding expectedType() {
		return this.expectedType;
	}
	
	public TypeBinding resolveType(BlockScope blockScope) {
		this.constant = Constant.NotAConstant;
		MethodBinding singleAbstractMethod = this.expectedType == null ? null : this.expectedType.getSingleAbstractMethod();
		if (singleAbstractMethod == null || !singleAbstractMethod.isValidBinding()) {
			blockScope.problemReporter().targetTypeIsNotAFunctionalInterface(this);
			return null;
		}
		return this.expectedType;
	}

	public int nullStatus(FlowInfo flowInfo) {
		return FlowInfo.NON_NULL;
	}

	public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
		int pc = codeStream.position;
		if (valueRequired) {
			codeStream.aconst_null(); // TODO: Real code
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}
	
}
