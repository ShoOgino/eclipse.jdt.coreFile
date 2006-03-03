/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.io.IOException;

import junit.framework.Test;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class InnerEmulationTest extends AbstractRegressionTest {
	
public InnerEmulationTest(String name) {
	super(name);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}
/**
 * Protected access emulation : should be performed onto implicit field and method accesses
 */
public void test001() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;								\n"+
			"import p2.*;								\n"+
			"public class A {							\n"+
			"	protected int value = 0;				\n"+
			"	protected A delegatee;					\n"+
			"	public A(A del, int val) {				\n"+
			"		this.delegatee = del;				\n"+
			"		this.value = val;					\n"+
			"	}										\n"+
			"	protected void foo() {					\n"+
			"		value += 3;							\n"+
			"	}										\n"+
			"	public static void main(String[] argv){	\n"+
			"		int result = new B(					\n"+
			"					  new B(null, 10), 20)  \n"+
			"					 .value; 				\n"+
			"		int expected = 30; 					\n"+
			"		System.out.println( 				\n"+
			"			result == expected 				\n"+
			"				? \"SUCCESS\"  				\n"+
			"				: \"FAILED : got \"+result+\" instead of \"+ expected); \n"+
			"	}										\n"+
			"}											\n",
			/* p2.B */
			"p2/B.java",
			"package p2;								\n"+
			"import p1.*;								\n"+
			"public class B extends A {					\n"+
			"	public B(B del, int val){				\n"+
			"		super(del, val);					\n"+
			"		Runnable r = new Runnable () {		\n"+
			"			public void run() {				\n"+
			"				foo(); 						\n"+
			"				if (delegatee != null) 		\n"+
			"					value += 7;				\n"+
			"			}								\n"+
			"		};									\n"+
			"		r.run();							\n"+
			"	}										\n"+
			"}											\n"
		}, 
		"SUCCESS"
	);									
}
/**
 * 1FN4S4Z: The compiler doesn't detect a illegal constructor invocation which leads to a VerifyError
 */
public void test002() {
	this.runNegativeTest(
		new String[] {
			/* A.java */
			"A.java",
			"class B { \n"+
			"} \n"+
			"public class A { \n"+
			"	B b; \n"+
			"	class C extends B { \n"+
			"		public C() { \n"+
			"		} \n"+
			"	} \n"+
			"	public A() { \n"+
			"		this(new C()); \n"+
			"	} \n"+
			"	public A(C c) { \n"+
			"		this.b = c; \n"+
			"	} \n"+
			"	public static void main(String[] args) { \n"+
			"		A a = new A(); \n"+
			"		System.out.println(a); \n"+
			"	} \n"+
			"} \n"
		}, 
		"----------\n" + 
		"1. ERROR in A.java (at line 10)\n" + 
		"	this(new C()); \n" + 
		"	     ^^^^^^^\n" + 
		"No enclosing instance of type A is available due to some intermediate constructor invocation\n" + 
		"----------\n"

	);									
}
/**
 * 1FZ2G7R: use of non static inner class in constuctor
 */
public void test003() {
	this.runNegativeTest(
		new String[] {
			/* A.java */
			"A.java",
			"public class A extends Thread { \n" +
			"	private class B { \n" +
			"	} \n" +
			"	private static Runnable getRunnable() { \n" +
			"		return null; \n" +
			"	} \n" +
			"	public A() { \n" +
			"		super(getRunnable(), new B().toString()); \n" +
			"	} \n" +
			"} \n"
		}, 
		"----------\n" + 
		"1. WARNING in A.java (at line 8)\n" + 
		"	super(getRunnable(), new B().toString()); \n" + 
		"	                     ^^^^^^^\n" + 
		"Access to enclosing constructor A.B() is emulated by a synthetic accessor method. Increasing its visibility will improve your performance\n" + 
		"----------\n" + 
		"2. ERROR in A.java (at line 8)\n" + 
		"	super(getRunnable(), new B().toString()); \n" + 
		"	                     ^^^^^^^\n" + 
		"No enclosing instance of type A is available due to some intermediate constructor invocation\n" + 
		"----------\n");									
}
/**
 * 1F995V9: Walkback in innerclass emulation when mixing source and binaries
 */
public void test004() {


	/* first compile A3.java */
	
	this.runConformTest(
		new String[] {
			/* A3.java */
			"A3.java",
			"class A3 { \n"+
			"	class B {} \n"+
			"} \n"
		},
		null,
		null); // no specific success output string
	
	/* then compile with previous input */
	
	this.runConformTest(
		new String[] {
			/* A4.java */
			"A4.java",
			"class A4 { \n"+
			"	void foo(){ \n"+
			"		new A3().new B(){}; \n"+
			"	} \n"+
			"} \n"
		},
		null, // no specific success output string
		null, // use default class-path
		false,
		null); // do not flush previous output dir content

}
/**
 * 1FK9ALJ: Cannot invoke private super constructor ... 
 */
public void test005() {
	this.runConformTest(
		new String[] {
			/* X.java */
			"X.java",
			"public class X { \n"+
			"	private X(){} \n"+
			"	class Y extends X { \n"+
			"	} \n"+
			"	public static void main(String[] argv){	\n"+
			"		new X().new Y();	 				 \n"+
			"		System.out.println(\"SUCCESS\");	 \n"+
			"	}										\n"+
			"} \n"
		}, 
		"SUCCESS"
	);									
}
/**
 * 1FKLXDL: Verification error due to incorrect private access emulation
 */
public void test006() {
	this.runConformTest(
		new String[] {
			/* X.java */
			"X.java",
			"public class X { \n"+
			"	public static void main(String[] argv){ \n"+
			"		new B(); \n"+
			"		System.out.println(\"SUCCESS\"); \n"+
			"	} \n"+
			"	private static void foo(int i, int j) { \n"+
			"		System.out.println(\"private foo\"); \n"+
			"	} \n"+
			"	static class B { \n"+
			"		{ \n"+
			"			foo(1, 2); \n"+
			"		} \n"+
			"	} \n"+
			"}		 \n"
		}, 
		"private foo\n" + 
		"SUCCESS"
	);									
}
/**
 * 1PQCT5T: Missing emulation for access to sibling local types
 */
public void test007() {
	this.runConformTest(
		new String[] {
			/* Y.java */
			"Y.java",
			"public class Y { \n" +
			"	public static void main(String[] argv){ \n" +
			"		if (new Y().bar() == 3) \n" +
			"			System.out.println(\"SUCCESS\"); \n" +
			"		else \n" +
			"			System.out.println(\"FAILED\"); \n" +
			"	} \n" +
			"	int bar() { \n" +
			"		final int i = \"xxx\".length(); \n" +
			"		class X { \n" +
			"			class AX { \n" +
			"				int foo() { \n" +
			"					return new BX().foo(); \n" +
			"				} \n" +
			"			} \n" +
			"			class BX { \n" +
			"				int foo() { \n" +
			"					return new CX().foo(); \n" +
			"				} \n" +
			"			} \n" +
			"			class CX { \n" +
			"				int foo() { \n" +
			"					return i; \n" +
			"				} \n" +
			"			} \n" +
			"		} \n" +
			"		return new X().new AX().foo(); \n" +
			"	} \n"+
			"} \n"
		}, 
		"SUCCESS"
	);									
}
/**
 * 1PQCT5T: Missing emulation for access to sibling local types
 */
public void test008() {
	this.runConformTest(
		new String[] {
			/* Y2.java */
			"Y2.java",
			"public class Y2 { \n" +
			"	public static void main(String[] argv){ \n" +
			"		if (new Y2().foo(45) == 45) \n" +
			"			System.out.println(\"SUCCESS\"); \n" +
			"		else \n" +
			"			System.out.println(\"FAILED\"); \n" +
			"	} \n" +
			"	int foo(final int i){ \n"+
			"		class B { \n"+
			"			int foo(){ \n"+
			"				return new C().foo(); \n"+
			"			} \n"+
			"			class C { \n"+
			"				int foo(){ return i; } \n"+
			"			} \n"+
			"		}; \n"+
			"		return new B().foo(); \n"+
			"	} \n"+
			"} \n"
		}, 
		"SUCCESS"
	);									
}
/**
 * 1PQCT5T: Missing emulation for access to sibling local types
 */
public void test009() {
	this.runConformTest(
		new String[] {
			/* Y3.java */
			"Y3.java",
			"public class Y3 { \n" +
			"	public static void main(String[] argv){ \n" +
			"		if (new Y3().bar() == 8) \n" +
			"			System.out.println(\"SUCCESS\"); \n" +
			"		else \n" +
			"			System.out.println(\"FAILED\"); \n" +
			"	} \n" +
			"	int bar() { \n" +
			"		final int i = \"xxx\".length(); \n" +
			"		final String s = this.toString(); \n" +
			"		class X { \n" +
			"			class AX { \n" +
			"				int foo() { \n" +
			"					return i + new CX().foo(); \n" +
			"				} \n" +
			"			} \n" +
			"			class BX { \n" +
			"				int foo() { \n" +
			"					return new AX().foo(); \n" +
			"				} \n" +
			"			} \n" +
			"			class CX { \n" +
			"				int foo() { \n" +
			"					return 5; \n" +
			"				} \n" +
			"			} \n" +
			"		} \n" +
			"		return new X().new AX().foo(); \n" +
			"	} \n" +
			"} \n"
		}, 
		"SUCCESS"
	);									
}
/**
 * 1PQCT5T: Missing emulation for access to sibling local types
 */
public void test010() {
	this.runConformTest(
		new String[] {
			/* Y4.java */
			"Y4.java",
			"public class Y4 { \n" +
			"	public static void main(String[] argv){ \n" +
			"		if (new Y4().bar() == 3) \n" +
			"			System.out.println(\"SUCCESS\"); \n" +
			"		else \n" +
			"			System.out.println(\"FAILED\"); \n" +
			"	} \n" +
			"	int bar() { \n" +
			"		final int i = \"xxx\".length(); \n" +
			"		final String s = this.toString(); \n" +
			"		class X { \n" +
			"			class AX { \n" +
			"				int bar() { \n" +
			"					class BX { \n" +
			"						int foo() { \n" +
			"							return new AX().foo(); \n" +
			"						} \n" +
			"					} \n" +
			"					return new BX().foo(); \n" +
			"				} \n" +
			"				int foo() { \n" +
			"					return i; \n" +
			"				} \n" +
			"			} \n" +
			"		} \n" +
			"		return new X().new AX().bar(); \n" +
			"	} \n" +
			"} \n"
		}, 
		"SUCCESS"
	);									
}
/**
 * 1PQCT5T: Missing emulation for access to sibling local types
 */
public void test011() {
	this.runConformTest(
		new String[] {
			/* Y5.java */
			"Y5.java",
			"public class Y5 { \n" +
			"	public static void main(String[] argv){ \n" +
			"		if (new Y5().bar(5) == 5) \n" +
			"			System.out.println(\"SUCCESS\"); \n" +
			"		else \n" +
			"			System.out.println(\"FAILED\"); \n" +
			"	} \n" +
			"	int bar(final int i) { \n" +
			"		class X { \n" +
			"			int bar() { \n" +
			"				return new Object(){  \n" +
			"						int foo(){ \n" +
			"							return i; \n" +
			"						} \n" +
			"					}.foo(); \n" +
			"			} \n" +
			"		} \n" +
			"		return new X().bar(); \n" +
			"	} \n" +
			"} \n"
		}, 
		"SUCCESS"
	);									
}
/**
 * 1F3AH7N: GPF on innerclass emulation for double anonymous type
 */
public void test012() {
	this.runConformTest(
		new String[] {
			/* A.java */
			"A.java",
			"public class A { \n" +
			"	public static void main(String[] argv){ \n" +
			"		if (new A().foo() == 5) \n" +
			"			System.out.println(\"SUCCESS\"); \n" +
			"		else \n" +
			"			System.out.println(\"FAILED\"); \n" +
			"	} \n" +
			"	int foo() { \n" +
			"		return new A() { \n" +
			"			int foo() { \n" +
			"				final int i = \"hello\".length(); \n" +
			"				return new A() { \n" +
			"					int foo() { \n" +
			"						return i; \n" +
			"					} \n" +
			"				} \n" +
			"				.foo(); \n" +
			"			} \n" +
			"		} \n" +
			"		.foo(); \n" +
			"	} \n" +
			"} \n"
		}, 
		"SUCCESS"
	);									
}
/**
 * 1F26XE2: Bug in inner class emulation
 */
public void test013() {
	this.runConformTest(
		new String[] {
			/* Inner.java */
			"Inner.java",
			"public class Inner { \n" +
			"	public static void main(String[] argv){ \n" +
			"		new B().new C(\"hello\"); \n" +
			"		System.out.println(\"SUCCESS\"); \n" +
			"	} \n" +
			"  class A { 	\n"+
			"    public A(String s){ this.s=s; }	\n"+
			"    String s;	\n"+
			"  }	\n"+
			"}	\n"+
			"class B {	\n"+
			"  class C extends Inner.A {	\n"+
			"    public C(String s){  B.this.inner.super(s); }   	\n"+
			"  }	\n"+
			"  Inner inner=new Inner();	\n"+
			"}	\n"
		}, 
		"SUCCESS"
	);									
}
/**
 * 1EX5I8Z: Inner class emulation bug
 */
public void test014() {
	this.runConformTest(
		new String[] {
			/* Z1.java */
			"Z1.java",
			"public class Z1 { \n" +
			"	public static void main(String[] argv){ \n" +
			"		new Z1().new W(); \n" +
			"		System.out.println(\"SUCCESS\"); \n" +
			"	} \n" +
			"	class W extends Y { \n" +
			"		W() { \n" +
			"			super(new Object(), foo()); \n" +
			"		} \n" +
			"	} \n" +
			"	String foo() { \n" +
			"		return \"\"; \n" +
			"	} \n" +
			"} \n" +
			"class Y { \n" +
			"	Y(Object o, String s) { \n" +
			"	} \n" +
			"}	\n"
		}, 
		"SUCCESS"
	);									
}
/**
 * 1EUC39Y: Incorrect Synthetic Emulation 
 */
public void test015() {
	this.runConformTest(
		new String[] {
			/* AAA.java */
			"AAA.java",
			"public class AAA {  \n"+
			"	public static void main(String argv[]){  \n"+
			"		if (new AAA().foo(5) == 15);  \n"+
			"		System.out.println(\"SUCCESS\"); \n" +
			"	}  \n"+
			"	int foo(final int loc){  \n"+
			"		class I {  \n"+
			"			int i = loc;  \n"+
			"			{  \n"+
			"				System.out.println(\"loc=\"+ loc );	  \n"+
			"			}  \n"+
			"			int foo(){   \n"+
			"				System.out.println(\"I:i=\"+ i );	  \n"+
			"				return i;}   \n"+
			"		}    \n"+
			"		class J extends I {  \n"+
			"			I obj = new I(){  \n"+
			"				int foo() {  \n"+
			"					System.out.println(\"J\");  \n"+
			"					return super.foo() + 10; }};  \n"+
			"		}  \n"+
			"		return new J().obj.foo();  \n"+
			"	}  \n"+
			" }  \n"
		}, 
		"loc=5\n" + 
		"loc=5\n" + 
		"J\n" + 
		"I:i=5\n" + 
		"SUCCESS"
	);									
}
/**
 * 1EUC39Y: Incorrect Synthetic Emulation 
 */
public void test016() {
	this.runConformTest(
		new String[] {
			/* AAA.java */
			"AAA.java",
			"public class AAA {  \n"+
			"	public static void main(String argv[]){  \n"+
			"		if (new AAA().foo(5) == 15);  \n"+
			"		System.out.println(\"SUCCESS\"); \n" +
			"	}  \n"+
			"	class B{} \n"+
			"	int foo(final int loc){ \n"+
			"		class I extends B { \n"+
			"			int i = loc; \n"+
			"			{ \n"+
			"				System.out.println(\"loc=\"+ loc );	 \n"+
			"			} \n"+
			"			int foo(){  \n"+
			"				System.out.println(\"I:i=\"+ i );	 \n"+
			"				return i;}  \n"+
			"		}   \n"+
			"		class J extends I { \n"+
			"			I obj = new I(){ \n"+
			"				int foo() { \n"+
			"					System.out.println(\"J\"); \n"+
			"					return super.foo() + 10; }}; \n"+
			"		} \n"+
			"		return new J().obj.foo(); \n"+
			"	} \n"+
			" }  \n"
		}, 
		"loc=5\n" + 
		"loc=5\n" + 
		"J\n" + 
		"I:i=5\n" + 
		"SUCCESS"
	);									
}
/**
 * Complex multi-threaded test involving inner classes
 */
public void test017() {
	this.runConformTest(
		new String[] {
			/* MultiComplex.java */
			"MultiComplex.java",
			"public class MultiComplex {\n" + 
			"	// should just be an interface, once supported...\n" + 
			"	abstract class AbstractTask implements Runnable {\n" + 
			"		public void run() {\n" + 
			"			MultiComplex.this.notifyCompletion(this,0); \n" + 
			"		}\n" + 
			"		abstract public String taskName();		\n" + 
			"	}\n" + 
			"	public static void main(String argv[]){\n" + 
			"		try {\n" + 
			"			new MultiComplex().performTasks(3);\n" + 
			"		}\n" + 
			"		catch(InterruptedException e){};\n" + 
			"	}  \n" + 
			"	void notifyCompleted(AbstractTask task) {\n" + 
			"	}\n" + 
			"	void notifyCompletion(AbstractTask task, int percentage) {\n" + 
			"	}\n" + 
			"	void notifyExecutionEnd() {\n" + 
			"		System.out.println(\"EXECUTION FINISHED\");\n" + 
			"	}\n" + 
			"	void notifyExecutionStart() {\n" + 
			"		System.out.println(\"EXECUTION STARTING\");\n" + 
			"	}\n" + 
			"	void performTasks(final int maxTasks) throws InterruptedException {\n" + 
			"		Thread workers[] = new Thread[maxTasks];\n" + 
			"		AbstractTask tasks[] = new AbstractTask[maxTasks];\n" + 
			"		final int maxIteration = 5;\n" + 
			" \n" + 
			"		// Local Task \n" + 
			"		class Task extends AbstractTask { \n" + 
			"				String taskName; \n" + 
			"				Task(String aName) {\n" + 
			"					taskName = aName;\n" + 
			"				}\n" + 
			"				public String taskName() { \n" + 
			"					return taskName; \n" + 
			"				}\n" + 
			"	\n" + 
			"				public void run() {\n" + 
			"					super.run();\n" + 
			"					for(int j = 0; j < maxIteration; j++)\n" + 
			"						MultiComplex.this.notifyCompletion(this,  (int)((float) (j + 1) / maxIteration * 100));\n" + 
			"				}\n" + 
			"		};\n" + 
			"		notifyExecutionStart();\n" + 
			"		\n" + 
			"		// Creating and launching the tasks\n" + 
			"		for (int ii = 0; ii < maxTasks; ii++) {\n" + 
			"			final int i = ii;\n" + 
			"			tasks[i] = new Task(String.valueOf(i + 1)) {			\n" + 
			"				public String taskName() { \n" + 
			"					return super.taskName() +  \" of \" + maxTasks; }\n" + 
			"				public void run() {\n" + 
			"					super.run();\n" + 
			"					MultiComplex.this.notifyCompleted(this);\n" + 
			"				}		\n" + 
			"			};\n" + 
			"			workers[i] = new Thread(tasks[i],tasks[i].taskName());\n" + 
			"			workers[i].start();\n" + 
			"		}\n" + 
			"		// Waiting for *all* tasks to be ended\n" + 
			"		for (int i = 0; i < tasks.length; i++)\n" + 
			"			workers[i].join();\n" + 
			"		notifyExecutionEnd();\n" + 
			"	}\n" + 
			"}\n"		
		}, 
		"EXECUTION STARTING\n" + 
		"EXECUTION FINISHED"
	);									
}
/**
 * Complex multi-threaded test involving inner classes
 */
public void test018() {
	this.runConformTest(
		new String[] {
			/* MultiAnonymous.java */
			"MultiAnonymous.java",
			"public class MultiAnonymous {\n" + 
			"	public static void main(String argv[]){\n" + 
			"		try {\n" + 
			"			new MultiAnonymous().performTasks(3);\n" + 
			"		}\n" + 
			"		catch(InterruptedException e){};\n" + 
			"	}\n" + 
			"	void notifyExecutionEnd() {\n" + 
			"		System.out.println(\"EXECUTION FINISHED\");\n" + 
			"	}\n" + 
			"	void notifyExecutionStart() {\n" + 
			"		System.out.println(\"EXECUTION STARTING\");\n" + 
			"	}\n" + 
			"	void performTasks(final int maxTasks) throws java.lang.InterruptedException {\n" + 
			"		Thread workers[] = new Thread[maxTasks];\n" + 
			"		Runnable tasks[] = new Runnable[maxTasks];\n" + 
			"		final int maxIteration = 5;\n" + 
			"		notifyExecutionStart();\n" + 
			"		\n" + 
			"		// Creating and launching the tasks\n" + 
			"		for (int ii = 0; ii < maxTasks; ii++) {\n" + 
			"			final int i = ii;\n" + 
			"			tasks[i] = new Runnable() {			\n" + 
			"				public String toString() { return ((i + 1) + \" of \" + maxTasks); }\n" + 
			"				public void run() {\n" + 
			"					for(int j = 0; j < maxIteration; j++)\n" + 
			"						notifyCompletion( (int)((float) (j + 1) / maxIteration * 100));\n" + 
			"				}		\n" + 
			"			\n" + 
			"				void notifyCompletion(int percentage) {\n" + 
			"				}\n" + 
			"			};\n" + 
			"			workers[i] = new Thread(tasks[i],\"Running task(\"+(tasks[i].toString())+\")\");\n" + 
			"			workers[i].start();\n" + 
			"		}\n" + 
			"		// Waiting for *all* tasks to be ended\n" + 
			"		for (int i = 0; i < tasks.length; i++)\n" + 
			"			workers[i].join();\n" + 
			"		notifyExecutionEnd();\n" + 
			"	}\n" + 
			"}\n"
		}, 
		"EXECUTION STARTING\n" + 
		"EXECUTION FINISHED"
	);									
}
/**
 * Complex multi-threaded test involving inner classes
 */
public void test019() {
	this.runConformTest(
		new String[] {
			/* MultiComplex2.java */
			"MultiComplex2.java",
			"public class MultiComplex2 {\n" + 
			"	public interface AbstractTask extends Runnable {\n" + 
			"		public void run();\n" + 
			"		public String taskName();		\n" + 
			"	}\n" + 
			"	\n" + 
			"	public static void main(String argv[]){\n" + 
			"		try {\n" + 
			"			new MultiComplex2().performTasks(3);\n" + 
			"		}\n" + 
			"		catch(InterruptedException e){};\n" + 
			"	}\n" + 
			"	void notifyCompleted(AbstractTask task) {\n" + 
			"	}\n" + 
			"	void notifyCompletion(AbstractTask task, int percentage) {\n" + 
			"	}\n" + 
			"	void notifyExecutionEnd() {\n" + 
			"		System.out.println(\"EXECUTION FINISHED\");\n" + 
			"	}\n" + 
			"	void notifyExecutionStart() {\n" + 
			"		System.out.println(\"EXECUTION STARTING\");\n" + 
			"	}\n" + 
			"		void performTasks(final int maxTasks) throws java.lang.InterruptedException {\n" + 
			"		Thread workers[] = new Thread[maxTasks];\n" + 
			"		AbstractTask tasks[] = new AbstractTask[maxTasks];\n" + 
			"		final int maxIteration = 5;\n" + 
			"		// Local Task\n" + 
			"		class Task implements AbstractTask {\n" + 
			"				String taskName;\n" + 
			"				Task(String aName) {\n" + 
			"					taskName = aName;\n" + 
			"				}\n" + 
			"				public String taskName() { \n" + 
			"					return taskName; \n" + 
			"				}\n" + 
			"	\n" + 
			"				public void run() {\n" + 
			"					MultiComplex2.this.notifyCompletion(this,0); \n" + 
			"					for(int j = 0; j < maxIteration; j++)\n" + 
			"						MultiComplex2.this.notifyCompletion(this,  (int)((float) (j + 1) / maxIteration * 100));\n" + 
			"				}\n" + 
			"		};\n" + 
			"		notifyExecutionStart();\n" + 
			"		\n" + 
			"		// Creating and launching the tasks\n" + 
			"		for (int ii = 0; ii < maxTasks; ii++) {\n" + 
			"			final int i = ii;\n" + 
			"			tasks[i] = new Task(String.valueOf(i + 1)) {			\n" + 
			"				public String taskName() { \n" + 
			"					return super.taskName() +  \" of \" + maxTasks; }\n" + 
			"				public void run() {\n" + 
			"					super.run();\n" + 
			"					MultiComplex2.this.notifyCompleted(this);\n" + 
			"				}		\n" + 
			"			};\n" + 
			"			workers[i] = new Thread(tasks[i],tasks[i].taskName());\n" + 
			"			workers[i].start();\n" + 
			"		}\n" + 
			"		// Waiting for *all* tasks to be ended\n" + 
			"		for (int i = 0; i < tasks.length; i++)\n" + 
			"			workers[i].join();\n" + 
			"		notifyExecutionEnd();\n" + 
			"	}\n" + 
			"}\n"
		}, 
		"EXECUTION STARTING\n" + 
		"EXECUTION FINISHED"
	);									
}
/**
 * Complex multi-threaded test involving inner classes
 */
public void test020() {
	this.runConformTest(
		new String[] {
			/* MultiLocal.java */
			"MultiLocal.java",
			"public class MultiLocal {\n" + 
			"	public static void main(String argv[]){\n" + 
			"		class Task implements Runnable {\n" + 
			"			private String taskName;\n" + 
			"			private int maxIteration; \n" + 
			"			public Task(String name, int value) {\n" + 
			"				taskName = name; \n" + 
			"				maxIteration = value;\n" + 
			"			}\n" + 
			"	\n" + 
			"			public String toString() { return taskName; }\n" + 
			"			public void run() {\n" + 
			"				for(int i = 0; i < maxIteration; i++)\n" + 
			"					notifyCompletion( (int)((float) (i + 1) / maxIteration * 100));\n" + 
			"			}		\n" + 
			"			\n" + 
			"			void notifyCompletion(int percentage) {\n" + 
			"			}\n" + 
			"		};\n" + 
			"		MultiLocal multi = new MultiLocal();\n" + 
			"		int maxTasks = 3;\n" + 
			"		Task tasks[] = new Task[maxTasks];\n" + 
			"		for (int i = 0; i < maxTasks; i++) \n" + 
			"			tasks[i] = new Task(String.valueOf(i),5);\n" + 
			"		try {\n" + 
			"			multi.performTasks(tasks);\n" + 
			"		}\n" + 
			"		catch(InterruptedException e){};\n" + 
			"	}\n" + 
			"	void notifyExecutionEnd() {\n" + 
			"		System.out.println(\"EXECUTION FINISHED\");\n" + 
			"	}\n" + 
			"	void notifyExecutionStart() {\n" + 
			"		System.out.println(\"EXECUTION STARTING\");\n" + 
			"	}\n" + 
			"	void performTasks(Runnable tasks[]) throws java.lang.InterruptedException {\n" + 
			"		Thread workers[] = new Thread[tasks.length];\n" + 
			"		notifyExecutionStart();\n" + 
			"		\n" + 
			"		// Launching the tasks\n" + 
			"		for (int i = 0; i < tasks.length; i++) {\n" + 
			"			workers[i] = new Thread(tasks[i],\"Running task(\"+(tasks[i].toString())+\")\");\n" + 
			"			workers[i].start();\n" + 
			"		}\n" + 
			"		// Waiting for *all* tasks to be ended\n" + 
			"		for (int i = 0; i < tasks.length; i++)\n" + 
			"			workers[i].join();\n" + 
			"		notifyExecutionEnd();\n" + 
			"	}\n" + 
			"}\n"
		}, 
		"EXECUTION STARTING\n" + 
		"EXECUTION FINISHED"
	);									
}
/**
 * Complex multi-threaded test involving inner classes
 */
public void test021() {
	this.runConformTest(
		new String[] {
			/* MultiLocal2.java */
			"MultiLocal2.java",
			"public class MultiLocal2 {\n" + 
			"	public static void main(String argv[]){\n" + 
			"		final int maxTasks = 3;\n" + 
			"		class Task implements Runnable {\n" + 
			"			private String taskName;\n" + 
			"			private int maxIteration;\n" + 
			"			public Task(String name, int value) {\n" + 
			"				taskName = name; \n" + 
			"				maxIteration = value;\n" + 
			"			}\n" + 
			"	\n" + 
			"			public String toString() { return taskName + \" of \" + String.valueOf(maxTasks); }\n" + 
			"			public void run() {\n" + 
			"				for(int i = 0; i < maxIteration; i++)\n" + 
			"					notifyCompletion( (int)((float) (i + 1) / maxIteration * 100));\n" + 
			"			}		\n" + 
			"			\n" + 
			"			void notifyCompletion(int percentage) {\n" + 
			"			}\n" + 
			"		};\n" + 
			"		MultiLocal2 multi = new MultiLocal2();\n" + 
			"		Task tasks[] = new Task[maxTasks];\n" + 
			"		for (int i = 0; i < maxTasks; i++) \n" + 
			"			tasks[i] = new Task(String.valueOf(i+1),5);\n" + 
			"		try {\n" + 
			"			multi.performTasks(tasks);\n" + 
			"		}\n" + 
			"		catch(InterruptedException e){};\n" + 
			"	}\n" + 
			"	void notifyExecutionEnd() {\n" + 
			"		System.out.println(\"EXECUTION FINISHED\");\n" + 
			"	}\n" + 
			"	void notifyExecutionStart() {\n" + 
			"		System.out.println(\"EXECUTION STARTING\");\n" + 
			"	}\n" + 
			"	void performTasks(Runnable tasks[]) throws java.lang.InterruptedException {\n" + 
			"		Thread workers[] = new Thread[tasks.length];\n" + 
			"		notifyExecutionStart();\n" + 
			"		\n" + 
			"		// Launching the tasks\n" + 
			"		for (int i = 0; i < tasks.length; i++) {\n" + 
			"			workers[i] = new Thread(tasks[i],\"Running task(\"+(tasks[i].toString())+\")\");\n" + 
			"			workers[i].start();\n" + 
			"		}\n" + 
			"		// Waiting for *all* tasks to be ended\n" + 
			"		for (int i = 0; i < tasks.length; i++)\n" + 
			"			workers[i].join();\n" + 
			"		notifyExecutionEnd();\n" + 
			"	}\n" + 
			"}\n"
		}, 
		"EXECUTION STARTING\n" + 
		"EXECUTION FINISHED"
	);									
}
/**
 * Complex multi-threaded test involving inner classes
 */
public void test022() {
	this.runConformTest(
		new String[] {
			/* MultiMember.java */
			"MultiMember.java",
			"public class MultiMember {\n" + 
			"	class Task implements Runnable {\n" + 
			"		private String taskName; \n" + 
			"		private int maxIteration;\n" + 
			"		public Task(String name, int value) {\n" + 
			"			taskName = name; \n" + 
			"			maxIteration = value;\n" + 
			"		}\n" + 
			"		public String toString() { return taskName; }\n" + 
			"		public void run() {\n" + 
			"			for(int i = 0; i < maxIteration; i++)\n" + 
			"				notifyCompletion( (int)((float) (i + 1) / maxIteration * 100));\n" + 
			"		}		\n" + 
			"		\n" + 
			"		void notifyCompletion(int percentage) {\n" + 
			"		}\n" + 
			"	}\n" + 
			"	public static void main(String argv[]){\n" + 
			"		MultiMember multi = new MultiMember();\n" + 
			"		int maxTasks = 3;\n" + 
			"		Task tasks[] = new Task[maxTasks];\n" + 
			"		for (int i = 0; i < maxTasks; i++) \n" + 
			"			tasks[i] = multi.new Task(String.valueOf(i),5);\n" + 
			"		try {\n" + 
			"			multi.performTasks(tasks);\n" + 
			"		}\n" + 
			"		catch(InterruptedException e){};\n" + 
			"	}\n" + 
			"	void notifyExecutionEnd() {\n" + 
			"		System.out.println(\"EXECUTION FINISHED\");\n" + 
			"	}\n" + 
			"	void notifyExecutionStart() {\n" + 
			"		System.out.println(\"EXECUTION STARTING\");\n" + 
			"	}\n" + 
			"	void performTasks(Task tasks[]) throws java.lang.InterruptedException {\n" + 
			"		Thread workers[] = new Thread[tasks.length];\n" + 
			"		notifyExecutionStart();\n" + 
			"		\n" + 
			"		// Launching the tasks\n" + 
			"		for (int i = 0; i < tasks.length; i++) {\n" + 
			"			workers[i] = new Thread(tasks[i],\"Running task(\"+(tasks[i].toString())+\")\");\n" + 
			"			workers[i].start();\n" + 
			"		}\n" + 
			"		// Waiting for *all* tasks to be ended\n" + 
			"		for (int i = 0; i < tasks.length; i++)\n" + 
			"			workers[i].join();\n" + 
			"		notifyExecutionEnd();\n" + 
			"	}\n" + 
			"}\n"
		}, 
		"EXECUTION STARTING\n" + 
		"EXECUTION FINISHED"
	);									
}
/**
 * No need for protected access emulation
 */
public void test023() {
	this.runNegativeTest(
		new String[] {
			/* X.java */
			"p/X.java",
			"public class X extends q.Y { \n" +
			"	void bar(){ Object o = someObject; } \n"+
			"	public static void main(String[] argv){ \n" +
			"		new X().bar();\n"+
			"		System.out.println(\"SUCCESS\");\n"+
			"	}\n"+
			"}\n",
			/* Y.java */
			"q/Y.java",
			"package q; \n" + 
			"public class Y { \n" + 
			"	protected Object someObject; \n" + 
			"}\n"
		}, 
		""	// no problem log: not even a synthetic access emulation one
	);									
}
/**
 * No need for protected access emulation
 */
public void test024() {
	this.runNegativeTest(
		new String[] {
			/* X.java */
			"p/X.java",
			"package p; \n" + 
			"public class X extends q.Y { \n" +
			"	void bar(){ foo(); } \n"+
			"	public static void main(String[] argv){ \n" +
			"		new X().bar();\n"+
			"		System.out.println(\"SUCCESS\");\n"+
			"	}\n"+
			"}\n",
			/* Y.java */
			"q/Y.java",
			"package q; \n" + 
			"public class Y { \n" + 
			"	protected Object foo(){ return null;} \n" + 
			"}\n"
		}, 
		""	// no problem log: not even a synthetic access emulation one
	);									
}

public void test025() {
	this.runConformTest(
		new String[] {
			/* PortReport.java */
			"PortReport.java",
			"import java.util.*; \n"+
			"public class PortReport { \n"+
			"	public static void main(String[] args) { \n"+
			"		Portfolio port = new Portfolio(\"foobar\"); \n"+
			"		System.out.println(\"SUCCESS\"); \n" +
			"	} \n"+
			"} \n",
			/* Portfolio.java */
			"Portfolio.java",
			"import java.util.*;	\n"+
			"public class Portfolio {	\n"+
			"	String name;	\n"+
			"	public Portfolio(String buf) {	\n"+
			"		TokenBuffer tbuf = new TokenBuffer();	\n"+
			"		switch (1) {	\n"+
			"			case TokenBuffer.T_NAME :	\n"+
			"				name = \"figi\";	\n"+
			"		}	\n"+
			"	}	\n"+
			"	String getName() {	\n"+
			"		return name;	\n"+
			"	}	\n"+
			"	class TokenBuffer {	\n"+
			"		static final int T_NAME = 3;	\n"+
			"	}	\n"+
			"}	\n"
		}, 
		"SUCCESS"
	);									
}
/**
 * Compatability - Compiler does not comply with 1.1 standard.
 */
public void test026() {
	this.runConformTest(
		new String[] {
			/* Test.java */
			"p2/Test.java",
			"package p2;	\n"+
			"public class Test {	\n"+
			"	public static void main (String args[]){	\n"+
			"		new c2();	\n"+
			"	}	\n"+
			"}	\n",
			/* c1.java */
			"p1/c1.java",
			"package p1;	\n"+
			"public class c1 {	\n"+
			"	protected class c1a{	\n"+
			"		public c1a(){}	\n"+
			"		public void foo(){ System.out.println(\"Foo called\");	\n"+
			"		}	\n"+
			"	};	\n"+
			"}	\n",
			/* c2.java */
			"p2/c2.java",
			"package p2;	\n"+
			"import p1.*;	\n"+
			"public class c2 extends c1 {	\n"+
			"	public c1a myC1a;	\n"+
			"	{	\n"+
			"		myC1a = new c1a();	\n"+
			"		myC1a.foo();	\n"+
			"	}	\n"+
			"}	\n"
		}, 
		"Foo called"
	);									
}
/**
 * Compatability - Compiler does not comply with 1.1 standard.
 */
public void test027() {
	this.runNegativeTest(
		new String[] {
			/* Test.java */
			"p2/Test.java",
			"package p2;	\n"+
			"public class Test {	\n"+
			"	public static void main (String args[]){	\n"+
			"		new c2();	\n"+
			"	}	\n"+
			"}	\n",
			/* c1.java */
			"p1/c1.java",
			"package p1;	\n"+
			"public class c1 {	\n"+
			"	public class c1m {	\n"+
			"		protected class c1a{	\n"+
			"			public c1a(){}	\n"+
			"			public void foo(){ System.out.println(\"Foo called\");	\n"+
			"			}	\n"+
			"		};	\n"+
			"	};	\n"+
			"}	\n",
			/* c2.java */
			"p2/c2.java",
			"package p2;	\n"+
			"import p1.*;	\n"+
			"public class c2 extends c1 {	\n"+
			"	public c1m.c1a myC1a;	\n"+
			"	{	\n"+
			"		myC1a = new c1m().new c1a();	\n"+
			"		myC1a.foo();	\n"+
			"	}	\n"+
			"}	\n"
		}, 
		"----------\n" + 
		"1. ERROR in p2\\c2.java (at line 4)\n" + 
		"	public c1m.c1a myC1a;	\n" + 
		"	       ^^^^^^^\n" + 
		"The type c1m.c1a is not visible\n" + 
		"----------\n" + 
		"2. ERROR in p2\\c2.java (at line 6)\n" + 
		"	myC1a = new c1m().new c1a();	\n" + 
		"	^^^^^\n" + 
		"myC1a cannot be resolved\n" + 
		"----------\n" + 
		"3. ERROR in p2\\c2.java (at line 6)\n" + 
		"	myC1a = new c1m().new c1a();	\n" + 
		"	                      ^^^\n" + 
		"The type c1.c1m.c1a is not visible\n" + 
		"----------\n" + 
		"4. ERROR in p2\\c2.java (at line 7)\n" + 
		"	myC1a.foo();	\n" + 
		"	^^^^^\n" + 
		"myC1a cannot be resolved\n" + 
		"----------\n"
	);									
}
/**
 * Compatability - Compiler does not comply with 1.1 standard.
 */
public void test028() {
	this.runNegativeTest(
		new String[] {
			/* Test.java */
			"p2/Test.java",
			"package p2;	\n"+
			"public class Test {	\n"+
			"	public static void main (String args[]){	\n"+
			"		new c2();	\n"+
			"	}	\n"+
			"}	\n",
			/* c1.java */
			"p1/c1.java",
			"package p1;	\n"+
			"public class c1 {	\n"+
			"	protected class c1m {	\n"+
			"		protected class c1a{	\n"+
			"			public c1a(){}	\n"+
			"			public void foo(){ System.out.println(\"Foo called\");	\n"+
			"			}	\n"+
			"		};	\n"+
			"	};	\n"+
			"}	\n",
			/* c2.java */
			"p2/c2.java",
			"package p2;	\n"+
			"import p1.*;	\n"+
			"public class c2 extends c1 {	\n"+
			"	public c1m.c1a myC1a;	\n"+
			"	{	\n"+
			"		myC1a = new c1m().new c1a();	\n"+
			"		myC1a.foo();	\n"+
			"	}	\n"+
			"}	\n"
		}, 
		"----------\n" + 
		"1. ERROR in p2\\c2.java (at line 4)\n" + 
		"	public c1m.c1a myC1a;	\n" + 
		"	       ^^^^^^^\n" + 
		"The type c1m.c1a is not visible\n" + 
		"----------\n" + 
		"2. ERROR in p2\\c2.java (at line 6)\n" + 
		"	myC1a = new c1m().new c1a();	\n" + 
		"	^^^^^\n" + 
		"myC1a cannot be resolved\n" + 
		"----------\n" + 
		"3. ERROR in p2\\c2.java (at line 6)\n" + 
		"	myC1a = new c1m().new c1a();	\n" + 
		"	        ^^^^^^^^^\n" + 
		"The constructor c1.c1m() is not visible\n" + 
		"----------\n" + 
		"4. ERROR in p2\\c2.java (at line 6)\n" + 
		"	myC1a = new c1m().new c1a();	\n" + 
		"	                      ^^^\n" + 
		"The type c1.c1m.c1a is not visible\n" + 
		"----------\n" + 
		"5. ERROR in p2\\c2.java (at line 7)\n" + 
		"	myC1a.foo();	\n" + 
		"	^^^^^\n" + 
		"myC1a cannot be resolved\n" + 
		"----------\n"
	);									
}
/**
 * Compatability - Compiler does not comply with 1.1 standard.
 */
public void test029() {
	this.runConformTest(
		new String[] {
			/* Test.java */
			"p2/Test.java",
			"package p2;	\n"+
			"public class Test {	\n"+
			"	public static void main (String args[]){	\n"+
			"		new c2();	\n"+
			"	}	\n"+
			"}	\n",
			/* c1.java */
			"p1/c1.java",
			"package p1;	\n"+
			"public class c1 {	\n"+
			"	protected class c1a{	\n"+
			"		public c1a(){}	\n"+
			"		public void foo(){ System.out.println(\"Foo called\");	\n"+
			"		}	\n"+
			"	};	\n"+
			"}	\n",
			/* c2.java */
			"p2/c2.java",
			"package p2;	\n"+
			"import p1.*;	\n"+
			"public class c2 extends c1 {	\n"+
			"	public c1.c1a myC1a;\n"+
			"	{	\n"+
			"		myC1a = new c1a();	\n"+
			"		myC1a.foo();	\n"+
			"	}	\n"+
			"}	\n"
		}, 
		"Foo called");									
}
/**
 * Compatability - Compiler does not comply with 1.1 standard.
 */
public void test030() {
	this.runNegativeTest(
		new String[] {
			/* Test.java */
			"p2/Test.java",
			"package p2;	\n"+
			"public class Test {	\n"+
			"	public static void main (String args[]){	\n"+
			"		new c2();	\n"+
			"	}	\n"+
			"}	\n",
			/* c1.java */
			"p1/c1.java",
			"package p1;	\n"+
			"public class c1 {	\n"+
			"	protected class c1a{	\n"+
			"		public c1a(){}	\n"+
			"		public void foo(){ System.out.println(\"Foo called\");	\n"+
			"		}	\n"+
			"	};	\n"+
			"}	\n",
			/* c2.java */
			"p2/c2.java",
			"package p2;	\n"+
			"import p1.*;	\n"+
			"public class c2 extends c1.c1a {// qualified acces does not work	\n"+
			"	public c1a myC1a; \n"+
			"	{	\n"+
			"		myC1a = new c1a();	\n"+
			"		myC1a.foo();	\n"+
			"	}	\n"+
			"}	\n"
		}, 
		"----------\n" + 
		"1. ERROR in p2\\c2.java (at line 3)\n" + 
		"	public class c2 extends c1.c1a {// qualified acces does not work	\n" + 
		"	                        ^^^^^^\n" + 
		"The type c1.c1a is not visible\n" + 
		"----------\n" + 
		"2. ERROR in p2\\c2.java (at line 4)\n" + 
		"	public c1a myC1a; \n" + 
		"	       ^^^\n" + 
		"c1a cannot be resolved to a type\n" + 
		"----------\n" + 
		"3. ERROR in p2\\c2.java (at line 6)\n" + 
		"	myC1a = new c1a();	\n" + 
		"	^^^^^\n" + 
		"myC1a cannot be resolved\n" + 
		"----------\n" + 
		"4. ERROR in p2\\c2.java (at line 6)\n" + 
		"	myC1a = new c1a();	\n" + 
		"	            ^^^\n" + 
		"c1a cannot be resolved to a type\n" + 
		"----------\n" + 
		"5. ERROR in p2\\c2.java (at line 7)\n" + 
		"	myC1a.foo();	\n" + 
		"	^^^^^\n" + 
		"myC1a cannot be resolved\n" + 
		"----------\n"
	);									
}
/**
 * Compatability - Compiler does not comply with 1.1 standard.
 */
public void test031() {
	this.runNegativeTest(
		new String[] {
			/* Test.java */
			"p2/Test.java",
			"package p2;	\n"+
			"public class Test {	\n"+
			"	public static void main (String args[]){	\n"+
			"		new c2();	\n"+
			"	}	\n"+
			"}	\n",
			/* c1.java */
			"p1/c1.java",
			"package p1;	\n"+
			"public class c1 {	\n"+
			"	protected class c1a{	\n"+
			"		public c1a(){}	\n"+
			"		public void foo(){ System.out.println(\"Foo called\");	\n"+
			"		}	\n"+
			"	};	\n"+
			"}	\n",
			/* c2.java */
			"p2/c2.java",
			"package p2;	\n"+
			"import p1.c1.*;	\n"+
			"public class c2 extends c1a {	\n"+
			"	public c1a myC1a; \n"+
			"	{	\n"+
			"		myC1a = new c1a();	\n"+
			"		myC1a.foo();	\n"+
			"	}	\n"+
			"}	\n"
		}, 
		"----------\n" + 
		"1. ERROR in p2\\c2.java (at line 3)\n" + 
		"	public class c2 extends c1a {	\n" + 
		"	                        ^^^\n" + 
		"The type c1a is not visible\n" + 
		"----------\n" + 
		"2. ERROR in p2\\c2.java (at line 4)\n" + 
		"	public c1a myC1a; \n" + 
		"	       ^^^\n" + 
		"The type c1a is not visible\n" + 
		"----------\n" + 
		"3. ERROR in p2\\c2.java (at line 6)\n" + 
		"	myC1a = new c1a();	\n" + 
		"	^^^^^\n" + 
		"myC1a cannot be resolved\n" + 
		"----------\n" + 
		"4. ERROR in p2\\c2.java (at line 6)\n" + 
		"	myC1a = new c1a();	\n" + 
		"	            ^^^\n" + 
		"The type c1a is not visible\n" + 
		"----------\n" + 
		"5. ERROR in p2\\c2.java (at line 7)\n" + 
		"	myC1a.foo();	\n" + 
		"	^^^^^\n" + 
		"myC1a cannot be resolved\n" + 
		"----------\n"

	);									
}
/**
 * VerifyError using .class literal inside inner classes
 */
public void test032() {
	this.runConformTest(
		new String[] {
			"p/A.java",
			"package p;	\n"+
			"public class A {\n" +
			"		public class B {\n" +
			"				public B() {\n" +
			"				}\n" +
			"				public Class getCls() {\n" +
			"						return A.class;\n" +
			"				}\n" +
			"		}\n" +
			"		public A() {\n" +
			"				super();\n" +
			"				B b = new B();\n" +
			"				System.out.println(\"Class: \" + b.getCls());\n" + 
			"		}\n" +
			"		public static void main(String[] args) {\n" +
			"				A a = new A();\n" +
			"		}\n" +
			"}\n"
		},
		"Class: class p.A");
}
/**
 * Missing implementation in the compiler compiling invalid code
 */
public void test033() {
	this.runNegativeTest(
		new String[] {
			/* A2.java */
			"p1/A2.java",
			"package p1;	\n"+
			"class D {	\n"+
			"	class E {	\n"+
			"		E(Object o, Object o1, Object o2, F f) {}	\n"+
			"		void execute() {}	\n"+
			"	}	\n"+
			"}	\n"+
			"class F {	\n"+
			"	F(Object o) {	\n"+
			"	}	\n"+
			"}	\n"+
			"public class A2 {	\n"+
			"	private abstract class B {	\n"+
			"		public Object get() {	\n"+
			"			return null;	\n"+
			"		}	\n"+
			"	}	\n"+
			"	private class C extends B {	\n"+
			"		public void foo() {	\n"+
			"			(new D.E(null, null, null, new F(get()) {}) {}).execute();	\n"+
			"		}	\n"+
			"	}	\n"+
			"}\n"
		}, 
		"----------\n" + 
		"1. WARNING in p1\\A2.java (at line 18)\n" + 
		"	private class C extends B {	\n" + 
		"	              ^\n" + 
		"The type A2.C is never used locally\n" + 
		"----------\n" + 
		"2. WARNING in p1\\A2.java (at line 18)\n" + 
		"	private class C extends B {	\n" + 
		"	              ^\n" + 
		"Access to enclosing constructor A2.B() is emulated by a synthetic accessor method. Increasing its visibility will improve your performance\n" + 
		"----------\n" + 
		"3. ERROR in p1\\A2.java (at line 20)\n" + 
		"	(new D.E(null, null, null, new F(get()) {}) {}).execute();	\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"No enclosing instance of type D is accessible. Must qualify the allocation with an enclosing instance of type D (e.g. x.new A() where x is an instance of D).\n" + 
		"----------\n");									
}
/**
 * Missing implementation in the compiler compiling invalid code
 */
public void test034() {
	this.runConformTest(
		new String[] {
			/* A2.java */
			"p1/A2.java",
			"package p1;	\n"+
			"class D {	\n"+
			"	class E {	\n"+
			"		E(Object o, Object o1, Object o2, F f) {}	\n"+
			"		void execute() {}	\n"+
			"	}	\n"+
			"}	\n"+
			"class F {	\n"+
			"	F(Object o) {	\n"+
			"	}	\n"+
			"}	\n"+
			"public class A2 {	\n"+
			"	private abstract class B {	\n"+
			"		public Object get() {	\n"+
			"			return null;	\n"+
			"		}	\n"+
			"	}	\n"+
			"	private class C extends B {	\n"+
			"		public void foo() {	\n"+
			"			(new D().new E(null, null, null, new F(get()) {}) {}).execute();	\n"+
			"		}	\n"+
			"	}	\n"+
			"	public static void main(String[] argv){	\n"+
			"		new A2().new C().foo();	\n"+
			"		System.out.println(\"SUCCESS\");	\n"+
			"	}	\n"+
			"}	\n"
		}, 
		"SUCCESS"
	);									
}
/**
 * Missing implementation in the compiler compiling invalid code
 */
public void test035() {
	this.runNegativeTest(
		new String[] {
			/* A2.java */
			"p1/A2.java",
			"package p1;	\n"+
			"class D {	\n"+
			"	class E {	\n"+
			"		E(Object o, Object o1, Object o2, F f) {}	\n"+
			"		void execute() {}	\n"+
			"	}	\n"+
			"}	\n"+
			"class F {	\n"+
			"	F(Object o) {	\n"+
			"	}	\n"+
			"}	\n"+
			"public class A2 {	\n"+
			"	private abstract class B {	\n"+
			"		public Object get() {	\n"+
			"			return null;	\n"+
			"		}	\n"+
			"	}	\n"+
			"	private class C extends B {	\n"+
			"		public void foo() {	\n"+
			"			(new D.E(null, null, null, new F(get()) {})).execute();	\n"+
			"		}	\n"+
			"	}	\n"+
			"}\n"
		}, 
		"----------\n" + 
		"1. WARNING in p1\\A2.java (at line 18)\n" + 
		"	private class C extends B {	\n" + 
		"	              ^\n" + 
		"The type A2.C is never used locally\n" + 
		"----------\n" + 
		"2. WARNING in p1\\A2.java (at line 18)\n" + 
		"	private class C extends B {	\n" + 
		"	              ^\n" + 
		"Access to enclosing constructor A2.B() is emulated by a synthetic accessor method. Increasing its visibility will improve your performance\n" + 
		"----------\n" + 
		"3. ERROR in p1\\A2.java (at line 20)\n" + 
		"	(new D.E(null, null, null, new F(get()) {})).execute();	\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"No enclosing instance of type D is accessible. Must qualify the allocation with an enclosing instance of type D (e.g. x.new A() where x is an instance of D).\n" + 
		"----------\n");									
}
/**
 * ClassCastException during inner class emulation
 */
public void test036() {
	this.runConformTest(
		new String[] {
			/* A.java */
			"p1/A.java",
			"package p1;	\n"+
			"public class A {	\n"+
			"	public static void main(String[] argv){	\n"+
			"		new A().foo();	\n"+
			"		System.out.println(\"SUCCESS\");	\n"+
			"	}	\n"+
			"	public Object foo() {	\n"+
			"		B b = new B() {	\n"+
			"			protected Object bar() {	\n"+
			"				return new B.C() {};	\n"+
			"			}	\n"+
			"		};	\n"+
			"		return b;	\n"+
			"	}	\n"+
			"}	\n"+
			"class B {	\n"+
			"	class C {	\n"+
			"	}	\n"+
			"}	\n"
		}, 
		"SUCCESS"
	);									
}
/**
 * ClassCastException during inner class emulation
 */
public void test037() {
	this.runConformTest(
		new String[] {
			/* A.java */
			"p1/A.java",
			"package p1;	\n"+
			"public class A {	\n"+
			"	public static void main(String[] argv){	\n"+
			"		new A().foo();	\n"+
			"		System.out.println(\"SUCCESS\");	\n"+
			"	}	\n"+
			"	public Object foo() {	\n"+
			"		I i = new I() {	\n"+
			"			protected Object bar() {	\n"+
			"				return new I.C() {};	\n"+
			"			}	\n"+
			"		};	\n"+
			"		return i;	\n"+
			"	}	\n"+
			"}	\n"+
			"interface I {	\n"+
			"	class C {	\n"+
			"	}	\n"+
			"}	\n"
		}, 
		"SUCCESS"
	);									
}

/**
 * Enclosing instance comparison
 */
public void test038() {
	this.runConformTest(
		new String[] {
			/* X.java */
			"X.java",
			"public class X {	\n"+
			"	public static void main(String argv[]) {	\n"+
			"		if (new X().foo())	\n"+
			"			System.out.println(\"FAILED\");	\n"+
			"		System.out.println(\"SUCCESS\");	\n"+
			"	}	\n"+
			"	boolean bar() {	\n"+
			"		return false;	\n"+
			"	}	\n"+
			"	boolean foo() {	\n"+
			"		X x = new X() {	\n"+
			"			boolean test() {	\n"+
			"				return (X.this == this);	\n"+
			"			}	\n"+
			"		};	\n"+
			"		return x.bar();	\n"+
			"	}	\n"+
			"}	\n"
		}, 
		"SUCCESS"
	);									
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=6456
 * Invalid error when compiling access to protected member inside innerclass   
 */
public void test039() {
	this.runConformTest(
		new String[] {
			/* p1/X.java */
			"p1/X.java",
			"package p1;	\n"+
			"public class X {	\n"+
			"	protected void foo() {	\n"+
			"		System.out.println(\"SUCCESS\");	\n"+
			"	}	\n"+
			"	public static void main(String argv[]) {	\n"+
			"		new p2.Y().bar();	\n"+
			"	}	\n"+
			"}	\n",
			/* p2/Y.java */
			"p2/Y.java",
			"package p2;	\n"+
			"public class Y extends p1.X {	\n"+
			"	public void bar() {	\n"+
			"		new Object(){	\n"+
			"	      void doSomething(){	\n"+
			"	        Y.this.foo(); \n"+
			"	      }	\n"+
			"	    }.doSomething();	\n"+
			"	}	\n"+
			"}	\n",
		}, 
		"SUCCESS"
	);									
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=6456
 * Invalid error when compiling access to protected member inside innerclass   
 */
public void test040() {
	this.runConformTest(
		new String[] {
			/* p1/X.java */
			"p1/X.java",
			"package p1;	\n"+
			"public class X {	\n"+
			"	protected void foo() {	\n"+
			"		System.out.println(\"SUCCESS\");	\n"+
			"	}	\n"+
			"	public static void main(String argv[]) {	\n"+
			"		new p2.Y().bar();	\n"+
			"	}	\n"+
			"}	\n",
			/* p2/Y.java */
			"p2/Y.java",
			"package p2;	\n"+
			"public class Y extends p1.X {	\n"+
			"	public void bar() {	\n"+
			"		new Object(){	\n"+
			"	      void doSomething(){	\n"+
			"	        new Y().foo(); \n"+
			"	      }	\n"+
			"	    }.doSomething();	\n"+
			"	}	\n"+
			"}	\n",
		}, 
		"SUCCESS"
	);									
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=6456
 * Invalid error when compiling access to protected member inside innerclass   
 */
public void test041() {
	this.runConformTest(
		new String[] {
			/* p1/X.java */
			"p1/X.java",
			"package p1;	\n"+
			"public class X {	\n"+
			"	protected void foo() {	\n"+
			"		System.out.println(\"SUCCESS\");	\n"+
			"	}	\n"+
			"	public static void main(String argv[]) {	\n"+
			"		new p2.Y().bar();	\n"+
			"	}	\n"+
			"}	\n",
			/* p2/Y.java */
			"p2/Y.java",
			"package p2;	\n"+
			"public class Y extends p1.X {	\n"+
			"	public void bar() {	\n"+
			"		new Object(){	\n"+
			"	      void doSomething(){	\n"+
			"	        Y.super.foo(); \n"+
			"	      }	\n"+
			"	    }.doSomething();	\n"+
			"	}	\n"+
			"}	\n",
		}, 
		"SUCCESS"
	);									
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=6456
 * Invalid error when compiling access to protected member inside innerclass   
 */
public void test042() {
	this.runConformTest(
		new String[] {
			/* p1/X.java */
			"p1/X.java",
			"package p1;	\n"+
			"public class X {	\n"+
			"	protected String foo = \"SUCCESS\";	\n"+
			"	public static void main(String argv[]) {	\n"+
			"		new p2.Y().bar();	\n"+
			"	}	\n"+
			"}	\n",
			/* p2/Y.java */
			"p2/Y.java",
			"package p2;	\n"+
			"public class Y extends p1.X {	\n"+
			"	public void bar() {	\n"+
			"		new Object(){	\n"+
			"	      void doSomething(){	\n"+
			"	        System.out.println(Y.super.foo); \n"+
			"	      }	\n"+
			"	    }.doSomething();	\n"+
			"	}	\n"+
			"}	\n",
		}, 
		"SUCCESS"
	);									
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=6456
 * Invalid error when compiling access to protected member inside innerclass   
 */
public void test043() {
	this.runConformTest(
		new String[] {
			/* p1/X.java */
			"p1/X.java",
			"package p1;	\n"+
			"public class X {	\n"+
			"	protected String foo = \"SUCCESS\";	\n"+
			"	public static void main(String argv[]) {	\n"+
			"		new p2.Y().bar();	\n"+
			"	}	\n"+
			"}	\n",
			/* p2/Y.java */
			"p2/Y.java",
			"package p2;	\n"+
			"public class Y extends p1.X {	\n"+
			"	public void bar() {	\n"+
			"		new Object(){	\n"+
			"	      void doSomething(){	\n"+
			"	        System.out.println(Y.this.foo); \n"+
			"	      }	\n"+
			"	    }.doSomething();	\n"+
			"	}	\n"+
			"}	\n",
		}, 
		"SUCCESS"
	);									
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=6456
 * Invalid error when compiling access to protected member inside innerclass   
 */
public void test044() {
	this.runConformTest(
		new String[] {
			/* p1/X.java */
			"p1/X.java",
			"package p1;	\n"+
			"public class X {	\n"+
			"	protected String foo = \"SUCCESS\";	\n"+
			"	public static void main(String argv[]) {	\n"+
			"		new p2.Y().bar();	\n"+
			"	}	\n"+
			"}	\n",
			/* p2/Y.java */
			"p2/Y.java",
			"package p2;	\n"+
			"public class Y extends p1.X {	\n"+
			"	public void bar() {	\n"+
			"		new Object(){	\n"+
			"	      void doSomething(){	\n"+
			"	        System.out.println(new Y().foo); \n"+
			"	      }	\n"+
			"	    }.doSomething();	\n"+
			"	}	\n"+
			"}	\n",
		}, 
		"SUCCESS"
	);									
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=6456
 * Invalid error when compiling access to protected member inside innerclass   
 */
public void test045() {
	this.runConformTest(
		new String[] {
			/* p1/X.java */
			"p1/X.java",
			"package p1;	\n"+
			"public class X {	\n"+
			"	protected String foo = \"SUCCESS\";	\n"+
			"	public static void main(String argv[]) {	\n"+
			"		new p2.Y().bar();	\n"+
			"	}	\n"+
			"}	\n",
			/* p2/Y.java */
			"p2/Y.java",
			"package p2;	\n"+
			"public class Y extends p1.X {	\n"+
			"	public void bar() {	\n"+
			"		new Object(){	\n"+
			"	      void doSomething(){	\n"+
			"	        System.out.println(foo); \n"+
			"	      }	\n"+
			"	    }.doSomething();	\n"+
			"	}	\n"+
			"}	\n",
		}, 
		"SUCCESS"
	);									
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=6456
 * Invalid error when compiling access to protected member inside innerclass   
 */
public void test046() {
	this.runConformTest(
		new String[] {
			/* p1/X.java */
			"p1/X.java",
			"package p1;	\n"+
			"public class X {	\n"+
			"	protected String foo = \"SUCCESS\";	\n"+
			"	public static void main(String argv[]) {	\n"+
			"		new p2.Y().bar();	\n"+
			"	}	\n"+
			"}	\n",
			/* p2/Y.java */
			"p2/Y.java",
			"package p2;	\n"+
			"public class Y extends p1.X {	\n"+
			"   Y someY;	\n"+
			"	public void bar() {	\n"+
			"		someY = this;	\n"+
			"		new Object(){	\n"+
			"	      void doSomething(){	\n"+
			"	        System.out.println(someY.foo); \n"+
			"	      }	\n"+
			"	    }.doSomething();	\n"+
			"	}	\n"+
			"}	\n",
		}, 
		"SUCCESS"
	);									
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=6456
 * Invalid error when compiling access to protected member inside innerclass   
 */
public void test047() {
	this.runConformTest(
		new String[] {
			/* p1/X.java */
			"p1/X.java",
			"package p1;	\n"+
			"public class X {	\n"+
			"	protected String foo = \"FAILED\";	\n"+
			"	public static void main(String argv[]) {	\n"+
			"		new p2.Y().bar();	\n"+
			"	}	\n"+
			"}	\n",
			/* p2/Y.java */
			"p2/Y.java",
			"package p2;	\n"+
			"public class Y extends p1.X {	\n"+
			"	public void bar() {	\n"+
			"		new Object(){	\n"+
			"	      void doSomething(){	\n"+
			"	        System.out.println(Y.super.foo = \"SUCCESS\"); \n"+
			"	      }	\n"+
			"	    }.doSomething();	\n"+
			"	}	\n"+
			"}	\n",
		}, 
		"SUCCESS"
	);									
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=6456
 * Invalid error when compiling access to protected member inside innerclass   
 */
public void test048() {
	this.runConformTest(
		new String[] {
			/* p1/X.java */
			"p1/X.java",
			"package p1;	\n"+
			"public class X {	\n"+
			"	protected String foo = \"FAILED\";	\n"+
			"	public static void main(String argv[]) {	\n"+
			"		new p2.Y().bar();	\n"+
			"	}	\n"+
			"}	\n",
			/* p2/Y.java */
			"p2/Y.java",
			"package p2;	\n"+
			"public class Y extends p1.X {	\n"+
			"	public void bar() {	\n"+
			"		new Object(){	\n"+
			"	      void doSomething(){	\n"+
			"	        System.out.println(Y.this.foo = \"SUCCESS\"); \n"+
			"	      }	\n"+
			"	    }.doSomething();	\n"+
			"	}	\n"+
			"}	\n",
		}, 
		"SUCCESS"
	);									
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=6456
 * Invalid error when compiling access to protected member inside innerclass   
 */
public void test049() {
	this.runConformTest(
		new String[] {
			/* p1/X.java */
			"p1/X.java",
			"package p1;	\n"+
			"public class X {	\n"+
			"	protected String foo = \"FAILED\";	\n"+
			"	public static void main(String argv[]) {	\n"+
			"		new p2.Y().bar();	\n"+
			"	}	\n"+
			"}	\n",
			/* p2/Y.java */
			"p2/Y.java",
			"package p2;	\n"+
			"public class Y extends p1.X {	\n"+
			"	public void bar() {	\n"+
			"		new Object(){	\n"+
			"	      void doSomething(){	\n"+
			"	        System.out.println(new Y().foo = \"SUCCESS\"); \n"+
			"	      }	\n"+
			"	    }.doSomething();	\n"+
			"	}	\n"+
			"}	\n",
		}, 
		"SUCCESS"
	);									
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=6456
 * Invalid error when compiling access to protected member inside innerclass   
 */
public void test050() {
	this.runConformTest(
		new String[] {
			/* p1/X.java */
			"p1/X.java",
			"package p1;	\n"+
			"public class X {	\n"+
			"	protected String foo = \"FAILED\";	\n"+
			"	public static void main(String argv[]) {	\n"+
			"		new p2.Y().bar();	\n"+
			"	}	\n"+
			"}	\n",
			/* p2/Y.java */
			"p2/Y.java",
			"package p2;	\n"+
			"public class Y extends p1.X {	\n"+
			"	public void bar() {	\n"+
			"		new Object(){	\n"+
			"	      void doSomething(){	\n"+
			"	        System.out.println(foo = \"SUCCESS\"); \n"+
			"	      }	\n"+
			"	    }.doSomething();	\n"+
			"	}	\n"+
			"}	\n",
		}, 
		"SUCCESS"
	);									
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=6456
 * Invalid error when compiling access to protected member inside innerclass   
 */
public void test051() {
	this.runConformTest(
		new String[] {
			/* p1/X.java */
			"p1/X.java",
			"package p1;	\n"+
			"public class X {	\n"+
			"	protected String foo = \"FAILED\";	\n"+
			"	public static void main(String argv[]) {	\n"+
			"		new p2.Y().bar();	\n"+
			"	}	\n"+
			"}	\n",
			/* p2/Y.java */
			"p2/Y.java",
			"package p2;	\n"+
			"public class Y extends p1.X {	\n"+
			"   Y someY;	\n"+
			"	public void bar() {	\n"+
			"		someY = this;	\n"+
			"		new Object(){	\n"+
			"	      void doSomething(){	\n"+
			"	        System.out.println(someY.foo = \"SUCCESS\"); \n"+
			"	      }	\n"+
			"	    }.doSomething();	\n"+
			"	}	\n"+
			"}	\n",
		}, 
		"SUCCESS"
	);									
}


public void test052() {
	this.runConformTest(
		new String[] {
			/* p2/Y.java */
			"p2/Y.java",
			"package p2;	\n"+
			"public class Y {	\n"+
			"	void bar(final int someVal){	\n"+
			"		class Local {	\n"+
			"			void localDo(final int localVal){	\n"+
			"				new Object(){	\n"+
			"					void doSomething(){	\n"+
			"						System.out.print(someVal + localVal);	\n"+
			"					}	\n"+
			"				}.doSomething();	\n"+
			"			}				\n"+
			"		};	\n"+
			"		Local loc = new Local();	\n"+
			"		loc.localDo(8);	\n"+
			"		class SubLocal extends Local {	\n"+
			"			void localDo(final int localVal){	\n"+
			"				super.localDo(localVal + 1);	\n"+
			"				new Object(){	\n"+
			"					void doSomething(){	\n"+
			"						SubLocal.super.localDo(localVal + 2);	\n"+
			"						System.out.print(someVal + localVal + 3);	\n"+
			"					}	\n"+
			"				}.doSomething();	\n"+
			"			}	\n"+
			"		};	\n"+
			"		SubLocal subloc = new SubLocal();	\n"+
			"		subloc.localDo(8);	\n"+
			"	}	\n"+
			"	public static void main(String[] arguments) {	\n"+
			"		new Y().bar(4);	\n"+
			"		System.out.println();	\n"+
			"	}	\n"+
			"}	\n",
		},
		"12131415"
	);
}

public void test053() {
	this.runConformTest(
		new String[] {
			/* p2/Z.java */
			"p2/Z.java",
			"package p2;	\n"+
			"import p1.X;	\n"+
			"public class Z {	\n"+
			"	class ZMember extends X {	\n"+
			"		ZMember(Object o){}	\n"+
			"		Z bar(){	\n"+
			"			System.out.println(foo = \"FAILED\");	\n"+
			"			return Z.this;	\n"+
			"		}	\n"+
			"	}	\n"+
			"	Z(final Object foo){	\n"+
			"		Object obj2 = new Object(){	\n"+
			"			Object doSomething(){	\n"+
			"				ZMember obj3 = new ZMember(foo){	\n"+
			"					Z bar(){	\n"+
			"						System.out.println(this.foo);	\n"+
			"						return Z.this;	\n"+
			"					} \n"+
			"				};	\n"+
			"				obj3.bar();	\n"+
			"				return this;	\n"+
			"			}	\n"+
			"		}.doSomething();	\n"+
			"	}		\n"+
			"	public static void main(String[] arguments) {	\n"+
			"		new Z(new Object());	\n"+
			"	}	\n"+
			"}	\n",
			/* p1/X.java */
			"p1/X.java",
			"package p1;	\n"+
			"public class X {	\n"+
			"	protected String foo = \"SUCCESS\";	\n"+
			"}	\n"
		},
		"SUCCESS"
	);
}
public void test055() {
	this.runNegativeTest(
		new String[] {
			/* p1/X.java */
			"p1/X.java",
			"package p1;	\n"+
			"public class X {	\n"+
			"	void foo() {	\n"+
			"		class L1 {	\n"+
			"			class LM1 {	\n"+
			"			}	\n"+
			"		}	\n"+
			"		class L2 extends L1.LM1 {	\n"+
			"		}	\n"+
			"		new L2();	\n"+
			"	}	\n"+
			"	public static void main(String[] arguments) {	\n"+
			"		new X().foo();	\n"+
			"	}	\n"+
			"}	\n",
		},
		"----------\n" + 
		"1. ERROR in p1\\X.java (at line 8)\n" + 
		"	class L2 extends L1.LM1 {	\n" + 
		"	      ^^\n" + 
		"No enclosing instance of type L1 is accessible to invoke the super constructor. Must define a constructor and explicitly qualify its super constructor invocation with an instance of L1 (e.g. x.super() where x is an instance of L1).\n" + 
		"----------\n"

	);
}

public void test056() {
	this.runNegativeTest(
		new String[] {
			/* p1/X.java */
			"p1/X.java",
			"package p1;	\n"+
			"public class X {	\n"+
			"	void foo() {	\n"+
			"		class L1 {	\n"+
			"			class LM1 {	\n"+
			"			}	\n"+
			"		}	\n"+
			"		new L1().new LM1(){};	//ok\n"+
			"		new L1.LM1(){};	//ko\n"+
			"	}	\n"+
			"	public static void main(String[] arguments) {	\n"+
			"		new X().foo();	\n"+
			"	}	\n"+
			"}	\n",
		},
		"----------\n" + 
		"1. ERROR in p1\\X.java (at line 9)\n" + 
		"	new L1.LM1(){};	//ko\n" + 
		"	^^^^^^^^^^^^^^\n" + 
		"No enclosing instance of type L1 is accessible. Must qualify the allocation with an enclosing instance of type L1 (e.g. x.new A() where x is an instance of L1).\n" + 
		"----------\n"
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=9813
 * VerifyError with Inner Class having private constructor
 */
public void test057() {
	this.runConformTest(
		new String[] {
			/* VE_Test.java */
			"VE_Test.java",
			"public class VE_Test {	\n"+
			"    class VE_Inner {	\n"+
			"        private VE_Inner() {}	\n"+
			"    }	\n"+
			"    private static void test(){	\n"+
			"        VE_Test ve_test = new VE_Test();	\n"+
			"        VE_Inner pi = ve_test.new VE_Inner();	\n"+
			"    }	\n"+
			"    public static void main(String[] args){	\n"+
			"        new VE_Test();	\n"+
			"        System.out.println(\"SUCCESS\");	\n"+
			"    }	\n"+
			"}	\n",
		},
		"SUCCESS"
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=11845
 * NPE during emulation
 */
public void test058() {
	this.runConformTest(
		new String[] {
			"p1/Test.java",
			"package p1;	\n"+
			"import p2.A;	\n"+
			"public class Test {	\n"+
			"    public static void main(String[] args){	\n"+
			"        new Test().a.bar();	\n"+
			"    }	\n"+
			"	private A a = new A() {	\n"+
			"		public void bar() {	\n"+
			"			new Object() {	\n"+
			"				protected void foo() {	\n"+
			"					init();	\n"+
			"				}	\n"+
			"			}.foo(); 	\n"+
			"		}	\n"+
			"	};	\n"+
			"	private void init() {	\n"+
			"		System.out.println(\"SUCCESS\");	\n"+
			"	}	\n" +
			"}	\n",
			"p2/A.java",
			"package p2;	\n"+
			"public class A {	\n"+
			"  public void bar() {	\n"+
			"  }	\n"+
			"  private void init() {	\n"+
			"		System.out.println(\"FAILED\");	\n"+
			"  }	\n"+
			"} 	\n"			
		},
		"SUCCESS"
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=11845
 * variant for single name ref
 */
public void test059() {
	this.runConformTest(
		new String[] {
			"p1/Test.java",
			"package p1;	\n"+
			"import p2.A;	\n"+
			"public class Test {	\n"+
			"    public static void main(String[] args){	\n"+
			"        new Test().a.bar();	\n"+
			"    }	\n"+
			"	private A a = new A() {	\n"+
			"		public void bar() {	\n"+
			"			new Object() {	\n"+
			"				protected void foo() {	\n"+
			"					System.out.println(init);	\n"+
			"				}	\n"+
			"			}.foo(); 	\n"+
			"		}	\n"+
			"	};	\n"+
			"	private String init = \"SUCCESS\";	\n"+
			"}	\n",
			"p2/A.java",
			"package p2;	\n"+
			"public class A {	\n"+
			"  public void bar() {	\n"+
			"  }	\n"+
			"	private String init = \"FAILED\";	\n"+
			"} 	\n"			
		},
		"SUCCESS"
	);
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=11845
 * variant for qualified name ref
 */
public void test060() {
	this.runConformTest(
		new String[] {
			"p1/Test.java",
			"package p1;	\n"+
			"import p2.A;	\n"+
			"public class Test {	\n"+
			"    public static void main(String[] args){	\n"+
			"        new Test().a.bar();	\n"+
			"    }	\n"+
			"	private A a = new A() {	\n"+
			"		public void bar() {	\n"+
			"			class L {	\n"+
			"				Test next = Test.this;	\n"+
			"				protected void foo() {	\n"+
			"					System.out.println(next.init);	\n"+
			"				}	\n"+
			"			};	\n" +
			"			new L().foo(); 	\n"+
			"		}	\n"+
			"	};	\n"+
			"	private String init = \"SUCCESS\";	\n"+
			"}	\n",
			"p2/A.java",
			"package p2;	\n"+
			"public class A {	\n"+
			"  public void bar() {	\n"+
			"  }	\n"+
			"} 	\n"			
		},
		"SUCCESS"
	);
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=11845
 * variant for field name ref
 */
public void test061() {
	this.runConformTest(
		new String[] {
			"p1/Test.java",
			"package p1;	\n"+
			"import p2.A;	\n"+
			"public class Test {	\n"+
			"    public static void main(String[] args){	\n"+
			"        new Test().a.bar();	\n"+
			"    }	\n"+
			"	private A a = new A() {	\n"+
			"		public void bar() {	\n"+
			"			class L {	\n"+
			"				protected void foo() {	\n"+
			"					System.out.println(Test.this.init);	\n"+
			"				}	\n"+
			"			};	\n" +
			"			new L().foo(); 	\n"+
			"		}	\n"+
			"	};	\n"+
			"	private String init = \"SUCCESS\";	\n"+
			"}	\n",
			"p2/A.java",
			"package p2;	\n"+
			"public class A {	\n"+
			"  public void bar() {	\n"+
			"  }	\n"+
			"} 	\n"			
		},
		"SUCCESS"
	);
}

public void test062() {
	this.runConformTest(
		new String[] {
			"p1/X.java",
			"package p1;	\n"+
			"public class X {	\n"+
			"    public static void main(String args[]) {	\n"+
			"		final String s = args.length == 0 ? \"SUCCESS\" : null;	\n"+
			"		final class Local {	\n"+
			"			private Local() {	\n"+
			"				new LocalMember();	\n"+
			"			}	\n"+
			"			class LocalMember {	\n"+
			"				{	\n"+
			"					new LocalMemberMember();	\n"+
			"				}	\n"+
			"				class LocalMemberMember {	\n"+
			"					{	\n"+
			"						System.out.println(s);	\n"+
			"					}	\n"+
			"				}	\n"+
			"			}	\n"+
			"		}	\n"+
			"		new Local();	\n"+
			"    }	\n"+
			"}	\n"
		},
		"SUCCESS"
	);
}

public void test062a() {
	this.runConformTest(
		new String[] {
			"p1/X.java",
			"package p1;	\n"+
			"public class X {	\n"+
			"    public static void main(String args[]) {	\n"+
			"		final String s = \"SUCCESS\";\n"+
			"		final class Local {	\n"+
			"			Local() {	\n"+
			"				new LocalMember();	\n"+
			"			}	\n"+
			"			class LocalMember {	\n"+
			"				{	\n"+
			"					new LocalMemberMember();	\n"+
			"				}	\n"+
			"				class LocalMemberMember {	\n"+
			"					{	\n"+
			"						System.out.println(s);	\n"+
			"					}	\n"+
			"				}	\n"+
			"			}	\n"+
			"		}	\n"+
			"		new Local();	\n"+
			"    }	\n"+
			"}	\n"
		},
		"SUCCESS"
	);
}

public void test063() {
	this.runConformTest(
		new String[] {
			"p1/X.java",
			"package p1;	\n"+
			"public class X {	\n"+
			"	String a = new Object(){	\n"+
			"			class LocalMember {	\n"+
			"				String s = \"SUCCESS\";	\n"+
			"			};	\n"+
			"			String get_a(){	\n"+
			"				return new LocalMember().s;	\n"+
			"			}	\n"+
			"	}.get_a();	\n"+
			"	public static void main(String argv[]) {	\n"+
			"		System.out.println(new X().a);	\n"+
			"	}	\n"+
			"}	\n"
		},
		"SUCCESS"
		);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=21447
 * should invoke Y.foo() at runtime
 */
public void test064(){
	this.runConformTest(
		new String[] {
			"p2/Y.java",
			"package p2;	\n"+
			"public class Y extends T {	\n"+
			"	public static void main(String argv[]) {	\n"+
			"		new Y().bar();	\n"+
			"	}	\n"+
			"	protected void foo() {	\n"+
			"		System.out.println(\"Y.foo()\");	\n"+
			"	}	\n"+
			"}	\n",
			"p1/X.java",
			"package p1;	\n"+
			"public class X {	\n"+
			"	protected void foo() {	\n"+
			"		System.out.println(\"X.foo()\");	\n"+
			"	}	\n"+
			"}	\n",
			"p2/T.java",
			"package p2;	\n"+
			"public class T extends p1.X {	\n"+
			"	public void bar() {	\n"+
			"		new Object(){	\n"+
			"			void doSomething(){	\n" +
			"				T.this.foo();	\n"+
			"			}	\n" +
			"		}.doSomething();	\n" +
			"	}	\n"+
			"}	\n",
		},
		"Y.foo()"
		);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=21447
 * variation - if T.foo() is defined
 */
public void test065(){
	this.runConformTest(
		new String[] {
			"p2/Y.java",
			"package p2;	\n"+
			"public class Y extends T {	\n"+
			"	public static void main(String argv[]) {	\n"+
			"		new Y().bar();	\n"+
			"	}	\n"+
			"	protected void foo() {	\n"+
			"		System.out.println(\"Y.foo()\");	\n"+
			"	}	\n"+
			"}	\n",
			"p1/X.java",
			"package p1;	\n"+
			"public class X {	\n"+
			"	protected void foo() {	\n"+
			"		System.out.println(\"X.foo()\");	\n"+
			"	}	\n"+
			"}	\n",
			"p2/T.java",
			"package p2;	\n"+
			"public class T extends p1.X {	\n"+
			"	public void bar() {	\n"+
			"		new Object(){	\n"+
			"			void doSomething(){	\n" +
			"				T.this.foo();	\n"+
			"			}	\n" +
			"		}.doSomething();	\n" +
			"	}	\n"+
			"	protected void foo() {	\n"+
			"		System.out.println(\"T.foo()\");	\n"+
			"	}	\n"+
			"}	\n",
		},
		"Y.foo()"
		);
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=21447
 * should invoke Y.foo() and X.foo() at runtime (through 2 separate access methods)
 */
public void test066(){
	this.runConformTest(
		new String[] {
			"p2/Y.java",
			"package p2;	\n"+
			"public class Y extends T {	\n"+
			"	public static void main(String argv[]) {	\n"+
			"		new Y().bar();	\n"+
			"	}	\n"+
			"	protected void foo() {	\n"+
			"		System.out.print(\"Y.foo()-\");	\n"+
			"	}	\n"+
			"}	\n",
			"p1/X.java",
			"package p1;	\n"+
			"public class X {	\n"+
			"	protected void foo() {	\n"+
			"		System.out.println(\"X.foo()\");	\n"+
			"	}	\n"+
			"}	\n",
			"p2/T.java",
			"package p2;	\n"+
			"public class T extends p1.X {	\n"+
			"	public void bar() {	\n"+
			"		new Object(){	\n"+
			"			void doSomething(){	\n" +
			"				T.this.foo();	\n"+
			"				T.super.foo();	//need extra access method \n"+
			"			}	\n" +
			"		}.doSomething();	\n" +
			"	}	\n"+
			"}	\n",
		},
		"Y.foo()-X.foo()"
		);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=22093
 * test collision check for synthetic accessors to constructors
 */
public void test067(){
	this.runConformTest(
		new String[] {
			"p/Test.java",
			"package p;	\n"+
			"public class Test {	\n"+
			"	class Member {	\n"+
			"		private Member(){	\n"+
			"		}	\n"+
			"		private Member(Member other){	\n"+
			"		}	\n"+
			"	}	\n"+
			"	public static void main(String[] arguments) {	\n"+
			"		Test t = new Test();	\n"+
			"		Member m1 = t.new Member();	\n"+
			"		t.new Member(m1);	\n"+
			"		System.out.println(\"SUCCESS\");	\n"+
			"	}	\n"+
			"}	\n"
		},
		"SUCCESS"); 
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=25229
public void test068(){
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n"+
			"	public static void main(String[] arguments) {	\n"+
			"		new X().new X2().foo();	\n"+
			"	}	\n"+
			"	class X1 {	\n"+
			"		void baz1() {	\n"+
			"			System.out.print(\"-X1.baz1()\");	\n"+
			"		}	\n"+
			"	}	\n"+
			"	class X2 {	\n"+
			"		void foo(){	\n"+
			"			X.this.new X1(){	\n"+
			"				void bar(){	\n"+
			"					baz();	\n"+
			"					baz1();	\n"+
			"					baz2();	\n"+
			"				}	\n"+
			"			}.bar();	\n"+
			"		}		\n"+
			"		void baz2() {	\n"+
			"			System.out.println(\"-X2.baz2()\");	\n"+
			"		}	\n"+
			"	}	\n"+
			"	void baz() {	\n"+
			"		System.out.print(\"X.baz()\");	\n"+
			"	}	\n"+
			"}	\n"
		},
		"X.baz()-X1.baz1()-X2.baz2()"); 
}

// http://bugs.eclipse.org/bugs/show_bug.cgi?id=26122
// synthetic outer local variables must be appended after user arguments
public void test069() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n"+
			"    X(String s, int j) {}	\n"+
			"    public static void main(String[] args) {	\n"+
			"        final int i;	\n"+
			"        new X(\"hello\", i = 1) {	\n"+ // val$i must be appended after i got assigned
			"            { 	\n"+
			"            	System.out.print(\"SUCCESS:\"+i); 	\n"+
			"            }	\n"+
			"        };	\n"+
			"    }	\n"+
			"}	\n"
		},
		"SUCCESS:1");
}
// variation on test069
public void test070() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n"+
			"    X() {}	\n"+
			"    public static void main(String[] args) {	\n"+
			"        final int i;	\n"+
			"        new X().new Y(\"hello\", i = 1) {	\n"+ // val$i must be appended after i got assigned
			"            { 	\n"+
			"            	System.out.print(\"SUCCESS:\"+i); 	\n"+
			"            }	\n"+
			"        };	\n"+
			"    }	\n"+
			"	class Y {	\n" +
			"		Y(String s, int j) {}	\n" +
			"	}	\n" +
			"}	\n"
		},
		"SUCCESS:1");
}

// test too many synthetic arguments
public void test071() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {	\n"+
			"	void foo(int i) {	\n"+
			"		final long v0 = i, v1 = i, v2 = i, v3 = i, v4 = i, v5 = i, v6 = i;	\n"+
			"		final long v7 = i, v8 = i, v9 = i, v10 = i, v11 = i, v12 = i, v13 = i;	\n"+
			"		final long v14 = i, v15 = i, v16 = i, v17 = i, v18 = i, v19 = i, v20 = i;	\n"+
			"		final long v21 = i, v22 = i, v23 = i, v24 = i, v25 = i, v26 = i, v27 = i;	\n"+
			"		final long v28 = i, v29 = i, v30 = i, v31 = i, v32 = i, v33 = i, v34 = i;	\n"+
			"		final long v35 = i, v36 = i, v37 = i, v38 = i, v39 = i, v40 = i, v41 = i;	\n"+
			"		final long v42 = i, v43 = i, v44 = i, v45 = i, v46 = i, v47 = i, v48 = i;	\n"+
			"		final long v49 = i, v50 = i, v51 = i, v52 = i, v53 = i, v54 = i, v55 = i;	\n"+
			"		final long v56 = i, v57 = i, v58 = i, v59 = i, v60 = i, v61 = i, v62 = i;	\n"+
			"		final long v63 = i, v64 = i, v65 = i, v66 = i, v67 = i, v68 = i, v69 = i;	\n"+
			"		final long v70 = i, v71 = i, v72 = i, v73 = i, v74 = i, v75 = i, v76 = i;	\n"+
			"		final long v77 = i, v78 = i, v79 = i, v80 = i, v81 = i, v82 = i, v83 = i;	\n"+
			"		final long v84 = i, v85 = i, v86 = i, v87 = i, v88 = i, v89 = i, v90 = i;	\n"+
			"		final long v91 = i, v92 = i, v93 = i, v94 = i, v95 = i, v96 = i, v97 = i;	\n"+
			"		final long v98 = i, v99 = i, v100 = i, v101 = i, v102 = i, v103 = i, v104 = i;	\n"+
			"		final long v105 = i, v106 = i, v107 = i, v108 = i, v109 = i, v110 = i, v111 = i;	\n"+
			"		final long v112 = i, v113 = i, v114 = i, v115 = i, v116 = i, v117 = i, v118 = i;	\n"+
			"		final long v119 = i, v120 = i, v121 = i, v122 = i, v123 = i, v124 = i, v125 = i;	\n"+
			"		final long v126 = i;												\n"+
			"		final int v127 = i;	// int is already too many arg					\n"+
			"		new X() {															\n"+
			"			{																\n"+
			"				System.out.println(											\n"+
			"					v0 + v1 + v2 + v3 + v4 + v5 + v6						\n" +
			"					+ v7 + v8 + v9 + v10 + v11 + v12 + v13					\n" +
			"					+ v14 + v15 + v16 + v17 + v18 + v19 + v20				\n" +
			"					+ v21 + v22 + v23 + v24 + v25 + v26 + v27				\n" +
			"					+ v28 + v29 + v30 + v31 + v32 + v33 + v34				\n" +
			"					+ v35 + v36 + v37 + v38 + v39 + v40 + v41				\n" +
			"					+ v42 + v43 + v44 + v45 + v46 + v47 + v48				\n" +
			"					+ v49 + v50 + v51 + v52 + v53 + v54 + v55				\n" +
			"					+ v56 + v57 + v58 + v59 + v60 + v61 + v62				\n" +
			"					+ v63 + v64 + v65 + v66 + v67 + v68 + v69				\n" +
			"					+ v70 + v71 + v72 + v73 + v74 + v75 + v76				\n" +
			"					+ v77 + v78 + v79 + v80 + v81 + v82 + v83				\n" +
			"					+ v84 + v85 + v86 + v87 + v88 + v89 + v90				\n" +
			"					+ v91 + v92 + v93 + v94 + v95 + v96 + v97				\n" +
			"					+ v98 + v99 + v100 + v101 + v102 + v103 + v104			\n" +
			"					+ v105 + v106 + v107 + v108 + v109 + v110 + v111		\n" +
			"					+ v112 + v113 + v114 + v115 + v116 + v117 + v118		\n" +
			"					+ v119 + v120 + v121 + v122 + v123 + v124 + v125		\n" +
			"					+ v126);												\n"+
			"			}	\n"+
			"		};	\n"+		
			"	}	\n" +
			"}	\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 23)\n" + 
		"	new X() {															\n" + 
		"	    ^^^\n" + 
		"Too many synthetic parameters, emulated parameter val$v126 is exceeding the limit of 255 words eligible for method parameters\n" + 
		"----------\n");
}

// test too many synthetic arguments
public void test072() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n"+
			"	void foo(int i) {	\n"+
			"		final long v0 = i, v1 = i, v2 = i, v3 = i, v4 = i, v5 = i, v6 = i;	\n"+
			"		final long v7 = i, v8 = i, v9 = i, v10 = i, v11 = i, v12 = i, v13 = i;	\n"+
			"		final long v14 = i, v15 = i, v16 = i, v17 = i, v18 = i, v19 = i, v20 = i;	\n"+
			"		final long v21 = i, v22 = i, v23 = i, v24 = i, v25 = i, v26 = i, v27 = i;	\n"+
			"		final long v28 = i, v29 = i, v30 = i, v31 = i, v32 = i, v33 = i, v34 = i;	\n"+
			"		final long v35 = i, v36 = i, v37 = i, v38 = i, v39 = i, v40 = i, v41 = i;	\n"+
			"		final long v42 = i, v43 = i, v44 = i, v45 = i, v46 = i, v47 = i, v48 = i;	\n"+
			"		final long v49 = i, v50 = i, v51 = i, v52 = i, v53 = i, v54 = i, v55 = i;	\n"+
			"		final long v56 = i, v57 = i, v58 = i, v59 = i, v60 = i, v61 = i, v62 = i;	\n"+
			"		final long v63 = i, v64 = i, v65 = i, v66 = i, v67 = i, v68 = i, v69 = i;	\n"+
			"		final long v70 = i, v71 = i, v72 = i, v73 = i, v74 = i, v75 = i, v76 = i;	\n"+
			"		final long v77 = i, v78 = i, v79 = i, v80 = i, v81 = i, v82 = i, v83 = i;	\n"+
			"		final long v84 = i, v85 = i, v86 = i, v87 = i, v88 = i, v89 = i, v90 = i;	\n"+
			"		final long v91 = i, v92 = i, v93 = i, v94 = i, v95 = i, v96 = i, v97 = i;	\n"+
			"		final long v98 = i, v99 = i, v100 = i, v101 = i, v102 = i, v103 = i, v104 = i;	\n"+
			"		final long v105 = i, v106 = i, v107 = i, v108 = i, v109 = i, v110 = i, v111 = i;	\n"+
			"		final long v112 = i, v113 = i, v114 = i, v115 = i, v116 = i, v117 = i, v118 = i;	\n"+
			"		final long v119 = i, v120 = i, v121 = i, v122 = i, v123 = i, v124 = i, v125 = i;	\n"+
			"		new X() {															\n"+
			"			{																\n"+
			"				System.out.println(											\n"+
			"					v0 + v1 + v2 + v3 + v4 + v5 + v6						\n" +
			"					+ v7 + v8 + v9 + v10 + v11 + v12 + v13					\n" +
			"					+ v14 + v15 + v16 + v17 + v18 + v19 + v20				\n" +
			"					+ v21 + v22 + v23 + v24 + v25 + v26 + v27				\n" +
			"					+ v28 + v29 + v30 + v31 + v32 + v33 + v34				\n" +
			"					+ v35 + v36 + v37 + v38 + v39 + v40 + v41				\n" +
			"					+ v42 + v43 + v44 + v45 + v46 + v47 + v48				\n" +
			"					+ v49 + v50 + v51 + v52 + v53 + v54 + v55				\n" +
			"					+ v56 + v57 + v58 + v59 + v60 + v61 + v62				\n" +
			"					+ v63 + v64 + v65 + v66 + v67 + v68 + v69				\n" +
			"					+ v70 + v71 + v72 + v73 + v74 + v75 + v76				\n" +
			"					+ v77 + v78 + v79 + v80 + v81 + v82 + v83				\n" +
			"					+ v84 + v85 + v86 + v87 + v88 + v89 + v90				\n" +
			"					+ v91 + v92 + v93 + v94 + v95 + v96 + v97				\n" +
			"					+ v98 + v99 + v100 + v101 + v102 + v103 + v104			\n" +
			"					+ v105 + v106 + v107 + v108 + v109 + v110 + v111		\n" +
			"					+ v112 + v113 + v114 + v115 + v116 + v117 + v118		\n" +
			"					+ v119 + v120 + v121 + v122 + v123 + v124 + v125);		\n" +
			"			}	\n"+
			"		};	\n"+		
			"	}	\n" +
			"    public static void main(String[] args) {	\n"+
			"       System.out.print(\"SUCCESS\"); 	\n"+
			"    }	\n"+
			"}	\n"
		},
		"SUCCESS");
}

/**
 * verify error in synthetic access to constructor
 * (ordering of parameters after moving outerlocals after user args)
 */
public void test073() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n" +
			"    public static void main(final String[] args) {	\n" +
			"		class Local {	\n" +
			"			private Local(String str){	\n" +
			"				Object o = args;	\n" +
			"				System.out.println(str);	\n" +
			"			}	\n" +
			"		};	\n" +
			"		new Local(\"SUCCESS\");	\n" +
			"	}	\n" +
			"}	\n" 
	},
	"SUCCESS");
}

/**
 * verify error in synthetic access to constructor - test collisions
 * (ordering of parameters after moving outerlocals after user args)
 */
public void test074() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n" +
			"    public static void main(final String[] args) {	\n" +
			"		class Local {	\n" +
			"			public Local(String str, Local loc, String str2){	\n" + // collision
			"			}	\n" +
			"			public Local(String str, Local loc){	\n" + // collision
			"			}	\n" +
			"			private Local(String str){	\n" +
			"				Object o = args;	\n" +
			"				System.out.println(str);	\n" +
			"			}	\n" +
			"		};	\n" +
			"		new Local(\"SUCCESS\");	\n" +
			"	}	\n" +
			"}	\n" 
	},
	"SUCCESS");
}

/**
 * should not consider synthetic methods on binary types
 */
public void test075() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n" +
			"   public static void main(final String[] args) {	\n" +
			"		System.out.println(\"SUCCESS\");	\n"+
			"	}	\n"+
			"	private static void foo() {	\n" +
			"	}	\n" +
			"    A a = new A();	\n" +
			"    class A {	\n" +
			"		private A() {}	\n" +
			"		A(String s) {	\n" +
			"			foo();	\n" +
			"		}	\n" +
			"		A(int s) {	\n" +
			"			foo();	\n" +
			"		}	\n" +
			"    }	\n" +
			"    class B extends A {	\n" +
			"    	B(){	\n" +
			"    		super();	\n" +
			"    	}	\n" +
			"    }	\n" +
			"}	\n" 
		},
		"SUCCESS");
		
	this.runNegativeTest(
		new String[] {
			"Y.java",
			"public class Y {	\n" +
			"	void foo(){	\n" +
			"		new X().new A(null);	\n" +
			"		new X().access$0();	\n" +
			"	}	\n"+
 			"}	\n"
		},
		"----------\n" + 
		"1. ERROR in Y.java (at line 4)\n" + 
		"	new X().access$0();	\n" + 
		"	        ^^^^^^^^\n" + 
		"The method access$0() is undefined for the type X\n" + 
		"----------\n",
		null, // use default class-path
		false); // do not flush previous output dir content
		
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=27413
 * implicit enclosing instances
 */
public void test076() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	if (options.sourceLevel <= ClassFileConstants.JDK1_3) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {	\n"+
				"	X(Object o){	\n"+
				"		class A { 	\n"+
				"			private A() {	\n"+ // implicit enclosing instance in non-static context
				"			}	\n"+
				"		}	\n"+
				"		class B extends X {	\n"+
				"			B() {	\n"+
				"				super(new A(){	\n"+
				"				});	\n"+
				"			}	\n"+
				"		}	\n"+
				"	}	\n"+
				"} 	\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 9)\n" + 
			"	super(new A(){	\n" + 
			"				});	\n" + 
			"	      ^^^^^^^^^^^^^^^\n" + 
			"No enclosing instance of type X is available due to some intermediate constructor invocation\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 9)\n" + 
			"	super(new A(){	\n" + 
			"	          ^^^\n" + 
			"Access to enclosing constructor A() is emulated by a synthetic accessor method. Increasing its visibility will improve your performance\n" + 
			"----------\n");
		return;
	}
	if (options.sourceLevel <= ClassFileConstants.JDK1_4) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {	\n"+
				"	X(Object o){	\n"+
				"		class A { 	\n"+
				"			private A() {	\n"+ // implicit enclosing instance in non-static context
				"			}	\n"+
				"		}	\n"+
				"		class B extends X {	\n"+
				"			B() {	\n"+
				"				super(new A(){	\n"+
				"				});	\n"+
				"			}	\n"+
				"		}	\n"+
				"	}	\n"+
				"} 	\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 9)\n" + 
			"	super(new A(){	\n" + 
			"				});	\n" + 
			"	      ^^^^^^^^^^^^^^^\n" + 
			"No enclosing instance of type X is available due to some intermediate constructor invocation\n" + 
			"----------\n");
		return;
	}
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n"+
			"	X(Object o){	\n"+
			"		class A { 	\n"+
			"			private A() {	\n"+ // implicit enclosing instance in non-static context
			"			}	\n"+
			"		}	\n"+
			"		class B extends X {	\n"+
			"			B() {	\n"+
			"				super(new A(){	\n"+
			"				});	\n"+
			"			}	\n"+
			"		}	\n"+
			"	}	\n"+
			"} 	\n"
		},
		"");
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=27413
 * implicit enclosing instances
 */
public void test077() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n"+
			"	public static void main(String[] args){	\n"+
			"		System.out.println(\"SUCCESS\");	\n"+
			"	}	\n" +
			"	X(Object o){	\n"+
			"	}	\n" +
			"	static void foo() {	\n"+ 
			"		class A { 	\n"+ // no implicit enclosing in STATIC context
			"			private A() {	\n"+
			"			}	\n"+
			"		}	\n"+
			"		class B extends X {	\n"+
			"			B() {	\n"+
			"				super(new A(){	\n"+
			"				});	\n"+
			"			}	\n"+
			"		}	\n"+
			"	}	\n"+
			"} 	\n"
		},
		"SUCCESS");
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=27413
 * implicit enclosing instances
 */
public void test078() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	if (options.sourceLevel <= ClassFileConstants.JDK1_3) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {	\n"+
				"	X(Object o){	\n"+
				"		class A { 	\n"+
				"			private A() {	\n"+ // implicit enclosing instance in non-static context
				"			}	\n"+
				"		}	\n"+
				"		class B extends X {	\n"+
				"			B() {	\n"+
				"				super(new A(){	\n"+
				"					void foo() { System.out.println(X.this);	} \n"+
				"				});	\n"+
				"			}	\n"+
				"		}	\n"+
				"	}	\n"+
				"} 	\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 9)\n" + 
			"	super(new A(){	\n" + 
			"					void foo() { System.out.println(X.this);	} \n" + 
			"				});	\n" + 
			"	      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"No enclosing instance of type X is available due to some intermediate constructor invocation\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 9)\n" + 
			"	super(new A(){	\n" + 
			"	          ^^^\n" + 
			"Access to enclosing constructor A() is emulated by a synthetic accessor method. Increasing its visibility will improve your performance\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 10)\n" + 
			"	void foo() { System.out.println(X.this);	} \n" + 
			"	     ^^^^^\n" + 
			"The method foo() from the type new A(){} is never used locally\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 10)\n" + 
			"	void foo() { System.out.println(X.this);	} \n" + 
			"	                                ^^^^^^\n" + 
			"No enclosing instance of the type X is accessible in scope\n" + 
			"----------\n");
		return;
	}
	if (options.sourceLevel <= ClassFileConstants.JDK1_4) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {	\n"+
				"	X(Object o){	\n"+
				"		class A { 	\n"+
				"			private A() {	\n"+ // implicit enclosing instance in non-static context
				"			}	\n"+
				"		}	\n"+
				"		class B extends X {	\n"+
				"			B() {	\n"+
				"				super(new A(){	\n"+
				"					void foo() { System.out.println(X.this);	} \n"+
				"				});	\n"+
				"			}	\n"+
				"		}	\n"+
				"	}	\n"+
				"} 	\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 9)\n" + 
			"	super(new A(){	\n" + 
			"					void foo() { System.out.println(X.this);	} \n" + 
			"				});	\n" + 
			"	      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"No enclosing instance of type X is available due to some intermediate constructor invocation\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 10)\n" + 
			"	void foo() { System.out.println(X.this);	} \n" + 
			"	     ^^^^^\n" + 
			"The method foo() from the type new A(){} is never used locally\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 10)\n" + 
			"	void foo() { System.out.println(X.this);	} \n" + 
			"	                                ^^^^^^\n" + 
			"No enclosing instance of the type X is accessible in scope\n" + 
			"----------\n");
		return;
	}
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n"+
			"	X(Object o){	\n"+
			"		class A { 	\n"+
			"			private A() {	\n"+ // implicit enclosing instance in non-static context
			"			}	\n"+
			"		}	\n"+
			"		class B extends X {	\n"+
			"			B() {	\n"+
			"				super(new A(){	\n"+
			"					void foo() { System.out.println(X.this);	} \n"+
			"				});	\n"+
			"			}	\n"+
			"		}	\n"+
			"	}	\n"+
			"} 	\n"
		},
		"");
}

/*
 * Check that X.this is actually bound to an X, and not innermost compatible type (Z)
 */
public void test079() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n"+
			"	public static void main(String[] arguments) {	\n"+
			"		new X().new Y().new Z().bar();	\n"+
			"	}	\n"+
			"	String foo() { return \"X-foo\"; }	\n"+
			"	class Y extends X {	\n"+
			"		String foo() { return \"Y-foo\"; }	\n"+
			"		class Z extends Y {	\n"+
			"			Z(){	\n"+
			"				X.this.super();	\n"+
			"			}	\n"+
			"			String foo() { return \"Z-foo\"; }	\n"+
			"			void bar () {	\n"+
			"				System.out.println(X.this.foo());	\n"+
			"			}	\n"+
			"		}	\n"+
			"	}	\n"+
			"}	\n"
		},
		"X-foo");
}

public void test080() { // verified as conform
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n"+
			"	public static void main(String[] arguments) { \n"+
			"		new X().new C();	\n"+
			"	}	\n"+
			"	int m() {	\n"+
			"		System.out.println(\"SUCCESS\");	\n"+
			"		return 1;	\n"+
			"	}	\n"+
			"	class C extends B {	\n"+
			"		C(int j){}	\n"+
			"		C(){	\n"+
			"			this(X.this.m());	\n"+
			"		}	\n"+
			"	}	\n"+
			"}	\n"+
			"class B extends X {	\n"+
			"}	\n"
		},
		"SUCCESS");
}

public void test081() {
	this.runConformTest(
		new String[] {
			"p/X.java",
			"package p;	\n"+
			"public class X {	\n"+
			"	public static void main(String[] arguments) { \n"+
			"		new X().new C();	\n"+
			"	}	\n"+
			"	int m() {	\n"+
			"		System.out.println(\"SUCCESS\");	\n"+
			"		return 1;	\n"+
			"	}	\n"+
			"	class C extends q.B {	\n"+
			"		C(int j){}	\n"+
			"		C(){	\n"+
			"			this(m());	\n"+ // only X.this can see m()
			"		}	\n"+
			"	}	\n"+
			"}	\n",
			"q/B.java",
			"package q;	\n"+
			"public class B extends p.X {	\n"+
			"}	\n"
		},
		"SUCCESS");
}

/*
 * Default constructor for Z, will use enclosing 'this' as default-value for enclosing instance for super().
 */
public void test083() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n"+
			"	public static void main(String[] arguments) {	\n"+
			"		new X().new Y().new Z().bar();	\n"+
			"	}	\n"+
			"	String foo() { return \"X-foo\"; }	\n"+
			"	class Y extends X {	\n"+
			"		String foo() { return \"Y-foo\"; }	\n"+
			"		class Z {	\n"+
			"			Z(){	\n"+
			"				//X.this.super();	\n"+
			"			}	\n"+
			"			String foo() { return \"Z-foo\"; }	\n"+
			"			void bar () {	\n"+
			"				System.out.println(X.this.foo());	\n"+
			"			}	\n"+
			"		}	\n"+
			"	}	\n"+
			"}	\n"
		},
		"X-foo");
}

public void test084() {
	this.runConformTest(
		new String[] {
			"Foo.java",
			"public class Foo {	\n"+
			"	public static void main(String[] arguments) {	\n"+
			"		new Foo(null);	\n"+
			"	}	\n"+
			"	Foo(int i){}	\n"+
			"	Foo(Object o){		\n"+
			"		class A { 		\n"+
			"			private A() {	 	\n"+
			"			}		\n"+
			"		}		\n"+
			"		class B extends Foo {		\n"+
			"			B() {		\n"+
			"				super(0);	\n"+
			"				new A(){		\n"+
			"					void foo() { 	\n"+
			"						System.out.println(Foo.this.getClass().getName());		\n"+
			"					} 	\n"+
			"				}.foo();		\n"+
			"			}		\n"+
			"		}		\n"+
			"		new B();	\n"+
			"	}		\n"+
			"} 	\n",
		},
		"Foo");
}

public void test085() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	if (options.sourceLevel < ClassFileConstants.JDK1_5) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {	\n"+
				"	int m() {		\n"+
				"		return 1;		\n"+
				"	}		\n"+
				"	class C extends B {		\n"+
				"		C(int j){}		\n"+
				"		C(){		\n"+
				"			this(	\n"+
				"				new B(){ 	\n"+
				"					X x = X.this; 	\n"+
				"					int m(){	\n"+
				"						return 1;	\n"+
				"					}	\n"+
				"				}.m());	\n"+
				"		}		\n"+
				"	}		\n"+
				"}		\n"+
				"class B extends X {		\n"+
				"}	\n"
			},
		"----------\n" + 
		"1. WARNING in X.java (at line 10)\n" + 
		"	X x = X.this; 	\n" + 
		"	  ^\n" + 
		"The field new B(){}.x is never read locally\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 10)\n" + 
		"	X x = X.this; 	\n" + 
		"	      ^^^^^^\n" + 
		"No enclosing instance of the type X is accessible in scope\n" + 
		"----------\n");
		return;
	}
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n"+
			"	int m() {		\n"+
			"		return 1;		\n"+
			"	}		\n"+
			"	class C extends B {		\n"+
			"		C(int j){}		\n"+
			"		C(){		\n"+
			"			this(	\n"+
			"				new B(){ 	\n"+
			"					X x = X.this; 	\n"+
			"					int m(){	\n"+
			"						return 1;	\n"+
			"					}	\n"+
			"				}.m());	\n"+
			"		}		\n"+
			"	}		\n"+
			"}		\n"+
			"class B extends X {		\n"+
			"}	\n"
		},
		"");
}

public void test086() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n"+
			"	public static void main(String[] arguments) { 	\n"+
			"		new X().new C();		\n"+
			"	}		\n"+
			"	int m() {		\n"+
			"		return 1;		\n"+
			"	}		\n"+
			"	class C extends B {		\n"+
			"		C(int j){}		\n"+
			"		C(){		\n"+
			"			this(	\n"+
			"				new B(){ 	\n"+
			"					int m(){	\n"+
			"						System.out.println(\"SUCCESS\");		\n"+
			"						return 1;	\n"+
			"					}	\n"+
			"				}.m());	\n"+
			"		}		\n"+
			"	}		\n"+
			"}		\n"+
			"class B extends X {		\n"+
			"}	\n"
		},
		"SUCCESS");
}

public void test087() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	if (options.complianceLevel <= ClassFileConstants.JDK1_4) {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {	\n"+
				"	public static void main(String[] arguments) {	\n"+
				"		new X().f();	\n"+
				"	}	\n"+
				"    void f () {	\n"+
				"        class C {	\n"+
				"        	C() {	\n"+
				"        		System.out.println(\"[\"+X.this.getClass().getName()+\"]\");	\n"+
				"        	}	\n"+
				"        }	\n"+
				"        class N extends X {	\n"+
				"            { new C(); } // selects N.this, not O.this	\n"+
				"        }	\n"+
				"        new N();	\n"+
				"    }	\n"+
				"}	\n"
			},
			"[X$1$N]");
		return;
	}
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n"+
			"	public static void main(String[] arguments) {	\n"+
			"		new X().f();	\n"+
			"	}	\n"+
			"    void f () {	\n"+
			"        class C {	\n"+
			"        	C() {	\n"+
			"        		System.out.println(\"[\"+X.this.getClass().getName()+\"]\");	\n"+
			"        	}	\n"+
			"        }	\n"+
			"        class N extends X {	\n"+
			"            { new C(); } // selects N.this, not O.this	\n"+
			"        }	\n"+
			"        new N();	\n"+
			"    }	\n"+
			"}	\n"
		},
		"[X$1N]");
}

public void test088() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {	\n"+
			"	class Middle {	\n"+
			"		class Inner {	\n"+
			"		}	\n"+
			"	} 	\n"+
			"	class M extends Middle.Inner {	\n"+
			"	}	\n"+
			"}	\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 6)\n" + 
		"	class M extends Middle.Inner {	\n" + 
		"	      ^\n" + 
		"No enclosing instance of type X.Middle is accessible to invoke the super constructor. Must define a constructor and explicitly qualify its super constructor invocation with an instance of X.Middle (e.g. x.super() where x is an instance of X.Middle).\n" + 
		"----------\n");
}

public void test089() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n"+
			"	static class Middle {	\n"+
			"		static class Inner {	\n"+
			"		}	\n"+
			"	} 	\n"+
			"	class M extends Middle.Inner {	\n"+
			"	}	\n"+
			"	public static void main(String[] arguments) {	\n"+
			"		new X().new M();	\n" +
			"		System.out.println(\"SUCCESS\");	\n"+
			"	}	\n"+
			"}	\n"
		},
		"SUCCESS");
}

public void test090() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n"+
			"	void foo() {	\n"+
			"		class Middle {	\n"+
			"			class Inner {	\n"+
			"			}	\n"+
			"		} 	\n"+
			"		class M extends Middle.Inner {	\n"+
			"			M() {	\n"+
			"				new Middle().super();	\n"+
			"			}	\n"+
			"		}	\n"+
			"	}	\n"+
			"	public static void main(String[] arguments) {	\n"+
			"		new X().foo();	\n" +
			"		System.out.println(\"SUCCESS\");	\n"+
			"	}	\n"+
			"}	\n"
		},
		"SUCCESS");
}

// ensure that local member empty constructor gets implicit constructor call
public void test091() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n"+
			"	Object o = new Object() {	\n"+
			"		class Inner {	\n"+
			"			private Inner() {}	\n"+
			"		}	\n"+
			"	}.new Inner(){};	\n"+
			"	public static void main(String[] arguments) {	\n"+
			"		new X();	\n"+
			"		System.out.println(\"SUCCESS\");	\n"+
			"	}	\n"+
			"}	\n",
		},
		"SUCCESS");
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=29636
 * ensure first anonymous is X$1(extends X), last is X$2(extends A)
 */
public void test092() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n"+
			"	class A {	\n"+
			"	}	\n"+
			"	public static void main(String[] arguments) {	\n"+
			"		System.out.println(\"[\"+new X(){}.new A(){}.getClass().getName()+\"]\");	\n"+
			"	}	\n"+
			"}	\n",
		},
		"[X$2]");
}

public void test093() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {	\n"+
			"	public static void main(String[] arguments) {	\n"+
			"		System.out.println(X.this);	\n"+
			"	}	\n"+
			"}	\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	System.out.println(X.this);	\n" + 
		"	                   ^^^^^^\n" + 
		"Cannot use this in a static context \n" + 
		"----------\n");
}

public void test094() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {	\n"+
			"	class Y {}	\n"+
			"	public static void main(String[] arguments) {	\n"+
			"		int i = 0;	\n"+
			"		i.new Y();	\n"+
			"	}	\n"+
			"}	\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	i.new Y();	\n" + 
		"	^\n" + 
		"Cannot use an expression of the type int as a valid enclosing instance\n" + 
		"----------\n");
}
//http://bugs.eclipse.org/bugs/show_bug.cgi?id=30280
public void test095() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {	\n"+
			"	class Y {}	\n"+
			"	public static void main(String[] arguments) {	\n"+
			"		int i = 0;	\n"+
			"		i.new Y(){};	\n"+
			"	}	\n"+
			"}	\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	i.new Y(){};	\n" + 
		"	^\n" + 
		"Cannot use an expression of the type int as a valid enclosing instance\n" + 
		"----------\n");
}
public void test096() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {	\n"+
			"	static class Y {}	\n"+
			"	void foo() {	\n"+
			"		new X().new Y(){};	\n"+
			"	}	\n"+
			"}	\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	new X().new Y(){};	\n" + 
		"	^^^^^^^\n" + 
		"Illegal enclosing instance specification for type X.Y\n" + 
		"----------\n");
}
/**
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=35456
 */
public void test097() { 
	this.runConformTest(
		new String[] {
			"apples/Base.java",
			"package apples;	\n" +
			"public class Base {	\n"+
			"	protected String foo = \"SUCCESS\";	\n" +
			"	public static void main(String[] args){	\n"+
			"		new oranges.Derived().new Inner(new oranges.Derived()).bug();	\n"+
			"	}	\n"+
			"}	\n",
			"oranges/Derived.java",
			"package oranges;	\n"+
			"import apples.*;	\n"+
			"public class Derived extends Base {	\n"+
			"    public class Inner {	\n"+
			"        Derived c = null;	\n"+
			"        public Inner(Derived c) {	\n"+
			"            this.c = c;	\n"+
			"        } 	\n"+
			"        public void bug() {	\n"+
			"            // The following reference to Base.foo causes the 	\n"+
			"            // VerifyError	\n"+
			"            System.out.println(c.foo);	\n"+
			"        }	\n"+
			"    }	\n"+
			"}	\n",
		},
		"SUCCESS");
}

/**
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=33751
 */
public void test098() { 
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n"+
			"	public static void main(String[] args) {	\n"+
			"		System.out.println(\"first inner class = \" + new Object() {}.getClass());	\n"+
			"		if (true) {	\n"+
			"			System.out.println(\"Always true\");	\n"+
			"		} else {	\n"+
			"		System.out.println(\"unreachable inner class = \" + new Object() {}.getClass());	\n"+
			"		}	\n"+
			"		System.out.println(\"last inner class = \" + new Object() {}.getClass());	\n"+
			"	}	\n"+
			"}	\n",
		},
		"first inner class = class X$1\n" + 
		"Always true\n" + 
		"last inner class = class X$2");
}

/**
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=40240
 */
public void test099() {
	this.runConformTest(
		new String[] {
			"Y.java",
			"public class Y { \n" +
			"	public static void main(String[] args) {	\n" +
			"		class Local {} \n"+
			"		System.out.println(\"SUCCESS\");	\n" +
			"	} \n" +
			"} \n",
		},
		"SUCCESS");

	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	if (options.complianceLevel <= ClassFileConstants.JDK1_4) {	
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X { \n" +
				"    public static void main(String argv[]) { \n" +
				"		Object a = new Y$1$Local();        // compile-time error \n" +
				"    } \n" +
				"} \n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	Object a = new Y$1$Local();        // compile-time error \n" + 
			"	               ^^^^^^^^^\n" + 
			"The nested type Y$1$Local cannot be referenced using its binary name\n" + 
			"----------\n",
			null,
			false);
		return;
	}
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X { \n" +
			"    public static void main(String argv[]) { \n" +
			"		Object a = new Y$1Local();        // compile-time error \n" +
			"    } \n" +
			"} \n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	Object a = new Y$1Local();        // compile-time error \n" + 
		"	               ^^^^^^^^\n" + 
		"The nested type Y$1Local cannot be referenced using its binary name\n" + 
		"----------\n",
		null,
		false);
}

/*
 * Check that member type allocation is granted access to compatible enclosing instance available as constructor argument
 */
public void test101() {
	this.runConformTest(
		new String[] {
			"X.java",
		"public class X {\n" + 
		"	X(Object o) {\n" + 
		"	}\n" + 
		"	public static void main(String[] args) {\n" + 
		"		new X(null).new M(null);\n" + 
		"		System.out.println(\"SUCCESS\");\n" + 
		"	}\n" + 
		"	class M extends Top {\n" + // no issue if M is unrelated to X
		"		M() {\n" + 
		"			super(null);\n" + 
		"		}\n" + 
		"		M(Object o) {\n" + 
		"			super(new M(){});\n" + 
		"		}\n" + 
		"	}\n" + 
		"	class Top {\n" + 
		"		Top(Object o) {\n" + 
		"		}\n" + 
		"	}\n" + 
		"}\n",
		},
		"SUCCESS");
}

/*
 * Check that direct member type allocation is denied access to compatible enclosing instance available as constructor argument
 */
public void test102() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	X(Object o) {\n" + 
			"	}\n" + 
			"	class M extends X {\n" + 
			"		M() {\n" + 
			"			super(null); //1\n" + 
			"		}\n" + 
			"		M(Object o) {\n" + 
			"			super(new M());//2\n" + 
			"		}\n" + 
			"	}\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 9)\n" + 
		"	super(new M());//2\n" + 
		"	      ^^^^^^^\n" + 
		"No enclosing instance of type X is available due to some intermediate constructor invocation\n" + 
		"----------\n");
}


/*
 * Check that indirect member type allocation is denied access to compatible enclosing instance available as constructor argument
 */
public void test104() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	X(Object o) {\n" + 
			"	}\n" + 
			"	public static void main(String[] args) {\n" + 
			"		new X(null).new M(null);\n" + 
			"		System.out.println(\"SUCCESS\");\n" + 
			"	}\n" + 
			"	class N extends X {\n" + 
			"		N() {\n" + 
			"			super(null); //1\n" + 
			"		}\n" + 
			"		N(Object o) {\n" + 
			"			super(new M());//2\n" + 
			"		}\n" + 
			"	}\n" + 
			" 	class M extends X {\n" + 
			"		M() {\n" + 
			"			super(null); //3\n" + 
			"		}\n" + 
			"		M(Object o) {\n" + 
			"			super(new M());//4\n" + 
			"		}\n" + 
			"	}\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 13)\n" + 
		"	super(new M());//2\n" + 
		"	      ^^^^^^^\n" + 
		"No enclosing instance of type X is available due to some intermediate constructor invocation\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 21)\n" + 
		"	super(new M());//4\n" + 
		"	      ^^^^^^^\n" + 
		"No enclosing instance of type X is available due to some intermediate constructor invocation\n" + 
		"----------\n");
}

public void test107() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X { \n" + 
			"	public static class Y { \n" + 
			"		public Y(Z z) {} \n" + 
			"	} \n" + 
			"	public interface Z {} \n" + 
			"} \n" + 
			"\n" + 
			"class A { \n" + 
			"	private static class B extends X.Y implements X.Z { \n" + 
			"		B(A a) { \n" + 
			"			super(B.this); \n" + 
			"		} \n" + 
			"	} \n" + 
			"} ",
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 9)\n" + 
		"	private static class B extends X.Y implements X.Z { \n" + 
		"	                     ^\n" + 
		"The type A.B is never used locally\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 11)\n" + 
		"	super(B.this); \n" + 
		"	      ^^^^^^\n" + 
		"Cannot refer to \'this\' nor \'super\' while explicitly invoking a constructor\n" + 
		"----------\n");
}

 // javac 1.4.2 incorrectly accepts it, jikes rejects it as we do
public void test108() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	if (options.sourceLevel == ClassFileConstants.JDK1_4) {	 // 1.3 and 1.5 both accept it
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	{\n" + 
				"		class Local1 extends X {\n" + 
				"		}\n" + 
				"		class Local2 extends Local1 {\n" +
				"		}\n" + 
				"	}\n" + 
				"}",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	class Local2 extends Local1 {\n" + 
			"	      ^^^^^^\n" + 
			"No enclosing instance of type X is available due to some intermediate constructor invocation\n" + 
			"----------\n");
		return;
	}
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	{\n" + 
			"		class Local1 extends X {\n" + 
			"		}\n" + 
			"		class Local2 extends Local1 {\n" +
			"		}\n" + 
			"	}\n" + 
			"}",
		},
		"");
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44538
public void test109() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	if (options.sourceLevel == ClassFileConstants.JDK1_4) {		 // 1.3 and 1.5 both accept it
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public void bar() {\n" + 
				"		class C extends X {\n" + 
				"			public void foo() {\n" + 
				"				\n" + 
				"			}\n" + 
				"		}\n" + 
				"		X a= new X() {\n" + 
				"			public void foo() {\n" + 
				"				\n" + 
				"			}\n" + 
				"		};\n" + 
				"		class D extends C {\n" + 
				"			\n" + 
				"		};\n" + 
				"	}\n" + 
				"}"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 4)\n" + 
			"	public void foo() {\n" + 
			"	            ^^^^^\n" + 
			"The method foo() from the type C is never used locally\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 9)\n" + 
			"	public void foo() {\n" + 
			"	            ^^^^^\n" + 
			"The method foo() from the type new X(){} is never used locally\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 13)\n" + 
			"	class D extends C {\n" + 
			"	      ^\n" + 
			"No enclosing instance of type X is available due to some intermediate constructor invocation\n" + 
			"----------\n");
		return;
	}
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public void bar() {\n" + 
			"		class C extends X {\n" + 
			"			public void foo() {\n" + 
			"				\n" + 
			"			}\n" + 
			"		}\n" + 
			"		X a= new X() {\n" + 
			"			public void foo() {\n" + 
			"				\n" + 
			"			}\n" + 
			"		};\n" + 
			"		class D extends C {\n" + 
			"			\n" + 
			"		};\n" + 
			"	}\n" + 
			"}"
		},
		"");
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44715 - NPE when generating fake reachable local type
public void test110() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		if (true) {\n" + 
			"			System.out.println(\"SUCCESS\");\n" + 
			"			return;\n" + 
			"		}\n" + 
			"		class ShouldNotBeGenerated {\n" + 
			"		}\n" + 
			"	}\n" + 
			"}"
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44715 - variation with anonymous type
public void test111() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		if (true) {\n" + 
			"			System.out.println(\"SUCCESS\");\n" + 
			"			return;\n" + 
			"		}\n" + 
			"		new Object() {}; \n" + 
			"	}\n" + 
			"}"
		},
		"SUCCESS");
}
public void test112() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    private Object t;\n" + 
			"    X(Object t) {\n" + 
			"        this.t = t;\n" + 
			"    }\n" + 
			"    public static void main(String[] args) {\n" + 
			"        new X(\"OUTER\").bar();\n" + 
			"    }\n" + 
			"    void bar() {\n" + 
			"        new X(this) {\n" + 
			"            void run() {\n" + 
			"                new Object() {\n" + 
			"                    void run() {\n" + 
			"				        System.out.println(t);\n" + 
			"                    }\n" + 
			"                }.run();\n" + 
			"            }\n" + 
			"        }.run();\n" + 
			"    }\n" + 
			"}\n"
		},
		"OUTER");
}
public void test113() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    private Object t;\n" + 
			"    X(Object t) {\n" + 
			"        this.t = t;\n" + 
			"    }\n" + 
			"    public static void main(String[] args) {\n" + 
			"        new X(\"OUTER\").bar();\n" + 
			"    }\n" + 
			"    void bar() {\n" + 
			"        new X(this) {\n" + 
			"            void run() {\n" + 
			"                new Object() {\n" + 
			"                    void run() {\n" + 
			"						try {	\n" +
			"							X x = (X) t;	\n" +
			"				        } catch(ClassCastException e){ \n" +
			"							System.out.println(\"SUCCESS\");\n" + 
			"						} \n" +
			"                    }\n" + 
			"                }.run();\n" + 
			"            }\n" + 
			"        }.run();\n" + 
			"    }\n" + 
			"}\n"
		},
		"SUCCESS");
}
public void test114() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	String s;\n" + 
			"	X(String s) {\n" + 
			"		this.s = s;\n" + 
			"	}\n" + 
			"	void foo() {\n" + 
			"		class L extends X {\n" + 
			"			L() {\n" + 
			"				super(s);\n" + 
			"				System.out.println(s);		\n" + 
			"			}\n" + 
			"		}\n" + 
			"		new L();\n" + 
			"	}\n" + 
			"	public static void main(String[] args) {\n" + 
			"		new X(\"SUCCESS\").foo();		\n" + 
			"	}\n" + 
			"}\n"		
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 9)\n" + 
		"	super(s);\n" + 
		"	      ^\n" + 
		"Cannot refer to an instance field s while explicitly invoking a constructor\n" + 
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=58606
public void test115() {
	this.runConformTest(
		new String[] {
			"p2/X2.java",
			"package p2;\n" + 
			"public class X2 extends p1.X1 {\n" + 
			"    private void foo() {\n" + 
			"        new p1.X1.M1() {\n" + 
			"            public void bar() {\n" + 
			"                System.out.print(X2.this.field);\n" + 
			"                X2.this.doit();\n" + 
			"            }\n" + 
			"        }.bar();\n" + 
			"    }\n" + 
			"    public static void main(String[] args) {\n" + 
			"        X2 t2 = new X2();\n" + 
			"        t2.foo();\n" + 
			"    }\n" + 
			"}",
			"p1/X1.java",
			"package p1;\n" + 
			"public class X1 {\n" + 
			"    public abstract class M1 {\n" + 
			"        public abstract void bar();\n" + 
			"    }\n" + 
			"    protected static String field = \"SUCC\";\n" + 
			"    protected static void doit() {\n" + 
			"        System.out.println(\"ESS\");\n" + 
			"    }\n" + 
			"}",
			},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=68698
public void test116() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class Display {\n" + 
			"  public interface Bla {\n" + 
			"    void a();\n" + 
			"  }\n" + 
			"}\n" + 
			"public class X {\n" + 
			"  void aMethod() {\n" + 
			"    Display display = null;\n" + 
			"    display.new Bla() {\n" + 
			"    };\n" + 
			"  }\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 9)\n" + 
		"	display.new Bla() {\n" + 
		"	^^^^^^^\n" + 
		"Illegal enclosing instance specification for type Display.Bla\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 9)\n" + 
		"	display.new Bla() {\n" + 
		"	            ^^^^^\n" + 
		"The type new Display.Bla(){} must implement the inherited abstract method Display.Bla.a()\n" + 
		"----------\n");
}

public void test117() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	\n" + 
			"	public static void main(String[] args) {\n" + 
			"		new X().bar();\n" + 
			"	}\n" + 
			"	void bar() {\n" + 
			"		new X(){\n" + 
			"			void baz() {\n" + 
			"				new M();\n" + 
			"			}\n" + 
			"		}.baz();\n" + 
			"	}\n" + 
			"	class M {\n" + 
			"		M() {\n" + 
			"			System.out.println(\"SUCCESS\");\n" + 
			"		}\n" + 
			"	}\n" + 
			"}\n",
		},
		"SUCCESS");
}

public void test118() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	void foo() {}\n" + 
			"	class M {\n" + 
			"		M(Object o) {}\n" + 
			"		M() {\n" + 
			"			this(new Object() {\n" + 
			"				void baz() {\n" + 
			"					foo();\n" + 
			"					bar();\n" + 
			"				}\n" + 
			"			});\n" + 
			"			new Object() {\n" + 
			"				void baz() {\n" + 
			"					foo();\n" + 
			"					bar();\n" + 
			"				}\n" + 
			"			};\n" + 
			"		}\n" + 
			"		void bar() {}\n" + 
			"		void baz() {\n" + 
			"			new Object() {\n" + 
			"				void baz() {\n" + 
			"					foo();\n" + 
			"					bar();\n" + 
			"				}\n" + 
			"			};\n" + 
			"		}\n" + 
			"	}\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 9)\n" + 
		"	bar();\n" + 
		"	^^^\n" + 
		"Cannot refer to an instance method while explicitly invoking a constructor\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 22)\n" + 
		"	void baz() {\n" + 
		"	     ^^^^^\n" + 
		"The method baz() from the type new Object(){} is never used locally\n" + 
		"----------\n");
}
public void test119() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	if (options.sourceLevel < ClassFileConstants.JDK1_5) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		new X().new M();\n" + 
				"	}\n" + 
				"	void foo(String s) { System.out.print(\"<foo:\"+s+\">\"); }\n" + 
				"	class M {\n" + 
				"		M(Runnable r) { r.run(); }\n" + 
				"		M() {\n" + 
				"			this(new Runnable() {\n" + 
				"				public void run() {\n" + 
				"					foo(\"0\");\n" + 
				"					new Object() {\n" + 
				"						void baz() {\n" + 
				"//							foo(\"1\");\n" + 
				"						}\n" + 
				"					};\n" + 
				"					class Local {\n" + 
				"						void baz() {\n" + 
				"//							foo(\"2\");\n" + 
				"						}\n" + 
				"					}				\n" + 
				"					new Local();\n" + 
				"				}\n" + 
				"			});\n" + 
				"			new Object() {\n" + 
				"				void baz() {\n" + 
				"					foo(\"3\");\n" + 
				"					bar(\"3\");\n" + 
				"				}\n" + 
				"			}.baz();\n" + 
				"		}\n" + 
				"		void bar(String s) { System.out.print(\"<bar:\"+s+\">\"); }\n" + 
				"		void baz() {\n" + 
				"			new Object() {\n" + 
				"				void baz() {\n" + 
				"					foo(\"4\");\n" + 
				"					bar(\"4\");\n" + 
				"				}\n" + 
				"			};\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 11)\n" + 
			"	foo(\"0\");\n" + 
			"	^^^^^^^^\n" + 
			"No enclosing instance of the type X is accessible in scope\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 13)\n" + 
			"	void baz() {\n" + 
			"	     ^^^^^\n" + 
			"The method baz() from the type new Object(){} is never used locally\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 18)\n" + 
			"	void baz() {\n" + 
			"	     ^^^^^\n" + 
			"The method baz() from the type Local is never used locally\n" + 
			"----------\n" + 
			"4. WARNING in X.java (at line 35)\n" + 
			"	void baz() {\n" + 
			"	     ^^^^^\n" + 
			"The method baz() from the type new Object(){} is never used locally\n" + 
			"----------\n");
		return;
	}
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		new X().new M();\n" + 
			"	}\n" + 
			"	void foo(String s) { System.out.print(\"<foo:\"+s+\">\"); }\n" + 
			"	class M {\n" + 
			"		M(Runnable r) { r.run(); }\n" + 
			"		M() {\n" + 
			"			this(new Runnable() {\n" + 
			"				public void run() {\n" + 
			"					foo(\"0\");\n" + 
			"					new Object() {\n" + 
			"						void baz() {\n" + 
			"//							foo(\"1\");\n" + 
			"						}\n" + 
			"					};\n" + 
			"					class Local {\n" + 
			"						void baz() {\n" + 
			"//							foo(\"2\");\n" + 
			"						}\n" + 
			"					}				\n" + 
			"					new Local();\n" + 
			"				}\n" + 
			"			});\n" + 
			"			new Object() {\n" + 
			"				void baz() {\n" + 
			"					foo(\"3\");\n" + 
			"					bar(\"3\");\n" + 
			"				}\n" + 
			"			}.baz();\n" + 
			"		}\n" + 
			"		void bar(String s) { System.out.print(\"<bar:\"+s+\">\"); }\n" + 
			"		void baz() {\n" + 
			"			new Object() {\n" + 
			"				void baz() {\n" + 
			"					foo(\"4\");\n" + 
			"					bar(\"4\");\n" + 
			"				}\n" + 
			"			};\n" + 
			"		}\n" + 
			"	}\n" + 
			"}\n",
		},
		"<foo:0><foo:3><bar:3>");
}
public void test120() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	if (options.sourceLevel < ClassFileConstants.JDK1_5) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	void foo() {}\n" + 
				"	class M {\n" + 
				"		M(Object o) {}\n" + 
				"		M() {\n" + 
				"			this(new Object() {\n" + 
				"				void baz() {\n" + 
				"					new Object() {\n" + 
				"						void baz() {\n" + 
				"							foo(); //0\n" + 
				"						}\n" + 
				"					};\n" + 
				"					class Local {\n" + 
				"						void baz() {\n" + 
				"							foo(); //1\n" + 
				"						}\n" + 
				"					}\n" + 
				"					new Local();\n" + 
				"					foo();//2\n" + 
				"				}\n" + 
				"			});\n" + 
				"			new Object() {\n" + 
				"				void baz() {\n" + 
				"					foo();//3\n" + 
				"					bar();\n" + 
				"				}\n" + 
				"			};\n" + 
				"		}\n" + 
				"		void bar() {}\n" + 
				"		void baz() {\n" + 
				"			new Object() {\n" + 
				"				void baz() {\n" + 
				"					foo();//4\n" + 
				"					bar();\n" + 
				"				}\n" + 
				"			};\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n",
			},
		"----------\n" + 
		"1. WARNING in X.java (at line 7)\n" + 
		"	void baz() {\n" + 
		"	     ^^^^^\n" + 
		"The method baz() from the type new Object(){} is never used locally\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 9)\n" + 
		"	void baz() {\n" + 
		"	     ^^^^^\n" + 
		"The method baz() from the type new Object(){} is never used locally\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 10)\n" + 
		"	foo(); //0\n" + 
		"	^^^^^\n" + 
		"No enclosing instance of the type X is accessible in scope\n" + 
		"----------\n" + 
		"4. WARNING in X.java (at line 14)\n" + 
		"	void baz() {\n" + 
		"	     ^^^^^\n" + 
		"The method baz() from the type Local is never used locally\n" + 
		"----------\n" + 
		"5. ERROR in X.java (at line 15)\n" + 
		"	foo(); //1\n" + 
		"	^^^^^\n" + 
		"No enclosing instance of the type X is accessible in scope\n" + 
		"----------\n" + 
		"6. ERROR in X.java (at line 19)\n" + 
		"	foo();//2\n" + 
		"	^^^^^\n" + 
		"No enclosing instance of the type X is accessible in scope\n" + 
		"----------\n" + 
		"7. WARNING in X.java (at line 23)\n" + 
		"	void baz() {\n" + 
		"	     ^^^^^\n" + 
		"The method baz() from the type new Object(){} is never used locally\n" + 
		"----------\n" + 
		"8. WARNING in X.java (at line 32)\n" + 
		"	void baz() {\n" + 
		"	     ^^^^^\n" + 
		"The method baz() from the type new Object(){} is never used locally\n" + 
		"----------\n");
		return;
	}
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	void foo() {}\n" + 
			"	class M {\n" + 
			"		M(Object o) {}\n" + 
			"		M() {\n" + 
			"			this(new Object() {\n" + 
			"				void baz() {\n" + 
			"					class Local {\n" + 
			"						void baz() {\n" + 
			"							foo(); //1\n" + 
			"						}\n" + 
			"					}\n" + 
			"					new Local();\n" + 
			"					foo();//2\n" + 
			"				}\n" + 
			"			});\n" + 
			"			new Object() {\n" + 
			"				void baz() {\n" + 
			"					foo();//3\n" + 
			"					bar();\n" + 
			"				}\n" + 
			"			};\n" + 
			"		}\n" + 
			"		void bar() {}\n" + 
			"		void baz() {\n" + 
			"			new Object() {\n" + 
			"				void baz() {\n" + 
			"					foo();//4\n" + 
			"					bar();\n" + 
			"				}\n" + 
			"			};\n" + 
			"		}\n" + 
			"	}\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 7)\n" + 
		"	void baz() {\n" + 
		"	     ^^^^^\n" + 
		"The method baz() from the type new Object(){} is never used locally\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 9)\n" + 
		"	void baz() {\n" + 
		"	     ^^^^^\n" + 
		"The method baz() from the type Local is never used locally\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 10)\n" + 
		"	foo(); //1\n" + 
		"	^^^^^\n" + 
		"No enclosing instance of the type X is accessible in scope\n" + 
		"----------\n" + 
		"4. WARNING in X.java (at line 18)\n" + 
		"	void baz() {\n" + 
		"	     ^^^^^\n" + 
		"The method baz() from the type new Object(){} is never used locally\n" + 
		"----------\n" + 
		"5. WARNING in X.java (at line 27)\n" + 
		"	void baz() {\n" + 
		"	     ^^^^^\n" + 
		"The method baz() from the type new Object(){} is never used locally\n" + 
		"----------\n");
}
public void test121() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	X(Object o) {\n" + 
			"	}\n" + 
			"	class M extends X {\n" + 
			"		M() {\n" + 
			"			super(null); //1\n" + 
			"		}\n" + 
			"		M(Object o) {\n" + 
			"			super(new X(null){});//2\n" + 
			"		}\n" + 
			"	}\n" + 
			"}\n",
		},
		"");
}
public void _test122() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	if (options.sourceLevel < ClassFileConstants.JDK1_5) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		new X().new M();\n" + 
				"	}\n" + 
				"	void foo(String s) { System.out.print(\"<foo:\"+s+\">\"); }\n" + 
				"	class M {\n" + 
				"		M(Runnable r) { r.run(); }\n" + 
				"		M() {\n" + 
				"			this(new Runnable() {\n" + 
				"				{\n" + 
				"					foo(\"0\");\n" + 
				"					bar(\"0\");\n" + 
				"				}\n" + 
				"				public void run() {\n" + 
				"				}\n" + 
				"			});\n" + 
				"		}\n" + 
				"		void bar(String s) { System.out.print(\"<bar:\"+s+\">\"); }\n" + 
				"	}\n" + 
				"}\n",
			},
			"----------\n" + 
			"cannot access foo(0)\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 12)\n" + 
			"	bar(\"0\");\n" + 
			"	^^^\n" + 
			"Cannot refer to an instance method while explicitly invoking a constructor\n" + 
			"----------\n");
		return;
	}
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		new X().new M();\n" + 
			"	}\n" + 
			"	void foo(String s) { System.out.print(\"<foo:\"+s+\">\"); }\n" + 
			"	class M {\n" + 
			"		M(Runnable r) { r.run(); }\n" + 
			"		M() {\n" + 
			"			this(new Runnable() {\n" + 
			"				{\n" + 
			"					foo(\"0\");\n" + 
			"					bar(\"0\");\n" + 
			"				}\n" + 
			"				public void run() {\n" + 
			"				}\n" + 
			"			});\n" + 
			"		}\n" + 
			"		void bar(String s) { System.out.print(\"<bar:\"+s+\">\"); }\n" + 
			"	}\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 12)\n" + 
		"	bar(\"0\");\n" + 
		"	^^^\n" + 
		"Cannot refer to an instance method while explicitly invoking a constructor\n" + 
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=110182 - variation
public void test123() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class Y {\n" + 
			"	public static final boolean b = false;\n" + 
			"}\n" + 
			"public class X {\n" + 
			"    private static Y y = new Y(); \n" + 
			"    private static Object o = new Object(); \n" + 
			"\n" + 
			"	static class Z {\n" + 
			"		Z() {\n" + 
			"	    	if (y.b) {\n" + 
			"	    		System.out.println(\"dead code\");\n" + 
			"	    	}\n" + 
			"		}\n" + 
			"		public int bar() {\n" + 
			"	    	if (y.b) {\n" + 
			"	    		System.out.println(\"dead code\");\n" + 
			"	    	}\n" + 
			"    		System.out.println(\"bar\");\n" + 
			"			return 0;\n" + 
			"		}\n" + 
			"	}\n" + 
			"    static int foo() {\n" + 
			"    	synchronized(o) { \n" + 
			"	    	Z z = new Z();\n" + 
			"    		return z.bar();\n" + 
			"    	}\n" + 
			"    }\n" + 
			"    \n" + 
			"    public static void main(String[] args) {\n" + 
			"    	foo();\n" + 
			"    }\n" + 
			"}\n",
		},
		"bar");
	// ensure synthetic access method got generated for enclosing field
	String expectedOutput =
		"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 1, Locals: 1\n" + 
			"  X$Z();\n" + 
			"    0  aload_0 [this]\n" + 
			"    1  invokespecial java.lang.Object() [8]\n" + 
			"    4  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 9]\n" + 
			"        [pc: 4, line: 13]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 5] local: this index: 0 type: X.Z\n" + 
			"  \n" + 
			"  // Method descriptor #15 ()I\n" + 
			"  // Stack: 2, Locals: 1\n" + 
			"  public int bar();\n" + 
			"     0  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"     3  ldc <String \"bar\"> [22]\n" + 
			"     5  invokevirtual java.io.PrintStream.println(java.lang.String) : void [23]\n" + 
			"     8  iconst_0\n" + 
			"     9  ireturn\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 18]\n" + 
			"        [pc: 8, line: 19]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 10] local: this index: 0 type: X.Z\n" + 
			"\n" + 
			"  Inner classes:\n" + 
			"    [inner class info: #1 X$Z, outer class info: #32 X\n" + 
			"     inner name: #34 Z, accessflags: 8 static ]\n" + 
			"}";
	
	try {
		File f = new File(OUTPUT_DIR + File.separator + "X$Z.class");
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
		int index = result.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(result, 3));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, result);
		}
	} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
		assertTrue(false);
	} catch (IOException e) {
		assertTrue(false);
	}		
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=77473
public void test124() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    public static void main(String[] args) throws Exception {\n" + 
			"        Foo foo = new Foo();\n" + 
			"        try {\n" + 
			"	        foo.frob(Baz.class);\n" + 
			"        	System.out.println(\"FAILED\");\n" + 
			"        } catch(IllegalAccessException e){\n" + 
			"        	System.out.println(\"SUCCESS\");\n" + 
			"        }\n" + 
			"    }\n" + 
			"    private static class Baz {\n" + 
			"    }\n" + 
			"}\n" + 
			"class Foo {\n" + 
			"    public void frob(Class cls) throws Exception {\n" + 
			"        Object o = cls.newInstance();\n" + 
			"    }\n" + 
			"}\n",
		},
		"SUCCESS");
	// ensure synthetic access method got generated for enclosing field
	String expectedOutput =
		"  // Method descriptor #6 ()V\n" + 
		"  // Stack: 1, Locals: 1\n" + 
		"  private X$Baz();\n" + 
		"    0  aload_0 [this]\n" + 
		"    1  invokespecial java.lang.Object() [8]\n" + 
		"    4  return\n" + 
		"      Line numbers:\n" + 
		"        [pc: 0, line: 11]\n" + 
		"      Local variable table:\n" + 
		"        [pc: 0, pc: 5] local: this index: 0 type: X.Baz\n";
	
	try {
		File f = new File(OUTPUT_DIR + File.separator + "X$Baz.class");
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
		int index = result.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(result, 3));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, result);
		}
	} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
		assertTrue(false);
	} catch (IOException e) {
		assertTrue(false);
	}		
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=77473 - variation
public void test125() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	\n" + 
			"	void foo(final String s) {\n" + 
			"		class Local {\n" + 
			"			private Local() {}\n" + 
			"				void bar() {\n" + 
			"					System.out.println(s);\n" + 
			"				}\n" + 
			"		}\n" + 
			"		new Local().bar();\n" + 
			"	}\n" + 
			"	public static void main(String[] args) {\n" + 
			"		new X().foo(\"SUCCESS\");\n" + 
			"	}\n" + 
			"}\n",
		},
		"SUCCESS");
	// check private constructor outcome (if >= 1.4 modifier change, if 1.3 synthetic emulation)
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	String expectedOutput = options.complianceLevel <= ClassFileConstants.JDK1_3
		? 	"private class X$1$Local {\n" + 
			"  \n" + 
			"  // Field descriptor #6 LX;\n" + 
			"  final synthetic X this$0;\n" + 
			"  \n" + 
			"  // Field descriptor #9 Ljava/lang/String;\n" + 
			"  private final synthetic java.lang.String val$s;\n" + 
			"  \n" + 
			"  // Method descriptor #11 (LX;Ljava/lang/String;)V\n" + 
			"  // Stack: 2, Locals: 3\n" + 
			"  private X$1$Local(X arg0, java.lang.String arg1);\n" + 
			"     0  aload_0 [this]\n" + 
			"     1  invokespecial java.lang.Object() [13]\n" + 
			"     4  aload_0 [this]\n" + 
			"     5  aload_1\n" + 
			"     6  putfield X$1$Local.this$0 : X [16]\n" + 
			"     9  aload_0 [this]\n" + 
			"    10  aload_2\n" + 
			"    11  putfield X$1$Local.val$s : java.lang.String [18]\n" + 
			"    14  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 5]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 15] local: this index: 0 type: X.1.Local\n" + 
			"  \n" + 
			"  // Method descriptor #15 ()V\n" + 
			"  // Stack: 2, Locals: 1\n" + 
			"  void bar();\n" + 
			"     0  getstatic java.lang.System.out : java.io.PrintStream [25]\n" + 
			"     3  aload_0 [this]\n" + 
			"     4  getfield X$1$Local.val$s : java.lang.String [18]\n" + 
			"     7  invokevirtual java.io.PrintStream.println(java.lang.String) : void [31]\n" + 
			"    10  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 7]\n" + 
			"        [pc: 10, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 11] local: this index: 0 type: X.1.Local\n" + 
			"  \n" + 
			"  // Method descriptor #37 (LX;Ljava/lang/String;LX$1$Local;)V\n" + 
			"  // Stack: 3, Locals: 4\n" + 
			"  synthetic X$1$Local(X arg0, java.lang.String arg1, X.1.Local arg2);\n" + 
			"    0  aload_0\n" + 
			"    1  aload_1\n" + 
			"    2  aload_2\n" + 
			"    3  invokespecial X$1$Local(X, java.lang.String) [38]\n" + 
			"    6  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 5]\n" + 
			"\n" + 
			"  Inner classes:\n" + 
			"    [inner class info: #1 X$1$Local, outer class info: #0\n" + 
			"     inner name: #43 Local, accessflags: 2 private ]\n"
		: options.complianceLevel == ClassFileConstants.JDK1_4
			?  	"private class X$1$Local {\n" + 
				"  \n" + 
				"  // Field descriptor #6 LX;\n" + 
				"  final synthetic X this$0;\n" + 
				"  \n" + 
				"  // Field descriptor #9 Ljava/lang/String;\n" + 
				"  private final synthetic java.lang.String val$s;\n" + 
				"  \n" + 
				"  // Method descriptor #11 (LX;Ljava/lang/String;)V\n" + 
				"  // Stack: 2, Locals: 3\n" + 
				"  X$1$Local(X arg0, java.lang.String arg1);\n" + 
				"     0  aload_0 [this]\n" + 
				"     1  aload_1\n" + 
				"     2  putfield X$1$Local.this$0 : X [13]\n" + 
				"     5  aload_0 [this]\n" + 
				"     6  aload_2\n" + 
				"     7  putfield X$1$Local.val$s : java.lang.String [15]\n" + 
				"    10  aload_0 [this]\n" + 
				"    11  invokespecial java.lang.Object() [17]\n" + 
				"    14  return\n" + 
				"      Line numbers:\n" + 
				"        [pc: 0, line: 5]\n" + 
				"      Local variable table:\n" + 
				"        [pc: 0, pc: 15] local: this index: 0 type: X.1.Local\n" + 
				"  \n" + 
				"  // Method descriptor #19 ()V\n" + 
				"  // Stack: 2, Locals: 1\n" + 
				"  void bar();\n" + 
				"     0  getstatic java.lang.System.out : java.io.PrintStream [25]\n" + 
				"     3  aload_0 [this]\n" + 
				"     4  getfield X$1$Local.val$s : java.lang.String [15]\n" + 
				"     7  invokevirtual java.io.PrintStream.println(java.lang.String) : void [31]\n" + 
				"    10  return\n" + 
				"      Line numbers:\n" + 
				"        [pc: 0, line: 7]\n" + 
				"        [pc: 10, line: 8]\n" + 
				"      Local variable table:\n" + 
				"        [pc: 0, pc: 11] local: this index: 0 type: X.1.Local\n" + 
				"\n" + 
				"  Inner classes:\n" + 
				"    [inner class info: #1 X$1$Local, outer class info: #0\n" + 
				"     inner name: #40 Local, accessflags: 2 private ]\n"
			:	"private class X$1Local {\n" + 
				"  \n" + 
				"  // Field descriptor #6 LX;\n" + 
				"  final synthetic X this$0;\n" + 
				"  \n" + 
				"  // Field descriptor #8 Ljava/lang/String;\n" + 
				"  private final synthetic java.lang.String val$s;\n" + 
				"  \n" + 
				"  // Method descriptor #10 (LX;Ljava/lang/String;)V\n" + 
				"  // Stack: 2, Locals: 3\n" + 
				"  X$1Local(X arg0, java.lang.String arg1);\n" + 
				"     0  aload_0 [this]\n" + 
				"     1  aload_1\n" + 
				"     2  putfield X$1Local.this$0 : X [12]\n" + 
				"     5  aload_0 [this]\n" + 
				"     6  aload_2\n" + 
				"     7  putfield X$1Local.val$s : java.lang.String [14]\n" + 
				"    10  aload_0 [this]\n" + 
				"    11  invokespecial java.lang.Object() [16]\n" + 
				"    14  return\n" + 
				"      Line numbers:\n" + 
				"        [pc: 0, line: 5]\n" + 
				"      Local variable table:\n" + 
				"        [pc: 0, pc: 15] local: this index: 0 type: X.1Local\n" + 
				"  \n" + 
				"  // Method descriptor #18 ()V\n" + 
				"  // Stack: 2, Locals: 1\n" + 
				"  void bar();\n" + 
				"     0  getstatic java.lang.System.out : java.io.PrintStream [24]\n" + 
				"     3  aload_0 [this]\n" + 
				"     4  getfield X$1Local.val$s : java.lang.String [14]\n" + 
				"     7  invokevirtual java.io.PrintStream.println(java.lang.String) : void [30]\n" + 
				"    10  return\n" + 
				"      Line numbers:\n" + 
				"        [pc: 0, line: 7]\n" + 
				"        [pc: 10, line: 8]\n" + 
				"      Local variable table:\n" + 
				"        [pc: 0, pc: 11] local: this index: 0 type: X.1Local\n" + 
				"\n" + 
				"  Inner classes:\n" + 
				"    [inner class info: #1 X$1Local, outer class info: #0\n" + 
				"     inner name: #39 Local, accessflags: 2 private ]\n" + 
				"  Enclosing Method: #41  #43 X.foo(Ljava/lang/String;)V\n";
	
	try {
		File f = new File(OUTPUT_DIR + File.separator + (options.complianceLevel >= ClassFileConstants.JDK1_5 ? "X$1Local.class" : "X$1$Local.class"));
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
		int index = result.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(result, 3));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, result);
		}
	} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
		assertTrue(false);
	} catch (IOException e) {
		assertTrue(false);
	}		
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130117
public void test126() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	if (options.sourceLevel < ClassFileConstants.JDK1_5) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public interface X {\n" + 
				"        private class Inner {}\n" + 
				"        private interface IInner {}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	private class Inner {}\n" + 
			"	              ^^^^^\n" + 
			"The interface member type Inner can only be public\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	private interface IInner {}\n" + 
			"	                  ^^^^^^\n" + 
			"The interface member type IInner can only be public\n" + 
			"----------\n");
		return;
	}
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public interface X {\n" + 
			"        private class Inner {}\n" + 
			"        private interface IInner {}\n" + 
			"        private enum EInner {}\n" + 
			"        private @interface AInner {}\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	private class Inner {}\n" + 
		"	              ^^^^^\n" + 
		"The interface member type Inner can only be public\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	private interface IInner {}\n" + 
		"	                  ^^^^^^\n" + 
		"The interface member type IInner can only be public\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 4)\n" + 
		"	private enum EInner {}\n" + 
		"	             ^^^^^^\n" + 
		"The interface member type EInner can only be public\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 5)\n" + 
		"	private @interface AInner {}\n" + 
		"	                   ^^^^^^\n" + 
		"The interface member type AInner can only be public\n" + 
		"----------\n");
}
public static Class testClass() {
	return InnerEmulationTest.class;
}
}
