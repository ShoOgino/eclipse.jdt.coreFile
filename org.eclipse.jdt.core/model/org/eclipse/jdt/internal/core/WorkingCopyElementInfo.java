/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.ArrayList;

import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.compiler.IProblem;

public class WorkingCopyElementInfo extends CompilationUnitElementInfo implements IProblemRequestor {
	ArrayList problems;
	public void endReporting() {
	}
	public void acceptProblem(IProblem problem) {
		if (this.problems != null) {
			this.problems.add(problem);
		}
	}
	public void beginReporting() {
	}
	public boolean isActive() {
		return true;
	}

}
