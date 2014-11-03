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
 * PCStatement.java
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
 * This parse component parses the assignment statement and concatenation of
 * statements.
 */
public class PCStatement implements ParseComponent {
	public String[] getParseMethods() {
		return new String[] { "STAT", "STATS" };
	}

	/**
	 * Scans the string to see if the first token in the string is handled by
	 * this component.
	 */
	public Token scan(ParseString ps, Token token) throws ParserException {
		return token;
	}

	/**
	 * Uses the specified method to parse the next tokens in the list.
	 */
	public ParseNode parse(String method, ParseList pl, ParseInfo info)
			throws ParserException {
		if ("STAT".equals(method))
			return STAT(pl, info);
		if ("STATS".equals(method))
			return STATS(pl, info);
		throw new ParserException("method '" + method
				+ "' is not handled by PCStatement");
	}

	public static class StatsNode extends ParseNode implements Cloneable {
		public List<ParseNode> stats = new LinkedList<ParseNode>();

		public StatsNode(ParseNode parent) {
			super(parent);
		}

		public List<ParseNode> getChildList() {
			return stats;
		}

		public Object get(String key) {
			if (key.equals("stats"))
				return stats;
			return null;
		}

		public String toString(Map<String, String> options) {
			StringBuffer result = new StringBuffer();
			for (ParseNode node : stats)
				result.append(node.toString(options) + "\n");
			return result.toString();
		}

		public String getType() {
			return "stats";
		}

		public Object clone() {
			StatsNode result = new StatsNode(parent);
			for (ParseNode node : stats)
				result.stats.add((ParseNode) node.clone());
			return result;
		}

		public int hashCode() {
			int result = 0;
			if (stats != null)
				result += stats.hashCode();
			return result;
		}

		public boolean equals(Object object) {
			if (object == null)
				return false;
			if (!(object instanceof StatsNode))
				return false;
			StatsNode node = (StatsNode) object;
			boolean result = true;
			result = result
					&& (stats == null ? node.stats == null : stats
							.equals(node.stats));
			return result;
		}
	}

	public static class AssignNode extends ParseNode implements Cloneable {
		public ParseNode variable = null;
		public ParseNode expr = null;

		public AssignNode(ParseNode parent) {
			super(parent);
		}

		public List<ParseNode> getChildList() {
			List<ParseNode> result = new LinkedList<ParseNode>();
			result.add(variable);
			result.add(expr);
			return result;
		}

		public Object get(String key) {
			if (key.equals("variable"))
				return variable;
			if (key.equals("expr"))
				return expr;
			return null;
		}

		public String toString(Map<String, String> options) {
			return variable.toString(options) + " = " + expr.toString(options);
		}

		public String getType() {
			return "assign";
		}

		public Object clone() {
			AssignNode result = new AssignNode(parent);
			if (variable != null)
				result.variable = (ParseNode) variable.clone();
			if (expr != null)
				result.expr = (ParseNode) expr.clone();
			return result;
		}

		public int hashCode() {
			int result = 0;
			if (variable != null)
				result += variable.hashCode();
			if (expr != null)
				result += expr.hashCode();
			return result;
		}

		public boolean equals(Object object) {
			if (object == null)
				return false;
			if (!(object instanceof AssignNode))
				return false;
			AssignNode node = (AssignNode) object;
			boolean result = true;
			result = result
					&& (variable == null ? node.variable == null : variable
							.equals(node.variable));
			result = result
					&& (expr == null ? node.expr == null : expr
							.equals(node.expr));
			return result;
		}
	}

	private ParseNode STATS(ParseList pl, ParseInfo info)
			throws ParserException {
		StatsNode result = new StatsNode(null);
		do {
			ParseNode stat = info.parse("STAT", pl);
			result.stats.add(stat);
			stat.setParent(result);
		} while (!pl.current().getType().equals("endofprogram"));
		return result;
	}

	private ParseNode STAT(ParseList pl, ParseInfo info) throws ParserException {
		Token token = pl.current();
		AssignNode result = new AssignNode(null);
		result.variable = info.parse("VARP", pl);
		token = pl.current();
		result.variable.setParent(result);
		if (!token.getType().equals("assign"))
			throw new ParserException("'=' expected", token);
		pl.moveNext();
		result.expr = info.parse("EXPR", pl);
		token = pl.current();
		result.expr.setParent(result);
		if (!token.getType().equals("semicol"))
			throw new ParserException("';' expected", token);
		pl.moveNext();
		return result;
	}

	/**
	 * Evaluates the specified ParseNode handled by this component.
	 */
	public Object evaluate(ParseNode node, ParseInfo info, VariableLocator vl)
			throws ParserException {
		throw new ParserException("operation not allowed");
	}
}