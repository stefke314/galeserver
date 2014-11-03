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
 * Scanner.java
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
 * General scanner that uses seperate modules allowing reuse of code in scanning
 * different languages.
 */
public class Scanner {
	// a reference to the parse string
	private ParseString expr;

	private ParseInfo info = null;

	/**
	 * Creates a new general lexical scanner using the specified expression.
	 * 
	 * @param expr
	 *            the expression to scan
	 * @param info
	 *            a reference to the object containing information about the
	 *            current parser (cannot be null)
	 */
	protected Scanner(String expr, ParseInfo info) {
		this.expr = new ParseString(expr);
		if (info == null)
			throw new NullPointerException();
		this.info = info;
	}

	/**
	 * Returns the next token in the expression.
	 * 
	 * @return The next token.
	 */
	protected Token nextToken() throws ParserException {
		char ch;
		do {
			ch = expr.nextChar();
		} while ((Character.isWhitespace(ch)) && (ch != '\f'));
		expr.returnChar(ch);
		Token result = null;
		if (ch == '\f')
			return new Token("endofprogram");
		int lcount = expr.lineCount();
		int ccount = expr.charCount();
		try {
			result = info.scan(expr);
		} catch (Exception e) {
			result = null;
		}
		if (result == null)
			throw new ParserException("Error scanning: line "
					+ expr.lineCount() + " char " + expr.charCount());
		result.lcount = lcount;
		result.ccount = ccount;
		return result;
	}
}