/*******************************************************************************
 * Copyright (c) 2002 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IPath;

public class Problem {
	private String location;
	private String message;
	private IPath resourcePath;
	
	public Problem(String location, String message, IPath resourcePath){
		this.location = location;
		this.message = message;
		this.resourcePath = resourcePath;
	}
	
	public Problem(IMarker marker){
		this.location = marker.getAttribute(IMarker.LOCATION, "");
		this.message = marker.getAttribute(IMarker.MESSAGE, "");
		this.resourcePath = marker.getResource().getFullPath();
	}
	/**
	 * Gets the location.
	 * @return Returns a String
	 */
	public String getLocation() {
		return location;
	}
	/**
	 * Gets the message.
	 * @return Returns a String
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * Gets the resourcePath.
	 * @return Returns a IPath
	 */
	public IPath getResourcePath() {
		return resourcePath;
	}
	
	public String toString(){
		return "Problem : " + message + " [ resource : <" + resourcePath + "> location <"+ location + "> ]";
	}
	
	public boolean equals(Object o){
		if(o instanceof Problem){
			return this.toString().equals(o.toString());
		}
		return false;
	}
}

