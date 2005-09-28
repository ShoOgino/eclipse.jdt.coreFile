/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;

public class NLSLine {

	public List elements;
	public int remainingElementsSize;

	public NLSLine() {
		this.elements = new ArrayList();
	}
	
	/**
	 * Adds a NLS element to this line.
	 */
	public void add(StringLiteral element) {
		this.elements.add(element);
		this.remainingElementsSize++;
	}
	
	public StringLiteral get(int index) {
		return (StringLiteral) this.elements.get(index);
	}
	
	public void set(int index, StringLiteral literal) {
		this.elements.set(index, literal);
		this.remainingElementsSize--;
	}
	
	public int size(){
		return this.elements.size();
	}
	
	public void clear() {
		this.elements.clear();
	}
	
	public String toString() {
		StringBuffer result= new StringBuffer();
		for (Iterator iter= iterator(); iter.hasNext(); ) {
			result.append("\t"); //$NON-NLS-1$
			result.append(iter.next().toString());
			result.append("\n"); //$NON-NLS-1$
		}
		return result.toString();
	}

	/**
	 * returns an Iterator over NLSElements
	 */
	public Iterator iterator() {
		return this.elements.iterator();
	}
}
