package org.eclipse.jdt.internal.core.nd.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.db.IString;
import org.eclipse.jdt.internal.core.nd.db.IndexException;
import org.eclipse.jdt.internal.core.nd.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.nd.field.FieldString;
import org.eclipse.jdt.internal.core.nd.field.StructDef;
import org.eclipse.jdt.internal.core.util.CharArrayBuffer;

/**
 * Represents a type signature that is anything other than a trivial reference to a concrete
 * type. If a type reference includes annotations, generic arguments, wildcards, or is a
 * type variable, this object represents it.
 * <p>
 * Arrays are encoded in a special way. The RAW_TYPE points to a sentinel type called '['
 * and the first type argument holds the array type.
 *
 * @since 3.12
 */
public class NdComplexTypeSignature extends NdTypeSignature {
	public static final FieldString VARIABLE_IDENTIFIER;
	public static final FieldManyToOne<NdTypeId> RAW_TYPE;
	public static final FieldOneToMany<NdAnnotation> ANNOTATIONS;
	public static final FieldOneToMany<NdTypeArgument> TYPE_ARGUMENTS;
	public static final FieldManyToOne<NdComplexTypeSignature> DECLARING_TYPE;
	public static final FieldOneToMany<NdComplexTypeSignature> DECLARED_TYPES;

	@SuppressWarnings("hiding")
	public static final StructDef<NdComplexTypeSignature> type;

	static {
		type = StructDef.create(NdComplexTypeSignature.class, NdTypeSignature.type);
		VARIABLE_IDENTIFIER = type.addString();
		RAW_TYPE = FieldManyToOne.create(type, NdTypeId.USED_AS_COMPLEX_TYPE);
		ANNOTATIONS = FieldOneToMany.create(type, NdAnnotation.PARENT_TYPE_SIGNATURE);
		TYPE_ARGUMENTS = FieldOneToMany.create(type, NdTypeArgument.PARENT);
		DECLARING_TYPE = FieldManyToOne.create(type, null);
		DECLARED_TYPES = FieldOneToMany.create(type, DECLARING_TYPE);

		type.useStandardRefCounting().done();
	}

	public NdComplexTypeSignature(Nd pdom, long address) {
		super(pdom, address);
	}

	public NdComplexTypeSignature(Nd pdom) {
		super(pdom);
	}

	@Override
	public NdTypeId getRawType() {
		return RAW_TYPE.get(getNd(), this.address);
	}

	public void setVariableIdentifier(char[] variableIdentifier) {
		VARIABLE_IDENTIFIER.put(getNd(), this.address, variableIdentifier);
	}

	/**
	 * If this type is a type variable, this returns the variable's identifier.
	 */
	public IString getVariableIdentifier() {
		return VARIABLE_IDENTIFIER.get(getNd(), this.address);
	}

	public void setRawType(NdTypeId rawType) {
		RAW_TYPE.put(getNd(), this.address, rawType);
	}

	public void setGenericDeclaringType(NdComplexTypeSignature enclosingType) {
		DECLARING_TYPE.put(getNd(), this.address, enclosingType);
	}

	/**
	 * Returns the declaring type (as reported by the type's generic signature).
	 * Not to be confused with the declaring type as stored in the class file.
	 * That is stored in {@link NdType#getDeclaringType}. Any class that is
	 * nested inside another class with generic arguments will have one of
	 * these. Classes nested inside non-generic classes won't have one of these,
	 * and neither will non-nested classes.
	 */
	public NdComplexTypeSignature getGenericDeclaringType() {
		return DECLARING_TYPE.get(getNd(), this.address);
	}

	public List<NdTypeArgument> getTypeArguments() {
		return TYPE_ARGUMENTS.asList(getNd(), this.address);
	}

	@Override
	public List<NdAnnotation> getAnnotations() {
		return ANNOTATIONS.asList(getNd(), this.address);
	}

	@Override
	public NdTypeSignature getArrayDimensionType() {
		if (isArrayType()) {
			long size = TYPE_ARGUMENTS.size(getNd(), this.address);

			if (size != 1) {
				throw new IndexException("Array types should have exactly one argument"); //$NON-NLS-1$
			}

			return TYPE_ARGUMENTS.get(getNd(), this.address, 0).getType();
		}
		return null;
	}

	@Override
	public void getSignature(CharArrayBuffer result) {
		NdComplexTypeSignature parentSignature = getGenericDeclaringType();

		if (parentSignature != null) {
			parentSignature.getSignature(result);
			result.append('.');
			return;
		}

		if (isTypeVariable()) {
			result.append('T');
			result.append(getVariableIdentifier().getChars());
			return;
		}

		NdTypeSignature arrayDimension = getArrayDimensionType();
		if (arrayDimension != null) {
			result.append('[');
			arrayDimension.getSignature(result);
			return;
		}

		result.append(getRawType().getFieldDescriptor().getChars());

		List<NdTypeArgument> arguments = getTypeArguments();
		if (!arguments.isEmpty()) {
			result.append('<');
			for (NdTypeArgument next : arguments) {
				next.getSignature(result);
			}
			result.append('>');
		}
	}

	@Override
	public boolean isTypeVariable() {
		return getVariableIdentifier().length() != 0;
	}

	@Override
	public List<NdTypeSignature> getDeclaringTypeChain() {
		NdComplexTypeSignature declaringType = getGenericDeclaringType();

		if (declaringType == null) {
			return Collections.singletonList((NdTypeSignature)this);
		}

		List<NdTypeSignature> result = new ArrayList<>();
		computeDeclaringTypes(result);
		return result;
	}

	private void computeDeclaringTypes(List<NdTypeSignature> result) {
		NdComplexTypeSignature declaringType = getGenericDeclaringType();

		if (declaringType != null) {
			computeDeclaringTypes(result);
		}

		result.add(declaringType);
	}
}
