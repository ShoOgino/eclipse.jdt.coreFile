/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package targets.inherited;

import java.awt.Point;

import org.eclipse.jdt.compiler.apt.tests.annotations.ArgsConstructor;

@ArgsConstructor({ Point.class })
public class TestGeneric<K extends Point> {

	public TestGeneric(K k) {
	}
}
