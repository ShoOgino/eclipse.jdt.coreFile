/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

/**
 * Handle returned by the selection engine representing a source field.
 * The uniqueKey contains the genericSignature of the selected field.
 */
public class SelectedSourceField extends SourceField {
	
	public String uniqueKey;
	
	/*
	 * See class comments.
	 */
	public SelectedSourceField(JavaElement parent, String name, String uniqueKey) {
		super(parent, name);
		this.uniqueKey = uniqueKey;
	}

}
