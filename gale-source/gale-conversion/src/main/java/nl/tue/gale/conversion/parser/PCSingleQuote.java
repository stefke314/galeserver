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
 * PCSingleQuote.java
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

import nl.tue.gale.common.parser.ParseComponent;
import nl.tue.gale.common.parser.ParseInfo;
import nl.tue.gale.common.parser.ParseList;
import nl.tue.gale.common.parser.ParseNode;
import nl.tue.gale.common.parser.ParseString;
import nl.tue.gale.common.parser.ParserException;
import nl.tue.gale.common.parser.Token;
import nl.tue.gale.common.parser.VariableLocator;

/**
 * This parse component implements the single quote string constant.
 */
public class PCSingleQuote implements ParseComponent {
	public PCSingleQuote() {
	}

	public String[] getParseMethods() {
		return new String[] {};
	}

	/**
	 * Scans the string to see if the first token in the string is handled by
	 * this component.
	 */
	public Token scan(ParseString ps, Token token) throws ParserException {
		if (token != null) {
			// a token is already found, so handle it here
			return token;
		}
		// no token is found yet
		char ch = ps.nextChar();
		Token result = null;
		if (ch == '\'') {
			StringBuffer aconst = new StringBuffer();
			char ach = ps.nextChar();
			while (ach != '\'') {
				aconst.append(ach);
				ach = ps.nextChar();
			}
			result = new Token("const");
			result.add("value", aconst.toString());
		} else
			ps.returnChar(ch);
		return result;
	}

	/**
	 * Uses the specified method to parse the next tokens in the list.
	 */
	public ParseNode parse(String method, ParseList pl, ParseInfo info)
			throws ParserException {
		throw new ParserException("method '" + method
				+ "' is not handled by PCSingleQuote");
	}

	/**
	 * Evaluates the specified ParseNode handled by this component.
	 */
	public Object evaluate(ParseNode node, ParseInfo info, VariableLocator vl)
			throws ParserException {
		throw new ParserException("'" + node + "' not handled by PCSingleQuote");
	}
}