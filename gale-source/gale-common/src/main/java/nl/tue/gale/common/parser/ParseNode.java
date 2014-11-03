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
 * ParseNode.java
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

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Represents a single node in the parse tree.
 */
public abstract class ParseNode implements Cloneable {
	protected ParseNode parent = null;

	/**
	 * Creates a new parse node with the specified parent. Parent may be null if
	 * this is the root node in the tree.
	 */
	public ParseNode(ParseNode parent) {
		this.parent = parent;
	}

	public abstract List<ParseNode> getChildList();

	public ParseNode getParent() {
		return parent;
	}

	public void setParent(ParseNode parent) {
		this.parent = parent;
	}

	public String toString() {
		final Map<String, String> map = null;
		return toString(map);
	}

	public String toString(String options) {
		return toString(decodeOptions(options));
	}

	public String toString(Map<String, String> options) {
		String result = "{";
		for (ParseNode pn : getChildList())
			result += pn.toString(options);
		result += "}";
		return result;
	}

	public abstract String getType();

	public abstract Object get(String key);

	public abstract Object clone();

	public abstract int hashCode();

	public abstract boolean equals(Object object);

	private static Map<String, String> decodeOptions(String options) {
		if (options == null)
			return null;
		final Map<String, String> result = new Hashtable<String, String>();
		for (final String o : options.split(";")) {
			if (o.indexOf(":") <= 0)
				continue;
			result.put(o.substring(0, o.indexOf(":")),
					o.substring(o.indexOf(":") + 1));
		}
		return result;
	}
}