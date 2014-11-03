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
 * PCComment.java
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
 * This parse component has only got a scanning part. It filters out comments
 * from the source program. Place it first in the list of parse components.
 */
public class PCComment implements ParseComponent {
	public PCComment() {
	}

	public String[] getParseMethods() {
		return new String[0];
	}

	/**
	 * Scans the string to see if the first token in the string is handled by
	 * this component.
	 */
	public Token scan(ParseString ps, Token token) throws ParserException {
		char ch = ps.nextChar();
		boolean comment;
		do {
			comment = false;
			if (ch == '/') {
				ch = ps.nextChar();
				if (ch == '/') {
					// comment
					comment = true;
					do {
						ch = ps.nextChar();
					} while (ch != '\n');
					do {
						ch = ps.nextChar();
					} while ((Character.isWhitespace(ch)) && (ch != '\f'));
				} else {
					// not a comment
					ps.returnChar(ch);
					ps.returnChar('/');
				}
			} else {
				// not a comment
				ps.returnChar(ch);
			}
		} while (comment);
		return null;
	}

	/**
	 * Uses the specified method to parse the next tokens in the list. In this
	 * class there are no parsing methods.
	 */
	public ParseNode parse(String method, ParseList pl, ParseInfo info)
			throws ParserException {
		throw new ParserException("method '" + method
				+ "' is not handled by PCLAG");
	}

	/**
	 * Evaluates the specified ParseNode handled by this component.
	 */
	public Object evaluate(ParseNode node, ParseInfo info, VariableLocator vl)
			throws ParserException {
		throw new ParserException("'" + node + "' not handled by PCComment");
	}
}