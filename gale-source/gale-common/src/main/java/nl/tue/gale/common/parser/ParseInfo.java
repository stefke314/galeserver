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
 * ParseInfo.java
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
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * This class contains information about the current general parser. It contains
 * a list of all registered parse components and all associated recursive decent
 * parsing methods.
 */
public class ParseInfo {
	private Parser parser = null;

	public ParseInfo(Parser parser) {
		this.parser = parser;
	}

	public Parser getParser() {
		return parser;
	}

	private List<ParseComponent> parsecomponents = new LinkedList<ParseComponent>();
	private Hashtable<String, ParseComponent> parsemethods = new Hashtable<String, ParseComponent>();
	private Hashtable<Class<? extends ParseNode>, ParseComponent> parsenodecomponents = new Hashtable<Class<? extends ParseNode>, ParseComponent>();

	@SuppressWarnings("unchecked")
	public void registerParseComponent(ParseComponent pc) {
		String[] methods = pc.getParseMethods();
		for (int i = 0; i < methods.length; i++) {
			parsemethods.put(methods[i], pc);
		}
		Class<?>[] sc = pc.getClass().getClasses();
		for (Class<?> c : sc)
			if (ParseNode.class.isAssignableFrom(c))
				parsenodecomponents.put((Class<? extends ParseNode>) c, pc);
		parsecomponents.add(pc);
	}

	public List<ParseComponent> getParseComponents() {
		return parsecomponents;
	}

	/**
	 * Scans the string to see if the first token in the string is handled by
	 * any registered component.
	 */
	public Token scan(ParseString ps) throws ParserException {
		Token token = null;
		ListIterator<ParseComponent> li = parsecomponents.listIterator(0);
		while (li.hasNext()) {
			ParseComponent pc = li.next();
			token = pc.scan(ps, token);
		}
		return token;
	}

	/**
	 * Tries to find the specified recursive decent parsing method to handle the
	 * first tokens in the list.
	 */
	public ParseNode parse(String method, ParseList pl) throws ParserException {
		ParseComponent pc = parsemethods.get(method);
		if (pc == null)
			throw new ParserException("no such method '" + method + "'");
		return pc.parse(method, pl, this);
	}

	/**
	 * Tries to evaluate the specified <code>ParseNode</code>.
	 */
	public Object evaluate(ParseNode node, VariableLocator vl)
			throws ParserException {
		ParseComponent pc = parsenodecomponents.get(node.getClass());
		if (pc == null)
			throw new ParserException("no evaluator for class '"
					+ node.getClass() + "'");
		return pc.evaluate(node, this, vl);
	}

	/**
	 * Returns wether the specified parse method exists.
	 */
	public boolean existsMethod(String method) {
		return parsemethods.containsKey(method);
	}
}