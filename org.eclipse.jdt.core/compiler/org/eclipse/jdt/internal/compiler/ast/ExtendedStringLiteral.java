package org.eclipse.jdt.internal.compiler.ast;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;

public class ExtendedStringLiteral extends StringLiteral {

	private static final int INIT_SIZE = 30;

	/** 
	 *  Build a string+char literal
	 */
	public ExtendedStringLiteral(StringLiteral str, CharLiteral character) {

		super(str.source, str.sourceStart, str.sourceEnd);
		extendWith(character);
	}

	/**	
	 * Build a two-strings literal
	 * */
	public ExtendedStringLiteral(StringLiteral str1, StringLiteral str2) {

		super(str1.source, str1.sourceStart, str1.sourceEnd);
		extendWith(str2);
	}

	/**
	 * Add the lit source to mine, just as if it was mine
	 */
	public ExtendedStringLiteral extendWith(CharLiteral lit) {

		//update the source
		int length = source.length;
		System.arraycopy(source, 0, (source = new char[length + 1]), 0, length);
		source[length] = lit.value;
		//position at the end of all literals
		sourceEnd = lit.sourceEnd;
		return this;
	}

	/**
	 *  Add the lit source to mine, just as if it was mine
	 */
	public ExtendedStringLiteral extendWith(StringLiteral lit) {

		//uddate the source
		int length = source.length;
		System.arraycopy(
			source,
			0,
			source = new char[length + lit.source.length],
			0,
			length);
		System.arraycopy(lit.source, 0, source, length, lit.source.length);
		//position at the end of all literals
		sourceEnd = lit.sourceEnd;
		return this;
	}

	public String toStringExpression() {

		String str = "ExtendedStringLiteral{" + new String(source) + "}";	//$NON-NLS-2$ //$NON-NLS-1$
		return str;
	}

	public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {

		visitor.visit(this, scope);
		visitor.endVisit(this, scope);
	}
}