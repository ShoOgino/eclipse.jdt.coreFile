/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

/**
 * A parser that locates ast nodes that match a given search pattern.
 */
public class MatchLocatorParser extends Parser {

	static final int UNSPECIFIED_REFERENCE_FINE_GRAIN_MASK =
		IJavaSearchConstants.CAST_TYPE_REFERENCE |
		IJavaSearchConstants.QUALIFIED_REFERENCE |
		IJavaSearchConstants.IMPLICIT_THIS_REFERENCE |
		IJavaSearchConstants.TYPE_ARGUMENT_TYPE_REFERENCE |
		IJavaSearchConstants.WILDCARD_BOUND_TYPE_REFERENCE;
	static final int FORMAL_PARAMETER_FINE_GRAIN_MASK = 
		IJavaSearchConstants.PARAMETER_DECLARATION_TYPE_REFERENCE |
		IJavaSearchConstants.CATCH_TYPE_REFERENCE;
	static final int GENERIC_FINE_GRAIN_MASK = 
		IJavaSearchConstants.TYPE_ARGUMENT_TYPE_REFERENCE |
		IJavaSearchConstants.TYPE_VARIABLE_BOUND_TYPE_REFERENCE |
		IJavaSearchConstants.WILDCARD_BOUND_TYPE_REFERENCE;
	MatchingNodeSet nodeSet;
	PatternLocator patternLocator;
	private ASTVisitor localDeclarationVisitor;
	int typeRefFineGrain;

public static MatchLocatorParser createParser(ProblemReporter problemReporter, MatchLocator locator) {
	if ((locator.matchContainer & PatternLocator.COMPILATION_UNIT_CONTAINER) != 0) {
		return new ImportMatchLocatorParser(problemReporter, locator);
	}
	return new MatchLocatorParser(problemReporter, locator);
}

/**
 * An ast visitor that visits local type declarations.
 */
public class NoClassNoMethodDeclarationVisitor extends ASTVisitor {
	public boolean visit(ConstructorDeclaration constructorDeclaration, ClassScope scope) {
		return (constructorDeclaration.bits & ASTNode.HasLocalType) != 0; // continue only if it has local type
	}
	public boolean visit(FieldDeclaration fieldDeclaration, MethodScope scope) {
		return (fieldDeclaration.bits & ASTNode.HasLocalType) != 0; // continue only if it has local type;
	}
	public boolean visit(Initializer initializer, MethodScope scope) {
		return (initializer.bits & ASTNode.HasLocalType) != 0; // continue only if it has local type
	}
	public boolean visit(MethodDeclaration methodDeclaration, ClassScope scope) {
		return (methodDeclaration.bits & ASTNode.HasLocalType) != 0; // continue only if it has local type
	}
}
public class MethodButNoClassDeclarationVisitor extends NoClassNoMethodDeclarationVisitor {
	public boolean visit(TypeDeclaration localTypeDeclaration, BlockScope scope) {
		patternLocator.match(localTypeDeclaration, nodeSet);
		return true;
	}
}
public class ClassButNoMethodDeclarationVisitor extends ASTVisitor {
	public boolean visit(ConstructorDeclaration constructorDeclaration, ClassScope scope) {
		patternLocator.match(constructorDeclaration, nodeSet);
		return (constructorDeclaration.bits & ASTNode.HasLocalType) != 0; // continue only if it has local type
	}
	public boolean visit(FieldDeclaration fieldDeclaration, MethodScope scope) {
		int patternFineGrain = patternLocator.fineGrain();
		if (patternFineGrain == 0 || (patternFineGrain & IJavaSearchConstants.FIELD_DECLARATION_TYPE_REFERENCE) != 0) {
			patternLocator.match(fieldDeclaration, nodeSet);
		}
		return (fieldDeclaration.bits & ASTNode.HasLocalType) != 0; // continue only if it has local type;
	}
	public boolean visit(Initializer initializer, MethodScope scope) {
		patternLocator.match(initializer, nodeSet);
		return (initializer.bits & ASTNode.HasLocalType) != 0; // continue only if it has local type
	}
	public boolean visit(TypeDeclaration memberTypeDeclaration, ClassScope scope) {
		patternLocator.match(memberTypeDeclaration, nodeSet);
		return true;
	}
	public boolean visit(MethodDeclaration methodDeclaration, ClassScope scope) {
		patternLocator.match(methodDeclaration, nodeSet);
		return (methodDeclaration.bits & ASTNode.HasLocalType) != 0; // continue only if it has local type
	}
	public boolean visit(AnnotationMethodDeclaration methodDeclaration, ClassScope scope) {
		patternLocator.match(methodDeclaration, nodeSet);
		return false; // no local type for annotation type members
	}
}
public class ClassAndMethodDeclarationVisitor extends ClassButNoMethodDeclarationVisitor {
	public boolean visit(TypeDeclaration localTypeDeclaration, BlockScope scope) {
		patternLocator.match(localTypeDeclaration, nodeSet);
		return true;
	}
}

protected MatchLocatorParser(ProblemReporter problemReporter, MatchLocator locator) {
	super(problemReporter, true);
	this.reportOnlyOneSyntaxError = true;
	this.patternLocator = locator.patternLocator;
	if ((locator.matchContainer & PatternLocator.CLASS_CONTAINER) != 0) {
		this.localDeclarationVisitor = (locator.matchContainer & PatternLocator.METHOD_CONTAINER) != 0
			? new ClassAndMethodDeclarationVisitor()
			: new ClassButNoMethodDeclarationVisitor();
	} else {
		this.localDeclarationVisitor = (locator.matchContainer & PatternLocator.METHOD_CONTAINER) != 0
			? new MethodButNoClassDeclarationVisitor()
			: new NoClassNoMethodDeclarationVisitor();
	}
}
public void checkComment() {
	super.checkComment();
	if (this.javadocParser.checkDocComment && this.javadoc != null) {

		// Search for pattern locator matches in javadoc comment parameters @param tags
		JavadocSingleNameReference[] paramReferences = this.javadoc.paramReferences;
		if (paramReferences != null) {
			for (int i=0, length=paramReferences.length; i < length; i++) {
				this.patternLocator.match(paramReferences[i], this.nodeSet);
			}
		}

		// Search for pattern locator matches in javadoc comment type parameters @param tags
		JavadocSingleTypeReference[] paramTypeParameters = this.javadoc.paramTypeParameters;
		if (paramTypeParameters != null) {
			for (int i=0, length=paramTypeParameters.length; i < length; i++) {
				this.patternLocator.match(paramTypeParameters[i], this.nodeSet);
			}
		}

		// Search for pattern locator matches in javadoc comment @throws/@exception tags
		TypeReference[] thrownExceptions = this.javadoc.exceptionReferences;
		if (thrownExceptions != null) {
			for (int i=0, length=thrownExceptions.length; i < length; i++) {
				this.patternLocator.match(thrownExceptions[i], this.nodeSet);
			}
		}

		// Search for pattern locator matches in javadoc comment @see tags
		Expression[] references = this.javadoc.seeReferences;
		if (references != null) {
			for (int i=0, length=references.length; i < length; i++) {
				Expression reference = references[i];
				if (reference instanceof TypeReference) {
					TypeReference typeRef = (TypeReference) reference;
					this.patternLocator.match(typeRef, this.nodeSet);
				} else if (reference instanceof JavadocFieldReference) {
					JavadocFieldReference fieldRef = (JavadocFieldReference) reference;
					this.patternLocator.match(fieldRef, this.nodeSet);
					if (fieldRef.receiver instanceof TypeReference && !fieldRef.receiver.isThis()) {
						TypeReference typeRef = (TypeReference) fieldRef.receiver;
						this.patternLocator.match(typeRef, this.nodeSet);
					}
				} else if (reference instanceof JavadocMessageSend) {
					JavadocMessageSend messageSend = (JavadocMessageSend) reference;
					this.patternLocator.match(messageSend, this.nodeSet);
					if (messageSend.receiver instanceof TypeReference && !messageSend.receiver.isThis()) {
						TypeReference typeRef = (TypeReference) messageSend.receiver;
						this.patternLocator.match(typeRef, this.nodeSet);
					}
					if (messageSend.arguments != null) {
						for (int a=0,al=messageSend.arguments.length; a<al; a++) {
							JavadocArgumentExpression argument = (JavadocArgumentExpression) messageSend.arguments[a];
							if (argument.argument != null && argument.argument.type != null) {
								this.patternLocator.match(argument.argument.type, this.nodeSet);
							}
						}
					}
				} else if (reference instanceof JavadocAllocationExpression) {
					JavadocAllocationExpression constructor = (JavadocAllocationExpression) reference;
					this.patternLocator.match(constructor, this.nodeSet);
					if (constructor.type != null && !constructor.type.isThis()) {
						this.patternLocator.match(constructor.type, this.nodeSet);
					}
					if (constructor.arguments != null) {
						for (int a=0,al=constructor.arguments.length; a<al; a++) {
							this.patternLocator.match(constructor.arguments[a], this.nodeSet);
							JavadocArgumentExpression argument = (JavadocArgumentExpression) constructor.arguments[a];
							if (argument.argument != null && argument.argument.type != null) {
								this.patternLocator.match(argument.argument.type, this.nodeSet);
							}
						}
					}
				}
			}
		}
	}
}
protected void classInstanceCreation(boolean alwaysQualified) {
	super.classInstanceCreation(alwaysQualified);
	this.patternLocator.match(this.expressionStack[this.expressionPtr], this.nodeSet);
}
protected void consumeAnnotationAsModifier() {
	super.consumeAnnotationAsModifier();
}
protected void consumeAnnotationName() {
	super.consumeAnnotationName();
}
protected void consumeAssignment() {
	super.consumeAssignment();
	this.patternLocator.match(this.expressionStack[this.expressionPtr], this.nodeSet);
}
protected void consumeClassInstanceCreationExpressionQualifiedWithTypeArguments() {
	super.consumeClassInstanceCreationExpressionWithTypeArguments();
	this.patternLocator.match(this.expressionStack[this.expressionPtr], this.nodeSet);
}
protected void consumeClassInstanceCreationExpressionWithTypeArguments() {
	super.consumeClassInstanceCreationExpressionWithTypeArguments();
	this.patternLocator.match(this.expressionStack[this.expressionPtr], this.nodeSet);
}
protected void consumeExplicitConstructorInvocation(int flag, int recFlag) {
	super.consumeExplicitConstructorInvocation(flag, recFlag);
	this.patternLocator.match(this.astStack[this.astPtr], this.nodeSet);
}
protected void consumeExplicitConstructorInvocationWithTypeArguments(int flag, int recFlag) {
	super.consumeExplicitConstructorInvocationWithTypeArguments(flag, recFlag);
	this.patternLocator.match(this.astStack[this.astPtr], this.nodeSet);
}
protected void consumeFieldAccess(boolean isSuperAccess) {
	super.consumeFieldAccess(isSuperAccess);

	int fineGrain = isSuperAccess ? IJavaSearchConstants.SUPER_REFERENCE : IJavaSearchConstants.THIS_REFERENCE;
	int patternFineGrain = this.patternLocator.fineGrain();
	if (patternFineGrain == 0 || (patternFineGrain & fineGrain) != 0) {
		// this is always a Reference
		this.patternLocator.match((Reference) this.expressionStack[this.expressionPtr], this.nodeSet);
	}
}
protected void consumeFormalParameter(boolean isVarArgs) {
	this.typeRefFineGrain |= FORMAL_PARAMETER_FINE_GRAIN_MASK;
	super.consumeFormalParameter(isVarArgs);
	this.patternLocator.match((LocalDeclaration) this.astStack[this.astPtr], this.nodeSet);
	this.typeRefFineGrain &= ~FORMAL_PARAMETER_FINE_GRAIN_MASK;
}

protected void consumeLocalVariableDeclaration() {
	super.consumeLocalVariableDeclaration();

	// this is always a LocalDeclaration
	this.patternLocator.match((LocalDeclaration) this.astStack[this.astPtr], this.nodeSet);
}
protected void consumeMarkerAnnotation() {
	super.consumeMarkerAnnotation();
	int patternFineGrain = this.patternLocator.fineGrain();
	if (patternFineGrain == 0 || (patternFineGrain & IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE) != 0) {
		// this is always an Annotation
		Annotation annotation = (Annotation) expressionStack[expressionPtr];
		this.patternLocator.match(annotation, nodeSet);
	}
}
protected void consumeMemberValuePair() {
//	this.validFineGrain |= IJavaSearchConstants.MEMBER_PAIR_VALUE_REFERENCE;
	super.consumeMemberValuePair();

	// this is always a MemberValuePair
	this.patternLocator.match((MemberValuePair) this.astStack[this.astPtr], this.nodeSet);
//	this.validFineGrain &= ~IJavaSearchConstants.MEMBER_PAIR_VALUE_REFERENCE;
}
protected void consumeMethodInvocationName() {
	super.consumeMethodInvocationName();
	
	int patternFineGrain = this.patternLocator.fineGrain();
	if (patternFineGrain == 0 || (patternFineGrain & (IJavaSearchConstants.IMPLICIT_THIS_REFERENCE | IJavaSearchConstants.QUALIFIED_REFERENCE)) != 0) {
		// this is always a MessageSend
		this.patternLocator.match((MessageSend) this.expressionStack[this.expressionPtr], this.nodeSet);
	}
}
protected void consumeMethodInvocationNameWithTypeArguments() {
	super.consumeMethodInvocationNameWithTypeArguments();

	int patternFineGrain = this.patternLocator.fineGrain();
	if (patternFineGrain == 0 || (patternFineGrain & (IJavaSearchConstants.IMPLICIT_THIS_REFERENCE | IJavaSearchConstants.QUALIFIED_REFERENCE)) != 0) {
		// this is always a MessageSend
		this.patternLocator.match((MessageSend) this.expressionStack[this.expressionPtr], this.nodeSet);
	}
}
protected void consumeMethodInvocationPrimary() {
	super.consumeMethodInvocationPrimary(); 

	int patternFineGrain = this.patternLocator.fineGrain();
	if (patternFineGrain == 0 || (patternFineGrain & IJavaSearchConstants.THIS_REFERENCE) != 0) {
		// this is always a MessageSend
		this.patternLocator.match((MessageSend) this.expressionStack[this.expressionPtr], this.nodeSet);
	}
}
protected void consumeMethodInvocationPrimaryWithTypeArguments() {
	super.consumeMethodInvocationPrimaryWithTypeArguments();

	int patternFineGrain = this.patternLocator.fineGrain();
	if (patternFineGrain == 0 || (patternFineGrain & IJavaSearchConstants.THIS_REFERENCE) != 0) {
		// this is always a MessageSend
		this.patternLocator.match((MessageSend) this.expressionStack[this.expressionPtr], this.nodeSet);
	}
}
protected void consumeMethodInvocationSuper() {
	super.consumeMethodInvocationSuper();

	int patternFineGrain = this.patternLocator.fineGrain();
	if (patternFineGrain == 0 || (patternFineGrain & IJavaSearchConstants.SUPER_REFERENCE) != 0) {
		// this is always a MessageSend
		this.patternLocator.match((MessageSend) this.expressionStack[this.expressionPtr], this.nodeSet);
	}
}
protected void consumeMethodInvocationSuperWithTypeArguments() {
	super.consumeMethodInvocationSuperWithTypeArguments();

	int patternFineGrain = this.patternLocator.fineGrain();
	if (patternFineGrain == 0 || (patternFineGrain & IJavaSearchConstants.SUPER_REFERENCE) != 0) {
		// this is always a MessageSend
		this.patternLocator.match((MessageSend) this.expressionStack[this.expressionPtr], this.nodeSet);
	}
}
protected void consumeNormalAnnotation() {
	super.consumeNormalAnnotation();
	int patternFineGrain = this.patternLocator.fineGrain();
	if (patternFineGrain == 0 || (patternFineGrain & IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE) != 0) {
		// this is always an Annotation
		Annotation annotation = (Annotation) expressionStack[expressionPtr];
		this.patternLocator.match(annotation, nodeSet);
	}
}
protected void consumePrimaryNoNewArray() {
	// pop parenthesis positions (and don't update expression positions
	// (see http://bugs.eclipse.org/bugs/show_bug.cgi?id=23329)
	intPtr--;
	intPtr--;
}

protected void consumePrimaryNoNewArrayWithName() {
	// PrimaryNoNewArray ::=  PushLPAREN Expression PushRPAREN 
	pushOnExpressionStack(getUnspecifiedReferenceOptimized());
	// pop parenthesis positions (and don't update expression positions
	// (see http://bugs.eclipse.org/bugs/show_bug.cgi?id=23329)
	intPtr--;
	intPtr--;
}
protected void consumeSingleMemberAnnotation() {
	super.consumeSingleMemberAnnotation();
	// this is always an Annotation
	Annotation annotation = (Annotation) expressionStack[expressionPtr];
	this.patternLocator.match(annotation, nodeSet);
}
protected void consumeTypeArgument() {
	this.typeRefFineGrain |= IJavaSearchConstants.TYPE_ARGUMENT_TYPE_REFERENCE;
	super.consumeTypeArgument();
	patternLocator.match((TypeReference)genericsStack[genericsPtr], nodeSet);
	this.typeRefFineGrain &= ~IJavaSearchConstants.TYPE_ARGUMENT_TYPE_REFERENCE;
}
protected void consumeTypeArgumentReferenceType1() {
	this.typeRefFineGrain |= IJavaSearchConstants.TYPE_ARGUMENT_TYPE_REFERENCE;
	super.consumeTypeArgumentReferenceType1();
	patternLocator.match((TypeReference)genericsStack[genericsPtr], nodeSet);
	this.typeRefFineGrain &= ~IJavaSearchConstants.TYPE_ARGUMENT_TYPE_REFERENCE;
}
protected void consumeTypeArgumentReferenceType2() {
	this.typeRefFineGrain |= IJavaSearchConstants.TYPE_ARGUMENT_TYPE_REFERENCE;
	super.consumeTypeArgumentReferenceType2();
	patternLocator.match((TypeReference)genericsStack[genericsPtr], nodeSet);
	this.typeRefFineGrain &= ~IJavaSearchConstants.TYPE_ARGUMENT_TYPE_REFERENCE;
}
protected void consumeTypeParameterHeader() {
	super.consumeTypeParameterHeader();
	patternLocator.match((TypeParameter)genericsStack[genericsPtr], nodeSet);
}
protected void consumeUnaryExpression(int op, boolean post) {
	super.consumeUnaryExpression(op, post);
	this.patternLocator.match(this.expressionStack[this.expressionPtr], this.nodeSet);
}
protected TypeReference copyDims(TypeReference typeRef, int dim) {
	TypeReference result = super.copyDims(typeRef, dim);
	 if (this.nodeSet.removePossibleMatch(typeRef) != null)
		this.nodeSet.addPossibleMatch(result);
	 else if (this.nodeSet.removeTrustedMatch(typeRef) != null)
		this.nodeSet.addTrustedMatch(result, true);
	return result;
}
protected TypeReference getTypeReference(int dim) {
	TypeReference typeRef = super.getTypeReference(dim);
	int patternFineGrain = this.patternLocator.fineGrain();
	if (patternFineGrain == 0 || (this.typeRefFineGrain & patternFineGrain) != 0) {
		this.patternLocator.match(typeRef, this.nodeSet); // NB: Don't check container since type reference can happen anywhere
	}
	return typeRef;
}
protected NameReference getUnspecifiedReference() {
	NameReference nameRef = super.getUnspecifiedReference();
	int patternFineGrain = this.patternLocator.fineGrain();
	if (patternFineGrain == 0 || (patternFineGrain & UNSPECIFIED_REFERENCE_FINE_GRAIN_MASK) != 0) {
		this.patternLocator.match(nameRef, this.nodeSet); // NB: Don't check container since unspecified reference can happen anywhere
	}
	return nameRef;
}
protected NameReference getUnspecifiedReferenceOptimized() {
	NameReference nameRef = super.getUnspecifiedReferenceOptimized();
	int patternFineGrain = this.patternLocator.fineGrain();
	if (patternFineGrain == 0 || (patternFineGrain & UNSPECIFIED_REFERENCE_FINE_GRAIN_MASK) != 0) {
		this.patternLocator.match(nameRef, this.nodeSet); // NB: Don't check container since unspecified reference can happen anywhere
	}
	return nameRef;
}
/**
 * Parses the method bodies in the given compilation unit
 * @param unit CompilationUnitDeclaration
 */
public void parseBodies(CompilationUnitDeclaration unit) {
	TypeDeclaration[] types = unit.types;
	if (types == null) return;

	for (int i = 0; i < types.length; i++) {
		TypeDeclaration type = types[i];
		this.patternLocator.match(type, this.nodeSet);
		this.parseBodies(type, unit);
	}
}
/**
 * Parses the member bodies in the given type.
 * @param type TypeDeclaration
 * @param unit CompilationUnitDeclaration
 */
protected void parseBodies(TypeDeclaration type, CompilationUnitDeclaration unit) {
	FieldDeclaration[] fields = type.fields;
	if (fields != null) {
		for (int i = 0; i < fields.length; i++) {
			FieldDeclaration field = fields[i];
			if (field instanceof Initializer)
				this.parse((Initializer) field, type, unit);
			field.traverse(localDeclarationVisitor, null);
		}
	}

	AbstractMethodDeclaration[] methods = type.methods;
	if (methods != null) {
		for (int i = 0; i < methods.length; i++) {
			AbstractMethodDeclaration method = methods[i];
			if (method.sourceStart >= type.bodyStart) { // if not synthetic
				if (method instanceof MethodDeclaration) {
					MethodDeclaration methodDeclaration = (MethodDeclaration) method;
					this.parse(methodDeclaration, unit);
					methodDeclaration.traverse(localDeclarationVisitor, (ClassScope) null);
				} else if (method instanceof ConstructorDeclaration) {
					ConstructorDeclaration constructorDeclaration = (ConstructorDeclaration) method;
					this.parse(constructorDeclaration, unit, false);
					constructorDeclaration.traverse(localDeclarationVisitor, (ClassScope) null);
				}
			} else if (method.isDefaultConstructor()) {
				method.parseStatements(this, unit);
			}
		}
	}

	TypeDeclaration[] memberTypes = type.memberTypes;
	if (memberTypes != null) {
		for (int i = 0; i < memberTypes.length; i++) {
			TypeDeclaration memberType = memberTypes[i];
			this.parseBodies(memberType, unit);
			memberType.traverse(localDeclarationVisitor, (ClassScope) null);
		}
	}
}

protected void consumeClassHeaderExtends() {
	this.typeRefFineGrain |= IJavaSearchConstants.SUPERTYPE_TYPE_REFERENCE;
	super.consumeClassHeaderExtends();
	this.typeRefFineGrain &= ~IJavaSearchConstants.SUPERTYPE_TYPE_REFERENCE;
}

protected void consumeInterfaceType() {
	this.typeRefFineGrain |= IJavaSearchConstants.SUPERINTERFACE_TYPE_REFERENCE;
	super.consumeInterfaceType();
	this.typeRefFineGrain &= ~IJavaSearchConstants.SUPERINTERFACE_TYPE_REFERENCE;
}

protected void consumeClassTypeElt() {
	this.typeRefFineGrain |= IJavaSearchConstants.THROWS_CLAUSE_TYPE_REFERENCE;
	super.consumeClassTypeElt();
	this.typeRefFineGrain  &= ~IJavaSearchConstants.THROWS_CLAUSE_TYPE_REFERENCE;
}

protected void consumeClassInstanceCreationExpression() {
	this.typeRefFineGrain |= IJavaSearchConstants.ALLOCATION_EXPRESSION_TYPE_REFERENCE;
	super.consumeClassInstanceCreationExpression();
	this.typeRefFineGrain &= ~IJavaSearchConstants.ALLOCATION_EXPRESSION_TYPE_REFERENCE;
}

protected void consumeMethodHeaderName(boolean isAnnotationMethod) {
	this.typeRefFineGrain |= IJavaSearchConstants.RETURN_TYPE_REFERENCE;
	super.consumeMethodHeaderName(isAnnotationMethod);
	this.typeRefFineGrain &= ~IJavaSearchConstants.RETURN_TYPE_REFERENCE;
}

protected void consumeEnterVariable() {
	int grain = IJavaSearchConstants.FIELD_DECLARATION_TYPE_REFERENCE | IJavaSearchConstants.LOCAL_VARIABLE_DECLARATION_TYPE_REFERENCE;
	this.typeRefFineGrain |= grain;
	super.consumeEnterVariable();
	this.typeRefFineGrain &= ~grain;
}

protected void consumeAllocationHeader() {
	this.typeRefFineGrain |= IJavaSearchConstants.ALLOCATION_EXPRESSION_TYPE_REFERENCE;
	super.consumeAllocationHeader();
	this.typeRefFineGrain &= ~IJavaSearchConstants.ALLOCATION_EXPRESSION_TYPE_REFERENCE;
}

protected void consumeStatementCatch() {
	this.typeRefFineGrain |= IJavaSearchConstants.CATCH_TYPE_REFERENCE;
	super.consumeStatementCatch();
	this.typeRefFineGrain &= ~IJavaSearchConstants.CATCH_TYPE_REFERENCE;
}

protected void consumeTypeParameterWithExtends() {
	this.typeRefFineGrain |= IJavaSearchConstants.TYPE_VARIABLE_BOUND_TYPE_REFERENCE;
	super.consumeTypeParameterWithExtends();
	this.typeRefFineGrain &= ~IJavaSearchConstants.TYPE_VARIABLE_BOUND_TYPE_REFERENCE;
}

protected void consumeReferenceType1() {
	this.typeRefFineGrain |= GENERIC_FINE_GRAIN_MASK;
	super.consumeReferenceType1();
	this.typeRefFineGrain &= ~GENERIC_FINE_GRAIN_MASK;
}

protected void consumeReferenceType2() {
	this.typeRefFineGrain |= GENERIC_FINE_GRAIN_MASK;
	super.consumeReferenceType2();
	this.typeRefFineGrain &= ~GENERIC_FINE_GRAIN_MASK;
}

protected void consumeReferenceType3() {
	this.typeRefFineGrain |= GENERIC_FINE_GRAIN_MASK;
	super.consumeReferenceType3();
	this.typeRefFineGrain &= ~GENERIC_FINE_GRAIN_MASK;
}

protected void consumeAdditionalBound() {
	this.typeRefFineGrain |= GENERIC_FINE_GRAIN_MASK;
	super.consumeAdditionalBound();
	this.typeRefFineGrain &= ~GENERIC_FINE_GRAIN_MASK;
}

protected void consumeTypeParameter1WithExtendsAndBounds() {
	this.typeRefFineGrain |= GENERIC_FINE_GRAIN_MASK;
	super.consumeTypeParameter1WithExtendsAndBounds();
	this.typeRefFineGrain &= ~GENERIC_FINE_GRAIN_MASK;
}

protected void consumeTypeParameterWithExtendsAndBounds() {
	this.typeRefFineGrain |= GENERIC_FINE_GRAIN_MASK;
	super.consumeTypeParameterWithExtendsAndBounds();
	this.typeRefFineGrain &= ~GENERIC_FINE_GRAIN_MASK;
}

protected void consumeWildcardBoundsExtends() {
	this.typeRefFineGrain |= IJavaSearchConstants.WILDCARD_BOUND_TYPE_REFERENCE;
	super.consumeWildcardBoundsExtends();
	this.typeRefFineGrain &= ~IJavaSearchConstants.WILDCARD_BOUND_TYPE_REFERENCE;
}

protected void consumeWildcardBoundsSuper() {
	this.typeRefFineGrain |= IJavaSearchConstants.WILDCARD_BOUND_TYPE_REFERENCE;
	super.consumeWildcardBoundsSuper();
	this.typeRefFineGrain &= ~IJavaSearchConstants.WILDCARD_BOUND_TYPE_REFERENCE;
}
protected void consumeWildcardBounds1Extends() {
	this.typeRefFineGrain |= IJavaSearchConstants.WILDCARD_BOUND_TYPE_REFERENCE;
	super.consumeWildcardBounds1Extends();
	this.typeRefFineGrain &= ~IJavaSearchConstants.WILDCARD_BOUND_TYPE_REFERENCE;
}
protected void consumeWildcardBounds1Super() {
	this.typeRefFineGrain |= IJavaSearchConstants.WILDCARD_BOUND_TYPE_REFERENCE;
	super.consumeWildcardBounds1Super();
	this.typeRefFineGrain &= ~IJavaSearchConstants.WILDCARD_BOUND_TYPE_REFERENCE;
}
protected void consumeWildcardBounds2Extends() {
	this.typeRefFineGrain |= IJavaSearchConstants.WILDCARD_BOUND_TYPE_REFERENCE;
	super.consumeWildcardBounds2Extends();
	this.typeRefFineGrain &= ~IJavaSearchConstants.WILDCARD_BOUND_TYPE_REFERENCE;
}
protected void consumeWildcardBounds2Super() {
	this.typeRefFineGrain |= IJavaSearchConstants.WILDCARD_BOUND_TYPE_REFERENCE;
	super.consumeWildcardBounds2Super();
	this.typeRefFineGrain &= ~IJavaSearchConstants.WILDCARD_BOUND_TYPE_REFERENCE;
}
protected void consumeWildcardBounds3Extends() {
	this.typeRefFineGrain |= IJavaSearchConstants.WILDCARD_BOUND_TYPE_REFERENCE;
	super.consumeWildcardBounds3Extends();
	this.typeRefFineGrain &= ~IJavaSearchConstants.WILDCARD_BOUND_TYPE_REFERENCE;
}
protected void consumeWildcardBounds3Super() {
	this.typeRefFineGrain |= IJavaSearchConstants.WILDCARD_BOUND_TYPE_REFERENCE;
	super.consumeWildcardBounds3Super();
	this.typeRefFineGrain &= ~IJavaSearchConstants.WILDCARD_BOUND_TYPE_REFERENCE;
}

}

