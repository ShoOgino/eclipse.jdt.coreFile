/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.formatter;

import org.eclipse.jdt.internal.formatter.comment.IJavaDocTagConstants;

/**
 * Represents a block in a {@link FormatJavadoc} which might be a
 * <b>description</b> or a <b>tag</b> (see{@link #isDescription()}).
 * </p><p>
 * The block might have a tag, a reference and nodes (see
 * {@link FormatJavadocNode}. Each of these elements might be present or not,
 * but at least one of them is.
 * </p>
 */
public class FormatJavadocBlock extends FormatJavadocNode implements IJavaDocTagConstants {
	// flags
	final static int INLINED = 0x0001;
	final static int FIRST = 0x0002;
	final static int ON_HEADER_LINE = 0x0004;
	final static int TEXT_ON_TAG_LINE = 0x0008;
	final static int ONE_LINE_TAG = 0x0010;
	final static int PARAM_TAG = 0x0020;
	final static int IN_PARAM_TAG = 0x0040;
	final static int IN_DESCRIPTION = 0x0080;
	
	// constants
	final static int MAX_TAG_HIERARCHY = 10;

	private int tagValue = NO_TAG_VALUE;
	int tagEnd;
	FormatJavadocReference reference;
	FormatJavadocNode[] nodes;
	int nodesPtr = -1;
	int flags = 0;

public FormatJavadocBlock(int start, int end, int line, int value) {
	super(start, end, line);
	this.tagValue = value;
	this.tagEnd = end;
	switch (value) {
		case TAG_PARAM_VALUE:
		// TODO why are following tags considered like @param by the formatter?
		case TAG_SERIAL_FIELD_VALUE:
		case TAG_THROWS_VALUE:
		case TAG_EXCEPTION_VALUE:
			flags |= PARAM_TAG;
	}
}

private void addNode(FormatJavadocNode node) {
	// Initialize or resize array if necessary
	if (++this.nodesPtr == 0) {
		this.nodes = new FormatJavadocNode[DEFAULT_ARRAY_SIZE];
	} else if (this.nodesPtr >= this.nodes.length) {
		System.arraycopy(
			this.nodes, 0, 
			this.nodes = new FormatJavadocNode[this.nodes.length+INCREMENT_ARRAY_SIZE], 0,
			this.nodesPtr);
	}

	// Store the node
	this.nodes[nodesPtr] = node;
	this.sourceEnd = node.sourceEnd;
}

void addBlock(FormatJavadocBlock block, int htmlLevel) {
	if (this.nodes != null) {
		FormatJavadocText[] textHierarchy = getTextHierarchy(block, htmlLevel);
		if (textHierarchy != null) {
			FormatJavadocText lastText = textHierarchy[htmlLevel];
			if (lastText != null) {
				lastText.appendNode(block);
				for (int i=htmlLevel-1; i>=0; i--) {
					textHierarchy[i].sourceEnd = block.sourceEnd;
				}
				this.sourceEnd = block.sourceEnd;
				if (isParamTag()) {
					block.flags |= IN_PARAM_TAG;
				} else if (isDescription()) {
					block.flags |= IN_DESCRIPTION;
				}
				block.flags |= INLINED;
				return;
			}
		}
	}
	addNode(block);
	if (isParamTag()) {
		block.flags |= IN_PARAM_TAG;
	} else if (isDescription()) {
		block.flags |= IN_DESCRIPTION;
	}
	block.flags |= INLINED;
}

void addText(FormatJavadocText text) {
	if (this.nodes != null) {
		FormatJavadocText[] textHierarchy = getTextHierarchy(text, text.depth);
		if (textHierarchy != null) {
			FormatJavadocText lastText = textHierarchy[text.depth];
			if (lastText != null) {
				lastText.appendText(text);
				for (int i=text.depth-1; i>=0; i--) {
					textHierarchy[i].sourceEnd = text.sourceEnd;
				}
				this.sourceEnd = text.sourceEnd;
				return;
			}
			if (text.depth > 0) {
				FormatJavadocText parentText = textHierarchy[text.depth-1];
				if (parentText != null) {
					parentText.appendText(text);
					for (int i=text.depth-2; i>=0; i--) {
						textHierarchy[i].sourceEnd = text.sourceEnd;
					}
					this.sourceEnd = text.sourceEnd;
					return;
				}
			}
		}
	}
	if (text.isHtmlTag()) {
		switch (text.getHtmlTagID()) {
			case JAVADOC_CODE_TAGS_ID:
				text.linesBefore = this.nodesPtr == -1 ? 0 : 2;
				break;
			case JAVADOC_SEPARATOR_TAGS_ID:
				text.linesBefore = 1;
				break;
//	    	case JAVADOC_BREAK_TAGS_ID:
//				if (this.nodesPtr >= 0) text.linesBefore = 1;
		}
	}
	addNode(text);
}

void clean() {
	int length = this.nodes == null ? 0 : this.nodes.length;
	if (this.nodesPtr != (length-1)) {
		System.arraycopy(this.nodes, 0, this.nodes = new FormatJavadocNode[this.nodesPtr+1], 0, this.nodesPtr+1);
	}
	for (int i=0; i<=this.nodesPtr; i++) {
		this.nodes[i].clean();
	}
}

FormatJavadocNode getLastNode() {
	if (this.nodes != null) {
		return this.nodes[this.nodesPtr];
	}
	return null;
}

/*
 * Return the text hierarchy for the given node
 */
FormatJavadocText[] getTextHierarchy(FormatJavadocNode node, int htmlDepth) {
	if (this.nodes == null) return null;
	FormatJavadocText[] textHierarchy = null;
	int ptr = 0;
	FormatJavadocText text = node.isText() ? (FormatJavadocText) node : null;
	FormatJavadocNode lastNode = this.nodes[this.nodesPtr];
	while (lastNode.isText()) {
		FormatJavadocText lastText = (FormatJavadocText) lastNode;
		int lastTagCategory = lastText.getHtmlTagID();
		boolean lastSingleTag = lastTagCategory <= JAVADOC_SINGLE_TAGS_ID;
		boolean lastTextCanHaveChildren = lastText.isHtmlTag() && !lastText.isClosingHtmlTag() && !lastSingleTag;
		if (lastText.depth == htmlDepth || // found same html tag level => use it
			lastText.htmlNodesPtr == -1) {	// no more sub-levels => add one
			// Text breakage
			if (lastText.isHtmlTag() && text != null) {
				// Set some lines before if previous was specific html tag
				if (lastText.separatorsPtr == -1 || lastText.isClosingHtmlTag()) {
					switch (lastText.getHtmlTagID()) {
						case JAVADOC_CODE_TAGS_ID:
							if (text.linesBefore < 2) {
								text.linesBefore = 2;
							}
							break;
						case JAVADOC_SEPARATOR_TAGS_ID:
				    	case JAVADOC_SINGLE_BREAK_TAG_ID:
							if (text.linesBefore < 1) {
								text.linesBefore = 1;
							}
					}
				}
				// If adding an html tag on same html tag, then close previous one and leave
				if (text.isHtmlTag() && !text.isClosingHtmlTag() && text.getHtmlTagIndex() == lastText.getHtmlTagIndex() && !lastText.isClosingHtmlTag()) {
					lastText.closeTag();
					return textHierarchy;
				}
			}
			// If we have a text after another text, keep the same level to append
			if (lastTextCanHaveChildren || (htmlDepth == 0 && !lastText.isHtmlTag() && text != null && !text.isHtmlTag())) {
				if (textHierarchy == null) textHierarchy = new FormatJavadocText[MAX_TAG_HIERARCHY];
				textHierarchy[ptr] = lastText;
				return textHierarchy;
			}
			// Last text cannot have children, so return the built hierarchy
			return textHierarchy;
		}
		if (textHierarchy == null) textHierarchy = new FormatJavadocText[MAX_TAG_HIERARCHY];
		textHierarchy[ptr++] = lastText;
		lastNode = lastText.htmlNodes[lastText.htmlNodesPtr];
	}
	return textHierarchy;
}

/**
 * Returns whether the text is on the same line of the tag or not.
 * 
 * @return <code>true</code> if the text is on the same line than the tag,
 * 	<code>false</code> otherwise.
 */
public boolean hasTextOnTagLine() {
	return (this.flags & TEXT_ON_TAG_LINE) != 0;
}

/**
 * Returns whether the block is the javadoc comment description or not.
 * The description begins after the starting delimiter and continues until the tag
 * section.
 * 
 * @return <code>true</code> if the block is the javadoc description,
 * 	<code>false</code> otherwise.
 */
public boolean isDescription() {
	return this.tagValue == NO_TAG_VALUE;
}

/**
 * Returns whether the block is the first block of the javadoc comment or not
 * (independently of the fact it's a description or not).
 * 
 * @return <code>true</code> if the block is the first of the javadoc
 * 	comment, <code>false</code> otherwise.
 */
public boolean isFirst() {
	return (this.flags & FIRST) != 0;
}

/**
 * Returns whether the first block starts on the same line than the javadoc
 * starting delimiter or not.
 * 
 * @return <code>true</code> if the the first block starts on the same line
 * 	than the javadoc starting delimiter, <code>false</code> otherwise.
 */
public boolean isHeaderLine() {
	return (this.flags & ON_HEADER_LINE) != 0;
}

/**
 * Returns whether the block is a description or inlined in a description. 
 * @see #isParamTag()
 * 
 * @return <code>true</code> if the block is a description or inlined in a
 * 	description, <code>false</code> otherwise.
 */
public boolean isInDescription() {
	return this.tagValue == NO_TAG_VALUE || (this.flags & IN_DESCRIPTION) == IN_DESCRIPTION;
}

/**
 * Returns whether the text is on the same line of the tag.
 * 
 * @return <code>true</code> if the text is on the same line than the tag,
 * 	<code>false</code> otherwise.
 */
public boolean isInlined() {
	return (this.flags & INLINED) != 0;
}

/**
 * Returns whether the block is a param tag or inlined in a param tag. 
 * @see #isParamTag()
 * 
 * @return <code>true</code> if the block is a param tag or inlined in a param
 * 	tag, <code>false</code> otherwise.
 */
public boolean isInParamTag() {
	return (this.flags & (PARAM_TAG | IN_PARAM_TAG)) != 0;
}

/**
 * Returns whether the text is on the same line of the tag.
 * 
 * @return <code>true</code> if the text is on the same line than the tag,
 * 	<code>false</code> otherwise.
 */
public boolean isOneLineTag() {
	return (this.flags & ONE_LINE_TAG) != 0;
}

/**
 * Returns whether the block is a param tag or not.  Note that this also includes
 * &#064;serialField, &#064;throws and &#064;exception tags.
 * 
 * @return <code>true</code> if the block is a param tag,
 * 	<code>false</code> otherwise.
 */
public boolean isParamTag() {
	return (this.flags & PARAM_TAG) == PARAM_TAG;
}

protected void toString(StringBuffer buffer) {
	if ((this.flags & INLINED) != 0) buffer.append('{');
	buffer.append('@');
	buffer.append(this.tagValue);
	super.toString(buffer);
	if (this.reference == null) {
		buffer.append('\n');
	} else {
		buffer.append(" ("); //$NON-NLS-1$
		this.reference.toString(buffer);
		buffer.append(")\n"); //$NON-NLS-1$
	}
	if (this.nodesPtr > -1) {
		for (int i = 0; i <= this.nodesPtr; i++) {
			this.nodes[i].toString(buffer);
		}
	}
}

public String toStringDebug(char[] source) {
	StringBuffer buffer = new StringBuffer();
	toStringDebug(buffer, source);
	return buffer.toString();
}

public void toStringDebug(StringBuffer buffer, char[] source) {
	if (this.tagValue > 0) {
		buffer.append(source, this.sourceStart, this.tagEnd-this.sourceStart+1);
		buffer.append(' ');
	}
	if (this.reference != null) {
		this.reference.toStringDebug(buffer, source);
	}
	for (int i=0; i<=this.nodesPtr; i++) {
		this.nodes[i].toStringDebug(buffer, source);
	}
}
}
