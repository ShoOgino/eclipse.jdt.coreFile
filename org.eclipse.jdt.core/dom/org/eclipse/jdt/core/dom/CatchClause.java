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

/**
 * Catch clause AST node type.
 *
 * <pre>
 * CatchClause:
 * 			<b>catch</b> <b>(</b> FormalParameter <b>)</b> Block
 * </pre>
 * 
 * @since 2.0
 */
public class CatchClause extends ASTNode {
	
	/**
	 * The "exception" structural property of this node type.
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor EXCEPTION_PROPERTY = 
		new ChildPropertyDescriptor(CatchClause.class, "exception", SingleVariableDeclaration.class, MANDATORY, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "body" structural property of this node type.
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor BODY_PROPERTY = 
		new ChildPropertyDescriptor(CatchClause.class, "body", Block.class, MANDATORY, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * A list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS;
	
	static {
		createPropertyList(CatchClause.class);
		addProperty(EXCEPTION_PROPERTY);
		addProperty(BODY_PROPERTY);
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
	 * The body; lazily initialized; defaults to an empty block.
	 */
	private Block body = null;

	/**
	 * The exception variable declaration; lazily initialized; defaults to a
	 * unspecified, but legal, variable declaration.
	 */
	private SingleVariableDeclaration exceptionDecl = null;

	/**
	 * Creates a new AST node for a catch clause owned by the given 
	 * AST. By default, the catch clause declares an unspecified, but legal, 
	 * exception declaration and has an empty block.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	CatchClause(AST ast) {
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
		if (property == EXCEPTION_PROPERTY) {
			if (get) {
				return getException();
			} else {
				setException((SingleVariableDeclaration) child);
				return null;
			}
		}
		if (property == BODY_PROPERTY) {
			if (get) {
				return getBody();
			} else {
				setBody((Block) child);
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
		return CATCH_CLAUSE;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		CatchClause result = new CatchClause(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setBody((Block) getBody().clone(target));
		result.setException(
			(SingleVariableDeclaration) ASTNode.copySubtree(target, getException()));
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
			// visit children in normal left to right reading order
			acceptChild(visitor, getException());
			acceptChild(visitor, getBody());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the exception variable declaration of this catch clause.
	 * 
	 * @return the exception variable declaration node
	 */ 
	public SingleVariableDeclaration getException() {
		if (this.exceptionDecl == null) {
			preLazyInit();
			this.exceptionDecl = new SingleVariableDeclaration(this.ast);
			postLazyInit(this.exceptionDecl, EXCEPTION_PROPERTY);
		}
		return this.exceptionDecl;
	}
		
	/**
	 * Sets the variable declaration of this catch clause.
	 * 
	 * @param exception the exception variable declaration node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setException(SingleVariableDeclaration exception) {
		if (exception == null) {
			throw new IllegalArgumentException();
		}
		preReplaceChild(this.exceptionDecl, exception, EXCEPTION_PROPERTY);
		this.exceptionDecl= exception;
		postReplaceChild(this.exceptionDecl, exception, EXCEPTION_PROPERTY);
	}
	
	/**
	 * Returns the body of this catch clause.
	 * 
	 * @return the catch clause body
	 */ 
	public Block getBody() {
		if (this.body == null) {
			preLazyInit();
			this.body = new Block(this.ast);
			postLazyInit(this.body, BODY_PROPERTY);
		}
		return this.body;
	}
	
	/**
	 * Sets the body of this catch clause.
	 * 
	 * @param body the catch clause block node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setBody(Block body) {
		if (body == null) {
			throw new IllegalArgumentException();
		}
		preReplaceChild(this.body, body, BODY_PROPERTY);
		this.body = body;
		postReplaceChild(this.body, body, BODY_PROPERTY);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		// treat Code as free
		return BASE_NODE_SIZE + 2 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return 
			memSize()
			+ (this.exceptionDecl == null ? 0 : getException().treeSize())
			+ (this.body == null ? 0 : getBody().treeSize());
	}
}
