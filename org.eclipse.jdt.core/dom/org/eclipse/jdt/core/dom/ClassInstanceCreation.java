/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.dom;

import java.util.ArrayList;
import java.util.List;

/**
 * Class instance creation expression AST node type.
 * For JLS2:
 * <pre>
 * ClassInstanceCreation:
 *        [ Expression <b>.</b> ] <b>new</b> Name
 *            <b>(</b> [ Expression { <b>,</b> Expression } ] <b>)</b>
 *            [ AnonymousClassDeclaration ]
 * </pre>
 * For JLS3, type arguments are added
 * and the type name is generalized to a type so that parameterized
 * types can be instantiated:
 * <pre>
 * ClassInstanceCreation:
 *        [ Expression <b>.</b> ]
 *            <b>new</b> [ <b>&lt;</b> Type { <b>,</b> Type } <b>&gt;</b> ]
 *            Type <b>(</b> [ Expression { <b>,</b> Expression } ] <b>)</b>
 *            [ AnonymousClassDeclaration ]
 * </pre>
 * <p>
 * Not all node arragements will represent legal Java constructs. In particular,
 * it is nonsense if the type is a primitive type or an array type (primitive
 * types cannot be instantiated, and array creations must be represented with
 * <code>ArrayCreation</code> nodes). The normal use is when the type is a
 * simple, qualified, or parameterized type.
 * </p>
 * <p>
 * A type like "A.B" can be represented either of two ways:
 * <ol>
 * <li>
 * <code>QualifiedType(SimpleType(SimpleName("A")),SimpleName("B"))</code>
 * </li>
 * <li>
 * <code>SimpleType(QualifiedName(SimpleName("A"),SimpleName("B")))</code>
 * </li>
 * </ol>
 * The first form is preferred when "A" is known to be a type (as opposed
 * to a package). However, a parser cannot always determine this. Clients
 * should be prepared to handle either rather than make assumptions.
 * (Note also that the first form became possible as of JLS3; only the second
 * form existed in JLS2.)
 * </p>
 * 
 * @since 2.0
 */
public class ClassInstanceCreation extends Expression {

	/**
	 * The "typeArguments" structural property of this node type (added in JLS3 API).
	 * @since 3.0
	 */
	public static final ChildListPropertyDescriptor TYPE_ARGUMENTS_PROPERTY = 
		new ChildListPropertyDescriptor(ClassInstanceCreation.class, "typeArguments", Type.class, NO_CYCLE_RISK); //$NON-NLS-1$
	
	/**
	 * The "expression" structural property of this node type.
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor EXPRESSION_PROPERTY = 
		new ChildPropertyDescriptor(ClassInstanceCreation.class, "expression", Expression.class, OPTIONAL, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "name" structural property of this node type (JLS2 API only).
	 * @since 3.0
	 */
	// TODO (jeem) When JLS3 support is complete (post 3.0) - deprecated Replaced by {@link #TYPE_PROPERTY} in the JLS3 API.
	public static final ChildPropertyDescriptor NAME_PROPERTY = 
		new ChildPropertyDescriptor(ClassInstanceCreation.class, "name", Name.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "type" structural property of this node type (added in JLS3 API).
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor TYPE_PROPERTY = 
		new ChildPropertyDescriptor(ClassInstanceCreation.class, "type", Type.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "arguments" structural property of this node type.
	 * @since 3.0
	 */
	public static final ChildListPropertyDescriptor ARGUMENTS_PROPERTY = 
		new ChildListPropertyDescriptor(ClassInstanceCreation.class, "arguments", Expression.class, CYCLE_RISK); //$NON-NLS-1$
	
	/**
	 * The "anonymousClassDeclaration" structural property of this node type.
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor ANONYMOUS_CLASS_DECLARATION_PROPERTY = 
		new ChildPropertyDescriptor(ClassInstanceCreation.class, "anonymousClassDeclaration", AnonymousClassDeclaration.class, OPTIONAL, CYCLE_RISK); //$NON-NLS-1$
	
	/**
	 * A list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 * @since 3.0
	 */
	private static final List PROPERTY_DESCRIPTORS_2_0;
	
	/**
	 * A list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 * @since 3.0
	 */
	private static final List PROPERTY_DESCRIPTORS_3_0;
	
	static {
		List properyList = new ArrayList(5);
		createPropertyList(ClassInstanceCreation.class, properyList);
		addProperty(EXPRESSION_PROPERTY, properyList);
		addProperty(NAME_PROPERTY, properyList);
		addProperty(ARGUMENTS_PROPERTY, properyList);
		addProperty(ANONYMOUS_CLASS_DECLARATION_PROPERTY, properyList);
		PROPERTY_DESCRIPTORS_2_0 = reapPropertyList(properyList);
		
		properyList = new ArrayList(6);
		createPropertyList(ClassInstanceCreation.class, properyList);
		addProperty(EXPRESSION_PROPERTY, properyList);
		addProperty(TYPE_ARGUMENTS_PROPERTY, properyList);
		addProperty(TYPE_PROPERTY, properyList);
		addProperty(ARGUMENTS_PROPERTY, properyList);
		addProperty(ANONYMOUS_CLASS_DECLARATION_PROPERTY, properyList);
		PROPERTY_DESCRIPTORS_3_0 = reapPropertyList(properyList);
	}

	/**
	 * Returns a list of structural property descriptors for this node type.
	 * Clients must not modify the result.
	 * 
	 * @param apiLevel the API level; one of the
	 * <code>AST.JLS&ast;</code> constants

	 * @return a list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor})
	 * @since 3.0
	 */
	public static List propertyDescriptors(int apiLevel) {
		if (apiLevel == AST.JLS2) {
			return PROPERTY_DESCRIPTORS_2_0;
		} else {
			return PROPERTY_DESCRIPTORS_3_0;
		}
	}
			
	/**
	 * The optional expression; <code>null</code> for none; defaults to none.
	 */
	private Expression optionalExpression = null;
	
	/**
	 * The type arguments (element type: <code>Type</code>). 
	 * Null in JLS2. Added in JLS3; defaults to an empty list
	 * (see constructor).
	 * @since 3.0
	 */
	private ASTNode.NodeList typeArguments = null;

	/**
	 * The type name; lazily initialized; defaults to a unspecified,
	 * legal type name. Not used in JLS3.
	 */
	private Name typeName = null;
	
	/**
	 * The type; lazily initialized; defaults to a unspecified type.
	 * @since 3.0
	 */
	private Type type = null;
	
	/**
	 * The list of argument expressions (element type: 
	 * <code>Expression</code>). Defaults to an empty list.
	 */
	private ASTNode.NodeList arguments =
		new ASTNode.NodeList(ARGUMENTS_PROPERTY);
		
	/**
	 * The optional anonymous class declaration; <code>null</code> for none; 
	 * defaults to none.
	 */
	private AnonymousClassDeclaration optionalAnonymousClassDeclaration = null;
	
	/**
	 * Creates a new AST node for a class instance creation expression owned 
	 * by the given AST. By default, there is no qualifying expression,
	 * an empty list of type parameters, an unspecified type, an empty
     * list of arguments, and does not declare an anonymous class.
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be 
	 * declared in the same package; clients are unable to declare 
	 * additional subclasses.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	ClassInstanceCreation (AST ast) {
		super(ast);
		if (ast.apiLevel >= AST.JLS3) {
			this.typeArguments = new ASTNode.NodeList(TYPE_ARGUMENTS_PROPERTY);
		}
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 * @since 3.0
	 */
	final List internalStructuralPropertiesForType(int apiLevel) {
		return propertyDescriptors(apiLevel);
	}
	

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
		if (property == EXPRESSION_PROPERTY) {
			if (get) {
				return getExpression();
			} else {
				setExpression((Expression) child);
				return null;
			}
		}
		if (property == NAME_PROPERTY) {
			if (get) {
				return getName();
			} else {
				setName((Name) child);
				return null;
			}
		}
		if (property == TYPE_PROPERTY) {
			if (get) {
				return getType();
			} else {
				setType((Type) child);
				return null;
			}
		}
		if (property == ANONYMOUS_CLASS_DECLARATION_PROPERTY) {
			if (get) {
				return getAnonymousClassDeclaration();
			} else {
				setAnonymousClassDeclaration((AnonymousClassDeclaration) child);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final List internalGetChildListProperty(ChildListPropertyDescriptor property) {
		if (property == ARGUMENTS_PROPERTY) {
			return arguments();
		}
		if (property == TYPE_ARGUMENTS_PROPERTY) {
			return typeArguments();
		}
		// allow default implementation to flag the error
		return super.internalGetChildListProperty(property);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final int getNodeType0() {
		return CLASS_INSTANCE_CREATION;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone0(AST target) {
		ClassInstanceCreation result = new ClassInstanceCreation(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setExpression(
			(Expression) ASTNode.copySubtree(target, getExpression()));
		if (this.ast.apiLevel == AST.JLS2) {
			result.setName((Name) getName().clone(target));
		}
		if (this.ast.apiLevel >= AST.JLS3) {
			result.typeArguments().addAll(ASTNode.copySubtrees(target, typeArguments()));
			result.setType((Type) getType().clone(target));
		}
		result.arguments().addAll(ASTNode.copySubtrees(target, arguments()));
		result.setAnonymousClassDeclaration(
			(AnonymousClassDeclaration) 
			   ASTNode.copySubtree(target, getAnonymousClassDeclaration()));
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
			acceptChild(visitor, getExpression());
			if (this.ast.apiLevel == AST.JLS2) {
				acceptChild(visitor, getName());
			}
			if (this.ast.apiLevel >= AST.JLS3) {
				acceptChildren(visitor, this.typeArguments);
				acceptChild(visitor, getType());
			}
			acceptChildren(visitor, this.arguments);
			acceptChild(visitor, getAnonymousClassDeclaration());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the expression of this class instance creation expression, or 
	 * <code>null</code> if there is none.
	 * 
	 * @return the expression node, or <code>null</code> if there is none
	 */ 
	public Expression getExpression() {
		return this.optionalExpression;
	}
	
	/**
	 * Sets or clears the expression of this class instance creation expression.
	 * 
	 * @param expression the expression node, or <code>null</code> if 
	 *    there is none
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setExpression(Expression expression) {
		// a ClassInstanceCreation may occur inside an Expression
		// must check cycles
		ASTNode oldChild = this.optionalExpression;
		preReplaceChild(oldChild, expression, EXPRESSION_PROPERTY);
		this.optionalExpression = expression;
		postReplaceChild(oldChild, expression, EXPRESSION_PROPERTY);
	}

	/**
	 * Returns the live ordered list of type arguments of this class
	 * instance creation (added in JLS3 API).
	 * <p>
	 * Note: This API element is only needed for dealing with Java code that uses
	 * new language features of J2SE 1.5. It is included in anticipation of J2SE
	 * 1.5 support, which is planned for the next release of Eclipse after 3.0, and
	 * may change slightly before reaching its final form.
	 * </p>
	 * 
	 * @return the live list of type arguments
	 *    (element type: <code>Type</code>)
	 * @exception UnsupportedOperationException if this operation is used in
	 * a JLS2 AST
	 * @since 3.0
	 */ 
	public List typeArguments() {
		// more efficient than just calling unsupportedIn2() to check
		if (this.typeArguments == null) {
			unsupportedIn2();
		}
		return this.typeArguments;
	}
	
    /**
	 * Returns the name of the type instantiated in this class instance 
	 * creation expression (JLS2 API only).
	 * 
	 * @return the type name node
	 * @exception UnsupportedOperationException if this operation is used in
	 * an AST later than JLS2
	 */ 
	// TODO (jeem) When JLS3 support is complete (post 3.0) - deprecated In the JLS3 API, this method is replaced by <code>getType</code>, which returns a <code>Type</code> instead of a <code>Name</code>.
	public Name getName() {
	    supportedOnlyIn2();
		if (this.typeName == null) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.typeName == null) {
					preLazyInit();
					this.typeName = new SimpleName(this.ast);
					postLazyInit(this.typeName, NAME_PROPERTY);
				}
			}
		}
		return typeName;
	}
	
	/**
	 * Sets the name of the type instantiated in this class instance 
	 * creation expression (JLS2 API only).
	 * 
	 * @param name the new type name
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>`
	 * </ul>
	 * @exception UnsupportedOperationException if this operation is used in
	 * an AST later than JLS2
	 */ 
	// TODO (jeem) When JLS3 support is complete (post 3.0) - deprecated In the JLS3 API, this method is replaced by <code>setType</code>, which expects a <code>Type</code> instead of a <code>Name</code>.
	public void setName(Name name) {
	    supportedOnlyIn2();
		if (name == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.typeName;
		preReplaceChild(oldChild, name, NAME_PROPERTY);
		this.typeName = name;
		postReplaceChild(oldChild, name, NAME_PROPERTY);
	}

	/**
	 * Returns the type instantiated in this class instance creation
	 * expression (added in JLS3 API).
	 * 
	 * @return the type node
	 * @exception UnsupportedOperationException if this operation is used in
	 * a JLS2 AST
	 * @since 3.0
	 */ 
	public Type getType() {
	    unsupportedIn2();
		if (this.type == null) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.type == null) {
					preLazyInit();
					this.type = new SimpleType(this.ast);
					postLazyInit(this.type, TYPE_PROPERTY);
				}
			}
		}
		return this.type;
	}
	
	/**
	 * Sets the type instantiated in this class instance creation
	 * expression (added in JLS3 API).
	 * 
	 * @param type the new type
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>`
	 * </ul>
	 * @exception UnsupportedOperationException if this operation is used in
	 * a JLS2 AST
	 * @since 3.0
	 */ 
	public void setType(Type type) {
	    unsupportedIn2();
		if (type == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.type;
		preReplaceChild(oldChild, type, TYPE_PROPERTY);
		this.type = type;
		postReplaceChild(oldChild, type, TYPE_PROPERTY);
	}

	/**
	 * Returns the live ordered list of argument expressions in this class
	 * instance creation expression.
	 * 
	 * @return the live list of argument expressions (possibly empty)
	 *    (element type: <code>Expression</code>)
	 */ 
	public List arguments() {
		return this.arguments;
	}
	
	/**
	 * Returns the anonymous class declaration introduced by this
	 * class instance creation expression, if it has one.
	 * 
	 * @return the anonymous class declaration, or <code>null</code> if none
	 */ 
	public AnonymousClassDeclaration getAnonymousClassDeclaration() {
		return this.optionalAnonymousClassDeclaration;
	}
	
	/**
	 * Sets whether this class instance creation expression declares
	 * an anonymous class (that is, has class body declarations).
	 * 
	 * @param decl the anonymous class declaration, or <code>null</code> 
	 *    if none
	 */ 
	public void setAnonymousClassDeclaration(AnonymousClassDeclaration decl) {
		ASTNode oldChild = this.optionalAnonymousClassDeclaration;
		preReplaceChild(oldChild, decl, ANONYMOUS_CLASS_DECLARATION_PROPERTY);
		this.optionalAnonymousClassDeclaration = decl;
		postReplaceChild(oldChild, decl, ANONYMOUS_CLASS_DECLARATION_PROPERTY);
	}

	/**
	 * Resolves and returns the binding for the constructor invoked by this
	 * expression. For anonymous classes, the binding is that of the anonymous
	 * constructor.
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 * 
	 * @return the constructor binding, or <code>null</code> if the binding
	 *    cannot be resolved
	 */	
	public IMethodBinding resolveConstructorBinding() {
		return this.ast.getBindingResolver().resolveConstructor(this);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		// treat Code as free
		return BASE_NODE_SIZE + 6 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		// n.b. type == null for ast.API_LEVEL == JLS2
		// n.b. typeArguments == null for ast.API_LEVEL == JLS2
		// n.b. typeName == null for ast.API_LEVEL >= JLS3
		return 
			memSize()
			+ (this.typeName == null ? 0 : getName().treeSize())
			+ (this.type == null ? 0 : getType().treeSize())
			+ (this.optionalExpression == null ? 0 : getExpression().treeSize())
			+ (this.typeArguments == null ? 0 : this.typeArguments.listSize())
			+ (this.arguments == null ? 0 : this.arguments.listSize())
			+ (this.optionalAnonymousClassDeclaration == null ? 0 : getAnonymousClassDeclaration().treeSize());
	}
}

