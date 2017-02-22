/*******************************************************************************
 * Copyright (c) 2016, 2017 IBM Corporation and others.
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
 *     
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist.select;

import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.ModuleDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;

public class SelectionOnModuleDeclaration extends ModuleDeclaration {

	public SelectionOnModuleDeclaration(CompilationResult compilationResult, char[][] tokens, long[] positions) {
		super(compilationResult, tokens, positions);
	}
	public void resolve(ClassScope scope) {
		super.resolve(scope);
		throw new SelectionNodeFound(this.moduleBinding);
	}

}
