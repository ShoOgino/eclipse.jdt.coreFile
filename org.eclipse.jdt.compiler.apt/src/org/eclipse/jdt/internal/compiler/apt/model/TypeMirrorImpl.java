/*******************************************************************************
 * Copyright (c) 2007, 2013 BEA Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *    IBM Corporation - Java 8 support
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.apt.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/**
 * Implementation of a TypeMirror.  TypeMirror represents a type, including
 * types that have no declaration, such as primitives (int, boolean) and
 * types that are specializations of declarations (List<String>).
 */
public class TypeMirrorImpl implements TypeMirror {

	// Caution: _env will be NULL for unannotated primitive types (PrimitiveTypeImpl).
	protected final BaseProcessingEnvImpl _env;
	protected final Binding _binding;
	
	/* package */ TypeMirrorImpl(BaseProcessingEnvImpl env, Binding binding) {
		_env = env;
		_binding = binding;
	}
	
	/* package */ Binding binding() {
		return _binding;
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.type.TypeMirror#accept(javax.lang.model.type.TypeVisitor, java.lang.Object)
	 */
	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p) {
		return v.visit(this, p);
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.type.TypeMirror#getKind()
	 */
	@Override
	public TypeKind getKind() {
		switch (_binding.kind()) {
		// case Binding.TYPE: 
		// case Binding.RAW_TYPE:
		// case Binding.GENERIC_TYPE:
		// case Binding.PARAMETERIZED_TYPE:
		// handled by DeclaredTypeImpl, etc.
		// case Binding.BASE_TYPE: handled by PrimitiveTypeImpl
		// case Binding.METHOD: handled by ExecutableTypeImpl
		// case Binding.PACKAGE: handled by NoTypeImpl
		// case Binding.WILDCARD_TYPE: handled by WildcardTypeImpl
		// case Binding.ARRAY_TYPE: handled by ArrayTypeImpl
		// case Binding.TYPE_PARAMETER: handled by TypeVariableImpl
		// TODO: fill in the rest of these
		case Binding.FIELD:
		case Binding.LOCAL:
		case Binding.VARIABLE:
		case Binding.IMPORT:
			throw new IllegalArgumentException("Invalid binding kind: " + _binding.kind()); //$NON-NLS-1$
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new String(_binding.readableName());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_binding == null) ? 0 : _binding.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof TypeMirrorImpl))
			return false;
		final TypeMirrorImpl other = (TypeMirrorImpl) obj;
		return _binding == other._binding;
	}

	/* Package any repeating annotations into containers, return others as is.
	   In the compiler bindings repeating annotations are left in as is, hence
	   this step. The return value would match what one would expect to see in
	   a class file.
	*/
	public final AnnotationBinding [] getPackedAnnotationBindings() {
		return Factory.getPackedAnnotationBindings(getAnnotationBindings());
	}
	
	protected AnnotationBinding[] getAnnotationBindings() {
		return ((TypeBinding)_binding).getTypeAnnotations();
	}

	public List<? extends AnnotationMirror> getAnnotationMirrors() {
		return _env == null ? Factory.EMPTY_ANNOTATION_MIRRORS : 
								_env.getFactory().getAnnotationMirrors(getPackedAnnotationBindings());
	}

	public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
		return _env == null ? null : _env.getFactory().getAnnotation(getPackedAnnotationBindings(), annotationType);
	}

	@SuppressWarnings("unchecked")
	public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
		if (_env == null)
			return (A[]) Array.newInstance(annotationType, 0);
		return _env.getFactory().getAnnotationsByType(Factory.getUnpackedAnnotationBindings(getPackedAnnotationBindings()), annotationType);
	}
}
