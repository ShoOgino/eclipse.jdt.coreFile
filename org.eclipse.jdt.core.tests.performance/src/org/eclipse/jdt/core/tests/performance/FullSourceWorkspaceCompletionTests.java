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
package org.eclipse.jdt.core.tests.performance;

import java.io.PrintStream;
import java.text.NumberFormat;
import junit.framework.*;

import org.eclipse.jdt.core.*;
import org.eclipse.test.performance.Dimension;


/**
 */
public class FullSourceWorkspaceCompletionTests extends FullSourceWorkspaceTests {
	private static final int WARMUP_COUNT = 10;
	private static final int ITERATION_COUNT = 40;

	// Log files
	private static int TESTS_COUNT = 0;
	private static PrintStream[] LOG_STREAMS = new PrintStream[4];

	public FullSourceWorkspaceCompletionTests(String name) {
		super(name);
	}

	public static Test suite() {
		Test suite = buildSuite(testClass());
		TESTS_COUNT = suite.countTestCases();
		createPrintStream(testClass().getName(), LOG_STREAMS, TESTS_COUNT, "Complete");
		return suite;
	}

	private static Class testClass() {
		return FullSourceWorkspaceCompletionTests.class;
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {

		// End of execution => one test less
        TESTS_COUNT--;

        // Log perf result
        if (LOG_DIR != null) {
            logPerfResult(LOG_STREAMS, TESTS_COUNT);
        }
		
		// Call super at the end as it close print streams
		super.tearDown();
	}

	/*
	private void waitUntilIndexesReady() {
		if (DEBUG) System.out.print("Wait until indexes ready...");
		// dummy query for waiting until the indexes are ready
		SearchEngine engine = new SearchEngine();
		IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
		try {
			engine.searchAllTypeNames(
				null,
				"!@$#!@".toCharArray(),
				SearchPattern.R_PATTERN_MATCH | SearchPattern.R_CASE_SENSITIVE,
				IJavaSearchConstants.CLASS,
				scope, 
				new ITypeNameRequestor() {
					public void acceptClass(
							char[] packageName,
							char[] simpleTypeName,
							char[][] enclosingTypeNames,
							String path){}
					public void acceptInterface(
							char[] packageName,
							char[] simpleTypeName,
							char[][] enclosingTypeNames,
							String path){}
				},
				IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
				null);
		} catch (CoreException e) {
		}
		if (DEBUG) System.out.println("done!");
	}
	*/
	
	private class TestCompletionRequestor extends CompletionRequestor {
		public int proposalCount = 0;
		public void accept(CompletionProposal proposal) {
			this.proposalCount++;
		}
	}

	private void complete(
			String testName,
			String projectName,
			String packageName,
			String unitName,
			String completeAt,
			String completeBehind,
			int warmupCount,
			int iterationCount) throws JavaModelException {
		this.complete(
				testName,
				projectName,
				packageName,
				unitName,
				completeAt,
				completeBehind,
				null,
				warmupCount,
				iterationCount);
	}
	private void complete(
			String testName,
			String projectName,
			String packageName,
			String unitName,
			String completeAt,
			String completeBehind,
			int[] ignoredKinds,
			int warmupCount,
			int iterationCount) throws JavaModelException {
		
		tagAsSummary(testName, Dimension.CPU_TIME, false/* do NOT put in fingerprint*/);
		
		waitUntilIndexesReady();
		
		TestCompletionRequestor requestor = new TestCompletionRequestor();
		if(ignoredKinds != null) {
			for (int i = 0; i < ignoredKinds.length; i++) {
				requestor.setIgnored(ignoredKinds[i], true);
			}
		}
		
		ICompilationUnit unit =
			getCompilationUnit(projectName, packageName, unitName);
		
		String str = unit.getSource();
		int completionIndex = str.indexOf(completeAt) + completeBehind.length();
		
		if (DEBUG) System.out.print("Perform code assist inside " + unitName + "...");
		
		// Warm up
		if(warmupCount > 0) {
			unit.codeComplete(completionIndex, requestor);
			for (int i = 1; i < warmupCount; i++) {
				unit.codeComplete(completionIndex, requestor);
			}
		}
		
		// Measure loops
		for (int i=0; i<MEASURES_COUNT; i++) {
			startMeasuring();
			for (int j = 0; j < iterationCount; j++) {
				unit.codeComplete(completionIndex, requestor);
			}
			stopMeasuring();
		}
		if (DEBUG) System.out.println("done!");
		
		// Commit measure
		commitMeasurements();
		assertPerformance();
		
		// Print statistics
		System.out.println("-------------------------------------");
		System.out.println("Completion performance test statistics:");
		NumberFormat intFormat = NumberFormat.getIntegerInstance();
		System.out.println("  - "+intFormat.format(iterationCount)+" completions have been performed");
		System.out.println("  - "+intFormat.format(requestor.proposalCount)+" proposals have been proposed");
		System.out.println("-------------------------------------\n");
	}
	
	public void testPerfCompleteMethodDeclaration() throws JavaModelException {
		this.complete(
				"Completion>Method>Declaration",
				"org.eclipse.jdt.core",
				"org.eclipse.jdt.internal.core",
				"SourceType.java",
				"IType {",
				"IType {",
				WARMUP_COUNT,
				ITERATION_COUNT);
	}
	public void testPerfCompleteMemberAccess() throws JavaModelException {
		this.complete(
				"Completion>Member>Access",
				"org.eclipse.jdt.core",
				"org.eclipse.jdt.internal.core",
				"SourceType.java",
				"this.",
				"this.",
				WARMUP_COUNT,
				ITERATION_COUNT);
	}
	public void testPerfCompleteTypeReference() throws JavaModelException {
		this.complete(
				"Completion>Type>Reference",
				"org.eclipse.jdt.core",
				"org.eclipse.jdt.internal.core",
				"SourceType.java",
				"ArrayList list",
				"A",
				WARMUP_COUNT,
				ITERATION_COUNT);
	}
	public void testPerfCompleteEmptyName() throws JavaModelException {
		this.complete(
				"Completion>Name>Empty",
				"org.eclipse.jdt.core",
				"org.eclipse.jdt.internal.core",
				"SourceType.java",
				"params.add",
				"",
				WARMUP_COUNT,
				ITERATION_COUNT);
	}
	public void testPerfCompleteName() throws JavaModelException {
		this.complete(
				"Completion>Name",
				"org.eclipse.jdt.core",
				"org.eclipse.jdt.internal.core",
				"SourceType.java",
				"params.add",
				"p",
				WARMUP_COUNT,
				ITERATION_COUNT);
	}
	public void testPerfCompleteEmptyNameWithoutTypes() throws JavaModelException {
		this.complete(
				"Completion>Name>Empty>No Type",
				"org.eclipse.jdt.core",
				"org.eclipse.jdt.internal.core",
				"SourceType.java",
				"params.add",
				"",
				new int[]{CompletionProposal.TYPE_REF},
				WARMUP_COUNT,
				ITERATION_COUNT);
	}
	public void testPerfCompleteNameWithoutTypes() throws JavaModelException {
		this.complete(
				"Completion>Name>No Type",
				"org.eclipse.jdt.core",
				"org.eclipse.jdt.internal.core",
				"SourceType.java",
				"params.add",
				"p",
				new int[]{CompletionProposal.TYPE_REF},
				WARMUP_COUNT,
				ITERATION_COUNT);
	}
	public void testPerfCompleteEmptyNameWithoutMethods() throws JavaModelException {
		this.complete(
				"Completion>Name>Empty>No Method",
				"org.eclipse.jdt.core",
				"org.eclipse.jdt.internal.core",
				"SourceType.java",
				"params.add",
				"",
				new int[]{CompletionProposal.METHOD_REF},
				WARMUP_COUNT,
				ITERATION_COUNT);
	}
	public void testPerfCompleteNameWithoutMethods() throws JavaModelException {
		this.complete(
				"Completion>Name>No Method",
				"org.eclipse.jdt.core",
				"org.eclipse.jdt.internal.core",
				"SourceType.java",
				"params.add",
				"p",
				new int[]{CompletionProposal.METHOD_REF},
				WARMUP_COUNT,
				ITERATION_COUNT);
	}
}
