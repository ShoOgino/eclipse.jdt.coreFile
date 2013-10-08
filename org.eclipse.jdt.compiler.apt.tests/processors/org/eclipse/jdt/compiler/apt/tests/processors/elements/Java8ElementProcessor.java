/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation.
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
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests.processors.elements;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.eclipse.jdt.compiler.apt.tests.annotations.Type;
import org.eclipse.jdt.compiler.apt.tests.annotations.Type$1;
import org.eclipse.jdt.compiler.apt.tests.processors.base.BaseProcessor;

/**
 * A processor that explores the java 8 specific elements and validates the lambda and 
 * type annotated elements. To enable this processor, add 
 * -Aorg.eclipse.jdt.compiler.apt.tests.processors.elements.Java8ElementProcessor to the command line.
 * @since 3.9 BETA_JAVA8
 */
@SupportedAnnotationTypes({"org.eclipse.jdt.compiler.apt.tests.annotations.Type", "org.eclipse.jdt.compiler.apt.tests.annotations.Type$1"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class Java8ElementProcessor extends BaseProcessor {
	
		private static final String[] ELEMENT_NAMES = new String[] {
			"targets.model8.X", "T", "U", "K", "V"};
	
	RoundEnvironment roundEnv = null;
	// Always return false from this processor, because it supports "*".
	// The return value does not signify success or failure!
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (roundEnv.processingOver()) {
			return false;
		}
		
		this.roundEnv = roundEnv;

		Map<String, String> options = processingEnv.getOptions();
		if (!options.containsKey(this.getClass().getName())) {
			// Disable this processor unless we are intentionally performing the test.
			return false;
		} else {
			try {
				if (!invokeTestMethods(options)) {
					testAll();
				}
				super.reportSuccess();
			} catch (AssertionFailedError e) {
				super.reportError(getExceptionStackTrace(e));
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	public boolean invokeTestMethods(Map<String, String> options) throws Throwable {
		Method testMethod = null;
		Set<String> keys = options.keySet();
		boolean testsFound = false;
		for (String option : keys) {
			if (option.startsWith("test")) {
				try {
					testMethod = this.getClass().getDeclaredMethod(option, new Class[0]);
					if (testMethod != null) {
						testsFound = true;
						testMethod.invoke(this,  new Object[0]);
					}
				} catch (InvocationTargetException e) {
					throw e.getCause();
				} catch (Exception e) {
					super.reportError(getExceptionStackTrace(e));
				}
			}
		}
		return testsFound;
	}
	public void testAll() throws AssertionFailedError {
		testSE8Specifics();
		testLambdaSpecifics();
		testTypeAnnotations();
		testTypeAnnotations1();
		testTypeAnnotations2();
		testTypeAnnotations3();
		testTypeAnnotations4();
		testTypeAnnotations5();
		testTypeAnnotations6();
		testTypeAnnotations7();
		testTypeAnnotations8();
		testTypeAnnotations9();
		testTypeAnnotations10();
		testTypeAnnotations11();
	}
	
	public void testLambdaSpecifics() {
		TypeElement annotatedType = _elementUtils.getTypeElement("targets.model8.LambdaTest");
		assertNotNull("Java8ElementProcessor#examineLambdaSpecifics: Type element for LambdaTest should not be null", annotatedType);
		assertFalse("Java8ElementProcessor#examineLambdaSpecifics: Type LambdaTest is not a functional interface", _elementUtils.isFunctionalInterface(annotatedType));
		List<? extends Element> members = _elementUtils.getAllMembers(annotatedType);
		ExecutableElement method = null;
		for (ExecutableElement member : ElementFilter.methodsIn(members)) {
			if ("foo".equals(member.getSimpleName().toString())) {
				method = member;
				break;
			}
		}
		assertNotNull("Java8ElementProcessor#examineLambdaSpecifics: Element for method foo should not be null", method);
		assertFalse("Java8ElementProcessor#examineLambdaSpecifics: Method foo is not a default method", method.isDefault());

		annotatedType = _elementUtils.getTypeElement("targets.model8.DefaultInterface");
		assertNotNull("Java8ElementProcessor#examineLambdaSpecifics: Type element for DefaultInterface should not be null", annotatedType);
		assertFalse("Java8ElementProcessor#examineLambdaSpecifics: Type DefaultInterface is not a functional interface", _elementUtils.isFunctionalInterface(annotatedType));

		method = null;
		members = _elementUtils.getAllMembers(annotatedType);
		for (ExecutableElement member : ElementFilter.methodsIn(members)) {
			if ("defaultMethod".equals(member.getSimpleName().toString())) {
				method = member;
				break;
			}
		}
		assertNotNull("Java8ElementProcessor#examineLambdaSpecifics: Element for method defaultMethod() should not be null", method);
		assertTrue("Java8ElementProcessor#examineLambdaSpecifics: Method defaultMethod() should be a default method", method.isDefault());

		method = null;
		for (ExecutableElement member : ElementFilter.methodsIn(members)) {
			if ("staticMethod".equals(member.getSimpleName().toString())) {
				method = member;
				break;
			}
		}
		assertNotNull("Java8ElementProcessor#examineLambdaSpecifics: Element for method staticMethod() should not be null", method);
		assertFalse("Java8ElementProcessor#examineLambdaSpecifics: Method staticMethod() shoule not be a default method", method.isDefault());

		annotatedType = _elementUtils.getTypeElement("targets.model8.FunctionalInterface");
		assertNotNull("Java8ElementProcessor#examineLambdaSpecifics: Type element for FunctionalInterface should not be null", annotatedType);
		assertTrue("Java8ElementProcessor#examineLambdaSpecifics: Type FunctionalInterface should be a functional interface", _elementUtils.isFunctionalInterface(annotatedType));

		method = null;
		members = _elementUtils.getAllMembers(annotatedType);
		for (ExecutableElement member : ElementFilter.methodsIn(members)) {
			if ("abstractMethod".equals(member.getSimpleName().toString())) {
				method = member;
				break;
			}
		}
		assertNotNull("Java8ElementProcessor#examineLambdaSpecifics: Element for method abstractMethod() should not be null", method);
		assertFalse("Java8ElementProcessor#examineLambdaSpecifics: Method abstractMethod() should not be a default method", method.isDefault());
	}

	public void testSE8Specifics() {
		TypeElement annotatedType = _elementUtils.getTypeElement("targets.model8.X");
		examineSE8AnnotationMethods("Java8ElementProcessor#examineSE8Specifics: ", annotatedType, "c");

		List<? extends Element> members = _elementUtils.getAllMembers(annotatedType);
		ExecutableElement method = null;
		VariableElement field = null, field1 = null;
		ExecutableElement method2 = null;
		for (Element member : members) {
			if ("foo".equals(member.getSimpleName().toString())) {
				method = (ExecutableElement) member;
			} else if ("_field".equals(member.getSimpleName().toString())) {
				field = (VariableElement) member;
			} else if ("noAnnotationHere".equals(member.getSimpleName().toString())) {
				method2 = (ExecutableElement) member;
			} else if ("_field1".equals(member.getSimpleName().toString())) {
				field1 = (VariableElement) member;
			}
		}
		assertNotNull("Method should not be null", method);
		TypeMirror typeMirror = method.getReturnType();
		assertNotNull("Java8ElementProcessor#examineSE8Specifics: Element for method foo should not be null", typeMirror);
		examineSE8AnnotationMethods("Java8ElementProcessor#examineSE8Specifics: ", typeMirror, "m");
		List<? extends AnnotationMirror> list = typeMirror.getAnnotationMirrors();
		assertEquals("Java8ElementProcessor#examineSE8Specifics: Incorrect no of annotation mirrors", 1, list.size());
		assertNotNull("Java8ElementProcessor#examineSE8Specifics: Element for field _field should not be null", field);
		typeMirror = field.asType(); 
		examineSE8AnnotationMethods("Java8ElementProcessor#examineSE8Specifics: ", typeMirror, "f");
		
		typeMirror = field1.asType();
		verifyTypeAnnotations(typeMirror, new String[]{"@Type(value=f1)"});
		
		typeMirror = method2.getReturnType();
		assertNotNull("Java8ElementProcessor#examineSE8Specifics: Element for method noAnnotationHere should not be null", typeMirror);
		Type annot = typeMirror.getAnnotation(Type.class);
		assertNull("Annotation should not be present", annot);
		Annotation[] annots = typeMirror.getAnnotationsByType(Type.class);
		assertEquals("Annotation is not empty list", 0, annots.length);
	}
	
	public void testTypeAnnotations() {
		TypeElement annotatedType = _elementUtils.getTypeElement("targets.model8.X");
		TypeMirror superType = annotatedType.getSuperclass();
		assertNotNull("Java8ElementProcessor#examineSE8Specifics: super type not be null", superType);
		verifyTypeAnnotations(superType, new String[]{"@Type(value=s)"});

		List<? extends TypeMirror> interfaces  = annotatedType.getInterfaces();
		assertNotNull("Java8ElementProcessor#examineSE8Specifics: super interfaces list should not be null", interfaces);
		assertEquals("Java8ElementProcessor#examineSE8Specifics: incorrect no of super interfaces", 2, interfaces.size());
		superType = interfaces.get(0);
		verifyTypeAnnotations(superType, new String[]{"@Type(value=i1)"});
		superType = interfaces.get(1);
		verifyTypeAnnotations(superType, new String[]{"@Type(value=i2)"});
	}
	
	public void testTypeAnnotations1() {
		TypeElement annotatedType = _elementUtils.getTypeElement("targets.model8.X");
		List<? extends Element> members = _elementUtils.getAllMembers(annotatedType);
		ExecutableElement method = null;
		for (Element member : members) {
			if ("bar".equals(member.getSimpleName().toString())) {
				method = (ExecutableElement) member;
			}
		}
		List<? extends VariableElement> params = method.getParameters();
		assertEquals("Incorrect no of params for method bar()", 2, params.size());
		VariableElement param = (VariableElement) params.get(0);
		TypeMirror typeMirror = param.asType();
		verifyTypeAnnotations(typeMirror, new String[]{"@Type(value=p1)"});
		param = (VariableElement) params.get(1);
		typeMirror = param.asType();
		verifyTypeAnnotations(typeMirror, new String[]{"@Type(value=p2)"});
	}
	
	public void testTypeAnnotations2() {
		TypeElement annotatedType = _elementUtils.getTypeElement("targets.model8.Y");
		List<? extends Element> members = _elementUtils.getAllMembers(annotatedType);
		VariableElement field2 = null;
		VariableElement field3 = null;
		for (Element member : members) {
			if ("_field2".equals(member.getSimpleName().toString())) {
				field2 = (VariableElement) member;
			} else if ("_field3".equals(member.getSimpleName().toString())) {
				field3 = (VariableElement) member;
			}
		}
		
		//@Type("f") String @Type("f1") [] @Type("f2") [] _field2 @Type("f3") [], _field3 @Type("f4") [][] = null;
		assertNotNull("Java8ElementProcessor#examineSE8Specifics: Element for field _field2 should not be null", field2);
		TypeMirror typeMirror = field2.asType();
		assertEquals("Should be an array type", TypeKind.ARRAY, typeMirror.getKind());
		verifyTypeAnnotations(typeMirror, new String[]{"@Type(value=f3)"});
		typeMirror = ((ArrayType) typeMirror).getComponentType();
		assertEquals("Should be an array type", TypeKind.ARRAY, typeMirror.getKind());
		verifyTypeAnnotations(typeMirror, new String[]{"@Type(value=f1)"});
		typeMirror = ((ArrayType) typeMirror).getComponentType();
		assertEquals("Should be an array type", TypeKind.ARRAY, typeMirror.getKind());
		verifyTypeAnnotations(typeMirror, new String[]{"@Type(value=f2)"});

		assertNotNull("Java8ElementProcessor#examineSE8Specifics: Element for field _field3 should not be null", field3);
		typeMirror = field3.asType();
		// The second field binding doesn't seem to have the annotations. To be investigated
		verifyTypeAnnotations(typeMirror, new String[]{"@Type(value=f4)"});
		typeMirror = ((ArrayType) typeMirror).getComponentType();
		assertEquals("Should be an array type", TypeKind.ARRAY, typeMirror.getKind());
		verifyTypeAnnotations(typeMirror, new String[]{});
		typeMirror = ((ArrayType) typeMirror).getComponentType();
		assertEquals("Should be an array type", TypeKind.ARRAY, typeMirror.getKind());
		verifyTypeAnnotations(typeMirror, new String[]{"@Type(value=f1)"});
		typeMirror = ((ArrayType) typeMirror).getComponentType();
		assertEquals("Should be an array type", TypeKind.ARRAY, typeMirror.getKind());
		verifyTypeAnnotations(typeMirror, new String[]{"@Type(value=f2)"});
	}
	
	public void testTypeAnnotations3() {
		TypeElement annotatedType = _elementUtils.getTypeElement("targets.model8.Y");
		List<? extends Element> members = _elementUtils.getAllMembers(annotatedType);
		ExecutableElement method = null;
		for (Element member : members) {
			if ("foo".equals(member.getSimpleName().toString())) {
				method = (ExecutableElement) member;
			}
		}
		// @Type("m") String @Type("m1") [] foo() @Type("m2") [] @Type("m3") [] {}
		assertNotNull("Method should not be null", method);
		TypeMirror typeMirror = method.getReturnType();
		verifyTypeAnnotations(typeMirror, new String[]{"@Type(value=m2)"});
		assertEquals("Should be an array type", TypeKind.ARRAY, typeMirror.getKind());
		typeMirror = ((ArrayType) typeMirror).getComponentType();
		verifyTypeAnnotations(typeMirror, new String[]{"@Type(value=m3)"});
		assertEquals("Should be an array type", TypeKind.ARRAY, typeMirror.getKind());
		typeMirror = ((ArrayType) typeMirror).getComponentType();
		verifyTypeAnnotations(typeMirror, new String[]{"@Type(value=m1)"});
		assertEquals("Should be an array type", TypeKind.ARRAY, typeMirror.getKind());
		typeMirror = ((ArrayType) typeMirror).getComponentType();
		verifyTypeAnnotations(typeMirror, new String[]{"@Type(value=m)"});
	}

	public void testTypeAnnotations4() {
		// void bar( @Type("p1") String [] a @Type("p2") [], @Type("p3") int @Type("p4") [] b [] @Type("p5") []) {}
		TypeElement annotatedType = _elementUtils.getTypeElement("targets.model8.Y");
		List<? extends Element> members = _elementUtils.getAllMembers(annotatedType);
		ExecutableElement method = null;
		for (Element member : members) {
			if ("bar".equals(member.getSimpleName().toString())) {
				method = (ExecutableElement) member;
			}
		}
		assertNotNull("Method should not be null", method);
		List<? extends VariableElement> params = method.getParameters();
		assertEquals("Incorrect no of params for method bar()", 2, params.size());
		VariableElement param = (VariableElement) params.get(0);
		TypeMirror typeMirror = param.asType();
		verifyTypeAnnotations(typeMirror, new String[]{"@Type(value=p2)"});
		assertEquals("Should be an array type", TypeKind.ARRAY, typeMirror.getKind());
		typeMirror = ((ArrayType) typeMirror).getComponentType();
		
		verifyTypeAnnotations(typeMirror, new String[]{});
		assertEquals("Should be an array type", TypeKind.ARRAY, typeMirror.getKind());
		typeMirror = ((ArrayType) typeMirror).getComponentType();
		verifyTypeAnnotations(typeMirror, new String[]{"@Type(value=p1)"});

		param = (VariableElement) params.get(1);
		typeMirror = param.asType();
		verifyTypeAnnotations(typeMirror, new String[]{});
		assertEquals("Should be an array type", TypeKind.ARRAY, typeMirror.getKind());
		typeMirror = ((ArrayType) typeMirror).getComponentType();
		
		verifyTypeAnnotations(typeMirror, new String[]{"@Type(value=p5)"});
		assertEquals("Should be an array type", TypeKind.ARRAY, typeMirror.getKind());
		typeMirror = ((ArrayType) typeMirror).getComponentType();
		verifyTypeAnnotations(typeMirror, new String[]{"@Type(value=p4)"});
		assertEquals("Should be an array type", TypeKind.ARRAY, typeMirror.getKind());
		typeMirror = ((ArrayType) typeMirror).getComponentType();
		verifyTypeAnnotations(typeMirror, new String[]{"@Type(value=p3)"});
		
	}
	
	public void testTypeAnnotations5() {
		// void foo2() throws (@Type("e1") NullPointerException, (@Type("e2") ArrayIndexOutOfBoundsException {}
		TypeElement annotatedType = _elementUtils.getTypeElement("targets.model8.Y");
		List<? extends Element> members = _elementUtils.getAllMembers(annotatedType);
		ExecutableElement method = null;
		for (Element member : members) {
			if ("foo2".equals(member.getSimpleName().toString())) {
				method = (ExecutableElement) member;
			}
		}
		List<?extends TypeMirror> exceptions = method.getThrownTypes();
		assertEquals("Incorrect no of thrown exceptions", 2, exceptions.size());
		TypeMirror typeMirror = exceptions.get(0);
		verifyTypeAnnotations(typeMirror, new String[]{"@Type(value=e1)"});
		typeMirror = exceptions.get(1);
		verifyTypeAnnotations(typeMirror, new String[]{"@Type(value=e2)"});
	}

	public void testTypeAnnotations6() {
		// void bar2 (@Type("p1") String @Type("p2") [] @Type("p3") ... args) {}
		TypeElement annotatedType = _elementUtils.getTypeElement("targets.model8.Y");
		List<? extends Element> members = _elementUtils.getAllMembers(annotatedType);
		ExecutableElement method = null;
		for (Element member : members) {
			if ("bar2".equals(member.getSimpleName().toString())) {
				method = (ExecutableElement) member;
			}
		}
		List<? extends VariableElement> params = method.getParameters();
		assertEquals("Incorrect no of parameters", 1, params.size());
		TypeMirror typeMirror = params.get(0).asType();
		verifyTypeAnnotations(typeMirror, new String[]{"@Type(value=p2)"});
		assertEquals("Should be an array type", TypeKind.ARRAY, typeMirror.getKind());
		typeMirror = ((ArrayType) typeMirror).getComponentType();
		verifyTypeAnnotations(typeMirror, new String[]{"@Type(value=p3)"});
		assertEquals("Should be an array type", TypeKind.ARRAY, typeMirror.getKind());
		typeMirror = ((ArrayType) typeMirror).getComponentType();
		verifyTypeAnnotations(typeMirror, new String[]{"@Type(value=p1)"});

	}

	public void testTypeAnnotations7() {
		// public class Z <@Type("tp1") K, @Type("tp2") V> {
		TypeElement typeZ = _elementUtils.getTypeElement("targets.model8.Z");
		TypeMirror typeMirror = typeZ.asType();
		List<? extends TypeParameterElement> typeParams = typeZ.getTypeParameters();
		assertEquals("Incorrect no of type params", 2, typeParams.size());
		TypeParameterElement typeParam = typeParams.get(0);
		verifyTypeAnnotations(typeParam, new String[]{"@Type(value=tp1)"});
		typeMirror = typeParam.asType();
		verifyTypeAnnotations(typeMirror, new String[]{"@Type(value=tp1)"});
		typeParam = typeParams.get(1);
		typeMirror = typeParam.asType();
		verifyTypeAnnotations(typeParam, new String[]{"@Type(value=tp2)"});
		verifyTypeAnnotations(typeMirror, new String[]{"@Type(value=tp2)"});
	}
	
	public void testTypeAnnotations8() {
		TypeElement typeZ = _elementUtils.getTypeElement("targets.model8.Z");
		List<? extends Element> members = _elementUtils.getAllMembers(typeZ);
		ExecutableElement method = null;
		VariableElement field = null;
		for (Element member : members) {
			if ("foo".equals(member.getSimpleName().toString())) {
				method = (ExecutableElement) member;
			} else if ("z1".equals(member.getSimpleName().toString())) {
				field = (VariableElement) member;
			}
		}
		
		// public <@Type("mp1") T, @Type("mp2") U> void foo() {}	
		List<? extends TypeParameterElement> typeParams = method.getTypeParameters();
		assertEquals("Incorrect no of type params", 2, typeParams.size());
		TypeParameterElement typeParam = typeParams.get(0);
		verifyTypeAnnotations(typeParam, new String[]{"@Type(value=mp1)"});
		typeParam = typeParams.get(1);
		verifyTypeAnnotations(typeParam, new String[]{"@Type(value=mp2)"});
		//Z<@Type("ta1") String, @Type("ta2") Object> z1 = null;
		// APIs don't expose the type arguments on a TypeMirror
		TypeMirror typeMirror = field.asType();
		verifyTypeAnnotations(typeMirror, new String[]{});
	}

	public void testTypeAnnotations9() {
		TypeElement annotatedType = _elementUtils.getTypeElement("targets.model8.X");
		List<? extends Element> members = _elementUtils.getAllMembers(annotatedType);
		
		VariableElement field2 = null;
		for (VariableElement member : ElementFilter.fieldsIn(members)) {
			if ("_field2".equals(member.getSimpleName().toString())) {
				field2 = (VariableElement) member;
				break;
			}
		}
		TypeMirror typeMirror = field2.asType();
		Type$1 annot1 = typeMirror.getAnnotation(Type$1.class);
		assertNotNull("Annotation should not be null", annot1);
		Type.One annot2 = typeMirror.getAnnotation(Type.One.class);
		assertNotNull("Annotation should not be null", annot2);
	}
	
	public void testTypeAnnotations10() {
		TypeElement annotatedType = _elementUtils.getTypeElement("targets.model8.X");
		List<? extends Element> members = _elementUtils.getAllMembers(annotatedType);
		VariableElement field3 = null;
		for (Element member : members) {
			if ("_field3".equals(member.getSimpleName().toString())) {
				field3 = (VariableElement) member;
			}
		}
		verifyTypeAnnotations(annotatedType, new String[]{"@Type(value=c)"});
		verifyTypeAnnotations(annotatedType.asType(), new String[]{});
		verifyTypeAnnotations(field3, new String[]{});
		verifyTypeAnnotations(field3.asType(), new String[]{});
	}

	public void testTypeAnnotations11() {
		TypeElement annotatedType = _elementUtils.getTypeElement("targets.model8.X");
		List<? extends Element> members = _elementUtils.getAllMembers(annotatedType);
		VariableElement xy = null;
		for (Element member : members) {
			if ("xy".equals(member.getSimpleName().toString())) {
				xy = (VariableElement) member;
			}
		}
		verifyTypeAnnotations(xy, new String[]{});
		verifyTypeAnnotations(xy.asType(), new String[]{"@Type(value=xy)"});
		
		Set<String> expectedElementNames = new HashSet<String>(ELEMENT_NAMES.length);
		for (String name : ELEMENT_NAMES) {
			expectedElementNames.add(name);
		}
		Set<? extends Element> actualElments = roundEnv.getElementsAnnotatedWith(Type.class);
		assertNotNull("RoundEnvironment#getElementsAnnotatedWith returned null", actualElments);
		
		for (Element e : actualElments) {
			if (e instanceof TypeElement) {
				String name = ((TypeElement) e).getQualifiedName().toString();
				if (!expectedElementNames.remove(name)) {
					reportError("Missing root element " + name);
				}
			} else if (e instanceof TypeParameterElement) {
				String name = ((TypeParameterElement) e).getSimpleName().toString();
				if (!expectedElementNames.remove(name)) {
					reportError("Missing root element " + name);
				}
			}
		}
		assertTrue("Found unexpected extra elements", expectedElementNames.isEmpty());
	}

	public void testTypeAnnotations12() {
		TypeElement annotatedType = _elementUtils.getTypeElement("targets.model8.X");
		List<? extends Element> members = _elementUtils.getAllMembers(annotatedType);
		ExecutableElement bar2 = null;
		for (Element member : members) {
			if ("bar2".equals(member.getSimpleName().toString())) {
				bar2 = (ExecutableElement) member;
			}
		}
		TypeMirror typeMirror = bar2.getReceiverType();
		verifyTypeAnnotations(typeMirror, new String[]{"@Type(value=receiver)"});
		ExecutableType type = (ExecutableType) bar2.asType();
		verifyTypeAnnotations(type.getReceiverType(), new String[]{"@Type(value=receiver)"});
	}
	private String getExceptionStackTrace(Throwable t) {
		StringBuffer buf = new StringBuffer(t.getMessage());
		StackTraceElement[] traces = t.getStackTrace();
		for (int i = 0; i < traces.length; i++) {
			StackTraceElement trace = traces[i];
			buf.append("\n\tat " + trace);
			if (i == 12)
				break; // Don't dump all stacks
		}
		return buf.toString();
	}

	
	private void verifyTypeAnnotations(AnnotatedConstruct construct, String[] annots) {
		List<? extends AnnotationMirror> annotations = construct.getAnnotationMirrors();
		assertEquals("Incorrect no of annotations", annots.length, annotations.size());
		for(int i = 0, length = annots.length; i < length; i++) {
			AnnotationMirror mirror = annotations.get(i);
			assertEquals("Invalid annotation value", annots[i], getAnnotationString(mirror));
		}
	}
	private String getAnnotationString(AnnotationMirror annot) {
		DeclaredType annotType = annot.getAnnotationType();
		TypeElement type = (TypeElement) annotType.asElement();
		StringBuffer buf = new StringBuffer("@" + type.getSimpleName());
		Map<? extends ExecutableElement, ? extends AnnotationValue> values = annot.getElementValues();
		Set<? extends ExecutableElement> keys = values.keySet();
		buf.append('(');
		for (ExecutableElement executableElement : keys) { // @Marker3()
			buf.append(executableElement.getSimpleName());
			buf.append('=');
			AnnotationValue value = values.get(executableElement);
			buf.append(value.getValue());
		}
		buf.append(')');
		return buf.toString();
	}
	private <A extends Annotation> void examineSE8AnnotationMethods(String msg, AnnotatedConstruct construct,  String value) {
		Type annot = construct.getAnnotation(Type.class);
		assertNotNull(msg + "Annotation for element " + construct.toString() + " should not be null", annot);
		assertSame(msg + "Invalid annotation type" , Type.class, annot.annotationType());
		assertEquals(msg + "Invalid annotation value", value, annot.value());
		
		Annotation[] annots = construct.getAnnotationsByType(Type.class);
		assertEquals(msg + "Incorrect no of annotations", 1, annots.length);
		annot = (Type) annots[0];
		assertSame(msg + "Invalid annotation type" , Type.class, annots[0].annotationType());
		assertEquals(msg + "Invalid annotation value", value, annot.value());
	}
	
	public void reportError(String msg) {
		throw new AssertionFailedError(msg);
	}
	
	public void assertTrue(String msg, boolean value) {
		if (!value) reportError(msg);
	}
	public void assertFalse(String msg, boolean value) {
		if (value) reportError(msg);
	}
	public void assertSame(String msg, Object obj1, Object obj2) {
		if (obj1 != obj2) {
			reportError(msg + ", should be " + obj1.toString() + " but " + obj2.toString());
		}
	}
	public void assertNotNull(String msg, Object obj) {
		if (obj == null) {
			reportError(msg);
		}
	}
	public void assertNull(String msg, Object obj) {
		if (obj != null) {
			reportError(msg);
		}
	}
    public void assertEquals(String message, Object expected, Object actual) {
        if (equalsRegardingNull(expected, actual)) {
            return;
        } else {
        	reportError(message + ", expected " + expected.toString() + " but was " + actual.toString());
        }
    }

    static boolean equalsRegardingNull(Object expected, Object actual) {
        if (expected == null) {
            return actual == null;
        }
        return expected.equals(actual);
    }
    
	public void assertEquals(String msg, int expected, int actual) {
		if (expected != actual) {
			StringBuffer buf = new StringBuffer();
			buf.append(msg);
			buf.append(", expected " + expected + " but was " + actual);
			reportError(buf.toString());
		}
	}
	public void assertEquals(Object expected, Object actual) {
		if (expected != actual) {
			
		}
	}
	private class AssertionFailedError extends Error {
		private static final long serialVersionUID = 1L;

		public AssertionFailedError(String msg) {
			super(msg);
		}
	}
	
}
