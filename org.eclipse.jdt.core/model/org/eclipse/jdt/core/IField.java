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
package org.eclipse.jdt.core;

/**
 * Represents a field declared in a type.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface IField extends IMember {
/**
 * Returns the constant value associated with this field
 * or <code>null</code> if this field has none. The field needs to be static and final to have
 * a constant value.
 * Returns an instance of the wrapper type corresponding to the the type of the field.
 * <table border="1">
 * <tr>
 * <th>field type</th>
 * <th>wrapper type</th>
 * </tr>
 * <tr>
 * <td>int
 * </td>
 * <td>java.lang.Integer
 * </td>
 * </tr>
 * <tr>
 * <td>byte
 * </td>
 * <td>java.lang.Byte
 * </td>
 * </tr>
 * <tr>
 * <td>boolean
 * </td>
 * <td>java.lang.Boolean
 * </td>
 * </tr>
 * <tr>
 * <td>char
 * </td>
 * <td>java.lang.Character
 * </td>
 * </tr>
 * <tr>
 * <td>double
 * </td>
 * <td>java.lang.Double
 * </td>
 * </tr>
 * <tr>
 * <td>float
 * </td>
 * <td>java.lang.Float
 * </td>
 * </tr>
 * <tr>
 * <td>long
 * </td>
 * <td>java.lang.Long
 * </td>
 * </tr>
 * <tr>
 * <td>short
 * </td>
 * <td>java.lang.Short
 * </td>
 * </tr>
 * <tr>
 * <td>java.lang.String
 * </td>
 * <td>java.lang.String
 * </td>
 * </tr>
 * </table>
 *
 * @return  the constant value associated with this field or <code>null</code> if this field has none.
 * @exception JavaModelException if this element does not exist or if an
 *      exception occurs while accessing its corresponding resource
 */
public Object getConstant() throws JavaModelException;
/**
 * Returns the simple name of this field.
 * @return the simple name of this field.
 */
String getElementName();
/**
 * Returns the type signature of this field.
 *
 * @see Signature
 * @return the type signature of this field.
 * @exception JavaModelException if this element does not exist or if an
 *      exception occurs while accessing its corresponding resource
 */
String getTypeSignature() throws JavaModelException;
}
