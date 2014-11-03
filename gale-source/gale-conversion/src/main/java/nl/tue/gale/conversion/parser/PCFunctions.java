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
 * PCFunctions.java
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
 * This parse component contains parsing methods for some string functions.
 */
public class PCFunctions implements ParseComponent {
	public PCFunctions() {
	}

	public String[] getParseMethods() {
		return new String[] { "substring" };
	}

	public Token scan(ParseString ps, Token token) throws ParserException {
		return token;
	}

	public ParseNode parse(String method, ParseList pl, ParseInfo info)
			throws ParserException {
		if ("substring".equals(method))
			return FUNC(pl, info, 3);
		throw new ParserException("method '" + method
				+ "' is not handled by PCFunctions");
	}

	public static class FunctionNode extends ParseNode implements Cloneable {
		public String name = null;
		public List<ParseNode> params = new LinkedList<ParseNode>();

		public FunctionNode(ParseNode parent) {
			super(parent);
		}

		public List<ParseNode> getChildList() {
			return params;
		}

		public Object get(String key) {
			if (key.equals("name"))
				return name;
			if (key.equals("params"))
				return params;
			return null;
		}

		public String toString(Map<String, String> options) {
			StringBuffer result = new StringBuffer();
			result.append(name + "(");
			for (int i = 0; i < params.size(); i++) {
				result.append(params.get(i).toString(options));
				if (i != params.size() - 1)
					result.append(", ");
			}
			result.append(")");
			return result.toString();
		}

		public String getType() {
			return "function";
		}

		public Object clone() {
			FunctionNode result = new FunctionNode(parent);
			result.name = name;
			for (ParseNode node : params)
				result.params.add((ParseNode) node.clone());
			return result;
		}

		public int hashCode() {
			int result = 0;
			if (name != null)
				result += name.hashCode();
			result += params.hashCode();
			return result;
		}

		public boolean equals(Object object) {
			if (object == null)
				return false;
			if (!(object instanceof FunctionNode))
				return false;
			FunctionNode node = (FunctionNode) object;
			if (name == null)
				return false;
			if (!name.equals(node.name))
				return false;
			return (params.equals(node.params));
		}
	}

	private ParseNode FUNC(ParseList pl, ParseInfo info, int params)
			throws ParserException {
		Token token = pl.current();
		if (!token.getType().equals("id"))
			throw new ParserException("identifier expected");
		FunctionNode result = new FunctionNode(null);
		result.name = (String) token.get("name");
		pl.moveNext();
		token = pl.current();
		if (!token.getType().equals("lbrack"))
			throw new ParserException("'(' expected");
		pl.moveNext();
		for (int i = 0; i < params; i++) {
			ParseNode expr = info.parse("EXPR", pl);
			result.params.add(expr);
			if (i != params - 1) {
				token = pl.current();
				pl.moveNext();
				if (!token.getType().equals("comma"))
					throw new ParserException("',' expected");
			}
		}
		token = pl.current();
		if (!token.getType().equals("rbrack"))
			throw new ParserException("')' expected");
		pl.moveNext();
		return result;
	}

	/**
	 * Evaluates the specified ParseNode handled by this component.
	 */
	public Object evaluate(ParseNode node, ParseInfo info, VariableLocator vl)
			throws ParserException {
		if (node instanceof FunctionNode) {
			if ("substring".equals(node.get("name")))
				return func_substring(node, info, vl);
			throw new ParserException("undefined function '" + node.get("name")
					+ "'");
		}
		throw new ParserException("'" + node + "' not handled by PCFunctions");
	}

	private Object func_substring(ParseNode node, ParseInfo info,
			VariableLocator vl) throws ParserException {
		Object ostr = info.evaluate((ParseNode) node.getChildList().get(0), vl);
		Object ostart = info.evaluate((ParseNode) node.getChildList().get(1),
				vl);
		Object ostop = info
				.evaluate((ParseNode) node.getChildList().get(2), vl);
		String str = null;
		if (ostr instanceof String)
			str = (String) ostr;
		else
			throw new ParserException("string expected");
		Float fstart = null;
		if (ostart instanceof Float)
			fstart = (Float) ostart;
		else
			throw new ParserException("float expected");
		Float fstop = null;
		if (ostop instanceof Float)
			fstop = (Float) ostop;
		else
			throw new ParserException("float expected");
		int start = fstart.intValue();
		int stop = fstop.intValue();
		return str.substring(start, stop);
	}
}