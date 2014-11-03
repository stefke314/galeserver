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
 * PCIdentifier.java
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
package nl.tue.gale.conversion.parser;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import nl.tue.gale.common.parser.ParseComponent;
import nl.tue.gale.common.parser.ParseInfo;
import nl.tue.gale.common.parser.ParseList;
import nl.tue.gale.common.parser.ParseNode;
import nl.tue.gale.common.parser.ParseString;
import nl.tue.gale.common.parser.ParserException;
import nl.tue.gale.common.parser.Token;
import nl.tue.gale.common.parser.VariableLocator;

/**
 * This parse components contains scanning and parsing methods for identifiers
 * that can contain periods.
 */
public class PCIdentifier implements ParseComponent {
	public PCIdentifier() {
	}

	public String[] getParseMethods() {
		return new String[] { "VARP" };
	}

	/**
	 * Scans the string to see if the first token in the string is handled by
	 * this component.
	 */
	public Token scan(ParseString ps, Token token) throws ParserException {
		if (token != null)
			return token; // if a token is already found, do nothing with it
		char ch = ps.nextChar();
		if (Character.isJavaIdentifierStart(ch) || (ch == '$')) {
			Token result = new Token("id");
			StringBuffer idname = new StringBuffer();
			while ((Character.isJavaIdentifierPart(ch)) || (ch == '.')
					|| (ch == '$')) {
				idname.append(ch);
				ch = ps.nextChar();
			}
			ps.returnChar(ch);
			result.add("name", idname.toString());
			return result;
		} else {
			// not an identifier
			ps.returnChar(ch);
			return null;
		}
	}

	/**
	 * Uses the specified method to parse the next tokens in the list. In this
	 * class there are no parsing methods.
	 */
	public ParseNode parse(String method, ParseList pl, ParseInfo info)
			throws ParserException {
		if ("VARP".equals(method))
			return VARP(pl, info);
		throw new ParserException("method '" + method
				+ "' is not handled by PCIdentifier");
	}

	public static class IDNode extends ParseNode implements Cloneable {
		public String name = null;

		public IDNode(ParseNode parent) {
			super(parent);
		}

		public List<ParseNode> getChildList() {
			return new LinkedList<ParseNode>();
		}

		public Object get(String key) {
			if (key.equals("name"))
				return name;
			return null;
		}

		public String toString(Map<String, String> options) {
			if ((options != null) && ("true".equals(options.get("var-encap"))))
				return "${" + name + "}";
			return name;
		}

		public String getType() {
			return "id";
		}

		public Object clone() {
			IDNode result = new IDNode(parent);
			result.name = name;
			return result;
		}

		public int hashCode() {
			return (name == null ? 0 : name.hashCode());
		}

		public boolean equals(Object object) {
			if (object == null)
				return false;
			if (!(object instanceof IDNode))
				return false;
			IDNode node = (IDNode) object;
			return (name == null ? node.name == null : name.equals(node.name));
		}
	}

	private ParseNode VARP(ParseList pl, ParseInfo info) throws ParserException {
		Token token = pl.current();
		if (!token.getType().equals("id"))
			throw new ParserException("identifier expected", token);
		IDNode result = new IDNode(null);
		result.name = (String) token.get("name");
		pl.moveNext();
		return result;
	}

	/**
	 * Evaluates the specified ParseNode handled by this component.
	 */
	public Object evaluate(ParseNode node, ParseInfo info, VariableLocator vl)
			throws ParserException {
		if (node instanceof IDNode)
			return vl.getVariableValue((String) node.get("name"),
					info.getParser());
		throw new ParserException("'" + node + "' not handled by PCIdentifier");
	}
}