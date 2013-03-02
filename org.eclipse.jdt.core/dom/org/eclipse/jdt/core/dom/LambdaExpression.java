/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

import java.util.ArrayList;
import java.util.List;

/**
 * Lambda expression AST node type.
 * <pre>
 * LambdaExpression:
 *    Identifier <b>-></b> Body
 *    <b>(</b> [ Identifier { <b>,</b> Identifier } ] <b>)</b> <b>-></b> Body
 *    <b>(</b> [ FormalParameter { <b>,</b> FormalParameter } ] <b>)</b> <b>-></b> Body
 * </pre>
 * 
 *<p> 
 * First two forms use {@link VariableDeclarationFragment} while the third form
 * uses {@link SingleVariableDeclaration}.</p>
 *<p>Body can be either a {@link Block} or an {@link Expression}.</p>
 *
 * @since 3.9 
 * @noinstantiate This class is not intended to be instantiated by clients 
 */

public class LambdaExpression extends Expression {

	/**
	 * The "parentheses" structural property of this node type (child type: {@link Boolean}). 
	 */
	public static final SimplePropertyDescriptor PARENTHESES_PROPERTY =
		new SimplePropertyDescriptor(LambdaExpression.class, "parentheses", boolean.class, MANDATORY); //$NON-NLS-1$

	/**
	 * The "parameters" structural property of this node type (child type: {@link VariableDeclaration}).
	 */
	public static final ChildListPropertyDescriptor PARAMETERS_PROPERTY =
		new ChildListPropertyDescriptor(LambdaExpression.class, "parameters", VariableDeclaration.class, NO_CYCLE_RISK); //$NON-NLS-1$
	
	/**
	 * The "body" structural property of this node type (child type: {@link ASTNode}) : can
	 *  be either a {@link Block} or an {@link Expression}.
	 */
	public static final ChildPropertyDescriptor BODY_PROPERTY =
		new ChildPropertyDescriptor(LambdaExpression.class, "body", ASTNode.class, MANDATORY, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS_8_0;
	
	static {
		List propertyList = new ArrayList(4);
		createPropertyList(LambdaExpression.class, propertyList);
		addProperty(PARENTHESES_PROPERTY, propertyList);
		addProperty(PARAMETERS_PROPERTY, propertyList);
		addProperty(BODY_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS_8_0 = reapPropertyList(propertyList);
	}
	
	/**
	 * Returns a list of structural property descriptors for this node type.
	 * Clients must not modify the result.
	 *
	 * @param apiLevel the API level; one of the AST.JLS* constants
	 * @return a list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor})
	 */
	public static List propertyDescriptors(int apiLevel) {
		return PROPERTY_DESCRIPTORS_8_0;
	}

	/**
	 * Indicates the presence or absence of parentheses.
	 * defaults to true. 
	 */
	private boolean hasParentheses = true;

	/**
	 * The parameter declarations
	 * (element type: {@link VariableDeclaration}).
	 * Defaults to an empty list.
	 */
	private ASTNode.NodeList parameters =
		new ASTNode.NodeList(PARAMETERS_PROPERTY);

	/**
	 * The method body.
	 * The method body; lazily initialized, defaults to an empty Block.
	 */
	private ASTNode body = null;

	/**
	 * Creates a new AST node for a LambdaExpression declaration owned
	 * by the given AST.
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be
	 * declared in the same package; clients are unable to declare
	 * additional subclasses.
	 * </p>
	 *
	 * @param ast the AST that is to own this node
	 */
	LambdaExpression(AST ast) {
		super(ast);
		unsupportedIn2_3_4();
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	List internalStructuralPropertiesForType(int apiLevel) {
		return propertyDescriptors(apiLevel);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final boolean internalGetSetBooleanProperty(SimplePropertyDescriptor property, boolean get, boolean value) {
		if (property == PARENTHESES_PROPERTY) {
			if (get) {
				return hasParentheses();
			} else {
				setParentheses(value);
				return false;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetBooleanProperty(property, get, value);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final List internalGetChildListProperty(ChildListPropertyDescriptor property) {
		if (property == PARAMETERS_PROPERTY) {
			return parameters();
		}
		// allow default implementation to flag the error
		return super.internalGetChildListProperty(property);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
		if (property == BODY_PROPERTY) {
			if (get) {
				return getBody();
			} else {
				setBody( child);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int getNodeType0() {
		return LAMBDA_EXPRESSION;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone0(AST target) {
		LambdaExpression result = new LambdaExpression(target);
		result.setSourceRange(getStartPosition(), getLength());
		result.setParentheses(hasParentheses());
		result.parameters().addAll(ASTNode.copySubtrees(target, parameters()));
		result.setBody(ASTNode.copySubtree(target, getBody()));
		return result;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final boolean subtreeMatch0(ASTMatcher matcher, Object other) {
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
			acceptChildren(visitor, this.parameters);
			acceptChild(visitor, getBody());
		}
		visitor.endVisit(this);
	}

	/**
	 * @return <code>true</code> if this lambda expression has parentheses around
	 * its parameters and <code>false</code> otherwise
	 */
	public boolean hasParentheses() {
		return this.hasParentheses;
	}

	/**
	 * Sets whether this lambda expression should have parentheses around its parameters or not.
	 *
	 * @param hasParentheses <code>true</code> if this lambda expression should have parentheses around its parameters
	 *  and <code>false</code> otherwise. Note: Illegal value settings are silently ignored.
	 */
	public void setParentheses(boolean hasParentheses) {
		preValueChange(PARENTHESES_PROPERTY);
		this.hasParentheses = hasParentheses;
		postValueChange(PARENTHESES_PROPERTY);
	}

	/**
	 * Returns the live ordered list of formal parameters of this lambda expression.
	 *
	 * @return the live list of formal parameters of this lambda expression
	 *    (element type: {@link VariableDeclaration} 
	 */
	public List parameters() {
		return this.parameters;
	}

	/**
	 * Returns the body of this lambda expression.
	 * <p>
	 * defaults to an empty block if body is null.
	 * </p>
	 * @return the lambda expression body which can be either a {@link Block} or a {@link Expression}
	 */
	public ASTNode getBody() {
		if (this.body == null) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.body == null) {
					preLazyInit();
					this.body = new Block(this.ast);
					postLazyInit(this.body, BODY_PROPERTY);
				}
			}
		}
		return this.body;
	}

	/**
	 * Sets the body of this lambda expression.
	 *
	 * @param body a block node or an expression node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * <li> body is neither an {@link Expression} nor a {@link Block} </li>
	 * </ul>
	 */
	public void setBody(ASTNode body) {
		if (!(body instanceof Expression || body instanceof Block)) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.body;
		preReplaceChild(oldChild, body, BODY_PROPERTY);
		this.body = body;
		postReplaceChild(oldChild, body, BODY_PROPERTY);
	}

	/**
	 * Resolves and returns the binding for the lambda expression
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 *
	 * @return the binding, or <code>null</code> if the binding cannot be
	 *    resolved
	 */
	public IMethodBinding resolveMethodBinding() {
		return this.ast.getBindingResolver().resolveMethod(this);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		return BASE_NODE_SIZE + 3 * 4;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return
			memSize()
			+ (this.parameters == null ? 0 : this.parameters.listSize())
			+ (this.body == null ? 0 : getBody().treeSize());
	}
}