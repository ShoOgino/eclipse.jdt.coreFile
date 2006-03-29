/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.rewrite.describing;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;

public class ASTRewritingTrackingTest extends ASTRewritingTest {

	private static final Class THIS= ASTRewritingTrackingTest.class;
	
	public ASTRewritingTrackingTest(String name) {
		super(name);
	}
	
	public static Test allTests() {
		return new Suite(THIS);
	}
	
	public static Test setUpTest(Test someTest) {
		TestSuite suite= new Suite("one test");
		suite.addTest(someTest);
		return suite;
	}
	
	public static Test suite() {
		return allTests();
	}
	
	public void testNamesWithDelete() throws Exception {
		
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("\n");
		buf.append("    public int x1;\n");
		buf.append("\n");
		buf.append("    public void foo(String s, int i) {\n");
		buf.append("        while (i == 0) {\n");
		buf.append("            i--;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");		
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);
	
		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		ASTRewrite rewrite= ASTRewrite.create(ast);
		
		ArrayList names= new ArrayList();
		ArrayList positions= new ArrayList();
		
		TypeDeclaration typeC= findTypeDeclaration(astRoot, "C");
		ITrackedNodePosition position= rewrite.track(typeC.getName());
		names.add("C");
		positions.add(position);
		
		List decls= typeC.bodyDeclarations();
		
		MethodDeclaration method= (MethodDeclaration) decls.get(1);
		ITrackedNodePosition position2= rewrite.track(method.getName());
		names.add("foo");
		positions.add(position2);
		
		FieldDeclaration field= (FieldDeclaration) decls.get(0);
		rewrite.remove(field, null);
						
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("\n");
		buf.append("    public void foo(String s, int i) {\n");
		buf.append("        while (i == 0) {\n");
		buf.append("            i--;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");	
		String expected= buf.toString();
		assertEqualString(preview, expected);
		
		assertCorrectTracking(names, positions, expected);

	}
	
	private void assertCorrectTracking(List names, List positions, String expected) {
		for (int i= 0; i < names.size(); i++) {
			String name= (String) names.get(i);
			ITrackedNodePosition pos= (ITrackedNodePosition) positions.get(i);
			String string= expected.substring(pos.getStartPosition(), pos.getStartPosition() + pos.getLength());
			assertEqualString(string, name);
		}
	}

	public void testNamesWithInsert() throws Exception {
		
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("\n");
		buf.append("    public int x1;\n");
		buf.append("\n");
		buf.append("    public void foo(String s, int i) {\n");
		buf.append("        while (i == 0) {\n");
		buf.append("            i--;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");	
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);
	
		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		
		ASTRewrite rewrite= ASTRewrite.create(ast);
		
		ArrayList names= new ArrayList();
		ArrayList positions= new ArrayList();
		
		TypeDeclaration typeC= findTypeDeclaration(astRoot, "C");
		ITrackedNodePosition position= rewrite.track(typeC.getName());
		names.add("C");
		positions.add(position);
		
		List decls= typeC.bodyDeclarations();
		
		MethodDeclaration method= (MethodDeclaration) decls.get(1);
		position= rewrite.track(method.getName());
		names.add("foo");
		positions.add(position);
		
		FieldDeclaration field= (FieldDeclaration) decls.get(0);
		List fragments= field.fragments();
		VariableDeclarationFragment frag1= (VariableDeclarationFragment) fragments.get(0);
		position= rewrite.track(frag1.getName());
		names.add("x1");
		positions.add(position);
		
		VariableDeclarationFragment newFrag= ast.newVariableDeclarationFragment();
		newFrag.setName(ast.newSimpleName("newVariable"));
		newFrag.setExtraDimensions(2);

		rewrite.getListRewrite(field, FieldDeclaration.FRAGMENTS_PROPERTY).insertFirst(newFrag, null);

						
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("\n");
		buf.append("    public int newVariable[][], x1;\n");
		buf.append("\n");
		buf.append("    public void foo(String s, int i) {\n");
		buf.append("        while (i == 0) {\n");
		buf.append("            i--;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");	
		String expected= buf.toString();
		assertEqualString(preview, expected);

		assertCorrectTracking(names, positions, expected);

	}
	
	public void testNamesWithReplace() throws Exception {
		
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("\n");
		buf.append("    public int x1;\n");
		buf.append("\n");
		buf.append("    public void foo(String s, int i) {\n");
		buf.append("        while (i == 0) {\n");
		buf.append("            ++i;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");	
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);
	
		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		
		ASTRewrite rewrite= ASTRewrite.create(ast);
		
		ArrayList names= new ArrayList();
		ArrayList positions= new ArrayList();
		
		// change type name
		TypeDeclaration typeC= findTypeDeclaration(astRoot, "C");
		SimpleName newName= ast.newSimpleName("XX");
		rewrite.replace(typeC.getName(), newName, null);
		ITrackedNodePosition position= rewrite.track(newName);
		names.add("XX");
		positions.add(position);
		
		List decls= typeC.bodyDeclarations();
		
		MethodDeclaration method= (MethodDeclaration) decls.get(1);
		position= rewrite.track(method.getName());
		names.add("foo");
		positions.add(position);
		
		WhileStatement whileStatement= (WhileStatement) method.getBody().statements().get(0);
		PrefixExpression prefixExpression= (PrefixExpression) ((ExpressionStatement) ((Block) whileStatement.getBody()).statements().get(0)).getExpression();
		position= rewrite.track(prefixExpression.getOperand());
		names.add("i");
		positions.add(position);
				
		FieldDeclaration field= (FieldDeclaration) decls.get(0);
		List fragments= field.fragments();
		VariableDeclarationFragment frag1= (VariableDeclarationFragment) fragments.get(0);
		position= rewrite.track(frag1.getName());
		names.add("x1");
		positions.add(position);
		
		// change modifier
		int newModifiers= Modifier.STATIC | Modifier.TRANSIENT | Modifier.PRIVATE;
		rewrite.set(field, FieldDeclaration.MODIFIERS_PROPERTY, new Integer(newModifiers), null);
								
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class XX {\n");
		buf.append("\n");
		buf.append("    private static transient int x1;\n");
		buf.append("\n");
		buf.append("    public void foo(String s, int i) {\n");
		buf.append("        while (i == 0) {\n");
		buf.append("            ++i;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");	
		String expected= buf.toString();
		assertEqualString(preview, expected);
		
		assertCorrectTracking(names, positions, expected);
	}
	
	public void testNamesWithMove1() throws Exception {
		
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("\n");
		buf.append("    public int x1;\n");
		buf.append("\n");
		buf.append("    public void foo(String s, int i) {\n");
		buf.append("        while (i == 0) {\n");
		buf.append("            ++i;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");	
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);
	
		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		ASTRewrite rewrite= ASTRewrite.create(ast);
		
		ArrayList names= new ArrayList();
		ArrayList positions= new ArrayList();
		
		// change type name
		TypeDeclaration typeC= findTypeDeclaration(astRoot, "C");
		ITrackedNodePosition position= rewrite.track(typeC.getName());
		names.add("C");
		positions.add(position);
		
		List decls= typeC.bodyDeclarations();
		
		MethodDeclaration method= (MethodDeclaration) decls.get(1);
		position= rewrite.track(method.getName());
		names.add("foo");
		positions.add(position);
		
		WhileStatement whileStatement= (WhileStatement) method.getBody().statements().get(0);
		PrefixExpression prefixExpression= (PrefixExpression) ((ExpressionStatement) ((Block) whileStatement.getBody()).statements().get(0)).getExpression();
		position= rewrite.track(prefixExpression.getOperand());
		names.add("i");
		positions.add(position);
					
		FieldDeclaration field= (FieldDeclaration) decls.get(0);
		List fragments= field.fragments();
		VariableDeclarationFragment frag1= (VariableDeclarationFragment) fragments.get(0);
		position= rewrite.track(frag1.getName());
		names.add("x1");
		positions.add(position);

		// move method before field
		ASTNode placeHolder= rewrite.createMoveTarget(method);
		rewrite.getListRewrite(typeC, TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertFirst(placeHolder, null);
								
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("\n");
		buf.append("    public void foo(String s, int i) {\n");
		buf.append("        while (i == 0) {\n");
		buf.append("            ++i;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    public int x1;\n");
		buf.append("}\n");	
		String expected= buf.toString();
		assertEqualString(preview, expected);
		
		assertCorrectTracking(names, positions, expected);

	}
	
	public void testNamesWithMove2() throws Exception {
		
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("    public void foo(String s, int i) {\n");
		buf.append("        while (i == 0) {\n");
		buf.append("            ++i;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");	
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);
	
		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		ASTRewrite rewrite= ASTRewrite.create(ast);
		
		ArrayList names= new ArrayList();
		ArrayList positions= new ArrayList();
		
		// change type name
		TypeDeclaration typeC= findTypeDeclaration(astRoot, "C");
		ITrackedNodePosition position= rewrite.track(typeC.getName());
		names.add("C");
		positions.add(position);
		
		List decls= typeC.bodyDeclarations();
		
		MethodDeclaration method= (MethodDeclaration) decls.get(0);
		position= rewrite.track(method.getName());
		names.add("foo");
		positions.add(position);
		
		WhileStatement whileStatement= (WhileStatement) method.getBody().statements().get(0);
		PrefixExpression prefixExpression= (PrefixExpression) ((ExpressionStatement) ((Block) whileStatement.getBody()).statements().get(0)).getExpression();
		position= rewrite.track(prefixExpression.getOperand());
		names.add("i");
		positions.add(position);

		// move method before field
		ASTNode placeHolder= rewrite.createMoveTarget(whileStatement);
		
		TryStatement tryStatement= ast.newTryStatement();
		tryStatement.getBody().statements().add(placeHolder);
		tryStatement.setFinally(ast.newBlock());
		rewrite.replace(whileStatement, tryStatement, null);
								
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("    public void foo(String s, int i) {\n");
		buf.append("        try {\n");
		buf.append("            while (i == 0) {\n");
		buf.append("                ++i;\n");
		buf.append("            }\n");
		buf.append("        } finally {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");	
		String expected= buf.toString();
		assertEqualString(preview, expected);
		
		assertCorrectTracking(names, positions, expected);
	}	
	
	public void testNamesWithMove3() throws Exception {
		
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("\n");
		buf.append("    public int x1;\n");
		buf.append("\n");
		buf.append("    public void foo(String s, int i) {\n");
		buf.append("    }\n");
		buf.append("}\n");	
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);
	
		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		ASTRewrite rewrite= ASTRewrite.create(ast);
		
		ArrayList names= new ArrayList();
		ArrayList positions= new ArrayList();
		
		// change type name
		TypeDeclaration typeC= findTypeDeclaration(astRoot, "C");
		ITrackedNodePosition position= rewrite.track(typeC.getName());
		names.add("C");
		positions.add(position);
		
		List decls= typeC.bodyDeclarations();
		
		MethodDeclaration method= (MethodDeclaration) decls.get(1);
		position=  rewrite.track(method.getName());
		names.add("foo");
		positions.add(position);
		
		// move method before field
		ASTNode placeHolder= rewrite.createMoveTarget(method);
		
		rewrite.getListRewrite(typeC, TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertFirst(placeHolder, null);
								
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("\n");
		buf.append("    public void foo(String s, int i) {\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    public int x1;\n");
		buf.append("}\n");	
		String expected= buf.toString();
		assertEqualString(preview, expected);
		
		assertCorrectTracking(names, positions, expected);

	}
	public void testNamesWithPlaceholder() throws Exception {
		
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("    public String foo(Object s) {\n");
		buf.append("        return s;\n");
		buf.append("    }\n");
		buf.append("}\n");	
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);
	
		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		ASTRewrite rewrite= ASTRewrite.create(ast);
		
		ArrayList names= new ArrayList();
		ArrayList positions= new ArrayList();
		
		// change type name
		TypeDeclaration typeC= findTypeDeclaration(astRoot, "C");
		ITrackedNodePosition position= rewrite.track(typeC.getName());
		names.add("C");
		positions.add(position);
		
		List decls= typeC.bodyDeclarations();
		
		MethodDeclaration method= (MethodDeclaration) decls.get(0);
		position= rewrite.track(method.getName());
		names.add("foo");
		positions.add(position);
		
		ReturnStatement returnStatement= (ReturnStatement) method.getBody().statements().get(0);
		
		CastExpression castExpression= ast.newCastExpression();
		Type type= (Type) rewrite.createStringPlaceholder("String", ASTNode.SIMPLE_TYPE);
		Expression expression= (Expression) rewrite.createMoveTarget(returnStatement.getExpression());
		castExpression.setType(type);
		castExpression.setExpression(expression);
		
		rewrite.replace(returnStatement.getExpression(), castExpression, null);
		
		position= rewrite.track(type);
		names.add("String");
		positions.add(position);
		
		position= rewrite.track(expression);
		names.add("s");
		positions.add(position);
		
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("    public String foo(Object s) {\n");
		buf.append("        return (String) s;\n");
		buf.append("    }\n");
		buf.append("}\n");	
		String expected= buf.toString();
		assertEqualString(preview, expected);
		
		assertCorrectTracking(names, positions, expected);

	}	

	
}



