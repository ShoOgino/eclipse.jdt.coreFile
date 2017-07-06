/*******************************************************************************
 * Copyright (c) 2017 GK Software AG, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.env;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Interface to a module as needed to implement the updates for {@code --add-reads}
 * and {@code --add-exports} command line options.
 */
public interface IUpdatableModule {
	
	enum UpdateKind { MODULE, PACKAGE }

	/** Structure for update operations, sorted by {@link UpdateKind}. */
	class UpdatesByKind {
		List<Consumer<IUpdatableModule>> moduleUpdates = Collections.emptyList();
		List<Consumer<IUpdatableModule>> packageUpdates = Collections.emptyList();
		public List<Consumer<IUpdatableModule>> getList(UpdateKind kind, boolean create) {
			switch (kind) {
				case MODULE:
					if (this.moduleUpdates == Collections.EMPTY_LIST && create)
						this.moduleUpdates = new ArrayList<>();
					return this.moduleUpdates;
				case PACKAGE:
					if (this.packageUpdates == Collections.EMPTY_LIST && create)
						this.packageUpdates = new ArrayList<>();
					return this.packageUpdates;
				default:
					throw new IllegalArgumentException("Unknown enum value "+kind); //$NON-NLS-1$
			}
		}
	}

	/** Answer the name of the module to update. */
	char[] name();
	/** Perform an --add-reads update on the module. */
	void addReads(char[] moduleName);
	/** Perform an --add-exports update on the module. */
	void addExports(char[] packageName, char[][] targetModules);
}
