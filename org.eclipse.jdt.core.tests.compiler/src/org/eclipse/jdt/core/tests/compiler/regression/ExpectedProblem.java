/*******************************************************************************
 * Copyright (c) 2002 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

public class ExpectedProblem {
	String fileName;
	int id;
	String[] arguments;
public ExpectedProblem(String fileName, int id, String[] arguments) {
	this.fileName = fileName;
	this.id = id;
	this.arguments = arguments;
}
}
