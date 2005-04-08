/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API    
 *******************************************************************************/
package org.eclipse.jdt.apt.core.util;

import org.eclipse.jdt.core.dom.ASTNode;

import com.sun.mirror.apt.Messager;

/**
 * Extend the APT {@link Messager} to allow the registrating of diagnostics on ast nodes. 
 */
public interface EclipseMessager extends Messager 
{
	/**
	 * Print an error message on the given AST node. 
	 * The AST node must came from the AST that is associated with the environment.
	 * @param node
	 * @param msg the error message
	 * @throws IllegalArgumentException if <code>node</code> or <code>msg</code> is null.
	 *         Also, if the node did not come from the ast in the environment.
	 */	
	void printError(ASTNode node, String msg);
	
	/**
	 * Print a warning on the given AST node.
	 * The AST node must came from the AST that is associated with the environment.
	 * @param node
	 * @param msg the warning message
	 * @throws IllegalArgumentException if <code>node</code> or <code>msg</code> is null.
	 * 		   Also, if the node did not come from the ast in the environment.
	 */
	void printWarning(ASTNode node, String msg);
	
	/**
	 * Print a notice on the given AST node.
	 * The AST node must came from the AST that is associated with the environment.
	 * @param node
	 * @param msg the warning message
	 * @throws IllegalArgumentException if <code>node</code> or <code>msg</code> is null.
	 *         Also, if the node did not come from the ast in the environment.
	 */
	void printNotice(ASTNode node, String msg);
}
