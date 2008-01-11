/*******************************************************************************
 * Copyright (c) 2007, 2008 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.apt.pluggable.tests.processors.filertester;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import org.eclipse.jdt.apt.pluggable.tests.ProcessorTestStatus;
import org.eclipse.jdt.apt.pluggable.tests.annotations.FilerTestTrigger;

/**
 * Testing annotation processors through JUnit in the IDE is complex, because each test requires
 * something different of the processor and all processors must coexist in the plugin registry, and
 * because the processor has very limited communication with the rest of the IDE. So, we make one
 * processor run many tests. The JUnit tests specify which test to run by passing its name in to the
 * FilerTest annotation. Test failures are reported via the Messager interface.
 * 
 * @since 3.4
 */
@SupportedAnnotationTypes( { "org.eclipse.jdt.apt.pluggable.tests.annotations.FilerTestTrigger" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedOptions( {})
public class FilerTesterProc extends AbstractProcessor {

	private ProcessingEnvironment _processingEnv;
	private Filer _filer;
	
	public static final String resource01FileContents = 
		"package g;\n" +
		"public class Test {}\n";
	public static final String resource01Name =
		".apt_generated/g/Test.java";
	
	public static final String resource02FileContents =
		"This is some test text\n";
	public static final String resource02Name =
		"bin/t/Test.txt";

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.annotation.processing.AbstractProcessor#init(javax.annotation.processing.ProcessingEnvironment)
	 */
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		_processingEnv = processingEnv;
		_filer = _processingEnv.getFiler();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.annotation.processing.AbstractProcessor#process(java.util.Set,
	 *      javax.annotation.processing.RoundEnvironment)
	 */
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		ProcessorTestStatus.setProcessorRan();
		if (!roundEnv.processingOver() && !annotations.isEmpty()) {
			round(annotations, roundEnv);
		}
		return true;
	}

	/**
	 * Perform a round of processing: for a given annotation instance, determine what test method it
	 * specifies, and invoke that method, passing in the annotated element.
	 */
	private void round(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		TypeElement filerTesterAnno = annotations.iterator().next();
		Set<? extends Element> annotatedEls = roundEnv.getElementsAnnotatedWith(filerTesterAnno);
		for (Element annotatedEl : annotatedEls) {
			FilerTestTrigger filerTesterMirror = annotatedEl.getAnnotation(FilerTestTrigger.class);
			String testMethodName = filerTesterMirror.test();
			String arg0 = filerTesterMirror.arg0();
			String arg1 = filerTesterMirror.arg1();
			if (null != testMethodName && testMethodName.length() > 0) {
				try {
					Method testMethod = FilerTesterProc.class.getMethod(testMethodName, 
							Element.class, String.class, String.class);
					testMethod.invoke(this, annotatedEl, arg0, arg1);
				} catch (Exception e) {
					Throwable t;
					t = (e instanceof InvocationTargetException) ? t = e.getCause() : e;
					t.printStackTrace();
					// IllegalStateException probably means test method called ProcessorTestStatus.fail()
					String msg = (t instanceof IllegalStateException) ?
							t.getMessage() :
							t.getClass().getSimpleName() + " invoking test method " + 
							testMethodName + " - see console for details";
					ProcessorTestStatus.fail(msg);
				}
			}
		}
	}

	/**
	 * Attempt to get an existing resource from the SOURCE_OUTPUT.
	 */
	public void testGetResource01(Element e, String arg0, String arg1) throws Exception {
		FileObject resource = _filer.getResource(StandardLocation.SOURCE_OUTPUT, arg0, arg1);
		checkResourceContents01(resource, resource01Name, resource01FileContents);
	}
	
	/**
	 * Attempt to get an existing resource from the CLASS_OUTPUT.
	 */
	public void testGetResource02(Element e, String arg0, String arg1) throws Exception {
		FileObject resource = _filer.getResource(StandardLocation.CLASS_OUTPUT, arg0, arg1);
		checkResourceContents01(resource, resource02Name, resource02FileContents);
	}
	
	/**
	 * Check that the resource can be opened, examined, and its contents match
	 * {@link #checkResourceContents01(FileObject)}getResource01FileContents
	 */
	private void checkResourceContents01(FileObject resource, String expectedName, String expectedContents) throws Exception {
		
		long modTime = resource.getLastModified();
		if (modTime <= 0) {
			ProcessorTestStatus.fail("resource had unexpected mod time: " + modTime);
		}
		
		String actualName = resource.getName();
		if (!expectedName.equals(actualName)) {
			System.out.println("Resource had unexpected name.  Expected " + expectedName +
					", actual was " + actualName);
			ProcessorTestStatus.fail("Resource had unexpected name");
		}
		
		InputStream stream = resource.openInputStream();
		if (stream.available() <= 0) {
			ProcessorTestStatus.fail("stream contained no data");
		}
		byte actualBytes[] = new byte[512];
		int length = stream.read(actualBytes);
		String actualStringContents = new String(actualBytes, 0, length);
		if (!expectedContents.equals(actualStringContents)) {
			System.out.println("Expected stream contents:\n" + expectedContents);
			System.out.println("Actual contents were:\n" + actualStringContents);
			ProcessorTestStatus.fail("stream did not contain expected contents");
		}
		stream.close();
		
		char actualChars[] = new char[512];
		Reader reader = resource.openReader(true);
		length = reader.read(actualChars, 0, actualChars.length);
		actualStringContents = new String(actualChars, 0, length);
		if (!expectedContents.equals(actualStringContents)) {
			System.out.println("Expected reader contents:\n" + expectedContents);
			System.out.println("Actual contents were:\n" + actualStringContents);
			ProcessorTestStatus.fail("reader did not contain expected contents");
		}
		reader.close();
	}

}
