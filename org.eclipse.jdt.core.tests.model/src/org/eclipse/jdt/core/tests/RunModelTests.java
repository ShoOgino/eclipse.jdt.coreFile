/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests;

import org.eclipse.jdt.core.tests.model.AllJavaModelTestsJUnit3;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Runs all Java model tests.
 */
public class RunModelTests extends TestCase {
public RunModelTests(String name) {
	super(name);
}
public static Test suite() {
	TestSuite suite = new TestSuite(RunModelTests.class.getName());
	suite.addTest(AllJavaModelTestsJUnit3.suite());
	return suite;
}
}

