/*******************************************************************************
 * Copyright (c) 2015 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.pdom.field;

import org.eclipse.jdt.internal.core.pdom.Nd;
import org.eclipse.jdt.internal.core.pdom.db.Database;

/**
 * Declares a PDOM field of type int. Can be used in place of {@link Field<Integer>} in order to
 * avoid extra GC overhead.
 * @since 3.12
 */
public class FieldInt implements IField {
	private int offset;

	public FieldInt() {
	}

	public int get(Nd pdom, long address) {
		Database db = pdom.getDB();
		return db.getInt(address + this.offset);
	}

	public void put(Nd pdom, long address, int newValue) {
		pdom.getDB().putInt(address + this.offset, newValue);
	}

	@Override
	public void setOffset(int offset) {
		this.offset = offset; 
	}

	@Override
	public int getRecordSize() {
		return Database.INT_SIZE;
	}
}
