/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;

public class ContainerInitializer extends ClasspathContainerInitializer {
	public static ITestInitializer initializer;

	public static interface ITestInitializer {
		public void initialize(IPath containerPath, IJavaProject project) throws CoreException;
		public boolean allowFailureContainer();
	}

	public static void setInitializer(ITestInitializer initializer) {
		ContainerInitializer.initializer = initializer;
	}

	public IClasspathContainer getFailureContainer(IPath containerPath, IJavaProject project) {
		if (initializer == null || !initializer.allowFailureContainer()) return null;
		return super.getFailureContainer(containerPath, project);
	}

	public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
		if (initializer == null) return;
		initializer.initialize(containerPath, project);
	}
}
