/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   wharley - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.tests.external.annotations.loadertest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;


import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

/**
 * Used to test loading an annotation processor from a jar file.
 */
public class LoaderTestAnnotationProcessorFactory implements
		AnnotationProcessorFactory {

	private static ArrayList<String> annotations = new ArrayList<String>();
	
	{
		annotations.add( LoaderTestAnnotation.class.getName() );
	}
	
	public Collection<String> supportedOptions() {
		return Collections.emptyList();
	}

	public Collection<String> supportedAnnotationTypes() {
		return annotations;
	}

	public AnnotationProcessor getProcessorFor(
			Set<AnnotationTypeDeclaration> atds,
			AnnotationProcessorEnvironment env) {
		return new LoaderTestAnnotationProcessor( env );
	}
}
