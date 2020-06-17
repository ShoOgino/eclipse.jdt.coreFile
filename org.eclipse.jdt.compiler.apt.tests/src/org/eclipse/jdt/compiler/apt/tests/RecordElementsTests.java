/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

import junit.framework.TestCase;

public class RecordElementsTests extends TestCase {
	private static final String MODULE_PROC = "org.eclipse.jdt.compiler.apt.tests.processors.elements.RecordElementProcessor";

	public void testPreviewFlagTrue() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "15", "testPreviewFlagTrue", null, "records", true);
	}
	public void testRecords1() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "15", "testRecords1", null, "records", true);
	}
	public void testRecords1Javac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "15", "testRecords1", null, "records", true);
	}
	public void testRecords2() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "15", "testRecords2", null, "records", true);
	}
	public void testRecords2Javac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "15", "testRecords2", null, "records", true);
	}
	public void testRecords3() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "15", "testRecords3", null, "records", true);
	}
	public void testRecords3Javac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "15", "testRecords3", null, "records", true);
	}
	public void testRecords3a() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "15", "testRecords3a", null, "records", true);
	}
	public void testRecords3aJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "15", "testRecords3a", null, "records", true);
	}
	public void testRecords4() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "15", "testRecords4", null, "records", true);
	}
	public void testRecords4Javac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "15", "testRecords4", null, "records", true);
	}
	public void testRecords4a() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "15", "testRecords4a", null, "records", true);
	}
	public void testRecords4aJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "15", "testRecords4a", null, "records", true);
	}
	public void testRecords5() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "15", "testRecords5", null, "records", true);
	}
	public void testRecords5Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "15", "testRecords5", null, "records", true);
	}
	public void testRecords5a() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "15", "testRecords5a", null, "records", true);
	}
	public void testRecords5aJavac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "15", "testRecords5a", null, "records", true);
	}
	public void testRecords6() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "15", "testRecords6", null, "records", true);
	}
	public void testRecords6Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "15", "testRecords6", null, "records", true);
	}
	public void testRecords7() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "15", "testRecords7", null, "records", true);
	}
	public void testRecords7Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "15", "testRecords7", null, "records", true);
	}
	public void testRecords8() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "15", "testRecords8", null, "records", true);
	}
	public void testRecords8Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "15", "testRecords8", null, "records", true);
	}
	public void testRecords9() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "15", "testRecords9", null, "records", true);
	}
	public void testRecords9Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "15", "testRecords9", null, "records", true);
	}

	protected void internalTestWithPreview(JavaCompiler compiler, String processor, String compliance,
			String testMethod, String testClass, String resourceArea, boolean preview) throws IOException {
		if (!isRunning15()) {
			return;
		}
		System.clearProperty(processor);
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "mod_locations", resourceArea);
		if (testClass == null || testClass.equals("")) {
			BatchTestUtils.copyResources("mod_locations/" + resourceArea, targetFolder);
		} else {
			BatchTestUtils.copyResource("mod_locations/" + resourceArea + "/" + testClass, targetFolder);
		}

		List<String> options = new ArrayList<String>();
		options.add("-A" + processor);
		options.add("-A" + testMethod);
		options.add("-processor");
		options.add(processor);
		if (compiler instanceof EclipseCompiler) {
			options.add("-" + compliance);
		} else {
			options.add("-source");
			options.add(compliance);
		}
		if (preview)
			options.add("--enable-preview");
		BatchTestUtils.compileInModuleMode(compiler, options, processor, targetFolder, null, true, false);
		// If it succeeded, the processor will have set this property to "succeeded";
		// if not, it will set it to an error value.
		assertEquals("succeeded", System.getProperty(processor));
	}
	public boolean isRunning15() {
		String specVersion = System.getProperty("java.specification.version");
		return CompilerOptions.VERSION_15.equals(specVersion);
	}
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		BatchTestUtils.init();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

}