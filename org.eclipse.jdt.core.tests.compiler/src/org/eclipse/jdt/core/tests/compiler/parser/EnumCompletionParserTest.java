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
package org.eclipse.jdt.core.tests.compiler.parser;


import java.util.Map;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;



public class EnumCompletionParserTest extends AbstractCompletionTest {
public EnumCompletionParserTest(String testName) {
	super(testName);
}

protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);	
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);	
	return options;
}

/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=83321
 */
public void test0001(){
	String str =
		"public class Completion {\n" + 
		"	/*here*/\n" + 
		"}\n" + 
		"enum Natural {\n" + 
		"	ONE;\n" + 
		"}\n";

	String completeBehind = "/*here*/";
	int cursorLocation = str.indexOf("/*here*/") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	String expectedUnitDisplayString =
		"public class Completion {\n" + 
		"  <CompleteOnType:>;\n" + 
		"  public Completion() {\n" + 
		"  }\n" + 
		"}\n" + 
		"enum Natural {\n" + 
		"  ONE(),\n" + 
		"  Natural() {\n" + 
		"  }\n" + 
		"  <clinit>() {\n" + 
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0002(){
	String str =
		"public class Test {\n" + 
		"	void foo() {\n" + 
		"	  switch(c) {\n" + 
		"	  	case FOO :\n" + 
		"	  	  break;\n" + 
		"	  }\n" + 
		"	}\n" + 
		"}\n";

	String completeBehind = "FOO";
	int cursorLocation = str.indexOf("FOO") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class Test {\n" + 
		"  public Test() {\n" + 
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
	
	expectedCompletionNodeToString = "<CompleteOnName:FOO>";
	expectedParentNodeToString =
		"switch (c) {\n" + 
		"case <CompleteOnName:FOO> : ;\n" + 
		"}";
	completionIdentifier = "FOO";
	expectedReplacedSource = "FOO";
	expectedUnitDisplayString =
		"public class Test {\n" + 
		"  public Test() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    {\n" + 
		"      switch (c) {\n" + 
		"      case <CompleteOnName:FOO> : ;\n" + 
		"      }\n" + 
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0003(){
	String str =
		"public class Test {\n" + 
		"	void foo() {\n" + 
		"	  switch(c) {\n" + 
		"	  	case BAR :\n" + 
		"	  	case FOO :\n" + 
		"	  	  break;\n" + 
		"	  }\n" + 
		"	}\n" + 
		"}\n";

	String completeBehind = "FOO";
	int cursorLocation = str.indexOf("FOO") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class Test {\n" + 
		"  public Test() {\n" + 
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
	
	expectedCompletionNodeToString = "<CompleteOnName:FOO>";
	expectedParentNodeToString =
		"switch (c) {\n" + 
		"case BAR : ;\n" + 
		"case <CompleteOnName:FOO> : ;\n" + 
		"}";
	completionIdentifier = "FOO";
	expectedReplacedSource = "FOO";
	expectedUnitDisplayString =
		"public class Test {\n" + 
		"  public Test() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    {\n" + 
		"      switch (c) {\n" + 
		"      case BAR : ;\n" + 
		"      case <CompleteOnName:FOO> : ;\n" + 
		"      }\n" + 
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0004(){
	String str =
		"public class Test {\n" + 
		"	void foo() {\n" + 
		"	  switch(c) {\n" + 
		"	  	case BAR :\n" + 
		"	  	  break;\n" + 
		"	  	case FOO :\n" + 
		"	  	  break;\n" + 
		"	  }\n" + 
		"	}\n" + 
		"}\n";

	String completeBehind = "FOO";
	int cursorLocation = str.indexOf("FOO") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class Test {\n" + 
		"  public Test() {\n" + 
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
	
	expectedCompletionNodeToString = "<CompleteOnName:FOO>";
	expectedParentNodeToString =
		"switch (c) {\n" + 
		"case BAR : ;\n" + 
		"    break ;\n" + 
		"case <CompleteOnName:FOO> : ;\n" + 
		"}";
	completionIdentifier = "FOO";
	expectedReplacedSource = "FOO";
	expectedUnitDisplayString =
		"public class Test {\n" + 
		"  public Test() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    {\n" + 
		"      switch (c) {\n" + 
		"      case BAR : ;\n" + 
		"          break ;\n" + 
		"      case <CompleteOnName:FOO> : ;\n" + 
		"      }\n" + 
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0005(){
	String str =
		"public class Test {\n" + 
		"	void foo() {\n" + 
		"	  switch(c) {\n" + 
		"	  	case BAR :\n" + 
		"	  	  break;\n" + 
		"	  	case FOO :\n" + 
		"	  }\n" + 
		"	}\n" + 
		"}\n";

	String completeBehind = "FOO";
	int cursorLocation = str.indexOf("FOO") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class Test {\n" + 
		"  public Test() {\n" + 
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
	
	expectedCompletionNodeToString = "<CompleteOnName:FOO>";
	expectedParentNodeToString =
		"switch (c) {\n" + 
		"case BAR : ;\n" + 
		"    break ;\n" + 
		"case <CompleteOnName:FOO> : ;\n" + 
		"}";
	completionIdentifier = "FOO";
	expectedReplacedSource = "FOO";
	expectedUnitDisplayString =
		"public class Test {\n" + 
		"  public Test() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    {\n" + 
		"      switch (c) {\n" + 
		"      case BAR : ;\n" + 
		"          break ;\n" + 
		"      case <CompleteOnName:FOO> : ;\n" + 
		"      }\n" + 
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0006(){
	String str =
		"public class Test {\n" + 
		"	void foo() {\n" + 
		"	  switch(c) {\n" + 
		"	  	case BAR :\n" + 
		"	  	  break;\n" + 
		"	  	case FOO\n" + 
		"	  }\n" + 
		"	}\n" + 
		"}\n";

	String completeBehind = "FOO";
	int cursorLocation = str.indexOf("FOO") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class Test {\n" + 
		"  public Test() {\n" + 
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
	
	expectedCompletionNodeToString = "<CompleteOnName:FOO>";
	expectedParentNodeToString =
		"switch (c) {\n" + 
		"case BAR : ;\n" + 
		"    break ;\n" + 
		"case <CompleteOnName:FOO> : ;\n" + 
		"}";
	completionIdentifier = "FOO";
	expectedReplacedSource = "FOO";
	expectedUnitDisplayString =
		"public class Test {\n" + 
		"  public Test() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    {\n" + 
		"      switch (c) {\n" + 
		"      case BAR : ;\n" + 
		"          break ;\n" + 
		"      case <CompleteOnName:FOO> : ;\n" + 
		"      }\n" + 
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0007(){
	String str =
		"public class Test {\n" + 
		"	void foo() {\n" + 
		"	  switch(c) {\n" + 
		"	  	case BAR0 :\n" + 
		"	      switch(c) {\n" + 
		"	        case BAR :\n" + 
		"	  	      break;\n" + 
		"	  	    case FOO\n" + 
		"	      }\n" + 
		"	  	  break;\n" + 
		"	  	case BAR2 :\n" + 
		"	  	  break;\n" + 
		"	  }\n" + 
		"	}\n" + 
		"}\n";

	String completeBehind = "FOO";
	int cursorLocation = str.indexOf("FOO") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class Test {\n" + 
		"  public Test() {\n" + 
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
	
	expectedCompletionNodeToString = "<CompleteOnName:FOO>";
	expectedParentNodeToString =
		"switch (c) {\n" + 
		"case BAR : ;\n" + 
		"    break ;\n" + 
		"case <CompleteOnName:FOO> : ;\n" + 
		"}";
	completionIdentifier = "FOO";
	expectedReplacedSource = "FOO";
	expectedUnitDisplayString =
		"public class Test {\n" + 
		"  public Test() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    {\n" + 
		"      {\n" + 
		"        switch (c) {\n" + 
		"        case BAR : ;\n" + 
		"            break ;\n" + 
		"        case <CompleteOnName:FOO> : ;\n" + 
		"        }\n" + 
		"      }\n" + 
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
}
