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

/*An interface that contains static declarations for some basic information
 about the parser such as the number of rules in the grammar, the starting state, etc...*/
public interface ParserBasicInformation {

	public final static int

	ERROR_SYMBOL = 132,
					MAX_NAME_LENGTH = 41,
					NUM_STATES = 1192,

					NT_OFFSET = 132,
					SCOPE_UBOUND = 307,
					SCOPE_SIZE = 308,
					LA_STATE_OFFSET = 17640,
					MAX_LA = 1,
					NUM_RULES = 894,
					NUM_TERMINALS = 132,
					NUM_NON_TERMINALS = 413,
					NUM_SYMBOLS = 545,
					START_STATE = 940,
					EOFT_SYMBOL = 62,
					EOLT_SYMBOL = 62,
					ACCEPT_ACTION = 17639,
					ERROR_ACTION = 17640;
}
