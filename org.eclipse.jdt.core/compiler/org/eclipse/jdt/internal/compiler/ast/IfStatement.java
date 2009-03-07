/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class IfStatement extends Statement {

	//this class represents the case of only one statement in
	//either else and/or then branches.

	public Expression condition;
	public Statement thenStatement;
	public Statement elseStatement;

	// for local variables table attributes
	int thenInitStateIndex = -1;
	int elseInitStateIndex = -1;
	int mergedInitStateIndex = -1;

public IfStatement(Expression condition, Statement thenStatement, 	int sourceStart, int sourceEnd) {
	this.condition = condition;
	this.thenStatement = thenStatement;
	// remember useful empty statement
	if (thenStatement instanceof EmptyStatement) thenStatement.bits |= IsUsefulEmptyStatement;
	this.sourceStart = sourceStart;
	this.sourceEnd = sourceEnd;
}

public IfStatement(Expression condition, Statement thenStatement, Statement elseStatement, int sourceStart, int sourceEnd) {
	this.condition = condition;
	this.thenStatement = thenStatement;
	// remember useful empty statement
	if (thenStatement instanceof EmptyStatement) thenStatement.bits |= IsUsefulEmptyStatement;
	this.elseStatement = elseStatement;
	if (elseStatement instanceof IfStatement) elseStatement.bits |= IsElseIfStatement;
	if (elseStatement instanceof EmptyStatement) elseStatement.bits |= IsUsefulEmptyStatement;
	this.sourceStart = sourceStart;
	this.sourceEnd = sourceEnd;
}

public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	// process the condition
	FlowInfo conditionFlowInfo = this.condition.analyseCode(currentScope, flowContext, flowInfo);
	int initialComplaintLevel = (flowInfo.reachMode() & FlowInfo.UNREACHABLE) != 0 ? Statement.COMPLAINED_FAKE_REACHABLE : Statement.NOT_COMPLAINED;

	Constant cst = this.condition.optimizedBooleanConstant();
	boolean isConditionOptimizedTrue = cst != Constant.NotAConstant && cst.booleanValue() == true;
	boolean isConditionOptimizedFalse = cst != Constant.NotAConstant && cst.booleanValue() == false;

	// process the THEN part
	FlowInfo thenFlowInfo = conditionFlowInfo.safeInitsWhenTrue();
	if (isConditionOptimizedFalse) {
		thenFlowInfo.setReachMode(FlowInfo.UNREACHABLE);
	}
	FlowInfo elseFlowInfo = conditionFlowInfo.initsWhenFalse();
	if (isConditionOptimizedTrue) {
		elseFlowInfo.setReachMode(FlowInfo.UNREACHABLE);
	}
	if (this.thenStatement != null) {
		// Save info for code gen
		this.thenInitStateIndex = currentScope.methodScope().recordInitializationStates(thenFlowInfo);
		if (isConditionOptimizedFalse) {
			if (!isKnowDeadCodePattern(this.condition) || currentScope.compilerOptions().reportDeadCodeInTrivialIfStatement) {
				this.thenStatement.complainIfUnreachable(thenFlowInfo, currentScope, initialComplaintLevel);
			}
		}
		thenFlowInfo = this.thenStatement.analyseCode(currentScope, flowContext, thenFlowInfo);
	}
	// code gen: optimizing the jump around the ELSE part
	if ((thenFlowInfo.tagBits & FlowInfo.UNREACHABLE) != 0) {
		this.bits |= ASTNode.ThenExit;
	}

	// process the ELSE part
	if (this.elseStatement != null) {
	    // signal else clause unnecessarily nested, tolerate else-if code pattern
	    if (thenFlowInfo == FlowInfo.DEAD_END
	            && (this.bits & IsElseIfStatement) == 0 	// else of an else-if
	            && !(this.elseStatement instanceof IfStatement)) {
	        currentScope.problemReporter().unnecessaryElse(this.elseStatement);
	    }
		// Save info for code gen
		this.elseInitStateIndex = currentScope.methodScope().recordInitializationStates(elseFlowInfo);
		if (isConditionOptimizedTrue) {
			if (!isKnowDeadCodePattern(this.condition) || currentScope.compilerOptions().reportDeadCodeInTrivialIfStatement) {
				this.elseStatement.complainIfUnreachable(elseFlowInfo, currentScope, initialComplaintLevel);
			}
		}
		elseFlowInfo = this.elseStatement.analyseCode(currentScope, flowContext, elseFlowInfo);
	}
	// merge THEN & ELSE initializations
	FlowInfo mergedInfo = FlowInfo.mergedOptimizedBranches(
		thenFlowInfo,
		isConditionOptimizedTrue,
		elseFlowInfo,
		isConditionOptimizedFalse,
		true /*if(true){ return; }  fake-reachable(); */);
	this.mergedInitStateIndex = currentScope.methodScope().recordInitializationStates(mergedInfo);
	return mergedInfo;
}

/**
 * If code generation
 *
 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
 */
public void generateCode(BlockScope currentScope, CodeStream codeStream) {
	if ((this.bits & IsReachable) == 0) {
		return;
	}
	int pc = codeStream.position;
	BranchLabel endifLabel = new BranchLabel(codeStream);

	// optimizing the then/else part code gen
	Constant cst;
	boolean hasThenPart =
		!(((cst = this.condition.optimizedBooleanConstant()) != Constant.NotAConstant
				&& cst.booleanValue() == false)
			|| this.thenStatement == null
			|| this.thenStatement.isEmptyBlock());
	boolean hasElsePart =
		!((cst != Constant.NotAConstant && cst.booleanValue() == true)
			|| this.elseStatement == null
			|| this.elseStatement.isEmptyBlock());
	if (hasThenPart) {
		BranchLabel falseLabel = null;
		// generate boolean condition
		this.condition.generateOptimizedBoolean(
			currentScope,
			codeStream,
			null,
			hasElsePart ? (falseLabel = new BranchLabel(codeStream)) : endifLabel,
			true/*cst == Constant.NotAConstant*/);
		// May loose some local variable initializations : affecting the local variable attributes
		if (this.thenInitStateIndex != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.thenInitStateIndex);
			codeStream.addDefinitelyAssignedVariables(currentScope, this.thenInitStateIndex);
		}
		// generate then statement
		this.thenStatement.generateCode(currentScope, codeStream);
		// jump around the else statement
		if (hasElsePart) {
			if ((this.bits & ASTNode.ThenExit) == 0) {
				this.thenStatement.branchChainTo(endifLabel);
				int position = codeStream.position;
				codeStream.goto_(endifLabel);
				//goto is tagged as part of the thenAction block
				codeStream.updateLastRecordedEndPC((this.thenStatement instanceof Block) ? ((Block) this.thenStatement).scope : currentScope, position);
				// generate else statement
			}
			// May loose some local variable initializations : affecting the local variable attributes
			if (this.elseInitStateIndex != -1) {
				codeStream.removeNotDefinitelyAssignedVariables(
					currentScope,
					this.elseInitStateIndex);
				codeStream.addDefinitelyAssignedVariables(currentScope, this.elseInitStateIndex);
			}
			if (falseLabel != null) falseLabel.place();
			this.elseStatement.generateCode(currentScope, codeStream);
		}
	} else if (hasElsePart) {
		// generate boolean condition
		this.condition.generateOptimizedBoolean(
			currentScope,
			codeStream,
			endifLabel,
			null,
			true/*cst == Constant.NotAConstant*/);
		// generate else statement
		// May loose some local variable initializations : affecting the local variable attributes
		if (this.elseInitStateIndex != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(
				currentScope,
				this.elseInitStateIndex);
			codeStream.addDefinitelyAssignedVariables(currentScope, this.elseInitStateIndex);
		}
		this.elseStatement.generateCode(currentScope, codeStream);
	} else {
		// generate condition side-effects
		this.condition.generateCode(currentScope, codeStream, false);
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}
	// May loose some local variable initializations : affecting the local variable attributes
	if (this.mergedInitStateIndex != -1) {
		codeStream.removeNotDefinitelyAssignedVariables(
			currentScope,
			this.mergedInitStateIndex);
		codeStream.addDefinitelyAssignedVariables(currentScope, this.mergedInitStateIndex);
	}
	endifLabel.place();
	codeStream.recordPositionsFrom(pc, this.sourceStart);
}

/**
 * Answers true if the if is identified as a known coding pattern which
 * should be tolerated by dead code analysis.
 * e.g. if (DEBUG) print(); // no complaint
 * Only invoked when overall condition is known to be optimizeable into false.
 */
public static boolean isKnowDeadCodePattern(Expression expression) {
	// if (!DEBUG) print(); - tolerated
	if (expression instanceof UnaryExpression) {
		expression = ((UnaryExpression) expression).expression;
	}
	// if (DEBUG) print(); - tolerated
	if (expression instanceof Reference) return true;

//	if (expression instanceof BinaryExpression) {
//		BinaryExpression binary = (BinaryExpression) expression;
//		switch ((binary.bits & ASTNode.OperatorMASK) >> ASTNode.OperatorSHIFT/* operator */) {
//			case OperatorIds.AND_AND :
//			case OperatorIds.OR_OR :
//				break;
//			default: 
//				// if (DEBUG_LEVEL > 0) print(); - tolerated
//				if ((binary.left instanceof Reference) && binary.right.constant != Constant.NotAConstant)
//					return true;
//				// if (0 < DEBUG_LEVEL) print(); - tolerated
//				if ((binary.right instanceof Reference) && binary.left.constant != Constant.NotAConstant)
//					return true;
//		}
//	}
	return false;
}

public StringBuffer printStatement(int indent, StringBuffer output) {
	printIndent(indent, output).append("if ("); //$NON-NLS-1$
	this.condition.printExpression(0, output).append(")\n");	//$NON-NLS-1$
	this.thenStatement.printStatement(indent + 2, output);
	if (this.elseStatement != null) {
		output.append('\n');
		printIndent(indent, output);
		output.append("else\n"); //$NON-NLS-1$
		this.elseStatement.printStatement(indent + 2, output);
	}
	return output;
}

public void resolve(BlockScope scope) {
	TypeBinding type = this.condition.resolveTypeExpecting(scope, TypeBinding.BOOLEAN);
	this.condition.computeConversion(scope, type, type);
	if (this.thenStatement != null)
		this.thenStatement.resolve(scope);
	if (this.elseStatement != null)
		this.elseStatement.resolve(scope);
}

public void traverse(ASTVisitor visitor, BlockScope blockScope) {
	if (visitor.visit(this, blockScope)) {
		this.condition.traverse(visitor, blockScope);
		if (this.thenStatement != null)
			this.thenStatement.traverse(visitor, blockScope);
		if (this.elseStatement != null)
			this.elseStatement.traverse(visitor, blockScope);
	}
	visitor.endVisit(this, blockScope);
}
}
