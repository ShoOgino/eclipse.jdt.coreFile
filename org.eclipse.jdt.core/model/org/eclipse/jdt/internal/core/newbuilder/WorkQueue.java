package org.eclipse.jdt.internal.core.newbuilder;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.*;

public class WorkQueue {

// WHY does the builder not hold onto a single array list itself?
// IS there real value in knowing the compiled list?
ArrayList needsCompileList;
ArrayList compiledList;

public WorkQueue() {
	this.needsCompileList = new ArrayList(11);
	this.compiledList = new ArrayList(11);
}

public void add(String element) {
	needsCompileList.add(element);
}

public void addAll(String[] elements) {
	for (int i = 0, length = elements.length; i < length; i++)
		add(elements[i]);
}

public void clear() {
	this.needsCompileList.clear();
	this.compiledList.clear();
}	

public void finished(String element) {
	needsCompileList.remove(element);
	compiledList.add(element);
}

public boolean isCompiled(String element) {
	return compiledList.contains(element);
}

public boolean isWaiting(String element) {
	return needsCompileList.contains(element);
}

public String toString() {
	return "WorkQueue: " + needsCompileList; //$NON-NLS-1$
}
}