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
package org.eclipse.jdt.internal.compiler.parser;

/*An interface that contains static declarations for some basic information
 about the parser such as the number of rules in the grammar, the starting state, etc...*/
public interface ParserBasicInformation {

	int ERROR_SYMBOL = 108,
		MAX_NAME_LENGTH = 41,
		NUM_STATES = 947,

		NT_OFFSET = 108,
		SCOPE_UBOUND = 133,
		SCOPE_SIZE = 134,
		LA_STATE_OFFSET = 12035,
		MAX_LA = 1,
		NUM_RULES = 683,
		NUM_TERMINALS = 108,
		NUM_NON_TERMINALS = 303,
		NUM_SYMBOLS = 411,
		START_STATE = 998,
		EOFT_SYMBOL = 44,
		EOLT_SYMBOL = 44,
		ACCEPT_ACTION = 12034,
		ERROR_ACTION = 12035;
}