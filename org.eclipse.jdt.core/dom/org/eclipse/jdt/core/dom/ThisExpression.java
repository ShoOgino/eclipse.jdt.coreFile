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

package org.eclipse.jdt.core.dom;

import java.util.List;

import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;

/**
 * Simple or qualified "this" AST node type.
 *
 * <pre>
 * ThisExpression:
 *     [ ClassName <b>.</b> ] <b>this</b>
 * </pre>
 * <p>
 * See <code>FieldAccess</code> for guidelines on handling other expressions
 * that resemble qualified names.
 * </p>
 * 
 * @see FieldAccess
 * @since 2.0
 */
public class ThisExpression extends Expression {
			
	/**
	 * The "qualifier" structural property of this node type.
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor QUALIFIER_PROPERTY = 
		new ChildPropertyDescriptor(ThisExpression.class, "qualifier", Name.class, OPTIONAL, NO_CYCLE_RISK); //$NON-NLS-1$

	/**
	 * A list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS;
	
	static {
		createPropertyList(ThisExpression.class);
		addProperty(QUALIFIER_PROPERTY);
		PROPERTY_DESCRIPTORS = reapPropertyList();
	}

	/**
	 * Returns a list of structural property descriptors for this node type.
	 * Clients must not modify the result.
	 * 
	 * @param apiLevel the API level; one of the
	 * <code>AST.LEVEL_*</code>LEVEL
	 * @return a list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor})
	 * @since 3.0
	 */
	public static List propertyDescriptors(int apiLevel) {
		return PROPERTY_DESCRIPTORS;
	}
			
	/**
	 * The optional qualifier; <code>null</code> for none; defaults to none.
	 */
	private Name optionalQualifier = null;

	/**
	 * Creates a new AST node for a "this" expression owned by the 
	 * given AST. By default, there is no qualifier.
	 * 
	 * @param ast the AST that is to own this node
	 */
	ThisExpression(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final List internalStructuralPropertiesForType(int apiLevel) {
		return propertyDescriptors(apiLevel);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
		if (property == QUALIFIER_PROPERTY) {
			if (get) {
				return getQualifier();
			} else {
				setQualifier((Name) child);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return THIS_EXPRESSION;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		ThisExpression result = new ThisExpression(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setQualifier((Name) ASTNode.copySubtree(target, getQualifier()));
		return result;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public boolean subtreeMatch(ASTMatcher matcher, Object other) {
		// dispatch to correct overloaded match method
		return matcher.match(this, other);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			acceptChild(visitor, getQualifier());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the qualifier of this "this" expression, or 
	 * <code>null</code> if there is none.
	 * 
	 * @return the qualifier name node, or <code>null</code> if there is none
	 */ 
	public Name getQualifier() {
		return this.optionalQualifier;
	}
	
	/**
	 * Sets or clears the qualifier of this "this" expression.
	 * 
	 * @param name the qualifier name node, or <code>null</code> if 
	 *    there is none
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setQualifier(Name name) {
		preReplaceChild(this.optionalQualifier, name, QUALIFIER_PROPERTY);
		this.optionalQualifier = name;
		postReplaceChild(this.optionalQualifier, name, QUALIFIER_PROPERTY);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		// treat Operator as free
		return BASE_NODE_SIZE + 1 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return 
			memSize()
			+ (this.optionalQualifier == null ? 0 : getQualifier().treeSize());
	}

	/**
	 * Internal method used by ASTConverter.
	 * 
	 * @return the block scope
	 */
	BlockScope lookupScope() {
		// TBD (olivier) - this method should be moved to ASTConverter
		ASTNode currentNode = this;
		while(currentNode != null
			&&!(currentNode instanceof MethodDeclaration)
			&& !(currentNode instanceof Initializer)
			&& !(currentNode instanceof FieldDeclaration)) {
			currentNode = currentNode.getParent();
		}
		if (currentNode == null) {
			return null;
		}
		if (currentNode instanceof Initializer) {
			Initializer initializer = (Initializer) currentNode;
			while(!(currentNode instanceof TypeDeclaration)) {
				currentNode = currentNode.getParent();
			}
			org.eclipse.jdt.internal.compiler.ast.TypeDeclaration typeDecl = (org.eclipse.jdt.internal.compiler.ast.TypeDeclaration) this.ast.getBindingResolver().getCorrespondingNode(currentNode);
			if ((initializer.getModifiers() & Modifier.STATIC) != 0) {
				return typeDecl.staticInitializerScope;
			} else {
				return typeDecl.initializerScope;
			}
		} else if (currentNode instanceof FieldDeclaration) {
			FieldDeclaration fieldDeclaration = (FieldDeclaration) currentNode;
			while(!(currentNode instanceof TypeDeclaration)) {
				currentNode = currentNode.getParent();
			}
			org.eclipse.jdt.internal.compiler.ast.TypeDeclaration typeDecl = (org.eclipse.jdt.internal.compiler.ast.TypeDeclaration) this.ast.getBindingResolver().getCorrespondingNode(currentNode);
			if ((fieldDeclaration.getModifiers() & Modifier.STATIC) != 0) {
				return typeDecl.staticInitializerScope;
			} else {
				return typeDecl.initializerScope;
			}
		}
		AbstractMethodDeclaration abstractMethodDeclaration = (AbstractMethodDeclaration) this.ast.getBindingResolver().getCorrespondingNode(currentNode);
		return abstractMethodDeclaration.scope;
	}	
}
