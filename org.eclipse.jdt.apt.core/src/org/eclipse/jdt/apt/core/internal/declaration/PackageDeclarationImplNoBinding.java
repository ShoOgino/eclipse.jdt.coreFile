/*******************************************************************************
 * Copyright (c) 2005, 2006 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal.declaration;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.core.IPackageFragment;

import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.EnumDeclaration;
import com.sun.mirror.declaration.InterfaceDeclaration;
import com.sun.mirror.declaration.Modifier;
import com.sun.mirror.declaration.PackageDeclaration;
import com.sun.mirror.util.DeclarationVisitor;
import com.sun.mirror.util.SourcePosition;

/**
 * For packages that have no binding. E.g. Packages with no
 * classes, like "java.lang", or possibly "" (the default package).
 */
public class PackageDeclarationImplNoBinding implements PackageDeclaration {
	
	private final IPackageFragment[] fragments;
	
	public PackageDeclarationImplNoBinding(final IPackageFragment[] fragments) {
		this.fragments = fragments;
	}

	public String getQualifiedName() {
		return fragments[0].getElementName();
	}

	public Collection<ClassDeclaration> getClasses() {
		return Collections.emptyList();
    }

    public Collection<EnumDeclaration> getEnums() {
		return Collections.emptyList();
    }

    public Collection<InterfaceDeclaration> getInterfaces() {
		return Collections.emptyList();
    }

	public Collection<AnnotationTypeDeclaration> getAnnotationTypes() {
		return Collections.emptyList();
	}

	public String getDocComment() {
		// Packages have no comments
		return null;
	}

	public Collection<AnnotationMirror> getAnnotationMirrors() {
		return Collections.emptyList();
	}

	public <A extends Annotation> A getAnnotation(Class<A> arg0) {
		return null;
	}

	public Collection<Modifier> getModifiers() {
		// Packages do not have modifiers
		return Collections.emptyList();
	}

	public String getSimpleName() {
		String components = getQualifiedName();
		int dotIndex = components.indexOf("."); //$NON-NLS-1$
		if (dotIndex < 0)
			return components;
		return components.substring(dotIndex + 1);
	}

	public SourcePosition getPosition() {
		// non-source, we do not have a source position
		return null;
	}

	public void accept(final DeclarationVisitor visitor) {
		visitor.visitDeclaration(this);
		visitor.visitPackageDeclaration(this);
	}

}
