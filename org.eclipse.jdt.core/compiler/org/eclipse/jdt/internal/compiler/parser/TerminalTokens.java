/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
	int TokenNameNotAToken = 0,
							TokenNameWHITESPACE = 1000,
							TokenNameCOMMENT_LINE = 1001,
							TokenNameCOMMENT_BLOCK = 1002,
							TokenNameCOMMENT_JAVADOC = 1003;

	// BEGIN_AUTOGENERATED_REGION
	int TokenNameIdentifier = 21,
							TokenNameabstract = 52,
							TokenNameassert = 79,
							TokenNameboolean = 103,
							TokenNamebreak = 80,
							TokenNamebyte = 104,
							TokenNamecase = 89,
							TokenNamecatch = 105,
							TokenNamechar = 106,
							TokenNameclass = 68,
							TokenNamecontinue = 81,
							TokenNameconst = 130,
							TokenNamedefault = 74,
							TokenNamedo = 82,
							TokenNamedouble = 107,
							TokenNameelse = 117,
							TokenNameenum = 72,
							TokenNameextends = 90,
							TokenNamefalse = 41,
							TokenNamefinal = 53,
							TokenNamefinally = 114,
							TokenNamefloat = 108,
							TokenNamefor = 83,
							TokenNamegoto = 131,
							TokenNameif = 84,
							TokenNameimplements = 127,
							TokenNameimport = 109,
							TokenNameinstanceof = 17,
							TokenNameint = 110,
							TokenNameinterface = 71,
							TokenNamelong = 111,
							TokenNamenative = 54,
							TokenNamenew = 36,
							TokenNamenull = 42,
							TokenNamepackage = 88,
							TokenNameprivate = 55,
							TokenNameprotected = 56,
							TokenNamepublic = 57,
							TokenNamereturn = 85,
							TokenNameshort = 112,
							TokenNamestatic = 39,
							TokenNamestrictfp = 58,
							TokenNamesuper = 34,
							TokenNameswitch = 61,
							TokenNamesynchronized = 40,
							TokenNamethis = 35,
							TokenNamethrow = 76,
							TokenNamethrows = 115,
							TokenNametransient = 59,
							TokenNametrue = 43,
							TokenNametry = 86,
							TokenNamevoid = 113,
							TokenNamevolatile = 60,
							TokenNamewhile = 77,
							TokenNamemodule = 118,
							TokenNameopen = 119,
							TokenNamerequires = 120,
							TokenNametransitive = 125,
							TokenNameexports = 121,
							TokenNameopens = 122,
							TokenNameto = 128,
							TokenNameuses = 123,
							TokenNameprovides = 124,
							TokenNamewith = 129,
							TokenNameIntegerLiteral = 44,
							TokenNameLongLiteral = 45,
							TokenNameFloatingPointLiteral = 46,
							TokenNameDoubleLiteral = 47,
							TokenNameCharacterLiteral = 48,
							TokenNameStringLiteral = 49,
							TokenNameTextBlock = 50,
							TokenNamePLUS_PLUS = 2,
							TokenNameMINUS_MINUS = 3,
							TokenNameEQUAL_EQUAL = 19,
							TokenNameLESS_EQUAL = 12,
							TokenNameGREATER_EQUAL = 13,
							TokenNameNOT_EQUAL = 20,
							TokenNameLEFT_SHIFT = 18,
							TokenNameRIGHT_SHIFT = 14,
							TokenNameUNSIGNED_RIGHT_SHIFT = 16,
							TokenNamePLUS_EQUAL = 91,
							TokenNameMINUS_EQUAL = 92,
							TokenNameMULTIPLY_EQUAL = 93,
							TokenNameDIVIDE_EQUAL = 94,
							TokenNameAND_EQUAL = 95,
							TokenNameOR_EQUAL = 96,
							TokenNameXOR_EQUAL = 97,
							TokenNameREMAINDER_EQUAL = 98,
							TokenNameLEFT_SHIFT_EQUAL = 99,
							TokenNameRIGHT_SHIFT_EQUAL = 100,
							TokenNameUNSIGNED_RIGHT_SHIFT_EQUAL = 101,
							TokenNameOR_OR = 31,
							TokenNameAND_AND = 30,
							TokenNamePLUS = 4,
							TokenNameMINUS = 5,
							TokenNameNOT = 64,
							TokenNameREMAINDER = 9,
							TokenNameXOR = 24,
							TokenNameAND = 22,
							TokenNameMULTIPLY = 8,
							TokenNameOR = 28,
							TokenNameTWIDDLE = 65,
							TokenNameDIVIDE = 10,
							TokenNameGREATER = 15,
							TokenNameLESS = 11,
							TokenNameLPAREN = 23,
							TokenNameRPAREN = 26,
							TokenNameLBRACE = 37,
							TokenNameRBRACE = 33,
							TokenNameLBRACKET = 6,
							TokenNameRBRACKET = 67,
							TokenNameSEMICOLON = 25,
							TokenNameQUESTION = 29,
							TokenNameCOLON = 63,
							TokenNameCOMMA = 32,
							TokenNameDOT = 1,
							TokenNameEQUAL = 75,
							TokenNameAT = 38,
							TokenNameELLIPSIS = 116,
							TokenNameARROW = 102,
							TokenNameCOLON_COLON = 7,
							TokenNameBeginLambda = 51,
							TokenNameBeginIntersectionCast = 66,
							TokenNameBeginTypeArguments = 87,
							TokenNameElidedSemicolonAndRightBrace = 69,
							TokenNameAT308 = 27,
							TokenNameAT308DOTDOTDOT = 126,
							TokenNameBeginCaseExpr = 70,
							TokenNameRestrictedIdentifierYield = 78,
							TokenNameRestrictedIdentifierrecord = 73,
							TokenNameEOF = 62,
							TokenNameERROR = 132;
}
