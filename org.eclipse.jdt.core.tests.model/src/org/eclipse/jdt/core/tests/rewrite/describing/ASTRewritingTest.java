/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.rewrite.describing;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jface.text.Document;

import org.eclipse.text.edits.TextEdit;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.core.tests.model.AbstractJavaModelTests;

/**
  */
public class ASTRewritingTest extends AbstractJavaModelTests {
	protected IJavaProject project1;
	protected IPackageFragmentRoot sourceFolder;
	
	public static Test suite() {
		TestSuite suite= new TestSuite();
		suite.addTest(ASTRewritingExpressionsTest.allTests());
		suite.addTest(ASTRewritingInsertBoundTest.allTests());
		suite.addTest(ASTRewritingMethodDeclTest.allTests());
		suite.addTest(ASTRewritingMoveCodeTest.allTests());
		suite.addTest(ASTRewritingStatementsTest.allTests());
		suite.addTest(ASTRewritingTrackingTest.allTests());
		suite.addTest(ASTRewritingJavadocTest.allTests());
		suite.addTest(ASTRewritingTypeDeclTest.allTests());
		suite.addTest(SourceModifierTest.allTests());
		suite.addTest(ASTRewritingJavadocTest.allTests());
		
		return suite;
	}

	
	public ASTRewritingTest(String name) {
		super(name);
	}
	
	public void setUpSuite() throws Exception {
		super.setUpSuite();
	}
	
	public void tearDownSuite() throws Exception {
		super.tearDownSuite();
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		
		IJavaProject proj= createJavaProject("P", new String[] {"src"}, "bin");
		proj.setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
		proj.setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
		
		this.project1 = proj;
		this.sourceFolder = this.getPackageFragmentRoot("P", "src");
		
		waitUntilIndexesReady();
	}
	
	protected void tearDown() throws Exception {
		deleteProject("P");
		super.tearDown();
	}
		
	protected CompilationUnit createAST(ICompilationUnit cu) {
		ASTParser parser= ASTParser.newParser(AST.JLS2);
		parser.setSource(cu);
		parser.setResolveBindings(false);
		return (CompilationUnit) parser.createAST(null);
	}
	
	protected CompilationUnit createAST3(ICompilationUnit cu) {
		ASTParser parser= ASTParser.newParser(AST.JLS3);
		parser.setSource(cu);
		parser.setResolveBindings(false);
		return (CompilationUnit) parser.createAST(null);
	}
	
	protected String evaluateRewrite(ICompilationUnit cu, ASTRewrite rewrite) throws Exception {
		Document document= new Document(cu.getSource());
		TextEdit res= rewrite.rewriteAST(document, cu.getJavaProject().getOptions(true));
		
		res.apply(document);
		return document.get();
	}
	
	
	public static void assertEqualString(String actual, String expected) {
		StringAsserts.assertEqualString(actual, expected);
	}
	
	public static TypeDeclaration findTypeDeclaration(CompilationUnit astRoot, String simpleTypeName) {
		List types= astRoot.types();
		for (int i= 0; i < types.size(); i++) {
			TypeDeclaration elem= (TypeDeclaration) types.get(i);
			if (simpleTypeName.equals(elem.getName().getIdentifier())) {
				return elem;
			}
		}
		return null;
	}
	
	public static MethodDeclaration findMethodDeclaration(TypeDeclaration typeDecl, String methodName) {
		MethodDeclaration[] methods= typeDecl.getMethods();
		for (int i= 0; i < methods.length; i++) {
			if (methodName.equals(methods[i].getName().getIdentifier())) {
				return methods[i];
			}
		}
		return null;
	}
	
	public static SingleVariableDeclaration createNewParam(AST ast, String name) {
		SingleVariableDeclaration newParam= ast.newSingleVariableDeclaration();
		newParam.setType(ast.newPrimitiveType(PrimitiveType.FLOAT));
		newParam.setName(ast.newSimpleName(name));
		return newParam;
	}
	
	protected FieldDeclaration createNewField(AST ast, String name) {
		VariableDeclarationFragment frag= ast.newVariableDeclarationFragment();
		frag.setName(ast.newSimpleName(name));
		FieldDeclaration newFieldDecl= ast.newFieldDeclaration(frag);
		if (ast.apiLevel() == AST.JLS2) {
			newFieldDecl.setModifiers(Modifier.PRIVATE);
		} else {
			newFieldDecl.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD));
		}
		newFieldDecl.setType(ast.newPrimitiveType(PrimitiveType.DOUBLE));
		return newFieldDecl;
	}
	
	protected MethodDeclaration createNewMethod(AST ast, String name, boolean isAbstract) {
		MethodDeclaration decl= ast.newMethodDeclaration();
		decl.setName(ast.newSimpleName(name));
		if (ast.apiLevel() == AST.JLS2) {
			decl.setModifiers(isAbstract ? (Modifier.ABSTRACT | Modifier.PRIVATE) : Modifier.PRIVATE);
			decl.setReturnType(ast.newPrimitiveType(PrimitiveType.VOID));
		} else {
			decl.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD));
			if (isAbstract) {
				decl.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.ABSTRACT_KEYWORD));
			}
			decl.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));
		}
		SingleVariableDeclaration param= ast.newSingleVariableDeclaration();
		param.setName(ast.newSimpleName("str"));
		param.setType(ast.newSimpleType(ast.newSimpleName("String")));
		decl.parameters().add(param);
		decl.setBody(isAbstract ? null : ast.newBlock());
		return decl;
	}

}
