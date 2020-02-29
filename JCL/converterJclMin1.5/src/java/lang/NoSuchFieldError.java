/*******************************************************************************
 * Copyright (c) 2020 GK Software SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package java.lang;
public class NoSuchFieldError extends IncompatibleClassChangeError {
  public NoSuchFieldError() { super(); }
  public NoSuchFieldError(String s) {super(s);}
}
