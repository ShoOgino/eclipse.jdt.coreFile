/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.rewrite.describing;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchExpression;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import junit.framework.Test;

public class ASTRewritingSwitchExpressionsTest extends ASTRewritingTest {


	public ASTRewritingSwitchExpressionsTest(String name) {
		super(name, 12);
	}

	public ASTRewritingSwitchExpressionsTest(String name, int apiLevel) {
		super(name, apiLevel);
	}

	public static Test suite() {
		return createSuite(ASTRewritingSwitchExpressionsTest.class);
	}

	@SuppressWarnings("rawtypes")
	public void testSwitchExpressions_since_12() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int i) {\n");
		buf.append("        switch (i) {\n");
		buf.append("        }\n");
		buf.append("        switch (i) {\n");
		buf.append("            case 1, 2->\n");
		buf.append("                i= 1;\n");
		buf.append("            case 3->\n");
		buf.append("                i= 3;\n");
		buf.append("            default->\n");
		buf.append("                i= 4;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List blockStatements= block.statements();
		assertTrue("Number of statements not 2", blockStatements.size() == 2);
		{ // insert statements, replace expression
			SwitchStatement switchStatement= (SwitchStatement) blockStatements.get(0);

			ASTNode expression= switchStatement.getExpression();
			SimpleName newExpression= ast.newSimpleName("x");
			rewrite.replace(expression, newExpression, null);

			List statements= switchStatement.statements();
			assertTrue("Number of statements not 0", statements.size() == 0);

			SwitchCase caseStatement1= ast.newSwitchCase();
			caseStatement1.setSwitchLabeledRule(true);
			caseStatement1.expressions().add(ast.newNumberLiteral("1"));
			caseStatement1.expressions().add(ast.newNumberLiteral("2"));
			

			Statement statement1= ast.newReturnStatement();

			SwitchCase caseStatement2= ast.newSwitchCase(); // default
			caseStatement2.setSwitchLabeledRule(true);


			ListRewrite listRewrite= rewrite.getListRewrite(switchStatement, SwitchStatement.STATEMENTS_PROPERTY);
			listRewrite.insertLast(caseStatement1, null);
			listRewrite.insertLast(statement1, null);
			listRewrite.insertLast(caseStatement2, null);
		}

		{ // insert, remove, replace statements, change case statements
			SwitchStatement switchStatement= (SwitchStatement) blockStatements.get(1);

			List statements= switchStatement.statements();
			assertTrue("Number of statements not 6", statements.size() == 6);

			// remove statements

			rewrite.remove((ASTNode) statements.get(0), null);
			rewrite.remove((ASTNode) statements.get(1), null);

			// change case statement
			SwitchCase caseStatement= (SwitchCase) statements.get(2);
			ListRewrite listRewrite= rewrite.getListRewrite(caseStatement, SwitchCase.EXPRESSIONS2_PROPERTY);

			{
				listRewrite.insertFirst(ast.newNumberLiteral("10"), null);
				listRewrite.insertLast(ast.newNumberLiteral("12"), null);

			}

		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int i) {\n");
		buf.append("        switch (x) {\n");
		buf.append("            case 1, 2->\n");
		buf.append("                return;\n");
		buf.append("            default->\n");
		buf.append("        }\n");
		buf.append("        switch (i) {\n");
		buf.append("            case 10, 3, 12->\n");
		buf.append("                i= 3;\n");
		buf.append("            default->\n");
		buf.append("                i= 4;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	@SuppressWarnings("rawtypes")
	public void testSwitchExpressions_02_since_12() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int i) {\n");
		buf.append("        switch (i) {\n");
		buf.append("            case 1, 2->\n");
		buf.append("                i= 1;\n");
		buf.append("            case 3->\n");
		buf.append("                i= 3;\n");
		buf.append("            default->\n");
		buf.append("                i= 4;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List blockStatements= block.statements();
		{ // insert statements, replace expression
			SwitchStatement switchStatement= (SwitchStatement) blockStatements.get(0);

			SwitchCase caseStatement1= ast.newSwitchCase();
			caseStatement1.setSwitchLabeledRule(true);
			caseStatement1.expressions().add(ast.newNumberLiteral("1024"));

			BreakStatement breakStatement = ast.newBreakStatement();
			breakStatement.setExpression(ast.newNumberLiteral("2048"));
			ListRewrite listRewrite= rewrite.getListRewrite(switchStatement, SwitchStatement.STATEMENTS_PROPERTY);

			SwitchCase defaultCase = (SwitchCase) switchStatement.statements().get(4);
			listRewrite.insertBefore(caseStatement1, defaultCase, null);
			listRewrite.insertBefore(breakStatement, defaultCase, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int i) {\n");
		buf.append("        switch (i) {\n");
		buf.append("            case 1, 2->\n");
		buf.append("                i= 1;\n");
		buf.append("            case 3->\n");
		buf.append("                i= 3;\n");
		buf.append("            case 1024->\n");
		buf.append("                break 2048;\n");
		buf.append("            default->\n");
		buf.append("                i= 4;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	@SuppressWarnings("rawtypes")
	public void testSwitchExpressions_03_since_12() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String s	=
				"package test1;\n"+
				"public class X {\n"+
				"	static int foo(int i) {\n"+
				"		int tw = \n"+
				"		switch (i) {\n"+
				"			case 1 -> {\n"+
				" 				int z = 100;\n"+
				" 				break z;\n"+
				"			}\n"+
				"			default -> {\n"+
				"				break 12;\n"+
				"			}\n"+
				"		};\n"+
				"		return tw;\n"+
				"	}\n"+
				"	public static void main(String[] args) {\n"+
				"		System.out.print(foo(1));\n"+
				"	}\n"+
				"}\n";
		StringBuffer buf = new StringBuffer(s);
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List blockStatements= block.statements();
		{ // insert statements, replace expression
			VariableDeclarationStatement stmt = (VariableDeclarationStatement) blockStatements.get(0);
			SwitchExpression switchExpression= (SwitchExpression) ((VariableDeclarationFragment) stmt.fragments().get(0)).getInitializer();

			SwitchCase caseStatement1= ast.newSwitchCase();
			caseStatement1.setSwitchLabeledRule(true);
			caseStatement1.expressions().add(ast.newNumberLiteral("100"));
			caseStatement1.expressions().add(ast.newNumberLiteral("200"));

			SwitchCase caseStatement2= ast.newSwitchCase(); // default
			caseStatement2.setSwitchLabeledRule(true);

			BreakStatement breakStatement = ast.newBreakStatement();
			breakStatement.setExpression(ast.newNumberLiteral("2048"));
			Block block1 = ast.newBlock();
			block1.statements().add(breakStatement);

			SwitchCase defaultCase = (SwitchCase) switchExpression.statements().get(2);
			ListRewrite listRewrite= rewrite.getListRewrite(switchExpression, SwitchExpression.STATEMENTS_PROPERTY);
			listRewrite.insertBefore(caseStatement1, defaultCase, null);
			listRewrite.insertBefore(block1, defaultCase, null);
		}


		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class X {\n");
		buf.append("	static int foo(int i) {\n");
		buf.append("		int tw = \n");
		buf.append("		switch (i) {\n");
		buf.append("			case 1 -> {\n");
		buf.append(" 				int z = 100;\n");
		buf.append(" 				break z;\n");
		buf.append("			}\n");
		buf.append("			case 100, 200->\n");
		buf.append("                {\n");
		buf.append("                    break 2048;\n");
		buf.append("                }\n");
		buf.append("            default -> {\n");
		buf.append("				break 12;\n");
		buf.append("			}\n");
		buf.append("		};\n");
		buf.append("		return tw;\n");
		buf.append("	}\n");
		buf.append("	public static void main(String[] args) {\n");
		buf.append("		System.out.print(foo(1));\n");
		buf.append("	}\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	@SuppressWarnings("rawtypes")
	public void testSwitchStatement_Bug543720_since_12() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String s	=
				"package test1;\n"+
				"public class X {\n"+
				"	static int foo(int i) {\n"+
				"		int tw = 0;\n"+
				"		switch (i) {\n"+
				"			case 1 : {\n"+
				" 				int z = 100;\n"+
				" 				break;\n"+
				"			}\n"+
				"			default : {\n"+
				"				break;\n"+
				"			}\n"+
				"		}\n"+
				"		return tw;\n"+
				"	}\n"+
				"	public static void main(String[] args) {\n"+
				"		System.out.print(foo(1));\n"+
				"	}\n"+
				"}\n";
		StringBuffer buf = new StringBuffer(s);
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List blockStatements= block.statements();
		{ // insert statements, replace expression
			SwitchStatement switchStmt = (SwitchStatement) blockStatements.get(1);

			SwitchCase caseStatement1= ast.newSwitchCase();
			caseStatement1.expressions().add(ast.newNumberLiteral("100"));

			BreakStatement breakStatement = ast.newBreakStatement();
			Block block1 = ast.newBlock();
			block1.statements().add(breakStatement);

			SwitchCase defaultCase = (SwitchCase) switchStmt.statements().get(2);
			ListRewrite listRewrite= rewrite.getListRewrite(switchStmt, SwitchStatement.STATEMENTS_PROPERTY);
			listRewrite.insertBefore(caseStatement1, defaultCase, null);
			listRewrite.insertBefore(block1, defaultCase, null);
		}


		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class X {\n");
		buf.append("	static int foo(int i) {\n");
		buf.append("		int tw = 0;\n");
		buf.append("		switch (i) {\n");
		buf.append("			case 1 : {\n");
		buf.append(" 				int z = 100;\n");
		buf.append(" 				break;\n");
		buf.append("			}\n");
		buf.append("			case 100:\n");
		buf.append("                {\n");
		buf.append("                    break;\n");
		buf.append("                }\n");
		buf.append("            default : {\n");
		buf.append("				break;\n");
		buf.append("			}\n");
		buf.append("		}\n");
		buf.append("		return tw;\n");
		buf.append("	}\n");
		buf.append("	public static void main(String[] args) {\n");
		buf.append("		System.out.print(foo(1));\n");
		buf.append("	}\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	@SuppressWarnings("rawtypes")
	public void testSwitchExpressions_04_since_12() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String s	= "package test1;\n"+
				"public class X {\n"+
				"	static int foo(int i) {\n"+
				"		int tw =\n"+
				"		switch (i) {\n"+
				"			case 1 -> \n"+
				"			 {\n"+
				" 				int z = 100;\n"+
				" 				break z;\n"+
				"			}\n"+
				"			default -> {\n"+
				"				break 12;\n"+
				"			}\n"+
				"		};\n"+
				"		return tw;\n"+
				"	}\n"+
				"	public static void main(String[] args) {\n"+
				"		System.out.print(foo(1));\n"+
				"	}\n"+
				"}\n";
		StringBuffer buf = new StringBuffer(s);
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List blockStatements= block.statements();
		{ // insert statements, replace expression
			VariableDeclarationStatement stmt = (VariableDeclarationStatement) blockStatements.get(0);
			SwitchExpression switchExpression= (SwitchExpression) ((VariableDeclarationFragment) stmt.fragments().get(0)).getInitializer();

			ASTNode expression= switchExpression.getExpression();
			SimpleName newExpression= ast.newSimpleName("x");
			rewrite.replace(expression, newExpression, null);

			List statements= switchExpression.statements();

			// remove statements
			rewrite.remove((ASTNode) statements.get(0), null);
			rewrite.remove((ASTNode) statements.get(1), null);
		}
		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class X {\n");
		buf.append("	static int foo(int i) {\n");
		buf.append("		int tw =\n");
		buf.append("		switch (x) {\n");
		buf.append("			default -> {\n");
		buf.append("				break 12;\n");
		buf.append("			}\n");
		buf.append("		};\n");
		buf.append("		return tw;\n");
		buf.append("	}\n");
		buf.append("	public static void main(String[] args) {\n");
		buf.append("		System.out.print(foo(1));\n");
		buf.append("	}\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	//Note: complete removal of statements under switch statements now added in ASTRSTest and hence not repeated here.
}
