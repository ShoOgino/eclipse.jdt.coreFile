package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;
import junit.framework.TestSuite;

public class SwitchTest extends AbstractRegressionTest {
	
public SwitchTest(String name) {
	super(name);
}
public static Test suite() {

	if (false) {
	   	TestSuite ts;
		//some of the tests depend on the order of this suite.
		ts = new TestSuite();
		ts.addTest(new SwitchTest("test221"));
		return new RegressionTestSetup(ts);
	}
	return setupSuite(testClass());
}

public void test001() {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" + 
		"public class X {\n" + 
		"  public static void main(String args[]) {\n" + 
		"    foo();\n" + 
		"  }\n" + 
		"  public static void foo() {\n" + 
		"    try {\n" + 
		"      switch(0) {\n" + 
		"      case 0 :\n" + 
		"      case 1 - (1 << 31) :\n" + 
		"      case (1 << 30) :\n" + 
		"      }\n" + 
		"    } catch (OutOfMemoryError e) {\n" + 
		"    }\n" + 
		"  }\n" + 
		"}\n",
	});
}
public void test002() {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" + 
		"public class X {\n" + 
		"  int k;\n" + 
		"  public void foo() {\n" + 
		"    int c;\n" + 
		"    switch (k) {\n" + 
		"      default :\n" + 
		"        c = 2;\n" + 
		"        break;\n" + 
		"      case 2 :\n" + 
		"        c = 3;\n" + 
		"        break;\n" + 
		"    }\n" + 
		"  }\n" + 
		"}\n",
	});
}

public void test003() {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" + 
		"public class X {\n" + 
		"  int i = 0;\n" + 
		"  void foo() {\n" + 
		"    switch (i) {\n" + 
		"      case 1 :\n" + 
		"        {\n" + 
		"          int j;\n" + 
		"          break;\n" + 
		"        }\n" + 
		"    }\n" + 
		"  }\n" + 
		"}\n",
	});
}

public void test004() {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" + 
		"public class X {\n" + 
		"  public static int foo() {\n" + 
		"    int i = 0, j;\n" + 
		"    switch (i) {\n" + 
		"      default :\n" + 
		"        int k = 2;\n" + 
		"        j = k;\n" + 
		"    }\n" + 
		"    if (j != -2) {\n" + 
		"      return 1;\n" + 
		"    }\n" + 
		"    return 0;\n" + 
		"  }\n" + 
		"}\n",
	});
}

public void test005() {
	this.runConformTest(new String[] {
		"p/BugJavaCase.java",
		"package p;\n" + 
		"class BugJavaCase {\n" + 
		"  public static final int BC_ZERO_ARG = 1;\n" + 
		"  public void test01(int i) {\n" + 
		"    switch (i) {\n" + 
		"      case BC_ZERO_ARG :\n" + 
		"        System.out.println(\"i = \" + i);\n" + 
		"        break;\n" + 
		"    }\n" + 
		"  }\n" + 
		"}\n",
	});
}


public void test006() {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" + 
		"public class X {\n" + 
		"  public static void main(String args[]) {\n" + 
		"    foo(); \n" + 
		"  } \n" + 
		" \n" + 
		"  public static void foo() { \n" + 
		"    char x = 5;\n" + 
		"    final short b = 5;\n" + 
		"    int a;\n" + 
		"    \n" + 
		"    switch (x) {\n" + 
		"      case b:        // compile time error\n" + 
		"        a = 0;\n" + 
		"        break; \n" + 
		"      default:\n" + 
		"        a=1;\n" + 
		"    }\n" + 
		"    \n" + 
		"  }\n" + 
		"}\n",
	});
}
public static Class testClass() {
	return SwitchTest.class;
}
}

