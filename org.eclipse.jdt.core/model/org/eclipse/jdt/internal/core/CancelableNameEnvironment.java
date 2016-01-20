/*******************************************************************************
 * Copyright (c) 2004, 2016 IBM Corporation and others.
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
package org.eclipse.jdt.internal.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.codeassist.ISearchRequestor;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;


public class CancelableNameEnvironment extends SearchableEnvironment implements INameEnvironmentWithProgress {
	private IProgressMonitor monitor;

	public CancelableNameEnvironment(JavaProject project, WorkingCopyOwner owner, IProgressMonitor monitor) throws JavaModelException {
		super(project, owner);
		setMonitor(monitor);
	}

	private void checkCanceled() {
		if (this.monitor != null && this.monitor.isCanceled()) {
			if (NameLookup.VERBOSE)
				System.out.println(Thread.currentThread() + " CANCELLING LOOKUP "); //$NON-NLS-1$
			throw new AbortCompilation(true/*silent*/, new OperationCanceledException());
		}
	}

	public void findPackages(char[] prefix, ISearchRequestor requestor) {
		checkCanceled();
		super.findPackages(prefix, requestor);
	}

	public NameEnvironmentAnswer findType(char[] name, char[][] packageName, IModule[] modules) {
		checkCanceled();
		return super.findType(name, packageName, modules);
	}

	public NameEnvironmentAnswer findType(char[][] compoundTypeName, IModule[] modules) {
		checkCanceled();
		return super.findType(compoundTypeName, modules);
	}

	public void findTypes(char[] prefix, boolean findMembers, boolean camelCaseMatch, int searchFor, ISearchRequestor storage, IProgressMonitor progressMonitor) {
		checkCanceled();
		super.findTypes(prefix, findMembers, camelCaseMatch, searchFor, storage, progressMonitor);
	}
	
	public void setMonitor(IProgressMonitor monitor) {
		this.monitor = monitor;
	}
}
