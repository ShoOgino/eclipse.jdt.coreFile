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
package org.eclipse.jdt.internal.core;

import java.util.ArrayList;

import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;

/**
 * @see IJavaElementDelta
 */
public class JavaElementDelta extends SimpleDelta implements IJavaElementDelta {
	/**
	 * @see #getAffectedChildren()
	 */
	protected IJavaElementDelta[] fAffectedChildren = fgEmptyDelta;

	/*
	 * The element that this delta describes the change to.
	 */
	protected IJavaElement changedElement;
	
	/**
	 * Collection of resource deltas that correspond to non java resources deltas.
	 */
	protected IResourceDelta[] resourceDeltas = null;

	/**
	 * Counter of resource deltas
	 */
	protected int resourceDeltasCounter;
	/**
	 * @see #getMovedFromHandle()
	 */
	protected IJavaElement fMovedFromHandle = null;
	/**
	 * @see #getMovedToHandle()
	 */
	protected IJavaElement fMovedToHandle = null;
	/**
	 * Empty array of IJavaElementDelta
	 */
	protected static  IJavaElementDelta[] fgEmptyDelta= new IJavaElementDelta[] {};
/**
 * Creates the root delta. To create the nested delta
 * hierarchies use the following convenience methods. The root
 * delta can be created at any level (for example: project, package root,
 * package fragment...).
 * <ul>
 * <li><code>added(IJavaElement)</code>
 * <li><code>changed(IJavaElement)</code>
 * <li><code>moved(IJavaElement, IJavaElement)</code>
 * <li><code>removed(IJavaElement)</code>
 * <li><code>renamed(IJavaElement, IJavaElement)</code>
 * </ul>
 */
public JavaElementDelta(IJavaElement element) {
	this.changedElement = element;
}
/**
 * Adds the child delta to the collection of affected children.  If the
 * child is already in the collection, walk down the hierarchy.
 */
protected void addAffectedChild(JavaElementDelta child) {
	switch (this.kind) {
		case ADDED:
		case REMOVED:
			// no need to add a child if this parent is added or removed
			return;
		case CHANGED:
			this.changeFlags |= F_CHILDREN;
			break;
		default:
			this.kind = CHANGED;
			this.changeFlags |= F_CHILDREN;
	}

	// if a child delta is added to a compilation unit delta or below, 
	// it's a fine grained delta
	if (this.changedElement.getElementType() >= IJavaElement.COMPILATION_UNIT) {
		this.fineGrained();
	}
	
	if (fAffectedChildren.length == 0) {
		fAffectedChildren = new IJavaElementDelta[] {child};
		return;
	}
	JavaElementDelta existingChild = null;
	int existingChildIndex = -1;
	if (fAffectedChildren != null) {
		for (int i = 0; i < fAffectedChildren.length; i++) {
			if (this.equalsAndSameParent(fAffectedChildren[i].getElement(), child.getElement())) { // handle case of two jars that can be equals but not in the same project
				existingChild = (JavaElementDelta)fAffectedChildren[i];
				existingChildIndex = i;
				break;
			}
		}
	}
	if (existingChild == null) { //new affected child
		fAffectedChildren= growAndAddToArray(fAffectedChildren, child);
	} else {
		switch (existingChild.getKind()) {
			case ADDED:
				switch (child.getKind()) {
					case ADDED: // child was added then added -> it is added
					case CHANGED: // child was added then changed -> it is added
						return;
					case REMOVED: // child was added then removed -> noop
						fAffectedChildren = this.removeAndShrinkArray(fAffectedChildren, existingChildIndex);
						return;
				}
				break;
			case REMOVED:
				switch (child.getKind()) {
					case ADDED: // child was removed then added -> it is changed
						child.kind = CHANGED;
						fAffectedChildren[existingChildIndex] = child;
						return;
					case CHANGED: // child was removed then changed -> it is removed
					case REMOVED: // child was removed then removed -> it is removed
						return;
				}
				break;
			case CHANGED:
				switch (child.getKind()) {
					case ADDED: // child was changed then added -> it is added
					case REMOVED: // child was changed then removed -> it is removed
						fAffectedChildren[existingChildIndex] = child;
						return;
					case CHANGED: // child was changed then changed -> it is changed
						IJavaElementDelta[] children = child.getAffectedChildren();
						for (int i = 0; i < children.length; i++) {
							JavaElementDelta childsChild = (JavaElementDelta) children[i];
							existingChild.addAffectedChild(childsChild);
						}
						
						// update flags
						boolean childHadContentFlag = (child.changeFlags & F_CONTENT) != 0;
						boolean existingChildHadChildrenFlag = (existingChild.changeFlags & F_CHILDREN) != 0;
						existingChild.changeFlags |= child.changeFlags;
						
						// remove F_CONTENT flag if existing child had F_CHILDREN flag set 
						// (case of fine grained delta (existing child) and delta coming from 
						// DeltaProcessor (child))
						if (childHadContentFlag && existingChildHadChildrenFlag) {
							existingChild.changeFlags &= ~F_CONTENT;
						}
						
						// add the non-java resource deltas if needed
						// note that the child delta always takes precedence over this existing child delta
						// as non-java resource deltas are always created last (by the DeltaProcessor)
						IResourceDelta[] resDeltas = child.getResourceDeltas();
						if (resDeltas != null) {
							existingChild.resourceDeltas = resDeltas;
							existingChild.resourceDeltasCounter = child.resourceDeltasCounter;
						}
						return;
				}
				break;
			default: 
				// unknown -> existing child becomes the child with the existing child's flags
				int flags = existingChild.getFlags();
				fAffectedChildren[existingChildIndex] = child;
				child.changeFlags |= flags;
		}
	}
}
/**
 * Creates the nested deltas resulting from an add operation.
 * Convenience method for creating add deltas.
 * The constructor should be used to create the root delta 
 * and then an add operation should call this method.
 */
public void added(IJavaElement element) {
	added(element, 0);
}
public void added(IJavaElement element, int flags) {
	JavaElementDelta addedDelta = new JavaElementDelta(element);
	addedDelta.added();
	addedDelta.changeFlags |= flags;
	insertDeltaTree(element, addedDelta);
}
/**
 * Adds the child delta to the collection of affected children.  If the
 * child is already in the collection, walk down the hierarchy.
 */
protected void addResourceDelta(IResourceDelta child) {
	switch (this.kind) {
		case ADDED:
		case REMOVED:
			// no need to add a child if this parent is added or removed
			return;
		case CHANGED:
			this.changeFlags |= F_CONTENT;
			break;
		default:
			this.kind = CHANGED;
			this.changeFlags |= F_CONTENT;
	}
	if (resourceDeltas == null) {
		resourceDeltas = new IResourceDelta[5];
		resourceDeltas[resourceDeltasCounter++] = child;
		return;
	}
	if (resourceDeltas.length == resourceDeltasCounter) {
		// need a resize
		System.arraycopy(resourceDeltas, 0, (resourceDeltas = new IResourceDelta[resourceDeltasCounter * 2]), 0, resourceDeltasCounter);
	}
	resourceDeltas[resourceDeltasCounter++] = child;
}
/**
 * Creates the nested deltas resulting from a change operation.
 * Convenience method for creating change deltas.
 * The constructor should be used to create the root delta 
 * and then a change operation should call this method.
 */
public void changed(IJavaElement element, int changeFlag) {
	JavaElementDelta changedDelta = new JavaElementDelta(element);
	changedDelta.changed(changeFlag);
	insertDeltaTree(element, changedDelta);
}
/**
 * Mark this delta as a content changed delta.
 */
public void contentChanged() {
	this.changeFlags |= F_CONTENT;
}
/**
 * Creates the nested deltas for a closed element.
 */
public void closed(IJavaElement element) {
	JavaElementDelta delta = new JavaElementDelta(element);
	delta.changed(F_CLOSED);
	insertDeltaTree(element, delta);
}
/**
 * Creates the nested delta deltas based on the affected element
 * its delta, and the root of this delta tree. Returns the root
 * of the created delta tree.
 */
protected JavaElementDelta createDeltaTree(IJavaElement element, JavaElementDelta delta) {
	JavaElementDelta childDelta = delta;
	ArrayList ancestors= getAncestors(element);
	if (ancestors == null) {
		if (this.equalsAndSameParent(delta.getElement(), getElement())) { // handle case of two jars that can be equals but not in the same project
			// the element being changed is the root element
			this.kind= delta.kind;
			this.changeFlags = delta.changeFlags;
			fMovedToHandle = delta.fMovedToHandle;
			fMovedFromHandle = delta.fMovedFromHandle;
		}
	} else {
		for (int i = 0, size = ancestors.size(); i < size; i++) {
			IJavaElement ancestor = (IJavaElement) ancestors.get(i);
			JavaElementDelta ancestorDelta = new JavaElementDelta(ancestor);
			ancestorDelta.addAffectedChild(childDelta);
			childDelta = ancestorDelta;
		}
	}
	return childDelta;
}
/**
 * Returns whether the two java elements are equals and have the same parent.
 */
protected boolean equalsAndSameParent(IJavaElement e1, IJavaElement e2) {
	IJavaElement parent1;
	return e1.equals(e2) && ((parent1 = e1.getParent()) != null) && parent1.equals(e2.getParent());
}
/**
 * Returns the <code>JavaElementDelta</code> for the given element
 * in the delta tree, or null, if no delta for the given element is found.
 */
protected JavaElementDelta find(IJavaElement e) {
	if (this.equalsAndSameParent(this.changedElement, e)) { // handle case of two jars that can be equals but not in the same project
		return this;
	} else {
		for (int i = 0; i < fAffectedChildren.length; i++) {
			JavaElementDelta delta = ((JavaElementDelta)fAffectedChildren[i]).find(e);
			if (delta != null) {
				return delta;
			}
		}
	}
	return null;
}
/**
 * Mark this delta as a fine-grained delta.
 */
public void fineGrained() {
	changed(F_FINE_GRAINED);
}
/**
 * @see IJavaElementDelta
 */
public IJavaElementDelta[] getAddedChildren() {
	return getChildrenOfType(ADDED);
}
/**
 * @see IJavaElementDelta
 */
public IJavaElementDelta[] getAffectedChildren() {
	return fAffectedChildren;
}
/**
 * Returns a collection of all the parents of this element up to (but
 * not including) the root of this tree in bottom-up order. If the given
 * element is not a descendant of the root of this tree, <code>null</code>
 * is returned.
 */
private ArrayList getAncestors(IJavaElement element) {
	IJavaElement parent = element.getParent();
	if (parent == null) {
		return null;
	}
	ArrayList parents = new ArrayList();
	while (!parent.equals(this.changedElement)) {
		parents.add(parent);
		parent = parent.getParent();
		if (parent == null) {
			return null;
		}
	}
	parents.trimToSize();
	return parents;
}
/**
 * @see IJavaElementDelta
 */
public IJavaElementDelta[] getChangedChildren() {
	return getChildrenOfType(CHANGED);
}
/**
 * @see IJavaElementDelta
 */
protected IJavaElementDelta[] getChildrenOfType(int type) {
	int length = fAffectedChildren.length;
	if (length == 0) {
		return new IJavaElementDelta[] {};
	}
	ArrayList children= new ArrayList(length);
	for (int i = 0; i < length; i++) {
		if (fAffectedChildren[i].getKind() == type) {
			children.add(fAffectedChildren[i]);
		}
	}

	IJavaElementDelta[] childrenOfType = new IJavaElementDelta[children.size()];
	children.toArray(childrenOfType);
	
	return childrenOfType;
}
/**
 * Returns the delta for a given element.  Only looks below this
 * delta.
 */
protected JavaElementDelta getDeltaFor(IJavaElement element) {
	if (this.equalsAndSameParent(getElement(), element)) // handle case of two jars that can be equals but not in the same project
		return this;
	if (fAffectedChildren.length == 0)
		return null;
	int childrenCount = fAffectedChildren.length;
	for (int i = 0; i < childrenCount; i++) {
		JavaElementDelta delta = (JavaElementDelta)fAffectedChildren[i];
		if (this.equalsAndSameParent(delta.getElement(), element)) { // handle case of two jars that can be equals but not in the same project
			return delta;
		} else {
			delta = delta.getDeltaFor(element);
			if (delta != null)
				return delta;
		}
	}
	return null;
}
/**
 * @see IJavaElementDelta
 */
public IJavaElement getElement() {
	return this.changedElement;
}
/**
 * @see IJavaElementDelta
 */
public IJavaElement getMovedFromElement() {
	return fMovedFromHandle;
}
/**
 * @see IJavaElementDelta
 */
public IJavaElement getMovedToElement() {
	return fMovedToHandle;
}
/**
 * @see IJavaElementDelta
 */
public IJavaElementDelta[] getRemovedChildren() {
	return getChildrenOfType(REMOVED);
}
/**
 * Return the collection of resource deltas. Return null if none.
 */
public IResourceDelta[] getResourceDeltas() {
	if (resourceDeltas == null) return null;
	if (resourceDeltas.length != resourceDeltasCounter) {
		System.arraycopy(resourceDeltas, 0, resourceDeltas = new IResourceDelta[resourceDeltasCounter], 0, resourceDeltasCounter);
	}
	return resourceDeltas;
}
/**
 * Adds the new element to a new array that contains all of the elements of the old array.
 * Returns the new array.
 */
protected IJavaElementDelta[] growAndAddToArray(IJavaElementDelta[] array, IJavaElementDelta addition) {
	IJavaElementDelta[] old = array;
	array = new IJavaElementDelta[old.length + 1];
	System.arraycopy(old, 0, array, 0, old.length);
	array[old.length] = addition;
	return array;
}
/**
 * Creates the delta tree for the given element and delta, and then
 * inserts the tree as an affected child of this node.
 */
protected void insertDeltaTree(IJavaElement element, JavaElementDelta delta) {
	JavaElementDelta childDelta= createDeltaTree(element, delta);
	if (!this.equalsAndSameParent(element, getElement())) { // handle case of two jars that can be equals but not in the same project
		addAffectedChild(childDelta);
	}
}
/**
 * Creates the nested deltas resulting from an move operation.
 * Convenience method for creating the "move from" delta.
 * The constructor should be used to create the root delta 
 * and then the move operation should call this method.
 */
public void movedFrom(IJavaElement movedFromElement, IJavaElement movedToElement) {
	JavaElementDelta removedDelta = new JavaElementDelta(movedFromElement);
	removedDelta.kind = REMOVED;
	removedDelta.changeFlags |= F_MOVED_TO;
	removedDelta.fMovedToHandle = movedToElement;
	insertDeltaTree(movedFromElement, removedDelta);
}
/**
 * Creates the nested deltas resulting from an move operation.
 * Convenience method for creating the "move to" delta.
 * The constructor should be used to create the root delta 
 * and then the move operation should call this method.
 */
public void movedTo(IJavaElement movedToElement, IJavaElement movedFromElement) {
	JavaElementDelta addedDelta = new JavaElementDelta(movedToElement);
	addedDelta.kind = ADDED;
	addedDelta.changeFlags |= F_MOVED_FROM;
	addedDelta.fMovedFromHandle = movedFromElement;
	insertDeltaTree(movedToElement, addedDelta);
}
/**
 * Creates the nested deltas for an opened element.
 */
public void opened(IJavaElement element) {
	JavaElementDelta delta = new JavaElementDelta(element);
	delta.changed(F_OPENED);
	insertDeltaTree(element, delta);
}
/**
 * Removes the child delta from the collection of affected children.
 */
protected void removeAffectedChild(JavaElementDelta child) {
	int index = -1;
	if (fAffectedChildren != null) {
		for (int i = 0; i < fAffectedChildren.length; i++) {
			if (this.equalsAndSameParent(fAffectedChildren[i].getElement(), child.getElement())) { // handle case of two jars that can be equals but not in the same project
				index = i;
				break;
			}
		}
	}
	if (index >= 0) {
		fAffectedChildren= removeAndShrinkArray(fAffectedChildren, index);
	}
}
/**
 * Removes the element from the array.
 * Returns the a new array which has shrunk.
 */
protected IJavaElementDelta[] removeAndShrinkArray(IJavaElementDelta[] old, int index) {
	IJavaElementDelta[] array = new IJavaElementDelta[old.length - 1];
	if (index > 0)
		System.arraycopy(old, 0, array, 0, index);
	int rest = old.length - index - 1;
	if (rest > 0)
		System.arraycopy(old, index + 1, array, index, rest);
	return array;
}
/**
 * Creates the nested deltas resulting from an delete operation.
 * Convenience method for creating removed deltas.
 * The constructor should be used to create the root delta 
 * and then the delete operation should call this method.
 */
public void removed(IJavaElement element) {
	removed(element, 0);
}
public void removed(IJavaElement element, int flags) {
	JavaElementDelta removedDelta= new JavaElementDelta(element);
	insertDeltaTree(element, removedDelta);
	JavaElementDelta actualDelta = getDeltaFor(element);
	if (actualDelta != null) {
		actualDelta.removed();
		actualDelta.changeFlags |= flags;
		actualDelta.fAffectedChildren = fgEmptyDelta;
	}
}
/**
 * Creates the nested deltas resulting from a change operation.
 * Convenience method for creating change deltas.
 * The constructor should be used to create the root delta 
 * and then a change operation should call this method.
 */
public void sourceAttached(IJavaElement element) {
	JavaElementDelta attachedDelta = new JavaElementDelta(element);
	attachedDelta.changed(F_SOURCEATTACHED);
	insertDeltaTree(element, attachedDelta);
}
/**
 * Creates the nested deltas resulting from a change operation.
 * Convenience method for creating change deltas.
 * The constructor should be used to create the root delta 
 * and then a change operation should call this method.
 */
public void sourceDetached(IJavaElement element) {
	JavaElementDelta detachedDelta = new JavaElementDelta(element);
	detachedDelta.changed(F_SOURCEDETACHED);
	insertDeltaTree(element, detachedDelta);
}
/** 
 * Returns a string representation of this delta's
 * structure suitable for debug purposes.
 *
 * @see #toString()
 */
public String toDebugString(int depth) {
	StringBuffer buffer = new StringBuffer();
	for (int i= 0; i < depth; i++) {
		buffer.append('\t');
	}
	buffer.append(((JavaElement)getElement()).toDebugString());
	toDebugString(buffer);
	IJavaElementDelta[] children = getAffectedChildren();
	if (children != null) {
		for (int i = 0; i < children.length; ++i) {
			buffer.append("\n"); //$NON-NLS-1$
			buffer.append(((JavaElementDelta) children[i]).toDebugString(depth + 1));
		}
	}
	for (int i = 0; i < resourceDeltasCounter; i++) {
		buffer.append("\n");//$NON-NLS-1$
		for (int j = 0; j < depth+1; j++) {
			buffer.append('\t');
		}
		IResourceDelta resourceDelta = resourceDeltas[i];
		buffer.append(resourceDelta.toString());
		buffer.append("["); //$NON-NLS-1$
		switch (resourceDelta.getKind()) {
			case IResourceDelta.ADDED :
				buffer.append('+');
				break;
			case IResourceDelta.REMOVED :
				buffer.append('-');
				break;
			case IResourceDelta.CHANGED :
				buffer.append('*');
				break;
			default :
				buffer.append('?');
				break;
		}
		buffer.append("]"); //$NON-NLS-1$
	}
	return buffer.toString();
}
protected boolean toDebugString(StringBuffer buffer, int flags) {
	boolean prev = super.toDebugString(buffer, flags);

	if ((flags & IJavaElementDelta.F_CHILDREN) != 0) {
		if (prev)
			buffer.append(" | "); //$NON-NLS-1$
		buffer.append("CHILDREN"); //$NON-NLS-1$
		prev = true;
	}
	if ((flags & IJavaElementDelta.F_CONTENT) != 0) {
		if (prev)
			buffer.append(" | "); //$NON-NLS-1$
		buffer.append("CONTENT"); //$NON-NLS-1$
		prev = true;
	}
	if ((flags & IJavaElementDelta.F_MOVED_FROM) != 0) {
		if (prev)
			buffer.append(" | "); //$NON-NLS-1$
		buffer.append("MOVED_FROM(" + ((JavaElement)getMovedFromElement()).toStringWithAncestors() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		prev = true;
	}
	if ((flags & IJavaElementDelta.F_MOVED_TO) != 0) {
		if (prev)
			buffer.append(" | "); //$NON-NLS-1$
		buffer.append("MOVED_TO(" + ((JavaElement)getMovedToElement()).toStringWithAncestors() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		prev = true;
	}
	if ((flags & IJavaElementDelta.F_ADDED_TO_CLASSPATH) != 0) {
		if (prev)
			buffer.append(" | "); //$NON-NLS-1$
		buffer.append("ADDED TO CLASSPATH"); //$NON-NLS-1$
		prev = true;
	}
	if ((flags & IJavaElementDelta.F_REMOVED_FROM_CLASSPATH) != 0) {
		if (prev)
			buffer.append(" | "); //$NON-NLS-1$
		buffer.append("REMOVED FROM CLASSPATH"); //$NON-NLS-1$
		prev = true;
	}
	if ((flags & IJavaElementDelta.F_REORDER) != 0) {
		if (prev)
			buffer.append(" | "); //$NON-NLS-1$
		buffer.append("REORDERED"); //$NON-NLS-1$
		prev = true;
	}
	if ((flags & IJavaElementDelta.F_ARCHIVE_CONTENT_CHANGED) != 0) {
		if (prev)
			buffer.append(" | "); //$NON-NLS-1$
		buffer.append("ARCHIVE CONTENT CHANGED"); //$NON-NLS-1$
		prev = true;
	}
	if ((flags & IJavaElementDelta.F_SOURCEATTACHED) != 0) {
		if (prev)
			buffer.append(" | "); //$NON-NLS-1$
		buffer.append("SOURCE ATTACHED"); //$NON-NLS-1$
		prev = true;
	}
	if ((flags & IJavaElementDelta.F_SOURCEDETACHED) != 0) {
		if (prev)
			buffer.append(" | "); //$NON-NLS-1$
		buffer.append("SOURCE DETACHED"); //$NON-NLS-1$
		prev = true;
	}
	if ((flags & IJavaElementDelta.F_FINE_GRAINED) != 0) {
		if (prev)
			buffer.append(" | "); //$NON-NLS-1$
		buffer.append("FINE GRAINED"); //$NON-NLS-1$
		prev = true;
	}
	if ((flags & IJavaElementDelta.F_PRIMARY_WORKING_COPY) != 0) {
		if (prev)
			buffer.append(" | "); //$NON-NLS-1$
		buffer.append("PRIMARY WORKING COPY"); //$NON-NLS-1$
		prev = true;
	}
	return prev;
}
/** 
 * Returns a string representation of this delta's
 * structure suitable for debug purposes.
 */
public String toString() {
	return toDebugString(0);
}
}
