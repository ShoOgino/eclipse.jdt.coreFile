/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.hierarchy;

import java.util.*;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObjectToInt;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.core.search.IndexQueryRequestor;
import org.eclipse.jdt.internal.core.search.JavaSearchParticipant;
import org.eclipse.jdt.internal.core.search.SubTypeSearchJob;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;
import org.eclipse.jdt.internal.core.search.matching.MatchLocator;
import org.eclipse.jdt.internal.core.search.matching.SuperTypeReferencePattern;
import org.eclipse.jdt.internal.core.util.HandleFactory;
import org.eclipse.jdt.internal.core.util.Util;

public class IndexBasedHierarchyBuilder extends HierarchyBuilder implements SuffixConstants {
	public static final int MAXTICKS = 800; // heuristic so that there still progress for deep hierachies
	/**
	 * A temporary cache of compilation units to handles to speed info
	 * to handle translation - it only contains the entries
	 * for the types in the region (in other words, it contains no supertypes outside
	 * the region).
	 */
	protected Map cuToHandle;

	/**
	 * The scope this hierarchy builder should restrain results to.
	 */
	protected IJavaSearchScope scope;

	/**
	 * Cache used to record binaries recreated from index matches
	 */
	protected Map binariesFromIndexMatches;
	
	/**
	 * Collection used to queue subtype index queries
	 */
	static class Queue {
		public char[][] names = new char[10][];
		public int start = 0;
		public int end = -1;
		public void add(char[] name){
			if (++this.end == this.names.length){
				this.end -= this.start;
				System.arraycopy(this.names, this.start, this.names = new char[this.end*2][], 0, this.end);
				this.start = 0;
			}
			this.names[this.end] = name;
		}
		public char[] retrieve(){
			if (this.start > this.end) return null; // none
			
			char[] name = this.names[this.start++];
			if (this.start > this.end){
				this.start = 0;
				this.end = -1;
			}
			return name;
		}
		public String toString(){
			StringBuffer buffer = new StringBuffer("Queue:\n"); //$NON-NLS-1$
			for (int i = this.start; i <= this.end; i++){
				buffer.append(this.names[i]).append('\n');		
			}
			return buffer.toString();
		}
	}
public IndexBasedHierarchyBuilder(TypeHierarchy hierarchy, IJavaSearchScope scope) throws JavaModelException {
	super(hierarchy);
	this.cuToHandle = new HashMap(5);
	this.binariesFromIndexMatches = new HashMap(10);
	this.scope = scope;
}
public void build(boolean computeSubtypes) {
	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	try {
		// optimize access to zip files while building hierarchy
		manager.cacheZipFiles();
				
		if (computeSubtypes) {
			// Note by construction there always is a focus type here
			IType focusType = getType();
			boolean focusIsObject = focusType.getElementName().equals(new String(IIndexConstants.OBJECT));
			int amountOfWorkForSubtypes = focusIsObject ? 5 : 80; // percentage of work needed to get possible subtypes
			IProgressMonitor possibleSubtypesMonitor = 
				this.hierarchy.progressMonitor == null ? 
					null : 
					new SubProgressMonitor(this.hierarchy.progressMonitor, amountOfWorkForSubtypes);
			HashSet localTypes = new HashSet(10); // contains the paths that have potential subtypes that are local/anonymous types
			String[] allPossibleSubtypes;
			if (((Member)focusType).getOuterMostLocalContext() == null) {
				// top level or member type
				allPossibleSubtypes = this.determinePossibleSubTypes(localTypes, possibleSubtypesMonitor);
			} else {
				// local or anonymous type
				allPossibleSubtypes = CharOperation.NO_STRINGS;
			}
			if (allPossibleSubtypes != null) {
				IProgressMonitor buildMonitor = 
					this.hierarchy.progressMonitor == null ? 
						null : 
						new SubProgressMonitor(this.hierarchy.progressMonitor, 100 - amountOfWorkForSubtypes);
				this.hierarchy.initialize(allPossibleSubtypes.length);
				buildFromPotentialSubtypes(allPossibleSubtypes, localTypes, buildMonitor);
			}
		} else {
			this.hierarchy.initialize(1);
			this.buildSupertypes();
		}
	} finally {
		manager.flushZipFiles();
	}
}
private void buildForProject(JavaProject project, ArrayList potentialSubtypes, org.eclipse.jdt.core.ICompilationUnit[] workingCopies, HashSet localTypes, IProgressMonitor monitor) throws JavaModelException {
	// resolve
	int openablesLength = potentialSubtypes.size();
	if (openablesLength > 0) {
		// copy vectors into arrays
		Openable[] openables = new Openable[openablesLength];
		potentialSubtypes.toArray(openables);

		// sort in the order of roots and in reverse alphabetical order for .class file
		// since requesting top level types in the process of caching an enclosing type is
		// not supported by the lookup environment
		IPackageFragmentRoot[] roots = project.getPackageFragmentRoots();
		int rootsLength = roots.length;
		final HashtableOfObjectToInt indexes = new HashtableOfObjectToInt(openablesLength);
		for (int i = 0; i < openablesLength; i++) {
			IJavaElement root = openables[i].getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
			int index;
			for (index = 0; index < rootsLength; index++) {
				if (roots[index].equals(root))
					break;
			}		
			indexes.put(openables[i], index);
		}
		Arrays.sort(openables, new Comparator() {
			public int compare(Object a, Object b) {
				int aIndex = indexes.get(a);
				int bIndex = indexes.get(b);
				if (aIndex != bIndex)
					return aIndex - bIndex;
				return ((Openable) b).getElementName().compareTo(((Openable) a).getElementName());
			}
		});
		
		IType focusType = this.getType();
		boolean inProjectOfFocusType = focusType != null && focusType.getJavaProject().equals(project);
		org.eclipse.jdt.core.ICompilationUnit[] unitsToLookInside = null;
		if (inProjectOfFocusType) {
			org.eclipse.jdt.core.ICompilationUnit unitToLookInside = focusType.getCompilationUnit();
			if (unitToLookInside != null) {
				int wcLength = workingCopies == null ? 0 : workingCopies.length;
				if (wcLength == 0) {
					unitsToLookInside = new org.eclipse.jdt.core.ICompilationUnit[] {unitToLookInside};
				} else {
					unitsToLookInside = new org.eclipse.jdt.core.ICompilationUnit[wcLength+1];
					unitsToLookInside[0] = unitToLookInside;
					System.arraycopy(workingCopies, 0, unitsToLookInside, 1, wcLength);
				}
			} else {
				unitsToLookInside = workingCopies;
			}
		}

		SearchableEnvironment searchableEnvironment = project.newSearchableNameEnvironment(unitsToLookInside);
		this.nameLookup = searchableEnvironment.nameLookup;
		Map options = project.getOptions(true);
		// disable task tags to speed up parsing
		options.put(JavaCore.COMPILER_TASK_TAGS, ""); //$NON-NLS-1$
		this.hierarchyResolver = 
			new HierarchyResolver(searchableEnvironment, options, this, new DefaultProblemFactory());
		if (focusType != null) {
			Member declaringMember = ((Member)focusType).getOuterMostLocalContext();
			if (declaringMember == null) {
				// top level or member type
				if (!inProjectOfFocusType) {
					char[] typeQualifiedName = focusType.getTypeQualifiedName('.').toCharArray();
					String[] packageName = ((PackageFragment) focusType.getPackageFragment()).names;
					if (searchableEnvironment.findType(typeQualifiedName, Util.toCharArrays(packageName)) == null) {
						// focus type is not visible in this project: no need to go further
						return;
					}
				}
			} else {
				// local or anonymous type
				Openable openable;
				if (declaringMember.isBinary()) {
					openable = (Openable)declaringMember.getClassFile();
				} else {
					openable = (Openable)declaringMember.getCompilationUnit();
				}
				localTypes = new HashSet();
				localTypes.add(openable.getPath().toString());
				this.hierarchyResolver.resolve(new Openable[] {openable}, localTypes, monitor);
				return;
			}
		}
		this.hierarchyResolver.resolve(openables, localTypes, monitor);
	}
}
/**
 * Configure this type hierarchy based on the given potential subtypes.
 */
private void buildFromPotentialSubtypes(String[] allPotentialSubTypes, HashSet localTypes, IProgressMonitor monitor) {
	IType focusType = this.getType();
		
	// substitute compilation units with working copies
	HashMap wcPaths = new HashMap(); // a map from path to working copies
	int wcLength;
	org.eclipse.jdt.core.ICompilationUnit[] workingCopies = this.hierarchy.workingCopies;
	if (workingCopies != null && (wcLength = workingCopies.length) > 0) {
		String[] newPaths = new String[wcLength];
		for (int i = 0; i < wcLength; i++) {
			org.eclipse.jdt.core.ICompilationUnit workingCopy = workingCopies[i];
			String path = workingCopy.getPath().toString();
			wcPaths.put(path, workingCopy);
			newPaths[i] = path;
		}
		int potentialSubtypesLength = allPotentialSubTypes.length;
		System.arraycopy(allPotentialSubTypes, 0, allPotentialSubTypes = new String[potentialSubtypesLength+wcLength], 0, potentialSubtypesLength);
		System.arraycopy(newPaths, 0, allPotentialSubTypes, potentialSubtypesLength, wcLength);
	}
			
	int length = allPotentialSubTypes.length;

	// inject the compilation unit of the focus type (so that types in
	// this cu have special visibility permission (this is also usefull
	// when the cu is a working copy)
	Openable focusCU = (Openable)focusType.getCompilationUnit();
	String focusPath = null;
	if (focusCU != null) {
		focusPath = focusCU.getPath().toString();
		if (length > 0) {
			System.arraycopy(allPotentialSubTypes, 0, allPotentialSubTypes = new String[length+1], 0, length);
			allPotentialSubTypes[length] = focusPath;	
		} else {
			allPotentialSubTypes = new String[] {focusPath};
		}
		length++;
	}
	
	/*
	 * Sort in alphabetical order so that potential subtypes are grouped per project
	 */
	Arrays.sort(allPotentialSubTypes);

	ArrayList potentialSubtypes = new ArrayList();
	try {
		// create element infos for subtypes
		HandleFactory factory = new HandleFactory();
		IJavaProject currentProject = null;
		if (monitor != null) monitor.beginTask("", length*2 /* 1 for build binding, 1 for connect hierarchy*/); //$NON-NLS-1$
		for (int i = 0; i < length; i++) {
			try {
				String resourcePath = allPotentialSubTypes[i];
				
				// skip duplicate paths (e.g. if focus path was injected when it was already a potential subtype)
				if (i > 0 && resourcePath.equals(allPotentialSubTypes[i-1])) continue;
				
				Openable handle;
				org.eclipse.jdt.core.ICompilationUnit workingCopy = (org.eclipse.jdt.core.ICompilationUnit)wcPaths.get(resourcePath);
				if (workingCopy != null) {
					handle = (Openable)workingCopy;
				} else {
					handle = 
						resourcePath.equals(focusPath) ? 
							focusCU :
							factory.createOpenable(resourcePath, this.scope);
					if (handle == null) continue; // match is outside classpath
				}
				
				IJavaProject project = handle.getJavaProject();
				if (currentProject == null) {
					currentProject = project;
					potentialSubtypes = new ArrayList(5);
				} else if (!currentProject.equals(project)) {
					// build current project
					this.buildForProject((JavaProject)currentProject, potentialSubtypes, workingCopies, localTypes, monitor);
					currentProject = project;
					potentialSubtypes = new ArrayList(5);
				}
				
				potentialSubtypes.add(handle);
			} catch (JavaModelException e) {
				continue;
			}
		}
		
		// build last project
		try {
			if (currentProject == null) {
				// case of no potential subtypes
				currentProject = focusType.getJavaProject();
				if (focusType.isBinary()) {
					potentialSubtypes.add(focusType.getClassFile());
				} else {
					potentialSubtypes.add(focusType.getCompilationUnit());
				}
			}
			this.buildForProject((JavaProject)currentProject, potentialSubtypes, workingCopies, localTypes, monitor);
		} catch (JavaModelException e) {
			// ignore
		}
		
		// Compute hierarchy of focus type if not already done (case of a type with potential subtypes that are not real subtypes)
		if (!this.hierarchy.contains(focusType)) {
			try {
				currentProject = focusType.getJavaProject();
				potentialSubtypes = new ArrayList();
				if (focusType.isBinary()) {
					potentialSubtypes.add(focusType.getClassFile());
				} else {
					potentialSubtypes.add(focusType.getCompilationUnit());
				}
				this.buildForProject((JavaProject)currentProject, potentialSubtypes, workingCopies, localTypes, monitor);
			} catch (JavaModelException e) {
				// ignore
			}
		}
		
		// Add focus if not already in (case of a type with no explicit super type)
		if (!this.hierarchy.contains(focusType)) {
			this.hierarchy.addRootClass(focusType);
		}
	} finally {
		if (monitor != null) monitor.done();
	}
}
protected ICompilationUnit createCompilationUnitFromPath(Openable handle, IFile file) {
	ICompilationUnit unit = super.createCompilationUnitFromPath(handle, file);
	this.cuToHandle.put(unit, handle);
	return unit;
}
protected IBinaryType createInfoFromClassFile(Openable classFile, IResource file) {
	String documentPath = classFile.getPath().toString();
	IBinaryType binaryType = (IBinaryType)this.binariesFromIndexMatches.get(documentPath);
	if (binaryType != null) {
		this.infoToHandle.put(binaryType, classFile);
		return binaryType;
	} else {
		return super.createInfoFromClassFile(classFile, file);
	}
}
protected IBinaryType createInfoFromClassFileInJar(Openable classFile) {
	String filePath = (((ClassFile)classFile).getType().getFullyQualifiedName('$')).replace('.', '/') + SuffixConstants.SUFFIX_STRING_class;
	IPackageFragmentRoot root = classFile.getPackageFragmentRoot();
	IPath path = root.getPath();
	// take the OS path for external jars, and the forward slash path for internal jars
	String rootPath = path.getDevice() == null ? path.toString() : path.toOSString();
	String documentPath = rootPath + IJavaSearchScope.JAR_FILE_ENTRY_SEPARATOR + filePath;
	IBinaryType binaryType = (IBinaryType)this.binariesFromIndexMatches.get(documentPath);
	if (binaryType != null) {
		this.infoToHandle.put(binaryType, classFile);
		return binaryType;
	} else {
		return super.createInfoFromClassFileInJar(classFile);
	}
}
/**
 * Returns all of the possible subtypes of this type hierarchy.
 * Returns null if they could not be determine.
 */
private String[] determinePossibleSubTypes(final HashSet localTypes, IProgressMonitor monitor) {

	class PathCollector implements IPathRequestor {
		HashSet paths = new HashSet(10);
		public void acceptPath(String path, boolean containsLocalTypes) {
			this.paths.add(path);
			if (containsLocalTypes) {
				localTypes.add(path);
			}
		}
	}
	PathCollector collector = new PathCollector();
	
	try {
		if (monitor != null) monitor.beginTask("", MAXTICKS); //$NON-NLS-1$
		searchAllPossibleSubTypes(
			this.getType(),
			this.scope,
			this.binariesFromIndexMatches,
			collector,
			IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
			monitor);
	} finally {
		if (monitor != null) monitor.done();
	}

	HashSet paths = collector.paths;
	int length = paths.size();
	String[] result = new String[length];
	int count = 0;
	for (Iterator iter = paths.iterator(); iter.hasNext();) {
		result[count++] = (String) iter.next();
	} 
	return result;
}

/**
 * Find the set of candidate subtypes of a given type.
 *
 * The requestor is notified of super type references (with actual path of
 * its occurrence) for all types which are potentially involved inside a particular
 * hierarchy.
 * The match locator is not used here to narrow down the results, the type hierarchy
 * resolver is rather used to compute the whole hierarchy at once.
 * @param type
 * @param scope
 * @param binariesFromIndexMatches
 * @param pathRequestor
 * @param waitingPolicy
 * @param progressMonitor
 */
public static void searchAllPossibleSubTypes(
	IType type,
	IJavaSearchScope scope,
	final Map binariesFromIndexMatches,
	final IPathRequestor pathRequestor,
	int waitingPolicy,	// WaitUntilReadyToSearch | ForceImmediateSearch | CancelIfNotReadyToSearch
	final IProgressMonitor progressMonitor) {

	/* embed constructs inside arrays so as to pass them to (inner) collector */
	final Queue queue = new Queue();
	final HashtableOfObject foundSuperNames = new HashtableOfObject(5);

	IndexManager indexManager = JavaModelManager.getJavaModelManager().getIndexManager();

	/* use a special collector to collect paths and queue new subtype names */
	IndexQueryRequestor searchRequestor = new IndexQueryRequestor() {
		public boolean acceptIndexMatch(String documentPath, SearchPattern indexRecord, SearchParticipant participant, AccessRuleSet access) {
			SuperTypeReferencePattern record = (SuperTypeReferencePattern)indexRecord;
			boolean isLocalOrAnonymous = record.enclosingTypeName == IIndexConstants.ONE_ZERO;
			pathRequestor.acceptPath(documentPath, isLocalOrAnonymous);
			char[] typeName = record.simpleName;
			int suffix = documentPath.toLowerCase().lastIndexOf(SUFFIX_STRING_class);
			if (suffix != -1){ 
				HierarchyBinaryType binaryType = (HierarchyBinaryType)binariesFromIndexMatches.get(documentPath);
				if (binaryType == null){
					char[] enclosingTypeName = record.enclosingTypeName;
					if (isLocalOrAnonymous) {
						int lastSlash = documentPath.lastIndexOf('/');
						int lastDollar = documentPath.lastIndexOf('$');
						if (lastDollar == -1) {
							// malformed local or anonymous type: it doesn't contain a $ in its name
							// treat it as a top level type
							enclosingTypeName = null;
							typeName = documentPath.substring(lastSlash+1, suffix).toCharArray();
						} else {
							enclosingTypeName = documentPath.substring(lastSlash+1, lastDollar).toCharArray();
							typeName = documentPath.substring(lastDollar+1, suffix).toCharArray();
						}
					}
					binaryType = new HierarchyBinaryType(record.modifiers, record.pkgName, typeName, enclosingTypeName, record.typeParameterSignatures, record.classOrInterface);
					binariesFromIndexMatches.put(documentPath, binaryType);
				}
				binaryType.recordSuperType(record.superSimpleName, record.superQualification, record.superClassOrInterface);
			}
			if (!isLocalOrAnonymous // local or anonymous types cannot have subtypes outside the cu that define them
					&& !foundSuperNames.containsKey(typeName)){
				foundSuperNames.put(typeName, typeName);
				queue.add(typeName);
			}
			return true;
		}		
	};

	int superRefKind;
	try {
		superRefKind = type.isClass() ? SuperTypeReferencePattern.ONLY_SUPER_CLASSES : SuperTypeReferencePattern.ALL_SUPER_TYPES;
	} catch (JavaModelException e) {
		superRefKind = SuperTypeReferencePattern.ALL_SUPER_TYPES;
	}
	SuperTypeReferencePattern pattern =
		new SuperTypeReferencePattern(null, null, superRefKind, SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE);
	MatchLocator.setFocus(pattern, type);
	SubTypeSearchJob job = new SubTypeSearchJob(
		pattern, 
		new JavaSearchParticipant(), // java search only
		scope, 
		searchRequestor);

	int ticks = 0;
	queue.add(type.getElementName().toCharArray());
	try {
		while (queue.start <= queue.end) {
			if (progressMonitor != null && progressMonitor.isCanceled()) return;

			// all subclasses of OBJECT are actually all types
			char[] currentTypeName = queue.retrieve();
			if (CharOperation.equals(currentTypeName, IIndexConstants.OBJECT))
				currentTypeName = null;

			// search all index references to a given supertype
			pattern.superSimpleName = currentTypeName;
			indexManager.performConcurrentJob(job, waitingPolicy, progressMonitor == null ? null : new NullProgressMonitor() { 
				// don't report progress since this is too costly for deep hierarchies
				// just handle isCanceled() (seehttps://bugs.eclipse.org/bugs/show_bug.cgi?id=179511)
				public void setCanceled(boolean value) {
					progressMonitor.setCanceled(value);
				}
				public boolean isCanceled() {
					return progressMonitor.isCanceled();
				}
			});
			if (progressMonitor != null && ++ticks <= MAXTICKS)
				progressMonitor.worked(1);

			// in case, we search all subtypes, no need to search further
			if (currentTypeName == null) break;
		}
	} finally {
		job.finished();
	}
}
}
