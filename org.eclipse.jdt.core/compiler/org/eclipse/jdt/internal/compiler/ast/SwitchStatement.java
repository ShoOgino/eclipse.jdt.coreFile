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
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class SwitchStatement extends Statement {

	public Expression testExpression;
	public Statement[] statements;
	public BlockScope scope;
	public int explicitDeclarations;
	public Label breakLabel;
	public CaseStatement[] cases;
	public CaseStatement defaultCase;
	public int caseCount = 0;

	// for local variables table attributes
	int preSwitchInitStateIndex = -1;
	int mergedInitStateIndex = -1;

	/**
	 * SwitchStatement constructor comment.
	 */
	public SwitchStatement() {

		super();
	}

	public FlowInfo analyseCode(
			BlockScope currentScope,
			FlowContext flowContext,
			FlowInfo flowInfo) {
			
		flowInfo = testExpression.analyseCode(currentScope, flowContext, flowInfo);
		SwitchFlowContext switchContext =
			new SwitchFlowContext(flowContext, this, (breakLabel = new Label()));

		// analyse the block by considering specially the case/default statements (need to bind them 
		// to the entry point)
		FlowInfo caseInits = FlowInfo.DEAD_END;
		// in case of statements before the first case
		preSwitchInitStateIndex =
			currentScope.methodScope().recordInitializationStates(flowInfo);
		int caseIndex = 0;
		if (statements != null) {
			boolean didAlreadyComplain = false;
			for (int i = 0, max = statements.length; i < max; i++) {
				Statement statement = statements[i];
				if ((caseIndex < caseCount) && (statement == cases[caseIndex])) { // statement is a case
					caseIndex++;
					caseInits = caseInits.mergedWith(flowInfo.copy().unconditionalInits());
					didAlreadyComplain = false; // reset complaint
				} else if (statement == defaultCase) { // statement is the default case
					caseInits = caseInits.mergedWith(flowInfo.copy().unconditionalInits());
					didAlreadyComplain = false; // reset complaint
				}
				if (!statement.complainIfUnreachable(caseInits, scope, didAlreadyComplain)) {
					caseInits = statement.analyseCode(scope, switchContext, caseInits);
				} else {
					didAlreadyComplain = true;
				}
			}
		}

		// if no default case, then record it may jump over the block directly to the end
		if (defaultCase == null) {
			// only retain the potential initializations
			flowInfo.addPotentialInitializationsFrom(
				caseInits.mergedWith(switchContext.initsOnBreak));
			mergedInitStateIndex =
				currentScope.methodScope().recordInitializationStates(flowInfo);
			return flowInfo;
		}

		// merge all branches inits
		FlowInfo mergedInfo = caseInits.mergedWith(switchContext.initsOnBreak);
		mergedInitStateIndex =
			currentScope.methodScope().recordInitializationStates(mergedInfo);
		return mergedInfo;
	}

	/**
	 * Switch code generation
	 *
	 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
	 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
	 */
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {

		int[] sortedIndexes = new int[caseCount];
		int[] localKeysCopy;
		if ((bits & IsReachableMASK) == 0) {
			return;
		}
		int pc = codeStream.position;

		// prepare the labels and constants
		breakLabel.codeStream = codeStream;
		CaseLabel[] caseLabels = new CaseLabel[caseCount];
		int[] constants = new int[caseCount];
		boolean needSwitch = caseCount != 0;
		for (int i = 0; i < caseCount; i++) {
			constants[i] = cases[i].constantExpression.constant.intValue();
			cases[i].targetLabel = (caseLabels[i] = new CaseLabel(codeStream));
		}

		// we sort the keys to be able to generate the code for tableswitch or lookupswitch
		for (int i = 0; i < caseCount; i++) {
			sortedIndexes[i] = i;
		}
		System.arraycopy(
			constants,
			0,
			(localKeysCopy = new int[caseCount]),
			0,
			caseCount);
		CodeStream.sort(localKeysCopy, 0, caseCount - 1, sortedIndexes);
		CaseLabel defaultLabel = new CaseLabel(codeStream);
		if (defaultCase != null) {
			defaultCase.targetLabel = defaultLabel;
		}
		// generate expression testes
		testExpression.generateCode(currentScope, codeStream, needSwitch);

		// generate the appropriate switch table/lookup bytecode
		if (needSwitch) {
			int max = localKeysCopy[caseCount - 1];
			int min = localKeysCopy[0];
			if ((long) (caseCount * 2.5) > ((long) max - (long) min)) {
				
				// work-around 1.3 VM bug, if max>0x7FFF0000, must use lookup bytecode
				// see http://dev.eclipse.org/bugs/show_bug.cgi?id=21557
				if (max > 0x7FFF0000 && currentScope.environment().options.complianceLevel < ClassFileConstants.JDK1_4) {
					codeStream.lookupswitch(defaultLabel, constants, sortedIndexes, caseLabels);

				} else {
					codeStream.tableswitch(
						defaultLabel,
						min,
						max,
						constants,
						sortedIndexes,
						caseLabels);
				}
			} else {
				codeStream.lookupswitch(defaultLabel, constants, sortedIndexes, caseLabels);
			}
			codeStream.updateLastRecordedEndPC(codeStream.position);
		}
		
		// generate the switch block statements
		int caseIndex = 0;
		if (statements != null) {
			for (int i = 0, maxCases = statements.length; i < maxCases; i++) {
				Statement statement = statements[i];
				if ((caseIndex < caseCount)
					&& (statement == cases[caseIndex])) { // statements[i] is a case
					if (preSwitchInitStateIndex != -1) {
						codeStream.removeNotDefinitelyAssignedVariables(
							currentScope,
							preSwitchInitStateIndex);
						caseIndex++;
					}
				} else {
					if (statement == defaultCase) { // statements[i] is a case or a default case
						if (preSwitchInitStateIndex != -1) {
							codeStream.removeNotDefinitelyAssignedVariables(
								currentScope,
								preSwitchInitStateIndex);
						}
					}
				}
				statement.generateCode(scope, codeStream);
			}
		}
		// place the trailing labels (for break and default case)
		breakLabel.place();
		if (defaultCase == null) {
			defaultLabel.place();
		}
		// May loose some local variable initializations : affecting the local variable attributes
		if (mergedInitStateIndex != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(
				currentScope,
				mergedInitStateIndex);
			codeStream.addDefinitelyAssignedVariables(currentScope, mergedInitStateIndex);
		}
		if (scope != currentScope) {
			codeStream.exitUserScope(scope);
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	public StringBuffer printStatement(int indent, StringBuffer output) {

		printIndent(indent, output).append("switch ("); //$NON-NLS-1$
		testExpression.printExpression(0, output).append(") {"); //$NON-NLS-1$
		if (statements != null) {
			for (int i = 0; i < statements.length; i++) {
				output.append('\n');
				if (statements[i] instanceof CaseStatement) {
					statements[i].printStatement(indent, output);
				} else {
					statements[i].printStatement(indent+2, output);
				}
			}
		}
		output.append("\n"); //$NON-NLS-1$
		return printIndent(indent, output).append('}');
	}

	public void resetStateForCodeGeneration() {

		if (this.breakLabel != null) {
			this.breakLabel.resetStateForCodeGeneration();
		}
	}

	public void resolve(BlockScope upperScope) {
	
		TypeBinding testType = testExpression.resolveType(upperScope);
		if (testType == null)
			return;
		testExpression.implicitWidening(testType, testType);
		if (!(testExpression.isConstantValueOfTypeAssignableToType(testType, IntBinding))) {
			if (!testType.isCompatibleWith(IntBinding)) {
				upperScope.problemReporter().incorrectSwitchType(testExpression, testType);
				return;
			}
		}
		if (statements != null) {
			scope = explicitDeclarations == 0 ? upperScope : new BlockScope(upperScope);
			int length;
			// collection of cases is too big but we will only iterate until caseCount
			cases = new CaseStatement[length = statements.length];
			int[] casesValues = new int[length];
			CaseStatement[] duplicateCaseStatements = null;
			int duplicateCaseStatementsCounter = 0;
			int counter = 0;
			for (int i = 0; i < length; i++) {
				Constant cst;
				final Statement statement = statements[i];
				if ((cst = statement.resolveCase(scope, testType, this)) != null) {
					//----check for duplicate case statement------------
					if (cst != NotAConstant) {
						int key = cst.intValue();
						for (int j = 0; j < counter; j++) {
							if (casesValues[j] == key) {
								final CaseStatement currentCaseStatement = (CaseStatement) statement;
								if (duplicateCaseStatements == null) {
									scope.problemReporter().duplicateCase(cases[j]);
									scope.problemReporter().duplicateCase(currentCaseStatement);
									duplicateCaseStatements = new CaseStatement[length];
									duplicateCaseStatements[duplicateCaseStatementsCounter++] = cases[j];
									duplicateCaseStatements[duplicateCaseStatementsCounter++] = currentCaseStatement;
								} else {
									boolean found = false;
									searchReportedDuplicate: for (int k = 2; k < duplicateCaseStatementsCounter; k++) {
										if (duplicateCaseStatements[k] == statement) {
											found = true;
											break searchReportedDuplicate;
										}
									}
									if (!found) {
										scope.problemReporter().duplicateCase(currentCaseStatement);
										duplicateCaseStatements[duplicateCaseStatementsCounter++] = currentCaseStatement;
									}
								}
							}
						}
						casesValues[counter++] = key;
					}
				}
			}
		}
	}

	public void traverse(
			IAbstractSyntaxTreeVisitor visitor,
			BlockScope blockScope) {

		if (visitor.visit(this, blockScope)) {
			testExpression.traverse(visitor, scope);
			if (statements != null) {
				int statementsLength = statements.length;
				for (int i = 0; i < statementsLength; i++)
					statements[i].traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, blockScope);
	}
	
	/**
	 * Dispatch the call on its last statement.
	 */
	public void branchChainTo(Label label) {
		
		// in order to improve debug attributes for stepping (11431)
		// we want to inline the jumps to #breakLabel which already got
		// generated (if any), and have them directly branch to a better
		// location (the argument label).
		// we know at this point that the breakLabel already got placed
		if (this.breakLabel.hasForwardReferences()) {
			label.appendForwardReferencesFrom(this.breakLabel);
		}
	}
}
