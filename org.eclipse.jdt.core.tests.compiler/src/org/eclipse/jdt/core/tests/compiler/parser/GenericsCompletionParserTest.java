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
package org.eclipse.jdt.core.tests.compiler.parser;

public class GenericsCompletionParserTest extends AbstractCompletionTest {
public GenericsCompletionParserTest(String testName) {
	super(testName);
}

public void test0001(){
	String str =
		"public class X  <T extends Z<Y>. {\n" +
		"}";


	String completeBehind = "Z<Y>.";
	int cursorLocation = str.indexOf("Z<Y>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z<Y>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Z<Y>.";
	String expectedUnitDisplayString =
		"public class X<T extends <CompleteOnType:Z<Y>.>> {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0002(){
	String str =
		"public class X  <T extends Z<Y>.W {\n" +
		"}";


	String completeBehind = "Z<Y>.W";
	int cursorLocation = str.indexOf("Z<Y>.W") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z<Y>.W>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "W";
	String expectedReplacedSource = "Z<Y>.W";
	String expectedUnitDisplayString =
		"public class X<T extends <CompleteOnType:Z<Y>.W>> {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0003(){
	String str =
		"public class Test<T extends test0001.X<Y>.Z> {\n" +
		"}";


	String completeBehind = "X<Y>.Z";
	int cursorLocation = str.indexOf("X<Y>.Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:test0001.X<Y>.Z>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "test0001.X<Y>.Z";
	String expectedUnitDisplayString =
		"public class Test<T extends <CompleteOnType:test0001.X<Y>.Z>> {\n" + 
		"  public Test() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0004(){
	String str =
		"public class X {\n" +
		"  public Y<Z>.\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  <CompleteOnType:Y<Z>.>;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0005(){
	String str =
		"public class X {\n" +
		"  public Y<Z>. foo\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  <CompleteOnType:Y<Z>.>;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0006(){
	String str =
		"public class X {\n" +
		"  public Y<Z>. foo;\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  <CompleteOnType:Y<Z>.>;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0007(){
	String str =
		"public class X {\n" +
		"  public Y<Z>. foo()\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  public <CompleteOnType:Y<Z>.> foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0008(){
	String str =
		"public class X {\n" +
		"  public Y<Z>. foo(){}\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  public <CompleteOnType:Y<Z>.> foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0009(){
	String str =
		"public class X {\n" +
		"  public Y<Z>.V<W>.\n" + 
		"}";


	String completeBehind = "Y<Z>.V<W>.";
	int cursorLocation = str.indexOf("Y<Z>.V<W>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.V<W>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.V<W>.";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  <CompleteOnType:Y<Z>.V<W>.>;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0010(){
	String str =
		"public class X {\n" +
		"  public Y<Z>.V<W>. foo\n" + 
		"}";


	String completeBehind = "Y<Z>.V<W>.";
	int cursorLocation = str.indexOf("Y<Z>.V<W>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.V<W>.>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.V<W>.";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  <CompleteOnType:Y<Z>.V<W>.>;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0011(){
	String str =
		"public class X {\n" +
		"  public Y<Z>.V<W>. foo;\n" + 
		"}";


	String completeBehind = "Y<Z>.V<W>.";
	int cursorLocation = str.indexOf("Y<Z>.V<W>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.V<W>.>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.V<W>.";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  <CompleteOnType:Y<Z>.V<W>.>;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0012(){
	String str =
		"public class X {\n" +
		"  public Y<Z>.V<W>. foo()\n" + 
		"}";


	String completeBehind = "Y<Z>.V<W>.";
	int cursorLocation = str.indexOf("Y<Z>.V<W>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.V<W>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.V<W>.";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  public <CompleteOnType:Y<Z>.V<W>.> foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0013(){
	String str =
		"public class X {\n" +
		"  public Y<Z>.V<W>. foo(){}\n" + 
		"}";


	String completeBehind = "Y<Z>.V<W>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.V<W>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.V<W>.";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  public <CompleteOnType:Y<Z>.V<W>.> foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0014(){
	String str =
		"public class X extends  Y<Z>. {\n" +
		"  \n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnClass:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X extends <CompleteOnClass:Y<Z>.> {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0015(){
	String str =
		"public class X implements I1, Y<Z>. {\n" +
		"  \n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnInterface:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X implements I1, <CompleteOnInterface:Y<Z>.> {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0016(){
	String str =
		"public class X {\n" +
		"  void foo(Y<Z>.){\n" + 
		"  \n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  <CompleteOnType:Y<Z>.>;\n" + 
		"  {\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0017(){
	String str =
		"public class X {\n" +
		"  void foo(Y<Z>. bar){\n" + 
		"  \n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo(<CompleteOnType:Y<Z>.> bar) {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0018(){
	String str =
		"public class X {\n" +
		"  Y<Z>. foo(){\n" + 
		"  \n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  <CompleteOnType:Y<Z>.> foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0019(){
	String str =
		"public class X  {\n" +
		"  void foo() throws Y<Z>. {\n" + 
		"  \n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnException:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() throws <CompleteOnException:Y<Z>.> {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0020(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z>.> void foo(){\n" + 
		"  \n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  <T extends <CompleteOnType:Y<Z>.>>void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0021(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z>.> void foo(\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  <T extends <CompleteOnType:Y<Z>.>>void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0022(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z>.> int foo\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  <T extends <CompleteOnType:Y<Z>.>>\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0023(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z>.> X\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  <T extends <CompleteOnType:Y<Z>.>>\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0024(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z>.>\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  <T extends <CompleteOnType:Y<Z>.>>\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0025(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z>. void foo(){\n" + 
		"  \n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  <T extends <CompleteOnType:Y<Z>.>>\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0026(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z>. void foo(\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  <T extends <CompleteOnType:Y<Z>.>>\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0027(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z>. int foo\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  int foo;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  <T extends <CompleteOnType:Y<Z>.>>\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0028(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z>. X\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  <T extends <CompleteOnType:Y<Z>.>>\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0029(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z>.\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  <T extends <CompleteOnType:Y<Z>.>>\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0030(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    Y<Z>.\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "";
	expectedReplacedSource = "Y<Z>.";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    <CompleteOnType:Y<Z>.>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0031(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    Y<Z>. var\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "";
	expectedReplacedSource = "Y<Z>.";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    <CompleteOnType:Y<Z>.>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0032(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    Y<Z>.W\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z>.W";
	int cursorLocation = str.indexOf("Y<Z>.W") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.W>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "W";
	expectedReplacedSource = "Y<Z>.W";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    <CompleteOnType:Y<Z>.W>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0033(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    Y<Z>.W var\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z>.W";
	int cursorLocation = str.indexOf("Y<Z>.W") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.W>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "W";
	expectedReplacedSource = "Y<Z>.W";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    <CompleteOnType:Y<Z>.W>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0034(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    this.<Y, Y<Z>.>bar();\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "";
	expectedReplacedSource = "Y<Z>.";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    <CompleteOnType:Y<Z>.>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0035(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    this.<Y, Y<Z>.>bar(\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "";
	expectedReplacedSource = "Y<Z>.";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    <CompleteOnType:Y<Z>.>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0036(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    this.<Y, Y<Z>.>bar\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "";
	expectedReplacedSource = "Y<Z>.";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    <CompleteOnType:Y<Z>.>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0037(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    this.<Y, Y<Z>.>\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "";
	expectedReplacedSource = "Y<Z>.";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    <CompleteOnType:Y<Z>.>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0038(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    this.<Y, Y<Z>.\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "";
	expectedReplacedSource = "Y<Z>.";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    <CompleteOnType:Y<Z>.>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0039(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    new <Y, Y<Z>.>X();\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "";
	expectedReplacedSource = "Y<Z>.";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    <CompleteOnType:Y<Z>.>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0040(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    aaa.new <Y, Y<Z>.>X();\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "";
	expectedReplacedSource = "Y<Z>.";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    <CompleteOnType:Y<Z>.>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0041(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    new V().new <Y, Y<Z>.>X();\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "";
	expectedReplacedSource = "Y<Z>.";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    <CompleteOnType:Y<Z>.>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0042(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    for(Y<Z>. var;;){}\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "";
	expectedReplacedSource = "Y<Z>.";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    <CompleteOnType:Y<Z>.>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0043(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    for(Y<Z>.\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "";
	expectedReplacedSource = "Y<Z>.";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    <CompleteOnType:Y<Z>.>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0044(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    try {\n" + 
		"    } catch(Y<Z>. e) {\n" +
		"   }\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnException:Y<Z>.>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "";
	expectedReplacedSource = "Y<Z>.";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    <CompleteOnException:Y<Z>.>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0045(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    try {\n" + 
		"    } catch(Y<Z>. e\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnException:Y<Z>.>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "";
	expectedReplacedSource = "Y<Z>.";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    <CompleteOnException:Y<Z>.>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0046(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    catch(Y<Z>. e\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnException:Y<Z>.>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "";
	expectedReplacedSource = "Y<Z>.";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    <CompleteOnException:Y<Z>.>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0047(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    Object a = (Y<Z>.) e;\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "";
	expectedReplacedSource = "Y<Z>.";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object a = <CompleteOnType:Y<Z>.>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0048(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    Object a = (Y<Z>.) e;\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "";
	expectedReplacedSource = "Y<Z>.";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object a = <CompleteOnType:Y<Z>.>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0049(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    (Y<Z>.) e;\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "";
	expectedReplacedSource = "Y<Z>.";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    <CompleteOnType:Y<Z>.>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0050(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    Object[] o = new Y<Z>.[0];\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<NONE>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "";
	expectedReplacedSource = "<NONE>";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0051(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    Object[] o = new Y<Z>.\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<NONE>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "";
	expectedReplacedSource = "<NONE>";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0052(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    new Y<Z>.\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<NONE>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "";
	expectedReplacedSource = "<NONE>";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0053(){
	String str =
		"public class X {\n" +
		"  public X() {\n" + 
		"    <Y<Z>.>super();\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "";
	expectedReplacedSource = "Y<Z>.";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"    super();\n" + 
		"    <CompleteOnType:Y<Z>.>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0054(){
	String str =
		"public class X {\n" +
		"  public X() {\n" + 
		"    aaa.<Y<Z>.>super();\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "";
	expectedReplacedSource = "Y<Z>.";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"    super();\n" + 
		"    <CompleteOnType:Y<Z>.>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0055(){
	String str =
		"public class X {\n" +
		"  public X() {\n" + 
		"    A.this.<Y<Z>.>super();\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "";
	expectedReplacedSource = "Y<Z>.";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"    super();\n" + 
		"    <CompleteOnType:Y<Z>.>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}

public void test0056(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    Y<Z\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0057(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    Y<V,Z\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<V,Z";
	int cursorLocation = str.indexOf("Y<V,Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<V, <CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Y<V, <CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0058(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    W<U>.Y<V,Z\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<V,Z";
	int cursorLocation = str.indexOf("Y<V,Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "W<U>.Y<V, <CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    W<U>.Y<V, <CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0059(){
	String str =
		"public class X  <T extends Z<Y {\n" +
		"}";


	String completeBehind = "Z<Y";
	int cursorLocation = str.indexOf("Z<Y") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y>";
	String expectedParentNodeToString = "Z<<CompleteOnType:Y>>";
	String completionIdentifier = "Y";
	String expectedReplacedSource = "Y";
	String expectedUnitDisplayString =
		"public class X<T> {\n" + 
		"  Z<<CompleteOnType:Y>>;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0060(){
	String str =
		"public class X {\n" +
		"  public Y<Z\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  Y<<CompleteOnType:Z>>;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0061(){
	String str =
		"public class X {\n" +
		"  public Y<Z>\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  Y<<CompleteOnType:Z>>;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0062(){
	String str =
		"public class X {\n" +
		"  public Y<Z> var\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
	"  public Y<<CompleteOnType:Z>> var;\n" + 
	"  public X() {\n" + 
	"  }\n" + 
	"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0063(){
	String str =
		"public class X {\n" +
		"  public Y<Z> var;\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public Y<<CompleteOnType:Z>> var;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0064(){
	String str =
		"public class X {\n" +
		"  public Y<Z foo()\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  Y<<CompleteOnType:Z>>;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0065(){
	String str =
		"public class X {\n" +
		"  public Y<Z> foo()\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  public Y<<CompleteOnType:Z>> foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0066(){
	String str =
		"public class X {\n" +
		"  public Y<Z foo(){}\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  Y<<CompleteOnType:Z>>;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0067(){
	String str =
		"public class X {\n" +
		"  public Y<Z> foo(){}\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  public Y<<CompleteOnType:Z>> foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0068(){
	String str =
		"public class X {\n" +
		"  public Y<Z>.V<W\n" + 
		"}";


	String completeBehind = "Y<Z>.V<W";
	int cursorLocation = str.indexOf("Y<Z>.V<W") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:W>";
	String expectedParentNodeToString = "Y<Z>.V<<CompleteOnType:W>>";
	String completionIdentifier = "W";
	String expectedReplacedSource = "W";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  Y<Z>.V<<CompleteOnType:W>>;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0069(){
	String str =
		"public class X {\n" +
		"  public Y<Z>.V<W>\n" + 
		"}";


	String completeBehind = "Y<Z>.V<W";
	int cursorLocation = str.indexOf("Y<Z>.V<W") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:W>";
	String expectedParentNodeToString = "Y<Z>.V<<CompleteOnType:W>>";
	String completionIdentifier = "W";
	String expectedReplacedSource = "W";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  Y<Z>.V<<CompleteOnType:W>>;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0070(){
	String str =
		"public class X {\n" +
		"  public Y<Z>.V<W> var\n" + 
		"}";


	String completeBehind = "Y<Z>.V<W";
	int cursorLocation = str.indexOf("Y<Z>.V<W") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:W>";
	String expectedParentNodeToString = "Y<Z>.V<<CompleteOnType:W>>";
	String completionIdentifier = "W";
	String expectedReplacedSource = "W";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public Y<Z>.V<<CompleteOnType:W>> var;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0071(){
	String str =
		"public class X {\n" +
		"  public Y<Z>.V<W> var;\n" + 
		"}";


	String completeBehind = "Y<Z>.V<W";
	int cursorLocation = str.indexOf("Y<Z>.V<W") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:W>";
	String expectedParentNodeToString = "Y<Z>.V<<CompleteOnType:W>>";
	String completionIdentifier = "W";
	String expectedReplacedSource = "W";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public Y<Z>.V<<CompleteOnType:W>> var;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0072(){
	String str =
		"public class X {\n" +
		"  public Y<Z>.V<W foo()\n" + 
		"}";


	String completeBehind = "Y<Z>.V<W";
	int cursorLocation = str.indexOf("Y<Z>.V<W") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:W>";
	String expectedParentNodeToString = "Y<Z>.V<<CompleteOnType:W>>";
	String completionIdentifier = "W";
	String expectedReplacedSource = "W";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  Y<Z>.V<<CompleteOnType:W>>;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0073(){
	String str =
		"public class X {\n" +
		"  public Y<Z>.V<W> foo()\n" + 
		"}";


	String completeBehind = "Y<Z>.V<W";
	int cursorLocation = str.indexOf("Y<Z>.V<W") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:W>";
	String expectedParentNodeToString = "Y<Z>.V<<CompleteOnType:W>>";
	String completionIdentifier = "W";
	String expectedReplacedSource = "W";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  public Y<Z>.V<<CompleteOnType:W>> foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0074(){
	String str =
		"public class X {\n" +
		"  public Y<Z>.V<W foo(){}\n" + 
		"}";


	String completeBehind = "Y<Z>.V<W";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:W>";
	String expectedParentNodeToString = "Y<Z>.V<<CompleteOnType:W>>";
	String completionIdentifier = "W";
	String expectedReplacedSource = "W";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  Y<Z>.V<<CompleteOnType:W>>;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0075(){
	String str =
		"public class X {\n" +
		"  public Y<Z>.V<W> foo(){}\n" + 
		"}";


	String completeBehind = "Y<Z>.V<W";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:W>";
	String expectedParentNodeToString = "Y<Z>.V<<CompleteOnType:W>>";
	String completionIdentifier = "W";
	String expectedReplacedSource = "W";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  public Y<Z>.V<<CompleteOnType:W>> foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0076(){
	String str =
		"public class X extends  Y<Z {\n" +
		"  \n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  Y<<CompleteOnType:Z>>;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0077(){
	String str =
		"public class X extends  Y<Z> {\n" +
		"  \n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X extends Y<<CompleteOnType:Z>> {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0078(){
	String str =
		"public class X implements I1, Y<Z {\n" +
		"  \n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X implements I1 {\n" + 
		"  Y<<CompleteOnType:Z>>;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0079(){
	String str =
		"public class X implements I1, Y<Z> {\n" +
		"  \n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X implements I1, Y<<CompleteOnType:Z>> {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0080(){
	String str =
		"public class X {\n" +
		"  void foo(Y<Z){\n" + 
		"  \n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0081(){
	String str =
		"public class X {\n" +
		"  void foo(Y<Z>){\n" + 
		"  \n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0082(){
	String str =
		"public class X {\n" +
		"  void foo(Y<Z> var){\n" + 
		"  \n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo(Y<<CompleteOnType:Z>> var) {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0083(){
	String str =
		"public class X {\n" +
		"  Y<Z foo(){\n" + 
		"  \n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  Y<<CompleteOnType:Z>>;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0084(){
	String str =
		"public class X {\n" +
		"  Y<Z> foo(){\n" + 
		"  \n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  Y<<CompleteOnType:Z>> foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0085(){
	String str =
		"public class X  {\n" +
		"  void foo() throws Y<Z {\n" + 
		"  \n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0086(){
	String str =
		"public class X  {\n" +
		"  void foo() throws Y<Z> {\n" + 
		"  \n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() throws Y<<CompleteOnType:Z>> {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0087(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z void foo(){\n" + 
		"  \n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  Y<<CompleteOnType:Z>>;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0088(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z> void foo(){\n" + 
		"  \n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  <T extends Y<<CompleteOnType:Z>>>\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0089(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z>> void foo(){\n" + 
		"  \n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  <T extends Y<<CompleteOnType:Z>>>void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0090(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z int foo\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  Y<<CompleteOnType:Z>>;\n" + 
		"  int foo;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0091(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z> int foo\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  int foo;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  <T extends Y<<CompleteOnType:Z>>>\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0092(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z>> int foo\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  <T extends Y<<CompleteOnType:Z>>>\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0093(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z X\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  Y<<CompleteOnType:Z>>;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0094(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z> X\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  <T extends Y<<CompleteOnType:Z>>>\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0095(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z>> X\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  <T extends Y<<CompleteOnType:Z>>>\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0096(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  Y<<CompleteOnType:Z>>;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0097(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z>\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  <T extends Y<<CompleteOnType:Z>>>\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0098(){
	String str =
		"public class X {\n" +
		"  <T extends Y<Z>>\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  <T extends Y<<CompleteOnType:Z>>>\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0099(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    this.<Y, Y<Z bar();\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0100(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    this.<Y, Y<Z> bar();\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0101(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    this.<Y, Y<Z>> bar();\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0102(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    this.<Y, Y<Z bar\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0103(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    this.<Y, Y<Z> bar\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0104(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    this.<Y, Y<Z>> bar\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0105(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    this.<Y, Y<Z\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0106(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    this.<Y, Y<Z>\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0107(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    this.<Y, Y<Z>>\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0108(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    new <Y, Y<Z X();\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0109(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    new <Y, Y<Z> X();\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0110(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    new <Y, Y<Z>> X();\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0111(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    aaa.new <Y, Y<Z X();\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0112(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    aaa.new <Y, Y<Z> X();\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0113(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    aaa.new <Y, Y<Z>> X();\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0114(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    new V().new <Y, Y<Z X();\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0115(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    new V().new <Y, Y<Z> X();\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0116(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    new V().new <Y, Y<Z>> X();\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0117(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    for(Y<Z var;;){}\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0118(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    for(Y<Z> var;;){}\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0119(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    for(Y<Z\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0120(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    for(Y<Z>\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0121(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    try {\n" + 
		"    } catch(Y<Z e) {\n" +
		"   }\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0122(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    try {\n" + 
		"    } catch(Y<Z> e) {\n" +
		"   }\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0123(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    try {\n" + 
		"    } catch(Y<Z e\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0124(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    try {\n" + 
		"    } catch(Y<Z> e\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0125(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    catch(Y<Z e\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0126(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    catch(Y<Z> e\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0127(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    Object a = (Y<Z ) e;\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:Z>";
	expectedParentNodeToString = "(Y < <CompleteOnName:Z>)";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object a = (Y < <CompleteOnName:Z>);\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0128(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    Object a = (Y<Z> ) e;\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:Z>";
	expectedParentNodeToString = "(Y < <CompleteOnName:Z>)";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object a = (Y < <CompleteOnName:Z>);\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0129(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    (Y<Z) e;\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:Z>";
	expectedParentNodeToString = "(Y < <CompleteOnName:Z>)";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    (Y < <CompleteOnName:Z>);\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0130(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    (Y<Z>) e;\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:Z>";
	expectedParentNodeToString = "(Y < <CompleteOnName:Z>)";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    (Y < <CompleteOnName:Z>);\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0131(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    Object[] o = new Y<Z[0];\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object[] o = Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0132(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    Object[] o = new Y<Z>[0];\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object[] o = Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0133(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    Object[] o = new Y<Z\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object[] o = Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0134(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    Object[] o = new Y<Z>\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object[] o = Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0135(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    new Y<Z\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0136(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    new Y<Z>\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0137(){
	String str =
		"public class X {\n" +
		"  public X() {\n" + 
		"    <Y<Z super(0);\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"    super();\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0138(){
	String str =
		"public class X {\n" +
		"  public X() {\n" + 
		"    <Y<Z> super(0);\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"    super();\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0139(){
	String str =
		"public class X {\n" +
		"  public X() {\n" + 
		"    <Y<Z>> super(0);\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"    super();\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0140(){
	String str =
		"public class X {\n" +
		"  public X() {\n" + 
		"    aaa.<Y<Z super(0);\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"    super();\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0141(){
	String str =
		"public class X {\n" +
		"  public X() {\n" + 
		"    aaa.<Y<Z> super(0);\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"    super();\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0142(){
	String str =
		"public class X {\n" +
		"  public X() {\n" + 
		"    aaa.<Y<Z>> super(0);\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"    super();\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0143(){
	String str =
		"public class X {\n" +
		"  public X() {\n" + 
		"    A.this.<Y<Z super(0);\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"    super();\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0144(){
	String str =
		"public class X {\n" +
		"  public X() {\n" + 
		"    A.this.<Y<Z> super(0);\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"    super();\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0145(){
	String str =
		"public class X {\n" +
		"  public X() {\n" + 
		"    A.this.<Y<Z>> super(0);\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"    super();\n" + 
		"    Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0146(){
	String str =
		"public class X {\n" +
		"  W<Y<Z\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  Y<<CompleteOnType:Z>>;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0147(){
	String str =
		"public class X {\n" +
		"  W<Y<Z>\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  Y<<CompleteOnType:Z>>;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0148(){
	String str =
		"public class X {\n" +
		"  W<Y<Z>>\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  Y<<CompleteOnType:Z>>;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0149(){
	String str =
		"public class X {\n" +
		"  W<Y<Z>> var\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  W<Y<<CompleteOnType:Z>>> var;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0150(){
	String str =
		"public class X {\n" +
		"  W<Y<Z>> var;\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  W<Y<<CompleteOnType:Z>>> var;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0151(){
	String str =
		"public class X {\n" +
		"  W<A,B,C\n" + 
		"}";


	String completeBehind = "A,B";
	int cursorLocation = str.indexOf("A,B") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:B>";
	String expectedParentNodeToString = "W<A, <CompleteOnType:B>, C>";
	String completionIdentifier = "B";
	String expectedReplacedSource = "B";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  W<A, <CompleteOnType:B>, C>;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0152(){
	String str =
		"public class X {\n" +
		"  W<A,B,C>\n" + 
		"}";


	String completeBehind = "A,B";
	int cursorLocation = str.indexOf("A,B") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:B>";
	String expectedParentNodeToString = "W<A, <CompleteOnType:B>, C>";
	String completionIdentifier = "B";
	String expectedReplacedSource = "B";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  W<A, <CompleteOnType:B>, C>;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0153(){
	String str =
		"public class X {\n" +
		"  W<A,B,C> var\n" + 
		"}";


	String completeBehind = "A,B";
	int cursorLocation = str.indexOf("A,B") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:B>";
	String expectedParentNodeToString = "W<A, <CompleteOnType:B>, C>";
	String completionIdentifier = "B";
	String expectedReplacedSource = "B";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  W<A, <CompleteOnType:B>, C> var;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0154(){
	String str =
		"public class X {\n" +
		"  W<A,B,C> var;\n" + 
		"}";


	String completeBehind = "A,B";
	int cursorLocation = str.indexOf("A,B") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:B>";
	String expectedParentNodeToString = "W<A, <CompleteOnType:B>, C>";
	String completionIdentifier = "B";
	String expectedReplacedSource = "B";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  W<A, <CompleteOnType:B>, C> var;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0155(){
	String str =
		"public class X {\n" +
		"  Y<Z>.V<W> var;\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>.V<W>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  Y<<CompleteOnType:Z>>.V<W> var;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0156(){
	String str =
		"public class X {\n" +
		"  Y<Z>.V<W> var\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>.V<W>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  Y<<CompleteOnType:Z>>.V<W> var;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0157(){
	String str =
		"public class X {\n" +
		"  Y<Z>.V<W>\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>.V<W>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  Y<<CompleteOnType:Z>>.V<W>;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0158(){
	String str =
		"public class X {\n" +
		"  Y<Z>.V<W\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>.V<W>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  Y<<CompleteOnType:Z>>.V<W>;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0159(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    Object a = (W<Y<Z> ) e;\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object a = Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0160(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    ((Y<Z>) e).foo();\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:Z>";
	expectedParentNodeToString = "(Y < <CompleteOnName:Z>)";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    (Y < <CompleteOnName:Z>);\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0161(){
	String str =
		"public class X  <T extends Z<Y>> {\n" +
		"}";


	String completeBehind = "Z";
	int cursorLocation = str.indexOf("Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X<T extends <CompleteOnType:Z>> {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0162(){
	String str =
		"public class X  <T extends X.Z<Y>> {\n" +
		"}";


	String completeBehind = "Z";
	int cursorLocation = str.indexOf("Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:X.Z>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "X.Z";
	String expectedUnitDisplayString =
		"public class X<T extends <CompleteOnType:X.Z>> {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0163(){
	String str =
		"public class X  <T extends X<W>.Z<Y>> {\n" +
		"}";


	String completeBehind = "Z";
	int cursorLocation = str.indexOf("Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:X<W>.Z>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "X<W>.Z";
	String expectedUnitDisplayString =
		"public class X<T extends <CompleteOnType:X<W>.Z>> {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0164(){
	String str =
		"public class X {\n" +
		"  <T extends X<W>.Z> foo() {}\n" +
		"}";


	String completeBehind = "Z";
	int cursorLocation = str.indexOf("Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:X<W>.Z>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "X<W>.Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  <T extends <CompleteOnType:X<W>.Z>>foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0165(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    Object a = (W.Y<Z>) e;\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:Z>";
	expectedParentNodeToString = "(W.Y < <CompleteOnName:Z>)";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object a = (W.Y < <CompleteOnName:Z>);\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0166(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" + 
		"    Object a = (W<U>.Y<Z>) e;\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:Z>";
	expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	completionIdentifier = "Z";
	expectedReplacedSource = "Z";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object a = W<U>.Y<<CompleteOnType:Z>>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
* https://bugs.eclipse.org/bugs/show_bug.cgi?id=69598
*/
public void test0167(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    List<Integer> cont=new ArrayList<Integer>();\n" +
		"    for (Integer i:cont){\n" +
		"      i.\n" +
		"    }\n" + 
		"  }\n" + 
		"}";


	String completeBehind = "i.";
	int cursorLocation = str.indexOf("i.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:i.>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "";
	expectedReplacedSource = "i.";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    List<Integer> cont;\n" + 
		"    Integer i;\n" + 
		"    {\n" + 
		"      <CompleteOnName:i.>;\n" + 
		"    }\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
* https://bugs.eclipse.org/bugs/show_bug.cgi?id=69598
*/
public void test0168(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    List<Integer> cont=new ArrayList<Integer>();\n" +
		"    for (Integer i:cont){\n" +
		"    }\n" + 
		"    i.\n" +
		"  }\n" + 
		"}";


	String completeBehind = "i.";
	int cursorLocation = str.indexOf("i.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:i.>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "";
	expectedReplacedSource = "i.";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    List<Integer> cont;\n" + 
		"    <CompleteOnName:i.>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
* https://bugs.eclipse.org/bugs/show_bug.cgi?id=71705
*/
public void test0169(){
	String str =
		"public class X {\n"+
		"  Object o;\n"+
		"  void foo(int[] a, int[] b){\n"+
		"    if(a.lenth < b.length)\n"+
		"      System.out.println();\n"+
		"  }\n"+
		"}";


	String completeBehind = "Object";
	int cursorLocation = str.indexOf("Object") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Object>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "Object";
	String expectedReplacedSource = "Object";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  <CompleteOnType:Object>;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo(int[] a, int[] b) {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
* https://bugs.eclipse.org/bugs/show_bug.cgi?id=71705
*/
public void test0170(){
	String str =
		"public class X {\n"+
		"  bar\n"+
		"  void foo(){\n"+
		"    A<B\n"+
		"  }\n"+
		"}";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:bar>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "bar";
	String expectedReplacedSource = "bar";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  <CompleteOnType:bar>;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void _testXXX2(){
	String str =
		"public class X extends Y. {\n" +
		"}";


	String completeBehind = "Y";
	int cursorLocation = str.indexOf("Y") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnClass:Y>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"public class X extends <CompleteOnClass:Y> {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0171(){
	String str =
		"public class X{\n" + 
		"  public void foo() {\n" + 
		"    Object o =(A<B>) tmp;\n" + 
		"    bar\n" + 
		"  }\n" + 
		"}\n" + 
		"\n";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  public void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:bar>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "bar";
	expectedReplacedSource = "bar";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  public void foo() {\n" + 
		"    Object o;\n" + 
		"    <CompleteOnName:bar>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0172(){
	String str =
		"public class X{\n" + 
		"  public void foo() {\n" + 
		"    Object o =(A<B>[]) tmp;\n" + 
		"    bar\n" + 
		"  }\n" + 
		"}\n" + 
		"\n";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  public void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:bar>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "bar";
	expectedReplacedSource = "bar";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  public void foo() {\n" + 
		"    Object o;\n" + 
		"    <CompleteOnName:bar>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0173(){
	String str =
		"public class X{\n" + 
		"  public void foo() {\n" + 
		"    Object o =(A<B>.C) tmp;\n" + 
		"    bar\n" + 
		"  }\n" + 
		"}\n" + 
		"\n";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  public void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:bar>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "bar";
	expectedReplacedSource = "bar";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  public void foo() {\n" + 
		"    Object o;\n" + 
		"    <CompleteOnName:bar>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0174(){
	String str =
		"public class X{\n" + 
		"  public void foo() {\n" + 
		"    Object o =(A<B>.C[]) tmp;\n" + 
		"    bar\n" + 
		"  }\n" + 
		"}\n" + 
		"\n";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  public void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:bar>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "bar";
	expectedReplacedSource = "bar";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  public void foo() {\n" + 
		"    Object o;\n" + 
		"    <CompleteOnName:bar>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0175(){
	String str =
		"public class X{\n" + 
		"  public void foo() {\n" + 
		"    Object o =(A<B>.C<D>) tmp;\n" + 
		"    bar\n" + 
		"  }\n" + 
		"}\n" + 
		"\n";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  public void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:bar>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "bar";
	expectedReplacedSource = "bar";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  public void foo() {\n" + 
		"    Object o;\n" + 
		"    <CompleteOnName:bar>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0176(){
	String str =
		"public class X{\n" + 
		"  public void foo() {\n" + 
		"    Object o =(A<B>.C<D>[]) tmp;\n" + 
		"    bar\n" + 
		"  }\n" + 
		"}\n" + 
		"\n";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  public void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:bar>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "bar";
	expectedReplacedSource = "bar";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  public void foo() {\n" + 
		"    Object o;\n" + 
		"    <CompleteOnName:bar>;\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=68594
 */
public void test0177(){
	String str =
		"public class X{\n" + 
		"  Stack<List<Object>> o = null;\n" + 
		"}\n" + 
		"\n";


	String completeBehind = "Stack";
	int cursorLocation = str.indexOf("Stack") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Stack>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "Stack";
	String expectedReplacedSource = "Stack";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  <CompleteOnType:Stack>;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n"
;

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
}
