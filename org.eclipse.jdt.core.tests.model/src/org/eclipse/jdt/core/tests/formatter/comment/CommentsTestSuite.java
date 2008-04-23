/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.formatter.comment;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @since 3.0
 */
public class CommentsTestSuite extends TestSuite {

	public static Test suite() {
		return new CommentsTestSuite();
	}
	
	// TODO (frederic) Fix the failures on SingleLineTestCase when new formatter is enabled...
	public CommentsTestSuite() {
		addTest(MultiLineTestCase.suite());
		String newFormatter = System.getProperty("org.eclipse.jdt.core.formatter.comments.new");
		if (newFormatter == null) {
			addTest(SingleLineTestCase.suite());
		} else {
			System.out.println("SingleLineTestCase temporarily disabled as new formatter is enabled!");
		}
		addTest(JavaDocTestCase.suite());
	}
}
