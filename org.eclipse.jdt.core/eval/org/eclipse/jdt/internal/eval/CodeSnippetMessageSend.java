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
package org.eclipse.jdt.internal.eval;

import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BindingIds;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;

public class CodeSnippetMessageSend extends MessageSend implements ProblemReasons, EvaluationConstants {
	EvaluationContext evaluationContext;
	FieldBinding delegateThis;
/**
 * CodeSnippetMessageSend constructor comment.
 */
public CodeSnippetMessageSend(EvaluationContext evaluationContext) {
	this.evaluationContext = evaluationContext;
}
/**
 * MessageSend code generation
 *
 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
 * @param valueRequired boolean
 */
public void generateCode(
	BlockScope currentScope,
	CodeStream codeStream,
	boolean valueRequired) {

	int pc = codeStream.position;

	if (this.binding.canBeSeenBy(this.receiverType, this, currentScope)) {
		// generate receiver/enclosing instance access
		boolean isStatic = this.binding.isStatic();
		// outer access ?
		if (!isStatic && ((this.bits & DepthMASK) != 0)) {
			// outer method can be reached through emulation
			ReferenceBinding targetType = currentScope.enclosingSourceType().enclosingTypeAt((this.bits & DepthMASK) >> DepthSHIFT);
			Object[] path = currentScope.getEmulationPath(targetType, true /*only exact match*/, false/*consider enclosing arg*/);
			if (path == null) {
				// emulation was not possible (should not happen per construction)
				currentScope.problemReporter().needImplementation();
			} else {
				codeStream.generateOuterAccess(path, this, targetType, currentScope);
			}
		} else {
			this.receiver.generateCode(currentScope, codeStream, !isStatic);
		}
		// generate arguments
		if (this.arguments != null) {
			for (int i = 0, max = this.arguments.length; i < max; i++) {
				this.arguments[i].generateCode(currentScope, codeStream, true);
			}
		}
		// actual message invocation
		if (isStatic) {
			codeStream.invokestatic(this.binding);
		} else {
			if (this.receiver.isSuper()) {
				codeStream.invokespecial(this.binding);
			} else {
				if (this.binding.declaringClass.isInterface()) {
					codeStream.invokeinterface(this.binding);
				} else {
					codeStream.invokevirtual(this.binding);
				}
			}
		}
	} else {
		((CodeSnippetCodeStream) codeStream).generateEmulationForMethod(currentScope, this.binding);
		// generate receiver/enclosing instance access
		boolean isStatic = this.binding.isStatic();
		// outer access ?
		if (!isStatic && ((this.bits & DepthMASK) != 0)) {
			// not supported yet
			currentScope.problemReporter().needImplementation();
		} else {
			this.receiver.generateCode(currentScope, codeStream, !isStatic);
		}
		if (isStatic) {
			// we need an object on the stack which is ignored for the method invocation
			codeStream.aconst_null();
		}
		// generate arguments
		if (this.arguments != null) {
			int argsLength = this.arguments.length;
			codeStream.generateInlinedValue(argsLength);
			codeStream.newArray(currentScope, new ArrayBinding(currentScope.getType(TypeConstants.JAVA_LANG_OBJECT), 1));
			codeStream.dup();
			for (int i = 0; i < argsLength; i++) {
				codeStream.generateInlinedValue(i);
				this.arguments[i].generateCode(currentScope, codeStream, true);
				TypeBinding parameterBinding = this.binding.parameters[i];
				if (parameterBinding.isBaseType() && parameterBinding != NullBinding) {
					((CodeSnippetCodeStream)codeStream).generateObjectWrapperForType(this.binding.parameters[i]);
				}
				codeStream.aastore();
				if (i < argsLength - 1) {
					codeStream.dup();
				}	
			}
		} else {
			codeStream.generateInlinedValue(0);
			codeStream.newArray(currentScope, new ArrayBinding(currentScope.getType(TypeConstants.JAVA_LANG_OBJECT), 1));			
		}
		((CodeSnippetCodeStream) codeStream).invokeJavaLangReflectMethodInvoke();

		// convert the return value to the appropriate type for primitive types
		if (this.binding.returnType.isBaseType()) {
			int typeID = this.binding.returnType.id;
			if (typeID == T_void) {
				// remove the null from the stack
				codeStream.pop();
			}
			((CodeSnippetCodeStream) codeStream).checkcast(typeID);
			((CodeSnippetCodeStream) codeStream).getBaseTypeValue(typeID);
		} else {
			codeStream.checkcast(this.binding.returnType);
		}
	}
	// operation on the returned value
	if (valueRequired) {
		// implicit conversion if necessary
		codeStream.generateImplicitConversion(this.implicitConversion);
	} else {
		// pop return value if any
		switch (this.binding.returnType.id) {
			case T_long :
			case T_double :
				codeStream.pop2();
				break;
			case T_void :
				break;
			default :
				codeStream.pop();
		}
	}
	codeStream.recordPositionsFrom(pc, this.sourceStart);
}
public void manageSyntheticAccessIfNecessary(BlockScope currentScope, FlowInfo flowInfo) {

	if (!flowInfo.isReachable()) return;
	
	// if the binding declaring class is not visible, need special action
	// for runtime compatibility on 1.2 VMs : change the declaring class of the binding
	// NOTE: from target 1.2 on, method's declaring class is touched if any different from receiver type
	// and not from Object or implicit static method call.	
	if (this.binding.declaringClass != this.qualifyingType
		&& !this.qualifyingType.isArrayType()
		&& ((currentScope.environment().options.targetJDK >= ClassFileConstants.JDK1_2
				&& (!this.receiver.isImplicitThis() || !this.binding.isStatic())
				&& this.binding.declaringClass.id != T_Object) // no change for Object methods
			|| !this.binding.declaringClass.canBeSeenBy(currentScope))) {
		this.codegenBinding = currentScope.enclosingSourceType().getUpdatedMethodBinding(this.binding, (ReferenceBinding) this.qualifyingType);
	}	
}
public TypeBinding resolveType(BlockScope scope) {
	// Answer the signature return type
	// Base type promotion

	this.constant = NotAConstant;
	this.qualifyingType = this.receiverType = this.receiver.resolveType(scope); 
	// will check for null after args are resolved
	TypeBinding[] argumentTypes = NoParameters;
	if (this.arguments != null) {
		boolean argHasError = false; // typeChecks all arguments 
		int length = this.arguments.length;
		argumentTypes = new TypeBinding[length];
		for (int i = 0; i < length; i++)
			if ((argumentTypes[i] = this.arguments[i].resolveType(scope)) == null)
				argHasError = true;
		if (argHasError)
			return null;
	}
	if (this.receiverType == null) 
		return null;

	// base type cannot receive any message
	if (this.receiverType.isBaseType()) {
		scope.problemReporter().errorNoMethodFor(this, this.receiverType, argumentTypes);
		return null;
	}

	this.binding = 
		this.receiver.isImplicitThis()
			? scope.getImplicitMethod(this.selector, argumentTypes, this)
			: scope.getMethod(this.receiverType, this.selector, argumentTypes, this); 
	if (!this.binding.isValidBinding()) {
		if (this.binding instanceof ProblemMethodBinding
			&& ((ProblemMethodBinding) this.binding).problemId() == NotVisible) {
			if (this.evaluationContext.declaringTypeName != null) {
				this.delegateThis = scope.getField(scope.enclosingSourceType(), DELEGATE_THIS, this);
				if (this.delegateThis == null){ // if not found then internal error, field should have been found
					this.constant = NotAConstant;
					scope.problemReporter().invalidMethod(this, this.binding);
					return null;
				}
			} else {
				this.constant = NotAConstant;
				scope.problemReporter().invalidMethod(this, this.binding);
				return null;
			}
			CodeSnippetScope localScope = new CodeSnippetScope(scope);			
			MethodBinding privateBinding = 
				this.receiver instanceof CodeSnippetThisReference && ((CodeSnippetThisReference) this.receiver).isImplicit
					? localScope.getImplicitMethod((ReferenceBinding)this.delegateThis.type, this.selector, argumentTypes, this)
					: localScope.getMethod(this.delegateThis.type, this.selector, argumentTypes, this); 
			if (!privateBinding.isValidBinding()) {
				if (this.binding.declaringClass == null) {
					if (this.receiverType instanceof ReferenceBinding) {
						this.binding.declaringClass = (ReferenceBinding) this.receiverType;
					} else { // really bad error ....
						scope.problemReporter().errorNoMethodFor(this, this.receiverType, argumentTypes);
						return null;
					}
				}
				scope.problemReporter().invalidMethod(this, this.binding);
				return null;
			} else {
				this.binding = privateBinding;
			}
		} else {
			if (this.binding.declaringClass == null) {
				if (this.receiverType instanceof ReferenceBinding) {
					this.binding.declaringClass = (ReferenceBinding) this.receiverType;
				} else { // really bad error ....
					scope.problemReporter().errorNoMethodFor(this, this.receiverType, argumentTypes);
					return null;
				}
			}
			scope.problemReporter().invalidMethod(this, this.binding);
			return null;
		}
	}
	if (!this.binding.isStatic()) {
		// the "receiver" must not be a type, in other words, a NameReference that the TC has bound to a Type
		if (this.receiver instanceof NameReference) {
			if ((((NameReference) this.receiver).bits & BindingIds.TYPE) != 0) {
				scope.problemReporter().mustUseAStaticMethod(this, this.binding);
				return null;
			}
		}
	}
	if (this.arguments != null)
		for (int i = 0; i < this.arguments.length; i++)
			this.arguments[i].implicitWidening(this.binding.parameters[i], argumentTypes[i]);

	//-------message send that are known to fail at compile time-----------
	if (this.binding.isAbstract()) {
		if (this.receiver.isSuper()) {
			scope.problemReporter().cannotDireclyInvokeAbstractMethod(this, this.binding);
			return null;
		}
		// abstract private methods cannot occur nor abstract static............
	}
	if (isMethodUseDeprecated(this.binding, scope))
		scope.problemReporter().deprecatedMethod(this.binding, this);

	return this.resolvedType = this.binding.returnType;
}
}
