/*******************************************************************************
 * Copyright (c) 2013, 2014 GK Software AG.
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
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.core.compiler.CharOperation;

/**
 * Capture-like type variable introduced during 1.8 type inference.
 */
public class CaptureBinding18 extends CaptureBinding {
	
	TypeBinding[] upperBounds;
	private char[] originalName;

	public CaptureBinding18(ReferenceBinding contextType, char[] sourceName, char[] originalName, int position, int captureID, LookupEnvironment environment) {
		super(contextType, sourceName, position, captureID, environment);
		this.originalName = originalName;
	}
	
	public boolean setUpperBounds(TypeBinding[] upperBounds, ReferenceBinding javaLangObject) {
		this.upperBounds = upperBounds;
		if (upperBounds.length > 0)
			this.firstBound = upperBounds[0];
		int numReferenceInterfaces = 0;
		for (int i = 0; i < upperBounds.length; i++) {
			TypeBinding aBound = upperBounds[i];
			if (!aBound.isWildcard() && !aBound.isTypeVariable() && aBound.isProperType(true)) {
				// check for inconsistency between any two real types:
				for (int j = 0; j < upperBounds.length; j++) {
					if (i == j) continue;
					TypeBinding otherBound = upperBounds[j];
					if (!otherBound.isWildcard() && !otherBound.isTypeVariable() && otherBound.isProperType(true))
						if (aBound.erasure().isCompatibleWith(otherBound.erasure()))
							if (!aBound.isCompatibleWith(otherBound))
								return false;
				}
			}
			if (aBound instanceof ReferenceBinding) {
				if (this.superclass == null && aBound.isClass())
					this.superclass = (ReferenceBinding) upperBounds[i];
				else if (aBound.isInterface())
					numReferenceInterfaces++;
				// TODO: what about additional super classes?? (see isCompatibleWith)
			}
		}
		this.superInterfaces = new ReferenceBinding[numReferenceInterfaces];
		int idx = 0;
		for (int i = 0; i < upperBounds.length; i++) {
			TypeBinding aBound = upperBounds[i];
			if (aBound.isInterface())
				this.superInterfaces[idx++] = (ReferenceBinding) aBound;
		}
		if (this.superclass == null)
			this.superclass = javaLangObject;
		return true;
	}

	public void initializeBounds(Scope scope, ParameterizedTypeBinding capturedParameterizedType) {
		// nothing to initialize here (and cannot use super methods which requires wildcard to be set).
	}

	public TypeBinding clone(TypeBinding enclosingType) {
		return new CaptureBinding18(this.sourceType, this.sourceName, this.originalName, this.position, this.captureID, this.environment);
	}

	public MethodBinding[] getMethods(char[] selector) {
		if (this.upperBounds.length == 1 && this.upperBounds[0] instanceof ReferenceBinding)
			return ((ReferenceBinding)this.upperBounds[0]).getMethods(selector);
		return super.getMethods(selector);
	}

	public TypeBinding erasure() {
		if (this.upperBounds != null && this.upperBounds.length > 1) {
			ReferenceBinding[] erasures = new ReferenceBinding[this.upperBounds.length];
			for (int i = 0; i < this.upperBounds.length; i++) {
				erasures[i] = (ReferenceBinding) this.upperBounds[i].erasure(); // FIXME cast?
			}
			return new IntersectionCastTypeBinding(erasures, this.environment);
		}
		return super.erasure();
	}

	/**
	 * @see TypeBinding#isEquivalentTo(TypeBinding)
	 */
	public boolean isEquivalentTo(TypeBinding otherType) {
		// from CaptureBinding:
		if (equalsEquals(this, otherType)) return true;
		if (otherType == null) return false;
		if (this.upperBounds != null) {
			// from CaptureBinding:
			for (int i = 0; i < this.upperBounds.length; i++) {
				TypeBinding aBound = this.upperBounds[i];
				// capture of ? extends X[]
				if (aBound != null && aBound.isArrayType()) {
					if (!aBound.isCompatibleWith(otherType))
						continue;
				}
				switch (otherType.kind()) {
					case Binding.WILDCARD_TYPE :
					case Binding.INTERSECTION_TYPE :
						if (!((WildcardBinding) otherType).boundCheck(aBound))
							return false;
						break;
					default:
						return false;
				}
			}
			return true;
		}
		return false;
	}

	public boolean isCompatibleWith(TypeBinding otherType, Scope captureScope) {
		if (this.upperBounds != null) {
			for (int i = 0; i < this.upperBounds.length; i++) {
				if (this.upperBounds[i].isCompatibleWith(otherType, captureScope))
					return true;
			}
		}
		return super.isCompatibleWith(otherType, captureScope);
	}

	public TypeBinding findSuperTypeOriginatingFrom(TypeBinding otherType) {
		if (this.upperBounds != null && this.upperBounds.length > 1) {
			for (int i = 0; i < this.upperBounds.length; i++) {
				TypeBinding candidate = this.upperBounds[i].findSuperTypeOriginatingFrom(otherType);
				if (candidate != null)
					return candidate;
				// TODO: maybe we should double check about multiple candidates here,
				// but upper bounds should be consistent so hopefully the first non-null candidate is good enough. 
			}
		}
		return super.findSuperTypeOriginatingFrom(otherType);
	}

	TypeBinding substituteInferenceVariable(InferenceVariable var, TypeBinding substituteType) {
		if (this.inRecursiveFunction) return this;
		this.inRecursiveFunction = true;
		try {
			boolean haveSubstitution = false;
			ReferenceBinding currentSuperclass = this.superclass;
			if (currentSuperclass != null) {
				currentSuperclass = (ReferenceBinding) currentSuperclass.substituteInferenceVariable(var, substituteType);
				haveSubstitution |= TypeBinding.notEquals(currentSuperclass, this.superclass);
			}
			ReferenceBinding[] currentSuperInterfaces = null;
			if (this.superInterfaces != null) {
				int length = this.superInterfaces.length;
				if (haveSubstitution)
					System.arraycopy(this.superInterfaces, 0, currentSuperInterfaces=new ReferenceBinding[length], 0, length);
				for (int i = 0; i < length; i++) {
					ReferenceBinding currentSuperInterface = this.superInterfaces[i];
					if (currentSuperInterface != null) {
						currentSuperInterface = (ReferenceBinding) currentSuperInterface.substituteInferenceVariable(var, substituteType);
						if (TypeBinding.notEquals(currentSuperInterface, this.superInterfaces[i])) {
							if (currentSuperInterfaces == null)
								System.arraycopy(this.superInterfaces, 0, currentSuperInterfaces=new ReferenceBinding[length], 0, length);
							currentSuperInterfaces[i] = currentSuperInterface;
							haveSubstitution = true;
						}
					}
				}
			}
			TypeBinding[] currentUpperBounds = null;
			if (this.upperBounds != null) {
				int length = this.upperBounds.length;
				if (haveSubstitution)
					System.arraycopy(this.upperBounds, 0, currentUpperBounds=new TypeBinding[length], 0, length);
				for (int i = 0; i < length; i++) {
					TypeBinding currentBound = this.upperBounds[i];
					if (currentBound != null) {
						currentBound = currentBound.substituteInferenceVariable(var, substituteType);
						if (TypeBinding.notEquals(currentBound, this.upperBounds[i])) {
							if (currentUpperBounds == null)
								System.arraycopy(this.upperBounds, 0, currentUpperBounds=new TypeBinding[length], 0, length);
							currentUpperBounds[i] = currentBound;
							haveSubstitution = true;
						}
					}
				}
			}
			if (haveSubstitution) {
				CaptureBinding18 newCapture = (CaptureBinding18) clone(enclosingType());
				newCapture.superclass = currentSuperclass;
				newCapture.superInterfaces = currentSuperInterfaces;
				newCapture.upperBounds = currentUpperBounds;
				newCapture.tagBits = this.tagBits;
				return newCapture;
			}
			return this;
		} finally {
			this.inRecursiveFunction = false;
		}
	}

	public boolean isProperType(boolean admitCapture18) {
		return admitCapture18;
	}

	int recursionLevel = 0; // used to give a hint at recursive types without going into infinity

	public char[] genericTypeSignature() {
		// since we have no wildcard, we combine the logic from CaptureBinding plus WildcardBinding here:
		if (this.genericTypeSignature == null) {
			char[] boundSignature;
			try {
				if (this.recursionLevel++ > 0 || this.firstBound == null) {
					boundSignature = TypeConstants.WILDCARD_STAR;
				} else if (this.upperBounds != null) {
					boundSignature = CharOperation.concat(TypeConstants.WILDCARD_PLUS, this.firstBound.genericTypeSignature());
				} else if (this.lowerBound != null) {
					boundSignature = CharOperation.concat(TypeConstants.WILDCARD_MINUS, this.lowerBound.genericTypeSignature());
				} else {
					boundSignature = TypeConstants.WILDCARD_STAR;
				}
				this.genericTypeSignature = CharOperation.concat(TypeConstants.WILDCARD_CAPTURE, boundSignature);
			} finally {
				this.recursionLevel--;
			}
		}
		return this.genericTypeSignature;
	}
	
	public char[] readableName() {
		if (this.lowerBound == null && this.firstBound != null) {
			if (this.recursionLevel < 2) {
				try {
					this.recursionLevel ++;
					if (this.upperBounds != null && this.upperBounds.length > 1) {
						StringBuffer sb = new StringBuffer();
						sb.append(this.upperBounds[0].readableName());
						for (int i = 1; i < this.upperBounds.length; i++)
							sb.append('&').append(this.upperBounds[i].readableName());
						int len = sb.length();
						char[] name = new char[len];
						sb.getChars(0, len, name, 0);
						return name;
					}
					return this.firstBound.readableName();
				} finally {
					this.recursionLevel--;
				}
			} else {
				return this.originalName;
			}
		}
		return super.readableName();
	}

	public char[] shortReadableName() {
		if (this.lowerBound == null && this.firstBound != null) {
			if (this.recursionLevel < 2) {
				try {
					this.recursionLevel++;
					if (this.upperBounds != null && this.upperBounds.length > 1) {
						StringBuffer sb = new StringBuffer();
						sb.append(this.upperBounds[0].shortReadableName());
						for (int i = 1; i < this.upperBounds.length; i++)
							sb.append('&').append(this.upperBounds[i].shortReadableName());
						int len = sb.length();
						char[] name = new char[len];
						sb.getChars(0, len, name, 0);
						return name;
					}
					return this.firstBound.shortReadableName();
				} finally {
					this.recursionLevel--;
				}
			} else {
				return this.originalName;
			}
		}
		return super.shortReadableName();
	}
	
	@Override
	public TypeBinding uncapture(Scope scope) {
		return this;
	}
	@Override
	public char[] computeUniqueKey(boolean isLeaf) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(TypeConstants.CAPTURE18);
		buffer.append('{').append(this.position).append('#').append(this.captureID).append('}');
		buffer.append(';');
		int length = buffer.length();
		char[] uniqueKey = new char[length];
		buffer.getChars(0, length, uniqueKey, 0);
		return uniqueKey;
	}
}
