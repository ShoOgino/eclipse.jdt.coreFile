/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
     IBM Corporation - initial API and implementation
**********************************************************************/

package org.eclipse.jdt.core.compiler;
 
/**
 * Maps each terminal symbol in the java-grammar into a unique integer. 
 * This integer is used to represent the terminal when computing a parsing action. 
 * 
 * Disclaimer : These constant values are generated automatically using a Java 
 * grammar, therefore their actual values are subject to change if new keywords 
 * were added to the language (i.e. 'assert' keyword in 1.4).
 * 
 * @see IScanner
 * @since 2.0
 */
public interface ITerminalSymbols {

	// special tokens not part of grammar - not autogenerated
	int 
		TokenNameWHITESPACE = 1000,
		TokenNameCOMMENT_LINE = 1001,
		TokenNameCOMMENT_BLOCK = 1002,
		TokenNameCOMMENT_JAVADOC = 1003;
	
    int
      TokenNameIdentifier = 6,
      TokenNameabstract = 99,
      TokenNameassert = 121,
      TokenNameboolean = 25,
      TokenNamebreak = 122,
      TokenNamebyte = 26,
      TokenNamecase = 211,
      TokenNamecatch = 225,
      TokenNamechar = 27,
      TokenNameclass = 166,
      TokenNamecontinue = 123,
      TokenNamedefault = 212,
      TokenNamedo = 124,
      TokenNamedouble = 28,
      TokenNameelse = 213,
      TokenNameextends = 243,
      TokenNamefalse = 44,
      TokenNamefinal = 100,
      TokenNamefinally = 226,
      TokenNamefloat = 29,
      TokenNamefor = 125,
      TokenNameif = 126,
      TokenNameimplements = 268,
      TokenNameimport = 191,
      TokenNameinstanceof = 16,
      TokenNameint = 30,
      TokenNameinterface = 169,
      TokenNamelong = 31,
      TokenNamenative = 101,
      TokenNamenew = 40,
      TokenNamenull = 45,
      TokenNamepackage = 214,
      TokenNameprivate = 102,
      TokenNameprotected = 103,
      TokenNamepublic = 104,
      TokenNamereturn = 127,
      TokenNameshort = 32,
      TokenNamestatic = 97,
      TokenNamestrictfp = 105,
      TokenNamesuper = 42,
      TokenNameswitch = 128,
      TokenNamesynchronized = 88,
      TokenNamethis = 43,
      TokenNamethrow = 129,
      TokenNamethrows = 227,
      TokenNametransient = 106,
      TokenNametrue = 46,
      TokenNametry = 130,
      TokenNamevoid = 33,
      TokenNamevolatile = 107,
      TokenNamewhile = 120,
      TokenNameIntegerLiteral = 47,
      TokenNameLongLiteral = 48,
      TokenNameFloatingPointLiteral = 49,
      TokenNameDoubleLiteral = 50,
      TokenNameCharacterLiteral = 51,
      TokenNameStringLiteral = 52,
      TokenNamePLUS_PLUS = 3,
      TokenNameMINUS_MINUS = 4,
      TokenNameEQUAL_EQUAL = 23,
      TokenNameLESS_EQUAL = 17,
      TokenNameGREATER_EQUAL = 18,
      TokenNameNOT_EQUAL = 24,
      TokenNameLEFT_SHIFT = 13,
      TokenNameRIGHT_SHIFT = 11,
      TokenNameUNSIGNED_RIGHT_SHIFT = 12,
      TokenNamePLUS_EQUAL = 170,
      TokenNameMINUS_EQUAL = 171,
      TokenNameMULTIPLY_EQUAL = 172,
      TokenNameDIVIDE_EQUAL = 173,
      TokenNameAND_EQUAL = 174,
      TokenNameOR_EQUAL = 175,
      TokenNameXOR_EQUAL = 176,
      TokenNameREMAINDER_EQUAL = 177,
      TokenNameLEFT_SHIFT_EQUAL = 178,
      TokenNameRIGHT_SHIFT_EQUAL = 179,
      TokenNameUNSIGNED_RIGHT_SHIFT_EQUAL = 180,
      TokenNameOR_OR = 73,
      TokenNameAND_AND = 72,
      TokenNamePLUS = 1,
      TokenNameMINUS = 2,
      TokenNameNOT = 75,
      TokenNameREMAINDER = 8,
      TokenNameXOR = 68,
      TokenNameAND = 38,
      TokenNameMULTIPLY = 7,
      TokenNameOR = 71,
      TokenNameTWIDDLE = 76,
      TokenNameDIVIDE = 9,
      TokenNameGREATER = 19,
      TokenNameLESS = 20,
      TokenNameLPAREN = 10,
      TokenNameRPAREN = 81,
      TokenNameLBRACE = 111,
      TokenNameRBRACE = 86,
      TokenNameLBRACKET = 14,
      TokenNameRBRACKET = 119,
      TokenNameSEMICOLON = 41,
      TokenNameQUESTION = 74,
      TokenNameCOLON = 110,
      TokenNameCOMMA = 82,
      TokenNameDOT = 5,
      TokenNameEQUAL = 167,
      TokenNameEOF = 114,
      TokenNameERROR = 308;
}
