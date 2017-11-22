/*
 * @(#)Modifier.java	1.1 04/01/26
 *
 * Copyright (c) 2004, Sun Microsystems, Inc.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sun Microsystems, Inc. nor the names of
 *       its contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.sun.mirror.declaration;


/**
 * Represents a modifier on the declaration of a program element such
 * as a class, method, or field.
 *
 * <p> Not all modifiers are applicable to all kinds of declarations.
 * When two or more modifiers appear in the source code of a declaration,
 * then it is customary, though not required, that they appear in the same
 * order as the constants listed in the detail section below.
 *
 * @author Joseph D. Darcy
 * @author Scott Seligman
 * @version 1.1 04/01/25
 * @since 1.5
 */

public enum Modifier {

    // See JLS2 sections 8.1.1, 8.3.1, 8.4.3, 8.8.3, and 9.1.1.
    // java.lang.reflect.Modifier includes INTERFACE, but that's a VMism.

    /** The modifier <tt>public</tt> */		PUBLIC,
    /** The modifier <tt>protected</tt> */	PROTECTED,
    /** The modifier <tt>private</tt> */	PRIVATE,
    /** The modifier <tt>abstract</tt> */	ABSTRACT,
    /** The modifier <tt>static</tt> */		STATIC,
    /** The modifier <tt>final</tt> */		FINAL,
    /** The modifier <tt>transient</tt> */	TRANSIENT,
    /** The modifier <tt>volatile</tt> */	VOLATILE,
    /** The modifier <tt>synchronized</tt> */	SYNCHRONIZED,
    /** The modifier <tt>native</tt> */		NATIVE,
    /** The modifier <tt>strictfp</tt> */	STRICTFP;


    private String lowercase = null;	// modifier name in lowercase

    /**
     * Returns this modifier's name in lowercase.
     */
    @Override
	public String toString() {
	if (lowercase == null) {
	   lowercase = name().toLowerCase(java.util.Locale.US); 
	}
	return lowercase;
    }
}
