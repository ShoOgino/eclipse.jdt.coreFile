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
package org.eclipse.jdt.internal.codeassist.impl;

import java.util.Map;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.codeassist.ISearchableNameEnvironment;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.env.*;

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.impl.*;

public abstract class Engine implements ITypeRequestor {

	public LookupEnvironment lookupEnvironment;
	
	protected CompilationUnitScope unitScope;
	protected ISearchableNameEnvironment nameEnvironment;

	public AssistOptions options;
	public CompilerOptions compilerOptions; 
	
	public Engine(Map settings){
		this.options = new AssistOptions(settings);
		this.compilerOptions = new CompilerOptions(settings);
	}
	
	/**
	 * Add an additional binary type
	 */
	public void accept(IBinaryType binaryType, PackageBinding packageBinding, AccessRestriction accessRestriction) {
		lookupEnvironment.createBinaryTypeFrom(binaryType, packageBinding, accessRestriction);
	}

	/**
	 * Add an additional compilation unit.
	 */
	public void accept(ICompilationUnit sourceUnit, AccessRestriction accessRestriction) {
		CompilationResult result = new CompilationResult(sourceUnit, 1, 1, this.compilerOptions.maxProblemsPerUnit);
		CompilationUnitDeclaration parsedUnit =
			this.getParser().dietParse(sourceUnit, result);

		lookupEnvironment.buildTypeBindings(parsedUnit, accessRestriction);
		lookupEnvironment.completeTypeBindings(parsedUnit, true);
	}

	/**
	 * Add additional source types (the first one is the requested type, the rest is formed by the
	 * secondary types defined in the same compilation unit).
	 */
	public void accept(ISourceType[] sourceTypes, PackageBinding packageBinding, AccessRestriction accessRestriction) {
		CompilationResult result =
			new CompilationResult(sourceTypes[0].getFileName(), 1, 1, this.compilerOptions.maxProblemsPerUnit);
		CompilationUnitDeclaration unit =
			SourceTypeConverter.buildCompilationUnit(
				sourceTypes,//sourceTypes[0] is always toplevel here
				SourceTypeConverter.FIELD_AND_METHOD // need field and methods
				| SourceTypeConverter.MEMBER_TYPE, // need member types
				// no need for field initialization
				lookupEnvironment.problemReporter,
				result);

		if (unit != null) {
			lookupEnvironment.buildTypeBindings(unit, accessRestriction);
			lookupEnvironment.completeTypeBindings(unit, true);
		}
	}

	public abstract AssistParser getParser();
	
	protected boolean mustQualifyType(
		char[] packageName,
		char[] typeName) {

		// If there are no types defined into the current CU yet.
		if (unitScope == null)
			return true;
			
		char[][] compoundPackageName = CharOperation.splitOn('.', packageName);
		char[] readableTypeName = CharOperation.concat(packageName, typeName, '.');

		if (CharOperation.equals(unitScope.fPackage.compoundName, compoundPackageName))
			return false;

		ImportBinding[] imports = unitScope.imports;
		if (imports != null){
			for (int i = 0, length = imports.length; i < length; i++) {
				if (imports[i].onDemand) {
					if (CharOperation.equals(imports[i].compoundName, compoundPackageName)) {
						for (int j = 0; j < imports.length; j++) {
							if(i != j){
								if(imports[j].onDemand) {
									if(nameEnvironment.findType(typeName, imports[j].compoundName) != null){
										return true;
									}
								} else {
									if(CharOperation.equals(CharOperation.lastSegment(imports[j].readableName(), '.'), typeName)
										&& !CharOperation.equals(imports[j].compoundName, CharOperation.splitOn('.', readableTypeName))) {
										return true;	
									}
								}
							}
						}
						return false; // how do you match p1.p2.A.* ?
					}
	
				} else
	
					if (CharOperation.equals(imports[i].readableName(), readableTypeName)) {
						return false;
					}
			}
		}
		return true;
	}

	/*
	 * Find the node (a field, a method or an initializer) at the given position 
	 * and parse its block statements if it is a method or an initializer.
	 * Returns the node or null if not found
	 */
	protected ASTNode parseBlockStatements(CompilationUnitDeclaration unit, int position) {
		int length = unit.types.length;
		for (int i = 0; i < length; i++) {
			TypeDeclaration type = unit.types[i];
			if (type.declarationSourceStart < position
				&& type.declarationSourceEnd >= position) {
				getParser().scanner.setSource(unit.compilationResult);
				return parseBlockStatements(type, unit, position);
			}
		}
		return null;
	}

	private ASTNode parseBlockStatements(
		TypeDeclaration type,
		CompilationUnitDeclaration unit,
		int position) {
		//members
		TypeDeclaration[] memberTypes = type.memberTypes;
		if (memberTypes != null) {
			int length = memberTypes.length;
			for (int i = 0; i < length; i++) {
				TypeDeclaration memberType = memberTypes[i];
				if (memberType.bodyStart > position)
					continue;
				if (memberType.declarationSourceEnd >= position) {
					return parseBlockStatements(memberType, unit, position);
				}
			}
		}
		//methods
		AbstractMethodDeclaration[] methods = type.methods;
		if (methods != null) {
			int length = methods.length;
			for (int i = 0; i < length; i++) {
				AbstractMethodDeclaration method = methods[i];
				if (method.bodyStart > position)
					continue;
				if (method.declarationSourceEnd >= position) {
					getParser().parseBlockStatements(method, unit);
					return method;
				}
			}
		}
		//initializers
		FieldDeclaration[] fields = type.fields;
		if (fields != null) {
			int length = fields.length;
			for (int i = 0; i < length; i++) {
				FieldDeclaration field = fields[i];
				if (field.sourceStart > position)
					continue;
				if (field.declarationSourceEnd >= position) {
					if (field instanceof Initializer) {
						getParser().parseBlockStatements((Initializer)field, type, unit);
					}
					return field;
				}
			}
		}
		return null;
	}

	protected void reset() {
		lookupEnvironment.reset();
	}
	
	public static String getSignature(Binding binding) {
		StringBuffer buffer = new StringBuffer();
		appendSignature(binding, buffer);
		return buffer.toString();
	}
	
	private static void appendSignature(Binding binding, StringBuffer sig) {
		switch(binding.bindingType()) {
			case BindingIds.TYPE:
				if(binding instanceof BaseTypeBinding) {
					BaseTypeBinding baseTypeBinding = (BaseTypeBinding) binding;
					sig.append(baseTypeBinding.constantPoolName());
				} else if(binding instanceof TypeVariableBinding) {
					TypeVariableBinding typeVariableBinding = (TypeVariableBinding) binding;
					sig.append(CharOperation.concat('T', typeVariableBinding.sourceName(), ';'));
				} else if(binding instanceof ParameterizedTypeBinding) {
					ParameterizedTypeBinding parameterizedTypeBinding = (ParameterizedTypeBinding) binding;
					if (parameterizedTypeBinding.isMemberType() && parameterizedTypeBinding.enclosingType().isParameterizedType()) {
						String tmpSig = getSignature(parameterizedTypeBinding.enclosingType());
						sig.append(tmpSig.substring(0, tmpSig.length()-1));
						sig.append('.');
						sig.append(parameterizedTypeBinding.sourceName());
					} else {
						String tmpSig = getSignature(parameterizedTypeBinding.type);
						sig.append(tmpSig.substring(0, tmpSig.length()-1));
					}	   	    
					if (parameterizedTypeBinding.arguments != null) {
					    sig.append('<');
					    for (int i = 0, length = parameterizedTypeBinding.arguments.length; i < length; i++) {
					        appendSignature(parameterizedTypeBinding.arguments[i], sig);
					    }
					    sig.append('>'); //$NON-NLS-1$
					}
					sig.append(';');
				} else {
					TypeBinding typeBinding = (TypeBinding) binding;
					sig.append('L');
					char[] qualifiedPackageName = typeBinding.qualifiedPackageName();
					if(qualifiedPackageName != null && qualifiedPackageName.length > 0) {
						sig.append(typeBinding.qualifiedPackageName());
						sig.append('.');
					}
					sig.append(CharOperation.replaceOnCopy(typeBinding.qualifiedSourceName(), '.', '$'));
					sig.append(';');
				}
				break;
		}
	}
}
