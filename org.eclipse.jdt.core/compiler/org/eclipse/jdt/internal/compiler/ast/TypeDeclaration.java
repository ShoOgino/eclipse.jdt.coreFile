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

import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.problem.*;

public class TypeDeclaration
	extends Statement
	implements ProblemSeverities, ReferenceContext {

	public int modifiers;
	public int modifiersSourceStart;
	public char[] name;
	public TypeReference superclass;
	public TypeReference[] superInterfaces;
	public FieldDeclaration[] fields;
	public AbstractMethodDeclaration[] methods;
	public MemberTypeDeclaration[] memberTypes;
	public SourceTypeBinding binding;
	public ClassScope scope;
	public MethodScope initializerScope;
	public MethodScope staticInitializerScope;
	public boolean ignoreFurtherInvestigation = false;
	public int maxFieldCount;
	public int declarationSourceStart;
	public int declarationSourceEnd;
	public int bodyStart;
	public int bodyEnd; // doesn't include the trailing comment if any.
	protected boolean hasBeenGenerated = false;
	public CompilationResult compilationResult;
	private MethodDeclaration[] missingAbstractMethods;
	public Javadoc javadoc;

	public TypeDeclaration(CompilationResult compilationResult){
		this.compilationResult = compilationResult;
	}
		
	/*
	 *	We cause the compilation task to abort to a given extent.
	 */
	public void abort(int abortLevel) {

		switch (abortLevel) {
			case AbortCompilation :
				throw new AbortCompilation(this.compilationResult);
			case AbortCompilationUnit :
				throw new AbortCompilationUnit(this.compilationResult);
			case AbortMethod :
				throw new AbortMethod(this.compilationResult);
			default :
				throw new AbortType(this.compilationResult);
		}
	}
	/**
	 * This method is responsible for adding a <clinit> method declaration to the type method collections.
	 * Note that this implementation is inserting it in first place (as VAJ or javac), and that this
	 * impacts the behavior of the method ConstantPool.resetForClinit(int. int), in so far as 
	 * the latter will have to reset the constant pool state accordingly (if it was added first, it does 
	 * not need to preserve some of the method specific cached entries since this will be the first method).
	 * inserts the clinit method declaration in the first position.
	 * 
	 * @see org.eclipse.jdt.internal.compiler.codegen.ConstantPool#resetForClinit(int, int)
	 */
	public final void addClinit() {

		//see comment on needClassInitMethod
		if (needClassInitMethod()) {
			int length;
			AbstractMethodDeclaration[] methodDeclarations;
			if ((methodDeclarations = this.methods) == null) {
				length = 0;
				methodDeclarations = new AbstractMethodDeclaration[1];
			} else {
				length = methodDeclarations.length;
				System.arraycopy(
					methodDeclarations,
					0,
					(methodDeclarations = new AbstractMethodDeclaration[length + 1]),
					1,
					length);
			}
			Clinit clinit = new Clinit(this.compilationResult);
			methodDeclarations[0] = clinit;
			// clinit is added in first location, so as to minimize the use of ldcw (big consumer of constant inits)
			clinit.declarationSourceStart = clinit.sourceStart = sourceStart;
			clinit.declarationSourceEnd = clinit.sourceEnd = sourceEnd;
			clinit.bodyEnd = sourceEnd;
			this.methods = methodDeclarations;
		}
	}

	/**
	 * INTERNAL USE ONLY - Creates a fake method declaration for the corresponding binding.
	 * It is used to report errors for missing abstract methods.
	 */
	public MethodDeclaration addMissingAbstractMethodFor(MethodBinding methodBinding) {
		TypeBinding[] argumentTypes = methodBinding.parameters;
		int argumentsLength = argumentTypes.length;
		//the constructor
		MethodDeclaration methodDeclaration = new MethodDeclaration(this.compilationResult);
		methodDeclaration.selector = methodBinding.selector;
		methodDeclaration.sourceStart = sourceStart;
		methodDeclaration.sourceEnd = sourceEnd;
		methodDeclaration.modifiers = methodBinding.getAccessFlags() & ~AccAbstract;

		if (argumentsLength > 0) {
			String baseName = "arg";//$NON-NLS-1$
			Argument[] arguments = (methodDeclaration.arguments = new Argument[argumentsLength]);
			for (int i = argumentsLength; --i >= 0;) {
				arguments[i] = new Argument((baseName + i).toCharArray(), 0L, null /*type ref*/, AccDefault);
			}
		}

		//adding the constructor in the methods list
		if (this.missingAbstractMethods == null) {
			this.missingAbstractMethods = new MethodDeclaration[] { methodDeclaration };
		} else {
			MethodDeclaration[] newMethods;
			System.arraycopy(
				this.missingAbstractMethods,
				0,
				newMethods = new MethodDeclaration[this.missingAbstractMethods.length + 1],
				1,
				this.missingAbstractMethods.length);
			newMethods[0] = methodDeclaration;
			this.missingAbstractMethods = newMethods;
		}

		//============BINDING UPDATE==========================
		methodDeclaration.binding = new MethodBinding(
				methodDeclaration.modifiers, //methodDeclaration
				methodBinding.selector,
				methodBinding.returnType,
				argumentsLength == 0 ? NoParameters : argumentTypes, //arguments bindings
				methodBinding.thrownExceptions, //exceptions
				binding); //declaringClass
				
		methodDeclaration.scope = new MethodScope(scope, methodDeclaration, true);
		methodDeclaration.bindArguments();

/*		if (binding.methods == null) {
			binding.methods = new MethodBinding[] { methodDeclaration.binding };
		} else {
			MethodBinding[] newMethods;
			System.arraycopy(
				binding.methods,
				0,
				newMethods = new MethodBinding[binding.methods.length + 1],
				1,
				binding.methods.length);
			newMethods[0] = methodDeclaration.binding;
			binding.methods = newMethods;
		}*/
		//===================================================

		return methodDeclaration;
	}

	/**
	 *	Flow analysis for a local innertype
	 *
	 */
	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		if (ignoreFurtherInvestigation)
			return flowInfo;
		try {
			if (flowInfo.isReachable()) {
				bits |= IsReachableMASK;
				LocalTypeBinding localType = (LocalTypeBinding) binding;
				localType.setConstantPoolName(currentScope.compilationUnitScope().computeConstantPoolName(localType));
			}
			manageEnclosingInstanceAccessIfNecessary(currentScope, flowInfo);
			updateMaxFieldCount(); // propagate down the max field count
			internalAnalyseCode(flowContext, flowInfo); 
		} catch (AbortType e) {
			this.ignoreFurtherInvestigation = true;
		}
		return flowInfo;
	}

	/**
	 *	Flow analysis for a member innertype
	 *
	 */
	public void analyseCode(ClassScope enclosingClassScope) {

		if (ignoreFurtherInvestigation)
			return;
		try {
			// propagate down the max field count
			updateMaxFieldCount();
			internalAnalyseCode(null, FlowInfo.initial(maxFieldCount));
		} catch (AbortType e) {
			this.ignoreFurtherInvestigation = true;
		}
	}

	/**
	 *	Flow analysis for a local member innertype
	 *
	 */
	public void analyseCode(
		ClassScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		if (ignoreFurtherInvestigation)
			return;
		try {
			if (flowInfo.isReachable()) {
				bits |= IsReachableMASK;
				LocalTypeBinding localType = (LocalTypeBinding) binding;
				localType.setConstantPoolName(currentScope.compilationUnitScope().computeConstantPoolName(localType));
			}
			manageEnclosingInstanceAccessIfNecessary(currentScope, flowInfo);
			updateMaxFieldCount(); // propagate down the max field count
			internalAnalyseCode(flowContext, flowInfo);
		} catch (AbortType e) {
			this.ignoreFurtherInvestigation = true;
		}
	}

	/**
	 *	Flow analysis for a package member type
	 *
	 */
	public void analyseCode(CompilationUnitScope unitScope) {

		if (ignoreFurtherInvestigation)
			return;
		try {
			internalAnalyseCode(null, FlowInfo.initial(maxFieldCount));
		} catch (AbortType e) {
			this.ignoreFurtherInvestigation = true;
		}
	}

	/*
	 * Check for constructor vs. method with no return type.
	 * Answers true if at least one constructor is defined
	 */
	public boolean checkConstructors(Parser parser) {

		//if a constructor has not the name of the type,
		//convert it into a method with 'null' as its return type
		boolean hasConstructor = false;
		if (methods != null) {
			for (int i = methods.length; --i >= 0;) {
				AbstractMethodDeclaration am;
				if ((am = methods[i]).isConstructor()) {
					if (!CharOperation.equals(am.selector, name)) {
						// the constructor was in fact a method with no return type
						// unless an explicit constructor call was supplied
						ConstructorDeclaration c = (ConstructorDeclaration) am;
						if (c.constructorCall == null || c.constructorCall.isImplicitSuper()) { //changed to a method
							MethodDeclaration m = parser.convertToMethodDeclaration(c, this.compilationResult);
							methods[i] = m;
						}
					} else {
						if (this.isInterface()) {
							// report the problem and continue the parsing
							parser.problemReporter().interfaceCannotHaveConstructors(
								(ConstructorDeclaration) am);
						}
						hasConstructor = true;
					}
				}
			}
		}
		return hasConstructor;
	}

	public CompilationResult compilationResult() {

		return this.compilationResult;
	}

	public ConstructorDeclaration createsInternalConstructor(
		boolean needExplicitConstructorCall,
		boolean needToInsert) {

		//Add to method'set, the default constuctor that just recall the
		//super constructor with no arguments
		//The arguments' type will be positionned by the TC so just use
		//the default int instead of just null (consistency purpose)

		//the constructor
		ConstructorDeclaration constructor = new ConstructorDeclaration(this.compilationResult);
		constructor.isDefaultConstructor = true;
		constructor.selector = name;
		if (modifiers != AccDefault) {
			constructor.modifiers =
				((this instanceof MemberTypeDeclaration) && (modifiers & AccPrivate) != 0)
					? AccDefault
					: modifiers & AccVisibilityMASK;
		}

		//if you change this setting, please update the 
		//SourceIndexer2.buildTypeDeclaration(TypeDeclaration,char[]) method
		constructor.declarationSourceStart = constructor.sourceStart = sourceStart;
		constructor.declarationSourceEnd =
			constructor.sourceEnd = constructor.bodyEnd = sourceEnd;

		//the super call inside the constructor
		if (needExplicitConstructorCall) {
			constructor.constructorCall = SuperReference.implicitSuperConstructorCall();
			constructor.constructorCall.sourceStart = sourceStart;
			constructor.constructorCall.sourceEnd = sourceEnd;
		}

		//adding the constructor in the methods list
		if (needToInsert) {
			if (methods == null) {
				methods = new AbstractMethodDeclaration[] { constructor };
			} else {
				AbstractMethodDeclaration[] newMethods;
				System.arraycopy(
					methods,
					0,
					newMethods = new AbstractMethodDeclaration[methods.length + 1],
					1,
					methods.length);
				newMethods[0] = constructor;
				methods = newMethods;
			}
		}
		return constructor;
	}
	
	/*
	 * Find the matching parse node, answers null if nothing found
	 */
	public FieldDeclaration declarationOf(FieldBinding fieldBinding) {

		if (fieldBinding != null) {
			for (int i = 0, max = this.fields.length; i < max; i++) {
				FieldDeclaration fieldDecl;
				if ((fieldDecl = this.fields[i]).binding == fieldBinding)
					return fieldDecl;
			}
		}
		return null;
	}

	/*
	 * Find the matching parse node, answers null if nothing found
	 */
	public TypeDeclaration declarationOf(MemberTypeBinding memberTypeBinding) {

		if (memberTypeBinding != null) {
			for (int i = 0, max = this.memberTypes.length; i < max; i++) {
				TypeDeclaration memberTypeDecl;
				if ((memberTypeDecl = this.memberTypes[i]).binding == memberTypeBinding)
					return memberTypeDecl;
			}
		}
		return null;
	}

	/*
	 * Find the matching parse node, answers null if nothing found
	 */
	public AbstractMethodDeclaration declarationOf(MethodBinding methodBinding) {

		if (methodBinding != null) {
			for (int i = 0, max = this.methods.length; i < max; i++) {
				AbstractMethodDeclaration methodDecl;

				if ((methodDecl = this.methods[i]).binding == methodBinding)
					return methodDecl;
			}
		}
		return null;
	}

	/*
	 * Finds the matching type amoung this type's member types.
	 * Returns null if no type with this name is found.
	 * The type name is a compound name relative to this type
	 * eg. if this type is X and we're looking for Y.X.A.B
	 *     then a type name would be {X, A, B}
	 */
	public TypeDeclaration declarationOfType(char[][] typeName) {

		int typeNameLength = typeName.length;
		if (typeNameLength < 1 || !CharOperation.equals(typeName[0], this.name)) {
			return null;
		}
		if (typeNameLength == 1) {
			return this;
		}
		char[][] subTypeName = new char[typeNameLength - 1][];
		System.arraycopy(typeName, 1, subTypeName, 0, typeNameLength - 1);
		for (int i = 0; i < this.memberTypes.length; i++) {
			TypeDeclaration typeDecl = this.memberTypes[i].declarationOfType(subTypeName);
			if (typeDecl != null) {
				return typeDecl;
			}
		}
		return null;
	}

	/**
	 * Generic bytecode generation for type
	 */
	public void generateCode(ClassFile enclosingClassFile) {

		if (hasBeenGenerated)
			return;
		hasBeenGenerated = true;
		if (ignoreFurtherInvestigation) {
			if (binding == null)
				return;
			ClassFile.createProblemType(
				this,
				scope.referenceCompilationUnit().compilationResult);
			return;
		}
		try {
			// create the result for a compiled type
			ClassFile classFile = new ClassFile(binding, enclosingClassFile, false);
			// generate all fiels
			classFile.addFieldInfos();

			// record the inner type inside its own .class file to be able
			// to generate inner classes attributes
			if (binding.isMemberType())
				classFile.recordEnclosingTypeAttributes(binding);
			if (binding.isLocalType()) {
				enclosingClassFile.recordNestedLocalAttribute(binding);
				classFile.recordNestedLocalAttribute(binding);
			}
			if (memberTypes != null) {
				for (int i = 0, max = memberTypes.length; i < max; i++) {
					// record the inner type inside its own .class file to be able
					// to generate inner classes attributes
					classFile.recordNestedMemberAttribute(memberTypes[i].binding);
					memberTypes[i].generateCode(scope, classFile);
				}
			}
			// generate all methods
			classFile.setForMethodInfos();
			if (methods != null) {
				for (int i = 0, max = methods.length; i < max; i++) {
					methods[i].generateCode(scope, classFile);
				}
			}
			
			classFile.generateMissingAbstractMethods(this.missingAbstractMethods, scope.referenceCompilationUnit().compilationResult);

			// generate all methods
			classFile.addSpecialMethods();

			if (ignoreFurtherInvestigation) { // trigger problem type generation for code gen errors
				throw new AbortType(scope.referenceCompilationUnit().compilationResult);
			}

			// finalize the compiled type result
			classFile.addAttributes();
			scope.referenceCompilationUnit().compilationResult.record(
				binding.constantPoolName(),
				classFile);
		} catch (AbortType e) {
			if (binding == null)
				return;
			ClassFile.createProblemType(
				this,
				scope.referenceCompilationUnit().compilationResult);
		}
	}

	/**
	 * Bytecode generation for a local inner type (API as a normal statement code gen)
	 */
	public void generateCode(BlockScope blockScope, CodeStream codeStream) {

		if ((this.bits & IsReachableMASK) == 0) {
			return;
		}		
		if (hasBeenGenerated) return;
		int pc = codeStream.position;
		if (binding != null) ((NestedTypeBinding) binding).computeSyntheticArgumentSlotSizes();
		generateCode(codeStream.classFile);
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	/**
	 * Bytecode generation for a member inner type
	 */
	public void generateCode(ClassScope classScope, ClassFile enclosingClassFile) {

		if (hasBeenGenerated) return;
		if (binding != null) ((NestedTypeBinding) binding).computeSyntheticArgumentSlotSizes();
		generateCode(enclosingClassFile);
	}

	/**
	 * Bytecode generation for a package member
	 */
	public void generateCode(CompilationUnitScope unitScope) {

		generateCode((ClassFile) null);
	}

	public boolean hasErrors() {
		return this.ignoreFurtherInvestigation;
	}

	/**
	 *	Common flow analysis for all types
	 *
	 */
	public void internalAnalyseCode(FlowContext flowContext, FlowInfo flowInfo) {

		if (this.binding.isPrivate() && !this.binding.isPrivateUsed()) {
			if (!scope.referenceCompilationUnit().compilationResult.hasSyntaxError()) {
				scope.problemReporter().unusedPrivateType(this);
			}
		}

		ReferenceBinding[] defaultHandledExceptions = new ReferenceBinding[] { scope.getJavaLangThrowable()}; // tolerate any kind of exception
		InitializationFlowContext initializerContext = new InitializationFlowContext(null, this, initializerScope);
		InitializationFlowContext staticInitializerContext = new InitializationFlowContext(null, this, staticInitializerScope);
		FlowInfo nonStaticFieldInfo = flowInfo.copy().unconditionalInits().discardFieldInitializations();
		FlowInfo staticFieldInfo = flowInfo.copy().unconditionalInits().discardFieldInitializations();
		if (fields != null) {
			for (int i = 0, count = fields.length; i < count; i++) {
				FieldDeclaration field = fields[i];
				if (field.isStatic()) {
					/*if (field.isField()){
						staticInitializerContext.handledExceptions = NoExceptions; // no exception is allowed jls8.3.2
					} else {*/
					staticInitializerContext.handledExceptions = defaultHandledExceptions; // tolerate them all, and record them
					/*}*/
					staticFieldInfo =
						field.analyseCode(
							staticInitializerScope,
							staticInitializerContext,
							staticFieldInfo);
					// in case the initializer is not reachable, use a reinitialized flowInfo and enter a fake reachable
					// branch, since the previous initializer already got the blame.
					if (staticFieldInfo == FlowInfo.DEAD_END) {
						staticInitializerScope.problemReporter().initializerMustCompleteNormally(field);
						staticFieldInfo = FlowInfo.initial(maxFieldCount).setReachMode(FlowInfo.UNREACHABLE);
					}
				} else {
					/*if (field.isField()){
						initializerContext.handledExceptions = NoExceptions; // no exception is allowed jls8.3.2
					} else {*/
						initializerContext.handledExceptions = defaultHandledExceptions; // tolerate them all, and record them
					/*}*/
					nonStaticFieldInfo =
						field.analyseCode(initializerScope, initializerContext, nonStaticFieldInfo);
					// in case the initializer is not reachable, use a reinitialized flowInfo and enter a fake reachable
					// branch, since the previous initializer already got the blame.
					if (nonStaticFieldInfo == FlowInfo.DEAD_END) {
						initializerScope.problemReporter().initializerMustCompleteNormally(field);
						nonStaticFieldInfo = FlowInfo.initial(maxFieldCount).setReachMode(FlowInfo.UNREACHABLE);
					}
				}
			}
		}
		if (memberTypes != null) {
			for (int i = 0, count = memberTypes.length; i < count; i++) {
				if (flowContext != null){ // local type
					memberTypes[i].analyseCode(scope, flowContext, nonStaticFieldInfo.copy());
				} else {
					memberTypes[i].analyseCode(scope);
				}
			}
		}
		if (methods != null) {
			UnconditionalFlowInfo outerInfo = flowInfo.copy().unconditionalInits().discardFieldInitializations();
			FlowInfo constructorInfo = nonStaticFieldInfo.unconditionalInits().discardNonFieldInitializations().addInitializationsFrom(outerInfo);
			for (int i = 0, count = methods.length; i < count; i++) {
				AbstractMethodDeclaration method = methods[i];
				if (method.ignoreFurtherInvestigation)
					continue;
				if (method.isInitializationMethod()) {
					if (method.isStatic()) { // <clinit>
						method.analyseCode(
							scope, 
							staticInitializerContext, 
							staticFieldInfo.unconditionalInits().discardNonFieldInitializations().addInitializationsFrom(outerInfo));
					} else { // constructor
						method.analyseCode(scope, initializerContext, constructorInfo.copy());
					}
				} else { // regular method
					method.analyseCode(scope, null, flowInfo.copy());
				}
			}
		}
	}

	public boolean isInterface() {

		return (modifiers & AccInterface) != 0;
	}

	/* 
	 * Access emulation for a local type
	 * force to emulation of access to direct enclosing instance.
	 * By using the initializer scope, we actually only request an argument emulation, the
	 * field is not added until actually used. However we will force allocations to be qualified
	 * with an enclosing instance.
	 * 15.9.2
	 */
	public void manageEnclosingInstanceAccessIfNecessary(BlockScope currentScope, FlowInfo flowInfo) {

		if (!flowInfo.isReachable()) return;
		NestedTypeBinding nestedType = (NestedTypeBinding) binding;
		
		MethodScope methodScope = currentScope.methodScope();
		if (!methodScope.isStatic && !methodScope.isConstructorCall){

			nestedType.addSyntheticArgumentAndField(binding.enclosingType());	
		}
		// add superclass enclosing instance arg for anonymous types (if necessary)
		if (binding.isAnonymousType()) { 
			ReferenceBinding superclassBinding = binding.superclass;
			if (superclassBinding.enclosingType() != null && !superclassBinding.isStatic()) {
				if (!superclassBinding.isLocalType()
						|| ((NestedTypeBinding)superclassBinding).getSyntheticField(superclassBinding.enclosingType(), true) != null){

					nestedType.addSyntheticArgument(superclassBinding.enclosingType());	
				}
			}
		}
	}
	
	/* 
	 * Access emulation for a local member type
	 * force to emulation of access to direct enclosing instance.
	 * By using the initializer scope, we actually only request an argument emulation, the
	 * field is not added until actually used. However we will force allocations to be qualified
	 * with an enclosing instance.
	 * 
	 * Local member cannot be static.
	 */
	public void manageEnclosingInstanceAccessIfNecessary(ClassScope currentScope, FlowInfo flowInfo) {

		if (!flowInfo.isReachable()) return;
		NestedTypeBinding nestedType = (NestedTypeBinding) binding;
		nestedType.addSyntheticArgumentAndField(binding.enclosingType());
	}	
	
	/**
	 * A <clinit> will be requested as soon as static fields or assertions are present. It will be eliminated during
	 * classfile creation if no bytecode was actually produced based on some optimizations/compiler settings.
	 */
	public final boolean needClassInitMethod() {

		// always need a <clinit> when assertions are present
		if ((this.bits & AddAssertionMASK) != 0)
			return true;
		if (fields == null)
			return false;
		if (isInterface())
			return true; // fields are implicitly statics
		for (int i = fields.length; --i >= 0;) {
			FieldDeclaration field = fields[i];
			//need to test the modifier directly while there is no binding yet
			if ((field.modifiers & AccStatic) != 0)
				return true;
		}
		return false;
	}

	public void parseMethod(Parser parser, CompilationUnitDeclaration unit) {

		//connect method bodies
		if (unit.ignoreMethodBodies)
			return;

		// no scope were created, so cannot report further errors
//		if (binding == null)
//			return;

		//members
		if (memberTypes != null) {
			int length = memberTypes.length;
			for (int i = 0; i < length; i++)
				memberTypes[i].parseMethod(parser, unit);
		}

		//methods
		if (methods != null) {
			int length = methods.length;
			for (int i = 0; i < length; i++)
				methods[i].parseStatements(parser, unit);
		}

		//initializers
		if (fields != null) {
			int length = fields.length;
			for (int i = 0; i < length; i++) {
				if (fields[i] instanceof Initializer) {
					((Initializer) fields[i]).parseStatements(parser, this, unit);
				}
			}
		}
	}

	public StringBuffer print(int tab, StringBuffer output) {

		printIndent(tab, output);
		printHeader(0, output);
		return printBody(tab, output);
	}

	public StringBuffer printBody(int indent, StringBuffer output) {

		output.append(" {"); //$NON-NLS-1$
		if (memberTypes != null) {
			for (int i = 0; i < memberTypes.length; i++) {
				if (memberTypes[i] != null) {
					output.append('\n');
					memberTypes[i].print(indent + 1, output);
				}
			}
		}
		if (fields != null) {
			for (int fieldI = 0; fieldI < fields.length; fieldI++) {
				if (fields[fieldI] != null) {
					output.append('\n');
					fields[fieldI].print(indent + 1, output);
				}
			}
		}
		if (methods != null) {
			for (int i = 0; i < methods.length; i++) {
				if (methods[i] != null) {
					output.append('\n');
					methods[i].print(indent + 1, output); 
				}
			}
		}
		output.append('\n');
		return printIndent(indent, output).append('}');
	}

	public StringBuffer printHeader(int indent, StringBuffer output) {

		printModifiers(this.modifiers, output);
		output.append(isInterface() ? "interface " : "class "); //$NON-NLS-1$ //$NON-NLS-2$
		output.append(name);
		if (superclass != null) {
			output.append(" extends ");  //$NON-NLS-1$
			superclass.print(0, output);
		}
		if (superInterfaces != null && superInterfaces.length > 0) {
			output.append(isInterface() ? " extends " : " implements ");//$NON-NLS-2$ //$NON-NLS-1$
			for (int i = 0; i < superInterfaces.length; i++) {
				if (i > 0) output.append( ", "); //$NON-NLS-1$
				superInterfaces[i].print(0, output);
			}
		}
		return output;
	}

	public StringBuffer printStatement(int tab, StringBuffer output) {
		return print(tab, output);
	}

	public void resolve() {

		if (binding == null) {
			ignoreFurtherInvestigation = true;
			return;
		}
		try {
			if ((this.bits & UndocumentedEmptyBlockMASK) != 0) {
				this.scope.problemReporter().undocumentedEmptyBlock(this.bodyStart-1, this.bodyEnd+1);
			}
			// check superclass & interfaces
			if (binding.superclass != null) // watch out for Object ! (and other roots)	
				if (isTypeUseDeprecated(binding.superclass, scope))
					scope.problemReporter().deprecatedType(binding.superclass, superclass);
			if (superInterfaces != null)
				for (int i = superInterfaces.length; --i >= 0;)
					if (superInterfaces[i].resolvedType != null)
						if (isTypeUseDeprecated(superInterfaces[i].resolvedType, scope))
							scope.problemReporter().deprecatedType(
								superInterfaces[i].resolvedType,
								superInterfaces[i]);
			maxFieldCount = 0;
			int lastFieldID = -1;
			if (fields != null) {
				for (int i = 0, count = fields.length; i < count; i++) {
					FieldDeclaration field = fields[i];
					if (field.isField()) {
						if (field.binding == null) {
							// still discover secondary errors
							if (field.initialization != null) field.initialization.resolve(field.isStatic() ? staticInitializerScope : initializerScope);
							ignoreFurtherInvestigation = true;
							continue;
						}
						maxFieldCount++;
						lastFieldID = field.binding.id;
					} else { // initializer
						 ((Initializer) field).lastFieldID = lastFieldID + 1;
					}
					field.resolve(field.isStatic() ? staticInitializerScope : initializerScope);
				}
			}
			if (memberTypes != null) {
				for (int i = 0, count = memberTypes.length; i < count; i++) {
					memberTypes[i].resolve(scope);
				}
			}
			int missingAbstractMethodslength = this.missingAbstractMethods == null ? 0 : this.missingAbstractMethods.length;
			int methodsLength = this.methods == null ? 0 : methods.length;
			if ((methodsLength + missingAbstractMethodslength) > 0xFFFF) {
				scope.problemReporter().tooManyMethods(this);
			}
			
			if (methods != null) {
				for (int i = 0, count = methods.length; i < count; i++) {
					methods[i].resolve(scope);
				}
			}
			// Resolve javadoc
			if (this.javadoc != null) {
				if (this.scope != null) {
					this.javadoc.resolve(this.scope);
				}
			} else if ((this.binding != null) && this.binding.isPublic()) {
				this.scope.problemReporter().javadocMissing(this.sourceStart, this.sourceEnd);
			}
			
		} catch (AbortType e) {
			this.ignoreFurtherInvestigation = true;
			return;
		}
	}

	public void resolve(BlockScope blockScope) {
		// local type declaration

		// need to build its scope first and proceed with binding's creation
		blockScope.addLocalType(this);

		// and TC....
		if (binding != null) {
			// remember local types binding for innerclass emulation propagation
			blockScope.referenceCompilationUnit().record((LocalTypeBinding)binding);

			// binding is not set if the receiver could not be created
			resolve();
			updateMaxFieldCount();
		}
	}

	public void resolve(ClassScope upperScope) {
		// member scopes are already created
		// request the construction of a binding if local member type

		if (binding != null && binding instanceof LocalTypeBinding) {
			// remember local types binding for innerclass emulation propagation
			upperScope.referenceCompilationUnit().record((LocalTypeBinding)binding);
		}
		resolve();
		updateMaxFieldCount();
	}

	public void resolve(CompilationUnitScope upperScope) {
		// top level : scope are already created

		resolve();
		updateMaxFieldCount();
	}

	public void tagAsHavingErrors() {
		ignoreFurtherInvestigation = true;
	}


	/**
	 *	Iteration for a package member type
	 *
	 */
	public void traverse(
		IAbstractSyntaxTreeVisitor visitor,
		CompilationUnitScope unitScope) {

		if (ignoreFurtherInvestigation)
			return;
		try {
			if (visitor.visit(this, unitScope)) {
				if (superclass != null)
					superclass.traverse(visitor, scope);
				if (superInterfaces != null) {
					int superInterfaceLength = superInterfaces.length;
					for (int i = 0; i < superInterfaceLength; i++)
						superInterfaces[i].traverse(visitor, scope);
				}
				if (memberTypes != null) {
					int memberTypesLength = memberTypes.length;
					for (int i = 0; i < memberTypesLength; i++)
						memberTypes[i].traverse(visitor, scope);
				}
				if (fields != null) {
					int fieldsLength = fields.length;
					for (int i = 0; i < fieldsLength; i++) {
						FieldDeclaration field;
						if ((field = fields[i]).isStatic()) {
							field.traverse(visitor, staticInitializerScope);
						} else {
							field.traverse(visitor, initializerScope);
						}
					}
				}
				if (methods != null) {
					int methodsLength = methods.length;
					for (int i = 0; i < methodsLength; i++)
						methods[i].traverse(visitor, scope);
				}
			}
			visitor.endVisit(this, unitScope);
		} catch (AbortType e) {
			// silent abort
		}
	}

	/**
	 * MaxFieldCount's computation is necessary so as to reserve space for
	 * the flow info field portions. It corresponds to the maximum amount of
	 * fields this class or one of its innertypes have.
	 *
	 * During name resolution, types are traversed, and the max field count is recorded
	 * on the outermost type. It is then propagated down during the flow analysis.
	 *
	 * This method is doing either up/down propagation.
	 */
	void updateMaxFieldCount() {

		if (binding == null)
			return; // error scenario
		TypeDeclaration outerMostType = scope.outerMostClassScope().referenceType();
		if (maxFieldCount > outerMostType.maxFieldCount) {
			outerMostType.maxFieldCount = maxFieldCount; // up
		} else {
			maxFieldCount = outerMostType.maxFieldCount; // down
		}
	}
}
