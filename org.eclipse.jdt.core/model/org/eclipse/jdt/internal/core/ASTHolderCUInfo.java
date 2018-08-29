/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.CompilationUnit;

@SuppressWarnings("rawtypes")
public class ASTHolderCUInfo extends CompilationUnitElementInfo {
	int astLevel;
	boolean resolveBindings;
	int reconcileFlags;
	HashMap problems = null;
	CompilationUnit ast;
}
