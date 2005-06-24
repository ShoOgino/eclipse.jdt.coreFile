/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BEA Systems Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.ui.internal.preferences;

import org.eclipse.jdt.apt.ui.AptUIPlugin;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

/*
 * The page to configure the naming style options.
 */
public class AptPreferencePage extends BasePreferencePage {

	private static final String PREF_ID= "org.eclipse.jdt.apt.ui.preferences.aptPreferences"; //$NON-NLS-1$
	private static final String PROP_ID= "org.eclipse.jdt.apt.ui.propertyPages.aptPreferences"; //$NON-NLS-1$

	public AptPreferencePage() {
		setPreferenceStore(AptUIPlugin.getDefault().getPreferenceStore());
		setDescription("Java annotation processing preferences:"); 
		
		// only used when page is shown programatically
		setTitle("AptPreferencePageTitle");		 
	}

	/*
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		IWorkbenchPreferenceContainer container= (IWorkbenchPreferenceContainer) getContainer();
		setConfigurationBlock(new AptConfigurationBlock(getNewStatusChangedListener(), getProject(), container));
		
		super.createControl(parent);
		//TODO: enable Help
		//PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IJavaHelpContextIds.ORGANIZE_IMPORTS_PREFERENCE_PAGE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.preferences.PropertyAndPreferencePage#getPreferencePageID()
	 */
	protected String getPreferencePageID() {
		return PREF_ID;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.preferences.PropertyAndPreferencePage#getPropertyPageID()
	 */
	protected String getPropertyPageID() {
		return PROP_ID;
	}
	
	

}



