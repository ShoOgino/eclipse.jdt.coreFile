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
package org.eclipse.jdt.core.jdom;

/**
 * Represents an import declaration.
 * The corresponding syntactic unit is ImportDeclaration (JLS2 7.5).
 * An import has no children and its parent is a compilation unit.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * @deprecated The JDOM was made obsolete by the addition in 2.0 of the more
 * powerful, fine-grained DOM/AST API found in the 
 * org.eclipse.jdt.core.dom package.
 */
public interface IDOMImport extends IDOMNode {
/**
 * The <code>IDOMImport</code> refinement of this <code>IDOMNode</code>
 * method returns the name of this import. The syntax for an import name 
 * corresponds to a fully qualified type name, or to an on-demand package name
 * as defined by ImportDeclaration (JLS2 7.5).
 * 
 * @return  the name of this import
 */
public String getName();
/**
 * Returns whether this import declaration ends with <code>".*"</code>.
 *
 * @return <code>true</code> if this in an on-demand import
 */
public boolean isOnDemand();

/**
 * Returns the modifier flags for this import. The flags can be examined using class
 * <code>Flags</code>. Only the static flag is meaningful for import declarations.
 * @return the modifier flags for this import
 * @see org.eclipse.jdt.core.Flags
 * @since 3.0
 */
int getFlags();

/**
 * Sets the modifier flags for this import. The flags can be examined using class
 * <code>Flags</code>. Only the static flag is meaningful for import declarations.
 * <p>
 * Note: Static imports are an experimental language feature 
 * under discussion in JSR-201 and under consideration for inclusion
 * in the 1.5 release of J2SE. The support here is therefore tentative
 * and subject to change.
 * </p>
 * @param flags the modifier flags for this import
 * @see org.eclipse.jdt.core.Flags
 * @since 3.0
 */
void setFlags(int flags);

/**
 * The <code>IDOMImport</code> refinement of this <code>IDOMNode</code>
 * method sets the name of this import. The syntax for an import name 
 * corresponds to a fully qualified type name, or to an on-demand package name
 * as defined by ImportDeclaration (JLS2 7.5).
 *
 * @param name the given name
 * @exception IllegalArgumentException if <code>null</code> is specified
 */
public void setName(String name);
}
