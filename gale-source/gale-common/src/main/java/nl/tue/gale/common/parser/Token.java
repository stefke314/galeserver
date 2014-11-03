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
 * Token.java
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

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a token in the general parser environment.
 */
public class Token {
	private String type = null;
	private Map<String, Object> props = new HashMap<String, Object>();

	// used in error reporting
	protected int lcount = 0;
	protected int ccount = 0;

	/**
	 * Creates a new token of the specified type. To avoid confusion types
	 * should be uniquely identifyable by their 'type' string.
	 * 
	 * @param type
	 *            a string identifying the type of this token
	 */
	public Token(String type) {
		this.type = type;
	}

	/**
	 * Returns a string identifying the type of this token.
	 * 
	 * @return the type of this token
	 */
	public String getType() {
		return type;
	}

	/**
	 * Adds a property to this token. For instance the name of an identifier if
	 * this token represents an identifier.
	 * 
	 * @param key
	 *            the name of the property
	 * @param o
	 *            the value of the property
	 */
	public void add(String key, Object o) {
		props.put(key, o);
	}

	/**
	 * Returns a property of this token. If the property does not exist,
	 * <code>null</code> is returned.
	 * 
	 * @param key
	 *            the name of the property
	 * @return the value of the property or <code>null</code> if it could not be
	 *         found
	 */
	public Object get(String key) {
		return props.get(key);
	}

	public String toString() {
		return "<" + type + ", " + props + ">";
	}
}