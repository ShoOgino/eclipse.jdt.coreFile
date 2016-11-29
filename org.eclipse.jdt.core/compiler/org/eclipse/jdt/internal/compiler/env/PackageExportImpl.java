/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
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
package org.eclipse.jdt.internal.compiler.env;

public class PackageExportImpl implements IModule.IPackageExport {
	public char[] pack;
	public char[][] exportedTo;
	@Override
	public char[] name() {
		return this.pack;
	}

	@Override
	public char[][] exportedTo() {
		return this.exportedTo;
	}
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(this.pack);
		if (this.exportedTo != null) {
			for (char[] cs : this.exportedTo) {
				buffer.append(cs);
			}
		}
		buffer.append(';');
		return buffer.toString();
	}
}