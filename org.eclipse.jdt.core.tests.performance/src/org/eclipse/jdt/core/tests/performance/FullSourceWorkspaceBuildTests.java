/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.performance;

import java.io.IOException;
import java.util.*;
import junit.framework.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.test.performance.Dimension;


/**
 */
public class FullSourceWorkspaceBuildTests extends FullSourceWorkspaceTests {

	/**
	 * @param name
	 */
	public FullSourceWorkspaceBuildTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildSuite(FullSourceWorkspaceBuildTests.class);
	}

	// No javadoc support
	public void testPerfFullBuildNoComments() throws CoreException, IOException {
		tagAsSummary("Full source workspace build without comment support", Dimension.CPU_TIME);
		Hashtable options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.DISABLED);
		options.put(JavaCore.COMPILER_PB_INVALID_JAVADOC, JavaCore.IGNORE);
		options.put(JavaCore.COMPILER_PB_MISSING_JAVADOC_TAGS, JavaCore.IGNORE);
		options.put(JavaCore.COMPILER_PB_MISSING_JAVADOC_COMMENTS, JavaCore.IGNORE);
		startBuild(options);
	}

	// Set doc comment support
	public void testPerfFullBuild() throws CoreException, IOException {
		tagAsGlobalSummary("Full source workspace build", Dimension.CPU_TIME);
		startBuild(JavaCore.getDefaultOptions());
	}

	// Set invalid javadoc tags
	// TODO (frederic) see if this can be intersting to add this test...
	public void _testPerfWithInvalidJavadocTags() throws CoreException, IOException {
		Hashtable options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_PB_INVALID_JAVADOC, JavaCore.WARNING);
		options.put(JavaCore.COMPILER_PB_INVALID_JAVADOC_TAGS_VISIBILITY, JavaCore.PRIVATE);
		options.put(JavaCore.COMPILER_PB_INVALID_JAVADOC_TAGS, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_PB_INVALID_JAVADOC_TAGS__DEPRECATED_REF, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_PB_INVALID_JAVADOC_TAGS__NOT_VISIBLE_REF, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_PB_MISSING_JAVADOC_TAGS, JavaCore.IGNORE);
		options.put(JavaCore.COMPILER_PB_MISSING_JAVADOC_COMMENTS, JavaCore.IGNORE);
		startBuild(options);
	}

	// Set missing javadoc tags
	// TODO (frederic) see if this can be intersting to add this test...
	public void _testPerfWithMissingJavadocTags() throws CoreException, IOException {
		Hashtable options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_PB_INVALID_JAVADOC, JavaCore.WARNING);
		options.put(JavaCore.COMPILER_PB_INVALID_JAVADOC_TAGS_VISIBILITY, JavaCore.PRIVATE);
		options.put(JavaCore.COMPILER_PB_INVALID_JAVADOC_TAGS, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_PB_INVALID_JAVADOC_TAGS__DEPRECATED_REF, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_PB_INVALID_JAVADOC_TAGS__NOT_VISIBLE_REF, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_PB_MISSING_JAVADOC_TAGS, JavaCore.WARNING);
		options.put(JavaCore.COMPILER_PB_MISSING_JAVADOC_TAGS_VISIBILITY, JavaCore.PRIVATE);
		options.put(JavaCore.COMPILER_PB_MISSING_JAVADOC_TAGS_OVERRIDING, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_PB_MISSING_JAVADOC_COMMENTS, JavaCore.IGNORE);
		startBuild(options);
	}

	// Set missing javadoc comments
	// TODO (frederic) see if this can be intersting to add this test...
	public void _testPerfWithMissingJavadocComments() throws CoreException, IOException {
		Hashtable options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_PB_INVALID_JAVADOC, JavaCore.WARNING);
		options.put(JavaCore.COMPILER_PB_INVALID_JAVADOC_TAGS_VISIBILITY, JavaCore.PRIVATE);
		options.put(JavaCore.COMPILER_PB_INVALID_JAVADOC_TAGS, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_PB_INVALID_JAVADOC_TAGS__DEPRECATED_REF, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_PB_INVALID_JAVADOC_TAGS__NOT_VISIBLE_REF, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_PB_MISSING_JAVADOC_TAGS, JavaCore.WARNING);
		options.put(JavaCore.COMPILER_PB_MISSING_JAVADOC_TAGS_VISIBILITY, JavaCore.PRIVATE);
		options.put(JavaCore.COMPILER_PB_MISSING_JAVADOC_TAGS_OVERRIDING, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_PB_MISSING_JAVADOC_COMMENTS, JavaCore.WARNING);
		options.put(JavaCore.COMPILER_PB_MISSING_JAVADOC_COMMENTS_VISIBILITY, JavaCore.PRIVATE);
		options.put(JavaCore.COMPILER_PB_MISSING_JAVADOC_COMMENTS_OVERRIDING, JavaCore.ENABLED);
		startBuild(options);
	}
}
