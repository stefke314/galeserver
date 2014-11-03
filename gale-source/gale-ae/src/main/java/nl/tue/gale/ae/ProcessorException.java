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
 * ProcessorException.java
 * Last modified: $Date$
 * In revision:   $Revision$
 * Modified by:   $Author$
 *
 * Copyright (c) 2008-2011 Eindhoven University of Technology.
 * All Rights Reserved.
 *
 * This software is proprietary information of the Eindhoven University
 * of Technology. It may be used according to the GNU LGPL license.
 */
package nl.tue.gale.ae;

/**
 * Thrown when something goes wrong while a <code>ResourceProcessor</code> is
 * processing a resource.
 * 
 * @author David Smits
 * @since 3.5
 * @version 4.0
 * @see ResourceProcessor
 */
public class ProcessorException extends Exception {
	private static final long serialVersionUID = -9023007349120904880L;

	/**
	 * Constructs a <code>ProcessorException</code> with no detail message. A
	 * detail message is a <code>String</code> that describes this particular
	 * exception.
	 */
	public ProcessorException() {
		super();
	}

	/**
	 * Constructs a <code>ProcessorException</code> with the specified detail
	 * message. A detail message is a <code>String</code> that describes this
	 * particular exception.
	 * 
	 * @param s
	 *            the <code>String</code> that contains a detailed message
	 */
	public ProcessorException(String s) {
		super(s);
	}

	/**
	 * Constructs a <code>ProcessorException</code> with the specified detail
	 * message and cause. A detail message is a <code>String</code> that
	 * describes this particular exception.
	 * 
	 * @param message
	 *            the <code>String</code> that contains a detailed message
	 * @param cause
	 *            the <code>Throwable</code> that caused this exception
	 */
	public ProcessorException(String message, Throwable cause) {
		super(message, cause);
	}
}