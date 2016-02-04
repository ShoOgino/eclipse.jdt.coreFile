package org.eclipse.jdt.internal.core.nd.java;

import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.FloatConstant;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.field.FieldFloat;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

/**
 * @since 3.12
 */
public final class NdConstantFloat extends NdConstant {
	public static final FieldFloat VALUE;

	@SuppressWarnings("hiding")
	public static StructDef<NdConstantFloat> type;

	static {
		type = StructDef.create(NdConstantFloat.class, NdConstant.type);
		VALUE = type.addFloat();
		type.done();
	}

	public NdConstantFloat(Nd pdom, long address) {
		super(pdom, address);
	}

	protected NdConstantFloat(Nd pdom) {
		super(pdom);
	}

	public static NdConstantFloat create(Nd pdom, float value) {
		NdConstantFloat result = new NdConstantFloat(pdom);
		result.setValue(value);
		return result;
	}

	public void setValue(float value) {
		VALUE.put(getNd(), this.address, value);
	}

	public float getValue() {
		return VALUE.get(getNd(), this.address);
	}

	@Override
	public Constant getConstant() {
		return FloatConstant.fromValue(getValue());
	}
}
