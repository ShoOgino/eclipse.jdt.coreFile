/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.parser;
 
/**
 * IMPORTANT NOTE: These constants are dedicated to the internal Scanner implementation. 
 * It is mirrored in org.eclipse.jdt.core.compiler public package where it is API. 
 * The mirror implementation is using the backward compatible ITerminalSymbols constant 
 * definitions (stable with 2.0), whereas the internal implementation uses TerminalTokens 
 * which constant values reflect the latest parser generation state.
 */
 
/**
 * Maps each terminal symbol in the java-grammar into a unique integer. 
 * This integer is used to represent the terminal when computing a parsing action. 
 * 
 * Disclaimer : These constant values are generated automatically using a Java 
 * grammar, therefore their actual values are subject to change if new keywords 
 * were added to the language (for instance, 'assert' is a keyword in 1.4).
 */
public interface TerminalTokens {

	// special tokens not part of grammar - not autogenerated
	int 
		TokenNameWHITESPACE = 1000,
		TokenNameCOMMENT_LINE = 1001,
		TokenNameCOMMENT_BLOCK = 1002,
		TokenNameCOMMENT_JAVADOC = 1003;
	
	int
      TokenNameIdentifier = 24,
      TokenNameabstract = 58,
      TokenNameassert = 71,
      TokenNameboolean = 31,
      TokenNamebreak = 72,
      TokenNamebyte = 32,
      TokenNamecase = 84,
      TokenNamecatch = 87,
      TokenNamechar = 33,
      TokenNameclass = 81,
      TokenNamecontinue = 73,
      TokenNamedefault = 85,
      TokenNamedo = 74,
      TokenNamedouble = 34,
      TokenNameelse = 88,
      TokenNameextends = 91,
      TokenNamefalse = 44,
      TokenNamefinal = 59,
      TokenNamefinally = 89,
      TokenNamefloat = 35,
      TokenNamefor = 75,
      TokenNameif = 76,
      TokenNameimplements = 103,
      TokenNameimport = 82,
      TokenNameinstanceof = 13,
      TokenNameint = 36,
      TokenNameinterface = 83,
      TokenNamelong = 37,
      TokenNamenative = 60,
      TokenNamenew = 41,
      TokenNamenull = 45,
      TokenNamepackage = 86,
      TokenNameprivate = 61,
      TokenNameprotected = 62,
      TokenNamepublic = 63,
      TokenNamereturn = 77,
      TokenNameshort = 38,
      TokenNamestatic = 57,
      TokenNamestrictfp = 64,
      TokenNamesuper = 42,
      TokenNameswitch = 78,
      TokenNamesynchronized = 54,
      TokenNamethis = 43,
      TokenNamethrow = 79,
      TokenNamethrows = 104,
      TokenNametransient = 65,
      TokenNametrue = 46,
      TokenNametry = 80,
      TokenNamevoid = 39,
      TokenNamevolatile = 66,
      TokenNamewhile = 70,
      TokenNameIntegerLiteral = 47,
      TokenNameLongLiteral = 48,
      TokenNameFloatingPointLiteral = 49,
      TokenNameDoubleLiteral = 50,
      TokenNameCharacterLiteral = 51,
      TokenNameStringLiteral = 52,
      TokenNamePLUS_PLUS = 7,
      TokenNameMINUS_MINUS = 8,
      TokenNameEQUAL_EQUAL = 18,
      TokenNameLESS_EQUAL = 14,
      TokenNameGREATER_EQUAL = 15,
      TokenNameNOT_EQUAL = 19,
      TokenNameLEFT_SHIFT = 11,
      TokenNameRIGHT_SHIFT = 9,
      TokenNameUNSIGNED_RIGHT_SHIFT = 10,
      TokenNamePLUS_EQUAL = 92,
      TokenNameMINUS_EQUAL = 93,
      TokenNameMULTIPLY_EQUAL = 94,
      TokenNameDIVIDE_EQUAL = 95,
      TokenNameAND_EQUAL = 96,
      TokenNameOR_EQUAL = 97,
      TokenNameXOR_EQUAL = 98,
      TokenNameREMAINDER_EQUAL = 99,
      TokenNameLEFT_SHIFT_EQUAL = 100,
      TokenNameRIGHT_SHIFT_EQUAL = 101,
      TokenNameUNSIGNED_RIGHT_SHIFT_EQUAL = 102,
      TokenNameOR_OR = 26,
      TokenNameAND_AND = 27,
      TokenNamePLUS = 1,
      TokenNameMINUS = 2,
      TokenNameNOT = 67,
      TokenNameREMAINDER = 5,
      TokenNameXOR = 21,
      TokenNameAND = 20,
      TokenNameMULTIPLY = 4,
      TokenNameOR = 23,
      TokenNameTWIDDLE = 68,
      TokenNameDIVIDE = 6,
      TokenNameGREATER = 16,
      TokenNameLESS = 17,
      TokenNameLPAREN = 25,
      TokenNameRPAREN = 29,
      TokenNameLBRACE = 55,
      TokenNameRBRACE = 40,
      TokenNameLBRACKET = 12,
      TokenNameRBRACKET = 69,
      TokenNameSEMICOLON = 22,
      TokenNameQUESTION = 28,
      TokenNameCOLON = 53,
      TokenNameCOMMA = 30,
      TokenNameDOT = 3,
      TokenNameEQUAL = 90,
      TokenNameEOF = 56,
	  TokenNameERROR = 105;
}
