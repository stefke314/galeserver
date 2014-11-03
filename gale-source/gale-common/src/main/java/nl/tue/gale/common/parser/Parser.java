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
 * Parser.java
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

import java.util.LinkedList;
import java.util.List;

/**
 * General parser that uses seperate modules allowing reuse of code in parsing
 * different languages.
 */
public class Parser {
	private ParseInfo info = null;

	public Parser() {
		info = new ParseInfo(this);
	}

	public synchronized void registerParseComponent(ParseComponent pc) {
		info.registerParseComponent(pc);
	}

	public synchronized ParseNode parse(String method, String program)
			throws ParserException {
		Scanner scanner = new Scanner(program, info);
		// build ParseList
		List<Token> tokens = new LinkedList<Token>();
		Token token;
		do {
			token = scanner.nextToken();
			tokens.add(token);
		} while (!token.getType().equals("endofprogram"));
		ParseList pl = new ParseList(tokens);
		// parse the specified method
		ParseNode result = null;
		try {
			result = info.parse(method, pl);
		} catch (ParserException e) {
			throw new ParserException(addTokenMessage(e.getMessage(),
					e.getToken(), program), e.getToken(), e);
		}
		if (pl.valid() && !pl.current().getType().equals("endofprogram")) {
			if (program.indexOf("\n") > 0)
				throw new ParserException(addTokenMessage("end of " + method
						+ " expected", pl.current(), program));
			else
				throw new ParserException("end of " + method + " expected in '"
						+ program + "'");
		}
		return result;
	}

	public synchronized Object evaluate(ParseNode node, VariableLocator vl)
			throws ParserException {
		try {
			return info.evaluate(node, vl);
		} catch (Exception e) {
			throw new ParserException(e.getMessage() + " [" + node + "]", e);
		}
	}

	private String addTokenMessage(String s, Token token, String program) {
		String res = s + " (" + program + ")";
		if (token == null)
			return res;
		if ((token.lcount == 0) && (token.ccount == 0))
			return res;
		return res + ", line " + token.lcount + " char " + token.ccount;
	}

	/*
	 * public static void main(String[] args) throws ParserException,
	 * IOException { //prepare parser GenParser parser = new GenParser();
	 * parser.registerParseComponent(new PCComment());
	 * parser.registerParseComponent(new PCLAGIdentifier());
	 * parser.registerParseComponent(new PCCommon());
	 * parser.registerParseComponent(new PCLAG());
	 * 
	 * //read input BufferedReader bin = new BufferedReader(new
	 * FileReader(args[0])); StringBuffer r = new StringBuffer(""); String s; do
	 * { s = bin.readLine(); if (s != null) r.append(s+"\n"); } while (s !=
	 * null);
	 * 
	 * //parse program ParseNode node = parser.parse("PROGRAM", r.toString());
	 * System.out.println(node); }
	 */
}