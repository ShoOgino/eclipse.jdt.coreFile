/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.internal.apt.pluggable.core.filer;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.apt.core.env.Phase;
import org.eclipse.jdt.apt.core.internal.generatedfile.FileGenerationResult;
import org.eclipse.jdt.apt.core.internal.generatedfile.GeneratedFileManager;
import org.eclipse.jdt.internal.apt.pluggable.core.Apt6Plugin;
import org.eclipse.jdt.internal.apt.pluggable.core.dispatch.IdeProcessingEnvImpl;

/**
 * 
 * @since 3.3
 */
public class IdeJavaSourceFileWriter extends StringWriter {
	
	private final IdeProcessingEnvImpl _env;
	private final CharSequence _name;
	private final Collection<IFile> _parentFiles;
	private boolean _closed = false;

	public IdeJavaSourceFileWriter(IdeProcessingEnvImpl env, CharSequence name, Collection<IFile> parentFiles) {
		_env = env;
		_parentFiles = parentFiles;
		_name = name;
		_env.getAptProject().getGeneratedSourceFolderManager().getFolder();
	}

	/**
	 * 
	 */
	@Override
	public void close() throws IOException {
		synchronized(this) {
			if (_closed) {
				return;
			}
			_closed = true;
		}
		try {
			GeneratedFileManager gfm = _env.getAptProject().getGeneratedFileManager();
			Phase phase = _env.getPhase();

			FileGenerationResult result = null;
			if ( phase == Phase.RECONCILE )
			{
				//TODO - implement reconcile
			}
			else if ( phase == Phase.BUILD)	{
				// TODO: Remember empty-parent situation.
				result = gfm.generateFileDuringBuild( 
						_parentFiles,  _name.toString(), this.toString(), 
						_env.currentProcessorSupportsRTTG(), null /* progress monitor */ );
			}
			if (result != null) {
				_env.addNewUnit(result);
			}
		}
		catch (CoreException ce) {
			Apt6Plugin.log(ce, "Unable to generate type when JavaSourceFilePrintWriter was closed"); //$NON-NLS-1$
		}
	}
}
