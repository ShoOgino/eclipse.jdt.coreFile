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
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

public class BlockScope extends Scope {

	// Local variable management
	public LocalVariableBinding[] locals;
	public int localIndex; // position for next variable
	public int startIndex;	// start position in this scope - for ordering scopes vs. variables
	public int offset; // for variable allocation throughout scopes
	public int maxOffset; // for variable allocation throughout scopes

	// finally scopes must be shifted behind respective try&catch scope(s) so as to avoid
	// collisions of secret variables (return address, save value).
	public BlockScope[] shiftScopes; 

	public final static VariableBinding[] EmulationPathToImplicitThis = {};
	public final static VariableBinding[] NoEnclosingInstanceInConstructorCall = {};
	public final static VariableBinding[] NoEnclosingInstanceInStaticContext = {};

	public Scope[] subscopes = new Scope[1]; // need access from code assist
	public int scopeIndex = 0; // need access from code assist

	protected BlockScope(int kind, Scope parent) {

		super(kind, parent);
	}

	public BlockScope(BlockScope parent) {

		this(parent, true);
	}

	public BlockScope(BlockScope parent, boolean addToParentScope) {

		this(BLOCK_SCOPE, parent);
		locals = new LocalVariableBinding[5];
		if (addToParentScope) parent.addSubscope(this);
		this.startIndex = parent.localIndex;
	}

	public BlockScope(BlockScope parent, int variableCount) {

		this(BLOCK_SCOPE, parent);
		locals = new LocalVariableBinding[variableCount];
		parent.addSubscope(this);
		this.startIndex = parent.localIndex;
	}

	/* Create the class scope & binding for the anonymous type.
	 */
	public final void addAnonymousType(
		TypeDeclaration anonymousType,
		ReferenceBinding superBinding) {

		ClassScope anonymousClassScope = new ClassScope(this, anonymousType);
		anonymousClassScope.buildAnonymousTypeBinding(
			enclosingSourceType(),
			superBinding);
	}

	/* Create the class scope & binding for the local type.
	 */
	public final void addLocalType(TypeDeclaration localType) {

		// check that the localType does not conflict with an enclosing type
		ReferenceBinding type = enclosingSourceType();
		do {
			if (CharOperation.equals(type.sourceName, localType.name)) {
				problemReporter().hidingEnclosingType(localType);
				return;
			}
			type = type.enclosingType();
		} while (type != null);

		// check that the localType does not conflict with another sibling local type
		Scope scope = this;
		do {
			if (((BlockScope) scope).findLocalType(localType.name) != null) {
				problemReporter().duplicateNestedType(localType);
				return;
			}
		} while ((scope = scope.parent) instanceof BlockScope);

		ClassScope localTypeScope = new ClassScope(this, localType);
		addSubscope(localTypeScope);
		localTypeScope.buildLocalTypeBinding(enclosingSourceType());
	}

	/* Insert a local variable into a given scope, updating its position
	 * and checking there are not too many locals or arguments allocated.
	 */
	public final void addLocalVariable(LocalVariableBinding binding) {

		checkAndSetModifiersForVariable(binding);

		// insert local in scope
		if (localIndex == locals.length)
			System.arraycopy(
				locals,
				0,
				(locals = new LocalVariableBinding[localIndex * 2]),
				0,
				localIndex);
		locals[localIndex++] = binding;

		// update local variable binding 
		binding.declaringScope = this;
		binding.id = this.outerMostMethodScope().analysisIndex++;
		// share the outermost method scope analysisIndex
	}

	public void addSubscope(Scope childScope) {
		if (scopeIndex == subscopes.length)
			System.arraycopy(
				subscopes,
				0,
				(subscopes = new Scope[scopeIndex * 2]),
				0,
				scopeIndex);
		subscopes[scopeIndex++] = childScope;
	}

	/* Answer true if the receiver is suitable for assigning final blank fields.
	 *
	 * in other words, it is inside an initializer, a constructor or a clinit 
	 */
	public final boolean allowBlankFinalFieldAssignment(FieldBinding binding) {

		if (enclosingSourceType() != binding.declaringClass)
			return false;

		MethodScope methodScope = methodScope();
		if (methodScope.isStatic != binding.isStatic())
			return false;
		return methodScope.isInsideInitializer() // inside initializer
				|| ((AbstractMethodDeclaration) methodScope.referenceContext)
					.isInitializationMethod(); // inside constructor or clinit
	}
	String basicToString(int tab) {
		String newLine = "\n"; //$NON-NLS-1$
		for (int i = tab; --i >= 0;)
			newLine += "\t"; //$NON-NLS-1$

		String s = newLine + "--- Block Scope ---"; //$NON-NLS-1$
		newLine += "\t"; //$NON-NLS-1$
		s += newLine + "locals:"; //$NON-NLS-1$
		for (int i = 0; i < localIndex; i++)
			s += newLine + "\t" + locals[i].toString(); //$NON-NLS-1$
		s += newLine + "startIndex = " + startIndex; //$NON-NLS-1$
		return s;
	}

	private void checkAndSetModifiersForVariable(LocalVariableBinding varBinding) {

		int modifiers = varBinding.modifiers;
		if ((modifiers & AccAlternateModifierProblem) != 0 && varBinding.declaration != null){
			problemReporter().duplicateModifierForVariable(varBinding.declaration, this instanceof MethodScope);
		}
		int realModifiers = modifiers & AccJustFlag;
		
		int unexpectedModifiers = ~AccFinal;
		if ((realModifiers & unexpectedModifiers) != 0 && varBinding.declaration != null){ 
			problemReporter().illegalModifierForVariable(varBinding.declaration, this instanceof MethodScope);
		}
		varBinding.modifiers = modifiers;
	}

	/* Compute variable positions in scopes given an initial position offset
	 * ignoring unused local variables.
	 * 
	 * No argument is expected here (ilocal is the first non-argument local of the outermost scope)
	 * Arguments are managed by the MethodScope method
	 */
	void computeLocalVariablePositions(int ilocal, int initOffset, CodeStream codeStream) {

		this.offset = initOffset;
		this.maxOffset = initOffset;

		// local variable init
		int maxLocals = this.localIndex;
		boolean hasMoreVariables = ilocal < maxLocals;

		// scope init
		int iscope = 0, maxScopes = this.scopeIndex;
		boolean hasMoreScopes = maxScopes > 0;

		// iterate scopes and variables in parallel
		while (hasMoreVariables || hasMoreScopes) {
			if (hasMoreScopes
				&& (!hasMoreVariables || (subscopes[iscope].startIndex() <= ilocal))) {
				// consider subscope first
				if (subscopes[iscope] instanceof BlockScope) {
					BlockScope subscope = (BlockScope) subscopes[iscope];
					int subOffset = subscope.shiftScopes == null ? this.offset : subscope.maxShiftedOffset();
					subscope.computeLocalVariablePositions(0, subOffset, codeStream);
					if (subscope.maxOffset > this.maxOffset)
						this.maxOffset = subscope.maxOffset;
				}
				hasMoreScopes = ++iscope < maxScopes;
			} else {
				
				// consider variable first
				LocalVariableBinding local = locals[ilocal]; // if no local at all, will be locals[ilocal]==null
				
				// check if variable is actually used, and may force it to be preserved
				boolean generateCurrentLocalVar = (local.useFlag == LocalVariableBinding.USED && (local.constant == Constant.NotAConstant));
					
				// do not report fake used variable
				if (local.useFlag == LocalVariableBinding.UNUSED
					&& (local.declaration != null) // unused (and non secret) local
					&& ((local.declaration.bits & AstNode.IsLocalDeclarationReachableMASK) != 0)) { // declaration is reachable
						
					if (!(local.declaration instanceof Argument))  // do not report unused catch arguments
						this.problemReporter().unusedLocalVariable(local.declaration);
				}
				
				// could be optimized out, but does need to preserve unread variables ?
				if (!generateCurrentLocalVar) {
					if (local.declaration != null && environment().options.preserveAllLocalVariables) {
						generateCurrentLocalVar = true; // force it to be preserved in the generated code
						local.useFlag = LocalVariableBinding.USED;
					}
				}
				
				// allocate variable
				if (generateCurrentLocalVar) {

					if (local.declaration != null) {
						codeStream.record(local); // record user-defined local variables for attribute generation
					}
					// assign variable position
					local.resolvedPosition = this.offset;

					if ((local.type == LongBinding) || (local.type == DoubleBinding)) {
						this.offset += 2;
					} else {
						this.offset++;
					}
					if (this.offset > 0xFFFF) { // no more than 65535 words of locals
						this.problemReporter().noMoreAvailableSpaceForLocal(
							local, 
							local.declaration == null ? (AstNode)this.methodScope().referenceContext : local.declaration);
					}
				} else {
					local.resolvedPosition = -1; // not generated
				}
				hasMoreVariables = ++ilocal < maxLocals;
			}
		}
		if (this.offset > this.maxOffset)
			this.maxOffset = this.offset;
	}

	/* Answer true if the variable name already exists within the receiver's scope.
	 */
	public final LocalVariableBinding duplicateName(char[] name) {
		for (int i = 0; i < localIndex; i++)
			if (CharOperation.equals(name, locals[i].name))
				return locals[i];

		if (this instanceof MethodScope)
			return null;
		else
			return ((BlockScope) parent).duplicateName(name);
	}

	/*
	 *	Record the suitable binding denoting a synthetic field or constructor argument,
	 * mapping to the actual outer local variable in the scope context.
	 * Note that this may not need any effect, in case the outer local variable does not
	 * need to be emulated and can directly be used as is (using its back pointer to its
	 * declaring scope).
	 */
	public void emulateOuterAccess(LocalVariableBinding outerLocalVariable) {

		MethodScope currentMethodScope;
		if ((currentMethodScope = this.methodScope())
			!= outerLocalVariable.declaringScope.methodScope()) {
			NestedTypeBinding currentType = (NestedTypeBinding) this.enclosingSourceType();

			//do nothing for member types, pre emulation was performed already
			if (!currentType.isLocalType()) {
				return;
			}
			// must also add a synthetic field if we're not inside a constructor
			if (!currentMethodScope.isInsideInitializerOrConstructor()) {
				currentType.addSyntheticArgumentAndField(outerLocalVariable);
			} else {
				currentType.addSyntheticArgument(outerLocalVariable);
			}
		}
	}

	/* Note that it must never produce a direct access to the targetEnclosingType,
	 * but instead a field sequence (this$2.this$1.this$0) so as to handle such a test case:
	 *
	 * class XX {
	 *	void foo() {
	 *		class A {
	 *			class B {
	 *				class C {
	 *					boolean foo() {
	 *						return (Object) A.this == (Object) B.this;
	 *					}
	 *				}
	 *			}
	 *		}
	 *		new A().new B().new C();
	 *	}
	 * }
	 * where we only want to deal with ONE enclosing instance for C (could not figure out an A for C)
	 */
	public final ReferenceBinding findLocalType(char[] name) {

		for (int i = 0, length = scopeIndex; i < length; i++) {
			if (subscopes[i] instanceof ClassScope) {
				SourceTypeBinding sourceType =
					((ClassScope) subscopes[i]).referenceContext.binding;
				if (CharOperation.equals(sourceType.sourceName(), name))
					return sourceType;
			}
		}
		return null;
	}

	public LocalVariableBinding findVariable(char[] variable) {

		int variableLength = variable.length;
		for (int i = 0, length = locals.length; i < length; i++) {
			LocalVariableBinding local = locals[i];
			if (local == null)
				return null;
			if (local.name.length == variableLength
				&& CharOperation.prefixEquals(local.name, variable))
				return local;
		}
		return null;
	}
	/* API
     * flag is a mask of the following values VARIABLE (= FIELD or LOCAL), TYPE.
	 * Only bindings corresponding to the mask will be answered.
	 *
	 *	if the VARIABLE mask is set then
	 *		If the first name provided is a field (or local) then the field (or local) is answered
	 *		Otherwise, package names and type names are consumed until a field is found.
	 *		In this case, the field is answered.
	 *
	 *	if the TYPE mask is set,
	 *		package names and type names are consumed until the end of the input.
	 *		Only if all of the input is consumed is the type answered
	 *
	 *	All other conditions are errors, and a problem binding is returned.
	 *	
	 *	NOTE: If a problem binding is returned, senders should extract the compound name
	 *	from the binding & not assume the problem applies to the entire compoundName.
	 *
	 *	The VARIABLE mask has precedence over the TYPE mask.
	 *
	 *	InvocationSite implements
	 *		isSuperAccess(); this is used to determine if the discovered field is visible.
	 *		setFieldIndex(int); this is used to record the number of names that were consumed.
	 *
	 *	For example, getBinding({"foo","y","q", VARIABLE, site) will answer
	 *	the binding for the field or local named "foo" (or an error binding if none exists).
	 *	In addition, setFieldIndex(1) will be sent to the invocation site.
	 *	If a type named "foo" exists, it will not be detected (and an error binding will be answered)
	 *
	 *	IMPORTANT NOTE: This method is written under the assumption that compoundName is longer than length 1.
	 */
	public Binding getBinding(char[][] compoundName, int mask, InvocationSite invocationSite) {

		Binding binding = getBinding(compoundName[0], mask | TYPE | PACKAGE, invocationSite);
		invocationSite.setFieldIndex(1);
		if (binding instanceof VariableBinding) return binding;
		compilationUnitScope().recordSimpleReference(compoundName[0]);
		if (!binding.isValidBinding()) return binding;

		int length = compoundName.length;
		int currentIndex = 1;
		foundType : if (binding instanceof PackageBinding) {
			PackageBinding packageBinding = (PackageBinding) binding;
			while (currentIndex < length) {
				compilationUnitScope().recordReference(packageBinding.compoundName, compoundName[currentIndex]);
				binding = packageBinding.getTypeOrPackage(compoundName[currentIndex++]);
				invocationSite.setFieldIndex(currentIndex);
				if (binding == null) {
					if (currentIndex == length)
						// must be a type if its the last name, otherwise we have no idea if its a package or type
						return new ProblemReferenceBinding(
							CharOperation.subarray(compoundName, 0, currentIndex),
							NotFound);
					else
						return new ProblemBinding(
							CharOperation.subarray(compoundName, 0, currentIndex),
							NotFound);
				}
				if (binding instanceof ReferenceBinding) {
					if (!binding.isValidBinding())
						return new ProblemReferenceBinding(
							CharOperation.subarray(compoundName, 0, currentIndex),
							binding.problemId());
					if (!((ReferenceBinding) binding).canBeSeenBy(this))
						return new ProblemReferenceBinding(
							CharOperation.subarray(compoundName, 0, currentIndex),
							binding,
							NotVisible);
					break foundType;
				}
				packageBinding = (PackageBinding) binding;
			}

			// It is illegal to request a PACKAGE from this method.
			return new ProblemReferenceBinding(
				CharOperation.subarray(compoundName, 0, currentIndex),
				NotFound);
		}

		// know binding is now a ReferenceBinding
		while (currentIndex < length) {
			ReferenceBinding typeBinding = (ReferenceBinding) binding;
			char[] nextName = compoundName[currentIndex++];
			invocationSite.setFieldIndex(currentIndex);
			invocationSite.setActualReceiverType(typeBinding);
			if ((mask & FIELD) != 0 && (binding = findField(typeBinding, nextName, invocationSite)) != null) {
				if (!binding.isValidBinding())
					return new ProblemFieldBinding(
						((FieldBinding) binding).declaringClass,
						CharOperation.subarray(compoundName, 0, currentIndex),
						binding.problemId());
				break; // binding is now a field
			}
			if ((binding = findMemberType(nextName, typeBinding)) == null) {
				if ((mask & FIELD) != 0) {
					return new ProblemBinding(
						CharOperation.subarray(compoundName, 0, currentIndex),
						typeBinding,
						NotFound);
				} else {
					return new ProblemReferenceBinding(
						CharOperation.subarray(compoundName, 0, currentIndex),
						typeBinding,
						NotFound);
				}
			}
			if (!binding.isValidBinding())
				return new ProblemReferenceBinding(
					CharOperation.subarray(compoundName, 0, currentIndex),
					binding.problemId());
		}

		if ((mask & FIELD) != 0 && (binding instanceof FieldBinding)) {
			// was looking for a field and found a field
			FieldBinding field = (FieldBinding) binding;
			if (!field.isStatic())
				return new ProblemFieldBinding(
					field.declaringClass,
					CharOperation.subarray(compoundName, 0, currentIndex),
					NonStaticReferenceInStaticContext);
			return binding;
		}
		if ((mask & TYPE) != 0 && (binding instanceof ReferenceBinding)) {
			// was looking for a type and found a type
			return binding;
		}

		// handle the case when a field or type was asked for but we resolved the compoundName to a type or field
		return new ProblemBinding(
			CharOperation.subarray(compoundName, 0, currentIndex),
			NotFound);
	}

	// Added for code assist... NOT Public API
	public final Binding getBinding(
		char[][] compoundName,
		InvocationSite invocationSite) {
		int currentIndex = 0;
		int length = compoundName.length;
		Binding binding =
			getBinding(
				compoundName[currentIndex++],
				VARIABLE | TYPE | PACKAGE,
				invocationSite);
		if (!binding.isValidBinding())
			return binding;

		foundType : if (binding instanceof PackageBinding) {
			while (currentIndex < length) {
				PackageBinding packageBinding = (PackageBinding) binding;
				binding = packageBinding.getTypeOrPackage(compoundName[currentIndex++]);
				if (binding == null) {
					if (currentIndex == length)
						// must be a type if its the last name, otherwise we have no idea if its a package or type
						return new ProblemReferenceBinding(
							CharOperation.subarray(compoundName, 0, currentIndex),
							NotFound);
					else
						return new ProblemBinding(
							CharOperation.subarray(compoundName, 0, currentIndex),
							NotFound);
				}
				if (binding instanceof ReferenceBinding) {
					if (!binding.isValidBinding())
						return new ProblemReferenceBinding(
							CharOperation.subarray(compoundName, 0, currentIndex),
							binding.problemId());
					if (!((ReferenceBinding) binding).canBeSeenBy(this))
						return new ProblemReferenceBinding(
							CharOperation.subarray(compoundName, 0, currentIndex),
							binding, 
							NotVisible);
					break foundType;
				}
			}
			return binding;
		}

		foundField : if (binding instanceof ReferenceBinding) {
			while (currentIndex < length) {
				ReferenceBinding typeBinding = (ReferenceBinding) binding;
				char[] nextName = compoundName[currentIndex++];
				if ((binding = findField(typeBinding, nextName, invocationSite)) != null) {
					if (!binding.isValidBinding())
						return new ProblemFieldBinding(
							((FieldBinding) binding).declaringClass,
							CharOperation.subarray(compoundName, 0, currentIndex),
							binding.problemId());
					if (!((FieldBinding) binding).isStatic())
						return new ProblemFieldBinding(
							((FieldBinding) binding).declaringClass,
							CharOperation.subarray(compoundName, 0, currentIndex),
							NonStaticReferenceInStaticContext);
					break foundField; // binding is now a field
				}
				if ((binding = findMemberType(nextName, typeBinding)) == null)
					return new ProblemBinding(
						CharOperation.subarray(compoundName, 0, currentIndex),
						typeBinding,
						NotFound);
				if (!binding.isValidBinding())
					return new ProblemReferenceBinding(
						CharOperation.subarray(compoundName, 0, currentIndex),
						binding.problemId());
			}
			return binding;
		}

		VariableBinding variableBinding = (VariableBinding) binding;
		while (currentIndex < length) {
			TypeBinding typeBinding = variableBinding.type;
			if (typeBinding == null)
				return new ProblemFieldBinding(
					null,
					CharOperation.subarray(compoundName, 0, currentIndex + 1),
					NotFound);
			variableBinding =
				findField(typeBinding, compoundName[currentIndex++], invocationSite);
			if (variableBinding == null)
				return new ProblemFieldBinding(
					null,
					CharOperation.subarray(compoundName, 0, currentIndex),
					NotFound);
			if (!variableBinding.isValidBinding())
				return variableBinding;
		}
		return variableBinding;
	}

	/* API
	 *
	 *	Answer the constructor binding that corresponds to receiverType, argumentTypes.
	 *
	 *	InvocationSite implements 
	 *		isSuperAccess(); this is used to determine if the discovered constructor is visible.
	 *
	 *	If no visible constructor is discovered, an error binding is answered.
	 */
	public MethodBinding getConstructor(
		ReferenceBinding receiverType,
		TypeBinding[] argumentTypes,
		InvocationSite invocationSite) {

		compilationUnitScope().recordTypeReference(receiverType);
		compilationUnitScope().recordTypeReferences(argumentTypes);
		MethodBinding methodBinding = receiverType.getExactConstructor(argumentTypes);
		if (methodBinding != null) {
			if (methodBinding.canBeSeenBy(invocationSite, this))
				return methodBinding;
		}
		MethodBinding[] methods =
			receiverType.getMethods(ConstructorDeclaration.ConstantPoolName);
		if (methods == NoMethods) {
			return new ProblemMethodBinding(
				ConstructorDeclaration.ConstantPoolName,
				argumentTypes,
				NotFound);
		}
		MethodBinding[] compatible = new MethodBinding[methods.length];
		int compatibleIndex = 0;
		for (int i = 0, length = methods.length; i < length; i++)
			if (areParametersAssignable(methods[i].parameters, argumentTypes))
				compatible[compatibleIndex++] = methods[i];
		if (compatibleIndex == 0)
			return new ProblemMethodBinding(
				ConstructorDeclaration.ConstantPoolName,
				argumentTypes,
				NotFound);
		// need a more descriptive error... cannot convert from X to Y

		MethodBinding[] visible = new MethodBinding[compatibleIndex];
		int visibleIndex = 0;
		for (int i = 0; i < compatibleIndex; i++) {
			MethodBinding method = compatible[i];
			if (method.canBeSeenBy(invocationSite, this))
				visible[visibleIndex++] = method;
		}
		if (visibleIndex == 1)
			return visible[0];
		if (visibleIndex == 0)
			return new ProblemMethodBinding(
				compatible[0],
				ConstructorDeclaration.ConstantPoolName,
				compatible[0].parameters,
				NotVisible);
		return mostSpecificClassMethodBinding(visible, visibleIndex);
	}

	/*
	 * This retrieves the argument that maps to an enclosing instance of the suitable type,
	 * 	if not found then answers nil -- do not create one
     *	
	 *		#implicitThis		  	 			: the implicit this will be ok
	 *		#((arg) this$n)						: available as a constructor arg
	 * 		#((arg) this$n ... this$p) 			: available as as a constructor arg + a sequence of fields
	 * 		#((fieldDescr) this$n ... this$p) 	: available as a sequence of fields
	 * 		nil 		 											: not found
	 *
	 * 	Note that this algorithm should answer the shortest possible sequence when
	 * 		shortcuts are available:
	 * 				this$0 . this$0 . this$0
	 * 		instead of
	 * 				this$2 . this$1 . this$0 . this$1 . this$0
	 * 		thus the code generation will be more compact and runtime faster
	 */
	public VariableBinding[] getEmulationPath(LocalVariableBinding outerLocalVariable) {

		MethodScope currentMethodScope = this.methodScope();
		SourceTypeBinding sourceType = currentMethodScope.enclosingSourceType();

		// identity check
		if (currentMethodScope == outerLocalVariable.declaringScope.methodScope()) {
			return new VariableBinding[] { outerLocalVariable };
			// implicit this is good enough
		}
		// use synthetic constructor arguments if possible
		if (currentMethodScope.isInsideInitializerOrConstructor()
			&& (sourceType.isNestedType())) {
			SyntheticArgumentBinding syntheticArg;
			if ((syntheticArg = ((NestedTypeBinding) sourceType).getSyntheticArgument(outerLocalVariable)) != null) {
				return new VariableBinding[] { syntheticArg };
			}
		}
		// use a synthetic field then
		if (!currentMethodScope.isStatic) {
			FieldBinding syntheticField;
			if ((syntheticField = sourceType.getSyntheticField(outerLocalVariable)) != null) {
				return new VariableBinding[] { syntheticField };
			}
		}
		return null;
	}

	/*
	 * This retrieves the argument that maps to an enclosing instance of the suitable type,
	 * 	if not found then answers nil -- do not create one
	 *
	 *		#implicitThis		  	 					:  the implicit this will be ok
	 *		#((arg) this$n)								: available as a constructor arg
	 * 		#((arg) this$n access$m... access$p) 		: available as as a constructor arg + a sequence of synthetic accessors to synthetic fields
	 * 		#((fieldDescr) this$n access#m... access$p)	: available as a first synthetic field + a sequence of synthetic accessors to synthetic fields
	 * 		nil 		 														: not found
	 *	jls 15.9.2
	 */
	public Object[] getEmulationPath(
			ReferenceBinding targetEnclosingType, 
			boolean onlyExactMatch,
			boolean ignoreEnclosingArgInConstructorCall) {
		//TODO: (philippe) investigate why exactly test76 fails if ignoreEnclosingArgInConstructorCall is always false
		MethodScope currentMethodScope = this.methodScope();
		SourceTypeBinding sourceType = currentMethodScope.enclosingSourceType();

		// identity check
		if (!currentMethodScope.isStatic 
			&& (!currentMethodScope.isConstructorCall || ignoreEnclosingArgInConstructorCall)
			&& (sourceType == targetEnclosingType
				|| (!onlyExactMatch && targetEnclosingType.isSuperclassOf(sourceType)))) {
			if (currentMethodScope.isConstructorCall) {
				return NoEnclosingInstanceInConstructorCall;
			}
			if (currentMethodScope.isStatic){
				return NoEnclosingInstanceInStaticContext;
			}
			return EmulationPathToImplicitThis; // implicit this is good enough
		}
		if (!sourceType.isNestedType() || sourceType.isStatic()) { // no emulation from within non-inner types
			if (currentMethodScope.isConstructorCall) {
				return NoEnclosingInstanceInConstructorCall;
			}
				if (currentMethodScope.isStatic){
					return NoEnclosingInstanceInStaticContext;
				}
			return null;
		}
		boolean insideConstructor = currentMethodScope.isInsideInitializerOrConstructor();
		// use synthetic constructor arguments if possible
		if (insideConstructor) {
			SyntheticArgumentBinding syntheticArg;
			if ((syntheticArg = ((NestedTypeBinding) sourceType).getSyntheticArgument(targetEnclosingType, onlyExactMatch)) != null) {
				return new Object[] { syntheticArg };
			}
		}

		// use a direct synthetic field then
		if (currentMethodScope.isStatic) {
			return NoEnclosingInstanceInStaticContext;
		}
		FieldBinding syntheticField = sourceType.getSyntheticField(targetEnclosingType, onlyExactMatch);
		if (syntheticField != null) {
			if (currentMethodScope.isConstructorCall){
				return NoEnclosingInstanceInConstructorCall;
			}
			return new Object[] { syntheticField };
		}
		// could be reached through a sequence of enclosing instance link (nested members)
		Object[] path = new Object[2]; // probably at least 2 of them
		ReferenceBinding currentType = sourceType.enclosingType();
		if (insideConstructor) {
			path[0] = ((NestedTypeBinding) sourceType).getSyntheticArgument(currentType, onlyExactMatch);
		} else {
			if (currentMethodScope.isConstructorCall){
				return NoEnclosingInstanceInConstructorCall;
			}
			path[0] = sourceType.getSyntheticField(currentType, onlyExactMatch);
		}
		if (path[0] != null) { // keep accumulating
			
			int count = 1;
			ReferenceBinding currentEnclosingType;
			while ((currentEnclosingType = currentType.enclosingType()) != null) {

				//done?
				if (currentType == targetEnclosingType
					|| (!onlyExactMatch && targetEnclosingType.isSuperclassOf(currentType)))	break;

				if (currentMethodScope != null) {
					currentMethodScope = currentMethodScope.enclosingMethodScope();
					if (currentMethodScope != null && currentMethodScope.isConstructorCall){
						return NoEnclosingInstanceInConstructorCall;
					}
					if (currentMethodScope != null && currentMethodScope.isStatic){
						return NoEnclosingInstanceInStaticContext;
					}
				}
				
				syntheticField = ((NestedTypeBinding) currentType).getSyntheticField(currentEnclosingType, onlyExactMatch);
				if (syntheticField == null) break;

				// append inside the path
				if (count == path.length) {
					System.arraycopy(path, 0, (path = new Object[count + 1]), 0, count);
				}
				// private access emulation is necessary since synthetic field is private
				path[count++] = ((SourceTypeBinding) syntheticField.declaringClass).addSyntheticMethod(syntheticField, true);
				currentType = currentEnclosingType;
			}
			if (currentType == targetEnclosingType
				|| (!onlyExactMatch && targetEnclosingType.isSuperclassOf(currentType))) {
				return path;
			}
		}
		return null;
	}

	/* API
     *	
	 *	Answer the field binding that corresponds to fieldName.
	 *	Start the lookup at the receiverType.
	 *	InvocationSite implements
	 *		isSuperAccess(); this is used to determine if the discovered field is visible.
	 *	Only fields defined by the receiverType or its supertypes are answered;
	 *	a field of an enclosing type will not be found using this API.
	 *
	 *	If no visible field is discovered, an error binding is answered.
	 */
	public FieldBinding getField(
		TypeBinding receiverType,
		char[] fieldName,
		InvocationSite invocationSite) {

		FieldBinding field = findField(receiverType, fieldName, invocationSite);
		if (field == null)
			return new ProblemFieldBinding(
				receiverType instanceof ReferenceBinding
					? (ReferenceBinding) receiverType
					: null,
				fieldName,
				NotFound);
		else
			return field;
	}

	/* API
     *	
	 *	Answer the method binding that corresponds to selector, argumentTypes.
	 *	Start the lookup at the enclosing type of the receiver.
	 *	InvocationSite implements 
	 *		isSuperAccess(); this is used to determine if the discovered method is visible.
	 *		setDepth(int); this is used to record the depth of the discovered method
	 *			relative to the enclosing type of the receiver. (If the method is defined
	 *			in the enclosing type of the receiver, the depth is 0; in the next enclosing
	 *			type, the depth is 1; and so on
	 * 
	 *	If no visible method is discovered, an error binding is answered.
	 */
	public MethodBinding getImplicitMethod(
		char[] selector,
		TypeBinding[] argumentTypes,
		InvocationSite invocationSite) {

		boolean insideStaticContext = false;
		boolean insideConstructorCall = false;
		MethodBinding foundMethod = null;
		ProblemMethodBinding foundFuzzyProblem = null;
		// the weird method lookup case (matches method name in scope, then arg types, then visibility)
		ProblemMethodBinding foundInsideProblem = null;
		// inside Constructor call or inside static context
		Scope scope = this;
		int depth = 0;
		done : while (true) { // done when a COMPILATION_UNIT_SCOPE is found
			switch (scope.kind) {
				case METHOD_SCOPE :
					MethodScope methodScope = (MethodScope) scope;
					insideStaticContext |= methodScope.isStatic;
					insideConstructorCall |= methodScope.isConstructorCall;
					break;
				case CLASS_SCOPE :
					ClassScope classScope = (ClassScope) scope;
					SourceTypeBinding receiverType = classScope.referenceContext.binding;
					boolean isExactMatch = true;
					// retrieve an exact visible match (if possible)
					MethodBinding methodBinding =
						(foundMethod == null)
							? classScope.findExactMethod(
								receiverType,
								selector,
								argumentTypes,
								invocationSite)
							: classScope.findExactMethod(
								receiverType,
								foundMethod.selector,
								foundMethod.parameters,
								invocationSite);
					//						? findExactMethod(receiverType, selector, argumentTypes, invocationSite)
					//						: findExactMethod(receiverType, foundMethod.selector, foundMethod.parameters, invocationSite);
					if (methodBinding == null) {
						// answers closest approximation, may not check argumentTypes or visibility
						isExactMatch = false;
						methodBinding =
							classScope.findMethod(receiverType, selector, argumentTypes, invocationSite);
						//					methodBinding = findMethod(receiverType, selector, argumentTypes, invocationSite);
					}
					if (methodBinding != null) { // skip it if we did not find anything
						if (methodBinding.problemId() == Ambiguous) {
							if (foundMethod == null || foundMethod.problemId() == NotVisible)
								// supercedes any potential InheritedNameHidesEnclosingName problem
								return methodBinding;
							else
								// make the user qualify the method, likely wants the first inherited method (javac generates an ambiguous error instead)
								return new ProblemMethodBinding(
									selector,
									argumentTypes,
									InheritedNameHidesEnclosingName);
						}

						ProblemMethodBinding fuzzyProblem = null;
						ProblemMethodBinding insideProblem = null;
						if (methodBinding.isValidBinding()) {
							if (!isExactMatch) {
								if (!areParametersAssignable(methodBinding.parameters, argumentTypes)) {
									if (foundMethod == null || foundMethod.problemId() == NotVisible){
										// inherited mismatch is reported directly, not looking at enclosing matches
										return new ProblemMethodBinding(methodBinding, selector, argumentTypes, NotFound);
									}
									// make the user qualify the method, likely wants the first inherited method (javac generates an ambiguous error instead)
									fuzzyProblem = new ProblemMethodBinding(selector, methodBinding.parameters, InheritedNameHidesEnclosingName);

								} else if (!methodBinding.canBeSeenBy(receiverType, invocationSite, classScope)) {
									// using <classScope> instead of <this> for visibility check does grant all access to innerclass
									fuzzyProblem =
										new ProblemMethodBinding(
											methodBinding,
											selector,
											methodBinding.parameters,
											NotVisible);
								}
							}
							if (fuzzyProblem == null && !methodBinding.isStatic()) {
								if (insideConstructorCall) {
									insideProblem =
										new ProblemMethodBinding(
											methodBinding.selector,
											methodBinding.parameters,
											NonStaticReferenceInConstructorInvocation);
								} else if (insideStaticContext) {
									insideProblem =
										new ProblemMethodBinding(
											methodBinding.selector,
											methodBinding.parameters,
											NonStaticReferenceInStaticContext);
								}
							}
							
							if (receiverType == methodBinding.declaringClass
								|| (receiverType.getMethods(selector)) != NoMethods
								|| ((fuzzyProblem == null || fuzzyProblem.problemId() != NotVisible) && environment().options.complianceLevel >= ClassFileConstants.JDK1_4)){
								// found a valid method in the 'immediate' scope (ie. not inherited)
								// OR the receiverType implemented a method with the correct name
								// OR in 1.4 mode (inherited visible shadows enclosing)
								if (foundMethod == null) {
									if (depth > 0){
										invocationSite.setDepth(depth);
										invocationSite.setActualReceiverType(receiverType);
									}
									// return the methodBinding if it is not declared in a superclass of the scope's binding (that is, inherited)
									if (fuzzyProblem != null)
										return fuzzyProblem;
									if (insideProblem != null)
										return insideProblem;
									return methodBinding;
								}
								// if a method was found, complain when another is found in an 'immediate' enclosing type (that is, not inherited)
								// NOTE: Unlike fields, a non visible method hides a visible method
								if (foundMethod.declaringClass != methodBinding.declaringClass)
									// ie. have we found the same method - do not trust field identity yet
									return new ProblemMethodBinding(
										methodBinding.selector,
										methodBinding.parameters,
										InheritedNameHidesEnclosingName);
							}
						}

						if (foundMethod == null
							|| (foundMethod.problemId() == NotVisible
								&& methodBinding.problemId() != NotVisible)) {
							// only remember the methodBinding if its the first one found or the previous one was not visible & methodBinding is...
							// remember that private methods are visible if defined directly by an enclosing class
							if (depth > 0){
								invocationSite.setDepth(depth);
								invocationSite.setActualReceiverType(receiverType);
							}
							foundFuzzyProblem = fuzzyProblem;
							foundInsideProblem = insideProblem;
							if (fuzzyProblem == null)
								foundMethod = methodBinding; // only keep it if no error was found
						}
					}
					depth++;
					insideStaticContext |= receiverType.isStatic();
					// 1EX5I8Z - accessing outer fields within a constructor call is permitted
					// in order to do so, we change the flag as we exit from the type, not the method
					// itself, because the class scope is used to retrieve the fields.
					MethodScope enclosingMethodScope = scope.methodScope();
					insideConstructorCall =
						enclosingMethodScope == null ? false : enclosingMethodScope.isConstructorCall;
					break;
				case COMPILATION_UNIT_SCOPE :
					break done;
			}
			scope = scope.parent;
		}

		if (foundFuzzyProblem != null)
			return foundFuzzyProblem;
		if (foundInsideProblem != null)
			return foundInsideProblem;
		if (foundMethod != null)
			return foundMethod;
		return new ProblemMethodBinding(selector, argumentTypes, NotFound);
	}

	/* API
     *	
	 *	Answer the method binding that corresponds to selector, argumentTypes.
	 *	Start the lookup at the receiverType.
	 *	InvocationSite implements 
	 *		isSuperAccess(); this is used to determine if the discovered method is visible.
	 *
	 *	Only methods defined by the receiverType or its supertypes are answered;
	 *	use getImplicitMethod() to discover methods of enclosing types.
	 *
	 *	If no visible method is discovered, an error binding is answered.
	 */
	public MethodBinding getMethod(
		TypeBinding receiverType,
		char[] selector,
		TypeBinding[] argumentTypes,
		InvocationSite invocationSite) {

		if (receiverType.isArrayType())
			return findMethodForArray(
				(ArrayBinding) receiverType,
				selector,
				argumentTypes,
				invocationSite);
		if (receiverType.isBaseType())
			return new ProblemMethodBinding(selector, argumentTypes, NotFound);

		ReferenceBinding currentType = (ReferenceBinding) receiverType;
		if (!currentType.canBeSeenBy(this))
			return new ProblemMethodBinding(selector, argumentTypes, ReceiverTypeNotVisible);

		// retrieve an exact visible match (if possible)
		MethodBinding methodBinding =
			findExactMethod(currentType, selector, argumentTypes, invocationSite);
		if (methodBinding != null)
			return methodBinding;

		// answers closest approximation, may not check argumentTypes or visibility
		methodBinding =
			findMethod(currentType, selector, argumentTypes, invocationSite);
		if (methodBinding == null)
			return new ProblemMethodBinding(selector, argumentTypes, NotFound);
		if (methodBinding.isValidBinding()) {
			if (!areParametersAssignable(methodBinding.parameters, argumentTypes))
				return new ProblemMethodBinding(
					methodBinding,
					selector,
					argumentTypes,
					NotFound);
			if (!methodBinding.canBeSeenBy(currentType, invocationSite, this))
				return new ProblemMethodBinding(
					methodBinding,
					selector,
					methodBinding.parameters,
					NotVisible);
		}
		return methodBinding;
	}

	public int maxShiftedOffset() {
		int max = -1;
		if (this.shiftScopes != null){
			for (int i = 0, length = this.shiftScopes.length; i < length; i++){
				int subMaxOffset = this.shiftScopes[i].maxOffset;
				if (subMaxOffset > max) max = subMaxOffset;
			}
		}
		return max;
	}
	
	/* Answer the problem reporter to use for raising new problems.
	 *
	 * Note that as a side-effect, this updates the current reference context
	 * (unit, type or method) in case the problem handler decides it is necessary
	 * to abort.
	 */
	public ProblemReporter problemReporter() {

		return outerMostMethodScope().problemReporter();
	}

	/*
	 * Code responsible to request some more emulation work inside the invocation type, so as to supply
	 * correct synthetic arguments to any allocation of the target type.
	 */
	public void propagateInnerEmulation(ReferenceBinding targetType, boolean isEnclosingInstanceSupplied) {

		// no need to propagate enclosing instances, they got eagerly allocated already.
		
		SyntheticArgumentBinding[] syntheticArguments;
		if ((syntheticArguments = targetType.syntheticOuterLocalVariables()) != null) {
			for (int i = 0, max = syntheticArguments.length; i < max; i++) {
				SyntheticArgumentBinding syntheticArg = syntheticArguments[i];
				// need to filter out the one that could match a supplied enclosing instance
				if (!(isEnclosingInstanceSupplied
					&& (syntheticArg.type == targetType.enclosingType()))) {
					this.emulateOuterAccess(syntheticArg.actualOuterLocalVariable);
				}
			}
		}
	}

	/* Answer the reference type of this scope.
	 *
	 * It is the nearest enclosing type of this scope.
	 */
	public TypeDeclaration referenceType() {

		return methodScope().referenceType();
	}

	// start position in this scope - for ordering scopes vs. variables
	int startIndex() {
		return startIndex;
	}

	public String toString() {
		return toString(0);
	}

	public String toString(int tab) {

		String s = basicToString(tab);
		for (int i = 0; i < scopeIndex; i++)
			if (subscopes[i] instanceof BlockScope)
				s += ((BlockScope) subscopes[i]).toString(tab + 1) + "\n"; //$NON-NLS-1$
		return s;
	}
}
