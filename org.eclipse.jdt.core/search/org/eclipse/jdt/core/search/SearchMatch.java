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
package org.eclipse.jdt.core.search;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.core.JavaElement;

/**
 * A search match represents the result of a search query.
 * 
 * Search matches may be accurate (<code>A_ACCURATE</code>) or they might be
 * merely potential matches (<code>A_INACCURATE</code>). The latter occurs when
 * a compile-time problem prevents the search engine from completely resolving
 * the match.
 * </p>
 * <p>
 * This class is intended to be instantiated and subclassed by clients.
 * </p>
 * 
 * @see SearchEngine#search(SearchPattern, SearchParticipant[], IJavaSearchScope, SearchRequestor, org.eclipse.core.runtime.IProgressMonitor)
 * @since 3.0
 */
public class SearchMatch {
	
	/**
	 * The search result corresponds an exact match of the search pattern.
	 */
	public static final int A_ACCURATE = 0;

	/**
	 * The search result is potentially a match for the search pattern,
	 * but the search engine is unable to fully check it (for example, because
	 * there are errors in the code or the classpath are not correctly set).
	 */
	public static final int A_INACCURATE = 1;

	/**
	 * The search result match and search pattern are compatible.
	 * Note this is always the case when either pattern or reference is a raw type.
	 * Example:
	 * <ul>
	 * 	<li>search pattern: <code>List&lt;Exception&gt;</code></li>
	 * 	<li>match:
	 * 		<ul>
	 * 		<li><code>List&lt;? extends Throwable&gt;</code></li>
	 * 		<li><code>List&lt;? super RuntimeException&gt;</code></li>
	 * 		<li><code>List&lt;?&gt;</code></li>
	 * 	</li>
	 * 	</ul>
	 * @since 3.1
	 */
	public static final int A_COMPATIBLE = 2;

	/**
	 * The search result match and search pattern has only common erasure.
	 * Note this is always the case when either pattern or reference is a raw type.
	 * Example:
	 * 	<ul>
	 * 	<li>search pattern: <code>List&lt;Exception&gt;</code></li>
	 * 	<li>match: <code>List&lt;Object&gt;</code></li>
	 * 	</ul>
	 * @since 3.1
	 */
	public static final int A_ERASURE = 4;

	/**
	 * Rule for raw match: compatible *and* erasure.
	 * @since 3.1
	 */
	public static final int RAW_MASK = A_COMPATIBLE + A_ERASURE;

	/**
	 * Mask used to get rule signifigant bits.
	 * @since 3.1
	 */
	public static final int RULE_MASK = RAW_MASK; // no other values for the while...
	
	private Object element;
	private int length;
	private int offset;

	private int accuracy;
	private SearchParticipant participant;	
	private IResource resource;

	private boolean insideDocComment = false;
	
	// store the rule used while reporting the match
	private int rule;

	/**
	 * Creates a new search match.
	 * <p>
	 * Note that <code>isInsideDocComment()</code> defaults to false.
	 * </p>
	 * 
	 * @param element the element that encloses or corresponds to the match,
	 * or <code>null</code> if none
	 * @param accuracy one of {@link #A_ACCURATE} or {@link #A_INACCURATE}
	 * @param offset the offset the match starts at, or -1 if unknown
	 * @param length the length of the match, or -1 if unknown
	 * @param participant the search participant that created the match
	 * @param resource the resource of the element, or <code>null</code> if none
	 */
	public SearchMatch(
			IJavaElement element,
			int accuracy,
			int offset,  
			int length,
			SearchParticipant participant, 
			IResource resource) {
		this.element = element;
		this.offset = offset;
		this.length = length;
		this.accuracy = accuracy;
		this.participant = participant;
		this.resource = resource;
	}

	/**
	 * Returns the accuracy of this search match.
	 * 
	 * @return one of {@link #A_ACCURATE} or {@link #A_INACCURATE}
	 */
	public final int getAccuracy() {
		return this.accuracy;
	}

	/**
	 * Returns the element of this search match.
	 * In case of a reference match, this is the inner-most enclosing element of the reference.
	 * In case of a declaration match, this is the declaration.
	 * 
	 * @return the element of the search match, or <code>null</code> if none
	 */
	public final Object getElement() {
		return this.element;
	}

	/**
	 * Returns the length of this search match.
	 * 
	 * @return the length of this search match, or -1 if unknown
	 */
	public final int getLength() {
		return this.length;
	}
	
	/**
	 * Returns the offset of this search match.
	 * 
	 * @return the offset of this search match, or -1 if unknown
	 */
	public final int getOffset() {
		return this.offset;
	}
	
	/**
	 * Returns the search participant which issued this search match.
	 * 
	 * @return the participant which issued this search match
	 */
	public final SearchParticipant getParticipant() {
		return this.participant;
	}
	
	/**
	 * Returns the resource containing this search match.
	 * 
	 * @return the resource of the match, or <code>null</code> if none
	 */
	public final IResource getResource() {
		return this.resource;
	}

	/**
	 * Returns the rule used while creating the match.
	 * 
	 * @return the rule of the match
	 * @since 3.1
	 * TODO (frederic) currently only set to A_COMPATIBLE, A_ERASURE. Should be finalized for M5.
	 */
	public final int getRule() {
		return rule;
	}

	/**
	 * Returns whether this search match is inside a doc comment of a Java
	 * source file.
	 * 
	 * @return <code>true</code> if this search match is inside a doc
	 * comment, and <code>false</code> otherwise
	 */
	public final boolean isInsideDocComment() {
		// default is outside a doc comment
		return this.insideDocComment;
	}

	/**
	 * Sets the accuracy of this match.
	 * 
	 * @param accuracy one of {@link #A_ACCURATE} or {@link #A_INACCURATE}
	 */
	public final void setAccuracy (int accuracy) {
		this.accuracy = accuracy;
	}

	/**
	 * Sets the element of this search match.
	 * 
	 * @param element the element that encloses or corresponds to the match,
	 * or <code>null</code> if none
	 */
	public final void setElement (Object element) {
		this.element = element;
	}

	/**
	 * Sets whether this search match is inside a doc comment of a Java
	 * source file.
	 * 
	 * @param insideDoc <code>true</code> if this search match is inside a doc
	 * comment, and <code>false</code> otherwise
	 */
	public final void setInsideDocComment (boolean insideDoc) {
		this.insideDocComment = insideDoc;
	}

	/**
	 * Sets the length of this search match.
	 * 
	 * @param length the length of the match, or -1 if unknown
	 */
	public final void setLength(int length) {
		this.length = length;
	}
	
	/**
	 * Sets the offset of this search match.
	 * 
	 * @param offset the offset the match starts at, or -1 if unknown
	 */
	public final void setOffset(int offset) {
		this.offset = offset;
	}

	/**
	 * Sets the participant of this match.
	 * 
	 * @param participant the search participant that created this match
	 */
	public final void setParticipant (SearchParticipant participant) {
		this.participant = participant;
	}

	/**
	 * Sets the resource of this match.
	 * 
	 * @param resource the resource of the match, or <code>null</code> if none
	 */
	public final void setResource (IResource resource) {
		this.resource = resource;
	}

	/**
	 * Returns the rule used while creating the match.
	 * 
	 * @param rule The rule to set.
	 * @since 3.1
	 */
	public final void setRule(int rule) {
		this.rule = rule;
	}

	/* (non-javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Search match"); //$NON-NLS-1$
		buffer.append("\n  accuracy="); //$NON-NLS-1$
		buffer.append(this.accuracy == A_ACCURATE ? "ACCURATE" : "INACCURATE"); //$NON-NLS-1$ //$NON-NLS-2$
		buffer.append("\n  offset="); //$NON-NLS-1$
		buffer.append(this.offset);
		buffer.append("\n  length="); //$NON-NLS-1$
		buffer.append(this.length);
		if (this.element != null) {
			buffer.append("\n  element="); //$NON-NLS-1$
			buffer.append(((JavaElement)getElement()).toStringWithAncestors());
		}
		buffer.append("\n"); //$NON-NLS-1$
		return buffer.toString();
	}
}
