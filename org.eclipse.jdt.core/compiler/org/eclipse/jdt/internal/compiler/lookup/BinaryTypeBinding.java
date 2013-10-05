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
 *     Stephan Herrmann - Contributions for
 *								bug 349326 - [1.7] new warning for missing try-with-resources
 *								bug 186342 - [compiler][null] Using annotations for null checking
 *								bug 364890 - BinaryTypeBinding should use char constants from Util
 *								bug 365387 - [compiler][null] bug 186342: Issues to follow up post review and verification.
 *								bug 358903 - Filter practically unimportant resource leak warnings
 *								bug 365531 - [compiler][null] investigate alternative strategy for internally encoding nullness defaults
 *								bug 388800 - [1.8][compiler] detect default methods in class files
 *								bug 388281 - [compiler][null] inheritance of null annotations as an option
 *								bug 331649 - [compiler][null] consider null annotations for fields
 *								bug 392384 - [1.8][compiler][null] Restore nullness info from type annotations in class files
 *								Bug 392099 - [1.8][compiler][null] Apply null annotation on types for null analysis
 *								Bug 415043 - [1.8][null] Follow-up re null type annotations after bug 392099
 *								Bug 415850 - [1.8] Ensure RunJDTCoreTests can cope with null annotations enabled
 *								Bug 417295 - [1.8[[null] Massage type annotated null analysis to gel well with deep encoded type bindings.
 *    Jesper Steen Moller - Contributions for
 *								Bug 412150 [1.8] [compiler] Enable reflected parameter names during annotation processing
 *								Bug 412153 - [1.8][compiler] Check validity of annotations which may be repeatable
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import java.util.ArrayList;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.classfmt.TypeAnnotationWalker;
import org.eclipse.jdt.internal.compiler.codegen.ConstantPool;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.impl.BooleanConstant;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.util.SimpleLookupTable;
import org.eclipse.jdt.internal.compiler.util.Util;

/*
Not all fields defined by this type are initialized when it is created.
Some are initialized only when needed.

Accessors have been provided for some public fields so all TypeBindings have the same API...
but access public fields directly whenever possible.
Non-public fields have accessors which should be used everywhere you expect the field to be initialized.

null is NOT a valid value for a non-public field... it just means the field is not initialized.
*/

public class BinaryTypeBinding extends ReferenceBinding {

	private static final IBinaryMethod[] NO_BINARY_METHODS = new IBinaryMethod[0];

	// all of these fields are ONLY guaranteed to be initialized if accessed using their public accessor method
	protected ReferenceBinding superclass;
	protected ReferenceBinding enclosingType;
	protected ReferenceBinding[] superInterfaces;
	protected FieldBinding[] fields;
	protected MethodBinding[] methods;
	protected ReferenceBinding[] memberTypes;
	protected TypeVariableBinding[] typeVariables;
	protected BinaryTypeBinding prototype;

	// For the link with the principle structure
	protected LookupEnvironment environment;

	protected SimpleLookupTable storedAnnotations = null; // keys are this ReferenceBinding & its fields and methods, value is an AnnotationHolder

	private ReferenceBinding containingAnnotation;

static Object convertMemberValue(Object binaryValue, LookupEnvironment env, char[][][] missingTypeNames) {
	if (binaryValue == null) return null;
	if (binaryValue instanceof Constant)
		return binaryValue;
	if (binaryValue instanceof ClassSignature)
		return env.getTypeFromSignature(((ClassSignature) binaryValue).getTypeName(), 0, -1, false, null, missingTypeNames, TypeAnnotationWalker.EMPTY_ANNOTATION_WALKER);
	if (binaryValue instanceof IBinaryAnnotation)
		return createAnnotation((IBinaryAnnotation) binaryValue, env, missingTypeNames);
	if (binaryValue instanceof EnumConstantSignature) {
		EnumConstantSignature ref = (EnumConstantSignature) binaryValue;
		ReferenceBinding enumType = (ReferenceBinding) env.getTypeFromSignature(ref.getTypeName(), 0, -1, false, null, missingTypeNames, TypeAnnotationWalker.EMPTY_ANNOTATION_WALKER);
		enumType = (ReferenceBinding) resolveType(enumType, env, false /* no raw conversion */);
		return enumType.getField(ref.getEnumConstantName(), false);
	}
	if (binaryValue instanceof Object[]) {
		Object[] objects = (Object[]) binaryValue;
		int length = objects.length;
		if (length == 0) return objects;
		Object[] values = new Object[length];
		for (int i = 0; i < length; i++)
			values[i] = convertMemberValue(objects[i], env, missingTypeNames);
		return values;
	}

	// should never reach here.
	throw new IllegalStateException();
}

public TypeBinding clone(TypeBinding outerType) {
	BinaryTypeBinding copy = new BinaryTypeBinding(this);
	
	copy.enclosingType = (ReferenceBinding) outerType;
	if (outerType instanceof UnresolvedReferenceBinding)
		copy.tagBits |= TagBits.HasUnresolvedEnclosingType;
	else 
		copy.tagBits &= ~TagBits.HasUnresolvedEnclosingType;
	
	return copy;
}

static AnnotationBinding createAnnotation(IBinaryAnnotation annotationInfo, LookupEnvironment env, char[][][] missingTypeNames) {
	IBinaryElementValuePair[] binaryPairs = annotationInfo.getElementValuePairs();
	int length = binaryPairs == null ? 0 : binaryPairs.length;
	ElementValuePair[] pairs = length == 0 ? Binding.NO_ELEMENT_VALUE_PAIRS : new ElementValuePair[length];
	for (int i = 0; i < length; i++)
		pairs[i] = new ElementValuePair(binaryPairs[i].getName(), convertMemberValue(binaryPairs[i].getValue(), env, missingTypeNames), null);

	char[] typeName = annotationInfo.getTypeName();
	ReferenceBinding annotationType = env.getTypeFromConstantPoolName(typeName, 1, typeName.length - 1, false, missingTypeNames);
	return new UnresolvedAnnotationBinding(annotationType, pairs, env);
}

public static AnnotationBinding[] createAnnotations(IBinaryAnnotation[] annotationInfos, LookupEnvironment env, char[][][] missingTypeNames) {
	int length = annotationInfos == null ? 0 : annotationInfos.length;
	AnnotationBinding[] result = length == 0 ? Binding.NO_ANNOTATIONS : new AnnotationBinding[length];
	for (int i = 0; i < length; i++)
		result[i] = createAnnotation(annotationInfos[i], env, missingTypeNames);
	return result;
}

public static TypeBinding resolveType(TypeBinding type, LookupEnvironment environment, boolean convertGenericToRawType) {
	switch (type.kind()) {
		case Binding.PARAMETERIZED_TYPE :
			((ParameterizedTypeBinding) type).resolve();
			break;

		case Binding.WILDCARD_TYPE :
		case Binding.INTERSECTION_TYPE :
			return ((WildcardBinding) type).resolve();

		case Binding.ARRAY_TYPE :
			resolveType(((ArrayBinding) type).leafComponentType, environment, convertGenericToRawType);
			break;

		case Binding.TYPE_PARAMETER :
			((TypeVariableBinding) type).resolve();
			break;

		case Binding.GENERIC_TYPE :
			if (convertGenericToRawType) // raw reference to generic ?
				return environment.convertUnresolvedBinaryToRawType(type);
			break;

		default:
			if (type instanceof UnresolvedReferenceBinding)
				return ((UnresolvedReferenceBinding) type).resolve(environment, convertGenericToRawType);
			if (convertGenericToRawType) // raw reference to generic ?
				return environment.convertUnresolvedBinaryToRawType(type);
			break;
	}
	return type;
}

/**
 * Default empty constructor for subclasses only.
 */
protected BinaryTypeBinding() {
	// only for subclasses
	this.prototype = this;
}

public BinaryTypeBinding(BinaryTypeBinding prototype) {
	super(prototype);
	this.superclass = prototype.superclass;
	this.enclosingType = prototype.enclosingType;
	this.superInterfaces = prototype.superInterfaces;
	this.fields = prototype.fields;
	this.methods = prototype.methods;
	this.memberTypes = prototype.memberTypes;
	this.typeVariables = prototype.typeVariables;
	this.prototype = prototype.prototype;
	this.environment = prototype.environment;
	this.storedAnnotations = prototype.storedAnnotations;
}
/**
 * Standard constructor for creating binary type bindings from binary models (classfiles)
 * @param packageBinding
 * @param binaryType
 * @param environment
 */
public BinaryTypeBinding(PackageBinding packageBinding, IBinaryType binaryType, LookupEnvironment environment) {
	
	this.prototype = this;
	this.compoundName = CharOperation.splitOn('/', binaryType.getName());
	computeId();

	this.tagBits |= TagBits.IsBinaryBinding;
	this.environment = environment;
	this.fPackage = packageBinding;
	this.fileName = binaryType.getFileName();

	/* https://bugs.eclipse.org/bugs/show_bug.cgi?id=324850, even in a 1.4 project, we
	   must internalize type variables and observe any parameterization of super class
	   and/or super interfaces in order to be able to detect overriding in the presence
	   of generics.
	 */
	char[] typeSignature = binaryType.getGenericSignature();
	this.typeVariables = typeSignature != null && typeSignature.length > 0 && typeSignature[0] == Util.C_GENERIC_START
		? null // is initialized in cachePartsFrom (called from LookupEnvironment.createBinaryTypeFrom())... must set to null so isGenericType() answers true
		: Binding.NO_TYPE_VARIABLES;

	this.sourceName = binaryType.getSourceName();
	this.modifiers = binaryType.getModifiers();

	if ((binaryType.getTagBits() & TagBits.HierarchyHasProblems) != 0)
		this.tagBits |= TagBits.HierarchyHasProblems;

	if (binaryType.isAnonymous()) {
		this.tagBits |= TagBits.AnonymousTypeMask;
	} else if (binaryType.isLocal()) {
		this.tagBits |= TagBits.LocalTypeMask;
	} else if (binaryType.isMember()) {
		this.tagBits |= TagBits.MemberTypeMask;
	}
	// need enclosing type to access type variables
	char[] enclosingTypeName = binaryType.getEnclosingTypeName();
	if (enclosingTypeName != null) {
		// attempt to find the enclosing type if it exists in the cache (otherwise - resolve it when requested)
		this.enclosingType = environment.getTypeFromConstantPoolName(enclosingTypeName, 0, -1, true, null /* could not be missing */); // pretend parameterized to avoid raw
		this.tagBits |= TagBits.MemberTypeMask;   // must be a member type not a top-level or local type
		this.tagBits |= TagBits.HasUnresolvedEnclosingType;
		if (enclosingType().isStrictfp())
			this.modifiers |= ClassFileConstants.AccStrictfp;
		if (enclosingType().isDeprecated())
			this.modifiers |= ExtraCompilerModifiers.AccDeprecatedImplicitly;
	}
}

/**
 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#availableFields()
 */
public FieldBinding[] availableFields() {
	
	if (this != this.prototype)
		return this.prototype.availableFields();
	
	if ((this.tagBits & TagBits.AreFieldsComplete) != 0)
		return this.fields;

	// lazily sort fields
	if ((this.tagBits & TagBits.AreFieldsSorted) == 0) {
		int length = this.fields.length;
		if (length > 1)
			ReferenceBinding.sortFields(this.fields, 0, length);
		this.tagBits |= TagBits.AreFieldsSorted;
	}
	FieldBinding[] availableFields = new FieldBinding[this.fields.length];
	int count = 0;
	for (int i = 0; i < this.fields.length; i++) {
		try {
			availableFields[count] = resolveTypeFor(this.fields[i]);
			count++;
		} catch (AbortCompilation a){
			// silent abort
		}
	}
	if (count < availableFields.length)
		System.arraycopy(availableFields, 0, availableFields = new FieldBinding[count], 0, count);
	return availableFields;
}

private TypeVariableBinding[] addMethodTypeVariables(TypeVariableBinding[] methodTypeVars) {
	if (this.typeVariables == null || this.typeVariables == Binding.NO_TYPE_VARIABLES) {
		return methodTypeVars;
	} 
	if (methodTypeVars == null || methodTypeVars == Binding.NO_TYPE_VARIABLES) {
		return this.typeVariables;
	}
	// uniq-merge both the arrays
	int total = this.typeVariables.length + methodTypeVars.length;
	TypeVariableBinding[] combinedTypeVars = new TypeVariableBinding[total];
	System.arraycopy(this.typeVariables, 0, combinedTypeVars, 0, this.typeVariables.length);
	int size = this.typeVariables.length;
	loop: for (int i = 0, len = methodTypeVars.length; i < len; i++) {
		for (int j = this.typeVariables.length -1 ; j >= 0; j--) {
			if (CharOperation.equals(methodTypeVars[i].sourceName, this.typeVariables[j].sourceName))
				continue loop;
		}
		combinedTypeVars[size++] = methodTypeVars[i];
	}
	if (size != total) {
		System.arraycopy(combinedTypeVars, 0, combinedTypeVars = new TypeVariableBinding[size], 0, size);
	}
	return combinedTypeVars;
}

/**
 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#availableMethods()
 */
public MethodBinding[] availableMethods() {
	
	if (this != this.prototype)
		return this.prototype.availableMethods();

	if ((this.tagBits & TagBits.AreMethodsComplete) != 0)
		return this.methods;

	// lazily sort methods
	if ((this.tagBits & TagBits.AreMethodsSorted) == 0) {
		int length = this.methods.length;
		if (length > 1)
			ReferenceBinding.sortMethods(this.methods, 0, length);
		this.tagBits |= TagBits.AreMethodsSorted;
	}
	MethodBinding[] availableMethods = new MethodBinding[this.methods.length];
	int count = 0;
	for (int i = 0; i < this.methods.length; i++) {
		try {
			availableMethods[count] = resolveTypesFor(this.methods[i]);
			count++;
		} catch (AbortCompilation a){
			// silent abort
		}
	}
	if (count < availableMethods.length)
		System.arraycopy(availableMethods, 0, availableMethods = new MethodBinding[count], 0, count);
	return availableMethods;
}

void cachePartsFrom(IBinaryType binaryType, boolean needFieldsAndMethods) {
	try {
		// default initialization for super-interfaces early, in case some aborting compilation error occurs,
		// and still want to use binaries passed that point (e.g. type hierarchy resolver, see bug 63748).
		this.typeVariables = Binding.NO_TYPE_VARIABLES;
		this.superInterfaces = Binding.NO_SUPERINTERFACES;

		// must retrieve member types in case superclass/interfaces need them
		this.memberTypes = Binding.NO_MEMBER_TYPES;
		IBinaryNestedType[] memberTypeStructures = binaryType.getMemberTypes();
		if (memberTypeStructures != null) {
			int size = memberTypeStructures.length;
			if (size > 0) {
				this.memberTypes = new ReferenceBinding[size];
				for (int i = 0; i < size; i++) {
					// attempt to find each member type if it exists in the cache (otherwise - resolve it when requested)
					this.memberTypes[i] = this.environment.getTypeFromConstantPoolName(memberTypeStructures[i].getName(), 0, -1, false, null /* could not be missing */);
				}
				this.tagBits |= TagBits.HasUnresolvedMemberTypes;
			}
		}

		CompilerOptions globalOptions = this.environment.globalOptions;
		long sourceLevel = globalOptions.originalSourceLevel;
		/* https://bugs.eclipse.org/bugs/show_bug.cgi?id=324850, even in a 1.4 project, we
		   must internalize type variables and observe any parameterization of super class
		   and/or super interfaces in order to be able to detect overriding in the presence
		   of generics.
		 */
		TypeAnnotationWalker walker = getTypeAnnotationWalker(binaryType.getTypeAnnotations());
		char[] typeSignature = binaryType.getGenericSignature(); // use generic signature even in 1.4
		this.tagBits |= binaryType.getTagBits();
		
		char[][][] missingTypeNames = binaryType.getMissingTypeNames();
		SignatureWrapper wrapper = null;
		if (typeSignature != null) {
			// ClassSignature = ParameterPart(optional) super_TypeSignature interface_signature
			wrapper = new SignatureWrapper(typeSignature);
			if (wrapper.signature[wrapper.start] == Util.C_GENERIC_START) {
				// ParameterPart = '<' ParameterSignature(s) '>'
				wrapper.start++; // skip '<'
				this.typeVariables = createTypeVariables(wrapper, true, missingTypeNames, walker, true/*class*/);
				wrapper.start++; // skip '>'
				this.tagBits |=  TagBits.HasUnresolvedTypeVariables;
				this.modifiers |= ExtraCompilerModifiers.AccGenericSignature;
			}
		}
		TypeVariableBinding[] typeVars = Binding.NO_TYPE_VARIABLES;
		char[] methodDescriptor = binaryType.getEnclosingMethod();
		if (methodDescriptor != null) {
			MethodBinding enclosingMethod = findMethod(methodDescriptor, missingTypeNames);
			if (enclosingMethod != null) {
				typeVars = enclosingMethod.typeVariables;
				this.typeVariables = addMethodTypeVariables(typeVars);			
			}
		}
		if (typeSignature == null)  {
			char[] superclassName = binaryType.getSuperclassName();
			if (superclassName != null) {
				// attempt to find the superclass if it exists in the cache (otherwise - resolve it when requested)
				this.superclass = this.environment.getTypeFromConstantPoolName(superclassName, 0, -1, false, missingTypeNames);
				this.tagBits |= TagBits.HasUnresolvedSuperclass;
			}

			this.superInterfaces = Binding.NO_SUPERINTERFACES;
			char[][] interfaceNames = binaryType.getInterfaceNames();
			if (interfaceNames != null) {
				int size = interfaceNames.length;
				if (size > 0) {
					this.superInterfaces = new ReferenceBinding[size];
					for (int i = 0; i < size; i++)
						// attempt to find each superinterface if it exists in the cache (otherwise - resolve it when requested)
						this.superInterfaces[i] = this.environment.getTypeFromConstantPoolName(interfaceNames[i], 0, -1, false, missingTypeNames);
					this.tagBits |= TagBits.HasUnresolvedSuperinterfaces;
				}
			}
		} else {
			// attempt to find the superclass if it exists in the cache (otherwise - resolve it when requested)
			this.superclass = (ReferenceBinding) this.environment.getTypeFromTypeSignature(wrapper, typeVars, this, missingTypeNames, 
																		walker.toSupertype((short) -1));
			this.tagBits |= TagBits.HasUnresolvedSuperclass;

			this.superInterfaces = Binding.NO_SUPERINTERFACES;
			if (!wrapper.atEnd()) {
				// attempt to find each superinterface if it exists in the cache (otherwise - resolve it when requested)
				java.util.ArrayList types = new java.util.ArrayList(2);
				short rank = 0;
				do {
					types.add(this.environment.getTypeFromTypeSignature(wrapper, typeVars, this, missingTypeNames, walker.toSupertype(rank++)));
				} while (!wrapper.atEnd());
				this.superInterfaces = new ReferenceBinding[types.size()];
				types.toArray(this.superInterfaces);
				this.tagBits |= TagBits.HasUnresolvedSuperinterfaces;
			}
		}

		if (needFieldsAndMethods) {
			IBinaryField[] iFields = binaryType.getFields();
			createFields(iFields, sourceLevel, missingTypeNames);
			IBinaryMethod[] iMethods = createMethods(binaryType.getMethods(), sourceLevel, missingTypeNames);
			boolean isViewedAsDeprecated = isViewedAsDeprecated();
			if (isViewedAsDeprecated) {
				for (int i = 0, max = this.fields.length; i < max; i++) {
					FieldBinding field = this.fields[i];
					if (!field.isDeprecated()) {
						field.modifiers |= ExtraCompilerModifiers.AccDeprecatedImplicitly;
					}
				}
				for (int i = 0, max = this.methods.length; i < max; i++) {
					MethodBinding method = this.methods[i];
					if (!method.isDeprecated()) {
						method.modifiers |= ExtraCompilerModifiers.AccDeprecatedImplicitly;
					}
				}
			}
			if (this.environment.globalOptions.isAnnotationBasedNullAnalysisEnabled) {
				// need annotations on the type before processing null annotations on members respecting any @NonNullByDefault:
				scanTypeForNullDefaultAnnotation(binaryType, this.fPackage, this);

				if (iFields != null) {
					for (int i = 0; i < iFields.length; i++)
						scanFieldForNullAnnotation(iFields[i], this.fields[i]);
				}
				if (iMethods != null) {
					for (int i = 0; i < iMethods.length; i++)
						scanMethodForNullAnnotation(iMethods[i], this.methods[i]);
				}
			}
		}
		if (this.environment.globalOptions.storeAnnotations)
			setAnnotations(createAnnotations(binaryType.getAnnotations(), this.environment, missingTypeNames));
		if (this.isAnnotationType())
			scanTypeForContainerAnnotation(binaryType, missingTypeNames);
	} finally {
		// protect against incorrect use of the needFieldsAndMethods flag, see 48459
		if (this.fields == null)
			this.fields = Binding.NO_FIELDS;
		if (this.methods == null)
			this.methods = Binding.NO_METHODS;
	}
}

private TypeAnnotationWalker getTypeAnnotationWalker(IBinaryTypeAnnotation[] annotations) {
	if (annotations == null || annotations.length == 0)
		return TypeAnnotationWalker.EMPTY_ANNOTATION_WALKER;
	
	return this.environment.usesAnnotatedTypeSystem() ? new TypeAnnotationWalker(annotations) : TypeAnnotationWalker.EMPTY_ANNOTATION_WALKER;
}

private void createFields(IBinaryField[] iFields, long sourceLevel, char[][][] missingTypeNames) {
	this.fields = Binding.NO_FIELDS;
	if (iFields != null) {
		int size = iFields.length;
		if (size > 0) {
			this.fields = new FieldBinding[size];
			boolean use15specifics = sourceLevel >= ClassFileConstants.JDK1_5;
			boolean hasRestrictedAccess = hasRestrictedAccess();
			int firstAnnotatedFieldIndex = -1;
			for (int i = 0; i < size; i++) {
				IBinaryField binaryField = iFields[i];
				char[] fieldSignature = use15specifics ? binaryField.getGenericSignature() : null;
				TypeAnnotationWalker walker = getTypeAnnotationWalker(binaryField.getTypeAnnotations()).toField();
				TypeBinding type = fieldSignature == null
					? this.environment.getTypeFromSignature(binaryField.getTypeName(), 0, -1, false, this, missingTypeNames, walker)
					: this.environment.getTypeFromTypeSignature(new SignatureWrapper(fieldSignature), Binding.NO_TYPE_VARIABLES, this, missingTypeNames, walker);
				FieldBinding field =
					new FieldBinding(
						binaryField.getName(),
						type,
						binaryField.getModifiers() | ExtraCompilerModifiers.AccUnresolved,
						this,
						binaryField.getConstant());
				if (firstAnnotatedFieldIndex < 0
						&& this.environment.globalOptions.storeAnnotations
						&& binaryField.getAnnotations() != null) {
					firstAnnotatedFieldIndex = i;
				}
				field.id = i; // ordinal
				if (use15specifics)
					field.tagBits |= binaryField.getTagBits();
				if (hasRestrictedAccess)
					field.modifiers |= ExtraCompilerModifiers.AccRestrictedAccess;
				if (fieldSignature != null)
					field.modifiers |= ExtraCompilerModifiers.AccGenericSignature;
				this.fields[i] = field;
			}
			// second pass for reifying annotations, since may refer to fields being constructed (147875)
			if (firstAnnotatedFieldIndex >= 0) {
				for (int i = firstAnnotatedFieldIndex; i <size; i++) {
					IBinaryField binaryField = iFields[i];
					this.fields[i].setAnnotations(createAnnotations(binaryField.getAnnotations(), this.environment, missingTypeNames));
				}
			}
		}
	}
}

private MethodBinding createMethod(IBinaryMethod method, long sourceLevel, char[][][] missingTypeNames) {
	int methodModifiers = method.getModifiers() | ExtraCompilerModifiers.AccUnresolved;
	if (sourceLevel < ClassFileConstants.JDK1_5)
		methodModifiers &= ~ClassFileConstants.AccVarargs; // vararg methods are not recognized until 1.5
	if (isInterface() && (methodModifiers & ClassFileConstants.AccAbstract) == 0) {
		// see https://bugs.eclipse.org/388954
		if (sourceLevel >= ClassFileConstants.JDK1_8)
			methodModifiers |= ExtraCompilerModifiers.AccDefaultMethod;
		else
			methodModifiers |= ClassFileConstants.AccAbstract;
	}
	ReferenceBinding[] exceptions = Binding.NO_EXCEPTIONS;
	TypeBinding[] parameters = Binding.NO_PARAMETERS;
	TypeVariableBinding[] typeVars = Binding.NO_TYPE_VARIABLES;
	AnnotationBinding[][] paramAnnotations = null;
	TypeBinding returnType = null;

	char[][] argumentNames = method.getArgumentNames();

	final boolean use15specifics = sourceLevel >= ClassFileConstants.JDK1_5;
	/* https://bugs.eclipse.org/bugs/show_bug.cgi?id=324850, Since a 1.4 project can have a 1.5
	   type as a super type and the 1.5 type could be generic, we must internalize usages of type
	   variables properly in order to be able to apply substitutions and thus be able to detect
	   overriding in the presence of generics. Seeing the erased form is not good enough.
	 */
	TypeAnnotationWalker walker = getTypeAnnotationWalker(method.getTypeAnnotations());
	char[] methodSignature = method.getGenericSignature(); // always use generic signature, even in 1.4
	if (methodSignature == null) { // no generics
		char[] methodDescriptor = method.getMethodDescriptor();   // of the form (I[Ljava/jang/String;)V
		int numOfParams = 0;
		char nextChar;
		int index = 0; // first character is always '(' so skip it
		while ((nextChar = methodDescriptor[++index]) != Util.C_PARAM_END) {
			if (nextChar != Util.C_ARRAY) {
				numOfParams++;
				if (nextChar == Util.C_RESOLVED)
					while ((nextChar = methodDescriptor[++index]) != Util.C_NAME_END){/*empty*/}
			}
		}

		// Ignore synthetic argument for member types or enum types.
		int startIndex = 0;
		if (method.isConstructor()) {
			if (isMemberType() && !isStatic()) {
				// enclosing type
				startIndex++;
			}
			if (isEnum()) {
				// synthetic arguments (String, int)
				startIndex += 2;
			}
		}
		int size = numOfParams - startIndex;
		if (size > 0) {
			parameters = new TypeBinding[size];
			if (this.environment.globalOptions.storeAnnotations)
				paramAnnotations = new AnnotationBinding[size][];
			index = 1;
			short visibleIdx = 0;
			int end = 0;   // first character is always '(' so skip it
			for (int i = 0; i < numOfParams; i++) {
				while ((nextChar = methodDescriptor[++end]) == Util.C_ARRAY){/*empty*/}
				if (nextChar == Util.C_RESOLVED)
					while ((nextChar = methodDescriptor[++end]) != Util.C_NAME_END){/*empty*/}

				if (i >= startIndex) {   // skip the synthetic arg if necessary
					parameters[i - startIndex] = this.environment.getTypeFromSignature(methodDescriptor, index, end, false, this, missingTypeNames, walker.toMethodParameter(visibleIdx++));
					// 'paramAnnotations' line up with 'parameters'
					// int parameter to method.getParameterAnnotations() include the synthetic arg
					if (paramAnnotations != null)
						paramAnnotations[i - startIndex] = createAnnotations(method.getParameterAnnotations(i - startIndex), this.environment, missingTypeNames);
				}
				index = end + 1;
			}
		}

		char[][] exceptionTypes = method.getExceptionTypeNames();
		if (exceptionTypes != null) {
			size = exceptionTypes.length;
			if (size > 0) {
				exceptions = new ReferenceBinding[size];
				for (int i = 0; i < size; i++)
					exceptions[i] = this.environment.getTypeFromConstantPoolName(exceptionTypes[i], 0, -1, false, missingTypeNames);
			}
		}

		if (!method.isConstructor())
			returnType = this.environment.getTypeFromSignature(methodDescriptor, index + 1, -1, false, this, missingTypeNames, walker.toMethodReturn());   // index is currently pointing at the ')'
		
		final int argumentNamesLength = argumentNames == null ? 0 : argumentNames.length;
		if (startIndex > 0 && argumentNamesLength > 0) {
			// We'll have to slice the starting arguments off
			if (startIndex >= argumentNamesLength) {
				argumentNames = Binding.NO_PARAMETER_NAMES; // We know nothing about the argument names
			} else {
				char[][] slicedArgumentNames = new char[argumentNamesLength - startIndex][];
				System.arraycopy(argumentNames, startIndex, slicedArgumentNames, 0, argumentNamesLength - startIndex);
				argumentNames = slicedArgumentNames;
			}
		}

	} else {
		methodModifiers |= ExtraCompilerModifiers.AccGenericSignature;
		// MethodTypeSignature = ParameterPart(optional) '(' TypeSignatures ')' return_typeSignature ['^' TypeSignature (optional)]
		SignatureWrapper wrapper = new SignatureWrapper(methodSignature, use15specifics);
		if (wrapper.signature[wrapper.start] == Util.C_GENERIC_START) {
			// <A::Ljava/lang/annotation/Annotation;>(Ljava/lang/Class<TA;>;)TA;
			// ParameterPart = '<' ParameterSignature(s) '>'
			wrapper.start++; // skip '<'
			typeVars = createTypeVariables(wrapper, false, missingTypeNames, walker, false/*class*/);
			wrapper.start++; // skip '>'
		}

// 		Note(stephan): currently the compiler is not interested in retrieving receiver annotations, here is how we would do it:
//		IBinaryAnnotation[] receiverAnnotations = walker.toReceiver().getAnnotationsAtCursor();
		
		if (wrapper.signature[wrapper.start] == Util.C_PARAM_START) {
			wrapper.start++; // skip '('
			if (wrapper.signature[wrapper.start] == Util.C_PARAM_END) {
				wrapper.start++; // skip ')'
			} else {
				java.util.ArrayList types = new java.util.ArrayList(2);
				short rank = 0;
				while (wrapper.signature[wrapper.start] != Util.C_PARAM_END)
					types.add(this.environment.getTypeFromTypeSignature(wrapper, typeVars, this, missingTypeNames, walker.toMethodParameter(rank++)));
				wrapper.start++; // skip ')'
				int numParam = types.size();
				parameters = new TypeBinding[numParam];
				types.toArray(parameters);
				if (this.environment.globalOptions.storeAnnotations) {
					paramAnnotations = new AnnotationBinding[numParam][];
					for (int i = 0; i < numParam; i++)
						paramAnnotations[i] = createAnnotations(method.getParameterAnnotations(i), this.environment, missingTypeNames);
				}
			}
		}

		// always retrieve return type (for constructors, its V for void - will be ignored)
		returnType = this.environment.getTypeFromTypeSignature(wrapper, typeVars, this, missingTypeNames, walker.toMethodReturn());

		if (!wrapper.atEnd() && wrapper.signature[wrapper.start] == Util.C_EXCEPTION_START) {
			// attempt to find each exception if it exists in the cache (otherwise - resolve it when requested)
			java.util.ArrayList types = new java.util.ArrayList(2);
			int excRank = 0;
			do {
				wrapper.start++; // skip '^'
				types.add(this.environment.getTypeFromTypeSignature(wrapper, typeVars, this, missingTypeNames,
					walker.toThrows(excRank++)));
			} while (!wrapper.atEnd() && wrapper.signature[wrapper.start] == Util.C_EXCEPTION_START);
			exceptions = new ReferenceBinding[types.size()];
			types.toArray(exceptions);
		} else { // get the exceptions the old way
			char[][] exceptionTypes = method.getExceptionTypeNames();
			if (exceptionTypes != null) {
				int size = exceptionTypes.length;
				if (size > 0) {
					exceptions = new ReferenceBinding[size];
					for (int i = 0; i < size; i++)
						exceptions[i] = this.environment.getTypeFromConstantPoolName(exceptionTypes[i], 0, -1, false, missingTypeNames);
				}
			}
		}
	}

	MethodBinding result = method.isConstructor()
		? new MethodBinding(methodModifiers, parameters, exceptions, this)
		: new MethodBinding(methodModifiers, method.getSelector(), returnType, parameters, exceptions, this);
	if (this.environment.globalOptions.storeAnnotations)
		result.setAnnotations(
			createAnnotations(method.getAnnotations(), this.environment, missingTypeNames),
			paramAnnotations,
			isAnnotationType() ? convertMemberValue(method.getDefaultValue(), this.environment, missingTypeNames) : null,
			this.environment);

	if (argumentNames != null) result.parameterNames = argumentNames;
	
	if (use15specifics)
		result.tagBits |= method.getTagBits();
	result.typeVariables = typeVars;
	// fixup the declaring element of the type variable
	for (int i = 0, length = typeVars.length; i < length; i++)
		typeVars[i].declaringElement = result;

	return result;
}

/**
 * Create method bindings for binary type, filtering out <clinit> and synthetics
 * As some iMethods may be ignored in this process we return the matching array of those
 * iMethods for which MethodBindings have been created; indices match those in this.methods.
 */
private IBinaryMethod[] createMethods(IBinaryMethod[] iMethods, long sourceLevel, char[][][] missingTypeNames) {
	int total = 0, initialTotal = 0, iClinit = -1;
	int[] toSkip = null;
	if (iMethods != null) {
		total = initialTotal = iMethods.length;
		boolean keepBridgeMethods = sourceLevel < ClassFileConstants.JDK1_5; // https://bugs.eclipse.org/bugs/show_bug.cgi?id=330347
		for (int i = total; --i >= 0;) {
			IBinaryMethod method = iMethods[i];
			if ((method.getModifiers() & ClassFileConstants.AccSynthetic) != 0) {
				if (keepBridgeMethods && (method.getModifiers() & ClassFileConstants.AccBridge) != 0)
					continue; // want to see bridge methods as real methods
				// discard synthetics methods
				if (toSkip == null) toSkip = new int[iMethods.length];
				toSkip[i] = -1;
				total--;
			} else if (iClinit == -1) {
				char[] methodName = method.getSelector();
				if (methodName.length == 8 && methodName[0] == Util.C_GENERIC_START) {
					// discard <clinit>
					iClinit = i;
					total--;
				}
			}
		}
	}
	if (total == 0) {
		this.methods = Binding.NO_METHODS;
		return NO_BINARY_METHODS;
	}

	boolean hasRestrictedAccess = hasRestrictedAccess();
	this.methods = new MethodBinding[total];
	if (total == initialTotal) {
		for (int i = 0; i < initialTotal; i++) {
			MethodBinding method = createMethod(iMethods[i], sourceLevel, missingTypeNames);
			if (hasRestrictedAccess)
				method.modifiers |= ExtraCompilerModifiers.AccRestrictedAccess;
			this.methods[i] = method;
		}
		return iMethods;
	} else {
		IBinaryMethod[] mappedBinaryMethods = new IBinaryMethod[total];
		for (int i = 0, index = 0; i < initialTotal; i++) {
			if (iClinit != i && (toSkip == null || toSkip[i] != -1)) {
				MethodBinding method = createMethod(iMethods[i], sourceLevel, missingTypeNames);
				if (hasRestrictedAccess)
					method.modifiers |= ExtraCompilerModifiers.AccRestrictedAccess;
				mappedBinaryMethods[index] = iMethods[i];
				this.methods[index++] = method;
			}
		}
		return mappedBinaryMethods;
	}
}

private TypeVariableBinding[] createTypeVariables(SignatureWrapper wrapper, boolean assignVariables, char[][][] missingTypeNames,
													TypeAnnotationWalker walker, boolean isClassTypeParameter)
{
	// detect all type variables first
	char[] typeSignature = wrapper.signature;
	int depth = 0, length = typeSignature.length;
	int rank = 0;
	ArrayList variables = new ArrayList(1);
	depth = 0;
	boolean pendingVariable = true;
	createVariables: {
		for (int i = 1; i < length; i++) {
			switch(typeSignature[i]) {
				case Util.C_GENERIC_START :
					depth++;
					break;
				case Util.C_GENERIC_END :
					if (--depth < 0)
						break createVariables;
					break;
				case Util.C_NAME_END :
					if ((depth == 0) && (i +1 < length) && (typeSignature[i+1] != Util.C_COLON))
						pendingVariable = true;
					break;
				default:
					if (pendingVariable) {
						pendingVariable = false;
						int colon = CharOperation.indexOf(Util.C_COLON, typeSignature, i);
						char[] variableName = CharOperation.subarray(typeSignature, i, colon);
						TypeVariableBinding typeVariable = new TypeVariableBinding(variableName, this, rank, this.environment);
						AnnotationBinding [] annotations = BinaryTypeBinding.createAnnotations(walker.toTypeParameter(isClassTypeParameter, rank++).getAnnotationsAtCursor(), 
																										this.environment, missingTypeNames);
						if (annotations != null && annotations != Binding.NO_ANNOTATIONS)
							typeVariable.setTypeAnnotations(annotations, this.environment.globalOptions.isAnnotationBasedNullAnalysisEnabled);
						variables.add(typeVariable);
					}
			}
		}
	}
	// initialize type variable bounds - may refer to forward variables
	TypeVariableBinding[] result;
	variables.toArray(result = new TypeVariableBinding[rank]);
	// when creating the type variables for a type, the type must remember them before initializing each variable
	// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=163680
	if (assignVariables)
		this.typeVariables = result;
	for (int i = 0; i < rank; i++) {
		initializeTypeVariable(result[i], result, wrapper, missingTypeNames, walker.toTypeParameterBounds(isClassTypeParameter, i));
	}
	return result;
}

/* Answer the receiver's enclosing type... null if the receiver is a top level type.
*
* NOTE: enclosingType of a binary type is resolved when needed
*/
public ReferenceBinding enclosingType() {
	
	if ((this.tagBits & TagBits.HasUnresolvedEnclosingType) == 0)
		return this.enclosingType;

	// finish resolving the type
	this.enclosingType = (ReferenceBinding) resolveType(this.enclosingType, this.environment, false /* no raw conversion */);
	this.tagBits &= ~TagBits.HasUnresolvedEnclosingType;
	return this.enclosingType;
}
// NOTE: the type of each field of a binary type is resolved when needed
public FieldBinding[] fields() {
	
	if (this != this.prototype)
		return this.prototype.fields();

	if ((this.tagBits & TagBits.AreFieldsComplete) != 0)
		return this.fields;

	// lazily sort fields
	if ((this.tagBits & TagBits.AreFieldsSorted) == 0) {
		int length = this.fields.length;
		if (length > 1)
			ReferenceBinding.sortFields(this.fields, 0, length);
		this.tagBits |= TagBits.AreFieldsSorted;
	}
	for (int i = this.fields.length; --i >= 0;)
		resolveTypeFor(this.fields[i]);
	this.tagBits |= TagBits.AreFieldsComplete;
	return this.fields;
}

private MethodBinding findMethod(char[] methodDescriptor, char[][][] missingTypeNames) {
	int index = -1;
	while (methodDescriptor[++index] != Util.C_PARAM_START) {
		// empty
	}
	char[] selector = new char[index];
	System.arraycopy(methodDescriptor, 0, selector, 0, index);
	TypeBinding[] parameters = Binding.NO_PARAMETERS;
	int numOfParams = 0;
	char nextChar;
	int paramStart = index;
	while ((nextChar = methodDescriptor[++index]) != Util.C_PARAM_END) {
		if (nextChar != Util.C_ARRAY) {
			numOfParams++;
			if (nextChar == Util.C_RESOLVED)
				while ((nextChar = methodDescriptor[++index]) != Util.C_NAME_END){/*empty*/}
		}
	}
	if (numOfParams > 0) {
		parameters = new TypeBinding[numOfParams];
		index = paramStart + 1;
		int end = paramStart; // first character is always '(' so skip it
		for (int i = 0; i < numOfParams; i++) {
			while ((nextChar = methodDescriptor[++end]) == Util.C_ARRAY){/*empty*/}
			if (nextChar == Util.C_RESOLVED)
				while ((nextChar = methodDescriptor[++end]) != Util.C_NAME_END){/*empty*/}

			// not interested in type annotations, type will be used for comparison only, and erasure() is used if needed
			TypeBinding param = this.environment.getTypeFromSignature(methodDescriptor, index, end, false, this, missingTypeNames, TypeAnnotationWalker.EMPTY_ANNOTATION_WALKER);
			if (param instanceof UnresolvedReferenceBinding) {
				param = resolveType(param, this.environment, true /* raw conversion */);
			}
			parameters[i] = param;
			index = end + 1;
		}
	}

	int parameterLength = parameters.length;
	MethodBinding[] methods2 = this.enclosingType.getMethods(selector, parameterLength);
	// find matching method using parameters
	loop: for (int i = 0, max = methods2.length; i < max; i++) {
		MethodBinding currentMethod = methods2[i];
		TypeBinding[] parameters2 = currentMethod.parameters;
		int currentMethodParameterLength = parameters2.length;
		if (parameterLength == currentMethodParameterLength) {
			for (int j = 0; j < currentMethodParameterLength; j++) {
				if (parameters[j] != parameters2[j] && parameters[j].erasure() != parameters2[j].erasure()) {
					continue loop;
				}
			}
			return currentMethod;
		}
	}
	return null;
}

/**
 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#genericTypeSignature()
 */
public char[] genericTypeSignature() {
	if (this != this.prototype)
		return this.prototype.computeGenericTypeSignature(this.typeVariables);
	return computeGenericTypeSignature(this.typeVariables);
}

//NOTE: the return type, arg & exception types of each method of a binary type are resolved when needed
public MethodBinding getExactConstructor(TypeBinding[] argumentTypes) {

	if (this != this.prototype)
		return this.prototype.getExactConstructor(argumentTypes);

	// lazily sort methods
	if ((this.tagBits & TagBits.AreMethodsSorted) == 0) {
		int length = this.methods.length;
		if (length > 1)
			ReferenceBinding.sortMethods(this.methods, 0, length);
		this.tagBits |= TagBits.AreMethodsSorted;
	}
	int argCount = argumentTypes.length;
	long range;
	if ((range = ReferenceBinding.binarySearch(TypeConstants.INIT, this.methods)) >= 0) {
		nextMethod: for (int imethod = (int)range, end = (int)(range >> 32); imethod <= end; imethod++) {
			MethodBinding method = this.methods[imethod];
			if (method.parameters.length == argCount) {
				resolveTypesFor(method);
				TypeBinding[] toMatch = method.parameters;
				for (int iarg = 0; iarg < argCount; iarg++)
					if (toMatch[iarg] != argumentTypes[iarg])
						continue nextMethod;
				return method;
			}
		}
	}
	return null;
}

//NOTE: the return type, arg & exception types of each method of a binary type are resolved when needed
//searches up the hierarchy as long as no potential (but not exact) match was found.
public MethodBinding getExactMethod(char[] selector, TypeBinding[] argumentTypes, CompilationUnitScope refScope) {
	// sender from refScope calls recordTypeReference(this)

	if (this != this.prototype)
		return this.prototype.getExactMethod(selector, argumentTypes, refScope);

	// lazily sort methods
	if ((this.tagBits & TagBits.AreMethodsSorted) == 0) {
		int length = this.methods.length;
		if (length > 1)
			ReferenceBinding.sortMethods(this.methods, 0, length);
		this.tagBits |= TagBits.AreMethodsSorted;
	}

	int argCount = argumentTypes.length;
	boolean foundNothing = true;

	long range;
	if ((range = ReferenceBinding.binarySearch(selector, this.methods)) >= 0) {
		nextMethod: for (int imethod = (int)range, end = (int)(range >> 32); imethod <= end; imethod++) {
			MethodBinding method = this.methods[imethod];
			foundNothing = false; // inner type lookups must know that a method with this name exists
			if (method.parameters.length == argCount) {
				resolveTypesFor(method);
				TypeBinding[] toMatch = method.parameters;
				for (int iarg = 0; iarg < argCount; iarg++)
					if (toMatch[iarg] != argumentTypes[iarg])
						continue nextMethod;
				return method;
			}
		}
	}
	if (foundNothing) {
		if (isInterface()) {
			 if (superInterfaces().length == 1) { // ensure superinterfaces are resolved before checking
				if (refScope != null)
					refScope.recordTypeReference(this.superInterfaces[0]);
				return this.superInterfaces[0].getExactMethod(selector, argumentTypes, refScope);
			 }
		} else if (superclass() != null) { // ensure superclass is resolved before checking
			if (refScope != null)
				refScope.recordTypeReference(this.superclass);
			return this.superclass.getExactMethod(selector, argumentTypes, refScope);
		}
	}
	return null;
}
//NOTE: the type of a field of a binary type is resolved when needed
public FieldBinding getField(char[] fieldName, boolean needResolve) {
	
	if (this != this.prototype)
		return this.prototype.getField(fieldName, needResolve);

	// lazily sort fields
	if ((this.tagBits & TagBits.AreFieldsSorted) == 0) {
		int length = this.fields.length;
		if (length > 1)
			ReferenceBinding.sortFields(this.fields, 0, length);
		this.tagBits |= TagBits.AreFieldsSorted;
	}
	FieldBinding field = ReferenceBinding.binarySearch(fieldName, this.fields);
	return needResolve && field != null ? resolveTypeFor(field) : field;
}
/**
 *  Rewrite of default getMemberType to avoid resolving eagerly all member types when one is requested
 */
public ReferenceBinding getMemberType(char[] typeName) {

	if (this != this.prototype)
		return this.prototype.getMemberType(typeName);

	for (int i = this.memberTypes.length; --i >= 0;) {
	    ReferenceBinding memberType = this.memberTypes[i];
	    if (memberType instanceof UnresolvedReferenceBinding) {
			char[] name = memberType.sourceName; // source name is qualified with enclosing type name
			int prefixLength = this.compoundName[this.compoundName.length - 1].length + 1; // enclosing$
			if (name.length == (prefixLength + typeName.length)) // enclosing $ typeName
				if (CharOperation.fragmentEquals(typeName, name, prefixLength, true)) // only check trailing portion
					return this.memberTypes[i] = (ReferenceBinding) resolveType(memberType, this.environment, false /* no raw conversion for now */);
	    } else if (CharOperation.equals(typeName, memberType.sourceName)) {
	        return memberType;
	    }
	}
	return null;
}
// NOTE: the return type, arg & exception types of each method of a binary type are resolved when needed
public MethodBinding[] getMethods(char[] selector) {
	
	if (this != this.prototype)
		return this.prototype.getMethods(selector);

	if ((this.tagBits & TagBits.AreMethodsComplete) != 0) {
		long range;
		if ((range = ReferenceBinding.binarySearch(selector, this.methods)) >= 0) {
			int start = (int) range, end = (int) (range >> 32);
			int length = end - start + 1;
			if ((this.tagBits & TagBits.AreMethodsComplete) != 0) {
				// simply clone method subset
				MethodBinding[] result;
				System.arraycopy(this.methods, start, result = new MethodBinding[length], 0, length);
				return result;
			}
		}
		return Binding.NO_METHODS;
	}
	// lazily sort methods
	if ((this.tagBits & TagBits.AreMethodsSorted) == 0) {
		int length = this.methods.length;
		if (length > 1)
			ReferenceBinding.sortMethods(this.methods, 0, length);
		this.tagBits |= TagBits.AreMethodsSorted;
	}
	long range;
	if ((range = ReferenceBinding.binarySearch(selector, this.methods)) >= 0) {
		int start = (int) range, end = (int) (range >> 32);
		int length = end - start + 1;
		MethodBinding[] result = new MethodBinding[length];
		// iterate methods to resolve them
		for (int i = start, index = 0; i <= end; i++, index++)
			result[index] = resolveTypesFor(this.methods[i]);
		return result;
	}
	return Binding.NO_METHODS;
}
// Answer methods named selector, which take no more than the suggestedParameterLength.
// The suggested parameter length is optional and may not be guaranteed by every type.
public MethodBinding[] getMethods(char[] selector, int suggestedParameterLength) {
	
	if (this != this.prototype)
		return this.prototype.getMethods(selector, suggestedParameterLength);

	if ((this.tagBits & TagBits.AreMethodsComplete) != 0)
		return getMethods(selector);
	// lazily sort methods
	if ((this.tagBits & TagBits.AreMethodsSorted) == 0) {
		int length = this.methods.length;
		if (length > 1)
			ReferenceBinding.sortMethods(this.methods, 0, length);
		this.tagBits |= TagBits.AreMethodsSorted;
	}
	long range;
	if ((range = ReferenceBinding.binarySearch(selector, this.methods)) >= 0) {
		int start = (int) range, end = (int) (range >> 32);
		int length = end - start + 1;
		int count = 0;
		for (int i = start; i <= end; i++) {
			int len = this.methods[i].parameters.length;
			if (len <= suggestedParameterLength || (this.methods[i].isVarargs() && len == suggestedParameterLength + 1))
				count++;
		}
		if (count == 0) {
			MethodBinding[] result = new MethodBinding[length];
			// iterate methods to resolve them
			for (int i = start, index = 0; i <= end; i++)
				result[index++] = resolveTypesFor(this.methods[i]);
			return result;
		} else {
			MethodBinding[] result = new MethodBinding[count];
			// iterate methods to resolve them
			for (int i = start, index = 0; i <= end; i++) {
				int len = this.methods[i].parameters.length;
				if (len <= suggestedParameterLength || (this.methods[i].isVarargs() && len == suggestedParameterLength + 1))
					result[index++] = resolveTypesFor(this.methods[i]);
			}
			return result;
		}
	}
	return Binding.NO_METHODS;
}
public boolean hasMemberTypes() {
	if (this != this.prototype)
		return this.prototype.hasMemberTypes();
    return this.memberTypes.length > 0;
}
// NOTE: member types of binary types are resolved when needed
public TypeVariableBinding getTypeVariable(char[] variableName) {
	if (this != this.prototype)
		return this.prototype.getTypeVariable(variableName);

	TypeVariableBinding variable = super.getTypeVariable(variableName);
	variable.resolve();
	return variable;
}
public boolean hasTypeBit(int bit) {
	
	if (this != this.prototype)
		return this.prototype.hasTypeBit(bit);
	
	// ensure hierarchy is resolved, which will propagate bits down to us
	boolean wasToleratingMissingTypeProcessingAnnotations = this.environment.mayTolerateMissingType;
	this.environment.mayTolerateMissingType = true;
	try {
		superclass();
		superInterfaces();
	} finally {
		this.environment.mayTolerateMissingType = wasToleratingMissingTypeProcessingAnnotations;
	}
	return (this.typeBits & bit) != 0;
}
private void initializeTypeVariable(TypeVariableBinding variable, TypeVariableBinding[] existingVariables, SignatureWrapper wrapper, char[][][] missingTypeNames, TypeAnnotationWalker walker) {
	// ParameterSignature = Identifier ':' TypeSignature
	//   or Identifier ':' TypeSignature(optional) InterfaceBound(s)
	// InterfaceBound = ':' TypeSignature
	int colon = CharOperation.indexOf(Util.C_COLON, wrapper.signature, wrapper.start);
	wrapper.start = colon + 1; // skip name + ':'
	ReferenceBinding type, firstBound = null;
	short rank = 0;
	if (wrapper.signature[wrapper.start] == Util.C_COLON) {
		type = this.environment.getResolvedType(TypeConstants.JAVA_LANG_OBJECT, null);
	} else {
		TypeBinding typeFromTypeSignature = this.environment.getTypeFromTypeSignature(wrapper, existingVariables, this, missingTypeNames, walker.toTypeBound(rank++));
		if (typeFromTypeSignature instanceof ReferenceBinding) {
			type = (ReferenceBinding) typeFromTypeSignature;
		} else {
			// this should only happen if the signature is corrupted (332423)
			type = this.environment.getResolvedType(TypeConstants.JAVA_LANG_OBJECT, null);
		}
		firstBound = type;
	}

	// variable is visible to its bounds
	variable.modifiers |= ExtraCompilerModifiers.AccUnresolved;
	variable.setSuperClass(type);

	ReferenceBinding[] bounds = null;
	if (wrapper.signature[wrapper.start] == Util.C_COLON) {
		java.util.ArrayList types = new java.util.ArrayList(2);
		do {
			wrapper.start++; // skip ':'
			types.add(this.environment.getTypeFromTypeSignature(wrapper, existingVariables, this, missingTypeNames, walker.toTypeBound(rank++)));
		} while (wrapper.signature[wrapper.start] == Util.C_COLON);
		bounds = new ReferenceBinding[types.size()];
		types.toArray(bounds);
	}

	variable.setSuperInterfaces(bounds == null ? Binding.NO_SUPERINTERFACES : bounds);
	if (firstBound == null) {
		firstBound = variable.superInterfaces.length == 0 ? null : variable.superInterfaces[0];
	}
	variable.setFirstBound(firstBound);
}
/**
 * Returns true if a type is identical to another one,
 * or for generic types, true if compared to its raw type.
 */
public boolean isEquivalentTo(TypeBinding otherType) {
	
	if (TypeBinding.equalsEquals(this, otherType)) return true;
	if (otherType == null) return false;
	switch(otherType.kind()) {
		case Binding.WILDCARD_TYPE :
		case Binding.INTERSECTION_TYPE :
			return ((WildcardBinding) otherType).boundCheck(this);
		case Binding.PARAMETERIZED_TYPE:
		/* With the hybrid 1.4/1.5+ projects modes, while establishing type equivalence, we need to
	       be prepared for a type such as Map appearing in one of three forms: As (a) a ParameterizedTypeBinding 
	       e.g Map<String, String>, (b) as RawTypeBinding Map#RAW and finally (c) as a BinaryTypeBinding 
	       When the usage of a type lacks type parameters, whether we land up with the raw form or not depends
	       on whether the underlying type was "seen to be" a generic type in the particular build environment or
	       not. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=186565 && https://bugs.eclipse.org/bugs/show_bug.cgi?id=328827 
		*/ 
		case Binding.RAW_TYPE :
			return TypeBinding.equalsEquals(otherType.erasure(), this);
	}
	return false;
}
public boolean isGenericType() {
	
	if (this != this.prototype)
		return this.prototype.isGenericType();
	
    return this.typeVariables != Binding.NO_TYPE_VARIABLES;
}
public boolean isHierarchyConnected() {
	
	if (this != this.prototype)
		return this.prototype.isHierarchyConnected();
	
	return (this.tagBits & (TagBits.HasUnresolvedSuperclass | TagBits.HasUnresolvedSuperinterfaces)) == 0;
}
public boolean isRepeatableAnnotation() {
	return this.containingAnnotation != null;
}
public int kind() {
	
	if (this != this.prototype)
		return this.prototype.kind();
	
	if (this.typeVariables != Binding.NO_TYPE_VARIABLES)
		return Binding.GENERIC_TYPE;
	return Binding.TYPE;
}
// NOTE: member types of binary types are resolved when needed
public ReferenceBinding[] memberTypes() {
 	
	if (this != this.prototype)
		return this.prototype.memberTypes();
	
	if ((this.tagBits & TagBits.HasUnresolvedMemberTypes) == 0)
		return this.memberTypes;

	for (int i = this.memberTypes.length; --i >= 0;)
		this.memberTypes[i] = (ReferenceBinding) resolveType(this.memberTypes[i], this.environment, false /* no raw conversion for now */);
	this.tagBits &= ~TagBits.HasUnresolvedMemberTypes;
	return this.memberTypes;
}
// NOTE: the return type, arg & exception types of each method of a binary type are resolved when needed
public MethodBinding[] methods() {
	
	if (this != this.prototype)
		return this.prototype.methods();
	
	if ((this.tagBits & TagBits.AreMethodsComplete) != 0)
		return this.methods;

	// lazily sort methods
	if ((this.tagBits & TagBits.AreMethodsSorted) == 0) {
		int length = this.methods.length;
		if (length > 1)
			ReferenceBinding.sortMethods(this.methods, 0, length);
		this.tagBits |= TagBits.AreMethodsSorted;
	}
	for (int i = this.methods.length; --i >= 0;)
		resolveTypesFor(this.methods[i]);
	this.tagBits |= TagBits.AreMethodsComplete;
	return this.methods;
}

public TypeBinding prototype() {
	return this.prototype;
}

public ReferenceBinding resolveContainerAnnotation() {
	if (this.containingAnnotation instanceof UnresolvedReferenceBinding) {
		this.containingAnnotation = (ReferenceBinding) BinaryTypeBinding.resolveType(this.containingAnnotation, this.environment, false);
	}
	return this.containingAnnotation;
}

private FieldBinding resolveTypeFor(FieldBinding field) {
	
	if (this != this.prototype)
		return this.prototype.resolveTypeFor(field);
	
	if ((field.modifiers & ExtraCompilerModifiers.AccUnresolved) == 0)
		return field;

	TypeBinding resolvedType = resolveType(field.type, this.environment, true /* raw conversion */);
	field.type = resolvedType;
	if ((resolvedType.tagBits & TagBits.HasMissingType) != 0) {
		field.tagBits |= TagBits.HasMissingType;
	}
	field.modifiers &= ~ExtraCompilerModifiers.AccUnresolved;
	return field;
}
MethodBinding resolveTypesFor(MethodBinding method) {
	
	if (this != this.prototype)
		return this.prototype.resolveTypesFor(method);
	
	if ((method.modifiers & ExtraCompilerModifiers.AccUnresolved) == 0)
		return method;

	if (!method.isConstructor()) {
		TypeBinding resolvedType = resolveType(method.returnType, this.environment, true /* raw conversion */);
		method.returnType = resolvedType;
		if ((resolvedType.tagBits & TagBits.HasMissingType) != 0) {
			method.tagBits |= TagBits.HasMissingType;
		}
	}
	for (int i = method.parameters.length; --i >= 0;) {
		TypeBinding resolvedType = resolveType(method.parameters[i], this.environment, true /* raw conversion */);
		method.parameters[i] = resolvedType;
		if ((resolvedType.tagBits & TagBits.HasMissingType) != 0) {
			method.tagBits |= TagBits.HasMissingType;
		}
	}
	for (int i = method.thrownExceptions.length; --i >= 0;) {
		ReferenceBinding resolvedType = (ReferenceBinding) resolveType(method.thrownExceptions[i], this.environment, true /* raw conversion */);
		method.thrownExceptions[i] = resolvedType;
		if ((resolvedType.tagBits & TagBits.HasMissingType) != 0) {
			method.tagBits |= TagBits.HasMissingType;
		}
	}
	for (int i = method.typeVariables.length; --i >= 0;) {
		method.typeVariables[i].resolve();
	}
	method.modifiers &= ~ExtraCompilerModifiers.AccUnresolved;
	return method;
}
AnnotationBinding[] retrieveAnnotations(Binding binding) {
	
	if (this != this.prototype)
		return this.prototype.retrieveAnnotations(binding);
	
	return AnnotationBinding.addStandardAnnotations(super.retrieveAnnotations(binding), binding.getAnnotationTagBits(), this.environment);
}
public void setContainingAnnotation(ReferenceBinding value) {
	this.containingAnnotation = value;
}
SimpleLookupTable storedAnnotations(boolean forceInitialize) {
	
	if (this != this.prototype)
		return this.prototype.storedAnnotations(forceInitialize);
	
	if (forceInitialize && this.storedAnnotations == null) {
		if (!this.environment.globalOptions.storeAnnotations)
			return null; // not supported during this compile
		this.storedAnnotations = new SimpleLookupTable(3);
	}
	return this.storedAnnotations;
}

private void scanFieldForNullAnnotation(IBinaryField field, FieldBinding fieldBinding) {
	if (this.environment.globalOptions.sourceLevel >= ClassFileConstants.JDK1_8) {
		TypeBinding fieldType = fieldBinding.type;
		if (fieldType != null
				&& !fieldType.isBaseType()
				&& (fieldType.tagBits & TagBits.AnnotationNullMASK) == 0
				&& (this.tagBits & TagBits.AnnotationNonNullByDefault) != 0) {
			fieldBinding.type = this.environment.createAnnotatedType(fieldType, new AnnotationBinding[]{this.environment.getNonNullAnnotation()});
		}
		return; // not using fieldBinding.tagBits when we have type annotations.
	}

	// global option is checked by caller
	char[][] nullableAnnotationName = this.environment.getNullableAnnotationName();
	char[][] nonNullAnnotationName = this.environment.getNonNullAnnotationName();
	if (nullableAnnotationName == null || nonNullAnnotationName == null)
		return; // not well-configured to use null annotations

	if (fieldBinding.type == null || fieldBinding.type.isBaseType())
		return; // null annotations are only applied to reference types

	boolean explicitNullness = false;
	IBinaryAnnotation[] annotations = field.getAnnotations();
	if (annotations != null) {
		for (int i = 0; i < annotations.length; i++) {
			char[] annotationTypeName = annotations[i].getTypeName();
			if (annotationTypeName[0] != Util.C_RESOLVED)
				continue;
			char[][] typeName = CharOperation.splitOn('/', annotationTypeName, 1, annotationTypeName.length-1); // cut of leading 'L' and trailing ';'
			if (CharOperation.equals(typeName, nonNullAnnotationName)) {
				fieldBinding.tagBits |= TagBits.AnnotationNonNull;
				explicitNullness = true;
				break;
			}
			if (CharOperation.equals(typeName, nullableAnnotationName)) {
				fieldBinding.tagBits |= TagBits.AnnotationNullable;
				explicitNullness = true;
				break;
			}
		}
	}
	if (!explicitNullness && (this.tagBits & TagBits.AnnotationNonNullByDefault) != 0) {
		fieldBinding.tagBits |= TagBits.AnnotationNonNull;
	}
}

private void scanMethodForNullAnnotation(IBinaryMethod method, MethodBinding methodBinding) {
	if (!this.environment.globalOptions.isAnnotationBasedNullAnalysisEnabled)
		return;
	boolean useTypeAnnotations = this.environment.globalOptions.sourceLevel >= ClassFileConstants.JDK1_8;
	// in 1.8 we only need @NonNullByDefault, see below and exit further down.
	char[][] nullableAnnotationName = this.environment.getNullableAnnotationName();
	char[][] nonNullAnnotationName = this.environment.getNonNullAnnotationName();
	char[][] nonNullByDefaultAnnotationName = this.environment.getNonNullByDefaultAnnotationName();
	if (nullableAnnotationName == null || nonNullAnnotationName == null || nonNullByDefaultAnnotationName == null)
		return; // not well-configured to use null annotations

	// return:
	IBinaryAnnotation[] annotations = method.getAnnotations();
	boolean explicitNullness = false;
	if (annotations != null) {
		for (int i = 0; i < annotations.length; i++) {
			char[] annotationTypeName = annotations[i].getTypeName();
			if (annotationTypeName[0] != Util.C_RESOLVED)
				continue;
			char[][] typeName = CharOperation.splitOn('/', annotationTypeName, 1, annotationTypeName.length-1); // cut of leading 'L' and trailing ';'
			if (CharOperation.equals(typeName, nonNullByDefaultAnnotationName)) {
				methodBinding.tagBits |= TagBits.AnnotationNonNullByDefault;
			}
			if (!useTypeAnnotations && !explicitNullness) {
				if (CharOperation.equals(typeName, nonNullAnnotationName)) {
					methodBinding.tagBits |= TagBits.AnnotationNonNull;
					explicitNullness = true;
				} else if (CharOperation.equals(typeName, nullableAnnotationName)) {
					methodBinding.tagBits |= TagBits.AnnotationNullable;
					explicitNullness = true;
				}
			}
		}
	}

	if (useTypeAnnotations)
		return;

	// parameters:
	TypeBinding[] parameters = methodBinding.parameters;
	int numVisibleParams = parameters.length;
	int numParamAnnotations = method.getAnnotatedParametersCount();
	if (numParamAnnotations > 0) {
		for (int j = 0; j < numVisibleParams; j++) {
			if (numParamAnnotations > 0) {
				int startIndex = numParamAnnotations - numVisibleParams;
				IBinaryAnnotation[] paramAnnotations = method.getParameterAnnotations(j+startIndex);
				if (paramAnnotations != null) {
					for (int i = 0; i < paramAnnotations.length; i++) {
						char[] annotationTypeName = paramAnnotations[i].getTypeName();
						if (annotationTypeName[0] != Util.C_RESOLVED)
							continue;
						char[][] typeName = CharOperation.splitOn('/', annotationTypeName, 1, annotationTypeName.length-1); // cut of leading 'L' and trailing ';'
						if (CharOperation.equals(typeName, nonNullAnnotationName)) {
							if (methodBinding.parameterNonNullness == null)
								methodBinding.parameterNonNullness = new Boolean[numVisibleParams];
							methodBinding.parameterNonNullness[j] = Boolean.TRUE;
							break;
						} else if (CharOperation.equals(typeName, nullableAnnotationName)) {
							if (methodBinding.parameterNonNullness == null)
								methodBinding.parameterNonNullness = new Boolean[numVisibleParams];
							methodBinding.parameterNonNullness[j] = Boolean.FALSE;
							break;
						}
					}
				}
			}
		}
	}
}
private void scanTypeForNullDefaultAnnotation(IBinaryType binaryType, PackageBinding packageBinding, BinaryTypeBinding binaryBinding) {
	char[][] nonNullByDefaultAnnotationName = this.environment.getNonNullByDefaultAnnotationName();
	if (nonNullByDefaultAnnotationName == null)
		return; // not well-configured to use null annotations

	IBinaryAnnotation[] annotations = binaryType.getAnnotations();
	boolean isPackageInfo = CharOperation.equals(binaryBinding.sourceName(), TypeConstants.PACKAGE_INFO_NAME);
	if (annotations != null) {
		long annotationBit = 0L;
		int nullness = NO_NULL_DEFAULT;
		int length = annotations.length;
		for (int i = 0; i < length; i++) {
			char[] annotationTypeName = annotations[i].getTypeName();
			if (annotationTypeName[0] != Util.C_RESOLVED)
				continue;
			char[][] typeName = CharOperation.splitOn('/', annotationTypeName, 1, annotationTypeName.length-1); // cut of leading 'L' and trailing ';'
			if (CharOperation.equals(typeName, nonNullByDefaultAnnotationName)) {
				IBinaryElementValuePair[] elementValuePairs = annotations[i].getElementValuePairs();
				if (elementValuePairs != null && elementValuePairs.length == 1) {
					Object value = elementValuePairs[0].getValue();
					if (value instanceof BooleanConstant
						&& !((BooleanConstant)value).booleanValue())
					{
						// parameter is 'false': this means we cancel defaults from outer scopes:
						annotationBit = TagBits.AnnotationNullUnspecifiedByDefault;
						nullness = NULL_UNSPECIFIED_BY_DEFAULT;
						break;
					}
				}
				annotationBit = TagBits.AnnotationNonNullByDefault;
				nullness = NONNULL_BY_DEFAULT;
				break;
			}
		}
		if (annotationBit != 0L) {
			binaryBinding.tagBits |= annotationBit;
			if (isPackageInfo)
				packageBinding.defaultNullness = nullness;
			return;
		}
	}
	if (isPackageInfo) {
		// no default annotations found in package-info
		packageBinding.defaultNullness = Binding.NULL_UNSPECIFIED_BY_DEFAULT;
		return;
	}
	ReferenceBinding enclosingTypeBinding = binaryBinding.enclosingType;
	if (enclosingTypeBinding != null) {
		if ((enclosingTypeBinding.tagBits & TagBits.AnnotationNonNullByDefault) != 0) {
			binaryBinding.tagBits |= TagBits.AnnotationNonNullByDefault;
			return;
		} else if ((enclosingTypeBinding.tagBits & TagBits.AnnotationNullUnspecifiedByDefault) != 0) {
			binaryBinding.tagBits |= TagBits.AnnotationNullUnspecifiedByDefault;
			return;
		}
	}
	// no annotation found on the type or its enclosing types
	// check the package-info for default annotation if not already done before
	if (packageBinding.defaultNullness == Binding.NO_NULL_DEFAULT && !isPackageInfo) {
		// this will scan the annotations in package-info
		ReferenceBinding packageInfo = packageBinding.getType(TypeConstants.PACKAGE_INFO_NAME);
		if (packageInfo == null) {
			packageBinding.defaultNullness = Binding.NULL_UNSPECIFIED_BY_DEFAULT;
		}
	}
	// no @NonNullByDefault at type level, check containing package:
	switch (packageBinding.defaultNullness) {
		case Binding.NONNULL_BY_DEFAULT : 
			binaryBinding.tagBits |= TagBits.AnnotationNonNullByDefault;
			break;
		case Binding.NULL_UNSPECIFIED_BY_DEFAULT :
			binaryBinding.tagBits |= TagBits.AnnotationNullUnspecifiedByDefault;
			break;
	}
}

private void scanTypeForContainerAnnotation(IBinaryType binaryType, char[][][] missingTypeNames) {
	IBinaryAnnotation[] annotations = binaryType.getAnnotations();
	if (annotations != null) {
		int length = annotations.length;
		for (int i = 0; i < length; i++) {
			char[] annotationTypeName = annotations[i].getTypeName();
			if (CharOperation.equals(annotationTypeName, ConstantPool.JAVA_LANG_ANNOTATION_REPEATABLE)) {
				IBinaryElementValuePair[] elementValuePairs = annotations[i].getElementValuePairs();
				if (elementValuePairs != null && elementValuePairs.length == 1) {
					Object value = elementValuePairs[0].getValue();
					if (value instanceof ClassSignature) {
						this.containingAnnotation = (ReferenceBinding) this.environment.getTypeFromSignature(((ClassSignature)value).getTypeName(), 0, -1, false, null, missingTypeNames, TypeAnnotationWalker.EMPTY_ANNOTATION_WALKER);
					}
				}
				break;
			}
		}
	}
}

/* Answer the receiver's superclass... null if the receiver is Object or an interface.
*
* NOTE: superclass of a binary type is resolved when needed
*/
public ReferenceBinding superclass() {
	
	if (this != this.prototype)
		return this.prototype.superclass();
	
	if ((this.tagBits & TagBits.HasUnresolvedSuperclass) == 0)
		return this.superclass;

	// finish resolving the type
	this.superclass = (ReferenceBinding) resolveType(this.superclass, this.environment, true /* raw conversion */);
	this.tagBits &= ~TagBits.HasUnresolvedSuperclass;
	if (this.superclass.problemId() == ProblemReasons.NotFound) {
		this.tagBits |= TagBits.HierarchyHasProblems; // propagate type inconsistency
	} else {
		// make super-type resolving recursive for propagating typeBits downwards
		boolean wasToleratingMissingTypeProcessingAnnotations = this.environment.mayTolerateMissingType;
		this.environment.mayTolerateMissingType = true; // https://bugs.eclipse.org/bugs/show_bug.cgi?id=360164
		try {
			this.superclass.superclass();
			this.superclass.superInterfaces();
		} finally {
			this.environment.mayTolerateMissingType = wasToleratingMissingTypeProcessingAnnotations;
		}
	}
	this.typeBits |= (this.superclass.typeBits & TypeIds.InheritableBits);
	if ((this.typeBits & (TypeIds.BitAutoCloseable|TypeIds.BitCloseable)) != 0) // avoid the side-effects of hasTypeBit()! 
		this.typeBits |= applyCloseableWhitelists();
	return this.superclass;
}
// NOTE: superInterfaces of binary types are resolved when needed
public ReferenceBinding[] superInterfaces() {
	
	if (this != this.prototype)
		return this.prototype.superInterfaces();
	
	if ((this.tagBits & TagBits.HasUnresolvedSuperinterfaces) == 0)
		return this.superInterfaces;

	for (int i = this.superInterfaces.length; --i >= 0;) {
		this.superInterfaces[i] = (ReferenceBinding) resolveType(this.superInterfaces[i], this.environment, true /* raw conversion */);
		if (this.superInterfaces[i].problemId() == ProblemReasons.NotFound) {
			this.tagBits |= TagBits.HierarchyHasProblems; // propagate type inconsistency
		} else {
			// make super-type resolving recursive for propagating typeBits downwards
			boolean wasToleratingMissingTypeProcessingAnnotations = this.environment.mayTolerateMissingType;
			this.environment.mayTolerateMissingType = true; // https://bugs.eclipse.org/bugs/show_bug.cgi?id=360164
			try {
				this.superInterfaces[i].superclass();
				this.superInterfaces[i].superInterfaces();
			} finally {
				this.environment.mayTolerateMissingType = wasToleratingMissingTypeProcessingAnnotations;
			}	
		}
		this.typeBits |= (this.superInterfaces[i].typeBits & TypeIds.InheritableBits);
	}
	this.tagBits &= ~TagBits.HasUnresolvedSuperinterfaces;
	return this.superInterfaces;
}
public TypeVariableBinding[] typeVariables() {
	
	if (this != this.prototype)
		return this.prototype.typeVariables();
	
 	if ((this.tagBits & TagBits.HasUnresolvedTypeVariables) == 0)
		return this.typeVariables;

 	for (int i = this.typeVariables.length; --i >= 0;)
		this.typeVariables[i].resolve();
	this.tagBits &= ~TagBits.HasUnresolvedTypeVariables;
	return this.typeVariables;
}
public String toString() {
	
	if (this.hasTypeAnnotations())
		return annotatedDebugName();
	
	StringBuffer buffer = new StringBuffer();

	if (isDeprecated()) buffer.append("deprecated "); //$NON-NLS-1$
	if (isPublic()) buffer.append("public "); //$NON-NLS-1$
	if (isProtected()) buffer.append("protected "); //$NON-NLS-1$
	if (isPrivate()) buffer.append("private "); //$NON-NLS-1$
	if (isAbstract() && isClass()) buffer.append("abstract "); //$NON-NLS-1$
	if (isStatic() && isNestedType()) buffer.append("static "); //$NON-NLS-1$
	if (isFinal()) buffer.append("final "); //$NON-NLS-1$

	if (isEnum()) buffer.append("enum "); //$NON-NLS-1$
	else if (isAnnotationType()) buffer.append("@interface "); //$NON-NLS-1$
	else if (isClass()) buffer.append("class "); //$NON-NLS-1$
	else buffer.append("interface "); //$NON-NLS-1$
	buffer.append((this.compoundName != null) ? CharOperation.toString(this.compoundName) : "UNNAMED TYPE"); //$NON-NLS-1$

	if (this.typeVariables == null) {
		buffer.append("<NULL TYPE VARIABLES>"); //$NON-NLS-1$
	} else if (this.typeVariables != Binding.NO_TYPE_VARIABLES) {
		buffer.append("<"); //$NON-NLS-1$
		for (int i = 0, length = this.typeVariables.length; i < length; i++) {
			if (i  > 0) buffer.append(", "); //$NON-NLS-1$
			if (this.typeVariables[i] == null) {
				buffer.append("NULL TYPE VARIABLE"); //$NON-NLS-1$
				continue;
			}
			char[] varChars = this.typeVariables[i].toString().toCharArray();
			buffer.append(varChars, 1, varChars.length - 2);
		}
		buffer.append(">"); //$NON-NLS-1$
	}
	buffer.append("\n\textends "); //$NON-NLS-1$
	buffer.append((this.superclass != null) ? this.superclass.debugName() : "NULL TYPE"); //$NON-NLS-1$

	if (this.superInterfaces != null) {
		if (this.superInterfaces != Binding.NO_SUPERINTERFACES) {
			buffer.append("\n\timplements : "); //$NON-NLS-1$
			for (int i = 0, length = this.superInterfaces.length; i < length; i++) {
				if (i  > 0)
					buffer.append(", "); //$NON-NLS-1$
				buffer.append((this.superInterfaces[i] != null) ? this.superInterfaces[i].debugName() : "NULL TYPE"); //$NON-NLS-1$
			}
		}
	} else {
		buffer.append("NULL SUPERINTERFACES"); //$NON-NLS-1$
	}

	if (this.enclosingType != null) {
		buffer.append("\n\tenclosing type : "); //$NON-NLS-1$
		buffer.append(this.enclosingType.debugName());
	}

	if (this.fields != null) {
		if (this.fields != Binding.NO_FIELDS) {
			buffer.append("\n/*   fields   */"); //$NON-NLS-1$
			for (int i = 0, length = this.fields.length; i < length; i++)
				buffer.append((this.fields[i] != null) ? "\n" + this.fields[i].toString() : "\nNULL FIELD"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	} else {
		buffer.append("NULL FIELDS"); //$NON-NLS-1$
	}

	if (this.methods != null) {
		if (this.methods != Binding.NO_METHODS) {
			buffer.append("\n/*   methods   */"); //$NON-NLS-1$
			for (int i = 0, length = this.methods.length; i < length; i++)
				buffer.append((this.methods[i] != null) ? "\n" + this.methods[i].toString() : "\nNULL METHOD"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	} else {
		buffer.append("NULL METHODS"); //$NON-NLS-1$
	}

	if (this.memberTypes != null) {
		if (this.memberTypes != Binding.NO_MEMBER_TYPES) {
			buffer.append("\n/*   members   */"); //$NON-NLS-1$
			for (int i = 0, length = this.memberTypes.length; i < length; i++)
				buffer.append((this.memberTypes[i] != null) ? "\n" + this.memberTypes[i].toString() : "\nNULL TYPE"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	} else {
		buffer.append("NULL MEMBER TYPES"); //$NON-NLS-1$
	}

	buffer.append("\n\n\n"); //$NON-NLS-1$
	return buffer.toString();
}

public TypeBinding unannotated() {
	return this.prototype;
}

MethodBinding[] unResolvedMethods() { // for the MethodVerifier so it doesn't resolve types
	
	if (this != this.prototype)
		return this.prototype.unResolvedMethods();
	
	return this.methods;
}

public FieldBinding[] unResolvedFields() {
	
	if (this != this.prototype)
		return this.prototype.unResolvedFields();
	
	return this.fields;
}
}
