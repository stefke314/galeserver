/*

	This file is part of GALE (Generic Adaptation Language and Engine).

    GALE is free software: you can redistribute it and/or modify it under the 
    terms of the GNU Lesser General Public License as published by the Free 
    Software Foundation, either version 3 of the License, or (at your option) 
    any later version.

    GALE is distributed in the hope that it will be useful, but WITHOUT ANY 
    WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
    FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for 
    more details.

    You should have received a copy of the GNU Lesser General Public License
    along with GALE. If not, see <http://www.gnu.org/licenses/>.
    
 */
/**
 * ParserException.java
 * Last modified: $Date$
 * In revision:   $Revision$
 * Modified by:   $Author: dsmits $
 *
 * Copyright (c) 2008-2011 Eindhoven University of Technology.
 * All Rights Reserved.
 *
 * This software is proprietary information of the Eindhoven University
 * of Technology. It may be used according to the GNU LGPL license.
 */
package nl.tue.gale.common.parser;

/**
 * Thrown when an error occurs while scanning, parsing or evaluating an
 * expression.
 * 
 * @author David Smits
 * @version 3.5.1
 */
public class ParserException extends Exception {
	private static final long serialVersionUID = 3550498823549789989L;
	private Token token = null;

	/**
	 * Constructs a <code>ParserException</code> with no detail message. A
	 * detail message is a <code>String</code> that describes this particular
	 * exception.
	 */
	public ParserException() {
		super();
	}

	/**
	 * Constructs a <code>ParserException</code> with the specified detail
	 * message. A detail message is a <code>String</code> that describes this
	 * particular exception.
	 * 
	 * @param s
	 *            the <code>String</code> that contains a detailed message
	 */
	public ParserException(String s) {
		super(s);
	}

	/**
	 * Constructs a <code>ParserException</code> with the specified detail
	 * message and the token where the exception was thrown. A detail message is
	 * a <code>String</code> that describes this particular exception.
	 * 
	 * @param s
	 *            the <code>String</code> that contains a detailed message
	 * @param token
	 *            the <code>Token</code> that caused the exception to be thrown
	 */
	public ParserException(String s, Token token) {
		super(s);
		this.token = token;
	}

	/**
	 * Constructs a <code>ParserException</code> with the specified detail
	 * message and cause. A detail message is a <code>String</code> that
	 * describes this particular exception.
	 * 
	 * @param message
	 *            the <code>String</code> that contains a detailed message
	 * @param cause
	 *            the <code>Throwable</code> that caused this exception
	 */
	public ParserException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a <code>ParserException</code> with the specified detail
	 * message, the token where the exception was thrown and the cause. A detail
	 * message is a <code>String</code> that describes this particular
	 * exception.
	 * 
	 * @param s
	 *            the <code>String</code> that contains a detailed message
	 * @param token
	 *            the <code>Token</code> that caused the exception to be thrown
	 * @param cause
	 *            the <code>Throwable</code> that caused this exception
	 */
	public ParserException(String s, Token token, Throwable cause) {
		super(s, cause);
		this.token = token;
	}

	/**
	 * Returns the token that caused this exception. This will return <code>
	 * null</code> if the exception was not caused by a particular token.
	 * 
	 * @return the token caused this exception
	 */
	public Token getToken() {
		return token;
	}
}
