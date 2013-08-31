/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for
 *								bug 392862 - [1.8][compiler][null] Evaluate null annotations on array types
 *								bug 395002 - Self bound generic class doesn't resolve bounds properly for wildcards for certain parametrisation.
 *								bug 392384 - [1.8][compiler][null] Restore nullness info from type annotations in class files
 *								Bug 392099 - [1.8][compiler][null] Apply null annotation on types for null analysis
 *								Bug 415291 - [1.8][null] differentiate type incompatibilities due to null annotations
 *								Bug 415850 - [1.8] Ensure RunJDTCoreTests can cope with null annotations enabled
 *								Bug 416176 - [1.8][compiler][null] null type annotations cause grief on type variables
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import java.util.List;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.Constant;

public final class ArrayBinding extends TypeBinding {
	// creation and initialization of the length field
	// the declaringClass of this field is intentionally set to null so it can be distinguished.
	public static final FieldBinding ArrayLength = new FieldBinding(TypeConstants.LENGTH, TypeBinding.INT, ClassFileConstants.AccPublic | ClassFileConstants.AccFinal, null, Constant.NotAConstant);

	public TypeBinding leafComponentType;
	public int dimensions;
	LookupEnvironment environment;
	char[] constantPoolName;
	char[] genericTypeSignature;

	// One bitset for each dimension plus one more for the leaf component type at position 'dimensions',
	// possible bits are TagBits.AnnotationNonNull and TagBits.AnnotationNullable
	// (only ever set when CompilerOptions.isAnnotationBasedNullAnalysisEnabled == true):
	public long[] nullTagBitsPerDimension;

public ArrayBinding(TypeBinding type, int dimensions, LookupEnvironment environment) {
	this(type, dimensions, environment, null);
}
public ArrayBinding(TypeBinding type, int dimensions, LookupEnvironment environment, long[] nullTagBitsPerDimension) {
	this.tagBits |= TagBits.IsArrayType;
	this.leafComponentType = type;
	this.dimensions = dimensions;
	this.environment = environment;
	if (type instanceof UnresolvedReferenceBinding)
		((UnresolvedReferenceBinding) type).addWrapper(this, environment);
	else
		this.tagBits |= type.tagBits & (TagBits.HasTypeVariable | TagBits.HasDirectWildcard | TagBits.HasMissingType | TagBits.ContainsNestedTypeReferences);
	
	if (nullTagBitsPerDimension != null) {
		this.tagBits |= nullTagBitsPerDimension[0]; // outer-most dimension
		this.nullTagBitsPerDimension = nullTagBitsPerDimension;
		this.tagBits |= TagBits.HasNullTypeAnnotation;
	}
}

public TypeBinding closestMatch() {
	if (isValidBinding()) {
		return this;
	}
	TypeBinding leafClosestMatch = this.leafComponentType.closestMatch();
	if (leafClosestMatch == null) {
		return null;
	}
	return this.environment.createArrayType(this.leafComponentType.closestMatch(), this.dimensions);
}

/**
 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#collectMissingTypes(java.util.List)
 */
public List collectMissingTypes(List missingTypes) {
	if ((this.tagBits & TagBits.HasMissingType) != 0) {
		missingTypes = this.leafComponentType.collectMissingTypes(missingTypes);
	}
	return missingTypes;
}

/**
 * Collect the substitutes into a map for certain type variables inside the receiver type
 * e.g.   Collection<T>.collectSubstitutes(Collection<List<X>>, Map), will populate Map with: T --> List<X>
 * Constraints:
 *   A << F   corresponds to:   F.collectSubstitutes(..., A, ..., CONSTRAINT_EXTENDS (1))
 *   A = F   corresponds to:      F.collectSubstitutes(..., A, ..., CONSTRAINT_EQUAL (0))
 *   A >> F   corresponds to:   F.collectSubstitutes(..., A, ..., CONSTRAINT_SUPER (2))
*/
public void collectSubstitutes(Scope scope, TypeBinding actualType, InferenceContext inferenceContext, int constraint) {

	if ((this.tagBits & TagBits.HasTypeVariable) == 0) return;
	if (actualType == TypeBinding.NULL || actualType.kind() == POLY_TYPE) return;

	switch(actualType.kind()) {
		case Binding.ARRAY_TYPE :
	        int actualDim = actualType.dimensions();
	        if (actualDim == this.dimensions) {
			    this.leafComponentType.collectSubstitutes(scope, actualType.leafComponentType(), inferenceContext, constraint);
	        } else if (actualDim > this.dimensions) {
	            ArrayBinding actualReducedType = this.environment.createArrayType(actualType.leafComponentType(), actualDim - this.dimensions);
	            this.leafComponentType.collectSubstitutes(scope, actualReducedType, inferenceContext, constraint);
	        }
			break;
		case Binding.TYPE_PARAMETER :
			//TypeVariableBinding variable = (TypeVariableBinding) otherType;
			// TODO (philippe) should consider array bounds, and recurse
			break;
	}
}

/*
 * brakets leafUniqueKey
 * p.X[][] --> [[Lp/X;
 */
public char[] computeUniqueKey(boolean isLeaf) {
	char[] brackets = new char[this.dimensions];
	for (int i = this.dimensions - 1; i >= 0; i--) brackets[i] = '[';
	return CharOperation.concat(brackets, this.leafComponentType.computeUniqueKey(isLeaf));
 }

/**
 * Answer the receiver's constant pool name.
 * NOTE: This method should only be used during/after code gen.
 * e.g. '[Ljava/lang/Object;'
 */
public char[] constantPoolName() {
	if (this.constantPoolName != null)
		return this.constantPoolName;

	char[] brackets = new char[this.dimensions];
	for (int i = this.dimensions - 1; i >= 0; i--) brackets[i] = '[';
	return this.constantPoolName = CharOperation.concat(brackets, this.leafComponentType.signature());
}
public String debugName() {
	StringBuffer brackets = new StringBuffer(this.dimensions * 2);
	for (int i = this.dimensions; --i >= 0;)
		brackets.append("[]"); //$NON-NLS-1$
	return this.leafComponentType.debugName() + brackets.toString();
}
public int dimensions() {
	return this.dimensions;
}

/* Answer an array whose dimension size is one less than the receiver.
*
* When the receiver's dimension size is one then answer the leaf component type.
*/

public TypeBinding elementsType() {
	long[] nullTagBitsSub = null;
	if (this.nullTagBitsPerDimension != null) {
		int len = this.nullTagBitsPerDimension.length-1;
		System.arraycopy(this.nullTagBitsPerDimension, 1, nullTagBitsSub = new long[len], 0, len);
	}
	if (this.dimensions == 1) {
		if (nullTagBitsSub != null && nullTagBitsSub[0] != 0L)
			return this.environment.createAnnotatedType(this.leafComponentType, nullTagBitsSub[0]);
		return this.leafComponentType;
	}
	return this.environment.createArrayType(this.leafComponentType, this.dimensions - 1, nullTagBitsSub);
}
/**
 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#erasure()
 */
public TypeBinding erasure() {
    TypeBinding erasedType = this.leafComponentType.erasure();
    if (this.leafComponentType != erasedType)
        return this.environment.createArrayType(erasedType, this.dimensions);
    return this;
}
public LookupEnvironment environment() {
    return this.environment;
}

public char[] genericTypeSignature() {

    if (this.genericTypeSignature == null) {
		char[] brackets = new char[this.dimensions];
		for (int i = this.dimensions - 1; i >= 0; i--) brackets[i] = '[';
		this.genericTypeSignature = CharOperation.concat(brackets, this.leafComponentType.genericTypeSignature());
    }
    return this.genericTypeSignature;
}

public PackageBinding getPackage() {
	return this.leafComponentType.getPackage();
}

public int hashCode() {
	return this.leafComponentType == null ? super.hashCode() : this.leafComponentType.hashCode();
}

/* Answer true if the receiver type can be assigned to the argument type (right)
*/
public boolean isCompatibleWith(TypeBinding otherType, Scope captureScope) {
	// disregard any type annotations on this and otherType
	// recursive call needed when this is annotated, unless the annotation was introduced on a declaration
	otherType = otherType.unannotated();
	TypeBinding unannotated = unannotated();
	if (unannotated != this)
		return unannotated.isCompatibleWith(otherType, captureScope);

	if (this == otherType)
		return true;

	switch (otherType.kind()) {
		case Binding.ARRAY_TYPE :
			ArrayBinding otherArray = (ArrayBinding) otherType;
			if (otherArray.leafComponentType.isBaseType())
				return false; // relying on the fact that all equal arrays are identical
			if (this.dimensions == otherArray.dimensions)
				return this.leafComponentType.isCompatibleWith(otherArray.leafComponentType);
			if (this.dimensions < otherArray.dimensions)
				return false; // cannot assign 'String[]' into 'Object[][]' but can assign 'byte[][]' into 'Object[]'
			break;
		case Binding.BASE_TYPE :
			return false;
		case Binding.WILDCARD_TYPE :
		case Binding.INTERSECTION_TYPE :
		    return ((WildcardBinding) otherType).boundCheck(this);

		case Binding.TYPE_PARAMETER :
			// check compatibility with capture of ? super X
			if (otherType.isCapture()) {
				CaptureBinding otherCapture = (CaptureBinding) otherType;
				TypeBinding otherLowerBound;
				if ((otherLowerBound = otherCapture.lowerBound) != null) {
					if (!otherLowerBound.isArrayType()) return false;
					return isCompatibleWith(otherLowerBound, captureScope);
				}
			}
			return false;

	}
	//Check dimensions - Java does not support explicitly sized dimensions for types.
	//However, if it did, the type checking support would go here.
	switch (otherType.leafComponentType().id) {
	    case TypeIds.T_JavaLangObject :
	    case TypeIds.T_JavaLangCloneable :
	    case TypeIds.T_JavaIoSerializable :
	        return true;
	}
	return false;
}

public int kind() {
	return ARRAY_TYPE;
}

public TypeBinding leafComponentType(){
	return this.leafComponentType;
}

public char[] nullAnnotatedReadableName(CompilerOptions options, boolean shortNames) /* java.lang.Object @o.e.j.a.NonNull[] */ {
	if (this.nullTagBitsPerDimension == null)
		return shortNames ? shortReadableName() : readableName();
	char[][] brackets = new char[this.dimensions][];
	for (int i = 0; i < this.dimensions; i++) {
		if ((this.nullTagBitsPerDimension[i] & TagBits.AnnotationNullMASK) != 0) {
			char[][] fqAnnotationName;
			if ((this.nullTagBitsPerDimension[i] & TagBits.AnnotationNonNull) != 0)
				fqAnnotationName = options.nonNullAnnotationName;
			else
				fqAnnotationName = options.nullableAnnotationName;
			char[] annotationName = shortNames 
										? fqAnnotationName[fqAnnotationName.length-1] 
										: CharOperation.concatWith(fqAnnotationName, '.');
			brackets[i] = new char[annotationName.length+3];
			brackets[i][0] = '@';
			System.arraycopy(annotationName, 0, brackets[i], 1, annotationName.length);
			brackets[i][annotationName.length+1] = '[';
			brackets[i][annotationName.length+2] = ']';
		} else {
			brackets[i] = new char[]{'[', ']'}; 
		}
	}
	char[] leafTypeName = shortNames ? this.leafComponentType.shortReadableName() : this.leafComponentType.readableName();
	return CharOperation.concat(leafTypeName, 
								 CharOperation.concatWith(brackets, ' '),
								 ' ');
}

/* API
* Answer the problem id associated with the receiver.
* NoError if the receiver is a valid binding.
*/
public int problemId() {
	return this.leafComponentType.problemId();
}
/**
* Answer the source name for the type.
* In the case of member types, as the qualified name from its top level type.
* For example, for a member type N defined inside M & A: "A.M.N".
*/

public char[] qualifiedSourceName() {
	char[] brackets = new char[this.dimensions * 2];
	for (int i = this.dimensions * 2 - 1; i >= 0; i -= 2) {
		brackets[i] = ']';
		brackets[i - 1] = '[';
	}
	return CharOperation.concat(this.leafComponentType.qualifiedSourceName(), brackets);
}
public char[] readableName() /* java.lang.Object[] */ {
	char[] brackets = new char[this.dimensions * 2];
	for (int i = this.dimensions * 2 - 1; i >= 0; i -= 2) {
		brackets[i] = ']';
		brackets[i - 1] = '[';
	}
	return CharOperation.concat(this.leafComponentType.readableName(), brackets);
}
public char[] shortReadableName(){
	char[] brackets = new char[this.dimensions * 2];
	for (int i = this.dimensions * 2 - 1; i >= 0; i -= 2) {
		brackets[i] = ']';
		brackets[i - 1] = '[';
	}
	return CharOperation.concat(this.leafComponentType.shortReadableName(), brackets);
}
public char[] sourceName() {
	char[] brackets = new char[this.dimensions * 2];
	for (int i = this.dimensions * 2 - 1; i >= 0; i -= 2) {
		brackets[i] = ']';
		brackets[i - 1] = '[';
	}
	return CharOperation.concat(this.leafComponentType.sourceName(), brackets);
}
public void swapUnresolved(UnresolvedReferenceBinding unresolvedType, ReferenceBinding resolvedType, LookupEnvironment env) {
	if (this.leafComponentType == unresolvedType) {
		this.leafComponentType = env.convertUnresolvedBinaryToRawType(resolvedType);
		this.tagBits |= this.leafComponentType.tagBits & (TagBits.HasTypeVariable | TagBits.HasDirectWildcard | TagBits.HasMissingType);
	}
}
public String toString() {
	return this.leafComponentType != null ? debugName() : "NULL TYPE ARRAY"; //$NON-NLS-1$
}
public TypeBinding unannotated() {
	if (this.nullTagBitsPerDimension == null)
		return this;
	return this.environment.createArrayType(this.leafComponentType, this.dimensions);
}
}
