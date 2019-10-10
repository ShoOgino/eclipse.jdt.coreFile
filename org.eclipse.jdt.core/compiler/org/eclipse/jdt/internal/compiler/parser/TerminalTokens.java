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
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
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

	int TokenNameIdentifier = 22,
							TokenNameabstract = 52,
							TokenNameassert = 78,
							TokenNameboolean = 101,
							TokenNamebreak = 79,
							TokenNamebyte = 102,
							TokenNamecase = 103,
							TokenNamecatch = 104,
							TokenNamechar = 105,
							TokenNameclass = 68,
							TokenNamecontinue = 80,
							TokenNameconst = 130,
							TokenNamedefault = 73,
							TokenNamedo = 81,
							TokenNamedouble = 106,
							TokenNameelse = 115,
							TokenNameenum = 72,
							TokenNameextends = 88,
							TokenNamefalse = 41,
							TokenNamefinal = 53,
							TokenNamefinally = 113,
							TokenNamefloat = 107,
							TokenNamefor = 82,
							TokenNamegoto = 131,
							TokenNameif = 83,
							TokenNameimplements = 126,
							TokenNameimport = 108,
							TokenNameinstanceof = 17,
							TokenNameint = 109,
							TokenNameinterface = 69,
							TokenNamelong = 110,
							TokenNamenative = 54,
							TokenNamenew = 36,
							TokenNamenull = 42,
							TokenNamepackage = 87,
							TokenNameprivate = 55,
							TokenNameprotected = 56,
							TokenNamepublic = 57,
							TokenNamereturn = 84,
							TokenNameshort = 111,
							TokenNamestatic = 39,
							TokenNamestrictfp = 58,
							TokenNamesuper = 34,
							TokenNameswitch = 61,
							TokenNamesynchronized = 40,
							TokenNamethis = 35,
							TokenNamethrow = 75,
							TokenNamethrows = 114,
							TokenNametransient = 59,
							TokenNametrue = 43,
							TokenNametry = 85,
							TokenNamevoid = 112,
							TokenNamevolatile = 60,
							TokenNamewhile = 76,
							TokenNamemodule = 116,
							TokenNameopen = 117,
							TokenNamerequires = 118,
							TokenNametransitive = 124,
							TokenNameexports = 119,
							TokenNameopens = 120,
							TokenNameto = 127,
							TokenNameuses = 121,
							TokenNameprovides = 122,
							TokenNamewith = 128,
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
							TokenNamePLUS_EQUAL = 89,
							TokenNameMINUS_EQUAL = 90,
							TokenNameMULTIPLY_EQUAL = 91,
							TokenNameDIVIDE_EQUAL = 92,
							TokenNameAND_EQUAL = 93,
							TokenNameOR_EQUAL = 94,
							TokenNameXOR_EQUAL = 95,
							TokenNameREMAINDER_EQUAL = 96,
							TokenNameLEFT_SHIFT_EQUAL = 97,
							TokenNameRIGHT_SHIFT_EQUAL = 98,
							TokenNameUNSIGNED_RIGHT_SHIFT_EQUAL = 99,
							TokenNameOR_OR = 31,
							TokenNameAND_AND = 30,
							TokenNamePLUS = 4,
							TokenNameMINUS = 5,
							TokenNameNOT = 64,
							TokenNameREMAINDER = 9,
							TokenNameXOR = 24,
							TokenNameAND = 21,
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
							TokenNameEQUAL = 74,
							TokenNameAT = 38,
							TokenNameELLIPSIS = 125,
							TokenNameARROW = 100,
							TokenNameCOLON_COLON = 7,
							TokenNameBeginLambda = 51,
							TokenNameBeginIntersectionCast = 66,
							TokenNameBeginTypeArguments = 86,
							TokenNameElidedSemicolonAndRightBrace = 70,
							TokenNameAT308 = 27,
							TokenNameAT308DOTDOTDOT = 129,
							TokenNameBeginCaseExpr = 71,
							TokenNameRestrictedIdentifierYield = 77,
							TokenNameRestrictedIdentifierrecord = 123,
							TokenNameEOF = 62,
							TokenNameERROR = 132;
}
